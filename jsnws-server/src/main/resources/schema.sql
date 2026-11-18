-- jsnws 博客后台建库脚本（MySQL 5.7+/8.0）
-- 执行方式：mysql -u<用户名> -p < schema.sql（先自行执行 CREATE DATABASE）
-- 或登录 MySQL 后执行本文件全部内容

CREATE DATABASE IF NOT EXISTS jsnws DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE jsnws;

-- 站点信息（单行记录，id=1）
DROP TABLE IF EXISTS web_info;
CREATE TABLE web_info (
    id BIGINT NOT NULL PRIMARY KEY,
    web_name VARCHAR(50) DEFAULT NULL COMMENT '站点名',
    web_title VARCHAR(50) DEFAULT NULL COMMENT '标题（字符串，前端逐字渲染）',
    tab_title VARCHAR(50) DEFAULT NULL COMMENT '浏览器标签页标题，空则回退站点名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '站点描述（侧栏个人信息卡片）',
    notices TEXT COMMENT '公告，JSON 数组字符串',
    random_cover TEXT COMMENT '随机封面，JSON 数组字符串',
    random_avatar TEXT COMMENT '随机头像，JSON 数组字符串',
    random_name TEXT COMMENT '随机名，JSON 数组字符串',
    footer VARCHAR(255) DEFAULT NULL COMMENT '页脚',
    background_image VARCHAR(500) DEFAULT NULL COMMENT '首页背景图',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '站点头像',
    logo VARCHAR(500) DEFAULT NULL COMMENT '站点 LOGO（顶栏图片，空则显示文字站名）',
    default_store_type VARCHAR(20) DEFAULT 'local' COMMENT '默认上传存储类型 local/qiniu',
    wechat VARCHAR(500) DEFAULT NULL COMMENT '微信联系方式（社交入口二维码）',
    wechat_reward VARCHAR(500) DEFAULT NULL COMMENT '微信打赏收款码',
    alipay VARCHAR(500) DEFAULT NULL COMMENT '支付宝打赏收款码',
    qq VARCHAR(500) DEFAULT NULL,
    github VARCHAR(500) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    bilibili VARCHAR(500) DEFAULT NULL,
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '站点开放状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点信息';

-- 追番（前台追番页 / 后台追番管理）
DROP TABLE IF EXISTS anime;
CREATE TABLE anime (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '番剧名称',
    cover VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    score DECIMAL(3,1) DEFAULT NULL COMMENT '评分，NULL 表示暂无评分',
    type VARCHAR(50) DEFAULT NULL COMMENT '题材，如 萌系 / 搞笑 / 日常',
    season_type TINYINT DEFAULT NULL COMMENT 'B站分类：1番剧 2电影 3纪录片 4国创 5电视剧 7综艺',
    source VARCHAR(50) DEFAULT NULL COMMENT '来源平台',
    status VARCHAR(50) DEFAULT NULL COMMENT '更新状态，如 更新至12话 / 全24话',
    description VARCHAR(500) DEFAULT NULL COMMENT '简介',
    url VARCHAR(500) DEFAULT NULL COMMENT '详情链接',
    release_date DATE DEFAULT NULL COMMENT '上映日期',
    season_id BIGINT DEFAULT NULL COMMENT 'B站 season_id',
    media_id BIGINT DEFAULT NULL COMMENT 'B站 media_id',
    origin VARCHAR(10) NOT NULL DEFAULT 'manual' COMMENT '数据来源：bili=同步 manual=手工',
    visible TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否在前台展示',
    last_sync_time DATETIME DEFAULT NULL COMMENT '最近一次同步时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_anime_season (season_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追番';

-- 音乐（前台侧栏播放器 / 后台音乐管理）
DROP TABLE IF EXISTS music;
CREATE TABLE music (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '歌曲名称',
    artist VARCHAR(100) DEFAULT NULL COMMENT '歌手',
    cover VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    url VARCHAR(500) NOT NULL COMMENT '音频地址',
    lyrics TEXT COMMENT '歌词，每行一句，可带 [mm:ss] 时间标签',
    sort INT DEFAULT 0 COMMENT '排序，越小越靠前',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐';

-- 系统配置（key-value）
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(100) NOT NULL COMMENT '配置名',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    config_type VARCHAR(10) NOT NULL DEFAULT '2' COMMENT '1=私有 2=公开',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- 用户表（站长 id=1 为主理人）
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '账号（登录用）',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称（展示用）',
    account VARCHAR(100) DEFAULT NULL COMMENT '登录邮箱',
    password VARCHAR(200) NOT NULL COMMENT '密码（前端 AES 加密后的密文，ECB 确定性加密，直接密文比对）',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像',
    gender INT DEFAULT NULL COMMENT '性别 0 保密 1 男 2 女',
    introduction VARCHAR(500) DEFAULT NULL COMMENT '个性签名',
    email VARCHAR(100) DEFAULT NULL,
    phone_number VARCHAR(20) DEFAULT NULL,
    subscribe TEXT COMMENT '订阅的分类/标签 id，JSON 数组字符串',
    user_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用状态',
    user_type INT NOT NULL DEFAULT 4 COMMENT '1 本站主理人 2 管理员 3 作者 4 普通用户',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 分类
DROP TABLE IF EXISTS sort;
CREATE TABLE sort (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sort_name VARCHAR(50) NOT NULL COMMENT '分类名',
    sort_description VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    sort_type INT NOT NULL DEFAULT 1 COMMENT '0 导航栏分类 1 普通分类',
    priority INT NOT NULL DEFAULT 1 COMMENT '优先级（小在前）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类';

-- 标签
DROP TABLE IF EXISTS label;
CREATE TABLE label (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sort_id BIGINT NOT NULL COMMENT '所属分类',
    label_name VARCHAR(50) NOT NULL,
    label_description VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签';

-- 文章
DROP TABLE IF EXISTS article;
CREATE TABLE article (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL DEFAULT 1 COMMENT '作者（站长）id',
    article_title VARCHAR(100) NOT NULL,
    article_content LONGTEXT COMMENT 'markdown 内容',
    article_cover VARCHAR(500) DEFAULT NULL,
    has_video TINYINT(1) NOT NULL DEFAULT 0,
    video_url VARCHAR(500) DEFAULT NULL,
    sort_id BIGINT DEFAULT NULL,
    label_id BIGINT DEFAULT NULL,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    view_status TINYINT(1) NOT NULL DEFAULT 1,
    comment_status TINYINT(1) NOT NULL DEFAULT 1,
    recommend_status TINYINT(1) NOT NULL DEFAULT 0,
    is_top TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    password VARCHAR(200) DEFAULT NULL COMMENT '访问密码（AES 密文）',
    tips VARCHAR(100) DEFAULT NULL COMMENT '密码错误提示语',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_sort (sort_id),
    KEY idx_label (label_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章';

-- 评论（article/love/message 三种类型；floor_comment_id 为 null 表示一楼主评论）
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    comment_content VARCHAR(1000) NOT NULL,
    source VARCHAR(50) NOT NULL COMMENT '评论目标（文章id 或常量 0/1）',
    comment_type VARCHAR(20) NOT NULL COMMENT 'article/love/message',
    floor_comment_id BIGINT DEFAULT NULL COMMENT '楼层（主评论）id',
    parent_comment_id BIGINT DEFAULT NULL COMMENT '被回复的评论 id',
    parent_user_id BIGINT DEFAULT NULL COMMENT '被回复人 id',
    ip VARCHAR(64) DEFAULT NULL COMMENT '留言客户端 IP（安全审核用，不对外返回）',
    location VARCHAR(100) DEFAULT NULL COMMENT 'IP 归属地（省份·城市·运营商，ip2region 解析，解析失败为「未知」）',
    browser VARCHAR(50) DEFAULT NULL COMMENT '浏览器名称+版本（UA 解析，如 Chrome151.0）',
    os VARCHAR(50) DEFAULT NULL COMMENT '操作系统名称+版本（UA 解析，如 macOS 10.15.7）',
    is_top TINYINT(1) NOT NULL DEFAULT 0 COMMENT '置顶标记：1置顶 0普通（仅站长可操作）',
    top_time DATETIME DEFAULT NULL COMMENT '置顶时间，取消置顶时置空',
    is_anonymous TINYINT(1) NOT NULL DEFAULT 0 COMMENT '匿名标记：1匿名（昵称固定「匿名网友」，头像取本地随机素材）0普通',
    anonymous_avatar VARCHAR(255) DEFAULT NULL COMMENT '匿名留言随机头像地址（本地素材，仅匿名时写入）',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_source (source, comment_type),
    KEY idx_floor (floor_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论';

-- 树洞（留言板）
DROP TABLE IF EXISTS tree_hole;
CREATE TABLE tree_hole (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    message VARCHAR(1000) NOT NULL,
    ip VARCHAR(64) DEFAULT NULL COMMENT '发言客户端 IP（安全审核用，后台可查）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='树洞';

-- 随笔/文章页留言（前端 weiYan 模块）
DROP TABLE IF EXISTS wei_yan;
CREATE TABLE wei_yan (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    content VARCHAR(1000) NOT NULL,
    source VARCHAR(50) DEFAULT NULL COMMENT '来源（文章页时为文章 id）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随笔';

-- 爱情（family）
DROP TABLE IF EXISTS family;
CREATE TABLE family (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bg_cover VARCHAR(500) DEFAULT NULL COMMENT '背景',
    man_cover VARCHAR(500) DEFAULT NULL COMMENT '男方头像',
    woman_cover VARCHAR(500) DEFAULT NULL COMMENT '女方头像',
    man_name VARCHAR(50) DEFAULT NULL,
    woman_name VARCHAR(50) DEFAULT NULL,
    countdown_title VARCHAR(50) DEFAULT NULL,
    countdown_time VARCHAR(30) DEFAULT NULL COMMENT '倒计时目标时间 yyyy-MM-dd HH:mm:ss',
    timing VARCHAR(50) DEFAULT NULL COMMENT '恋爱计时开始时间',
    family_info VARCHAR(1000) DEFAULT NULL COMMENT '个性签名',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '审核/启用状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爱情';

-- 资源文件记录（/resource/upload 的登记）
DROP TABLE IF EXISTS resource;
CREATE TABLE resource (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    path VARCHAR(500) NOT NULL COMMENT '访问 URL',
    name VARCHAR(255) DEFAULT NULL COMMENT '存储名（key）',
    original_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    size BIGINT DEFAULT NULL COMMENT '字节大小',
    mime_type VARCHAR(100) DEFAULT NULL,
    type VARCHAR(50) DEFAULT NULL COMMENT '资源类型（articlePicture/graffiti/lovePhoto/funny/favorites/friendUrl...）',
    store_type VARCHAR(20) DEFAULT 'local',
    status TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源文件';

-- 资源路径（照片墙/音乐等二次管理，type=lovePhoto/funny/favorites/friendUrl）
DROP TABLE IF EXISTS resource_path;
CREATE TABLE resource_path (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) DEFAULT NULL,
    classify VARCHAR(50) DEFAULT NULL COMMENT '分组（照片墙相册名/收藏夹分类）',
    introduction VARCHAR(500) DEFAULT NULL,
    cover VARCHAR(500) DEFAULT NULL,
    url VARCHAR(500) DEFAULT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'lovePhoto/funny/favorites/friendUrl',
    remark VARCHAR(255) DEFAULT NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源路径';

-- 友链
DROP TABLE IF EXISTS friend;
CREATE TABLE friend (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) DEFAULT NULL,
    introduction VARCHAR(255) DEFAULT NULL,
    cover VARCHAR(500) DEFAULT NULL COMMENT '友链图标',
    avatar VARCHAR(500) DEFAULT NULL,
    url VARCHAR(500) NOT NULL,
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '审核状态（0待审核/1已通过/2已拒绝）',
    tier TINYINT NOT NULL DEFAULT 0 COMMENT '友链档次（0普通/1推荐/2本站主理人/3优秀友链）',
    tags VARCHAR(200) DEFAULT NULL COMMENT '标签，英文逗号分隔',
    recommended TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐（0否/1是）',
    recommend_label VARCHAR(40) DEFAULT NULL COMMENT '推荐文案',
    latency_ms INT DEFAULT NULL COMMENT '探测首响应毫秒',
    latency_checked_at DATETIME DEFAULT NULL COMMENT '最近探测时间',
    health_status TINYINT NOT NULL DEFAULT 0 COMMENT '可用状态（0正常/1短暂失联/2违规）',
    offline_since DATETIME DEFAULT NULL COMMENT '首次探测失败时间',
    rectify_deadline DATETIME DEFAULT NULL COMMENT '违规整改期限',
    rss_url VARCHAR(500) DEFAULT NULL COMMENT '友链 RSS 地址',
    rss_updated_at DATETIME DEFAULT NULL COMMENT 'RSS 最近文章时间',
    rss_title VARCHAR(200) DEFAULT NULL COMMENT 'RSS 最近文章标题',
    click_count INT NOT NULL DEFAULT 0 COMMENT '本站点击累计',
    avatar_hue INT DEFAULT NULL COMMENT '头像主色相（0-360）',
    avatar_saturation INT DEFAULT NULL COMMENT '头像主饱和度（0-100）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='友链';

-- 赞助记录（前台赞助页展示，后台手动登记）
DROP TABLE IF EXISTS sponsor_record;
CREATE TABLE sponsor_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '赞助人昵称',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '赞助人头像',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '赞助金额',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '赞助时间',
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赞助记录';

-- 更新日志（前台 /changelog 公告页，后台手动录入）
DROP TABLE IF EXISTS changelog;
CREATE TABLE changelog (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(30) DEFAULT NULL COMMENT '版本号，如 V4.6.0',
    log_date DATE NOT NULL COMMENT '更新日期',
    tone VARCHAR(20) NOT NULL COMMENT '分类配色：major/feature/minor/fix/content',
    kind VARCHAR(60) DEFAULT NULL COMMENT '分类文案，如 功能迭代',
    title VARCHAR(200) NOT NULL COMMENT '更新标题',
    summary VARCHAR(500) DEFAULT NULL COMMENT '更新摘要',
    topics TEXT COMMENT '涉及内容，JSON 数组字符串',
    details TEXT COMMENT '更新详情，JSON 数组字符串（每项一条）',
    related_ids VARCHAR(500) DEFAULT NULL COMMENT '相关更新 id，英文逗号分隔',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_changelog_date (log_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='更新日志';

-- 访问统计（前台路由切换时埋点）
DROP TABLE IF EXISTS history;
CREATE TABLE history (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ip VARCHAR(50) NOT NULL,
    province VARCHAR(50) DEFAULT '未知',
    user_id BIGINT DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    path VARCHAR(255) DEFAULT NULL,
    user_agent VARCHAR(500) DEFAULT NULL,
    referrer VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_day (create_time),
    KEY idx_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问记录';

-- ============================================================
-- 初始数据（仅管理员 + 站点基础配置）
-- ============================================================

-- 管理员账号：账号 jsnws / 密码 123456（密文由前端 AES 加密生成，登录时直接密文比对）
INSERT INTO web_info (id, web_name, web_title, notices, random_cover, random_avatar, random_name, footer,
                      background_image, avatar, default_store_type, wechat, alipay, qq, github, email, bilibili, status)
VALUES (1, 'jsnws 的博客', 'jsnws的博客',
        '[]', '[]', '[]', '[]',
        'jar and cat',
        'http://localhost:8080/images/home-default-bg.jpg',
        'http://localhost:8080/images/default-avatar.jpg',
        'local', '', '', '', '', '', '', 1);

INSERT INTO sys_config (id, config_name, config_key, config_value, config_type) VALUES
(1, '静态资源前缀', 'webStaticResourcePrefix', '', '2'),
(2, '七牛云上传地址', 'qiniuUrl', '', '1'),
(3, '七牛云外链地址', 'qiniu.downloadUrl', '', '1'),
(4, '备案信息', 'beian', '', '2');

-- 密码密文说明：S1TbWZ3KoFpR2qkkySTSHg== 是 "123456" 用前端 encrypt()（AES/ECB/PKCS7，key=sarasarasarasara，Base64 后 /→_ +→-）加密的结果
-- account/email 留空：站长需在后台「账号设置」里自行绑定邮箱后才能用邮箱登录
INSERT INTO sys_user (id, username, nickname, account, password, avatar, gender, introduction, email, phone_number, subscribe, user_status, user_type)
VALUES (1, 'jsnws', 'jsnws', NULL, 'S1TbWZ3KoFpR2qkkySTSHg==', 'http://localhost:8080/images/default-avatar.jpg', 0, '站点管理员', NULL, '', '', 1, 1);
