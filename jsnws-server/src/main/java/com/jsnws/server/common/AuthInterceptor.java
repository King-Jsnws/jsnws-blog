package com.jsnws.server.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 鉴权拦截器：
 * 1. 解析 Authorization 头中的 token，把 userId、userRole 放入 request 属性
 * 2. /admin/** 按角色等级放行（1 主理人 / 2 管理员 / 3 作者 / 4 普通用户）
 * 3. 未挂在 /admin 下的管理接口按同一套角色规则校验
 * 4. 默认拒绝：只有 PUBLIC_PATHS 可匿名访问，其余接口一律要求有效登录
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID = "userId";
    public static final String USER_ROLE = "userRole";

    /**
     * 公开接口白名单：无需登录即可访问。
     * 新增接口必须显式登记到这里，否则默认要求登录（默认拒绝）。
     */
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            // 登录、注册、退出、找回密码（找回密码是否可用由业务层校验验证码）
            "/user/login",
            "/user/regist",
            "/user/logout",
            "/user/getCodeForForgetPassword",
            "/user/getCodeForBind",
            "/user/updateForForgetPassword",
            // 前台站点信息
            "/webInfo/getWebInfo",
            "/webInfo/getRandomAvatar",
            "/webInfo/getBossInfo",
            "/webInfo/getSortInfo",
            "/webInfo/listSortAndLabel",
            "/webInfo/listCollect",
            "/webInfo/listTreeHole",
            "/webInfo/saveTreeHole",
            "/webInfo/listAdminLovePhoto",
            "/webInfo/listResourcePath",
            "/webInfo/listFunny",
            "/webInfo/getAdmire",
            "/webInfo/listChangelog",
            "/webInfo/getVisitSummary",
            "/webInfo/getAnalytics",
            "/webInfo/recordVisit",
            // 服务状态（只读健康检查，不写库）
            "/webInfo/serverStatus",
            // 前台友链
            "/webInfo/listFriend",
            "/webInfo/friendClick",
            // 前台文章
            "/article/listArticle",
            "/article/listSortArticle",
            "/article/getArticleById",
            // 前台评论与随笔
            "/comment/getCommentCount",
            "/comment/listComment",
            "/comment/saveComment",
            "/comment/likeComment",
            "/weiYan/listWeiYan",
            "/weiYan/listNews",
            // 前台番剧、音乐
            "/webInfo/listAnime",
            "/webInfo/getAnimeSyncStatus",
            "/webInfo/listMusic",
            // 前台公开配置（仅 config_type=2）
            "/sysConfig/listSysConfig",
            // 站点 RSS 订阅（只读公开文章，无需登录）
            "/rss.xml",
            // 站点 Sitemap（只读公开文章与静态路由，无需登录）
            "/sitemap.xml",
            // 爱情页公开数据
            "/family/getAdminFamily",
            "/family/listRandomFamily"
    ));

    /** 2 级管理员可调用的管理接口（友链、留言、评论、赞助、娱乐） */
    private static final Set<String> ADMIN_PATHS = new HashSet<>(Arrays.asList(
            // 友链
            "/webInfo/listAdminFriend",
            "/webInfo/updateFriend",
            "/webInfo/deleteFriend",
            "/webInfo/auditFriend",
            "/webInfo/recommendFriend",
            "/webInfo/runFriendProbe",
            "/sysConfig/listFriendConfig",
            "/sysConfig/saveFriendConfig",
            // 留言（树洞）
            "/webInfo/deleteTreeHole",
            // 娱乐-番剧
            "/webInfo/listAdminAnime",
            "/webInfo/syncAnime",
            "/webInfo/getBilibiliConfig",
            "/webInfo/saveBilibiliConfig",
            "/webInfo/saveAnime",
            "/webInfo/updateAnime",
            "/webInfo/deleteAnime",
            // 娱乐-音乐
            "/webInfo/saveMusic",
            "/webInfo/updateMusic",
            "/webInfo/deleteMusic",
            // 娱乐-相册（公开列表在 PUBLIC_PATHS，这里只限制写操作与后台列表）
            "/webInfo/listAdminResourcePath",
            "/webInfo/saveResourcePath",
            "/webInfo/updateResourcePath",
            "/webInfo/deleteResourcePath",
            // 娱乐-视频/资源（管理端）
            "/resource/deleteResource",
            "/resource/changeResourceStatus",
            "/resource/listResource",
            // 访问统计（IP/用户明细，仅管理员及以上）
            "/webInfo/getHistoryInfo",
            // 后台配置读取（全部配置含非公开项）
            "/sysConfig/listConfig",
            // 爱情小屋后台管理
            "/family/listFamily",
            "/family/deleteFamily",
            "/family/changeLoveStatus"
    ));

    /** 仅主理人可调用的站点配置 / 分类 / 标签接口 */
    private static final Set<String> BOSS_PATHS = new HashSet<>(Arrays.asList(
            "/webInfo/updateWebInfo",
            "/webInfo/saveSort",
            "/webInfo/updateSort",
            "/webInfo/deleteSort",
            "/webInfo/saveLabel",
            "/webInfo/updateLabel",
            "/webInfo/deleteLabel",
            "/sysConfig/saveOrUpdateConfig",
            "/sysConfig/sendTestMail",
            "/sysConfig/deleteConfig"
    ));

    /** 3 级作者可调用：发文、改自己的文章、取上传 token（普通登录用户上传素材走默认登录校验） */
    private static final Set<String> AUTHOR_PATHS = new HashSet<>(Arrays.asList(
            "/article/saveArticle",
            "/article/updateArticle",
            "/qiniu/getUpToken"
    ));

    /**
     * 维护模式仍放行的前台接口：只保留登录入口与站点状态本身，
     * 保证维护期间能登录后台、前端能识别维护状态并展示维护页。
     */
    private static final Set<String> MAINTENANCE_ALLOWED = new HashSet<>(Arrays.asList(
            "/user/login",
            "/user/regist",
            "/user/logout",
            "/user/getCodeForForgetPassword",
            "/user/getCodeForBind",
            "/user/updateForForgetPassword",
            "/webInfo/getWebInfo",
            "/webInfo/getBossInfo",
            "/webInfo/getRandomAvatar",
            "/rss.xml",
            "/sitemap.xml"
    ));

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    /** 主理人账号名：与 boss-user-id 同时匹配才授予主理人，避免数字 id 被复用后自动获得权限 */
    @Value("${jsnws.boss-username}")
    private String bossUsername;

    private final TokenUtil tokenUtil;
    private final ObjectMapper objectMapper;
    private final SysUserMapper userMapper;
    private final MaintenanceService maintenanceService;

    public AuthInterceptor(TokenUtil tokenUtil, ObjectMapper objectMapper, SysUserMapper userMapper,
                           MaintenanceService maintenanceService) {
        this.tokenUtil = tokenUtil;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.maintenanceService = maintenanceService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求与静态上传目录不参与鉴权（CORS 由 CorsFilter 处理）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/resource/file/") || "/error".equals(path)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        Long userId = tokenUtil.verify(token);
        request.setAttribute(USER_ID, userId);
        int role = resolveRole(userId);
        request.setAttribute(USER_ROLE, role);

        if (path.startsWith("/admin/")) {
            if (userId == null) {
                return reject(response, "登录已失效");
            }
            if (role == UserRole.AI_OWNER) {
                // AI 站长：只放行日常内容管理，账号/运维类接口一律拒绝
                if (!AiStationPermissions.canUseAdminPath(path)) {
                    return rejectForbidden(response);
                }
                return true;
            }
            if (!canUseAdminPath(role, path)) {
                return reject(response, "权限不足");
            }
            return true;
        }

        // 维护模式：只拦截前台访客（匿名与普通用户）。
        // 登录入口、站点状态、RSS/Sitemap 放行；管理员/作者与主理人继续走后续鉴权，后台可正常使用。
        if (role == UserRole.USER && !MAINTENANCE_ALLOWED.contains(path) && maintenanceService.isMaintenanceOn()) {
            return rejectMaintenance(response);
        }

        // 公开接口直接放行
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }

        // 默认拒绝：未登记为公开的接口一律要求登录
        if (userId == null) {
            return reject(response, "登录已失效");
        }

        // AI 站长：仅放行 AiStationPermissions 中登记的内容/设置接口，其余一律拒绝
        if (role == UserRole.AI_OWNER) {
            if (!AiStationPermissions.canUseApiPath(path)) {
                return rejectForbidden(response);
            }
            return true;
        }

        if (ADMIN_PATHS.contains(path) && role > UserRole.ADMIN) {
            return reject(response, "权限不足");
        }

        if (AUTHOR_PATHS.contains(path) && role > UserRole.AUTHOR) {
            return reject(response, "权限不足");
        }

        if (BOSS_PATHS.contains(path) || "/article/deleteArticle".equals(path)) {
            if (role != UserRole.BOSS) {
                return reject(response, "权限不足");
            }
        }

        return true;
    }

    /**
     * 解析角色：主理人需满足「配置的 id」，且账号名与配置一致或 user_type=1
     * （站长可自助改用户名，配置项不会随改；不改配置也不应失去主理人权限）；
     * 其余按 user_type 归一化。被禁用或不存在的账号一律按普通用户处理。
     */
    private int resolveRole(Long userId) {
        if (userId == null) {
            return UserRole.USER;
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || Boolean.FALSE.equals(user.getUserStatus())) {
            return UserRole.USER;
        }
        if (bossUserId.equals(userId)
                && (bossUsername.equals(user.getUsername()) || Integer.valueOf(1).equals(user.getUserType()))) {
            return UserRole.BOSS;
        }
        return UserRole.normalize(user.getUserType());
    }

    /** /admin/** 路由的角色矩阵 */
    private boolean canUseAdminPath(int role, String path) {
        if (role == UserRole.BOSS) {
            return true;
        }
        if (role == UserRole.ADMIN) {
            // 管理员：留言、评论、赞助、自己的文章统计（友链与娱乐接口不在 /admin 下，由 ADMIN_PATHS 控制）
            return path.startsWith("/admin/comment/")
                    || path.startsWith("/admin/treeHole/")
                    || path.startsWith("/admin/sponsor/")
                    || "/admin/article/user/list".equals(path);
        }
        if (role == UserRole.AUTHOR) {
            // 作者：仅自己的文章列表与详情
            return "/admin/article/user/list".equals(path)
                    || "/admin/article/getArticleById".equals(path);
        }
        return false;
    }

    private boolean reject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.tokenInvalid(message)));
        response.getWriter().flush();
        return false;
    }

    /** 维护拦截使用业务错误码（500），避免前端把维护提示当成登录失效而清掉 token */
    private boolean rejectMaintenance(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error("站点维护中，请稍后再访问")));
        response.getWriter().flush();
        return false;
    }

    /** 越权拦截使用业务错误码（500），避免前端把「权限不足」当成登录失效而清 token */
    private boolean rejectForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error("权限不足，当前角色不允许该操作")));
        response.getWriter().flush();
        return false;
    }
}
