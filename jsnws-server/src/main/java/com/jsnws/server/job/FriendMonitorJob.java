package com.jsnws.server.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jsnws.server.entity.Friend;
import com.jsnws.server.mapper.FriendMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 友链监测任务：响应时间 + 可用状态、RSS 最近更新。
 * <p>
 * 只探测公网 http(s) 地址；单个站点异常只影响自身，不中断整轮。
 * 探测失败不写任何估算值：响应时间保留上一次成功值，可用状态按“首次失败时间 + 阈值”流转。
 */
@Component
public class FriendMonitorJob {

    private static final Logger log = LoggerFactory.getLogger(FriendMonitorJob.class);
    private static final Pattern IPV4 = Pattern.compile("^172\\.(\\d+)\\..*");

    private final FriendMapper friendMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Value("${jsnws.friend-probe.enabled:true}")
    private boolean probeEnabled;
    @Value("${jsnws.friend-probe.timeout-ms:5000}")
    private long probeTimeoutMs;
    @Value("${jsnws.friend-probe.offline-threshold-ms:1800000}")
    private long offlineThresholdMs;

    @Value("${jsnws.friend-rss.enabled:true}")
    private boolean rssEnabled;
    @Value("${jsnws.friend-rss.timeout-ms:8000}")
    private long rssTimeoutMs;

    public FriendMonitorJob(FriendMapper friendMapper) {
        this.friendMapper = friendMapper;
    }

    @Scheduled(initialDelayString = "${jsnws.friend-probe.initial-delay-ms:60000}",
            fixedDelayString = "${jsnws.friend-probe.interval-ms:1800000}")
    public void scheduledProbe() {
        if (!probeEnabled) {
            return;
        }
        try {
            probeAll();
        } catch (Exception e) {
            log.error("友链可用性探测任务异常：{}", e.getMessage());
        }
    }

    @Scheduled(initialDelayString = "${jsnws.friend-rss.initial-delay-ms:120000}",
            fixedDelayString = "${jsnws.friend-rss.interval-ms:7200000}")
    public void scheduledRss() {
        if (!rssEnabled) {
            return;
        }
        try {
            refreshAllRss();
        } catch (Exception e) {
            log.error("友链 RSS 任务异常：{}", e.getMessage());
        }
    }

    /** 后台手动触发，返回本轮统计（不影响定时任务） */
    public synchronized Map<String, Object> runNow() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (probeEnabled) {
            result.put("probe", probeAll());
        } else {
            result.put("probe", "disabled");
        }
        if (rssEnabled) {
            result.put("rss", refreshAllRss());
        } else {
            result.put("rss", "disabled");
        }
        return result;
    }

    /** 探测所有已通过友链：真实 GET 首包耗时，失败不写耗时、只记录失败起点 */
    public synchronized Map<String, Integer> probeAll() {
        List<Friend> friends = friendMapper.selectList(new QueryWrapper<Friend>().eq("status", 1));
        Map<String, Integer> stat = new LinkedHashMap<>();
        stat.put("checked", 0);
        stat.put("ok", 0);
        stat.put("failed", 0);
        stat.put("skipped", 0);
        for (Friend friend : friends) {
            if (!isSafeHttpUrl(friend.getUrl())) {
                stat.put("skipped", stat.get("skipped") + 1);
                continue;
            }
            // 人工标记为违规的友链不做自动流转
            if (friend.getHealthStatus() != null && friend.getHealthStatus() == 2) {
                stat.put("skipped", stat.get("skipped") + 1);
                continue;
            }
            stat.put("checked", stat.get("checked") + 1);
            long begin = System.nanoTime();
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(friend.getUrl().trim()))
                        .timeout(Duration.ofMillis(probeTimeoutMs))
                        .header("User-Agent", "Mozilla/5.0 (compatible; jsnws-friend-probe)")
                        .GET()
                        .build();
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                long cost = (System.nanoTime() - begin) / 1_000_000L;
                try (InputStream body = response.body()) {
                    // 只取响应头，读完即关，避免下载整页
                } catch (Exception ignored) {
                }
                friendMapper.update(null, new UpdateWrapper<Friend>()
                        .eq("id", friend.getId())
                        .set("latency_ms", (int) Math.min(cost, 99999L))
                        .set("latency_checked_at", new Date())
                        .set("health_status", 0)
                        .set("offline_since", null));
                stat.put("ok", stat.get("ok") + 1);
            } catch (Exception e) {
                stat.put("failed", stat.get("failed") + 1);
                markOffline(friend, e);
            }
        }
        log.info("友链探测完成：共 {} 条，成功 {}，失败 {}，跳过 {}", stat.get("checked"), stat.get("ok"),
                stat.get("failed"), stat.get("skipped"));
        return stat;
    }

    /** 失败流转：首次失败只记起点；超过阈值仍未恢复才标记“短暂失联”；成功时自动恢复 */
    private void markOffline(Friend friend, Exception e) {
        Date now = new Date();
        Date since = friend.getOfflineSince();
        if (since == null) {
            friendMapper.update(null, new UpdateWrapper<Friend>()
                    .eq("id", friend.getId())
                    .set("offline_since", now));
            log.warn("友链「{}」探测失败，记录失联起点：{}", friend.getTitle(), e.getMessage());
            return;
        }
        if (now.getTime() - since.getTime() >= offlineThresholdMs
                && (friend.getHealthStatus() == null || friend.getHealthStatus() == 0)) {
            friendMapper.update(null, new UpdateWrapper<Friend>()
                    .eq("id", friend.getId())
                    .set("health_status", 1));
            log.warn("友链「{}」持续失联超过 {} 分钟，标记为短暂失联", friend.getTitle(), offlineThresholdMs / 60000);
        } else {
            log.warn("友链「{}」仍然失联：{}", friend.getTitle(), e.getMessage());
        }
    }

    /** 抓取所有已通过友链的 RSS 最近文章，失败保留旧值不覆盖 */
    public synchronized Map<String, Integer> refreshAllRss() {
        List<Friend> friends = friendMapper.selectList(new QueryWrapper<Friend>().eq("status", 1));
        Map<String, Integer> stat = new LinkedHashMap<>();
        stat.put("checked", 0);
        stat.put("ok", 0);
        stat.put("failed", 0);
        stat.put("skipped", 0);
        for (Friend friend : friends) {
            String rssUrl = friend.getRssUrl();
            if (rssUrl == null || rssUrl.trim().isEmpty() || !isSafeHttpUrl(rssUrl)) {
                stat.put("skipped", stat.get("skipped") + 1);
                continue;
            }
            stat.put("checked", stat.get("checked") + 1);
            if (refreshOneRss(friend)) {
                stat.put("ok", stat.get("ok") + 1);
            } else {
                stat.put("failed", stat.get("failed") + 1);
            }
        }
        log.info("友链 RSS 完成：共 {} 条，成功 {}，失败 {}，跳过 {}", stat.get("checked"), stat.get("ok"),
                stat.get("failed"), stat.get("skipped"));
        return stat;
    }

    private boolean refreshOneRss(Friend friend) {
        String rssUrl = friend.getRssUrl().trim();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(rssUrl))
                    .timeout(Duration.ofMillis(rssTimeoutMs))
                    .header("User-Agent", "Mozilla/5.0 (compatible; jsnws-friend-rss)")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 400) {
                log.warn("友链「{}」RSS 返回 {}，保留原值", friend.getTitle(), response.statusCode());
                return false;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(response.body()));
            doc.getDocumentElement().normalize();

            String title = null;
            String dateText = null;
            NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() > 0) {
                Element item = (Element) items.item(0);
                title = text(item, "title");
                dateText = firstNonEmpty(text(item, "pubDate"), text(item, "date"), text(item, "updated"));
            } else {
                NodeList entries = doc.getElementsByTagName("entry");
                if (entries.getLength() > 0) {
                    Element entry = (Element) entries.item(0);
                    title = text(entry, "title");
                    dateText = firstNonEmpty(text(entry, "updated"), text(entry, "published"));
                }
            }
            Date publishedAt = parseRssDate(dateText);
            if (publishedAt == null) {
                log.warn("友链「{}」RSS 未解析到可用时间，保留原值", friend.getTitle());
                return false;
            }
            String safeTitle = title == null ? null : (title.length() > 200 ? title.substring(0, 200) : title);
            friendMapper.update(null, new UpdateWrapper<Friend>()
                    .eq("id", friend.getId())
                    .set("rss_updated_at", publishedAt)
                    .set("rss_title", safeTitle));
            return true;
        } catch (Exception e) {
            log.warn("友链「{}」RSS 抓取失败，保留原值：{}", friend.getTitle(), e.getMessage());
            return false;
        }
    }

    private String text(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return null;
        }
        String value = nodes.item(0).getTextContent();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private Date parseRssDate(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        try {
            return Date.from(ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
        } catch (Exception ignored) {
        }
        try {
            return Date.from(OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        } catch (Exception ignored) {
        }
        try {
            return Date.from(Instant.parse(text));
        } catch (Exception ignored) {
        }
        try {
            return Date.from(LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant());
        } catch (Exception ignored) {
        }
        try {
            return Date.from(LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());
        } catch (Exception ignored) {
        }
        return null;
    }

    /** 只允许公网 http(s)：跳过内网、本机、非法协议 */
    private boolean isSafeHttpUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            host = host.toLowerCase();
            if ("localhost".equals(host) || host.endsWith(".local") || host.endsWith(".internal")) {
                return false;
            }
            if (host.matches("\\d+(\\.\\d+){3}")) {
                if (host.startsWith("10.") || host.startsWith("127.") || host.startsWith("192.168.")
                        || host.startsWith("169.254.") || host.startsWith("0.")) {
                    return false;
                }
                Matcher matcher = IPV4.matcher(host);
                if (matcher.matches()) {
                    int second = Integer.parseInt(matcher.group(1));
                    if (second >= 16 && second <= 31) {
                        return false;
                    }
                }
            }
            return !host.contains(":");
        } catch (Exception e) {
            return false;
        }
    }
}
