package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.entity.Article;
import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.mapper.ArticleMapper;
import com.jsnws.server.mapper.WebInfoMapper;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 站点 RSS 2.0 订阅：只读现有文章表，不新增表与依赖。
 * 默认仅收录已发布（view_status=1）且无访问密码的文章，按发布时间倒序。
 */
@RestController
public class RssController {

    /** 站点正式域名，RSS 内所有链接均使用该绝对地址 */
    private static final String SITE_URL = "https://jsnws.com";

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int SUMMARY_LENGTH = 200;

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter RFC_1123 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    private static final Pattern FENCED_CODE = Pattern.compile("(?s)```.*?```");
    private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]]*]\\([^)]*\\)");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]*)]\\([^)]*\\)");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`]*)`");
    private static final Pattern LINE_MARKER = Pattern.compile("(?m)^\\s{0,3}(#{1,6}\\s*|>\\s*|[-*+]\\s+|\\d+\\.\\s+)");
    private static final Pattern EMPHASIS = Pattern.compile("[*_~]{1,3}");
    private static final Pattern SPACES = Pattern.compile("[ \\t\\x0B\\f\\r\\n]+");

    private final ArticleMapper articleMapper;
    private final WebInfoMapper webInfoMapper;

    public RssController(ArticleMapper articleMapper, WebInfoMapper webInfoMapper) {
        this.articleMapper = articleMapper;
        this.webInfoMapper = webInfoMapper;
    }

    /**
     * @param limit 条数上限，默认 20，最大 50；非法值回落到默认值
     */
    @GetMapping(value = "/rss.xml", produces = "application/rss+xml;charset=UTF-8")
    public ResponseEntity<String> rss(@RequestParam(value = "limit", required = false) Integer limit) {
        int size = normalizeLimit(limit);
        List<Article> articles = articleMapper.selectList(new QueryWrapper<Article>()
                .eq("view_status", true)
                // 有访问密码的文章不进入 RSS，避免摘要泄露正文
                .and(w -> w.isNull("password").or().eq("password", ""))
                .orderByDesc("create_time")
                .orderByDesc("id")
                .last("LIMIT " + size));

        WebInfo webInfo = webInfoMapper.selectById(1L);
        String channelTitle = firstNotBlank(webInfo == null ? null : webInfo.getWebName(),
                webInfo == null ? null : webInfo.getTabTitle(), "博客");
        String channelDescription = firstNotBlank(webInfo == null ? null : webInfo.getDescription(),
                webInfo == null ? null : webInfo.getFooter(), channelTitle);

        StringBuilder xml = new StringBuilder(1024 + articles.size() * 512);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        xml.append("  <channel>\n");
        xml.append("    <title>").append(escapeXml(channelTitle)).append("</title>\n");
        xml.append("    <link>").append(SITE_URL).append("/</link>\n");
        xml.append("    <description>").append(escapeXml(channelDescription)).append("</description>\n");
        xml.append("    <language>zh-CN</language>\n");
        xml.append("    <lastBuildDate>").append(RFC_1123.format(java.time.ZonedDateTime.now(ZONE))).append("</lastBuildDate>\n");
        xml.append("    <generator>jsnws-server</generator>\n");
        xml.append("    <atom:link href=\"").append(SITE_URL).append("/rss.xml\" rel=\"self\" type=\"application/rss+xml\"/>\n");

        for (Article article : articles) {
            String link = SITE_URL + "/article/" + article.getId();
            String summary = summarize(article.getArticleContent());
            xml.append("    <item>\n");
            xml.append("      <title>").append(escapeXml(article.getArticleTitle())).append("</title>\n");
            xml.append("      <link>").append(link).append("</link>\n");
            xml.append("      <guid isPermaLink=\"true\">").append(link).append("</guid>\n");
            xml.append("      <pubDate>")
                    .append(article.getCreateTime() == null
                            ? RFC_1123.format(java.time.ZonedDateTime.now(ZONE))
                            : RFC_1123.format(article.getCreateTime().toInstant().atZone(ZONE)))
                    .append("</pubDate>\n");
            xml.append("      <description>").append(escapeXml(summary.isEmpty() ? article.getArticleTitle() : summary))
                    .append("</description>\n");
            xml.append("    </item>\n");
        }

        xml.append("  </channel>\n");
        xml.append("</rss>\n");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/rss+xml;charset=UTF-8"))
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                .body(xml.toString());
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /** 把 Markdown 正文压成一段纯文本摘要 */
    private String summarize(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String text = FENCED_CODE.matcher(content).replaceAll(" ");
        text = IMAGE.matcher(text).replaceAll("");
        text = LINK.matcher(text).replaceAll("$1");
        text = HTML_TAG.matcher(text).replaceAll(" ");
        text = INLINE_CODE.matcher(text).replaceAll("$1");
        text = LINE_MARKER.matcher(text).replaceAll("");
        text = EMPHASIS.matcher(text).replaceAll("");
        text = SPACES.matcher(text).replaceAll(" ").trim();
        text = unescapeBasic(text);
        return truncate(text, SUMMARY_LENGTH);
    }

    /** 常见 HTML 实体还原，避免后续转义出现 &amp;amp; 之类的双重编码 */
    private String unescapeBasic(String text) {
        return text.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }

    /** 按码点截断，避免截断 emoji 等代理对字符 */
    private String truncate(String text, int maxCodePoints) {
        if (text.codePointCount(0, text.length()) <= maxCodePoints) {
            return text;
        }
        int end = text.offsetByCodePoints(0, maxCodePoints);
        return text.substring(0, end) + "…";
    }

    private static String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String escapeXml(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    // XML 1.0 不允许的控制字符统一替换为空格
                    sb.append(c < 0x20 && c != '\n' && c != '\r' && c != '\t' ? ' ' : c);
            }
        }
        return sb.toString();
    }
}
