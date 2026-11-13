<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <section class="dash-hero">
      <div class="hero-glow hero-glow-a"></div>
      <div class="hero-glow hero-glow-b"></div>
      <div class="hero-main">
        <p class="hero-date">{{ dateLabel }}</p>
        <h1 class="hero-title">{{ greeting }}，{{ adminName }}</h1>
        <p class="hero-sub">{{ siteName }} 运行正常，这里是站点的内容与数据概览。</p>
        <div class="hero-actions">
          <button type="button" class="hero-btn hero-btn-primary" @click="go('/postEdit')">
            写文章
          </button>
          <button type="button" class="hero-btn hero-btn-ghost" @click="openSite">
            查看站点
          </button>
        </div>
      </div>
      <div class="hero-aside">
        <div class="hero-aside-item">
          <span class="hero-aside-label">今日访问</span>
          <span class="hero-aside-value">{{ display(history.ip_count_today) }}</span>
        </div>
        <div class="hero-aside-split"></div>
        <div class="hero-aside-item">
          <span class="hero-aside-label">累计访问</span>
          <span class="hero-aside-value">{{ display(history.ip_history_count) }}</span>
        </div>
      </div>
    </section>

    <!-- 数据卡片 -->
    <section class="dash-stats">
      <div v-for="card in statCards" :key="card.key" class="stat-card">
        <span class="stat-icon" :style="{ background: card.bg, color: card.color }">
          <el-icon>
            <component :is="card.icon"/>
          </el-icon>
        </span>
        <div class="stat-body">
          <p class="stat-label">{{ card.label }}</p>
          <p class="stat-value">{{ display(card.value) }}</p>
        </div>
        <p class="stat-foot">{{ card.foot }}</p>
      </div>
    </section>

    <!-- 主体内容 -->
    <section class="dash-grid">
      <div class="dash-col">
        <!-- 最近文章 -->
        <div class="dash-card">
          <div class="card-head">
            <h2 class="card-title">最近文章</h2>
            <router-link class="card-link" to="/postList">全部文章 →</router-link>
          </div>
          <el-table :data="recentArticles" class="dash-table" empty-text="暂无文章">
            <el-table-column label="标题" min-width="200">
              <template #default="scope">
                <span class="article-title" :title="scope.row.articleTitle"
                      @click="editArticle(scope.row)">{{ scope.row.articleTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="110" align="center">
              <template #default="scope">
                {{ scope.row.sort ? scope.row.sort.sortName : '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览" width="80" align="center"></el-table-column>
            <el-table-column label="发布时间" width="150" align="center">
              <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.viewStatus === false ? 'info' : 'success'" size="small"
                        effect="light" disable-transitions>
                  {{ scope.row.viewStatus === false ? '隐藏' : '已发布' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 访问概览 -->
        <div class="dash-card">
          <div class="card-head">
            <h2 class="card-title">访问概览</h2>
            <span class="card-hint">数据来自站点访问记录</span>
          </div>
          <div class="visit-numbers">
            <div class="visit-item">
              <p class="visit-value">{{ display(history.ip_count_today) }}</p>
              <p class="visit-label">今日访问</p>
            </div>
            <div class="visit-item">
              <p class="visit-value">{{ display(history.ip_count_yest) }}</p>
              <p class="visit-label">昨日访问</p>
            </div>
            <div class="visit-item">
              <p class="visit-value">{{ display(history.ip_history_count) }}</p>
              <p class="visit-label">累计访问</p>
            </div>
          </div>
          <div v-if="provinceBars.length" class="visit-provinces">
            <div v-for="item in provinceBars" :key="item.province" class="province-row">
              <span class="province-name">{{ item.province }}</span>
              <span class="province-track">
                <span class="province-bar" :style="{ width: item.width }"></span>
              </span>
              <span class="province-num">{{ item.num }}</span>
            </div>
          </div>
          <p v-else class="empty-text">暂无省份访问数据</p>
        </div>
      </div>

      <div class="dash-col">
        <!-- 快捷入口 -->
        <div class="dash-card">
          <div class="card-head">
            <h2 class="card-title">快捷入口</h2>
          </div>
          <div class="quick-grid">
            <router-link v-for="item in quickLinks" :key="item.path" class="quick-item" :to="item.path">
              <span class="quick-icon" :style="{ background: item.bg, color: item.color }">
                <el-icon>
                  <component :is="item.icon"/>
                </el-icon>
              </span>
              <span class="quick-text">
                <strong>{{ item.title }}</strong>
                <small>{{ item.desc }}</small>
              </span>
            </router-link>
          </div>
        </div>

        <!-- 最新评论 -->
        <div class="dash-card">
          <div class="card-head">
            <h2 class="card-title">最新评论</h2>
            <router-link class="card-link" to="/commentList">全部评论 →</router-link>
          </div>
          <div v-if="recentComments.length" class="comment-list">
            <div v-for="comment in recentComments" :key="comment.id" class="comment-item">
              <el-avatar class="comment-avatar" :size="34" :src="comment.avatar">
                {{ (comment.username || 'U').slice(0, 1).toUpperCase() }}
              </el-avatar>
              <div class="comment-main">
                <div class="comment-top">
                  <span class="comment-name">{{ comment.username || '匿名用户' }}</span>
                  <span class="comment-type">{{ commentTypeText(comment.commentType) }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <p class="comment-content">{{ comment.commentContent }}</p>
              </div>
            </div>
          </div>
          <p v-else class="empty-text">暂无评论</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
  export default {
    name: "WelcomeView",

    data() {
      return {
        metrics: {
          article: null,
          comment: null,
          sort: null,
          label: null,
          user: null
        },
        history: {
          ip_history_count: null,
          ip_count_today: null,
          ip_count_yest: null,
          ip_history_province: []
        },
        recentArticles: [],
        recentComments: []
      }
    },

    computed: {
      admin() {
        return this.$store.state.currentAdmin || {};
      },

      adminName() {
        return this.$common.isEmpty(this.admin.username) ? "管理员" : this.admin.username;
      },

      siteName() {
        let webName = this.$store.state.webInfo.webName;
        return this.$common.isEmpty(webName) ? "King" : webName;
      },

      isBoss() {
        return !!this.admin.isBoss;
      },

      greeting() {
        const hour = new Date().getHours();
        if (hour < 6) return "凌晨好";
        if (hour < 12) return "上午好";
        if (hour < 14) return "中午好";
        if (hour < 18) return "下午好";
        return "晚上好";
      },

      dateLabel() {
        const now = new Date();
        const week = ["日", "一", "二", "三", "四", "五", "六"];
        return now.getFullYear() + " 年 " + (now.getMonth() + 1) + " 月 " + now.getDate() + " 日 · 星期" + week[now.getDay()];
      },

      statCards() {
        return [{
          key: "article",
          label: "文章",
          value: this.metrics.article,
          icon: "Tickets",
          bg: "rgba(76, 111, 255, 0.12)",
          color: "#4c6fff",
          foot: "累计发布"
        }, {
          key: "comment",
          label: "评论",
          value: this.metrics.comment,
          icon: "ChatDotRound",
          bg: "rgba(16, 185, 129, 0.12)",
          color: "#10b981",
          foot: "累计收到"
        }, {
          key: "sort",
          label: "分类",
          value: this.metrics.sort,
          icon: "Collection",
          bg: "rgba(249, 115, 22, 0.12)",
          color: "#f97316",
          foot: "内容分类"
        }, {
          key: "label",
          label: "标签",
          value: this.metrics.label,
          icon: "Paperclip",
          bg: "rgba(139, 92, 246, 0.12)",
          color: "#8b5cf6",
          foot: "内容标签"
        }, {
          key: "user",
          label: "用户",
          value: this.metrics.user,
          icon: "UserFilled",
          bg: "rgba(6, 182, 212, 0.12)",
          color: "#06b6d4",
          foot: "注册用户"
        }, {
          key: "visit",
          label: "累计访问",
          value: this.history.ip_history_count,
          icon: "Histogram",
          bg: "rgba(244, 63, 94, 0.12)",
          color: "#f43f5e",
          foot: "今日 " + this.display(this.history.ip_count_today)
        }];
      },

      quickLinks() {
        const links = [{
          path: "/postEdit",
          title: "写文章",
          desc: "发布新的内容",
          icon: "EditPen",
          bg: "rgba(76, 111, 255, 0.12)",
          color: "#4c6fff"
        }, {
          path: "/postList",
          title: "文章管理",
          desc: "编辑与维护文章",
          icon: "Tickets",
          bg: "rgba(16, 185, 129, 0.12)",
          color: "#10b981"
        }, {
          path: "/commentList",
          title: "评论管理",
          desc: "查看与清理评论",
          icon: "ChatDotRound",
          bg: "rgba(249, 115, 22, 0.12)",
          color: "#f97316"
        }];
        if (this.isBoss) {
          links.push({
            path: "/webEdit",
            title: "网站设置",
            desc: "站点信息与外观",
            icon: "Setting",
            bg: "rgba(139, 92, 246, 0.12)",
            color: "#8b5cf6"
          });
        }
        return links;
      },

      provinceBars() {
        const list = this.history.ip_history_province || [];
        if (!list.length) {
          return [];
        }
        const max = Math.max.apply(null, list.map(item => Number(item.num) || 0).concat(1));
        return list.slice(0, 5).map(item => ({
          province: item.province || "未知",
          num: item.num,
          width: Math.max(6, Math.round((Number(item.num) || 0) / max * 100)) + "%"
        }));
      }
    },

    created() {
      this.loadDashboard();
    },

    methods: {
      display(value) {
        return value === null || value === undefined || value === "" ? "—" : value;
      },

      formatTime(value) {
        if (!value) {
          return "—";
        }
        return String(value).replace("T", " ").slice(0, 16);
      },

      commentTypeText(type) {
        const map = {
          article: "文章评论",
          message: "留言",
          love: "祝福"
        };
        return map[type] || "评论";
      },

      go(path) {
        this.$router.push({path: path});
      },

      openSite() {
        window.open("/", "_blank");
      },

      editArticle(article) {
        this.$router.push({path: "/postEdit", query: {id: article.id}});
      },

      async loadDashboard() {
        const base = this.$constant.baseURL;
        const articleUrl = this.isBoss ? "/admin/article/boss/list" : "/admin/article/user/list";
        const commentUrl = this.isBoss ? "/admin/comment/boss/list" : "/admin/comment/user/list";

        const results = await Promise.allSettled([
          this.$http.post(base + articleUrl, {current: 1, size: 5}, true),
          this.$http.post(base + commentUrl, {current: 1, size: 6}, true),
          this.$http.get(base + "/webInfo/listSortAndLabel", {}, true),
          this.$http.get(base + "/webInfo/getHistoryInfo", {}, true)
        ]);

        const articleRes = results[0];
        if (articleRes.status === "fulfilled" && articleRes.value.data) {
          this.recentArticles = articleRes.value.data.records || [];
          this.metrics.article = articleRes.value.data.total;
        }

        const commentRes = results[1];
        if (commentRes.status === "fulfilled" && commentRes.value.data) {
          this.recentComments = commentRes.value.data.records || [];
          this.metrics.comment = commentRes.value.data.total;
        }

        const metaRes = results[2];
        if (metaRes.status === "fulfilled" && metaRes.value.data) {
          const meta = metaRes.value.data;
          this.metrics.sort = (meta.sorts || []).length;
          this.metrics.label = (meta.labels || []).length;
        }

        const historyRes = results[3];
        if (historyRes.status === "fulfilled" && historyRes.value.data) {
          this.history = Object.assign({}, this.history, historyRes.value.data);
        }

        if (this.isBoss) {
          this.$http.post(base + "/admin/user/list", {current: 1, size: 1}, true)
            .then((res) => {
              if (res.data) {
                this.metrics.user = res.data.total;
              }
            })
            .catch(() => {
            });
        }
      }
    }
  }
</script>

<style scoped>

  .dashboard {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  /* ---------- 欢迎横幅 ---------- */
  .dash-hero {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    overflow: hidden;
    padding: 30px 34px;
    border-radius: 16px;
    background: linear-gradient(120deg, #3a55e8 0%, #4c6fff 45%, #6b5cff 100%);
    color: #fff;
    box-shadow: 0 18px 40px -24px rgba(58, 85, 232, 0.8);
  }

  .hero-glow {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.14);
    pointer-events: none;
  }

  .hero-glow-a {
    width: 220px;
    height: 220px;
    right: 18%;
    top: -120px;
  }

  .hero-glow-b {
    width: 160px;
    height: 160px;
    right: -60px;
    bottom: -90px;
    background: rgba(255, 255, 255, 0.1);
  }

  .hero-main {
    position: relative;
    z-index: 1;
    min-width: 0;
  }

  .hero-date {
    margin: 0 0 8px;
    font-size: 13px;
    letter-spacing: 0.5px;
    color: rgba(255, 255, 255, 0.75);
  }

  .hero-title {
    margin: 0;
    font-size: 26px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }

  .hero-sub {
    margin: 10px 0 18px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.82);
  }

  .hero-actions {
    display: flex;
    gap: 10px;
  }

  .hero-btn {
    height: 36px;
    padding: 0 18px;
    border-radius: 9px;
    border: 1px solid transparent;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
  }

  .hero-btn:hover {
    transform: translateY(-1px);
  }

  .hero-btn-primary {
    background: #fff;
    color: #3a55e8;
    box-shadow: 0 8px 18px -10px rgba(15, 23, 42, 0.6);
  }

  .hero-btn-ghost {
    background: rgba(255, 255, 255, 0.12);
    border-color: rgba(255, 255, 255, 0.35);
    color: #fff;
  }

  .hero-btn-ghost:hover {
    background: rgba(255, 255, 255, 0.2);
  }

  .hero-aside {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: 26px;
    flex-shrink: 0;
    padding: 16px 26px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.16);
    backdrop-filter: blur(6px);
  }

  .hero-aside-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .hero-aside-label {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }

  .hero-aside-value {
    font-size: 24px;
    font-weight: 600;
    line-height: 1;
    font-variant-numeric: tabular-nums;
  }

  .hero-aside-split {
    width: 1px;
    height: 36px;
    background: rgba(255, 255, 255, 0.22);
  }

  /* ---------- 数据卡片 ---------- */
  .dash-stats {
    display: grid;
    grid-template-columns: repeat(6, minmax(0, 1fr));
    gap: 16px;
  }

  .stat-card {
    position: relative;
    padding: 18px 18px 14px;
    border-radius: 14px;
    background: var(--admin-card);
    border: 1px solid var(--admin-border);
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
    transition: transform 0.18s ease, box-shadow 0.18s ease;
  }

  .stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 14px 28px -18px rgba(15, 23, 42, 0.35);
  }

  .stat-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 38px;
    height: 38px;
    border-radius: 11px;
    font-size: 18px;
  }

  .stat-body {
    margin-top: 12px;
  }

  .stat-label {
    margin: 0;
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .stat-value {
    margin: 4px 0 0;
    font-size: 26px;
    font-weight: 650;
    line-height: 1.15;
    color: var(--admin-text);
    font-variant-numeric: tabular-nums;
  }

  .stat-foot {
    margin: 10px 0 0;
    padding-top: 10px;
    border-top: 1px dashed var(--admin-border);
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  /* ---------- 内容区 ---------- */
  .dash-grid {
    display: grid;
    grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
    gap: 20px;
    align-items: start;
  }

  .dash-col {
    display: flex;
    flex-direction: column;
    gap: 20px;
    min-width: 0;
  }

  .dash-card {
    padding: 20px 22px;
    border-radius: 14px;
    background: var(--admin-card);
    border: 1px solid var(--admin-border);
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
  }

  .card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;
  }

  .card-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: var(--admin-text);
  }

  .card-title::before {
    content: "";
    display: inline-block;
    width: 3px;
    height: 14px;
    margin-right: 8px;
    border-radius: 2px;
    background: #4c6fff;
    vertical-align: -2px;
  }

  .card-link {
    font-size: 12px;
    color: var(--admin-text-secondary);
    text-decoration: none;
    white-space: nowrap;
  }

  .card-link:hover {
    color: #4c6fff;
  }

  .card-hint {
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .dash-card :deep(.el-table) {
    box-shadow: none;
    border-radius: 0;
  }

  .dash-table :deep(.el-table__cell) {
    padding: 9px 0;
  }

  .article-title {
    display: block;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    color: var(--admin-text);
    cursor: pointer;
  }

  .article-title:hover {
    color: #4c6fff;
  }

  /* ---------- 访问概览 ---------- */
  .visit-numbers {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    padding: 14px 0 16px;
  }

  .visit-item {
    padding: 12px 16px;
    border-radius: 12px;
    background: var(--admin-subtle);
  }

  .visit-value {
    margin: 0;
    font-size: 22px;
    font-weight: 650;
    color: var(--admin-text);
    font-variant-numeric: tabular-nums;
  }

  .visit-label {
    margin: 4px 0 0;
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .visit-provinces {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-top: 6px;
  }

  .province-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: var(--admin-text-soft);
  }

  .province-name {
    width: 64px;
    flex-shrink: 0;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .province-track {
    flex: 1;
    height: 8px;
    border-radius: 999px;
    background: var(--admin-hover);
    overflow: hidden;
  }

  .province-bar {
    display: block;
    height: 100%;
    border-radius: 999px;
    background: linear-gradient(90deg, #7b93ff, #4c6fff);
  }

  .province-num {
    width: 40px;
    text-align: right;
    color: var(--admin-text-secondary);
    font-variant-numeric: tabular-nums;
  }

  /* ---------- 快捷入口 ---------- */
  .quick-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .quick-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px;
    border-radius: 12px;
    border: 1px solid var(--admin-border);
    text-decoration: none;
    transition: border-color 0.18s ease, background-color 0.18s ease, transform 0.18s ease;
  }

  .quick-item:hover {
    border-color: var(--el-color-primary-light-8);
    background: var(--admin-subtle);
    transform: translateY(-1px);
  }

  .quick-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    width: 34px;
    height: 34px;
    border-radius: 10px;
    font-size: 16px;
  }

  .quick-text {
    display: flex;
    flex-direction: column;
    min-width: 0;
    line-height: 1.3;
  }

  .quick-text strong {
    font-size: 13px;
    font-weight: 600;
    color: var(--admin-text);
    white-space: nowrap;
  }

  .quick-text small {
    margin-top: 3px;
    font-size: 11px;
    color: var(--admin-text-secondary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* ---------- 最新评论 ---------- */
  .comment-list {
    display: flex;
    flex-direction: column;
  }

  .comment-item {
    display: flex;
    gap: 10px;
    padding: 12px 0;
    border-bottom: 1px solid var(--admin-border);
  }

  .comment-item:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  .comment-avatar {
    flex-shrink: 0;
    background: var(--el-color-primary-light-9);
    color: #4c6fff;
    font-size: 13px;
    font-weight: 600;
  }

  .comment-main {
    min-width: 0;
    flex: 1;
  }

  .comment-top {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .comment-name {
    font-size: 13px;
    font-weight: 500;
    color: var(--admin-text);
  }

  .comment-type {
    padding: 1px 6px;
    border-radius: 5px;
    background: var(--admin-hover);
    font-size: 11px;
    color: var(--admin-text-secondary);
  }

  .comment-time {
    margin-left: auto;
    white-space: nowrap;
  }

  .comment-content {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin: 6px 0 0;
    font-size: 13px;
    line-height: 1.6;
    color: var(--admin-text-soft);
    word-break: break-word;
  }

  .empty-text {
    margin: 18px 0;
    text-align: center;
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  /* ---------- 响应式 ---------- */
  @media (max-width: 1400px) {
    .dash-stats {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  @media (max-width: 1100px) {
    .dash-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }

  @media (max-width: 768px) {
    .dash-hero {
      flex-direction: column;
      align-items: flex-start;
      padding: 24px;
    }

    .hero-aside {
      width: 100%;
      justify-content: space-between;
      padding: 14px 18px;
    }

    .hero-title {
      font-size: 22px;
    }

    .dash-stats {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .visit-numbers {
      grid-template-columns: 1fr;
    }

    .quick-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }

</style>
