package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.SensitiveUtil;
import com.jsnws.server.entity.SysConfig;
import com.jsnws.server.mapper.SysConfigMapper;
import com.jsnws.server.service.BilibiliConfigService;
import com.jsnws.server.service.MailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置模块：前台读取公开配置（key→value）、后台增删改
 */
@RestController
@RequestMapping("/sysConfig")
public class SysConfigController extends BaseController {

    private final SysConfigMapper sysConfigMapper;
    private final MailService mailService;

    /** 友链页允许管理员维护的配置键（白名单） */
    private static final java.util.Set<String> FRIEND_CONFIG_KEYS = new java.util.HashSet<>(java.util.Arrays.asList(
            "friendWebName", "friendIntroduction", "friendUrl",
            "friendAvatar", "friendRssUrl", "friendScreenshotUrl"));

    public SysConfigController(SysConfigMapper sysConfigMapper, MailService mailService) {
        this.sysConfigMapper = sysConfigMapper;
        this.mailService = mailService;
    }

    /** 公开配置：{configKey: configValue} */
    @GetMapping("/listSysConfig")
    public Result<Map<String, String>> listSysConfig() {
        List<SysConfig> list = sysConfigMapper.selectList(new QueryWrapper<SysConfig>().eq("config_type", "2"));
        Map<String, String> data = new LinkedHashMap<>();
        for (SysConfig config : list) {
            data.put(config.getConfigKey(), config.getConfigValue());
        }
        return Result.ok(data);
    }

    /** 全部配置（管理员）；敏感项只返回脱敏值，绝不返回明文 */
    @GetMapping("/listConfig")
    public Result<List<SysConfig>> listConfig() {
        List<SysConfig> list = sysConfigMapper.selectList(new QueryWrapper<SysConfig>().orderByAsc("id"));
        for (SysConfig config : list) {
            if (BilibiliConfigService.KEY_SESSDATA.equals(config.getConfigKey())) {
                config.setConfigValue(SensitiveUtil.maskSecret(config.getConfigValue()));
            }
            if (MailService.KEY_PASSWORD.equals(config.getConfigKey())) {
                // 授权码只回显末四位，前端再次保存时由 saveConfig 还原原值
                config.setConfigValue(SensitiveUtil.maskTail(config.getConfigValue(), 4));
            }
        }
        return Result.ok(list);
    }

    /** 新增或更新配置（主理人） */
    @PostMapping("/saveOrUpdateConfig")
    public Result<Void> saveOrUpdateConfig(@RequestBody SysConfig config) {
        return saveConfig(config);
    }

    /** 发送测试邮件（主理人）：使用已保存的邮箱配置，返回可读的成功/失败原因 */
    @PostMapping("/sendTestMail")
    public Result<Map<String, Object>> sendTestMail(@RequestBody Map<String, String> body) {
        String to = body == null || body.get("to") == null ? "" : body.get("to").trim();
        if (!to.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ApiException("请输入正确的收件邮箱地址！");
        }
        mailService.sendTest(to);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("to", to);
        data.put("host", mailService.loadConfig().getHost());
        data.put("message", "测试邮件已发送，请检查收件箱（若未收到请查看垃圾箱）");
        return Result.ok(data);
    }

    /** 删除配置（主理人） */
    @GetMapping("/deleteConfig")
    public Result<Void> deleteConfig(@RequestParam("id") Long id) {
        sysConfigMapper.deleteById(id);
        return Result.ok();
    }

    /** 友链页配置（管理员可读） */
    @GetMapping("/listFriendConfig")
    public Result<List<SysConfig>> listFriendConfig() {
        return Result.ok(sysConfigMapper.selectList(new QueryWrapper<SysConfig>()
                .in("config_key", FRIEND_CONFIG_KEYS).orderByAsc("id")));
    }

    /** 友链页配置保存（管理员可写，仅限白名单键） */
    @PostMapping("/saveFriendConfig")
    public Result<Void> saveFriendConfig(@RequestBody SysConfig config) {
        if (config.getConfigKey() == null || !FRIEND_CONFIG_KEYS.contains(config.getConfigKey())) {
            throw new ApiException("仅允许修改友链相关配置！");
        }
        config.setConfigType("2");
        return saveConfig(config);
    }

    private Result<Void> saveConfig(SysConfig config) {
        // 授权码：前端拿到的是 ****末四位，回传脱敏值时保留库里原值，绝不把掩码写进数据库
        if (MailService.KEY_PASSWORD.equals(config.getConfigKey())
                && SensitiveUtil.isMasked(config.getConfigValue())) {
            SysConfig existing = findMailPassword(config.getId());
            if (existing == null) {
                throw new ApiException("授权码不能保存为脱敏占位值，请重新输入完整授权码！");
            }
            config.setId(existing.getId());
            config.setConfigValue(existing.getConfigValue());
        }
        if (config.getId() != null && sysConfigMapper.selectById(config.getId()) != null) {
            sysConfigMapper.updateById(config);
        } else {
            config.setId(null);
            config.setCreateTime(new Date());
            sysConfigMapper.insert(config);
        }
        return Result.ok();
    }

    private SysConfig findMailPassword(Long id) {
        if (id != null) {
            SysConfig byId = sysConfigMapper.selectById(id);
            if (byId != null && MailService.KEY_PASSWORD.equals(byId.getConfigKey())) {
                return byId;
            }
        }
        return sysConfigMapper.selectOne(new QueryWrapper<SysConfig>()
                .eq("config_key", MailService.KEY_PASSWORD).last("LIMIT 1"));
    }
}
