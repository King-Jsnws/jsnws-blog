package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.SysConfig;
import com.jsnws.server.mapper.SysConfigMapper;
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

    public SysConfigController(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
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

    /** 全部配置（管理员） */
    @GetMapping("/listConfig")
    public Result<List<SysConfig>> listConfig() {
        return Result.ok(sysConfigMapper.selectList(new QueryWrapper<SysConfig>().orderByAsc("id")));
    }

    /** 新增或更新配置（管理员） */
    @PostMapping("/saveOrUpdateConfig")
    public Result<Void> saveOrUpdateConfig(@RequestBody SysConfig config) {
        if (config.getId() != null && sysConfigMapper.selectById(config.getId()) != null) {
            sysConfigMapper.updateById(config);
        } else {
            config.setId(null);
            config.setCreateTime(new Date());
            sysConfigMapper.insert(config);
        }
        return Result.ok();
    }

    /** 删除配置（管理员） */
    @GetMapping("/deleteConfig")
    public Result<Void> deleteConfig(@RequestParam("id") Long id) {
        sysConfigMapper.deleteById(id);
        return Result.ok();
    }
}
