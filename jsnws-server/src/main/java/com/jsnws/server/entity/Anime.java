package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

@TableName("anime")
public class Anime {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String cover;
    private BigDecimal score;
    private String type;
    /** B站分类：1番剧 2电影 3纪录片 4国创 5电视剧 7综艺；手工条目为空 */
    private Integer seasonType;
    private String source;
    private String status;
    private String description;
    private String url;
    private String releaseDate;
    /** B站 season_id，同步条目唯一标识 */
    private Long seasonId;
    /** B站 media_id */
    private Long mediaId;
    /** 数据来源：bili=同步 manual=手工 */
    private String origin;
    /** 是否在前台展示，B站取消追番后置 0 保留数据 */
    private Integer visible;
    /** 最近一次从 B 站同步的时间 */
    private Date lastSyncTime;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getSeasonType() { return seasonType; }
    public void setSeasonType(Integer seasonType) { this.seasonType = seasonType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }
    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public Integer getVisible() { return visible; }
    public void setVisible(Integer visible) { this.visible = visible; }
    public Date getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(Date lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
