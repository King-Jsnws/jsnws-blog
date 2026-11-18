-- 站点 LOGO 独立配置：为已初始化的数据库补充 web_info.logo 字段
-- 适用场景：数据库已按旧版 schema.sql 初始化，不能再次执行 schema.sql（其中含 DROP TABLE）。
-- 新字段与 avatar（站点头像）分开存储，为空时顶栏回退文字站名。
ALTER TABLE web_info
    ADD COLUMN logo VARCHAR(500) DEFAULT NULL COMMENT '站点 LOGO（顶栏图片，空则显示文字站名）' AFTER avatar;
