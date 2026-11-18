package com.jsnws.server.common;

import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 离线 IP 归属地解析（ip2region xdb）。
 * xdb 文件随包打包在 resources 下，启动时一次性加载到内存，Searcher 基于全量内存索引，线程安全。
 * 客户端为本机/内网地址时，改用服务器公网出口 IP 解析（出口 IP 进程内缓存）。
 */
@Component
public class IpLocationUtil {

    private static final Logger log = LoggerFactory.getLogger(IpLocationUtil.class);
    private static final String UNKNOWN = "未知";

    /** 公网出口 IP 查询服务，按顺序尝试；均为纯文本/文本内包含 IP，超时 2s 连接 + 3s 读取 */
    private static final String[] EGRESS_IP_SERVICES = {
            "https://myip.ipip.net",
            "https://ifconfig.me/ip"
    };
    private static final Pattern IPV4_PATTERN = Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{1,3}){3})\\b");
    /** 出口 IP 成功缓存 6 小时，失败缓存 5 分钟，避免每次内网留言都外呼 */
    private static final long EGRESS_SUCCESS_TTL = Duration.ofHours(6).toMillis();
    private static final long EGRESS_FAILURE_TTL = Duration.ofMinutes(5).toMillis();

    private Searcher searcher;

    private final Object egressLock = new Object();
    private volatile String cachedEgressIp;
    private volatile long egressCachedAt;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @PostConstruct
    public void init() {
        try (java.io.InputStream in = new ClassPathResource("ip2region.xdb").getInputStream()) {
            byte[] buffer = in.readAllBytes();
            this.searcher = Searcher.newWithBuffer(buffer);
            log.info("ip2region 离线库加载成功，大小 {} 字节", buffer.length);
        } catch (Exception e) {
            this.searcher = null;
            log.error("ip2region 离线库加载失败，归属地统一返回「未知」", e);
        }
    }

    /**
     * 解析归属地，输出「省份·城市·运营商」，能取到的部分才拼接。
     * 本机/内网/保留地址改用服务器公网出口 IP 解析；IPv6、外呼失败、解析异常均如实返回「未知」。
     */
    public String resolve(String ip) {
        if (searcher == null) {
            return UNKNOWN;
        }
        String target = ip == null ? "" : ip.trim();
        if (target.isEmpty() || "unknown".equalsIgnoreCase(target) || isInternalAddress(target)) {
            target = egressIp();
        }
        if (target == null) {
            return UNKNOWN;
        }
        try {
            // 返回格式：国家|区域|省份|城市|运营商，占位为 0
            String region = searcher.search(target);
            if (region == null || region.isEmpty()) {
                return UNKNOWN;
            }
            String[] parts = region.split("\\|", -1);
            List<String> values = new ArrayList<>();
            addIfPresent(values, parts, 2);
            addIfPresent(values, parts, 3);
            addIfPresent(values, parts, 4);
            if (values.isEmpty()) {
                return UNKNOWN;
            }
            String location = String.join("·", values);
            log.debug("IP 归属地解析成功，查询 IP={}，结果={}", target, location);
            return location;
        } catch (Exception e) {
            log.warn("IP 归属地解析失败，ip={}，降级为「未知」", target, e);
            return UNKNOWN;
        }
    }

    /**
     * 获取服务器公网出口 IP，带进程内缓存：
     * 成功缓存 6 小时；失败/取到非公网地址缓存 5 分钟，期间直接返回 null，避免频繁外呼。
     */
    private String egressIp() {
        long now = System.currentTimeMillis();
        long age = now - egressCachedAt;
        if (egressCachedAt > 0) {
            String cached = cachedEgressIp;
            if (cached != null && age < EGRESS_SUCCESS_TTL) {
                return cached;
            }
            if (cached == null && age < EGRESS_FAILURE_TTL) {
                return null;
            }
        }
        synchronized (egressLock) {
            now = System.currentTimeMillis();
            age = now - egressCachedAt;
            if (egressCachedAt > 0) {
                if (cachedEgressIp != null && age < EGRESS_SUCCESS_TTL) {
                    return cachedEgressIp;
                }
                if (cachedEgressIp == null && age < EGRESS_FAILURE_TTL) {
                    return null;
                }
            }
            String ip = fetchEgressIp();
            cachedEgressIp = ip;
            egressCachedAt = now;
            return ip;
        }
    }

    /** 依次请求公网 IP 服务，取响应文本中第一个合法公网 IPv4 */
    private String fetchEgressIp() {
        for (String service : EGRESS_IP_SERVICES) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(service))
                        .timeout(Duration.ofSeconds(3))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    log.warn("公网 IP 服务 {} 返回状态码 {}", service, response.statusCode());
                    continue;
                }
                Matcher matcher = IPV4_PATTERN.matcher(response.body());
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    if (isPublicAddress(ip)) {
                        log.info("获取服务器公网出口 IP 成功：{}（来源 {}）", ip, service);
                        return ip;
                    }
                    log.warn("公网 IP 服务 {} 返回非公网地址：{}", service, ip);
                }
            } catch (Exception e) {
                log.warn("请求公网 IP 服务 {} 失败：{}", service, e.getMessage());
            }
        }
        log.warn("未能获取服务器公网出口 IP，内网留言归属地降级为「未知」");
        return null;
    }

    private boolean isPublicAddress(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return !(address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()
                    || address.isAnyLocalAddress()
                    || address.isMulticastAddress());
        } catch (Exception e) {
            return false;
        }
    }

    private void addIfPresent(List<String> values, String[] parts, int index) {
        if (index < parts.length) {
            String value = parts[index] == null ? "" : parts[index].trim();
            if (!value.isEmpty() && !"0".equals(value)) {
                values.add(value);
            }
        }
    }

    /** 本机、内网、链路本地、保留/组播地址不做归属地解析 */
    private boolean isInternalAddress(String ip) {
        if (!ip.contains(".")) {
            // ip2region 仅支持 IPv4，IPv6 统一视为无法解析
            return true;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()
                    || address.isAnyLocalAddress()
                    || address.isMulticastAddress();
        } catch (Exception e) {
            return true;
        }
    }
}
