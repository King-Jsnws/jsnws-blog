<template>
  <!-- 归档页右栏：按参考站归档页右栏实际模块复刻（问候 / 站点统计 / 站点信息 / 日历+贡献图+最近更新 / 时间进度） -->
  <div class="archive-side">
    <div class="side-group">
      <!-- 问候卡 -->
      <section class="side-card greeting-card">
        <div class="greeting-info">
          <div class="greeting-copy">
            <p class="greeting-msg">{{ greetingText }}</p>
            <div class="greeting-meta">
              <time class="greeting-clock">{{ clockText }}</time>
              <div class="greeting-date">
                <span class="greeting-week">{{ weekEn }}</span>
                <div class="greeting-day">
                  <span>{{ now.getDate() }}</span>
                  <span>/{{ now.getMonth() + 1 }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="greeting-icon" aria-hidden="true">
            <svg v-if="isDaytime" viewBox="0 0 24 24" width="25" height="25" fill="none"
                 stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
              <circle cx="12" cy="12" r="4.2"/>
              <path d="M12 2.6v2.2M12 19.2v2.2M2.6 12h2.2M19.2 12h2.2M5.4 5.4l1.6 1.6M17 17l1.6 1.6M18.6 5.4 17 7M7 17l-1.6 1.6"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="25" height="25" fill="none"
                 stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 14.4A8.4 8.4 0 0 1 9.6 4a8.4 8.4 0 1 0 10.4 10.4z"/>
            </svg>
          </div>
        </div>
        <!-- 参考站同款：320×180 真实图片按 cover 铺满 278×144 区域，叠加顶部白色渐变 -->
        <div class="greeting-image" aria-hidden="true"></div>
      </section>

      <!-- 站点统计 -->
      <section class="side-card side-widget">
        <h2 class="widget-title">站点统计</h2>
        <div class="stat-list">
          <div v-for="item in statRows" :key="item.key" class="stat-row">
            <div class="row-label">
              <span class="row-icon" v-html="item.icon"></span>
              <span>{{ item.label }}</span>
            </div>
            <div class="row-value">
              <span>{{ item.value }}</span>
              <span v-if="item.unit" class="row-unit">{{ item.unit }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 站点信息 -->
      <section class="side-card side-widget info-widget">
        <h2 class="widget-title">站点信息</h2>
        <div class="info-block">
          <div class="info-list">
            <div v-for="item in infoRows" :key="item.label" class="info-row">
              <div class="row-label">
                <span class="row-icon" v-html="item.icon"></span>
                <span>{{ item.label }}</span>
              </div>
              <span class="info-value" :title="item.value">{{ item.value }}</span>
            </div>
          </div>

          <!-- 展开内容在按钮上方，与参考站 DOM 顺序一致 -->
          <div class="info-detail" :class="{ 'is-open': buildOpen }">
            <div class="info-detail-inner">
              <div class="build-grid">
                <div v-for="item in buildRows"
                     :key="item.label"
                     class="build-cell"
                     :class="{ 'is-wide': item.wide }">
                  <span class="build-icon" v-html="item.icon"></span>
                  <span class="build-label">{{ item.label }}</span>
                  <span class="build-value" :class="{ 'is-small': item.small }" :title="item.value">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>

          <button type="button" class="info-toggle" :aria-expanded="buildOpen ? 'true' : 'false'"
                  @click="buildOpen = !buildOpen">
            <svg class="toggle-arrow" :class="{ 'is-open': buildOpen }" viewBox="0 0 24 24" width="24" height="24"
                 fill="currentColor" aria-hidden="true">
              <path d="m12 14.4-4.6-4.6q-.275-.275-.275-.7t.275-.7.7-.275.7.275L12 12l3.2-3.2q.275-.275.7-.275t.7.275.275.7-.275.7z"/>
            </svg>
            <span>{{ buildOpen ? '收起构建信息' : '展开构建信息' }}</span>
          </button>
        </div>
      </section>
    </div>

    <div class="side-group">
      <!-- 日历 + 贡献图 + 最近更新（参考站同一张卡片内） -->
      <section v-if="!hideCalendar" class="side-card side-widget calendar-widget">
        <div class="calendar-head">
          <button type="button" class="calendar-nav" aria-label="上一月" @click="changeMonth(-1)">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M15 5l-7 7 7 7"/>
            </svg>
          </button>
          <div class="calendar-title">{{ calendarYear }}年{{ calendarMonth }}月</div>
          <button type="button" class="calendar-nav" aria-label="下一月" @click="changeMonth(1)">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M9 5l7 7-7 7"/>
            </svg>
          </button>
        </div>

        <div class="calendar-week">
          <span v-for="week in weekNames" :key="week">{{ week }}</span>
        </div>
        <div class="calendar-grid">
          <div v-for="(day, index) in calendarDays"
               :key="'day-' + index"
               class="calendar-day"
               :class="{ 'is-empty': !day.date, 'has-post': day.hasPost }">
            <span class="day-num">{{ day.date }}</span>
            <span v-if="day.hasPost" class="day-dot"></span>
          </div>
        </div>

        <div class="heatmap" aria-hidden="true">
          <div class="heatmap-months">
            <span v-for="month in 12" :key="'m-' + month">{{ month }}</span>
          </div>
          <div class="heatmap-grid">
            <span v-for="cell in heatmapCells"
                  :key="'h-' + cell.key"
                  class="heatmap-cell"
                  :class="'level-' + cell.level"></span>
          </div>
        </div>

        <div v-if="recentArticles.length" class="recent-list">
          <router-link v-for="article in recentArticles"
                       :key="'recent-' + article.id"
                       class="recent-item"
                       :to="`/article/${article.id}`">
            <span class="recent-title">{{ article.articleTitle }}</span>
            <span class="recent-date">{{ shortDate(article.createTime) }}</span>
          </router-link>
        </div>
      </section>

      <!-- 时间进度 -->
      <section class="side-card side-widget progress-widget">
        <h2 class="widget-title">时间进度</h2>
        <div class="progress-body">
          <div class="progress-list">
            <div v-for="item in progressRows" :key="item.key" class="progress-row">
              <span class="progress-percent">{{ item.percent }}%</span>
              <div class="progress-detail">
                <span class="progress-copy">{{ item.copy }}</span>
                <div class="progress-track">
                  <i :style="{ width: item.percent + '%' }"></i>
                </div>
              </div>
            </div>
          </div>

          <!-- 距离下一站：与参考站同结构、同口径（含当天「今天是 XX 快乐~」状态） -->
          <div v-if="nextFestival"
               class="milestone"
               :class="{ 'milestone--today': nextFestival.days === 0 }"
               aria-live="polite">
            <span class="milestone-label">{{ nextFestival.days === 0 ? '今天是' : '距离下一站 · ' + nextFestival.kind }}</span>
            <strong class="milestone-name">{{ nextFestival.days === 0 ? nextFestival.name + '快乐~' : nextFestival.name }}</strong>
            <div class="milestone-count">
              <span class="milestone-days">{{ nextFestival.days === 0 ? '' : nextFestival.days }}</span>
              <small>天</small>
            </div>
            <time class="milestone-date" :datetime="nextFestival.dateISO">{{ nextFestival.dateISO }}</time>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
  /* global __BUILD_TIME__ */
  import { getNextFestival } from '../../utils/festival';
  import { fetchAllArticles } from '../../utils/article';

  const WEEK_EN = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

  // 统计/信息行的图标：统一 20×20，颜色交给 CSS 的 currentColor
  const ICONS = {
    article: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true"><path d="M5 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9l5 5v13a2 2 0 0 1-2 2H5zm8-16v4h4l-4-4z"/></svg>',
    category: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true"><path d="M4 5h6v6H4zm10 0h6v6h-6zM4 13h6v6H4zm10 0h6v6h-6z"/></svg>',
    tag: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true"><path d="M3 5a2 2 0 0 1 2-2h6.2a2 2 0 0 1 1.4.6l7.8 7.8a2 2 0 0 1 0 2.8l-6.2 6.2a2 2 0 0 1-2.8 0L3.6 12.6A2 2 0 0 1 3 11.2V5zm4 1.5A1.5 1.5 0 1 0 7 9.5a1.5 1.5 0 0 0 0-3z"/></svg>',
    word: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true"><path d="M4 18a1 1 0 1 1 0-2h10a1 1 0 1 1 0 2H4zm0-5a1 1 0 1 1 0-2h16a1 1 0 1 1 0 2H4zm0-5a1 1 0 1 1 0-2h16a1 1 0 1 1 0 2H4z"/></svg>',
    clock: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true"><path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 10.6V7a1 1 0 0 0-2 0v6a1 1 0 0 0 .3.7l3.3 3.3a1 1 0 0 0 1.4-1.4L13 12.6z"/></svg>',
    activity: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true"><path d="M3 12h4l2.2-5.5 4.2 11L15.6 12H21v2h-4.2l-2.6 6.4-4.2-11L7.7 14H3z"/></svg>',
    platform: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M4 4h16v5H4zm0 7h7v9H4zm9 0h7v9h-7z"/></svg>',
    version: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M12 2 3 6.5v11L12 22l9-4.5v-11L12 2zm0 2.2 6.8 3.4L12 11 5.2 7.6 12 4.2z"/></svg>',
    license: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M6 2h9l4 4v16H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm8 2v3h3l-3-3zM8 11h8v2H8zm0 4h8v2H8zm0-8h4v2H8z"/></svg>',
    language: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm6.9 6h-2.9a15.7 15.7 0 0 0-1.2-3.6A8 8 0 0 1 18.9 8zM12 4.2c.6.9 1.2 2.2 1.6 3.8h-3.2c.4-1.6 1-2.9 1.6-3.8zM4.3 14a8.2 8.2 0 0 1 0-4h3.2a17 17 0 0 0 0 4zm.8 2h2.9c.3 1.3.7 2.5 1.2 3.6A8 8 0 0 1 5.1 16zm2.9-8H5.1a8 8 0 0 1 4.1-3.6A15.7 15.7 0 0 0 8 8zm4 11.8c-.6-.9-1.2-2.2-1.6-3.8h3.2c-.4 1.6-1 2.9-1.6 3.8zm1.9-5.8H10a15 15 0 0 1 0-4h3.9a15 15 0 0 1 0 4zm.5 5.6c.5-1.1.9-2.3 1.2-3.6h2.9a8 8 0 0 1-4.1 3.6zm1.6-5.6a17 17 0 0 0 0-4h3.2a8.2 8.2 0 0 1 0 4z"/></svg>',
    vue: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M12 21 2 4h5.2L12 12.7 16.8 4H22L12 21z"/></svg>',
    spring: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M20.6 3.4C17 5.7 13.9 7 10.9 7.3 7.5 7.7 5 9.5 5 12.5c0 3 2.4 5.2 5.4 5.2 3.6 0 6.1-2.7 6.4-6.4.2-2.4 1.2-5 3.8-7.9zM4 20c3.4-2.4 6.3-3.7 9.6-4.2-2.6-1.5-4.4-1.7-6.1-1.3C5 15 3 16.8 3 19.2c0 .3 0 .6.1.8H4z"/></svg>',
    element: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M12 2 3 6.6v10.8L12 22l9-4.6V6.6L12 2zm0 2.3 6.7 3.4L12 11.1 5.3 7.7 12 4.3zM5.2 9.5l6 3.1v6.9l-6-3.1V9.5zm7.6 10V12.6l6-3.1v6.9l-6 3.1z"/></svg>',
    build: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="m21.7 18.6-7.2-7.2a6.4 6.4 0 0 0-7.9-7.9L10 6.9 6.8 10 3.4 6.6a6.4 6.4 0 0 0 7.9 7.9l7.2 7.2a1.6 1.6 0 0 0 2.3 0l.9-.9a1.6 1.6 0 0 0 0-2.2zM4.4 4.4l3.2 3.2-2.6 2.6 1.4 1.4 2.6-2.6 3.2 3.2c-.9.3-1.9.4-2.9.2l-3.2 3.2-1.4-1.4 2.6-2.6c-1.7-.6-3.1-1.9-3.8-3.5a4.6 4.6 0 0 1-.3-3.3z"/></svg>',
    monitor: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M3 4h18a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1h-7v2h3v2H7v-2h3v-2H3a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1zm1 2v9h16V6H4z"/></svg>'
  };

  export default {
    name: 'ArchiveSidebar',
    props: {
      // 由归档页传入的全量文章，用于统计、最近更新、日历标记与贡献图
      articles: {
        type: Array,
        default: () => []
      },
      // 该页左栏已有日历时传 true，避免同页出现两份日历
      hideCalendar: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        now: new Date(),
        clockTimer: null,
        midnightTimer: null,
        // 页面未传入全量文章时，组件自行拉取，保证统计 / 最近更新 / 日历标记一致
        localArticles: [],
        weekNames: ['日', '一', '二', '三', '四', '五', '六'],
        calendarYear: new Date().getFullYear(),
        calendarMonth: new Date().getMonth() + 1,
        buildOpen: false
      };
    },
    computed: {
      // 优先用页面传入的全量文章；未传时用组件自拉的数据
      allArticles() {
        return this.articles.length ? this.articles : this.localArticles;
      },
      isDaytime() {
        const hour = this.now.getHours();
        return hour >= 6 && hour < 18;
      },
      greetingText() {
        const hour = this.now.getHours();
        if (hour < 5) return '夜深了，早点休息！';
        if (hour < 9) return '早上好，新的一天！';
        if (hour < 12) return '上午好，继续加油！';
        if (hour < 14) return '中午好，记得午休！';
        if (hour < 18) return '下午好，喝杯茶吧！';
        return '晚上好，放松一下！';
      },
      clockText() {
        const hh = String(this.now.getHours()).padStart(2, '0');
        const mm = String(this.now.getMinutes()).padStart(2, '0');
        return hh + ':' + mm;
      },
      weekEn() {
        return WEEK_EN[this.now.getDay()];
      },
      sortedArticles() {
        return this.allArticles.slice().sort((a, b) => {
          return String(b.createTime || '').localeCompare(String(a.createTime || ''));
        });
      },
      recentArticles() {
        return this.sortedArticles.slice(0, 3);
      },
      navigationBar() {
        return this.$store.getters.navigationBar || [];
      },
      webInfo() {
        return this.$store.state.webInfo || {};
      },
      stats() {
        const articles = this.allArticles;
        const tags = new Set();
        const categories = new Set();
        this.navigationBar.forEach((sort) => {
          if (sort.sortName) categories.add(sort.sortName);
          (sort.labels || []).forEach((label) => tags.add(label.labelName));
        });
        // 导航数据未就绪时，用文章自身的分类/标签兜底
        articles.forEach((article) => {
          if (article.sort && article.sort.sortName) categories.add(article.sort.sortName);
          if (Array.isArray(article.labels)) {
            article.labels.forEach((label) => label.labelName && tags.add(label.labelName));
          } else if (article.label && article.label.labelName) {
            tags.add(article.label.labelName);
          }
        });
        let words = 0;
        articles.forEach((article) => {
          words += this.wordCount(article.articleContent);
        });
        return {
          articles: articles.length || this.$store.getters.articleTotal || 0,
          categories: categories.size,
          tags: tags.size,
          words: words,
          uptimeDays: this.uptimeDays,
          lastActive: this.lastActiveText
        };
      },
      uptimeDays() {
        const raw = this.webInfo.createTime;
        if (!raw) return 0;
        const start = new Date(String(raw).replace(/-/g, '/')).getTime();
        if (isNaN(start)) return 0;
        return Math.max(0, Math.floor((Date.now() - start) / 86400000));
      },
      lastActiveText() {
        const latest = this.sortedArticles[0];
        if (!latest || !latest.createTime) return '—';
        const day = String(latest.createTime).split(' ')[0];
        const target = new Date(day.replace(/-/g, '/')).getTime();
        if (isNaN(target)) return day;
        const today = new Date();
        const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
        const diff = Math.round((todayStart - target) / 86400000);
        if (diff <= 0) return '今天';
        if (diff === 1) return '昨天';
        if (diff < 30) return diff + ' 天前';
        return day;
      },
      statRows() {
        const s = this.stats;
        return [
          { key: 'articles', label: '文章', value: this.formatNumber(s.articles), icon: ICONS.article },
          { key: 'categories', label: '分类', value: this.formatNumber(s.categories), icon: ICONS.category },
          { key: 'tags', label: '标签', value: this.formatNumber(s.tags), icon: ICONS.tag },
          { key: 'words', label: '总字数', value: this.formatNumber(s.words), icon: ICONS.word },
          { key: 'uptime', label: '运行时长', value: this.formatNumber(s.uptimeDays), unit: '天', icon: ICONS.clock },
          { key: 'active', label: '最后活动', value: s.lastActive, icon: ICONS.activity }
        ];
      },
      infoRows() {
        return [
          { label: '构建平台', value: 'Vue 3 + Spring Boot', icon: ICONS.platform },
          { label: '博客版本', value: 'jsnws-ui V3', icon: ICONS.version },
          { label: '文章许可', value: 'CC BY-NC-SA 4.0', icon: ICONS.license }
        ];
      },
      buildRows() {
        // 字段槽位与参考站一致（域名 + 4 个版本项 + 构建时间 + 系统信息），值换成本站真实技术栈
        return [
          { label: '站点域名', value: this.$constant.host, icon: ICONS.language, wide: true, small: true },
          { label: 'jsnws-ui', value: 'V3', icon: ICONS.version },
          { label: 'Vue', value: '3.5.13', icon: ICONS.vue },
          { label: 'Spring Boot', value: '2.7.18', icon: ICONS.spring },
          { label: 'Element Plus', value: '2.4.4', icon: ICONS.element },
          { label: '构建时间', value: this.buildTimeText, icon: ICONS.build, wide: true, small: true },
          { label: '系统信息', value: 'Java 17 / MySQL 8.0.33', icon: ICONS.monitor, wide: true, small: true }
        ];
      },
      // 构建时间：生产构建由 DefinePlugin 注入；开发态退化为页面最后修改时间
      buildTimeText() {
        if (typeof __BUILD_TIME__ === 'string') {
          return __BUILD_TIME__;
        }
        const modified = new Date(document.lastModified);
        if (isNaN(modified.getTime())) return '—';
        const pad = value => String(value).padStart(2, '0');
        return `${modified.getFullYear()}年${modified.getMonth() + 1}月${modified.getDate()}日 ` +
          `${pad(modified.getHours())}:${pad(modified.getMinutes())}:${pad(modified.getSeconds())}`;
      },
      // 下一个节日（含剩余天数），跨天由午夜定时器触发重算
      nextFestival() {
        return getNextFestival(this.now);
      },
      // 当月日历：日期 + 当天是否有文章
      calendarDays() {
        const year = this.calendarYear;
        const month = this.calendarMonth;
        const firstWeekDay = new Date(year, month - 1, 1).getDay();
        const daysInMonth = new Date(year, month, 0).getDate();
        const today = new Date();
        const isCurrentMonth = today.getFullYear() === year && today.getMonth() + 1 === month;
        const postedDays = new Set();
        this.allArticles.forEach((article) => {
          const day = String(article.createTime || '').split(' ')[0];
          const parts = day.split('-');
          if (parts.length === 3 && Number(parts[0]) === year && Number(parts[1]) === month) {
            postedDays.add(Number(parts[2]));
          }
        });
        const days = [];
        for (let i = 0; i < firstWeekDay; i++) {
          days.push({ date: null, hasPost: false });
        }
        for (let d = 1; d <= daysInMonth; d++) {
          days.push({
            date: d,
            hasPost: postedDays.has(d) || (isCurrentMonth && today.getDate() === d)
          });
        }
        while (days.length % 7 !== 0) {
          days.push({ date: null, hasPost: false });
        }
        return days;
      },
      // 贡献图：12 个月 × 4 个周区间，按文章数量分 4 档
      heatmapCells() {
        const year = this.calendarYear;
        const counts = new Array(48).fill(0);
        this.allArticles.forEach((article) => {
          const date = String(article.createTime || '').split(' ')[0].split('-');
          if (date.length < 3 || Number(date[0]) !== year) return;
          const month = Number(date[1]);
          const day = Number(date[2]);
          const week = Math.min(3, Math.floor((day - 1) / 7));
          counts[(month - 1) * 4 + week] += 1;
        });
        const max = Math.max(1, ...counts);
        return counts.map((count, index) => {
          const level = count === 0 ? 0 : Math.min(4, Math.ceil((count / max) * 4));
          return { key: index, level };
        });
      },
      progressRows() {
        const today = new Date();
        const year = today.getFullYear();
        const month = today.getMonth();
        const day = today.getDate();
        const daysInYear = ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0) ? 366 : 365;
        const dayOfYear = Math.floor((new Date(year, month, day) - new Date(year, 0, 1)) / 86400000) + 1;
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        // 参考站按周一为一周开始：周日取 7，进度 100%，还剩 0 天
        const dayOfWeek = today.getDay() || 7;
        return [
          {
            key: 'year',
            percent: this.percent(dayOfYear, daysInYear),
            copy: '本年还剩 ' + (daysInYear - dayOfYear) + ' 天'
          },
          {
            key: 'month',
            percent: this.percent(day, daysInMonth),
            copy: '本月还剩 ' + (daysInMonth - day) + ' 天'
          },
          {
            key: 'week',
            percent: this.percent(dayOfWeek, 7),
            copy: '本周还剩 ' + (7 - dayOfWeek) + ' 天'
          }
        ];
      }
    },
    created() {
      this.clockTimer = setInterval(() => {
        this.now = new Date();
      }, 30000);
      this.scheduleMidnightUpdate();
      if (!this.articles.length) {
        fetchAllArticles(this.$http, this.$constant)
          .then((articles) => {
            this.localArticles = articles;
          })
          .catch(() => {
            this.localArticles = [];
          });
      }
    },
    beforeUnmount() {
      clearInterval(this.clockTimer);
      clearTimeout(this.midnightTimer);
    },
    methods: {
      // 参考站口径：跨过零点后立即重算「距离下一站」
      scheduleMidnightUpdate() {
        const now = new Date();
        const next = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
        this.midnightTimer = setTimeout(() => {
          this.now = new Date();
          this.scheduleMidnightUpdate();
        }, next.getTime() - now.getTime() + 100);
      },
      changeMonth(step) {
        const date = new Date(this.calendarYear, this.calendarMonth - 1 + step, 1);
        this.calendarYear = date.getFullYear();
        this.calendarMonth = date.getMonth() + 1;
      },
      percent(value, total) {
        if (!total) return '0.0';
        return ((value / total) * 100).toFixed(1);
      },
      formatNumber(value) {
        return Number(value || 0).toLocaleString('en-US');
      },
      shortDate(createTime) {
        const parts = String(createTime || '').split(' ')[0].split('-');
        return parts.length >= 3 ? `${parts[1]}-${Number(parts[2])}` : createTime;
      },
      // 与文章卡一致的字数口径：去掉代码块、图片/链接语法、markdown 符号与空白
      wordCount(content) {
        const text = String(content || '')
          .replace(/```[\s\S]*?```/g, '')
          .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
          .replace(/\[[^\]]*\]\([^)]*\)/g, '')
          .replace(/[#>*`\-_~|]/g, '')
          .replace(/\s+/g, '');
        return text.length;
      }
    }
  };
</script>

<style scoped>
  .archive-side {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    align-content: start;
    min-width: 0;
  }

  .side-group {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    align-content: start;
    min-width: 0;
  }

  /* 与站内其它侧栏卡同一套外壳 */
  .side-card {
    min-width: 0;
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  /* 卡片标题：18px/700，左缩进 32px，左侧 4×16 主色竖条 */
  .widget-title {
    position: relative;
    margin: 16px 16px 8px 32px;
    font-size: 18px;
    font-weight: 700;
    line-height: 1.4;
    color: var(--fontColor);
  }

  .widget-title::before {
    content: "";
    position: absolute;
    top: 5.5px;
    left: -16px;
    width: 4px;
    height: 16px;
    border-radius: 6px;
    background: var(--ui-accent);
  }

  /* ---------- 问候卡 ---------- */
  .greeting-card {
    overflow: hidden;
  }

  .greeting-info {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 19px 20px 14px;
  }

  .greeting-msg {
    margin: 0;
    font-size: 15.7px;
    font-weight: 700;
    line-height: 1.35;
    color: var(--fontColor);
  }

  .greeting-meta {
    display: flex;
    align-items: flex-end;
    gap: 12px;
    margin-top: 5px;
  }

  .greeting-clock {
    font-size: 34px;
    font-weight: 700;
    line-height: 1;
    letter-spacing: -0.5px;
    color: var(--fontColor);
    font-variant-numeric: tabular-nums;
  }

  .greeting-date {
    padding-bottom: 2px;
  }

  .greeting-week {
    display: block;
    font-size: 10.5px;
    font-weight: 700;
    line-height: 1.1;
    letter-spacing: 0.4px;
    color: var(--greyFont);
  }

  .greeting-day {
    font-size: 13px;
    font-weight: 700;
    line-height: 1.35;
    color: var(--greyFont);
    font-variant-numeric: tabular-nums;
  }

  .greeting-icon {
    flex: none;
    margin-top: 2px;
    color: #8b5cf6;
  }

  /* 参考站同款：320×180 图片 cover 铺满 144px 高区域，顶部叠 16% 白色渐变 */
  .greeting-image {
    position: relative;
    height: 144px;
    background: url("/images/greeting/greeting-01-320x180.jpg") center / cover no-repeat;
  }

  .greeting-image::after {
    content: "";
    position: absolute;
    inset: 0;
    background: linear-gradient(rgba(255, 255, 255, 0.16), rgba(0, 0, 0, 0) 30%);
  }

  /* ---------- 统计 / 信息行 ---------- */
  .stat-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 0 16px 16px;
  }

  .stat-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 36px;
    padding: 6px 12px;
  }

  .row-label {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
    font-size: 14px;
    font-weight: 500;
    color: var(--ui-accent);
  }

  .row-label > span:last-child {
    color: var(--fontColor);
    white-space: nowrap;
  }

  .row-icon {
    display: inline-flex;
    flex: none;
    color: var(--ui-accent);
  }

  .row-value {
    display: flex;
    align-items: baseline;
    flex: none;
    font-size: 16px;
    font-weight: 700;
    color: var(--fontColor);
    font-variant-numeric: tabular-nums;
  }

  .row-unit {
    margin-left: 4px;
    font-size: 14px;
    font-weight: 400;
    color: var(--greyFont);
  }

  /* 参考站结构：三行基本信息 + 可折叠的 2 列构建信息网格 + 底部展开按钮 */
  .info-block {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 0 16px;
  }

  .info-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .info-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    min-height: 36px;
    padding: 8px 12px;
    border-radius: 8px;
    background: color-mix(in srgb, var(--fontColor) 5%, var(--cardBg));
  }

  .info-row .row-label {
    flex: none;
    font-size: 14px;
  }

  .info-value {
    min-width: 0;
    max-width: 55%;
    overflow: hidden;
    font-size: 12px;
    font-weight: 700;
    color: var(--fontColor);
    text-align: right;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .info-detail {
    display: grid;
    grid-template-rows: 0fr;
    transition: grid-template-rows 0.2s ease;
  }

  .info-detail.is-open {
    grid-template-rows: 1fr;
  }

  .info-detail-inner {
    min-height: 0;
    overflow: hidden;
  }

  .build-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .build-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    min-width: 0;
    padding: 8px 4px;
    border-radius: 8px;
    background: color-mix(in srgb, var(--fontColor) 5%, var(--cardBg));
    text-align: center;
  }

  .build-cell.is-wide {
    grid-column: span 2;
  }

  .build-icon {
    display: inline-flex;
    color: var(--ui-accent);
  }

  .build-label {
    font-size: 12px;
    line-height: 16px;
    color: var(--greyFont);
  }

  .build-value {
    max-width: 100%;
    overflow: hidden;
    font-size: 14px;
    font-weight: 700;
    line-height: 20px;
    color: var(--fontColor);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .build-value.is-small {
    font-size: 12px;
    line-height: 18px;
  }

  .info-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    width: 100%;
    height: 32px;
    margin-top: 0;
    padding: 0;
    border: 0;
    border-radius: 8px;
    background: none;
    color: var(--ui-accent);
    font-family: inherit;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.15s ease;
  }

  .info-toggle:hover {
    background: color-mix(in srgb, var(--ui-accent) 8%, transparent);
  }

  .toggle-arrow {
    transition: transform 0.2s ease;
  }

  .toggle-arrow.is-open {
    transform: rotate(180deg);
  }

  .side-widget {
    padding-bottom: 16px;
  }

  /* ---------- 日历 ---------- */
  .calendar-widget {
    padding: 16px 16px 16px;
  }

  .calendar-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .calendar-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--fontColor);
  }

  .calendar-nav {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    padding: 0;
    border: 1px solid var(--cardBorder);
    border-radius: 8px;
    background: none;
    color: var(--ui-accent);
    cursor: pointer;
    transition: border-color 0.15s ease, background-color 0.15s ease;
  }

  .calendar-nav:hover {
    border-color: var(--ui-accent);
    background: color-mix(in srgb, var(--ui-accent) 8%, transparent);
  }

  .calendar-week,
  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    gap: 4px;
  }

  .calendar-week {
    margin-bottom: 8px;
  }

  .calendar-week span {
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--greyFont);
    text-align: center;
  }

  .calendar-day {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    aspect-ratio: 1 / 1;
    border-radius: 4px;
    font-size: 14px;
    color: var(--fontColor);
  }

  .calendar-day.is-empty {
    visibility: hidden;
  }

  .calendar-day.has-post {
    font-weight: 700;
  }

  .day-dot {
    position: absolute;
    left: 50%;
    bottom: 1px;
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: var(--ui-accent);
    transform: translateX(-50%);
  }

  /* ---------- 贡献图 ---------- */
  .heatmap {
    margin-top: 12px;
  }

  .heatmap-months {
    display: grid;
    grid-template-columns: repeat(12, minmax(0, 1fr));
    gap: 2px;
    margin-bottom: 4px;
  }

  .heatmap-months span {
    font-size: 10px;
    line-height: 15px;
    color: var(--greyFont);
    text-align: center;
  }

  .heatmap-grid {
    display: grid;
    grid-template-columns: repeat(12, minmax(0, 1fr));
    grid-template-rows: repeat(4, minmax(0, 1fr));
    grid-auto-flow: column;
    gap: 3px;
  }

  .heatmap-cell {
    aspect-ratio: 1 / 1;
    border-radius: 4px;
    background: color-mix(in srgb, var(--ui-accent) 7%, var(--cardBg));
  }

  .heatmap-cell.level-1 {
    background: color-mix(in srgb, var(--ui-accent) 28%, var(--cardBg));
  }

  .heatmap-cell.level-2 {
    background: color-mix(in srgb, var(--ui-accent) 48%, var(--cardBg));
  }

  .heatmap-cell.level-3 {
    background: color-mix(in srgb, var(--ui-accent) 70%, var(--cardBg));
  }

  .heatmap-cell.level-4 {
    background: var(--ui-accent);
  }

  /* ---------- 最近更新 ---------- */
  .recent-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-top: 12px;
  }

  .recent-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 14px;
    color: var(--fontColor);
    text-decoration: none;
    transition: color 0.15s ease, background-color 0.15s ease;
  }

  .recent-item:hover {
    background: color-mix(in srgb, var(--ui-accent) 8%, transparent);
    color: var(--ui-accent);
  }

  .recent-title {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .recent-date {
    flex: none;
    font-size: 12px;
    color: var(--greyFont);
    font-variant-numeric: tabular-nums;
  }

  /* ---------- 时间进度 ---------- */
  .progress-body {
    padding: 4px 16px 0;
  }

  .progress-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .progress-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }

  .progress-percent {
    flex: none;
    width: 50px;
    font-size: 12.3px;
    font-weight: 700;
    line-height: 18px;
    color: var(--ui-accent);
    font-variant-numeric: tabular-nums;
  }

  .progress-detail {
    flex: 1;
    min-width: 0;
  }

  .progress-copy {
    display: block;
    font-size: 12.2px;
    line-height: 18px;
    color: var(--fontColor);
  }

  .progress-track {
    height: 7px;
    margin-top: 4px;
    border-radius: 999px;
    background: color-mix(in srgb, var(--ui-accent) 10%, var(--cardBg));
    overflow: hidden;
  }

  .progress-track i {
    display: block;
    height: 100%;
    border-radius: 999px;
    background: var(--ui-accent);
  }

  /* ---------- 距离下一站 ---------- */
  .milestone {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    grid-template-rows: auto auto auto;
    margin-top: 16px;
    padding-top: 13.6px;
    border-top: 1px dashed color-mix(in srgb, var(--fontColor) 8%, transparent);
  }

  .milestone-label {
    grid-column: 1 / -1;
    font-size: 10.9px;
    line-height: 16.3px;
    color: var(--greyFont);
  }

  .milestone-name {
    grid-column: 1;
    margin-top: 1.6px;
    overflow: hidden;
    font-size: 15.2px;
    font-weight: 700;
    line-height: 22.8px;
    color: var(--fontColor);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .milestone-count {
    display: flex;
    grid-column: 2;
    grid-row: 2 / span 2;
    align-items: baseline;
    align-self: end;
    gap: 2.88px;
    color: var(--ui-accent);
  }

  .milestone-days {
    font-size: 33.6px;
    font-weight: 800;
    line-height: 1;
    font-variant-numeric: tabular-nums;
  }

  .milestone-count small {
    font-size: 10.88px;
    font-weight: 700;
    line-height: 1;
  }

  .milestone-date {
    grid-column: 1;
    margin-top: 2.88px;
    font-size: 10.9px;
    line-height: 16.3px;
    color: var(--greyFont);
  }
</style>
