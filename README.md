# jsnws 博客

一个前后端分离的个人博客系统：

- **jsnws-server**：Spring Boot 后端，提供前台/后台全部接口。
- **jsnws-ui-v3**：Vue 3 前端，包含博客前台与内容管理后台。

前端默认 `http://localhost:8080`，后端默认 `http://localhost:8081`。

---

## 技术栈

| 端 | 技术 |
| --- | --- |
| jsnws-server | Java 17、Spring Boot 2.7.18、MyBatis-Plus 3.5.7、MySQL 8、Maven |
| jsnws-ui-v3 | Vue 3.5、Vue CLI 4.5（webpack 4）、Element Plus 2.4.4、Vuex 4、Vue Router 4、axios、md-editor-v3 |

---

## 目录结构

```
jsnws-bk/
├── README.md
├── jsnws-server/                 # 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/jsnws/server/
│       │   ├── BlogApplication.java
│       │   ├── common/           # Result、异常、AES、Token、拦截器
│       │   ├── config/           # CORS、MyBatis-Plus、静态资源映射
│       │   ├── controller/       # 全部接口
│       │   ├── entity/           # 实体
│       │   └── mapper/           # MyBatis-Plus Mapper
│       └── resources/
│           ├── application.yml   # 端口、数据库、上传、密钥等配置
│           └── schema.sql        # 建库建表 + 初始数据
└── jsnws-ui-v3/                  # 前端
    ├── package.json
    ├── vue.config.js             # devServer / 构建兼容配置
    └── src/
        ├── main.js               # Element Plus 按需注册
        ├── router/index.js       # 前台 + 后台路由
        ├── store/                # Vuex（当前用户、站长信息、系统配置）
        ├── utils/                # request、constant、AES 等
        ├── components/           # 公共组件与后台布局/侧栏
        ├── layout/               # 前台布局、后台 AdminLayout
        └── views/                # 前台页面 + views/admin 后台页面
```

---

## 快速开始

### 1. 环境要求

- JDK 17
- Maven 3.8+
- Node.js 16+（npm）
- MySQL 8.0

### 2. 初始化数据库

```bash
mysql -uroot -p < jsnws-server/src/main/resources/schema.sql
```

脚本会创建 `jsnws` 库、全部数据表并写入初始数据。

默认数据库连接（见 `application.yml`）：`localhost:3306/jsnws`，账号 `root`，密码 `root`。

默认管理员：**账号 `jsnws` / 密码 `123456`**（登录后可修改）。

### 3. 启动后端

```bash
cd jsnws-server
mvn spring-boot:run
# 或打包运行
mvn -DskipTests package
java -jar target/jsnws-server-1.0.0.jar
```

启动后监听 `8081`，浏览器访问 `http://localhost:8081/webInfo/getWebInfo` 可验证。

### 4. 启动前端

```bash
cd jsnws-ui-v3
npm install
npm run serve      # 开发模式，http://localhost:8080
npm run build      # 生产构建，产物在 dist/
```

前端接口地址在 `src/utils/constant.js` 的 `baseURL` 中配置，默认 `http://localhost:8081`。

---

## 配置说明

### 后端 `application.yml`

| 配置 | 说明 |
| --- | --- |
| `server.port` | 后端端口，默认 `8081` |
| `spring.datasource.*` | MySQL 连接信息 |
| `spring.servlet.multipart.*` | 上传大小限制（单文件 100MB / 请求 200MB） |
| `jsnws.upload-base-dir` | 上传文件落盘根目录，默认 `./resource` |
| `jsnws.upload-url-prefix` | 上传文件访问前缀 |
| `jsnws.crypto-key` | 密码 AES 密钥，必须与前端 `cryptojs_key` 一致 |
| `jsnws.token-secret` / `token-expire` | 登录 token 签名密钥与有效期（默认 30 天） |
| `jsnws.boss-user-id` | 站长用户 id（默认 1），后台接口仅该用户可访问 |
| `jsnws.mail-enabled` | 是否启用邮件验证码；`false` 时验证码打印到控制台且校验放行 |

### 前端 `src/utils/constant.js`

- `baseURL`：后端接口地址。
- `cryptojs_key`：与后端一致的 AES 密钥。
- `userId`：站长用户 id，与 `jsnws.boss-user-id` 对应。
- `weatherCity`、`hitokoto` 等：前台展示类配置。

---

## 功能总览

### 前台

- 首页：站点标题、打字机诗句（可开关）、文章流、公告、背景图
- 文章：详情、分类/标签筛选、归档、图谱、密码访问、评论
- 分类与标签、搜索
- 留言板（树洞）、随笔、收藏夹
- 相册（照片墙，数据来自 `resource_path` 的 `lovePhoto`）
- 追番、音乐播放器、友链、赞助记录、关于页
- 用户中心：注册/登录、资料与密码修改、订阅

### 后台（`/admin`，仅站长）

侧栏按分组组织：

| 分组 | 页面 |
| --- | --- |
| 概览 | 仪表盘、数据统计 |
| 内容 | 文章管理（md-editor-v3 Markdown 编辑器）、分类管理、资源管理 |
| 互动 | 评论管理、留言管理、友链管理（含审核）、赞助记录 |
| 娱乐 | 追番管理、音乐管理、相片管理 |
| 系统 | 用户管理、网站设置、邮箱配置、配置管理 |

后台要点：

- **网站设置**：站点名称/标题/描述、首页诗句与首屏标题开关、背景图、头像、社交链接，以及底部备案信息（ICP 备案、公安备案）。
- **友链审核**：待审核 / 已通过 / 已拒绝，前台仅展示已通过友链。
- **赞助记录**：独立记录赞助人、头像、金额、备注、赞助时间；用户管理中的「赞赏」可快捷登记一条赞助记录。
- **配置管理**：基于 `sys_config` 键值表的通用配置维护；邮箱 SMTP 参数单独在「邮箱配置」页维护。
- **用户管理**：账号（登录用）+ 昵称（展示用），支持启用/禁用、身份调整。

---

## 接口约定

- 统一返回：`{ "code": 200, "message": "...", "data": ... }`，`code != 200` 时前端按错误处理。
- 鉴权：请求头 `Authorization` 携带登录返回的 `accessToken`。
  - 前台普通接口无需 token；用户相关写操作需用户 token。
  - `/admin/**` 仅站长 token 可访问。
  - 登录失效或权限不足返回 `code=300`，前端清除 token 并跳转登录。
- 密码：前端使用 AES/ECB/PKCS7（key=`sarasarasarasara`）加密，Base64 后把 `/` 替换为 `_`、`+` 替换为 `-`，后端直接密文比对。
- 上传：`POST /resource/upload`，文件落盘到 `jsnws.upload-base-dir`，通过 `/resource/file/**` 访问；资源记录写入 `resource` 表。

---

## 数据表

| 表 | 说明 |
| --- | --- |
| `web_info` | 站点信息（单行 id=1） |
| `sys_user` | 用户/管理员（`username` 登录账号，`nickname` 展示昵称） |
| `sys_config` | 系统配置键值表 |
| `article` / `sort` / `label` | 文章、分类、标签 |
| `comment` | 评论（文章/爱情/留言） |
| `tree_hole` / `wei_yan` | 树洞留言、随笔 |
| `friend` | 友链（0 待审核 / 1 已通过 / 2 已拒绝） |
| `sponsor_record` | 赞助记录（赞助人、头像、金额、备注、时间） |
| `anime` / `music` | 追番、音乐 |
| `family` | 爱情/情侣页 |
| `resource` / `resource_path` | 上传资源、照片墙/收藏等二次资源 |
| `history` | 访问记录（ip、省份、用户） |

---

## 构建与部署

```bash
# 后端
cd jsnws-server
mvn -DskipTests package
java -jar target/jsnws-server-1.0.0.jar

# 前端
cd jsnws-ui-v3
npm run build
```

将 `jsnws-ui-v3/dist` 交给 Nginx 等静态服务器托管，并把接口地址（`constant.js` 的 `baseURL`）改为线上后端地址或同域 `/api`；后端部署时同步修改 `application.yml` 中的数据库、上传目录与对外地址前缀。
