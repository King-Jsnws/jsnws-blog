package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("family")
public class Family {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String bgCover;
    private String manCover;
    private String womanCover;
    private String manName;
    private String womanName;
    private String countdownTitle;
    private String countdownTime;
    private String timing;
    private String familyInfo;
    private Boolean status;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getBgCover() { return bgCover; }
    public void setBgCover(String bgCover) { this.bgCover = bgCover; }
    public String getManCover() { return manCover; }
    public void setManCover(String manCover) { this.manCover = manCover; }
    public String getWomanCover() { return womanCover; }
    public void setWomanCover(String womanCover) { this.womanCover = womanCover; }
    public String getManName() { return manName; }
    public void setManName(String manName) { this.manName = manName; }
    public String getWomanName() { return womanName; }
    public void setWomanName(String womanName) { this.womanName = womanName; }
    public String getCountdownTitle() { return countdownTitle; }
    public void setCountdownTitle(String countdownTitle) { this.countdownTitle = countdownTitle; }
    public String getCountdownTime() { return countdownTime; }
    public void setCountdownTime(String countdownTime) { this.countdownTime = countdownTime; }
    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }
    public String getFamilyInfo() { return familyInfo; }
    public void setFamilyInfo(String familyInfo) { this.familyInfo = familyInfo; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
