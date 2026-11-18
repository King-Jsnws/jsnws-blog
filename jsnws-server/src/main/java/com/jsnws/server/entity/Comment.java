package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private String commentContent;
    private String source;
    private String commentType;
    private Long floorCommentId;
    private Long parentCommentId;
    private Long parentUserId;
    private Date createTime;

    /** 客户端 IP，仅落库用于安全审核，不随接口返回 */
    @JsonIgnore
    private String ip;
    /** IP 归属地（省份·城市·运营商）；无离线 IP 库时统一写入「未知」 */
    private String location;
    /** 浏览器名称+版本（发表时解析 User-Agent，如 Chrome151.0） */
    private String browser;
    /** 操作系统名称+版本（发表时解析 User-Agent，如 macOS 10.15.7） */
    private String os;
    /** 置顶标记：1 置顶，0 普通；仅站长可操作 */
    private Integer isTop;
    /** 置顶时间，取消置顶时置空 */
    private Date topTime;
    /** 匿名标记：1 匿名，0 普通 */
    private Integer isAnonymous;
    /** 匿名留言随机头像地址（仅匿名时写入，来自前端本地素材） */
    private String anonymousAvatar;
    /** 点赞数 */
    private Integer likeCount;

    /** 子评论列表（楼层评论返回时填充） */
    @TableField(exist = false)
    private List<Comment> childComments;
    @TableField(exist = false)
    private Integer childTotal;
    @TableField(exist = false)
    private Integer childCurrent;
    /** 被回复人用户名 */
    @TableField(exist = false)
    private String parentUsername;
    /** 评论者昵称（非数据库字段，查询时从 sys_user 填充） */
    @TableField(exist = false)
    private String nickname;
    /** 评论者角色等级（非数据库字段，前台列表按 UserRole 归一化后从 sys_user 填充） */
    @TableField(exist = false)
    private Integer userType;
    /** 后台评论/留言列表展示用 IP（仅 /admin/comment/** 填充，前台接口恒为 null） */
    @TableField(exist = false)
    private String ipAddress;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCommentType() { return commentType; }
    public void setCommentType(String commentType) { this.commentType = commentType; }
    public Long getFloorCommentId() { return floorCommentId; }
    public void setFloorCommentId(Long floorCommentId) { this.floorCommentId = floorCommentId; }
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    public Long getParentUserId() { return parentUserId; }
    public void setParentUserId(Long parentUserId) { this.parentUserId = parentUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }
    public Date getTopTime() { return topTime; }
    public void setTopTime(Date topTime) { this.topTime = topTime; }
    public Integer getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }
    public String getAnonymousAvatar() { return anonymousAvatar; }
    public void setAnonymousAvatar(String anonymousAvatar) { this.anonymousAvatar = anonymousAvatar; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public List<Comment> getChildComments() { return childComments; }
    public void setChildComments(List<Comment> childComments) { this.childComments = childComments; }
    public Integer getChildTotal() { return childTotal; }
    public void setChildTotal(Integer childTotal) { this.childTotal = childTotal; }
    public Integer getChildCurrent() { return childCurrent; }
    public void setChildCurrent(Integer childCurrent) { this.childCurrent = childCurrent; }
    public String getParentUsername() { return parentUsername; }
    public void setParentUsername(String parentUsername) { this.parentUsername = parentUsername; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getUserType() { return userType; }
    public void setUserType(Integer userType) { this.userType = userType; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
