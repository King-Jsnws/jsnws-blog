package com.jsnws.server.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 鉴权拦截器：
 * 1. 解析 Authorization 头中的 token，把 userId 放入 request 属性
 * 2. /admin/** 要求站长登录，登录失效或权限不足时返回 code=300（前端自动跳转登录页）
 * 3. REQUIRE_LOGIN 中的接口要求任意有效登录（用户或管理员）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID = "userId";

    /** 需要登录（用户 token 或管理员 token 均可）的接口 */
    private static final Set<String> REQUIRE_LOGIN = new HashSet<>(Arrays.asList(
            "/user/updateUserInfo",
            "/user/updateSecretInfo",
            "/user/subscribe",
            "/comment/saveComment",
            "/resource/upload",
            "/resource/saveResource",
            "/weiYan/saveNews",
            "/weiYan/deleteWeiYan",
            "/family/saveFamily",
            "/family/getFamily",
            "/qiniu/getUpToken"
    ));

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    private final TokenUtil tokenUtil;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(TokenUtil tokenUtil, ObjectMapper objectMapper) {
        this.tokenUtil = tokenUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        Long userId = tokenUtil.verify(token);
        request.setAttribute(USER_ID, userId);

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.startsWith("/admin/")) {
            if (userId == null) {
                return reject(response, "登录已失效");
            }
            if (!bossUserId.equals(userId)) {
                return reject(response, "权限不足");
            }
            return true;
        }

        if (REQUIRE_LOGIN.contains(path) && userId == null) {
            return reject(response, "登录已失效");
        }

        return true;
    }

    private boolean reject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.tokenInvalid(message)));
        response.getWriter().flush();
        return false;
    }
}
