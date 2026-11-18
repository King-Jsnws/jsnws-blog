package com.jsnws.server.bilibili;

/**
 * 后台保存 B 站同步配置的请求体。
 * 字段为 null 表示不修改；sessdata 为空字符串表示保留原值。
 */
public class BilibiliConfigRequest {

    private String uid;
    private String sessdata;
    private Boolean enabled;
    private Long intervalMs;
    private Integer type;
    private Boolean detailEnrich;

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getSessdata() { return sessdata; }
    public void setSessdata(String sessdata) { this.sessdata = sessdata; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Long getIntervalMs() { return intervalMs; }
    public void setIntervalMs(Long intervalMs) { this.intervalMs = intervalMs; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Boolean getDetailEnrich() { return detailEnrich; }
    public void setDetailEnrich(Boolean detailEnrich) { this.detailEnrich = detailEnrich; }
}
