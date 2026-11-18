-- 站点统计：访问记录补充页面路径、User-Agent、来源主机名
-- 历史数据保持 NULL，依赖这些字段的模块在无数据时不渲染
ALTER TABLE history
    ADD COLUMN path VARCHAR(255) DEFAULT NULL AFTER avatar,
    ADD COLUMN user_agent VARCHAR(500) DEFAULT NULL AFTER path,
    ADD COLUMN referrer VARCHAR(255) DEFAULT NULL AFTER user_agent;
