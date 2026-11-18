package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.AesUtil;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.UserRole;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.mapper.SysUserMapper;
import com.jsnws.server.mapper.WebInfoMapper;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 后台用户管理（/admin/user/**，仅站长）
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController extends BaseController {

    private final SysUserMapper userMapper;
    private final WebInfoMapper webInfoMapper;
    private final ObjectMapper objectMapper;
    private final AesUtil aesUtil;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    public AdminUserController(SysUserMapper userMapper, WebInfoMapper webInfoMapper, ObjectMapper objectMapper,
                               AesUtil aesUtil) {
        this.userMapper = userMapper;
        this.webInfoMapper = webInfoMapper;
        this.objectMapper = objectMapper;
        this.aesUtil = aesUtil;
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

    /** 创建用户（主理人专用，密码为前端 AES 加密后的密文，直接入库） */
    @PostMapping("/create")
    public Result<Void> create(@RequestBody Map<String, Object> body) {
        String username = strVal(body.get("username"));
        String nickname = strVal(body.get("nickname"));
        String password = strVal(body.get("password"));
        Integer userType;
        try {
            userType = body.get("userType") == null ? UserRole.USER
                    : Integer.parseInt(String.valueOf(body.get("userType")));
        } catch (Exception e) {
            throw new ApiException("用户类型不正确！");
        }
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new ApiException("请填写账号和初始密码！");
        }
        String account = username.trim();
        if (account.length() > 50) {
            throw new ApiException("账号长度不能超过 50 个字符！");
        }
        for (int i = 0; i < account.length(); i++) {
            char c = account.charAt(i);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                throw new ApiException("账号不能包含空格！");
            }
        }
        if (!UserRole.assignable(userType)) {
            throw new ApiException("只能创建管理员、作者、AI 站长或普通用户！");
        }
        if (userMapper.selectCount(new QueryWrapper<SysUser>().eq("username", account)) > 0) {
            throw new ApiException("账号已存在！");
        }

        WebInfo webInfo = webInfoMapper.selectById(1L);
        SysUser user = new SysUser();
        user.setUsername(account);
        user.setNickname(nickname == null || nickname.trim().isEmpty() ? account : nickname.trim());
        user.setAccount(account);
        user.setPassword(password);
        user.setAvatar(randomValue(webInfo == null ? null : webInfo.getRandomAvatar()));
        user.setGender(0);
        user.setSubscribe("");
        user.setUserStatus(true);
        user.setUserType(userType);
        user.setCreateTime(new Date());
        userMapper.insert(user);
        return Result.ok();
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
        checkNotBoss(userId, "不能修改站长账号名！");
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

    /**
     * 站长自助修改登录用户名 / 密码。
     * 密码由前端按现有 AES 规则加密后提交，服务端只校验旧密码密文并原样存新密文，不落明文。
     */
    @PostMapping("/updateBossAccount")
    public Result<Void> updateBossAccount(@RequestBody Map<String, Object> body) {
        if (!bossUserId.equals(currentUserId())) {
            throw new ApiException("无权修改站长账号！");
        }
        SysUser boss = userMapper.selectById(bossUserId);
        if (boss == null) {
            throw new ApiException("站长账号不存在！");
        }

        String username = strVal(body.get("username"));
        String oldPassword = strVal(body.get("oldPassword"));
        String newPassword = strVal(body.get("newPassword"));
        boolean emailProvided = body.containsKey("email");
        String email = strVal(body.get("email"));
        username = username == null ? "" : username.trim();
        oldPassword = oldPassword == null ? "" : oldPassword.trim();
        newPassword = newPassword == null ? "" : newPassword.trim();
        email = email == null ? "" : email.trim();

        if (emailProvided && !email.isEmpty()) {
            if (email.length() > 100) {
                throw new ApiException("邮箱长度不能超过 100 个字符！");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new ApiException("邮箱格式不正确！");
            }
            Long usedEmail = userMapper.selectCount(new QueryWrapper<SysUser>()
                    .eq("email", email).ne("id", bossUserId));
            Long usedAccount = userMapper.selectCount(new QueryWrapper<SysUser>()
                    .eq("account", email).ne("id", bossUserId));
            if (usedEmail > 0 || usedAccount > 0) {
                throw new ApiException("该邮箱已被其他账号使用！");
            }
        }

        if (username.isEmpty()) {
            throw new ApiException("账号不能为空！");
        }
        if (username.length() > 50) {
            throw new ApiException("账号长度不能超过 50 个字符！");
        }
        for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                throw new ApiException("账号不能包含空格！");
            }
        }
        if (oldPassword.isEmpty()) {
            throw new ApiException("请输入当前密码！");
        }
        if (!oldPassword.equals(boss.getPassword())) {
            throw new ApiException("当前密码不正确！");
        }

        String plainNewPassword = null;
        if (!newPassword.isEmpty()) {
            plainNewPassword = aesUtil.decrypt(newPassword);
            if (plainNewPassword.length() < 6 || plainNewPassword.length() > 20) {
                throw new ApiException("新密码长度需为 6-20 位！");
            }
            for (int i = 0; i < plainNewPassword.length(); i++) {
                if (Character.isWhitespace(plainNewPassword.charAt(i))) {
                    throw new ApiException("新密码不能包含空格！");
                }
            }
        }

        boolean usernameChanged = !username.equals(boss.getUsername());
        boolean emailChanged = emailProvided && !email.equals(boss.getEmail() == null ? "" : boss.getEmail());
        if (!usernameChanged && plainNewPassword == null && !emailChanged) {
            throw new ApiException("账号、邮箱和新密码均未修改！");
        }
        if (usernameChanged) {
            Long count = userMapper.selectCount(new QueryWrapper<SysUser>()
                    .eq("username", username).ne("id", bossUserId));
            if (count > 0) {
                throw new ApiException("该账号名已被占用，请换一个！");
            }
        }

        // 用 UpdateWrapper 显式 set：解绑邮箱时要把 email/account 置为 NULL，
        // updateById 会忽略 null 字段，无法清空。
        UpdateWrapper<SysUser> update = new UpdateWrapper<>();
        update.eq("id", bossUserId);
        if (usernameChanged) {
            update.set("username", username);
        }
        if (plainNewPassword != null) {
            update.set("password", newPassword);
        }
        if (emailProvided) {
            update.set("email", email.isEmpty() ? null : email);
            update.set("account", email.isEmpty() ? null : email);
        }
        userMapper.update(null, update);
        return Result.ok();
    }

    /** 修改用户身份（2 管理员 / 3 作者 / 4 普通用户 / 5 AI 站长，主理人不可授予） */
    @GetMapping("/changeUserType")
    public Result<Void> changeUserType(@RequestParam("userId") Long userId,
                                       @RequestParam("userType") Integer userType) {
        checkNotBoss(userId, "不能修改站长身份！");
        if (!UserRole.assignable(userType)) {
            throw new ApiException("只能设置为管理员、作者、AI 站长或普通用户！");
        }
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
        map.put("userTypeLabel", UserRole.label(user.getUserType()));
        map.put("createTime", user.getCreateTime());
        return map;
    }

    private String randomValue(String jsonArray) {
        try {
            if (jsonArray != null && !jsonArray.isEmpty()) {
                List<String> list = objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
                if (!list.isEmpty()) {
                    return list.get(new Random().nextInt(list.size()));
                }
            }
        } catch (Exception ignored) {
        }
        return null;
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
