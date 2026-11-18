package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.Family;
import com.jsnws.server.mapper.FamilyMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 爱情模块（family）：个人小屋申请、站长爱情展示、后台审核
 */
@RestController
@RequestMapping("/family")
public class FamilyController extends BaseController {

    private final FamilyMapper familyMapper;

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    public FamilyController(FamilyMapper familyMapper) {
        this.familyMapper = familyMapper;
    }

    /** 我申请的爱情小屋 */
    @GetMapping("/getFamily")
    public Result<Family> getFamily() {
        Long userId = currentUserId();
        return Result.ok(familyMapper.selectOne(new QueryWrapper<Family>()
                .eq("user_id", userId).orderByDesc("create_time").last("LIMIT 1")));
    }

    /** 站长的爱情（审核通过状态） */
    @GetMapping("/getAdminFamily")
    public Result<Family> getAdminFamily() {
        return Result.ok(familyMapper.selectOne(new QueryWrapper<Family>()
                .eq("user_id", bossUserId).eq("status", true).orderByDesc("create_time").last("LIMIT 1")));
    }

    /** 随机爱情小屋 */
    @GetMapping("/listRandomFamily")
    public Result<List<Family>> listRandomFamily() {
        return Result.ok(familyMapper.selectList(new QueryWrapper<Family>()
                .eq("status", true).orderByDesc("create_time").last("LIMIT 10")));
    }

    /** 申请/更新爱情小屋（保存后默认待审核） */
    @PostMapping("/saveFamily")
    public Result<Family> saveFamily(@RequestBody Family family) {
        Long userId = currentUserId();
        Family existing = familyMapper.selectOne(new QueryWrapper<Family>()
                .eq("user_id", userId).orderByDesc("create_time").last("LIMIT 1"));
        if (existing != null) {
            family.setId(existing.getId());
            // 归属与审核状态只能由服务端决定，忽略客户端传入的 userId/status
            family.setUserId(userId);
            family.setStatus(false);
            family.setCreateTime(existing.getCreateTime());
            familyMapper.updateById(family);
        } else {
            family.setId(null);
            family.setUserId(userId);
            family.setStatus(false);
            family.setCreateTime(new Date());
            familyMapper.insert(family);
        }
        return Result.ok(family);
    }

    /** 爱情小屋分页（管理员） */
    @PostMapping("/listFamily")
    public Result<Page<Family>> listFamily(@RequestBody Map<String, Object> params) {
        Page<Family> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<Family> wrapper = new QueryWrapper<>();
        if (params.get("status") != null) {
            wrapper.eq("status", Boolean.parseBoolean(String.valueOf(params.get("status"))));
        }
        wrapper.orderByDesc("create_time");
        return Result.ok(familyMapper.selectPage(page, wrapper));
    }

    /** 删除爱情小屋（管理员） */
    @GetMapping("/deleteFamily")
    public Result<Void> deleteFamily(@RequestParam("id") Long id) {
        familyMapper.deleteById(id);
        return Result.ok();
    }

    /** 审核/切换爱情小屋状态（管理员） */
    @GetMapping("/changeLoveStatus")
    public Result<Void> changeLoveStatus(@RequestParam("id") Long id,
                                         @RequestParam("flag") Boolean flag) {
        Family family = familyMapper.selectById(id);
        if (family == null) {
            throw new ApiException("记录不存在！");
        }
        family.setStatus(flag);
        familyMapper.updateById(family);
        return Result.ok();
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
