# jsnws-ui-v3

`jsnws-ui`（旧项目，位于 `../jsnws-ui`）的 Vue 3 重构版本。公开站点与后台管理页面已从旧项目迁入，旧项目保持只读，可随时对照或回退。

## 与旧项目的关系

| 项目 | 目录 | 说明 |
| --- | --- | --- |
| 旧项目 | `../jsnws-ui` | Vue 2.6 + element-ui 2.x + vue-router 3 + vuex 3，**保持原样、不作任何改动**，作为迁移参考与回退依据 |
| 新项目 | `./`（`jsnws-ui-v3`） | Vue 3 + Vue CLI 4.5 + Element Plus + Vue Router 4 + Vuex 4 |

两个目录相互独立，各自安装依赖、各自启动，可同时运行对照（旧项目 80 端口，新项目 8080 端口）。

## 技术栈（实际安装版本）

| 依赖 | 版本 |
| --- | --- |
| vue | 3.5.45 |
| vue-router | 4.6.4 |
| vuex | 4.1.0 |
| element-plus | 2.4.4 |
| @element-plus/icons-vue | 2.3.2 |
| @vue/cli-service | 4.5.19 |
| @vue/compiler-sfc | 3.5.45 |
| vue-loader | 16.8.3 |
| axios | 0.27.2 |
| eslint / eslint-plugin-vue | 7.32.0 / 8.7.1 |

## 启动与构建

```bash
npm install
npm run serve   # 开发环境，http://localhost:8080
npm run build   # 生产构建，产物在 dist/
npm run lint    # 代码检查
```

> `serve` 与 `build` 已通过 `cross-env` 注入 `NODE_OPTIONS=--openssl-legacy-provider`：Vue CLI 4.5 使用 webpack 4，在 Node 17+ 上需要该参数才能正常计算 hash。

## 目录约定

```
src/
├── assets/
│   ├── css/            全局样式（animation、index、tocbot、color、markdown-highlight、font-awesome）
│   └── fonts/          字体文件
├── components/
│   ├── admin/          后台外壳：MyHeader、Sidebar
│   ├── comment/        Comment、CommentBox、Graffiti
│   └── common/         可复用组件（Card、Emoji、Loader、MyAside、Printer 等）
├── layout/
│   ├── HomeLayout.vue  公开站点外壳（导航、抽屉菜单、回到顶部、深浅色切换）
│   └── AdminLayout.vue 后台外壳
├── router/index.js     路由表（公开站点 + /admin 子路由 + /verify）
├── store/index.js      Vuex 4 根 store（toolbar、sortInfo、currentUser、currentAdmin、sysConfig、webInfo）
├── utils/
│   ├── request.js      axios 收口：实例、拦截器、错误提示
│   ├── common.js       旧项目的公共方法（原样迁移）
│   └── constant.js     接口地址与常量
├── views/              公开站点页面
│   └── admin/          后台页面
├── App.vue
└── main.js             入口：插件注册、全局属性挂载
```

页面即组件的约定沿用骨架：Options API + 静态导入，路由页面放 `views`，外壳放 `layout`。

## 依赖替换清单

旧项目中只支持 Vue 2 的库，替换方案如下：

| 旧依赖 | 新方案 | 说明 |
| --- | --- | --- |
| vue-seamless-scroll | `vue3-seamless-scroll` | 用法基本一致 |
| vue-video-player | `@videojs-player/vue` + `video.js` | 播放器功能一致，样式需引入 `video.js/dist/video-js.css` |
| vue-baberrage | 自研 `components/common/Barrage.vue` | 5 轨道弹幕，支持 `barrageList`、`loop`，无第三方依赖 |
| mavon-editor | `md-editor-v3` | 同为左右分栏、实时预览；图片上传改为 `onUploadImg(files, callback)`，上传接口沿用旧逻辑 |

`md-editor-v3` 在 webpack 4 下需要三处兼容处理，均已写在 `vue.config.js` 并有注释：主题 CSS 的相对 `@import` 重定向、`linkify-it` 强制 CJS 产物、`@codemirror/*` 与 `@lezer/*` 统一指向 CJS 产物（避免 `instanceof` 校验失败导致编辑器无法初始化）。

## 迁移说明

- **接口行为保持不变**：`utils/request.js` 与 `utils/constant.js` 原样沿用，请求地址、参数顺序、管理端 token 参数均与旧项目一致；后端未启动时只提示错误，不填充假数据。
- **图标**：Element Plus 不再提供 `el-icon-*` 字体图标，统一改用 `@element-plus/icons-vue` 组件（已在 `main.js` 全局注册）；font-awesome 图标保留。
- **全局脚本**：jQuery、anime、hljs、WOW、ClipboardJS、qiniu 仍由 `public/index.html` 的 CDN 提供，`$`、`WOW`、`qiniu` 等已加入 ESLint globals。

## 已知说明

- **Element Plus 按需引入**：`main.js` 只注册项目实际用到的 32 个组件与 21 个图标，样式按组件逐个 `import 'element-plus/es/components/<name>/style/css.mjs'`；国际化用 `provideGlobalConfig({ locale: zhCn }, app, true)` 提供，`$message`/`$notify`/`$confirm` 等全局方法手动挂载。**新增组件或图标时必须同步补充注册与样式导入**，否则该组件不渲染、样式缺失。
- **`transpileDependencies`**：`element-plus`、`vue-router`、`vue3-seamless-scroll`、`@videojs-player/vue`、`md-editor-v3` 等发布包使用了可选链等 webpack 4 无法直接解析的语法，需要 Babel 转译。
- **history 模式**：部署时需要服务端把未知路径回退到 `index.html`。
- **路由与状态**：路由 4 的 `push` 返回 NavigationFailure 而非抛错，旧项目里对 `VueRouter.prototype.push` 的补丁不再需要。
- **全局属性**：`app.config.globalProperties.$http` 对应旧项目的 `Vue.prototype.$http`。
