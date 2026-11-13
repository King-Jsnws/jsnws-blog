package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String articleTitle;
    private String articleContent;
    private String articleCover;
    private Boolean hasVideo;
    private String videoUrl;
    private Long sortId;
    private Long labelId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean viewStatus;
    private Boolean commentStatus;
    private Boolean recommendStatus;
    private String password;
    private String tips;
    private Date createTime;

    /** 列表/详情返回时填充的关联对象 */
    @TableField(exist = false)
    private Sort sort;
    @TableField(exist = false)
    private Label label;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getArticleTitle() { return articleTitle; }
    public void setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; }
    public String getArticleContent() { return articleContent; }
    public void setArticleContent(String articleContent) { this.articleContent = articleContent; }
    public String getArticleCover() { return articleCover; }
    public void setArticleCover(String articleCover) { this.articleCover = articleCover; }
    public Boolean getHasVideo() { return hasVideo; }
    public void setHasVideo(Boolean hasVideo) { this.hasVideo = hasVideo; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Long getSortId() { return sortId; }
    public void setSortId(Long sortId) { this.sortId = sortId; }
    public Long getLabelId() { return labelId; }
    public void setLabelId(Long labelId) { this.labelId = labelId; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Boolean getViewStatus() { return viewStatus; }
    public void setViewStatus(Boolean viewStatus) { this.viewStatus = viewStatus; }
    public Boolean getCommentStatus() { return commentStatus; }
    public void setCommentStatus(Boolean commentStatus) { this.commentStatus = commentStatus; }
    public Boolean getRecommendStatus() { return recommendStatus; }
    public void setRecommendStatus(Boolean recommendStatus) { this.recommendStatus = recommendStatus; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Sort getSort() { return sort; }
    public void setSort(Sort sort) { this.sort = sort; }
    public Label getLabel() { return label; }
    public void setLabel(Label label) { this.label = label; }
}
