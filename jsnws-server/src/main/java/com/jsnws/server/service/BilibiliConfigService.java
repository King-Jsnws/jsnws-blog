package com.jsnws.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.bilibili.BiliRuntimeConfig;
import com.jsnws.server.bilibili.BilibiliConfigRequest;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.SensitiveUtil;
import com.jsnws.server.config.BilibiliProperties;
import com.jsnws.server.entity.SysConfig;
import com.jsnws.server.mapper.SysConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * B 站追番同步配置：后台可改，存 sys_config（config_type=1，仅管理员可见）。
 * 生效优先级：后台配置 > application.yml > 环境变量 BILI_SESSDATA。
 * 后台未配置任何键时，行为与改造前完全一致。
 */
@Service
public class BilibiliConfigService {

    private static final Logger log = LoggerFactory.getLogger(BilibiliConfigService.class);

    /** 后台配置键：全部 config_type=1（仅管理员可读） */
    public static final String KEY_ENABLED = "anime_bili_enabled";
    public static final String KEY_UID = "anime_bili_uid";
    public static final String KEY_SESSDATA = "anime_bili_sessdata";
    public static final String KEY_INTERVAL_MS = "anime_bili_interval_ms";
    public static final String KEY_TYPE = "anime_bili_type";
    public static final String KEY_DETAIL_ENRICH = "anime_bili_detail_enrich";

    private static final List<String> ALL_KEYS = Arrays.asList(
            KEY_ENABLED, KEY_UID, KEY_SESSDATA, KEY_INTERVAL_MS, KEY_TYPE, KEY_DETAIL_ENRICH);

    /** 同步间隔下限：10 分钟，避免把 B 站接口打成风控 */
    private static final long MIN_INTERVAL_MS = 600000L;

    private static final String SOURCE_ADMIN = "后台配置";
    private static final String SOURCE_PROPERTIES = "配置文件/环境变量";
    private static final String SOURCE_NONE = "未配置";

    private final SysConfigMapper sysConfigMapper;
    private final BilibiliProperties properties;

    public BilibiliConfigService(SysConfigMapper sysConfigMapper, BilibiliProperties properties) {
        this.sysConfigMapper = sysConfigMapper;
        this.properties = properties;
    }

    /** 解析本次同步实际使用的配置（每次调用都重新读库，后台改完无需重启） */
    public BiliRuntimeConfig resolve() {
        BiliRuntimeConfig cfg = new BiliRuntimeConfig(properties);
        Map<String, String> values = loadValues();

        String enabled = values.get(KEY_ENABLED);
        if (!isBlank(enabled)) {
            cfg.setEnabled(Boolean.parseBoolean(enabled.trim()));
        }
        String uid = values.get(KEY_UID);
        if (!isBlank(uid)) {
            cfg.setUid(uid.trim());
        }
        String interval = values.get(KEY_INTERVAL_MS);
        if (!isBlank(interval)) {
            try {
                long parsed = Long.parseLong(interval.trim());
                if (parsed > 0) {
                    cfg.setIntervalMs(parsed);
                }
            } catch (NumberFormatException e) {
                log.warn("后台同步间隔配置无法解析，继续使用配置文件的值");
            }
        }
        String type = values.get(KEY_TYPE);
        if (!isBlank(type)) {
            try {
                int parsed = Integer.parseInt(type.trim());
                if (parsed == 0 || parsed == 1 || parsed == 2) {
                    cfg.setType(parsed);
                }
            } catch (NumberFormatException e) {
                log.warn("后台拉取类型配置无法解析，继续使用配置文件的值");
            }
        }
        String enrich = values.get(KEY_DETAIL_ENRICH);
        if (!isBlank(enrich)) {
            cfg.setDetailEnrich(Boolean.parseBoolean(enrich.trim()));
        }

        // SESSDATA：后台配置 > application.yml > 环境变量 BILI_SESSDATA
        String sessdata = firstNonBlank(values.get(KEY_SESSDATA), properties.getSessdata());
        if (isBlank(sessdata)) {
            sessdata = System.getenv("BILI_SESSDATA");
        }
        cfg.setSessdata(normalizeSessdata(sessdata));
        return cfg;
    }

    /** 后台读取：SESSDATA 只返回脱敏值 */
    public Map<String, Object> getAdminConfig() {
        BiliRuntimeConfig cfg = resolve();
        Map<String, String> values = loadValues();
        boolean adminSessdata = !isBlank(values.get(KEY_SESSDATA));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", cfg.isEnabled());
        data.put("uid", cfg.getUid());
        data.put("intervalMs", cfg.getIntervalMs());
        data.put("type", cfg.getType());
        data.put("detailEnrich", cfg.isDetailEnrich());
        data.put("sessdataConfigured", !isBlank(cfg.getSessdata()));
        data.put("sessdataMasked", SensitiveUtil.maskSecret(cfg.getSessdata()));
        data.put("sessdataSource", resolveSessdataSource(adminSessdata, cfg.getSessdata()));
        // 未配置后台值时，前端提示这些值来自配置文件
        data.put("adminKeysConfigured", !values.isEmpty());
        return data;
    }

    /** 后台保存：字段为 null 不修改，sessdata 为空表示保留原值；保存后立即生效于下一次同步 */
    public Map<String, Object> saveAdminConfig(BilibiliConfigRequest request) {
        if (request == null) {
            throw new ApiException("配置内容不能为空！");
        }
        if (request.getEnabled() != null) {
            writeConfig(KEY_ENABLED, "追番同步-是否启用", String.valueOf(request.getEnabled()));
        }
        if (request.getUid() != null) {
            String uid = request.getUid().trim();
            if (!uid.isEmpty() && !uid.matches("\\d{1,20}")) {
                throw new ApiException("B 站 UID 只能是数字！");
            }
            writeConfig(KEY_UID, "追番同步-B站UID", uid);
        }
        if (request.getIntervalMs() != null) {
            long interval = request.getIntervalMs();
            if (interval < MIN_INTERVAL_MS) {
                throw new ApiException("同步间隔不能小于 10 分钟！");
            }
            writeConfig(KEY_INTERVAL_MS, "追番同步-间隔(毫秒)", String.valueOf(interval));
        }
        if (request.getType() != null) {
            if (request.getType() < 0 || request.getType() > 2) {
                throw new ApiException("拉取类型只能是 0（追番+追剧）、1（追番）或 2（追剧）！");
            }
            writeConfig(KEY_TYPE, "追番同步-拉取类型", String.valueOf(request.getType()));
        }
        if (request.getDetailEnrich() != null) {
            writeConfig(KEY_DETAIL_ENRICH, "追番同步-补全评分与题材",
                    String.valueOf(request.getDetailEnrich()));
        }
        if (request.getSessdata() != null && !request.getSessdata().trim().isEmpty()) {
            String sessdata = normalizeSessdata(request.getSessdata());
            if (sessdata.length() < 10) {
                throw new ApiException("SESSDATA 长度异常，请确认复制完整！");
            }
            writeConfig(KEY_SESSDATA, "追番同步-SESSDATA", sessdata);
            // 只记录是否更新，不打印任何凭证内容
            log.info("后台更新了 B 站 SESSDATA");
        }
        log.info("后台已保存 B 站同步配置");
        return getAdminConfig();
    }

    private String resolveSessdataSource(boolean adminSessdata, String effective) {
        if (isBlank(effective)) {
            return SOURCE_NONE;
        }
        if (adminSessdata) {
            return SOURCE_ADMIN;
        }
        return SOURCE_PROPERTIES;
    }

    private Map<String, String> loadValues() {
        Map<String, String> values = new HashMap<>();
        List<SysConfig> configs = sysConfigMapper.selectList(new QueryWrapper<SysConfig>()
                .in("config_key", ALL_KEYS));
        for (SysConfig config : configs) {
            String value = config.getConfigValue();
            if (value != null && !value.trim().isEmpty()) {
                values.putIfAbsent(config.getConfigKey(), value);
            }
        }
        return values;
    }

    private void writeConfig(String key, String name, String value) {
        SysConfig config = sysConfigMapper.selectOne(new QueryWrapper<SysConfig>()
                .eq("config_key", key).last("LIMIT 1"));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigName(name);
            config.setConfigValue(value);
            // 1 = 仅管理员可见，绝不会出现在 /sysConfig/listSysConfig 的公开配置里
            config.setConfigType("1");
            config.setCreateTime(new Date());
            sysConfigMapper.insert(config);
        } else {
            config.setConfigValue(value);
            sysConfigMapper.updateById(config);
        }
    }

    /** 兼容用户把整段 Cookie 粘进来的情况：去掉 SESSDATA= 前缀与后续分号 */
    private String normalizeSessdata(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.regionMatches(true, 0, "SESSDATA=", 0, 9)) {
            value = value.substring(9);
        }
        int semicolon = value.indexOf(';');
        if (semicolon >= 0) {
            value = value.substring(0, semicolon);
        }
        return value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }
        return second == null ? "" : second;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
