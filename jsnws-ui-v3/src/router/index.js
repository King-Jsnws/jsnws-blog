import { createRouter, createWebHistory } from 'vue-router'
import { canEnterAdmin, clearAdminEntry } from '../utils/adminEntry'
import { pageOpenState, isHistoryPop } from '../utils/pageOpen'
import store from '../store'
import constant from '../utils/constant'

// 公开站点外壳
const HomeLayout = () => import('../layout/HomeLayout.vue')
// 后台外壳
const AdminLayout = () => import('../layout/AdminLayout.vue')

const routes = [
  {
    path: '/',
    component: HomeLayout,
    children: [
      {
        path: '/',
        name: 'index',
        component: () => import('../views/IndexView.vue')
      },
      {
        path: '/categories',
        name: 'categories',
        component: () => import('../views/CategoriesView.vue')
      },
      {
        path: '/tags',
        name: 'tags',
        component: () => import('../views/TagsView.vue')
      },
      {
        path: '/archive',
        name: 'archive',
        component: () => import('../views/ArchiveView.vue')
      },
      {
        path: '/graph',
        name: 'graph',
        component: () => import('../views/GraphView.vue')
      },
      {
        path: '/article/:id',
        name: 'article',
        component: () => import('../views/ArticleView.vue')
      },
      {
        path: '/jotting',
        name: 'jotting',
        component: () => import('../views/JottingView.vue')
      },
      {
        path: '/favorite',
        name: 'favorite',
        component: () => import('../views/FavoriteView.vue')
      },
      {
        path: '/anime',
        name: 'anime',
        component: () => import('../views/AnimeView.vue')
      },
      {
        path: '/sponsor',
        name: 'sponsor',
        component: () => import('../views/SponsorView.vue')
      },
      {
        path: '/travel',
        name: 'travel',
        component: () => import('../views/TravelView.vue')
      },
      {
        path: '/message',
        name: 'message',
        component: () => import('../views/MessageView.vue')
      },
      {
        path: '/about',
        name: 'about',
        component: () => import('../views/AboutView.vue')
      },
      {
        path: '/analytics',
        name: 'analytics',
        component: () => import('../views/AnalyticsView.vue')
      },
      {
        path: '/user',
        name: 'user',
        component: () => import('../views/UserView.vue')
      },
      {
        path: '/changelog',
        name: 'changelog',
        component: () => import('../views/ChangelogView.vue')
      },
      {
        path: '/blog-log',
        name: 'blog-log',
        component: () => import('../views/BlogLogView.vue')
      }
    ]
  },
  {
    path: '/admin',
    redirect: '/welcome',
    meta: { requiresAuth: true, adminArea: true },
    component: AdminLayout,
    children: [
      {
        path: '/welcome',
        name: 'welcome',
        component: () => import('../views/admin/WelcomeView.vue')
      },
      {
        path: '/main',
        name: 'main',
        component: () => import('../views/admin/MainView.vue')
      },
      {
        path: '/webEdit',
        name: 'webEdit',
        component: () => import('../views/admin/WebEditView.vue')
      },
      {
        path: '/userList',
        name: 'userList',
        component: () => import('../views/admin/UserListView.vue')
      },
      {
        path: '/postList',
        name: 'postList',
        component: () => import('../views/admin/PostListView.vue')
      },
      {
        path: '/postEdit',
        name: 'postEdit',
        component: () => import('../views/admin/PostEditView.vue')
      },
      {
        path: '/sortList',
        name: 'sortList',
        component: () => import('../views/admin/SortListView.vue')
      },
      {
        path: '/navManage',
        name: 'navManage',
        component: () => import('../views/admin/NavManageView.vue')
      },
      {
        path: '/configList',
        name: 'configList',
        component: () => import('../views/admin/ConfigListView.vue')
      },
      {
        path: '/emailConfig',
        name: 'emailConfig',
        component: () => import('../views/admin/EmailConfigView.vue')
      },
      {
        path: '/commentList',
        name: 'commentList',
        component: () => import('../views/admin/CommentListView.vue')
      },
      {
        path: '/treeHoleList',
        name: 'treeHoleList',
        component: () => import('../views/admin/TreeHoleListView.vue')
      },
      {
        path: '/barrageList',
        name: 'barrageList',
        component: () => import('../views/admin/BarrageListView.vue')
      },
      {
        path: '/resourceList',
        name: 'resourceList',
        component: () => import('../views/admin/ResourceListView.vue')
      },
      {
        path: '/animeList',
        name: 'animeList',
        component: () => import('../views/admin/AnimeListView.vue')
      },
      {
        path: '/musicList',
        name: 'musicList',
        component: () => import('../views/admin/MusicListView.vue')
      },
      {
        path: '/friendList',
        name: 'friendList',
        component: () => import('../views/admin/FriendListView.vue')
      },
      {
        path: '/photoList',
        name: 'photoList',
        component: () => import('../views/admin/PhotoListView.vue')
      },
      {
        path: '/sponsorList',
        name: 'sponsorList',
        component: () => import('../views/admin/SponsorListView.vue')
      },
      {
        path: '/changelogList',
        name: 'changelogList',
        component: () => import('../views/admin/ChangelogListView.vue')
      }
    ]
  },
  {
    path: '/verify',
    name: 'verify',
    component: () => import('../views/admin/VerifyView.vue')
  },
  {
    // 站点维护页：维护状态下前台访客被导到这里，站长仍可点「登录入口」进后台恢复
    path: '/maintenance',
    name: 'maintenance',
    component: () => import('../views/MaintenanceView.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
  // 与旧项目一致：每次切换回到顶部
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { left: 0, top: 0 }
  }
})

// 后台各页面允许的角色等级（1 主理人 2 管理员 3 作者 4 普通用户 5 AI 站长）
const ADMIN_ROLE_PATHS = {
  '/welcome': [1, 2, 3, 5],
  '/main': [1],
  '/postList': [1, 3, 5],
  '/postEdit': [1, 3, 5],
  '/sortList': [1, 5],
  '/resourceList': [1, 2, 5],
  '/commentList': [1, 2, 5],
  '/treeHoleList': [1, 2, 5],
  '/barrageList': [1, 2, 5],
  '/friendList': [1, 2],
  '/sponsorList': [1, 2],
  '/changelogList': [1],
  '/animeList': [1, 2, 5],
  '/musicList': [1, 2, 5],
  '/photoList': [1, 2, 5],
  '/userList': [1],
  '/webEdit': [1, 5],
  '/emailConfig': [1],
  '/configList': [1],
  '/navManage': [1]
}

// 当前后台登录者的角色等级，口径与侧边栏一致
function adminRoleLevel() {
  let admin = {}
  try {
    admin = JSON.parse(localStorage.getItem('currentAdmin') || '{}')
  } catch (e) {
    admin = {}
  }
  if (admin.isBoss) {
    return 1
  }
  // 与后端 resolveRole 兜底一致：站长固定 id=1 且 user_type=1 时始终按主理人处理，
  // 避免线上 boss-username 与用户名不一致、isBoss=false 时站长进不了 /webEdit 关维护
  if (Number(admin.id) === Number(constant.userId) && Number(admin.userType) === 1) {
    return 1
  }
  const type = Number(admin.userType)
  if (type === 5) {
    // AI 站长：独立等级，不走 1-4 的兼容归一化
    return 5
  }
  if (type === 1) {
    return 2 // 兼容未迁移的旧管理员数据
  }
  return type >= 2 && type <= 4 ? type : 4
}

// 旧版 /sort 已下架：sortId 能映射到分类名就进归档筛选态，映射不到就回归档总览
function legacySortCategory(raw) {
  const sortId = parseInt(Array.isArray(raw) ? raw[0] : raw, 10)
  if (!Number.isFinite(sortId)) {
    return Promise.resolve('')
  }
  const pick = (list) => {
    const hit = (list || []).find(item => Number(item.id) === sortId)
    return hit && hit.sortName ? hit.sortName : ''
  }
  const cached = pick(store.state.sortInfo)
  if (cached) {
    return Promise.resolve(cached)
  }
  // 本地缓存里没有（首次访问/过期）时现取一次分类数据
  return fetch(constant.baseURL + '/webInfo/getSortInfo')
    .then(res => res.json())
    .then(json => pick(json && json.data))
    .catch(() => '')
}

// 后台鉴权逻辑与旧项目保持一致（维护模式规则见下方守卫）
// ===== 维护模式 =====
// 站点状态来自 web_info.status；5 秒缓存，避免每次路由切换都请求接口
let maintenanceCache = { value: null, at: 0 }

function readJson(key) {
  try {
    return JSON.parse(localStorage.getItem(key) || '{}')
  } catch (e) {
    return {}
  }
}

// 站长身份：后端计算的 isBoss，兜底「固定 id=1 且 user_type=1」
function isBossUser() {
  const user = readJson('currentUser')
  if (!user) {
    return false
  }
  return user.isBoss === true ||
    (Number(user.id) === Number(constant.userId) && Number(user.userType) === 1)
}

function isMaintenanceOn() {
  const now = Date.now()
  if (maintenanceCache.value !== null && now - maintenanceCache.at < 5000) {
    return Promise.resolve(maintenanceCache.value)
  }
  return fetch(constant.baseURL + '/webInfo/getWebInfo')
    .then(res => res.json())
    .then(json => {
      const on = Boolean(json && json.data) && json.data.status === false
      maintenanceCache = { value: on, at: Date.now() }
      return on
    })
    .catch(() => {
      // 接口异常时回退本地缓存，避免把访客误挡在维护页
      return Boolean(store.state.webInfo) && store.state.webInfo.status === false
    })
}

router.beforeEach((to, from, next) => {
  // 统计页首屏背景图较大：进入路由时先预加载，不等异步 chunk 里的 CSS 解析后才开始下载
  if (to.path === '/analytics' && !document.querySelector('link[data-preload="analytics-hero"]')) {
    const heroPreload = document.createElement('link')
    heroPreload.rel = 'preload'
    heroPreload.as = 'image'
    heroPreload.href = '/images/analytics/analytics-hero.7c3f21.webp'
    heroPreload.setAttribute('data-preload', 'analytics-hero')
    document.head.appendChild(heroPreload)
  }

  // 老收藏链接不 404：/sort?sortId=1 → /archive?category=分类名
  if (to.path.replace(/\/+$/, '') === '/sort') {
    legacySortCategory(to.query.sortId).then((category) => {
      next({path: '/archive', query: category ? {category} : {}})
    })
    return
  }

  // 页面打开动画：同页锚点/查询变化、浏览器回退与前进都不重播，避免闪一下
  const samePage = Boolean(from.name) && to.path === from.path
  pageOpenState.instant = isHistoryPop() || samePage

  const isAdminArea = to.matched.some(record => record.meta.adminArea)

  // 独立后台登录页：有导航标记直接放行；维护状态下始终可进入，避免关闭站点后无法登录后台
  if (to.path === '/verify') {
    if (canEnterAdmin()) {
      next()
      return
    }
    isMaintenanceOn().then((on) => {
      if (on) {
        next()
      } else {
        next({ path: '/' })
      }
    })
    return
  }

  if (isAdminArea) {
    // 后台只认可「从导航点击进入」后写入的标记
    if (!canEnterAdmin()) {
      next({ path: '/' })
      return
    }
    if (!Boolean(localStorage.getItem('adminToken'))) {
      next({
        path: '/verify',
        query: { redirect: to.fullPath }
      })
      return
    }
    const allowedRoles = ADMIN_ROLE_PATHS[to.path]
    if (allowedRoles && !allowedRoles.includes(adminRoleLevel())) {
      const level = adminRoleLevel()
      // 普通用户不应停留在后台，作者回文章列表，其余回仪表盘
      next({ path: level === 4 ? '/' : (level === 3 ? '/postList' : '/welcome') })
      return
    }
    next()
    return
  }

  // 维护页与个人中心（登录入口）始终放行
  if (to.path === '/maintenance' || to.path === '/user') {
    next()
    return
  }

  // 前台访客页面：维护状态下只允许站长继续访问
  isMaintenanceOn().then((on) => {
    if (on && !isBossUser()) {
      next({ path: '/maintenance' })
    } else {
      next()
    }
  })
})

// 一旦离开后台/后台登录页进入前台，立即作废本次进入标记
router.afterEach((to) => {
  const isAdminArea = to.matched.some(record => record.meta.adminArea)
  if (!isAdminArea && to.path !== '/verify') {
    clearAdminEntry()
  }
})

export default router
