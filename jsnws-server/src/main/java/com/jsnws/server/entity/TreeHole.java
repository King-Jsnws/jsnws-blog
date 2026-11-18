package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

@TableName("tree_hole")
public class TreeHole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String avatar;
    private String message;
    /** 发言客户端 IP，仅落库用于安全审核，不随前台接口返回 */
    @JsonIgnore
    private String ip;
    private Date createTime;
    /** 展示用昵称：由 user_id 关联 sys_user 实时补齐，不落库 */
    @TableField(exist = false)
    private String nickname;
    /** 后台弹幕列表展示用 IP（仅 /admin/treeHole/** 填充，前台接口恒为 null） */
    @TableField(exist = false)
    private String ipAddress;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
