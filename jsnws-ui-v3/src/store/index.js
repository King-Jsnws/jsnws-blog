import { createStore } from 'vuex'
import { resolveTheme, setTheme } from '../utils/theme'

// 与旧项目保持完全一致的 state / getters / mutations 命名，页面代码无需改写调用方式
export default createStore({
  state: {
    // 全站主题：'light' | 'dark'，由 utils/theme.js 统一应用与持久化
    theme: resolveTheme(),
    toolbar: JSON.parse(localStorage.getItem('toolbar') || '{"visible": false, "enter": true}'),
    sortInfo: JSON.parse(localStorage.getItem('sortInfo') || '[]'),
    currentUser: JSON.parse(localStorage.getItem('currentUser') || '{}'),
    currentAdmin: JSON.parse(localStorage.getItem('currentAdmin') || '{}'),
    sysConfig: JSON.parse(localStorage.getItem('sysConfig') || '{}'),
    // 站长信息（头像卡片上显示站长名称，来自站长账号而非站点信息表）
    bossInfo: JSON.parse(localStorage.getItem('bossInfo') || '{}'),
    webInfo: JSON.parse(localStorage.getItem('webInfo') || '{"webName": "", "webTitle": [], "notices": [], "randomCover": [], "footer": "", "backgroundImage": "", "avatar": ""}')
  },
  getters: {
    articleTotal: state => {
      if (state.sortInfo !== null && state.sortInfo.length !== 0) {
        if (state.sortInfo.length === 1) {
          return state.sortInfo[0].countOfSort
        } else {
          return state.sortInfo.reduce((prev, curr) => {
            if (typeof prev === 'number') {
              return prev + curr.countOfSort
            } else {
              return prev.countOfSort + curr.countOfSort
            }
          })
        }
      } else {
        return 0
      }
    },
    navigationBar: state => {
      if (state.sortInfo !== null && state.sortInfo.length !== 0) {
        return state.sortInfo.filter(f => f.sortType === 0)
      } else {
        return []
      }
    }
  },
  mutations: {
    changeTheme(state, theme) {
      state.theme = setTheme(theme)
    },
    changeToolbarStatus(state, toolbarState) {
      state.toolbar = toolbarState
      localStorage.setItem('toolbar', JSON.stringify(toolbarState))
    },
    loadSortInfo(state, sortInfo) {
      if (sortInfo !== null && sortInfo.length !== 0) {
        state.sortInfo = sortInfo.sort((s1, s2) => s1.priority - s2.priority)
        localStorage.setItem('sortInfo', JSON.stringify(sortInfo.sort((s1, s2) => s1.priority - s2.priority)))
      }
    },
    loadCurrentUser(state, user) {
      state.currentUser = user
      localStorage.setItem('currentUser', JSON.stringify(user))
    },
    loadSysConfig(state, sysConfig) {
      state.sysConfig = sysConfig
      localStorage.setItem('sysConfig', JSON.stringify(sysConfig))
    },
    loadCurrentAdmin(state, user) {
      state.currentAdmin = user
      localStorage.setItem('currentAdmin', JSON.stringify(user))
    },
    loadBossInfo(state, bossInfo) {
      state.bossInfo = bossInfo
      localStorage.setItem('bossInfo', JSON.stringify(bossInfo))
    },
    loadWebInfo(state, webInfo) {
      webInfo.webTitle = webInfo.webTitle.split('')
      webInfo.notices = JSON.parse(webInfo.notices)
      webInfo.randomCover = JSON.parse(webInfo.randomCover)
      state.webInfo = webInfo
      localStorage.setItem('webInfo', JSON.stringify(webInfo))
    }
  },
  actions: {},
  modules: {},
  plugins: []
})
