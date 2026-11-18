package com.jsnws.server.bilibili;

import java.math.BigDecimal;

/**
 * B 站追番列表的规范化条目：只保留现有 anime 表能消费的字段。
 */
public class BiliAnimeItem {

    private Long seasonId;
    private Long mediaId;
    private String title;
    private String cover;
    private String typeName;
    /** B站分类：1番剧 2电影 3纪录片 4国创 5电视剧 7综艺 */
    private Integer seasonType;
    private String statusText;
    private boolean finished;
    private String description;
    private String url;
    private String releaseDate;
    /** 详情补全得到，列表接口不返回 */
    private BigDecimal score;
    /** 详情补全得到的题材，如 “奇幻 / 冒险” */
    private String genre;

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }
    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public Integer getSeasonType() { return seasonType; }
    public void setSeasonType(Integer seasonType) { this.seasonType = seasonType; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
}
