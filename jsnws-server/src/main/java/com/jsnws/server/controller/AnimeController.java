package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.bilibili.BilibiliConfigRequest;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.Anime;
import com.jsnws.server.mapper.AnimeMapper;
import com.jsnws.server.service.AnimeSyncService;
import com.jsnws.server.service.BilibiliConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 追番模块：前台追番页与后台追番管理共用
 */
@RestController
public class AnimeController {

    private final AnimeMapper animeMapper;
    private final AnimeSyncService animeSyncService;
    private final BilibiliConfigService bilibiliConfigService;

    public AnimeController(AnimeMapper animeMapper,
                           AnimeSyncService animeSyncService,
                           BilibiliConfigService bilibiliConfigService) {
        this.animeMapper = animeMapper;
        this.animeSyncService = animeSyncService;
        this.bilibiliConfigService = bilibiliConfigService;
    }

    /** 追番列表（前台，只返回未隐藏的条目，按评分倒序） */
    @GetMapping("/webInfo/listAnime")
    public Result<List<Anime>> listAnime() {
        return Result.ok(animeMapper.selectList(
                new QueryWrapper<Anime>().eq("visible", 1).orderByDesc("score").orderByDesc("id")));
    }

    /** 追番列表（后台，含已隐藏条目） */
    @GetMapping("/webInfo/listAdminAnime")
    public Result<List<Anime>> listAdminAnime() {
        return Result.ok(animeMapper.selectList(
                new QueryWrapper<Anime>().orderByDesc("score").orderByDesc("id")));
    }

    /** 立即从 B 站同步一次（管理员） */
    @GetMapping("/webInfo/syncAnime")
    public Result<Map<String, Object>> syncAnime() {
        return Result.ok(animeSyncService.syncNow());
    }

    /** 同步状态：前台用于展示数据来源与更新时间，后台用于展示最后一次结果 */
    @GetMapping("/webInfo/getAnimeSyncStatus")
    public Result<Map<String, Object>> getAnimeSyncStatus() {
        return Result.ok(animeSyncService.getStatus());
    }

    /** B 站同步配置（管理员读取，SESSDATA 已脱敏） */
    @GetMapping("/webInfo/getBilibiliConfig")
    public Result<Map<String, Object>> getBilibiliConfig() {
        return Result.ok(bilibiliConfigService.getAdminConfig());
    }

    /** B 站同步配置（管理员保存，SESSDATA 留空表示不修改） */
    @PostMapping("/webInfo/saveBilibiliConfig")
    public Result<Map<String, Object>> saveBilibiliConfig(@RequestBody BilibiliConfigRequest request) {
        return Result.ok(bilibiliConfigService.saveAdminConfig(request));
    }

    /** 新增番剧（管理员手工补充） */
    @PostMapping("/webInfo/saveAnime")
    public Result<Void> saveAnime(@RequestBody Anime anime) {
        if (anime.getTitle() == null || anime.getTitle().trim().isEmpty()) {
            throw new ApiException("番剧名称不能为空！");
        }
        normalize(anime);
        anime.setId(null);
        anime.setSeasonId(null);
        anime.setMediaId(null);
        anime.setOrigin("manual");
        anime.setVisible(anime.getVisible() == null ? 1 : anime.getVisible());
        anime.setLastSyncTime(null);
        anime.setCreateTime(new Date());
        animeMapper.insert(anime);
        return Result.ok();
    }

    /** 更新番剧（管理员） */
    @PostMapping("/webInfo/updateAnime")
    public Result<Void> updateAnime(@RequestBody Anime anime) {
        if (anime.getId() == null || animeMapper.selectById(anime.getId()) == null) {
            throw new ApiException("番剧不存在！");
        }
        normalize(anime);
        animeMapper.updateById(anime);
        return Result.ok();
    }

    /**
     * 表单里未填写的日期会以空字符串提交，DATE 字段无法接收，统一转为 null
     */
    private void normalize(Anime anime) {
        if (anime.getReleaseDate() != null && anime.getReleaseDate().trim().isEmpty()) {
            anime.setReleaseDate(null);
        }
        if (anime.getTitle() != null) {
            anime.setTitle(anime.getTitle().trim());
        }
    }

    /** 删除番剧（管理员） */
    @GetMapping("/webInfo/deleteAnime")
    public Result<Void> deleteAnime(@RequestParam("id") Long id) {
        animeMapper.deleteById(id);
        return Result.ok();
    }
}
