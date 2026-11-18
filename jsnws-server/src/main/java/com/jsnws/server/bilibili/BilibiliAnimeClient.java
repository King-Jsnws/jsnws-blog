package com.jsnws.server.bilibili;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * B 站追番接口客户端：分页拉取追番列表，按 season_id 补全评分与题材。
 * 只读、串行请求，页间与详情间按配置延迟，避免触发风控。
 */
@Component
public class BilibiliAnimeClient {

    private static final Logger log = LoggerFactory.getLogger(BilibiliAnimeClient.class);

    private static final String LIST_URL = "https://api.bilibili.com/x/space/bangumi/follow/list";
    private static final String SEASON_URL = "https://api.bilibili.com/pgc/view/web/season";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";
    private static final int MAX_PAGES = 100;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper objectMapper;

    public BilibiliAnimeClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 拉取追番 + 追剧列表（分页直到 total 或空页），配置由调用方按优先级解析后传入。
     * type=0 时同时拉取 1（追番）与 2（追剧），按 season_id 去重；
     * 电影/纪录片/电视剧来自追剧列表，番剧/国创来自追番列表，合并后才能覆盖全部类型页签。
     */
    public List<BiliAnimeItem> fetchFollowList(BiliRuntimeConfig cfg) {
        String uid = trim(cfg.getUid());
        if (uid.isEmpty()) {
            throw new BilibiliApiException(0, "未配置 B 站 UID");
        }
        int pageSize = Math.min(Math.max(cfg.getPageSize(), 1), 30);
        List<Integer> types = new ArrayList<>();
        if (cfg.getType() == 0) {
            types.add(1);
            types.add(2);
        } else {
            types.add(cfg.getType());
        }
        Map<Long, BiliAnimeItem> merged = new LinkedHashMap<>();
        for (Integer type : types) {
            for (BiliAnimeItem item : fetchByType(uid, type, pageSize, cfg)) {
                if (item.getSeasonId() != null) {
                    merged.putIfAbsent(item.getSeasonId(), item);
                }
            }
        }
        List<BiliAnimeItem> items = new ArrayList<>(merged.values());
        log.info("B站关注列表拉取完成：uid={} types={} 去重后={}", uid, types, items.size());
        return items;
    }

    /** 单个列表类型的分页拉取：type=1 追番，type=2 追剧 */
    private List<BiliAnimeItem> fetchByType(String uid, int type, int pageSize, BiliRuntimeConfig cfg) {
        List<BiliAnimeItem> items = new ArrayList<>();
        int total = -1;
        for (int pn = 1; pn <= MAX_PAGES; pn++) {
            String url = LIST_URL
                    + "?type=" + type
                    + "&follow_status=" + cfg.getFollowStatus()
                    + "&pn=" + pn
                    + "&ps=" + pageSize
                    + "&vmid=" + uid;
            JsonNode root = getJson(url, cfg);
            int code = root.path("code").asInt(-1);
            if (code != 0) {
                throw buildError(code, root.path("message").asText(""));
            }
            JsonNode list = root.path("data").path("list");
            if (!list.isArray() || list.size() == 0) {
                break;
            }
            for (JsonNode node : list) {
                items.add(mapItem(node));
            }
            total = root.path("data").path("total").asInt(total);
            if (total >= 0 && items.size() >= total) {
                break;
            }
            if (list.size() < pageSize) {
                break;
            }
            if (pn < MAX_PAGES) {
                sleep(cfg.getPageDelayMs());
            }
        }
        log.info("B站关注列表：uid={} type={} total={} 实际={}", uid, type, total, items.size());
        return items;
    }

    /** 按 season_id 补全评分与题材；失败由调用方决定是否保留旧值 */
    public void enrich(BiliAnimeItem item, BiliRuntimeConfig cfg) {
        if (item == null || item.getSeasonId() == null) {
            return;
        }
        JsonNode root = getJson(SEASON_URL + "?season_id=" + item.getSeasonId(), cfg);
        int code = root.path("code").asInt(-1);
        if (code != 0) {
            throw buildError(code, root.path("message").asText(""));
        }
        JsonNode result = root.path("result");
        JsonNode seasonType = result.path("type");
        if (seasonType.isNumber() && seasonType.asInt() > 0) {
            item.setSeasonType(seasonType.asInt());
        }
        JsonNode rating = result.path("rating");
        if (rating.hasNonNull("score")) {
            double score = rating.get("score").asDouble(0);
            if (score > 0) {
                item.setScore(BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP));
            }
        }
        JsonNode styles = result.path("styles");
        if (styles.isArray() && styles.size() > 0) {
            List<String> names = new ArrayList<>();
            for (JsonNode style : styles) {
                String name = style.asText("").trim();
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
            if (!names.isEmpty()) {
                item.setGenre(String.join(" / ", names));
            }
        }
        if (isBlank(item.getDescription())) {
            String summary = text(result, "summary");
            if (isBlank(summary)) {
                summary = text(result, "evaluate");
            }
            item.setDescription(summary);
        }
        sleep(cfg.getDetailDelayMs());
    }

    private JsonNode getJson(String url, BiliRuntimeConfig cfg) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(Math.max(1000L, cfg.getTimeoutMs())))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://www.bilibili.com/")
                .header("Accept", "application/json, text/plain, */*")
                .GET();
        String sessdata = normalizeSessdata(cfg.getSessdata());
        if (!sessdata.isEmpty()) {
            builder.header("Cookie", "SESSDATA=" + sessdata);
        }
        try {
            HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new BilibiliApiException(response.statusCode(),
                        "B站返回 HTTP " + response.statusCode() + "，可能触发风控");
            }
            return objectMapper.readTree(response.body());
        } catch (BilibiliApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BilibiliApiException(-1, "B站请求被中断");
        } catch (JsonProcessingException e) {
            throw new BilibiliApiException(-1, "B站返回内容无法解析，可能触发风控");
        } catch (Exception e) {
            throw new BilibiliApiException(-1, "B站请求失败：" + e.getMessage());
        }
    }

    private BiliAnimeItem mapItem(JsonNode node) {
        BiliAnimeItem item = new BiliAnimeItem();
        item.setSeasonId(node.path("season_id").isNumber() ? node.get("season_id").asLong() : null);
        item.setMediaId(node.path("media_id").isNumber() ? node.get("media_id").asLong() : null);
        String title = text(node, "title");
        if (isBlank(title)) {
            title = text(node, "season_title");
        }
        item.setTitle(title);
        item.setCover(toHttps(text(node, "cover")));
        item.setTypeName(text(node, "season_type_name"));
        item.setSeasonType(seasonTypeOf(node));

        JsonNode newEp = node.path("new_ep");
        String statusText = text(newEp, "index_show");
        if (isBlank(statusText)) {
            if (node.path("is_finish").asInt(0) == 1) {
                statusText = "已完结";
            } else if (node.path("is_started").asInt(0) == 1) {
                statusText = "连载中";
            } else {
                statusText = "未开播";
            }
        }
        item.setStatusText(statusText);
        boolean finished = node.path("is_finish").asInt(0) == 1;
        item.setFinished(finished);

        String description = text(node, "summary");
        if (isBlank(description)) {
            description = text(node, "evaluate");
        }
        item.setDescription(description);

        item.setUrl(text(node, "url"));

        JsonNode publish = node.path("publish");
        String releaseDate = text(publish, "release_date");
        if (isBlank(releaseDate)) {
            String pubTime = text(publish, "pub_time");
            if (pubTime.length() >= 10) {
                releaseDate = pubTime.substring(0, 10);
            }
        }
        item.setReleaseDate(releaseDate);
        return item;
    }

    /**
     * B 站分类：优先取数值 season_type，缺失时按 season_type_name 反查。
     * 1番剧 2电影 3纪录片 4国创 5电视剧 7综艺
     */
    private Integer seasonTypeOf(JsonNode node) {
        JsonNode type = node.path("season_type");
        if (type.isNumber() && type.asInt() > 0) {
            return type.asInt();
        }
        switch (text(node, "season_type_name")) {
            case "番剧": return 1;
            case "电影": return 2;
            case "纪录片": return 3;
            case "国创": return 4;
            case "电视剧": return 5;
            case "综艺": return 7;
            default: return null;
        }
    }

    private BilibiliApiException buildError(int code, String message) {
        String detail;
        switch (code) {
            case -101:
                detail = "B站账号未登录，请配置 SESSDATA";
                break;
            case 53013:
                detail = "B站追番隐私设置未公开，请配置 SESSDATA 或把该账号追番隐私设为公开";
                break;
            case -412:
                detail = "B站风控拦截（-412），请降低请求频率或更换网络";
                break;
            case -352:
                detail = "B站风控校验失败（-352），需要人工验证";
                break;
            case -400:
                detail = "B站接口参数错误（-400）";
                break;
            default:
                detail = "B站接口返回 code=" + code + "：" + message;
        }
        return new BilibiliApiException(code, detail);
    }

    private String normalizeSessdata(String raw) {
        String value = trim(raw);
        if (value.regionMatches(true, 0, "SESSDATA=", 0, 9)) {
            value = value.substring(9);
        }
        int semicolon = value.indexOf(';');
        if (semicolon >= 0) {
            value = value.substring(0, semicolon);
        }
        return value.trim();
    }

    private String toHttps(String url) {
        if (url == null) {
            return null;
        }
        String value = url.trim();
        if (value.startsWith("http://")) {
            return "https://" + value.substring(7);
        }
        return value;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return "";
        }
        return value.asText("").trim();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 接口错误或网络错误，message 已转成可直接展示给管理员的文案 */
    public static class BilibiliApiException extends RuntimeException {

        private final int code;

        public BilibiliApiException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
