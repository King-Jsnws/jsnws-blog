package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.Music;
import com.jsnws.server.mapper.MusicMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 音乐模块：前台侧栏播放器与后台音乐管理共用
 */
@RestController
public class MusicController {

    private final MusicMapper musicMapper;

    public MusicController(MusicMapper musicMapper) {
        this.musicMapper = musicMapper;
    }

    /** 音乐列表（按排序值升序） */
    @GetMapping("/webInfo/listMusic")
    public Result<List<Music>> listMusic() {
        return Result.ok(musicMapper.selectList(
                new QueryWrapper<Music>().orderByAsc("sort").orderByAsc("id")));
    }

    /** 新增歌曲（管理员） */
    @PostMapping("/webInfo/saveMusic")
    public Result<Void> saveMusic(@RequestBody Music music) {
        normalize(music);
        music.setId(null);
        music.setCreateTime(new Date());
        musicMapper.insert(music);
        return Result.ok();
    }

    /** 更新歌曲（管理员） */
    @PostMapping("/webInfo/updateMusic")
    public Result<Void> updateMusic(@RequestBody Music music) {
        if (music.getId() == null || musicMapper.selectById(music.getId()) == null) {
            throw new ApiException("歌曲不存在！");
        }
        normalize(music);
        musicMapper.updateById(music);
        return Result.ok();
    }

    /** 删除歌曲（管理员） */
    @GetMapping("/webInfo/deleteMusic")
    public Result<Void> deleteMusic(@RequestParam("id") Long id) {
        musicMapper.deleteById(id);
        return Result.ok();
    }

    private void normalize(Music music) {
        if (music.getName() == null || music.getName().trim().isEmpty()) {
            throw new ApiException("歌曲名称不能为空！");
        }
        music.setName(music.getName().trim());
        if (music.getUrl() == null || music.getUrl().trim().isEmpty()) {
            throw new ApiException("播放地址不能为空！");
        }
        music.setUrl(music.getUrl().trim());
        if (music.getSort() == null) {
            music.setSort(0);
        }
    }
}
