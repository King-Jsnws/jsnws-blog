import { createRouter, createWebHistory } from 'vue-router'
import { canEnterAdmin, clearAdminEntry } from '../utils/adminEntry'

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
        path: '/sort',
        name: 'sort',
        component: () => import('../views/SortView.vue')
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
        path: '/user',
        name: 'user',
        component: () => import('../views/UserView.vue')
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
      }
    ]
  },
  {
    path: '/verify',
    name: 'verify',
    component: () => import('../views/admin/VerifyView.vue')
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

// 后台鉴权逻辑与旧项目保持一致
router.beforeEach((to, from, next) => {
  const isAdminArea = to.matched.some(record => record.meta.adminArea)

  // 后台与独立后台登录页都只认可「从导航点击进入」后写入的标记
  if (isAdminArea || to.path === '/verify') {
    if (!canEnterAdmin()) {
      next({ path: '/' })
      return
    }
  }

  if (isAdminArea) {
    if (!Boolean(localStorage.getItem('adminToken'))) {
      next({
        path: '/verify',
        query: { redirect: to.fullPath }
      })
    } else {
      next()
    }
  } else {
    next()
  }
})

// 一旦离开后台/后台登录页进入前台，立即作废本次进入标记
router.afterEach((to) => {
  const isAdminArea = to.matched.some(record => record.meta.adminArea)
  if (!isAdminArea && to.path !== '/verify') {
    clearAdminEntry()
  }
})

export default router
