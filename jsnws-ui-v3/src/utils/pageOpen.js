import { reactive } from 'vue'

/**
 * 前台页面打开动画的统一开关。
 * instant 为 true 时，布局层会给 #main-container 挂上 page-stage--instant，
 * 本次导航中的页面级入场动画（my-animation-*）不播放，直接呈现稳定态。
 */
export const pageOpenState = reactive({
  instant: false
})

// 历史回退/前进标记：必须赶在 vue-router 自己的 popstate 监听之前置位
let historyPop = false

if (typeof window !== 'undefined') {
  window.addEventListener('popstate', () => {
    historyPop = true
    // 本次导航的守卫是同步执行的，宏任务开始时清掉，避免影响后续导航
    window.setTimeout(() => {
      historyPop = false
    }, 0)
  })
}

export function isHistoryPop() {
  return historyPop
}
