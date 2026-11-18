package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 更新日志：前台 /changelog 时间线与关系图展示，后台手动录入
 */
@TableName("changelog")
public class Changelog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String version;
    private String logDate;
    private String tone;
    private String kind;
    private String title;
    private String summary;
    /** 涉及内容，JSON 数组字符串 */
    private String topics;
    /** 更新详情，JSON 数组字符串（每项一条） */
    private String details;
    /** 相关更新 id，英文逗号分隔 */
    private String relatedIds;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getLogDate() { return logDate; }
    public void setLogDate(String logDate) { this.logDate = logDate; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getTopics() { return topics; }
    public void setTopics(String topics) { this.topics = topics; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getRelatedIds() { return relatedIds; }
    public void setRelatedIds(String relatedIds) { this.relatedIds = relatedIds; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
