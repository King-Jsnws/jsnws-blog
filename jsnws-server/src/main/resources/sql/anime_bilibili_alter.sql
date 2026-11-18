-- 追番接入 B 站同步：已有数据库升级脚本（MySQL 8）
-- 适用场景：数据库已经按旧版 schema.sql 初始化过，不能再次执行 schema.sql（其中含 DROP TABLE）。
-- 当前 anime 表为 0 条数据，下列 ALTER 只加列/改默认值，不会删除任何数据。
-- 只需执行一次；MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报“字段已存在”。

USE jsnws;

ALTER TABLE anime
    MODIFY COLUMN score DECIMAL(3,1) DEFAULT NULL COMMENT '评分，NULL 表示暂无评分',
    ADD COLUMN season_id BIGINT DEFAULT NULL COMMENT 'B站 season_id' AFTER release_date,
    ADD COLUMN media_id BIGINT DEFAULT NULL COMMENT 'B站 media_id' AFTER season_id,
    ADD COLUMN origin VARCHAR(10) NOT NULL DEFAULT 'manual' COMMENT '数据来源：bili=同步 manual=手工' AFTER media_id,
    ADD COLUMN visible TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否在前台展示' AFTER origin,
    ADD COLUMN last_sync_time DATETIME DEFAULT NULL COMMENT '最近一次同步时间' AFTER visible,
    ADD UNIQUE KEY uk_anime_season (season_id);
