-- 追番页类型页签：anime 表新增 B 站分类字段（MySQL 8）
-- 适用场景：数据库已按旧版 schema.sql 初始化，不能重复执行 schema.sql。
-- 只新增可空列，不改动现有数据；存量 B 站条目由下一次同步自动回填 season_type。
-- MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报“字段已存在”。

USE jsnws;

ALTER TABLE anime
    ADD COLUMN season_type TINYINT DEFAULT NULL COMMENT 'B站分类：1番剧 2电影 3纪录片 4国创 5电视剧 7综艺' AFTER type;
