package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("friend")
public class Friend {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String introduction;
    private String cover;
    private String avatar;
    private String url;
    /** 审核状态：0 待审核、1 已通过、2 已拒绝 */
    private Integer status;
    /** 友链档次：0 普通、1 推荐、2 本站主理人 */
    private Integer tier;
    /** 标签，英文逗号分隔 */
    private String tags;
    /** 是否推荐：0 否、1 是；仅叠加视觉与徽标，不改变档次分组 */
    private Integer recommended;
    /** 推荐文案，为空时前台按月生成 */
    private String recommendLabel;
    /** 服务端探测的首响应毫秒，未探测过为空 */
    private Integer latencyMs;
    private Date latencyCheckedAt;
    /** 可用状态：0 正常、1 短暂失联、2 违规 */
    private Integer healthStatus;
    /** 首次探测失败时间 */
    private Date offlineSince;
    /** 违规整改期限 */
    private Date rectifyDeadline;
    /** 友链 RSS 地址 */
    private String rssUrl;
    /** RSS 最近文章时间与标题 */
    private Date rssUpdatedAt;
    private String rssTitle;
    /** 本站点击累计 */
    private Integer clickCount;
    /** 头像主色相（0-360）与饱和度（0-100） */
    private Integer avatarHue;
    private Integer avatarSaturation;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getTier() { return tier; }
    public void setTier(Integer tier) { this.tier = tier; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Integer getRecommended() { return recommended; }
    public void setRecommended(Integer recommended) { this.recommended = recommended; }
    public String getRecommendLabel() { return recommendLabel; }
    public void setRecommendLabel(String recommendLabel) { this.recommendLabel = recommendLabel; }
    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }
    public Date getLatencyCheckedAt() { return latencyCheckedAt; }
    public void setLatencyCheckedAt(Date latencyCheckedAt) { this.latencyCheckedAt = latencyCheckedAt; }
    public Integer getHealthStatus() { return healthStatus; }
    public void setHealthStatus(Integer healthStatus) { this.healthStatus = healthStatus; }
    public Date getOfflineSince() { return offlineSince; }
    public void setOfflineSince(Date offlineSince) { this.offlineSince = offlineSince; }
    public Date getRectifyDeadline() { return rectifyDeadline; }
    public void setRectifyDeadline(Date rectifyDeadline) { this.rectifyDeadline = rectifyDeadline; }
    public String getRssUrl() { return rssUrl; }
    public void setRssUrl(String rssUrl) { this.rssUrl = rssUrl; }
    public Date getRssUpdatedAt() { return rssUpdatedAt; }
    public void setRssUpdatedAt(Date rssUpdatedAt) { this.rssUpdatedAt = rssUpdatedAt; }
    public String getRssTitle() { return rssTitle; }
    public void setRssTitle(String rssTitle) { this.rssTitle = rssTitle; }
    public Integer getClickCount() { return clickCount; }
    public void setClickCount(Integer clickCount) { this.clickCount = clickCount; }
    public Integer getAvatarHue() { return avatarHue; }
    public void setAvatarHue(Integer avatarHue) { this.avatarHue = avatarHue; }
    public Integer getAvatarSaturation() { return avatarSaturation; }
    public void setAvatarSaturation(Integer avatarSaturation) { this.avatarSaturation = avatarSaturation; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
