package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.UserRole;
import com.jsnws.server.entity.TreeHole;
import com.jsnws.server.entity.WeiYan;
import com.jsnws.server.mapper.TreeHoleMapper;
import com.jsnws.server.mapper.WeiYanMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 随笔模块（前端 weiYan/news）：文章页留言 + 爱情页随笔 + 后台树洞管理
 */
@RestController
public class WeiYanController extends BaseController {

    private final WeiYanMapper weiYanMapper;
    private final TreeHoleMapper treeHoleMapper;

    public WeiYanController(WeiYanMapper weiYanMapper, TreeHoleMapper treeHoleMapper) {
        this.weiYanMapper = weiYanMapper;
        this.treeHoleMapper = treeHoleMapper;
    }

    /** 随笔分页（爱情页按 userId 过滤站长的随笔） */
    @PostMapping("/weiYan/listWeiYan")
    public Result<Page<WeiYan>> listWeiYan(@RequestBody Map<String, Object> params) {
        Page<WeiYan> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<WeiYan> wrapper = new QueryWrapper<>();
        if (params.get("userId") != null) {
            wrapper.eq("user_id", longVal(params.get("userId"), 0));
        }
        wrapper.orderByDesc("create_time");
        return Result.ok(weiYanMapper.selectPage(page, wrapper));
    }

    /** 发表随笔/留言（前端会传格式化好的 createTime 字符串） */
    @PostMapping("/weiYan/saveNews")
    public Result<WeiYan> saveNews(@RequestBody Map<String, Object> params) {
        Long userId = currentUserId();
        String content = params.get("content") == null ? null : String.valueOf(params.get("content"));
        if (content == null || content.isEmpty()) {
            throw new ApiException("内容不能为空！");
        }
        WeiYan weiYan = new WeiYan();
        weiYan.setUserId(userId);
        weiYan.setContent(content);
        weiYan.setSource(params.get("source") == null ? null : String.valueOf(params.get("source")));
        Object createTime = params.get("createTime");
        if (createTime != null && !String.valueOf(createTime).isEmpty()) {
            try {
                weiYan.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(String.valueOf(createTime)));
            } catch (ParseException e) {
                weiYan.setCreateTime(new Date());
            }
        } else {
            weiYan.setCreateTime(new Date());
        }
        weiYanMapper.insert(weiYan);
        return Result.ok(weiYan);
    }

    /** 文章页留言分页（按 source 过滤） */
    @PostMapping("/weiYan/listNews")
    public Result<Page<WeiYan>> listNews(@RequestBody Map<String, Object> params) {
        Page<WeiYan> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<WeiYan> wrapper = new QueryWrapper<>();
        if (params.get("source") != null) {
            wrapper.eq("source", String.valueOf(params.get("source")));
        }
        wrapper.orderByDesc("create_time");
        return Result.ok(weiYanMapper.selectPage(page, wrapper));
    }

    /** 删除随笔（本人或站长） */
    @GetMapping("/weiYan/deleteWeiYan")
    public Result<Void> deleteWeiYan(@RequestParam("id") Long id) {
        WeiYan weiYan = weiYanMapper.selectById(id);
        if (weiYan == null) {
            throw new ApiException("记录不存在！");
        }
        Long userId = currentUserId();
        if (currentRole() != UserRole.BOSS && !userId.equals(weiYan.getUserId())) {
            throw new ApiException("无权删除！");
        }
        weiYanMapper.deleteById(id);
        return Result.ok();
    }

    // ==================== 后台管理 ====================

    /** 树洞分页（管理员） */
    @PostMapping("/admin/treeHole/boss/list")
    public Result<Page<TreeHole>> bossList(@RequestBody Map<String, Object> params) {
        Page<TreeHole> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<TreeHole> wrapper = new QueryWrapper<>();
        if (params.get("searchKey") != null && !String.valueOf(params.get("searchKey")).isEmpty()) {
            wrapper.like("message", String.valueOf(params.get("searchKey")));
        }
        wrapper.orderByDesc("create_time");
        Page<TreeHole> result = treeHoleMapper.selectPage(page, wrapper);
        // 后台列表额外提供 IP 字段（实体中 ip 带 @JsonIgnore，避免前台接口泄露）
        result.getRecords().forEach(item -> item.setIpAddress(item.getIp()));
        return Result.ok(result);
    }

    private long longVal(Object value, long def) {
        if (value == null) return def;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }
}
