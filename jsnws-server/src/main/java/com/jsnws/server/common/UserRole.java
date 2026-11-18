package com.jsnws.server.common;

/**
 * 角色等级：数字越小权限越高
 * 1 本站主理人（由 jsnws.boss-user-id 固定）
 * 2 管理员（友链、留言、评论、赞助、娱乐）
 * 3 作者（新增/编辑自己的文章）
 * 4 普通用户（无后台权限）
 * 5 AI 站长（站点日常内容/设置编辑，账号与站点开关除外，权限见 AiStationPermissions）
 */
public final class UserRole {

    public static final int BOSS = 1;
    public static final int ADMIN = 2;
    public static final int AUTHOR = 3;
    public static final int USER = 4;
    /** AI 站长：新增的第三种后台角色，不复用 1-4 的既有编号 */
    public static final int AI_OWNER = 5;

    private UserRole() {
    }

    /** 后台可授予、可存储的用户类型（主理人不可授予） */
    public static boolean assignable(Integer type) {
        return type != null && type >= ADMIN && type <= AI_OWNER;
    }

    /** 把数据库中的 user_type 归一化为等级：兼容旧数据 0=普通、1=旧管理员 */
    public static int normalize(Integer type) {
        if (type == null || type <= 0) {
            return USER;
        }
        if (type == 1) {
            return ADMIN;
        }
        if (type > AI_OWNER) {
            return USER;
        }
        return type;
    }

    /** 角色的中文名称，登录与用户列表展示用 */
    public static String label(Integer type) {
        int role = normalize(type);
        if (role == BOSS) {
            return "本站主理人";
        }
        if (role == ADMIN) {
            return "管理员";
        }
        if (role == AUTHOR) {
            return "作者";
        }
        if (role == AI_OWNER) {
            return "AI 站长";
        }
        // 数据库里的 1 是旧管理员，展示为「管理员」，主理人仅由登录身份判定
        return "普通用户";
    }
}
