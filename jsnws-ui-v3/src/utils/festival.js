// 节日倒计时数据与「下一个节日」计算
// 口径与参考站归档页右栏「距离下一站」一致：
// 固定公历节日 + 农历传统节日 + 按规则推算的公历节日（复活节/母亲节/父亲节/感恩节）+ 清明，
// 从今天起向后扫描 400 天，按日期升序取最近的一个。

// 固定公历节日（月份按 1-12 记）
const FIXED_FESTIVALS = [
  { name: '元旦', month: 1, day: 1, kind: '国内' },
  { name: '情人节', month: 2, day: 14, kind: '国际' },
  { name: '妇女节', month: 3, day: 8, kind: '国际' },
  { name: '植树节', month: 3, day: 12, kind: '国内' },
  { name: '圣帕特里克节', month: 3, day: 17, kind: '国际' },
  { name: '愚人节', month: 4, day: 1, kind: '国际' },
  { name: '劳动节', month: 5, day: 1, kind: '国际' },
  { name: '青年节', month: 5, day: 4, kind: '国内' },
  { name: '儿童节', month: 6, day: 1, kind: '国际' },
  { name: '建党节', month: 7, day: 1, kind: '国内' },
  { name: '美国独立日', month: 7, day: 4, kind: '国际' },
  { name: '建军节', month: 8, day: 1, kind: '国内' },
  { name: '教师节', month: 9, day: 10, kind: '国内' },
  { name: '国庆节', month: 10, day: 1, kind: '国内' },
  { name: '万圣夜', month: 10, day: 31, kind: '国际' },
  { name: '双十一', month: 11, day: 11, kind: '国内' },
  { name: '平安夜', month: 12, day: 24, kind: '国际' },
  { name: '圣诞节', month: 12, day: 25, kind: '国际' },
  { name: '跨年夜', month: 12, day: 31, kind: '国际' }
]

// 农历传统节日（农历月/日）
const LUNAR_FESTIVALS = [
  { name: '春节', month: 1, day: 1 },
  { name: '元宵节', month: 1, day: 15 },
  { name: '龙抬头', month: 2, day: 2 },
  { name: '端午节', month: 5, day: 5 },
  { name: '七夕节', month: 7, day: 7 },
  { name: '中元节', month: 7, day: 15 },
  { name: '中秋节', month: 8, day: 15 },
  { name: '重阳节', month: 9, day: 9 },
  { name: '腊八节', month: 12, day: 8 },
  { name: '小年', month: 12, day: 23 }
]

// 二十四节气（仅用于判断清明）
const SOLAR_TERMS = [
  '小寒', '大寒', '立春', '雨水', '惊蛰', '春分', '清明', '谷雨',
  '立夏', '小满', '芒种', '夏至', '小暑', '大暑', '立秋', '处暑',
  '白露', '秋分', '寒露', '霜降', '立冬', '小雪', '大雪', '冬至'
]
const SOLAR_TERM_OFFSETS = [
  0, 21208, 42467, 63836, 85337, 107014, 128867, 150921,
  173149, 195551, 218072, 240693, 263343, 285989, 308563, 331033,
  353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758
]
const TROPICAL_YEAR_MS = 31556925974.7
const BEIJING_OFFSET_MS = 288e5
const SOLAR_TERM_EPOCH = Date.UTC(1900, 0, 5, 18, 5)

const DAY_MS = 864e5

// 农历月/日解析（浏览器内置中国农历）
let lunarFormatter = null
function getLunarFormatter() {
  if (!lunarFormatter) {
    lunarFormatter = new Intl.DateTimeFormat('zh-CN-u-ca-chinese', {
      month: 'numeric',
      day: 'numeric'
    })
  }
  return lunarFormatter
}

function atNoon(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate(), 12)
}

function sameDay(a, b) {
  return a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
}

function lunarParts(date) {
  const parts = getLunarFormatter().formatToParts(date)
  const month = parts.find(part => part.type === 'month')
  const day = parts.find(part => part.type === 'day')
  return {
    month: month ? Number(month.value) : NaN,
    day: day ? Number(day.value) : NaN
  }
}

// 复活节（西方教会算法）
function easter(year) {
  const a = year % 19
  const b = Math.floor(year / 100)
  const c = year % 100
  const d = Math.floor(b / 4)
  const e = b % 4
  const f = Math.floor((b + 8) / 25)
  const g = Math.floor((b - f + 1) / 3)
  const h = (19 * a + b - d - g + 15) % 30
  const i = Math.floor(c / 4)
  const k = c % 4
  const l = (32 + 2 * e + 2 * i - h - k) % 7
  const m = Math.floor((a + 11 * h + 22 * l) / 451)
  const month = Math.floor((h + l - 7 * m + 114) / 31)
  const day = ((h + l - 7 * m + 114) % 31) + 1
  return new Date(year, month - 1, day, 12)
}

// 某月第 n 个星期几（weekday: 0=周日）
function nthWeekday(year, monthIndex, weekday, n) {
  const offset = (weekday - new Date(year, monthIndex, 1).getDay() + 7) % 7
  return new Date(year, monthIndex, 1 + offset + (n - 1) * 7, 12)
}

// 判断某天是否落在某个节气上
function solarTermOf(date) {
  const year = date.getFullYear()
  for (let i = 0; i < SOLAR_TERM_OFFSETS.length; i += 1) {
    const utc = new Date(SOLAR_TERM_EPOCH + TROPICAL_YEAR_MS * (year - 1900) + SOLAR_TERM_OFFSETS[i] * 60000)
    const beijing = new Date(utc.getTime() + BEIJING_OFFSET_MS)
    if (beijing.getUTCFullYear() === date.getFullYear() &&
      beijing.getUTCMonth() === date.getMonth() &&
      beijing.getUTCDate() === date.getDate()) {
      return SOLAR_TERMS[i]
    }
  }
  return ''
}

// 某一天命中的全部节日
function festivalsOn(date) {
  const base = atNoon(date)
  const year = base.getFullYear()
  const month = base.getMonth() + 1
  const day = base.getDate()
  const result = []

  FIXED_FESTIVALS.forEach((festival) => {
    if (festival.month === month && festival.day === day) {
      result.push({ ...festival, date: base })
    }
  })

  const moveable = [
    { name: '复活节', date: easter(year), kind: '国际' },
    { name: '母亲节', date: nthWeekday(year, 4, 0, 2), kind: '国际' },
    { name: '父亲节', date: nthWeekday(year, 5, 0, 3), kind: '国际' },
    { name: '感恩节', date: nthWeekday(year, 10, 4, 4), kind: '国际' }
  ]
  moveable.forEach((festival) => {
    if (sameDay(festival.date, base)) {
      result.push({ ...festival, date: base })
    }
  })

  const lunar = lunarParts(base)
  const lunarFestival = LUNAR_FESTIVALS.find((festival) => {
    return festival.month === lunar.month && festival.day === lunar.day
  })
  if (lunarFestival) {
    result.push({ ...lunarFestival, date: base, kind: '传统' })
  }

  // 除夕：次日为农历正月初一
  const tomorrowLunar = lunarParts(new Date(year, base.getMonth(), day + 1, 12))
  if (tomorrowLunar.month === 1 && tomorrowLunar.day === 1) {
    result.push({ name: '除夕', date: base, kind: '传统' })
  }

  if (solarTermOf(base) === '清明') {
    result.push({ name: '清明节', date: base, kind: '国内' })
  }

  return result
}

function formatISO(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

// 同一天只保留一个同名节日，按日期升序
function upcomingFestivals(today) {
  const list = []
  for (let offset = 0; offset <= 400; offset += 1) {
    const date = new Date(today.getFullYear(), today.getMonth(), today.getDate() + offset, 12)
    list.push(...festivalsOn(date))
  }
  const unique = new Map()
  list.forEach((festival) => {
    const key = `${festival.name}-${festival.date.getFullYear()}-${festival.date.getMonth()}-${festival.date.getDate()}`
    unique.set(key, festival)
  })
  return Array.from(unique.values())
    .filter((festival) => festival.date >= today)
    .sort((a, b) => a.date.getTime() - b.date.getTime())
}

let cacheKey = ''
let cacheValue = null

/**
 * 取下一个节日（含剩余天数）
 * @param {Date} date 基准日期，默认今天
 * @returns {{name: string, kind: string, date: Date, days: number, dateISO: string}|null}
 */
export function getNextFestival(date = new Date()) {
  const today = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const key = formatISO(today)
  if (key === cacheKey) {
    return cacheValue
  }
  const festival = upcomingFestivals(today)[0]
  if (!festival) {
    cacheKey = key
    cacheValue = null
    return null
  }
  const from = Date.UTC(today.getFullYear(), today.getMonth(), today.getDate())
  const to = Date.UTC(festival.date.getFullYear(), festival.date.getMonth(), festival.date.getDate())
  const value = {
    name: festival.name,
    kind: festival.kind,
    date: festival.date,
    days: Math.max(0, Math.ceil((to - from) / DAY_MS)),
    dateISO: formatISO(festival.date)
  }
  cacheKey = key
  cacheValue = value
  return value
}
