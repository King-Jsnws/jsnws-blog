package com.jsnws.server.bilibili;

import com.jsnws.server.config.BilibiliProperties;

/**
 * 一次同步实际使用的配置快照。
 * 先复制 application.yml / 环境变量的值，再由 BilibiliConfigService 用后台配置覆盖，
 * 因此优先级为：后台配置 > application.yml > 环境变量 BILI_SESSDATA。
 */
public class BiliRuntimeConfig {

    private boolean enabled;
    private String uid;
    private String sessdata;
    private long intervalMs;
    private int type;
    private int followStatus;
    private int pageSize;
    private boolean detailEnrich;
    private int detailLimit;
    private long timeoutMs;
    private long pageDelayMs;
    private long detailDelayMs;

    public BiliRuntimeConfig() {
    }

    public BiliRuntimeConfig(BilibiliProperties properties) {
        this.enabled = properties.isEnabled();
        this.uid = properties.getUid();
        this.sessdata = properties.getSessdata();
        this.intervalMs = properties.getIntervalMs();
        this.type = properties.getType();
        this.followStatus = properties.getFollowStatus();
        this.pageSize = properties.getPageSize();
        this.detailEnrich = properties.isDetailEnrich();
        this.detailLimit = properties.getDetailLimit();
        this.timeoutMs = properties.getTimeoutMs();
        this.pageDelayMs = properties.getPageDelayMs();
        this.detailDelayMs = properties.getDetailDelayMs();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getUid() { return uid == null ? "" : uid.trim(); }
    public void setUid(String uid) { this.uid = uid; }
    public String getSessdata() { return sessdata == null ? "" : sessdata.trim(); }
    public void setSessdata(String sessdata) { this.sessdata = sessdata; }
    public long getIntervalMs() { return intervalMs; }
    public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public int getFollowStatus() { return followStatus; }
    public void setFollowStatus(int followStatus) { this.followStatus = followStatus; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public boolean isDetailEnrich() { return detailEnrich; }
    public void setDetailEnrich(boolean detailEnrich) { this.detailEnrich = detailEnrich; }
    public int getDetailLimit() { return detailLimit; }
    public void setDetailLimit(int detailLimit) { this.detailLimit = detailLimit; }
    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    public long getPageDelayMs() { return pageDelayMs; }
    public void setPageDelayMs(long pageDelayMs) { this.pageDelayMs = pageDelayMs; }
    public long getDetailDelayMs() { return detailDelayMs; }
    public void setDetailDelayMs(long detailDelayMs) { this.detailDelayMs = detailDelayMs; }
}
