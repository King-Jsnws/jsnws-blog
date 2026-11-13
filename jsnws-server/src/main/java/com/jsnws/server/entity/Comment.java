package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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
    public List<Comment> getChildComments() { return childComments; }
    public void setChildComments(List<Comment> childComments) { this.childComments = childComments; }
    public Integer getChildTotal() { return childTotal; }
    public void setChildTotal(Integer childTotal) { this.childTotal = childTotal; }
    public Integer getChildCurrent() { return childCurrent; }
    public void setChildCurrent(Integer childCurrent) { this.childCurrent = childCurrent; }
    public String getParentUsername() { return parentUsername; }
    public void setParentUsername(String parentUsername) { this.parentUsername = parentUsername; }
}
