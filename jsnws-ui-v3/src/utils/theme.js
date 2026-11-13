// 全站主题（白天 / 黑夜）统一入口
// 约定：html.dark 同时驱动 Element Plus 官方暗色变量与项目自定义变量（color.css / admin.css）
const STORAGE_KEY = 'theme'

export const THEME_LIGHT = 'light'
export const THEME_DARK = 'dark'

// 与 public/index.html 里的首屏脚本保持一致：22:00 - 07:00 默认黑夜
function isNightTime() {
  const hour = new Date().getHours()
  return hour >= 22 || hour < 7
}

export function getStoredTheme() {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    return value === THEME_DARK || value === THEME_LIGHT ? value : ''
  } catch (e) {
    return ''
  }
}

// 用户手动切换过的主题优先，未切换过则沿用旧行为按时间自动判断
export function resolveTheme() {
  return getStoredTheme() || (isNightTime() ? THEME_DARK : THEME_LIGHT)
}

// 只应用，不写本地存储（用于首屏恢复用户已保存的主题）
export function applyTheme(theme) {
  const isDark = theme === THEME_DARK
  const root = document.documentElement
  root.classList.toggle('dark', isDark)
  root.setAttribute('data-theme', isDark ? THEME_DARK : THEME_LIGHT)
  return isDark ? THEME_DARK : THEME_LIGHT
}

// 用户主动切换：应用并持久化，刷新 / 切换路由后保持
export function setTheme(theme) {
  const next = applyTheme(theme)
  try {
    localStorage.setItem(STORAGE_KEY, next)
  } catch (e) {
    // localStorage 不可用时仅当次生效
  }
  return next
}

export function toggleTheme(theme) {
  return theme === THEME_DARK ? THEME_LIGHT : THEME_DARK
}
