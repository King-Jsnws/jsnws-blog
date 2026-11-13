<template>
  <div class="myFooter-wrap" v-show="showFooter">
    <div class="myFooter">
      <div class="pets-bar">
        <img class="pets-img" src="/images/pets-line.png" alt="动物" width="660" height="80">
      </div>
      <div class="footer-body">
        <el-avatar class="footer-avatar" :size="80" shape="circle"
                   :src="$store.state.webInfo.avatar || '/images/default-avatar.jpg'"></el-avatar>
        <div class="footer-desc">{{$store.state.webInfo.footer}}</div>
      </div>

      <!-- 徽章：建站运行时长，起点取站点信息里的 createTime，按秒实时更新；取不到时整块隐藏 -->
      <div class="footer-extra">
        <div class="footer-badge" v-if="hasSiteStart">
          <svg class="badge-icon" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.6"/>
            <ellipse cx="12" cy="12" rx="4" ry="9" fill="none" stroke="currentColor" stroke-width="1.4"/>
            <path d="M3.2 9h17.6M3.2 15h17.6" fill="none" stroke="currentColor" stroke-width="1.4"/>
          </svg>
          <span class="badge-text">本站已萌萌哒 {{ uptime.days }} 天 {{ uptime.hours }} 小时 {{ uptime.minutes }} 分 {{ uptime.seconds }} 秒</span>
          <svg class="badge-sparkle" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 2l2.2 7.8L22 12l-7.8 2.2L12 22l-2.2-7.8L2 12l7.8-2.2z" fill="currentColor"/>
          </svg>
        </div>

        <!-- 访问统计：今日访问 / 本站访问 后端暂未提供接口，取不到时显示占位符 -->
        <div class="footer-stats">
          <span class="stat-item">
            <span class="stat-label">今日访客</span>
            <span class="stat-value">{{ displayStat(historyInfo.ip_count_today) }}</span>
          </span>
          <span class="stat-sep"></span>
          <span class="stat-item">
            <span class="stat-label">今日访问</span>
            <span class="stat-value">{{ displayStat(historyInfo.page_view_today) }}</span>
          </span>
          <span class="stat-sep"></span>
          <span class="stat-item">
            <span class="stat-label">本站访客</span>
            <span class="stat-value">{{ displayStat(historyInfo.ip_history_count) }}</span>
          </span>
          <span class="stat-sep"></span>
          <span class="stat-item">
            <span class="stat-label">本站访问</span>
            <span class="stat-value">{{ displayStat(historyInfo.page_view_total) }}</span>
          </span>
        </div>

        <!-- 装饰分隔线 -->
        <div class="footer-divider">
          <span class="divider-dot"></span>
        </div>

        <!-- 版权 / RSS / Sitemap -->
        <div class="footer-meta">
          <span>© {{ currentYear }} {{ siteOwner }}. All Rights Reserved.</span>
          <span class="meta-sep">｜</span>
          <span class="meta-link">RSS</span>
          <span class="meta-sep">｜</span>
          <span class="meta-link">Sitemap</span>
        </div>
        <div class="footer-powered">Powered by Vue 3 &amp; Spring Boot</div>
      </div>
      <div class="icp" v-if="$store.state.sysConfig.beian">
        <svg class="beian-icon" viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
          <circle cx="12" cy="12" r="11" fill="#c9302c"/>
          <path d="M12 5l2.1 4.3 4.7.7-3.4 3.3.8 4.7-4.2-2.2-4.2 2.2.8-4.7L5.2 10l4.7-.7z" fill="#fff"/>
        </svg>
        <a :href="$store.state.sysConfig.beianUrl || 'http://beian.miit.gov.cn/'" target="_blank">{{$store.state.sysConfig.beian}}</a>
      </div>
      <div class="icp" v-if="$store.state.sysConfig.policeBeian">
        <svg class="beian-icon" viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
          <path d="M12 2l8 3v6c0 5-3.4 8.6-8 11-4.6-2.4-8-6-8-11V5z" fill="#1f5fa8"/>
          <path d="M12 7l1.3 2.7 3 .4-2.2 2.1.5 3-2.6-1.4-2.6 1.4.5-3-2.2-2.1 3-.4z" fill="#fff"/>
        </svg>
        <a :href="$store.state.sysConfig.policeBeianUrl || 'https://beian.mps.gov.cn/'" target="_blank">{{$store.state.sysConfig.policeBeian}}</a>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      showFooter: {
        type: Boolean,
        default: true
      }
    },
    data() {
      return {
        // 运行时长（秒），每秒刷新一次
        uptimeSeconds: 0,
        uptimeTimer: null,
        // 访问统计：后端 getHistoryInfo 只提供今日访客与本站访客
        historyInfo: {
          ip_count_today: null,
          ip_history_count: null
        }
      }
    },
    computed: {
      // 版权主体：优先站点名称，缺失时用站长名称
      siteOwner() {
        const webInfo = this.$store.state.webInfo || {}
        if (!this.$common.isEmpty(webInfo.webName)) {
          return webInfo.webName
        }
        const boss = this.$store.state.bossInfo || {}
        if (!this.$common.isEmpty(boss.username)) {
          return boss.username
        }
        return '本站'
      },
      currentYear() {
        return new Date().getFullYear()
      },
      // 站点起始时间：取 /webInfo/getWebInfo 返回的 web_info.create_time
      // （单行站点记录，不受文章分页/排序影响，每次进页面结果一致）
      siteStart() {
        const webInfo = this.$store.state.webInfo || {}
        return webInfo.createTime || ''
      },
      hasSiteStart() {
        return !this.$common.isEmpty(this.siteStart)
      },
      uptime() {
        const total = this.uptimeSeconds
        return {
          days: Math.floor(total / 86400),
          hours: Math.floor(total % 86400 / 3600),
          minutes: Math.floor(total % 3600 / 60),
          seconds: total % 60
        }
      }
    },
    created() {
      this.refreshUptime()
      this.uptimeTimer = setInterval(this.refreshUptime, 1000)
      this.getHistoryInfo()
    },
    beforeUnmount() {
      clearInterval(this.uptimeTimer)
    },
    methods: {
      // 按站点起始时间实时计算已运行秒数（起始时间缺失时保持 0，徽章不展示）
      refreshUptime() {
        const raw = this.siteStart
        if (this.$common.isEmpty(raw)) {
          this.uptimeSeconds = 0
          return
        }
        const start = new Date(typeof raw === 'number' ? raw : String(raw).replace(/-/g, '/')).getTime()
        if (isNaN(start)) {
          this.uptimeSeconds = 0
          return
        }
        this.uptimeSeconds = Math.max(0, Math.floor((Date.now() - start) / 1000))
      },
      // 统计接口公开可读；失败时保持空位，不显示假数据
      getHistoryInfo() {
        this.$http.get(this.$constant.baseURL + '/webInfo/getHistoryInfo').then((res) => {
          if (!this.$common.isEmpty(res.data)) {
            this.historyInfo = res.data
          }
        }).catch(() => {
        })
      },
      displayStat(value) {
        return this.$common.isEmpty(value) ? '—' : Number(value).toLocaleString('en-US')
      }
    }
  }
</script>

<style scoped>
  .myFooter-wrap {
    user-select: none;
    animation: hideToShow 2s;
  }

  .myFooter {
    background: var(--cardBg);
    color: var(--fontColor);
    padding-bottom: 18px;
  }

  .pets-bar {
    position: sticky;
    bottom: 0;
    z-index: 30;
    display: flex;
    justify-content: center;
    width: 100%;
    transform: translateY(25px);
  }

  /* 图片底部向上的白色渐变，让内容淡入页脚 */
  .pets-bar::after {
    content: "";
    position: absolute;
    left: 0;
    bottom: 25px;
    width: 100%;
    height: 60%;
    background: linear-gradient(to top, var(--cardBg), transparent);
    pointer-events: none;
  }

  .pets-img {
    position: relative;
    z-index: 40;
    width: 660px;
    height: 80px;
    max-width: 100%;
    display: block;
  }

  .footer-body {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    max-width: 900px;
    margin: 50px auto 0;
    padding: 16px 20px;
  }

  .footer-avatar {
    flex-shrink: 0;
    margin-right: 32px;
    box-shadow: 5px 11px 30px 20px rgba(255, 255, 255, 0.1);
    transition: transform 0.3s ease;
  }

  .footer-avatar:hover {
    transform: rotate(360deg);
  }

  .footer-desc {
    width: 90%;
    font-size: 14px;
    line-height: 1.8;
    color: var(--maxGreyFont);
    text-align: left;
    white-space: pre-line;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 4;
    overflow: hidden;
  }

  /* ===== 页脚上半部分：徽章 / 统计 / 分隔线 / 版权 ===== */
  .footer-extra {
    display: flex;
    flex-direction: column;
    align-items: center;
    max-width: 900px;
    margin: 26px auto 0;
    padding: 0 20px;
    text-align: center;
  }

  .footer-badge {
    display: inline-flex;
    align-items: center;
    max-width: 100%;
    padding: 6px 16px;
    border: 1px solid var(--borderColor);
    border-radius: 999px;
    background: var(--cardBg);
    font-size: 13px;
    line-height: 1.6;
    color: var(--maxGreyFont);
  }

  .badge-icon {
    flex-shrink: 0;
    width: 15px;
    height: 15px;
    margin-right: 7px;
    color: var(--maxGreyFont);
  }

  .badge-text {
    word-break: break-word;
  }

  .badge-sparkle {
    flex-shrink: 0;
    width: 11px;
    height: 11px;
    margin-left: 6px;
    color: var(--themeBackground);
  }

  .footer-stats {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;
    width: 100%;
    margin-top: 14px;
    font-size: 13px;
    color: var(--maxGreyFont);
  }

  .stat-item {
    display: inline-flex;
    align-items: center;
    padding: 2px 14px;
    white-space: nowrap;
  }

  .stat-label {
    margin-right: 6px;
  }

  .stat-value {
    color: var(--fontColor);
    font-weight: 600;
    font-variant-numeric: tabular-nums;
  }

  .stat-sep {
    width: 1px;
    height: 12px;
    /* 直接用当前主题的边框色，不再叠加透明度，深色下也能看见 */
    background: var(--borderHoverColor);
  }

  .footer-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    max-width: 520px;
    margin: 20px 0 16px;
  }

  .footer-divider::before,
  .footer-divider::after {
    content: "";
    flex: 1;
    height: 1px;
  }

  .footer-divider::before {
    background: linear-gradient(to right, transparent, var(--borderColor));
  }

  .footer-divider::after {
    background: linear-gradient(to left, transparent, var(--borderColor));
  }

  .divider-dot {
    width: 5px;
    height: 5px;
    margin: 0 10px;
    border-radius: 1px;
    background: var(--themeBackground);
    transform: rotate(45deg);
    opacity: 0.8;
  }

  .footer-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;
    width: 100%;
    font-size: 12px;
    line-height: 1.9;
    text-align: center;
    color: var(--maxGreyFont);
  }

  .meta-sep {
    margin: 0 10px;
    opacity: 0.45;
  }

  .meta-link {
    cursor: default;
  }

  .footer-powered {
    margin-top: 4px;
    font-size: 12px;
    letter-spacing: 0.4px;
    color: var(--maxGreyFont);
    opacity: 0.75;
  }

  .icp {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    margin-top: 22px;
    font-size: 12px;
  }

  .icp + .icp {
    margin-top: 8px;
  }

  .beian-icon {
    margin-right: 5px;
  }

  .icp a {
    color: var(--maxGreyFont);
    font-size: 12px;
    text-decoration: none;
    transition: all 0.3s;
  }

  .icp a:hover {
    color: var(--themeBackground);
  }

  @media (max-width: 768px) {
    .pets-bar {
      display: none;
    }

    .footer-body {
      margin-top: 20px;
      padding: 16px 14px;
    }

    .footer-avatar {
      margin-right: 18px;
    }

    .footer-desc {
      width: 100%;
    }

    .footer-extra {
      margin-top: 18px;
      padding: 0 14px;
    }

    .footer-badge {
      padding: 5px 12px;
      font-size: 12px;
    }

    .footer-stats {
      font-size: 12px;
    }

    .stat-item {
      padding: 2px 8px;
    }

    .footer-divider {
      margin: 16px 0 12px;
    }

    /* 窄屏：版权独占一行，RSS / Sitemap 另起一行居中，避免被右下角悬浮按钮压住 */
    .footer-meta {
      gap: 2px 14px;
    }

    .footer-meta > span:first-child {
      flex-basis: 100%;
    }

    .meta-sep {
      display: none;
    }
  }
</style>
