package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("web_info")
public class WebInfo {

    /** 单行记录，id 固定为 1 */
    @TableId(type = IdType.INPUT)
    private Long id;
    private String webName;
    private String webTitle;
    private String description;
    private String notices;
    private String randomCover;
    private String randomAvatar;
    private String randomName;
    private String footer;
    private String backgroundImage;
    private String avatar;
    private String defaultStoreType;
    private String wechat;
    private String alipay;
    private String qq;
    private String github;
    private String email;
    private String bilibili;
    private Boolean status;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWebName() { return webName; }
    public void setWebName(String webName) { this.webName = webName; }
    public String getWebTitle() { return webTitle; }
    public void setWebTitle(String webTitle) { this.webTitle = webTitle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNotices() { return notices; }
    public void setNotices(String notices) { this.notices = notices; }
    public String getRandomCover() { return randomCover; }
    public void setRandomCover(String randomCover) { this.randomCover = randomCover; }
    public String getRandomAvatar() { return randomAvatar; }
    public void setRandomAvatar(String randomAvatar) { this.randomAvatar = randomAvatar; }
    public String getRandomName() { return randomName; }
    public void setRandomName(String randomName) { this.randomName = randomName; }
    public String getFooter() { return footer; }
    public void setFooter(String footer) { this.footer = footer; }
    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getDefaultStoreType() { return defaultStoreType; }
    public void setDefaultStoreType(String defaultStoreType) { this.defaultStoreType = defaultStoreType; }
    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }
    public String getAlipay() { return alipay; }
    public void setAlipay(String alipay) { this.alipay = alipay; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBilibili() { return bilibili; }
    public void setBilibili(String bilibili) { this.bilibili = bilibili; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
