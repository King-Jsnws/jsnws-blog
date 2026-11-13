package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("label")
public class Label {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sortId;
    private String labelName;
    private String labelDescription;
    private Date createTime;

    /** getSortInfo 返回时填充 */
    @TableField(exist = false)
    private Integer countOfLabel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSortId() { return sortId; }
    public void setSortId(Long sortId) { this.sortId = sortId; }
    public String getLabelName() { return labelName; }
    public void setLabelName(String labelName) { this.labelName = labelName; }
    public String getLabelDescription() { return labelDescription; }
    public void setLabelDescription(String labelDescription) { this.labelDescription = labelDescription; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Integer getCountOfLabel() { return countOfLabel; }
    public void setCountOfLabel(Integer countOfLabel) { this.countOfLabel = countOfLabel; }
}
