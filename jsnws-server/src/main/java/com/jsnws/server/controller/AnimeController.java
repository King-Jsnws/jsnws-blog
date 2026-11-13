package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.Anime;
import com.jsnws.server.mapper.AnimeMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 追番模块：前台追番页与后台追番管理共用
 */
@RestController
public class AnimeController {

    private final AnimeMapper animeMapper;

    public AnimeController(AnimeMapper animeMapper) {
        this.animeMapper = animeMapper;
    }

    /** 追番列表（按评分倒序） */
    @GetMapping("/webInfo/listAnime")
    public Result<List<Anime>> listAnime() {
        return Result.ok(animeMapper.selectList(
                new QueryWrapper<Anime>().orderByDesc("score").orderByDesc("id")));
    }

    /** 新增番剧（管理员） */
    @PostMapping("/webInfo/saveAnime")
    public Result<Void> saveAnime(@RequestBody Anime anime) {
        if (anime.getTitle() == null || anime.getTitle().trim().isEmpty()) {
            throw new ApiException("番剧名称不能为空！");
        }
        normalize(anime);
        anime.setId(null);
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
