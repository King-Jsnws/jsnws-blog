package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsnws.server.entity.Article;
import com.jsnws.server.mapper.ArticleMapper;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 站点 Sitemap：只读现有文章表，不新增表与依赖。
 * 收录已发布且无访问密码的文章页，以及前端实际存在的公开静态路由。
 */
@RestController
public class SitemapController {

    /** 站点正式域名，sitemap 内所有地址均使用该绝对地址 */
    private static final String SITE_URL = "https://jsnws.com";

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US).withZone(ZONE);

    /** 与 router/index.js 中实际存在的公开路由保持一致（登录页、后台路由不收录） */
    private static final String[] STATIC_PATHS = {
            "/",
            "/sort",
            "/categories",
            "/tags",
            "/archive",
            "/graph",
            "/jotting",
            "/favorite",
            "/anime",
            "/sponsor",
            "/travel",
            "/message",
            "/about"
    };

    private final ArticleMapper articleMapper;

    public SitemapController(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> sitemap() {
        List<Article> articles = articleMapper.selectList(new QueryWrapper<Article>()
                .eq("view_status", true)
                // 有访问密码的文章不收录，与 RSS 保持一致
                .and(w -> w.isNull("password").or().eq("password", ""))
                .orderByDesc("create_time")
                .orderByDesc("id"));

        String siteLastMod = articles.stream()
                .map(Article::getCreateTime)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .map(date -> DATE.format(date.toInstant()))
                .orElse(LocalDate.now(ZONE).toString());

        StringBuilder xml = new StringBuilder(1024 + articles.size() * 160);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 静态页：lastmod 取全站最新文章的发布时间，没有文章时用当天
        for (String path : STATIC_PATHS) {
            appendUrl(xml, SITE_URL + path, siteLastMod);
        }

        for (Article article : articles) {
            String lastMod = article.getCreateTime() == null
                    ? siteLastMod
                    : DATE.format(article.getCreateTime().toInstant());
            appendUrl(xml, SITE_URL + "/article/" + article.getId(), lastMod);
        }

        xml.append("</urlset>\n");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml;charset=UTF-8"))
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                .body(xml.toString());
    }

    private void appendUrl(StringBuilder xml, String loc, String lastMod) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        xml.append("    <lastmod>").append(lastMod).append("</lastmod>\n");
        xml.append("  </url>\n");
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
                    sb.append(c < 0x20 && c != '\n' && c != '\r' && c != '\t' ? ' ' : c);
            }
        }
        return sb.toString();
    }
}
