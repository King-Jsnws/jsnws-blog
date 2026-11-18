package com.jsnws.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 追番同步配置：jsnws.bilibili.*
 * 所有项均可通过 application.yml 或同名环境变量覆盖。
 */
@Component
@ConfigurationProperties(prefix = "jsnws.bilibili")
public class BilibiliProperties {

    /** 是否启用追番同步 */
    private boolean enabled = true;
    /** B 站数字 UID，留空则同步任务直接跳过 */
    private String uid = "335349376";
    /** B 站登录 Cookie 中的 SESSDATA，公开追番可留空 */
    private String sessdata = "";
    /** 同步间隔（毫秒），默认 6 小时 */
    private long intervalMs = 21600000L;
    /** 启动后首次同步延迟（毫秒） */
    private long initialDelayMs = 120000L;
    /** 拉取类型：0=追番+追剧（全部） 1=追番（含番剧/国创） 2=追剧（电影/纪录片/电视剧等） */
    private int type = 0;
    /** 追番状态过滤：0=全部 1=想看 2=在看 3=看过 */
    private int followStatus = 0;
    /** 每页数量，B 站上限 30 */
    private int pageSize = 30;
    /** 是否按 season_id 补全评分与题材 */
    private boolean detailEnrich = true;
    /** 单轮最多补全多少部，避免请求过多触发风控 */
    private int detailLimit = 30;
    /** 单次请求超时（毫秒） */
    private long timeoutMs = 10000L;
    /** 列表分页之间的等待（毫秒） */
    private long pageDelayMs = 1000L;
    /** 详情请求之间的等待（毫秒） */
    private long detailDelayMs = 600L;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getSessdata() { return sessdata; }
    public void setSessdata(String sessdata) { this.sessdata = sessdata; }
    public long getIntervalMs() { return intervalMs; }
    public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }
    public long getInitialDelayMs() { return initialDelayMs; }
    public void setInitialDelayMs(long initialDelayMs) { this.initialDelayMs = initialDelayMs; }
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
