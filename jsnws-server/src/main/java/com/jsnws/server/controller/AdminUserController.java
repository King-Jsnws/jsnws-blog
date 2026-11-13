package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台用户管理（/admin/user/**，仅站长）
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController extends BaseController {

    private final SysUserMapper userMapper;

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    public AdminUserController(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 用户分页（不含密码字段） */
    @PostMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestBody Map<String, Object> params) {
        Page<SysUser> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        String searchKey = strVal(params.get("searchKey"));
        if (searchKey != null && !searchKey.isEmpty()) {
            wrapper.and(w -> w.like("username", searchKey).or().like("nickname", searchKey)
                    .or().like("email", searchKey).or().like("account", searchKey));
        }
        if (params.get("userStatus") != null) {
            wrapper.eq("user_status", Boolean.parseBoolean(String.valueOf(params.get("userStatus"))));
        }
        if (params.get("userType") != null) {
            wrapper.eq("user_type", Integer.parseInt(String.valueOf(params.get("userType"))));
        }
        wrapper.orderByAsc("id");
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        List<Map<String, Object>> records = result.getRecords().stream()
                .map(this::safeUser).collect(Collectors.toList());

        Page<Map<String, Object>> data = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        data.setRecords(records);
        return Result.ok(data);
    }

    /** 启用/禁用用户（站长不可操作自己） */
    @GetMapping("/changeUserStatus")
    public Result<Void> changeUserStatus(@RequestParam("userId") Long userId,
                                         @RequestParam("flag") Boolean flag) {
        checkNotBoss(userId, "不能修改站长状态！");
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException("用户不存在！");
        }
        user.setUserStatus(flag);
        userMapper.updateById(user);
        return Result.ok();
    }

    /** 修改用户账号（登录用户名，仅改这一个字段） */
    @GetMapping("/updateUsername")
    public Result<Void> updateUsername(@RequestParam("userId") Long userId,
                                       @RequestParam("username") String username) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException("用户不存在！");
        }
        String raw = strVal(username);
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            throw new ApiException("账号不能为空！");
        }
        if (value.length() > 50) {
            throw new ApiException("账号长度不能超过 50 个字符！");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                throw new ApiException("账号不能包含空格！");
            }
        }
        Long count = userMapper.selectCount(new QueryWrapper<SysUser>()
                .eq("username", value).ne("id", userId));
        if (count > 0) {
            throw new ApiException("账号已存在！");
        }
        user.setUsername(value);
        userMapper.updateById(user);
        return Result.ok();
    }

    /** 修改用户身份（普通用户/管理员） */
    @GetMapping("/changeUserType")
    public Result<Void> changeUserType(@RequestParam("userId") Long userId,
                                       @RequestParam("userType") Integer userType) {
        checkNotBoss(userId, "不能修改站长身份！");
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException("用户不存在！");
        }
        user.setUserType(userType);
        userMapper.updateById(user);
        return Result.ok();
    }

    private void checkNotBoss(Long userId, String message) {
        if (bossUserId.equals(userId)) {
            throw new ApiException(message);
        }
    }

    private Map<String, Object> safeUser(SysUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("account", user.getAccount());
        map.put("avatar", user.getAvatar());
        map.put("gender", user.getGender());
        map.put("introduction", user.getIntroduction());
        map.put("email", user.getEmail());
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("subscribe", user.getSubscribe());
        map.put("userStatus", user.getUserStatus());
        map.put("userType", user.getUserType());
        map.put("createTime", user.getCreateTime());
        return map;
    }

    private String strVal(Object value) {
        return value == null ? null : String.valueOf(value);
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
