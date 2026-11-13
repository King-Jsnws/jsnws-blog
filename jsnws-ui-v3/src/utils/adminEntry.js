// 后台「仅导航进入」标记：存在 sessionStorage，随标签页存活，刷新不丢失，关标签页即失效
const ADMIN_ENTRY_KEY = 'adminEntryFromNav'

// 前台导航点击「后台」时写入
export function allowAdminEntry() {
  sessionStorage.setItem(ADMIN_ENTRY_KEY, '1')
}

// 退出登录、token 失效、回到前台时清除
export function clearAdminEntry() {
  sessionStorage.removeItem(ADMIN_ENTRY_KEY)
}

// 路由守卫判断
export function canEnterAdmin() {
  return sessionStorage.getItem(ADMIN_ENTRY_KEY) === '1'
}
