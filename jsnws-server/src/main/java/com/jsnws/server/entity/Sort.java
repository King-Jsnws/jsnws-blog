package com.jsnws.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;
import java.util.List;

@TableName("sort")
public class Sort {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String sortName;
    private String sortDescription;
    private Integer sortType;
    private Integer priority;
    private Date createTime;

    /** getSortInfo 返回时填充 */
    @TableField(exist = false)
    private Integer countOfSort;
    @TableField(exist = false)
    private List<Label> labels;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSortName() { return sortName; }
    public void setSortName(String sortName) { this.sortName = sortName; }
    public String getSortDescription() { return sortDescription; }
    public void setSortDescription(String sortDescription) { this.sortDescription = sortDescription; }
    public Integer getSortType() { return sortType; }
    public void setSortType(Integer sortType) { this.sortType = sortType; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Integer getCountOfSort() { return countOfSort; }
    public void setCountOfSort(Integer countOfSort) { this.countOfSort = countOfSort; }
    public List<Label> getLabels() { return labels; }
    public void setLabels(List<Label> labels) { this.labels = labels; }
}
