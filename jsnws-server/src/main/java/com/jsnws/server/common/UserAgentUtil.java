package com.jsnws.server.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-Agent 解析：浏览器、操作系统与设备类型。
 * 供评论落库与站点统计数据聚合共用，分类结果保持与评论模块一致。
 */
public final class UserAgentUtil {

    private UserAgentUtil() {
    }

    /** 浏览器名称（不含版本），无法识别时返回「其他」 */
    public static String browserName(String ua) {
        if (ua == null || ua.isEmpty()) {
            return null;
        }
        if (ua.contains("Edg/") || ua.contains("EdgA/") || ua.contains("EdgiOS/")) return "Edge";
        if (ua.contains("OPR/")) return "Opera";
        if (ua.contains("MicroMessenger/")) return "WeChat";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Chrome/") || ua.contains("CriOS/")) return "Chrome";
        if (ua.contains("Safari")) return "Safari";
        return "其他";
    }

    /** 从 User-Agent 解析浏览器名称+版本，格式与参考站一致（如 Chrome151.0） */
    public static String resolveBrowser(String ua) {
        if (ua == null || ua.isEmpty()) {
            return null;
        }
        String version;
        if ((version = uaVersion(ua, "Edg(?:A|iOS)?/([\\d.]+)")) != null) return "Edge" + shortVersion(version);
        if ((version = uaVersion(ua, "OPR/([\\d.]+)")) != null) return "Opera" + shortVersion(version);
        if ((version = uaVersion(ua, "MicroMessenger/([\\d.]+)")) != null) return "WeChat" + shortVersion(version);
        if ((version = uaVersion(ua, "Firefox/([\\d.]+)")) != null) return "Firefox" + shortVersion(version);
        if ((version = uaVersion(ua, "Chrome/([\\d.]+)")) != null) return "Chrome" + shortVersion(version);
        if ((version = uaVersion(ua, "Version/([\\d.]+).*Safari")) != null) return "Safari" + shortVersion(version);
        if (ua.contains("Safari")) return "Safari";
        return null;
    }

    /** 从 User-Agent 解析操作系统名称+版本（如 Windows 10 / macOS 10.15.7 / Android 13 / iOS 17.1） */
    public static String resolveOs(String ua) {
        if (ua == null || ua.isEmpty()) {
            return null;
        }
        if (ua.contains("Windows NT 10.0")) return "Windows 10";
        if (ua.contains("Windows NT 6.3")) return "Windows 8.1";
        if (ua.contains("Windows NT 6.2")) return "Windows 8";
        if (ua.contains("Windows NT 6.1")) return "Windows 7";
        if (ua.contains("Windows")) return "Windows";
        String version;
        if ((version = uaVersion(ua, "Android\\s([\\d.]+)")) != null) return "Android " + version;
        if (ua.contains("Android")) return "Android";
        if ((version = uaVersion(ua, "(?:iPhone )?OS (\\d+)[_.](\\d+)(?:[_.](\\d+))?")) != null) {
            return "iOS " + version;
        }
        if ((version = uaVersion(ua, "Mac OS X (\\d+)[_.](\\d+)(?:[_.](\\d+))?")) != null) {
            return "macOS " + version;
        }
        if (ua.contains("CrOS")) return "Chrome OS";
        if (ua.contains("Linux")) return "Linux";
        return null;
    }

    /**
     * 设备类型：Tablet / Mobile / Desktop / 其他。
     * User-Agent 无法可靠区分笔记本与台式机，因此桌面端统一归为 Desktop，不臆造 Laptop。
     */
    public static String device(String ua) {
        if (ua == null || ua.isEmpty()) {
            return null;
        }
        String lower = ua.toLowerCase();
        if (lower.contains("ipad") || lower.contains("tablet") || lower.contains("kindle")
                || (lower.contains("android") && !lower.contains("mobile"))) {
            return "Tablet";
        }
        if (lower.contains("mobile") || lower.contains("iphone") || lower.contains("ipod")
                || lower.contains("android") || lower.contains("windows phone")) {
            return "Mobile";
        }
        if (lower.contains("windows") || lower.contains("macintosh") || lower.contains("mac os x")
                || lower.contains("linux") || lower.contains("cros")) {
            return "Desktop";
        }
        return "其他";
    }

    /** 取 UA 中正则的首个捕获组；多个捕获组时按「主.次[.补丁]」拼接 */
    private static String uaVersion(String ua, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(ua);
        if (!matcher.find()) {
            return null;
        }
        StringBuilder version = new StringBuilder(matcher.group(1));
        for (int i = 2; i <= matcher.groupCount(); i++) {
            if (matcher.group(i) != null) {
                version.append('.').append(matcher.group(i));
            }
        }
        return version.toString();
    }

    /** 浏览器版本只保留主.次两段，避免 151.0.0.0 这类冗长版本号 */
    private static String shortVersion(String version) {
        String[] parts = version.split("\\.");
        return parts.length > 1 ? parts[0] + "." + parts[1] : parts[0];
    }
}
