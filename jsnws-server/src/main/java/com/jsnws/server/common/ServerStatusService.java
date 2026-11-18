package com.jsnws.server.common;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 服务器运行状态采集：处理器、内存、存储、系统版本。
 * <p>
 * 每 5 秒采样一次并缓存，前台轮询只读缓存，不会每秒都去读 /proc；
 * 只输出公开可见的容量与使用率，不返回主机名、内网地址、进程路径等敏感信息。
 */
@Component
public class ServerStatusService {

    /** 站点统一按东八区展示，避免服务器时区变化导致时间漂移 */
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** 采样间隔：前台可以更频繁地轮询，但真正读系统文件最多 5 秒一次 */
    private static final long SAMPLE_INTERVAL_SECONDS = 5L;

    private final AtomicReference<Map<String, Object>> snapshot = new AtomicReference<>(new LinkedHashMap<>());

    private ScheduledExecutorService scheduler;
    private long[] lastCpuSample;
    /** 读不到就保持空串，接口不返回该字段，由前端直接省略，不展示「未知」占位 */
    private String cpuModel = "";
    private String osName = "";
    private String kernel = "";

    @PostConstruct
    public void start() {
        readStaticInfo();
        lastCpuSample = readCpuSample();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "server-status-sampler");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::sample, 0, SAMPLE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    /** 读取缓存快照；启动瞬间尚未采样时同步补一次 */
    public Map<String, Object> getSnapshot() {
        Map<String, Object> cached = snapshot.get();
        if (cached.isEmpty()) {
            sample();
            cached = snapshot.get();
        }
        return cached;
    }

    private void sample() {
        try {
            // 启动时 /proc 尚不可读时补一次静态信息，读到了就不再重复读
            if (cpuModel.isEmpty() || osName.isEmpty() || kernel.isEmpty()) {
                readStaticInfo();
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "online");
            data.put("checkedAt", LocalDateTime.now(ZONE).format(TIME_FORMAT));
            if (!osName.isEmpty()) {
                data.put("osName", osName);
            }
            if (!kernel.isEmpty()) {
                data.put("kernel", kernel);
            }
            int cores = readCpuCores();
            if (cores > 0) {
                data.put("cpuCores", cores);
            }
            if (!cpuModel.isEmpty()) {
                data.put("cpuModel", cpuModel);
            }

            Double cpuUsage = readCpuUsage();
            if (cpuUsage != null) {
                data.put("cpuUsage", cpuUsage);
            }

            long[] memory = readMemory();
            if (memory != null && memory[0] > 0) {
                long total = memory[0];
                long available = memory[1];
                long used = Math.max(0L, total - available);
                data.put("memTotal", total);
                data.put("memUsed", used);
                data.put("memAvailable", available);
                data.put("memUsage", percent(used, total));
            }

            File root = new File("/");
            long diskTotal = root.getTotalSpace();
            // 用 freeSpace 而不是 usableSpace：与 df 的「已用」口径一致（usable 会扣掉 root 预留块）
            long diskFree = root.getFreeSpace();
            if (diskTotal > 0) {
                long diskUsed = Math.max(0L, diskTotal - diskFree);
                data.put("diskTotal", diskTotal);
                data.put("diskUsed", diskUsed);
                data.put("diskUsage", percent(diskUsed, diskTotal));
            }

            data.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
            data.put("javaVersion", System.getProperty("java.version"));
            snapshot.set(data);
        } catch (Exception ignored) {
            // 采样失败时保留上一份快照，接口继续返回可用数据
        }
    }

    private void readStaticInfo() {
        try {
            List<String> cpuInfo = Files.readAllLines(Paths.get("/proc/cpuinfo"), StandardCharsets.UTF_8);
            for (String line : cpuInfo) {
                if (line.startsWith("model name")) {
                    int index = line.indexOf(':');
                    if (index > 0) {
                        cpuModel = line.substring(index + 1).trim();
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            List<String> osRelease = Files.readAllLines(Paths.get("/etc/os-release"), StandardCharsets.UTF_8);
            for (String line : osRelease) {
                if (line.startsWith("PRETTY_NAME=")) {
                    osName = line.substring("PRETTY_NAME=".length()).trim().replace("\"", "");
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            Path release = Paths.get("/proc/sys/kernel/osrelease");
            if (Files.isReadable(release)) {
                kernel = new String(Files.readAllBytes(release), StandardCharsets.UTF_8).trim();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 以 /proc/cpuinfo 的 processor 条目数为准（与 nproc 口径一致）；
     * 读不到时返回 0，接口不返回 cpuCores，前端直接不显示处理器核数，
     * 不使用 JVM 估算值或任何写死数字兜底。
     */
    private int readCpuCores() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("/proc/cpuinfo"), StandardCharsets.UTF_8);
            int count = 0;
            for (String line : lines) {
                if (line.startsWith("processor\t") || line.startsWith("processor ")) {
                    count++;
                }
            }
            if (count > 0) {
                return count;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    /** /proc/stat 第一行：cpu user nice system idle iowait irq softirq steal ... */
    private long[] readCpuSample() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("/proc/stat"), StandardCharsets.UTF_8);
            if (!lines.isEmpty() && lines.get(0).startsWith("cpu ")) {
                String[] parts = lines.get(0).trim().split("\\s+");
                long total = 0L;
                for (int i = 1; i < parts.length; i++) {
                    total += Long.parseLong(parts[i]);
                }
                long idle = Long.parseLong(parts[4]) + (parts.length > 5 ? Long.parseLong(parts[5]) : 0L);
                return new long[]{total, idle};
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Double readCpuUsage() {
        long[] current = readCpuSample();
        if (current == null) {
            return null;
        }
        Double usage = null;
        if (lastCpuSample != null) {
            long totalDelta = current[0] - lastCpuSample[0];
            long idleDelta = current[1] - lastCpuSample[1];
            if (totalDelta > 0) {
                double value = (1.0 - (double) idleDelta / (double) totalDelta) * 100.0;
                usage = round(Math.max(0.0, Math.min(100.0, value)));
            }
        }
        lastCpuSample = current;
        return usage;
    }

    /** 返回 [总量, 可用量]（字节），取 /proc/meminfo 的 MemTotal 与 MemAvailable */
    private long[] readMemory() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("/proc/meminfo"), StandardCharsets.UTF_8);
            long totalKb = 0L;
            long availableKb = 0L;
            for (String line : lines) {
                if (line.startsWith("MemTotal:")) {
                    totalKb = parseKb(line);
                } else if (line.startsWith("MemAvailable:")) {
                    availableKb = parseKb(line);
                }
            }
            if (totalKb > 0) {
                return new long[]{totalKb * 1024L, availableKb * 1024L};
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private long parseKb(String line) {
        String[] parts = line.trim().split("\\s+");
        return parts.length > 1 ? Long.parseLong(parts[1]) : 0L;
    }

    private double percent(long used, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return round(((double) used / (double) total) * 100.0);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
