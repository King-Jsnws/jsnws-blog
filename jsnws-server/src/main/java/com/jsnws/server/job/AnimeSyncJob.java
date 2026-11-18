package com.jsnws.server.job;

import com.jsnws.server.bilibili.BiliRuntimeConfig;
import com.jsnws.server.config.BilibiliProperties;
import com.jsnws.server.service.AnimeSyncService;
import com.jsnws.server.service.BilibiliConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 追番同步定时任务：每轮结束后按「当前生效配置」的间隔重新排期，
 * 因此在后台改完间隔后，下一轮排期即按新间隔执行，无需重启。
 * 关闭同步或 UID 为空时静默跳过，不影响页面。
 */
@Component
public class AnimeSyncJob {

    private static final Logger log = LoggerFactory.getLogger(AnimeSyncJob.class);

    private final AnimeSyncService animeSyncService;
    private final BilibiliConfigService bilibiliConfigService;
    private final BilibiliProperties properties;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "anime-sync-job");
        thread.setDaemon(true);
        return thread;
    });

    public AnimeSyncJob(AnimeSyncService animeSyncService,
                        BilibiliConfigService bilibiliConfigService,
                        BilibiliProperties properties) {
        this.animeSyncService = animeSyncService;
        this.bilibiliConfigService = bilibiliConfigService;
        this.properties = properties;
    }

    @PostConstruct
    public void start() {
        scheduleNext(properties.getInitialDelayMs());
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
    }

    private void scheduleNext(long delayMs) {
        try {
            scheduler.schedule(this::runOnce, Math.max(1000L, delayMs), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 调度器已关闭（应用停止中），忽略
            log.debug("追番同步任务排期失败：{}", e.getMessage());
        }
    }

    private void runOnce() {
        long nextDelay = properties.getIntervalMs();
        try {
            BiliRuntimeConfig cfg = bilibiliConfigService.resolve();
            nextDelay = cfg.getIntervalMs();
            if (!cfg.isEnabled()) {
                log.debug("追番同步已关闭，本轮跳过");
                return;
            }
            if (cfg.getUid().isEmpty()) {
                log.debug("未配置 B 站 UID，本轮跳过");
                return;
            }
            Map<String, Object> result = animeSyncService.syncNow();
            log.info("追番同步任务：status={} message={}", result.get("status"), result.get("message"));
        } catch (Exception e) {
            log.error("追番同步任务异常：{}", e.getMessage());
        } finally {
            scheduleNext(nextDelay);
        }
    }
}
