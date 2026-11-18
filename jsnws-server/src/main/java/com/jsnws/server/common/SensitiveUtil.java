package com.jsnws.server.common;

/**
 * 敏感信息脱敏：接口返回时只保留前几位，避免明文外泄。
 */
public final class SensitiveUtil {

    private static final int KEEP_PREFIX = 6;
    private static final String MASK = "******";

    private SensitiveUtil() {
    }

    /**
     * 掩码规则：长度大于 6 时返回「前 6 位 + ...」，否则统一返回 ******。
     * 空值返回空串，便于前端判断「未配置」。
     */
    public static String maskSecret(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        if (text.isEmpty()) {
            return "";
        }
        if (text.length() <= KEEP_PREFIX) {
            return MASK;
        }
        return text.substring(0, KEEP_PREFIX) + "...";
    }

    /**
     * 只保留末几位（用于授权码回显，如 ****a1b2）。
     * 长度不足时统一返回 ****，避免把短密码整段暴露。
     */
    public static String maskTail(String value, int keepTail) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        if (text.isEmpty()) {
            return "";
        }
        if (keepTail <= 0 || text.length() <= keepTail) {
            return "****";
        }
        return "****" + text.substring(text.length() - keepTail);
    }

    /** 判断前端回传的是否为脱敏占位值（****开头），避免把掩码写进数据库 */
    public static boolean isMasked(String value) {
        if (value == null) {
            return false;
        }
        return value.trim().matches("^\\*{4,}.*$");
    }
}
