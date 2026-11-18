package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.AesUtil;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.TokenUtil;
import com.jsnws.server.common.UserRole;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.mapper.SysUserMapper;
import com.jsnws.server.mapper.WebInfoMapper;
import com.jsnws.server.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户模块：登录 / 注册 / 个人信息 / 验证码 / 订阅
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    private final SysUserMapper userMapper;
    private final WebInfoMapper webInfoMapper;
    private final TokenUtil tokenUtil;
    private final AesUtil aesUtil;
    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    @Value("${jsnws.boss-username}")
    private String bossUsername;

    /** 验证码缓存：key = flag + ":" + place */
    private final Map<String, CodeEntry> codeCache = new ConcurrentHashMap<>();

    private static class CodeEntry {
        final String code;
        final long createAt;
        CodeEntry(String code) {
            this.code = code;
            this.createAt = System.currentTimeMillis();
        }
    }

    public UserController(SysUserMapper userMapper, WebInfoMapper webInfoMapper,
                          TokenUtil tokenUtil, AesUtil aesUtil, ObjectMapper objectMapper,
                          MailService mailService) {
        this.userMapper = userMapper;
        this.webInfoMapper = webInfoMapper;
        this.tokenUtil = tokenUtil;
        this.aesUtil = aesUtil;
        this.objectMapper = objectMapper;
        this.mailService = mailService;
    }

    /** 登录（表单提交，密码为前端 AES 加密后的密文，直接密文比对） */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam("account") String account,
                                             @RequestParam("password") String password,
                                             @RequestParam(value = "isAdmin", required = false, defaultValue = "false") Boolean isAdmin) {
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>()
                .eq("username", account)
                .or()
                .eq("account", account)
                .or()
                .eq("email", account)
                .last("LIMIT 1"));
        if (user == null || !password.equals(user.getPassword())) {
            throw new ApiException("账号或密码错误！");
        }
        if (user.getUserStatus() == null || !user.getUserStatus()) {
            throw new ApiException("账号已被禁用！");
        }
        if (Boolean.TRUE.equals(isAdmin) && !canLoginAdmin(user)) {
            throw new ApiException("该账号没有管理员权限！");
        }
        Map<String, Object> data = safeUser(user);
        data.put("accessToken", tokenUtil.create(user.getId()));
        return Result.ok(data);
    }

    /** 注册（JSON 提交） */
    @PostMapping("/regist")
    public Result<Map<String, Object>> regist(@RequestBody Map<String, Object> body) {
        String username = str(body.get("username"));
        String password = str(body.get("password"));
        String code = str(body.get("code"));
        String account = str(body.get("account"));
        if (account == null || account.isEmpty()) {
            account = str(body.get("email"));
        }
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new ApiException("请完善注册信息！");
        }
        if (userMapper.selectCount(new QueryWrapper<SysUser>().eq("username", username)) > 0) {
            throw new ApiException("账号已存在！");
        }

        WebInfo webInfo = webInfoMapper.selectById(1L);
        SysUser user = new SysUser();
        user.setUsername(username);
        String nickname = str(body.get("nickname"));
        user.setNickname(nickname == null || nickname.trim().isEmpty() ? username : nickname.trim());
        user.setAccount(account);
        user.setPassword(password);
        user.setAvatar(randomValue(webInfo == null ? null : webInfo.getRandomAvatar()));
        user.setGender(0);
        user.setEmail(account);
        user.setSubscribe("");
        user.setUserStatus(true);
        user.setUserType(4);
        user.setCreateTime(new Date());
        userMapper.insert(user);

        Map<String, Object> data = safeUser(user);
        data.put("accessToken", tokenUtil.create(user.getId()));
        return Result.ok(data);
    }

    /** 退出登录（无状态 token，直接返回成功） */
    @GetMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    /** 修改个人资料（username/gender/introduction/avatar） */
    @PostMapping("/updateUserInfo")
    public Result<Map<String, Object>> updateUserInfo(@RequestBody Map<String, Object> body) {
        SysUser user = currentUser(userMapper);
        if (body.containsKey("username")) {
            String username = str(body.get("username"));
            if (username != null && !username.isEmpty()) {
                if (bossUserId.equals(user.getId()) && !username.equals(user.getUsername())) {
                    throw new ApiException("主理人账号名不可修改，请修改昵称！");
                }
                Long count = userMapper.selectCount(new QueryWrapper<SysUser>()
                        .eq("username", username).ne("id", user.getId()));
                if (count > 0) {
                    throw new ApiException("账号已存在！");
                }
                user.setUsername(username);
            }
        }
        if (body.containsKey("nickname")) {
            user.setNickname(str(body.get("nickname")));
        }
        if (body.containsKey("gender") && body.get("gender") != null) {
            user.setGender(intVal(body.get("gender")));
        }
        if (body.containsKey("introduction")) {
            user.setIntroduction(str(body.get("introduction")));
        }
        if (body.containsKey("avatar")) {
            user.setAvatar(str(body.get("avatar")));
        }
        userMapper.updateById(user);
        return Result.ok(safeUser(user));
    }

    /** 修改手机号/邮箱/密码（已登录） */
    @PostMapping("/updateSecretInfo")
    public Result<Map<String, Object>> updateSecretInfo(@RequestParam("code") String code,
                                                        @RequestParam("password") String password,
                                                        @RequestParam("flag") Integer flag,
                                                        @RequestParam("place") String place) {
        SysUser user = currentUser(userMapper);
        checkCode(flag, place, code);
        user.setPassword(password);
        if (flag != null && flag == 1) {
            user.setPhoneNumber(place);
        } else if (flag != null && flag == 2) {
            user.setEmail(place);
            if (user.getAccount() != null && user.getAccount().isEmpty()) {
                user.setAccount(place);
            }
        }
        userMapper.updateById(user);
        return Result.ok(safeUser(user));
    }

    /** 找回密码（未登录） */
    @PostMapping("/updateForForgetPassword")
    public Result<Void> updateForForgetPassword(@RequestParam("code") String code,
                                                @RequestParam("password") String password,
                                                @RequestParam("flag") Integer flag,
                                                @RequestParam("place") String place) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (flag != null && flag == 1) {
            wrapper.eq("phone_number", place);
        } else {
            wrapper.eq("email", place);
        }
        SysUser user = userMapper.selectOne(wrapper.last("LIMIT 1"));
        if (user == null) {
            throw new ApiException("账号不存在！");
        }
        checkCode(flag, place, code);
        user.setPassword(password);
        userMapper.updateById(user);
        return Result.ok();
    }

    /** 发送找回密码验证码（目前仅支持邮箱；配置缺失或发送失败时返回明确提示） */
    @GetMapping("/getCodeForForgetPassword")
    public Result<Void> getCodeForForgetPassword(@RequestParam("flag") Integer flag,
                                                 @RequestParam("place") String place) {
        return sendCode(flag, place);
    }

    /** 发送绑定验证码 */
    @GetMapping("/getCodeForBind")
    public Result<Void> getCodeForBind(@RequestParam("flag") Integer flag,
                                       @RequestParam("place") String place) {
        return sendCode(flag, place);
    }

    /** 订阅/取消订阅分类标签 */
    @GetMapping("/subscribe")
    public Result<Map<String, Object>> subscribe(@RequestParam("labelId") Long labelId,
                                                 @RequestParam("flag") Boolean flag) {
        SysUser user = currentUser(userMapper);
        try {
            List<Long> list = user.getSubscribe() == null || user.getSubscribe().isEmpty()
                    ? new java.util.ArrayList<>()
                    : objectMapper.readValue(user.getSubscribe(), new TypeReference<List<Long>>() {});
            if (Boolean.TRUE.equals(flag)) {
                if (!list.contains(labelId)) {
                    list.add(labelId);
                }
            } else {
                list.remove(labelId);
            }
            user.setSubscribe(objectMapper.writeValueAsString(list));
        } catch (Exception e) {
            user.setSubscribe("[]");
        }
        userMapper.updateById(user);
        return Result.ok(safeUser(user));
    }

    private Result<Void> sendCode(Integer flag, String place) {
        if (flag != null && flag == 1) {
            throw new ApiException("暂不支持短信验证码，请使用邮箱验证！");
        }
        if (place == null || !place.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ApiException("请输入正确的邮箱地址！");
        }
        if (!mailService.isEnabled()) {
            throw new ApiException("邮件服务已关闭，暂不支持验证码相关操作，请联系站长！");
        }
        String to = place.trim();
        String code = String.format("%06d", new Random().nextInt(1000000));
        // 站点名称每次发送时现读，后台改名后无需重启，邮件标题立即跟着变
        String siteName = mailService.siteName();
        // 配置缺失 / 认证失败时由 MailService 抛出明确提示，验证码只在发送成功后写入缓存
        mailService.send(to, "【" + siteName + "】验证码",
                "您正在「" + siteName + "」进行邮箱验证，验证码是：" + code + "，5 分钟内有效。\n如非本人操作，请忽略本邮件。");
        codeCache.put(flag + ":" + to, new CodeEntry(code));
        return Result.ok();
    }

    private void checkCode(Integer flag, String place, String code) {
        if (!mailService.isEnabled()) {
            // 安全兜底：邮件服务关闭时拒绝，不再“开发模式放行”
            throw new ApiException("邮件服务未启用，暂不支持验证码校验！");
        }
        if (flag != null && flag == 1) {
            throw new ApiException("暂不支持短信验证码，请使用邮箱验证！");
        }
        String key = flag + ":" + (place == null ? "" : place.trim());
        CodeEntry entry = codeCache.get(key);
        if (entry == null || System.currentTimeMillis() - entry.createAt > 5 * 60 * 1000L
                || !entry.code.equals(code)) {
            throw new ApiException("验证码错误！");
        }
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

    /** 输出给前端的用户字段（不含密码） */
    private Map<String, Object> safeUser(SysUser user) {
        Map<String, Object> map = new HashMap<>();
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
        // 后台侧边栏据此显示站长菜单：必须同时匹配固定 id 与账号名
        boolean isBoss = user.getId() != null && user.getId().equals(bossUserId)
                && bossUsername.equals(user.getUsername());
        map.put("isBoss", isBoss);
        map.put("userTypeLabel", isBoss ? "本站主理人" : UserRole.label(user.getUserType()));
        map.put("createTime", user.getCreateTime());
        return map;
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /** 允许登录后台的角色：旧管理员(1)、管理员(2)、作者(3)、AI 站长(5) */
    private boolean canLoginAdmin(SysUser user) {
        Integer type = user.getUserType();
        return type != null && (type == 1 || type == 2 || type == 3 || type == UserRole.AI_OWNER);
    }

    private Integer intVal(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
