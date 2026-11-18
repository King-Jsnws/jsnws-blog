package com.jsnws.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.bilibili.BiliAnimeItem;
import com.jsnws.server.bilibili.BiliRuntimeConfig;
import com.jsnws.server.bilibili.BilibiliAnimeClient;
import com.jsnws.server.entity.Anime;
import com.jsnws.server.entity.SysConfig;
import com.jsnws.server.mapper.AnimeMapper;
import com.jsnws.server.mapper.SysConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 追番同步：拉取指定 B 站账号的追番列表，在事务里 upsert 到 anime 表。
 * 规则：
 * 1. 按 season_id 幂等更新；没有 season_id 的手工记录按标题绑定一次。
 * 2. B 站字段优先，手工数据只补 B 站拿不到的空缺（评分/题材）。
 * 3. B 站已取消追番的条目只置 visible=0，不物理删除。
 * 4. 任何失败都保留上一份数据，只写同步状态。
 */
@Service
public class AnimeSyncService {

    private static final Logger log = LoggerFactory.getLogger(AnimeSyncService.class);

    private static final String KEY_LAST_SUCCESS = "anime_sync_last_success";
    private static final String KEY_LAST_ATTEMPT = "anime_sync_last_attempt";
    private static final String KEY_STATUS = "anime_sync_status";
    private static final String KEY_MESSAGE = "anime_sync_message";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AnimeMapper animeMapper;
    private final SysConfigMapper sysConfigMapper;
    private final BilibiliAnimeClient bilibiliAnimeClient;
    private final BilibiliConfigService bilibiliConfigService;
    private final TransactionTemplate transactionTemplate;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AnimeSyncService(AnimeMapper animeMapper,
                            SysConfigMapper sysConfigMapper,
                            BilibiliAnimeClient bilibiliAnimeClient,
                            BilibiliConfigService bilibiliConfigService,
                            TransactionTemplate transactionTemplate) {
        this.animeMapper = animeMapper;
        this.sysConfigMapper = sysConfigMapper;
        this.bilibiliAnimeClient = bilibiliAnimeClient;
        this.bilibiliConfigService = bilibiliConfigService;
        this.transactionTemplate = transactionTemplate;
    }

    /** 立即同步一次；网络失败也返回结构化结果，方便管理端展示 */
    public Map<String, Object> syncNow() {
        if (!running.compareAndSet(false, true)) {
            return result("running", "同步正在进行中，请稍后再试");
        }
        try {
            // 每次同步都重新解析配置：后台改完无需重启
            BiliRuntimeConfig cfg = bilibiliConfigService.resolve();
            if (!cfg.isEnabled()) {
                return skipped("追番同步已关闭");
            }
            if (trim(cfg.getUid()).isEmpty()) {
                return skipped("未配置 B 站 UID，同步已跳过");
            }
            writeStatus("running", "正在拉取 B 站追番列表");

            List<BiliAnimeItem> items = bilibiliAnimeClient.fetchFollowList(cfg);
            if (items.isEmpty()) {
                throw new IllegalStateException("B站返回的追番列表为空");
            }
            enrichMissingDetails(items, cfg);

            SyncSummary summary = transactionTemplate.execute(status -> applyItems(items));
            if (summary == null) {
                throw new IllegalStateException("写入追番数据失败");
            }
            String message = "同步成功：共 " + items.size() + " 部，新增 "
                    + summary.inserted + "，更新 " + summary.updated + "，隐藏 " + summary.hidden;
            writeStatus("success", message);

            Map<String, Object> data = result("success", message);
            data.put("total", items.size());
            data.put("inserted", summary.inserted);
            data.put("updated", summary.updated);
            data.put("hidden", summary.hidden);
            return data;
        } catch (Exception e) {
            String message = e.getMessage() == null || e.getMessage().trim().isEmpty()
                    ? e.getClass().getSimpleName()
                    : e.getMessage();
            log.warn("追番同步失败：{}", message);
            writeStatus("failed", message);
            return result("failed", "同步失败：" + message);
        } finally {
            running.set(false);
        }
    }

    /** 同步状态（前台展示数据来源/更新时间，后台展示最后一次结果） */
    public Map<String, Object> getStatus() {
        Map<String, String> values = loadStatusValues();
        String lastSuccess = values.getOrDefault(KEY_LAST_SUCCESS, "");
        BiliRuntimeConfig cfg = bilibiliConfigService.resolve();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", cfg.isEnabled());
        data.put("uidConfigured", !trim(cfg.getUid()).isEmpty());
        data.put("type", cfg.getType());
        data.put("detailEnrich", cfg.isDetailEnrich());
        data.put("intervalMs", cfg.getIntervalMs());
        data.put("syncing", running.get());
        data.put("lastSuccessTime", lastSuccess);
        data.put("lastAttemptTime", values.getOrDefault(KEY_LAST_ATTEMPT, ""));
        data.put("status", values.getOrDefault(KEY_STATUS, "never"));
        data.put("message", values.getOrDefault(KEY_MESSAGE, ""));
        data.put("dataSource", !lastSuccess.isEmpty() ? "B站" : "后台维护");
        return data;
    }

    /**
     * 只为“新条目 / 缺评分 / 缺题材”的番剧请求详情，单轮数量受 detail-limit 限制。
     * 详情失败只记日志，保留旧值。
     */
    private void enrichMissingDetails(List<BiliAnimeItem> items, BiliRuntimeConfig cfg) {
        if (!cfg.isDetailEnrich()) {
            return;
        }
        Map<Long, Anime> existing = new HashMap<>();
        for (Anime anime : animeMapper.selectList(null)) {
            if (anime.getSeasonId() != null) {
                existing.put(anime.getSeasonId(), anime);
            }
        }
        int limit = Math.max(0, cfg.getDetailLimit());
        int attempted = 0;
        for (BiliAnimeItem item : items) {
            if (attempted >= limit) {
                break;
            }
            if (item.getSeasonId() == null) {
                continue;
            }
            Anime old = existing.get(item.getSeasonId());
            boolean missing = old == null || old.getScore() == null || isBlank(old.getType());
            if (!missing) {
                continue;
            }
            // 按“尝试次数”计数，失败也计入，避免连续失败时打出过多请求
            attempted++;
            try {
                bilibiliAnimeClient.enrich(item, cfg);
            } catch (Exception e) {
                log.warn("B站番剧详情补全失败 season_id={}：{}", item.getSeasonId(), e.getMessage());
            }
        }
    }

    /** 事务内执行：按 season_id upsert，并把本轮未出现的同步条目置为隐藏 */
    private SyncSummary applyItems(List<BiliAnimeItem> items) {
        Date now = new Date();
        List<Anime> existing = animeMapper.selectList(null);
        Map<Long, Anime> bySeason = new HashMap<>();
        Map<String, Anime> manualByTitle = new HashMap<>();
        for (Anime anime : existing) {
            if (anime.getSeasonId() != null) {
                bySeason.put(anime.getSeasonId(), anime);
            } else {
                manualByTitle.putIfAbsent(normalizeTitle(anime.getTitle()), anime);
            }
        }

        Set<Long> seen = new HashSet<>();
        int inserted = 0;
        int updated = 0;
        for (BiliAnimeItem item : items) {
            if (item.getSeasonId() == null || isBlank(item.getTitle())) {
                continue;
            }
            seen.add(item.getSeasonId());

            Anime target = bySeason.get(item.getSeasonId());
            if (target == null) {
                // 首次接入时，把同名手工记录绑定到 B 站条目，避免重复
                target = manualByTitle.remove(normalizeTitle(item.getTitle()));
            }
            if (target == null) {
                Anime anime = new Anime();
                anime.setSeasonId(item.getSeasonId());
                anime.setMediaId(item.getMediaId());
                anime.setOrigin("bili");
                anime.setCreateTime(now);
                fill(anime, item, now, true);
                animeMapper.insert(anime);
                inserted++;
            } else {
                fill(target, item, now, false);
                target.setSeasonId(item.getSeasonId());
                target.setMediaId(item.getMediaId());
                target.setOrigin("bili");
                animeMapper.updateById(target);
                updated++;
            }
        }

        int hidden = 0;
        for (Anime anime : existing) {
            if (anime.getSeasonId() == null || seen.contains(anime.getSeasonId())) {
                continue;
            }
            if (anime.getVisible() != null && anime.getVisible() == 0) {
                continue;
            }
            Anime patch = new Anime();
            patch.setId(anime.getId());
            patch.setVisible(0);
            animeMapper.updateById(patch);
            hidden++;
        }
        return new SyncSummary(inserted, updated, hidden);
    }

    /** B 站字段优先；B 站没有的字段保留手工/上一次的值 */
    private void fill(Anime anime, BiliAnimeItem item, Date now, boolean isNew) {
        anime.setTitle(truncate(firstNonBlank(item.getTitle(), anime.getTitle()), 100));
        anime.setCover(truncate(firstNonBlank(item.getCover(), anime.getCover()), 500));
        if (item.getScore() != null) {
            anime.setScore(item.getScore());
        } else if (isNew) {
            anime.setScore(null);
        }
        anime.setType(truncate(firstNonBlank(item.getGenre(), anime.getType()), 50));
        if (item.getSeasonType() != null) {
            anime.setSeasonType(item.getSeasonType());
        }
        anime.setSource("B站");
        anime.setStatus(truncate(firstNonBlank(item.getStatusText(), anime.getStatus()), 50));
        anime.setDescription(truncate(firstNonBlank(item.getDescription(), anime.getDescription()), 500));
        anime.setUrl(truncate(firstNonBlank(item.getUrl(), anime.getUrl()), 500));
        anime.setReleaseDate(firstNonBlank(item.getReleaseDate(), anime.getReleaseDate()));
        anime.setLastSyncTime(now);
        anime.setVisible(1);
    }

    private Map<String, String> loadStatusValues() {
        List<SysConfig> configs = sysConfigMapper.selectList(new QueryWrapper<SysConfig>()
                .in("config_key", Arrays.asList(KEY_LAST_SUCCESS, KEY_LAST_ATTEMPT, KEY_STATUS, KEY_MESSAGE)));
        Map<String, String> values = new HashMap<>();
        for (SysConfig config : configs) {
            values.putIfAbsent(config.getConfigKey(),
                    config.getConfigValue() == null ? "" : config.getConfigValue());
        }
        return values;
    }

    private void writeStatus(String status, String message) {
        String now = LocalDateTime.now().format(TIME_FORMAT);
        writeConfig(KEY_LAST_ATTEMPT, "追番同步-最后尝试时间", now);
        writeConfig(KEY_STATUS, "追番同步-状态", status);
        writeConfig(KEY_MESSAGE, "追番同步-说明", truncate(message, 480));
        if ("success".equals(status)) {
            writeConfig(KEY_LAST_SUCCESS, "追番同步-最后成功时间", now);
        }
    }

    private void writeConfig(String key, String name, String value) {
        SysConfig config = sysConfigMapper.selectOne(new QueryWrapper<SysConfig>()
                .eq("config_key", key).last("LIMIT 1"));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigName(name);
            config.setConfigValue(value);
            config.setConfigType("1");
            config.setCreateTime(new Date());
            sysConfigMapper.insert(config);
        } else {
            config.setConfigValue(value);
            sysConfigMapper.updateById(config);
        }
    }

    private Map<String, Object> skipped(String message) {
        writeStatus("skipped", message);
        return result("skipped", message);
    }

    private Map<String, Object> result(String status, String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status);
        data.put("message", message);
        return data;
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.trim().toLowerCase(Locale.ROOT);
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first.trim();
        }
        return second == null ? null : second.trim();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.length() > max ? text.substring(0, max) : text;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class SyncSummary {
        private final int inserted;
        private final int updated;
        private final int hidden;

        private SyncSummary(int inserted, int updated, int hidden) {
            this.inserted = inserted;
            this.updated = updated;
            this.hidden = hidden;
        }
    }
}
