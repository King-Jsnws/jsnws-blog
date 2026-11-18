package com.jsnws.server.common;

import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.mapper.WebInfoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 站点维护开关（web_info.status）：
 * status=false 表示维护中，只拦截前台访客；登录入口、后台接口始终放行。
 * 读库异常时按“未维护”处理，避免数据库抖动把站点锁死。
 * 兜底：环境变量 JSNWS_MAINTENANCE_FORCE_OPEN=true 可忽略数据库状态，强制对外服务。
 */
@Service
public class MaintenanceService {

    /** 缓存时长：改库后最多 2 秒生效，避免每个请求都查库 */
    private static final long CACHE_MS = 2000L;

    private final WebInfoMapper webInfoMapper;

    @Value("${jsnws.maintenance-force-open:false}")
    private boolean forceOpen;

    private volatile Boolean cached;
    private volatile long cachedAt;

    public MaintenanceService(WebInfoMapper webInfoMapper) {
        this.webInfoMapper = webInfoMapper;
    }

    /** 服务端兜底开关：为 true 时忽略 web_info.status，站点始终对外服务 */
    public boolean isForceOpen() {
        return forceOpen;
    }

    /** 当前是否处于维护中 */
    public boolean isMaintenanceOn() {
        if (forceOpen) {
            return false;
        }
        long now = System.currentTimeMillis();
        Boolean snapshot = cached;
        if (snapshot != null && now - cachedAt < CACHE_MS) {
            return snapshot;
        }
        boolean on;
        try {
            WebInfo info = webInfoMapper.selectById(1L);
            on = info != null && Boolean.FALSE.equals(info.getStatus());
        } catch (Exception e) {
            on = false;
        }
        cached = on;
        cachedAt = now;
        return on;
    }
}
