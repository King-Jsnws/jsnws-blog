package com.jsnws.server.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * AI 站长（user_type=5）权限配置：这是该角色权限的唯一维护入口。
 *
 * 已开放（日常内容与站点设置编辑）：
 * 文章、评论/留言/弹幕、分类、标签、资源/相册、追番、音乐、站点基本信息设置。
 *
 * 明确不开放：
 * 账号与权限管理（/admin/user/**）、关闭或恢复站点（updateWebInfo 中的 status 字段）、
 * 数据库与运维类操作（同步任务、B 站接入配置、邮件测试、访问明细等）。
 *
 * 调整权限时只改本类的路径集合即可，拦截器与控制器均引用这里。
 */
public final class AiStationPermissions {

    // ===== 权限点（供后续细粒度扩展与文档对照） =====
    public static final String ARTICLE = "article";
    public static final String COMMENT = "comment";
    public static final String TAXONOMY = "taxonomy";
    public static final String RESOURCE = "resource";
    public static final String SETTINGS = "settings";

    /** /admin/** 下按前缀放行的路径（文章、评论、留言/弹幕） */
    private static final Set<String> ADMIN_PREFIXES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "/admin/article/",   // 文章列表/详情/状态
            "/admin/comment/",   // 评论管理
            "/admin/treeHole/"   // 留言、弹幕
    )));

    /** /admin/** 下按完整路径放行的接口（站点设置读取） */
    private static final Set<String> ADMIN_PATHS = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
            "/admin/webInfo/getAdminWebInfo"
    )));

    /** 非 /admin 前缀的接口白名单：写操作、分类标签、资源与站点设置 */
    private static final Set<String> API_PATHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            // 文章编辑
            "/article/saveArticle",
            "/article/updateArticle",
            "/article/deleteArticle",
            // 分类（taxonomy）
            "/webInfo/saveSort",
            "/webInfo/updateSort",
            "/webInfo/deleteSort",
            // 标签（taxonomy）
            "/webInfo/saveLabel",
            "/webInfo/updateLabel",
            "/webInfo/deleteLabel",
            // 资源/相册（resource）
            "/webInfo/listAdminResourcePath",
            "/webInfo/saveResourcePath",
            "/webInfo/updateResourcePath",
            "/webInfo/deleteResourcePath",
            "/resource/upload",
            "/resource/saveResource",
            "/resource/listResource",
            "/resource/deleteResource",
            "/resource/changeResourceStatus",
            "/qiniu/getUpToken",
            // 追番/音乐（resource）
            "/webInfo/listAdminAnime",
            "/webInfo/saveAnime",
            "/webInfo/updateAnime",
            "/webInfo/deleteAnime",
            "/webInfo/saveMusic",
            "/webInfo/updateMusic",
            "/webInfo/deleteMusic",
            // 弹幕删除（后台列表走 /admin/treeHole/**）
            "/webInfo/deleteTreeHole",
            // 站点基本信息（settings）：status 字段由 WebInfoController 单独锁定
            "/webInfo/updateWebInfo"
    )));

    private AiStationPermissions() {
    }

    /** AI 站长可访问的 /admin/** 接口 */
    public static boolean canUseAdminPath(String path) {
        if (path == null) {
            return false;
        }
        if (ADMIN_PATHS.contains(path)) {
            return true;
        }
        for (String prefix : ADMIN_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /** AI 站长可访问的普通管理接口（不含公开接口，公开接口由拦截器统一放行） */
    public static boolean canUseApiPath(String path) {
        return path != null && API_PATHS.contains(path);
    }
}
