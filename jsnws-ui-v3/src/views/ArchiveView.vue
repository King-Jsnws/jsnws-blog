<template>
  <div>
    <div class="archive-page my-animation-slide-bottom">
      <aside class="archive-aside">
        <!-- 右栏已有参考站日历，左栏关闭同款日历卡，避免同页重复 -->
        <MyAside hide-calendar
                 @selectSort="selectSort"
                 @selectArticle="selectArticle"></MyAside>
      </aside>

      <main class="archive-main">
        <section class="archive-card">
          <h1 class="sr-only">归档</h1>

          <!-- 筛选摘要：仅在带 category / tag 参数时出现（与参考站一致） -->
          <div v-if="categoryName || tagNames.length" class="archive-breadcrumb">
            <div class="crumb-left">
              <template v-if="categoryName">
                <router-link class="crumb-root" to="/categories">分类</router-link>
                <span class="crumb-sep">/</span>
                <span class="crumb-tag">{{ categoryName }}</span>
              </template>
              <template v-else>
                <router-link class="crumb-root" to="/tags">标签</router-link>
                <span class="crumb-sep">/</span>
                <template v-for="(name, index) in tagNames" :key="'crumb-' + name">
                  <router-link class="crumb-tag"
                               :to="{path: '/archive', query: {tag: name}}">#{{ name }}</router-link>
                  <span v-if="index < tagNames.length - 1" class="crumb-sep">/</span>
                </template>
              </template>
            </div>
            <div class="crumb-count">{{ filteredArticles.length }} 篇文章</div>
          </div>

          <div v-if="loading" class="archive-state">正在加载归档…</div>

          <div v-else-if="error" class="archive-state">
            <p>{{ error }}</p>
            <p class="state-tip">请检查网络或稍后重试</p>
          </div>

          <template v-else>
            <div v-for="year in yearGroups" :key="year.key" class="archive-year">
              <button type="button"
                      class="year-head"
                      :aria-expanded="String(isYearOpen(year.key))"
                      @click="toggleYear(year.key)">
                <div class="col-year">
                  <span class="year-num">{{ year.key }}</span>
                </div>
                <div class="col-axis">
                  <span class="year-dot"></span>
                </div>
                <div class="col-summary">
                  <span class="year-count">{{ year.count }} 篇文章</span>
                  <svg class="year-arrow"
                       :class="{ 'is-closed': !isYearOpen(year.key) }"
                       viewBox="0 0 24 24" width="16" height="16"
                       fill="none" stroke="currentColor" stroke-width="2"
                       stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M6 9l6 6 6-6"></path>
                  </svg>
                </div>
              </button>

              <div v-show="isYearOpen(year.key)" class="year-body">
                <router-link v-for="article in year.articles"
                             :key="article.id"
                             class="archive-row"
                             :to="`/article/${article.id}`">
                  <div class="col-year">
                    <span class="row-date">{{ formatMonthDay(article.createTime) }}</span>
                  </div>
                  <div class="col-axis">
                    <span class="dash-line"></span>
                    <span class="row-dot"></span>
                  </div>
                  <div class="col-title">
                    <span v-if="getSortName(article)" class="cat-chip">{{ getSortName(article) }}</span>
                    <span class="row-title">{{ article.articleTitle }}</span>
                  </div>
                  <div class="col-tags">
                    <span class="row-tag"
                          v-for="label in getLabelNames(article)"
                          :key="label">#{{ label }}</span>
                  </div>
                </router-link>
              </div>
            </div>
          </template>
        </section>
      </main>

      <!-- 右栏：按参考站归档页右栏实际模块复刻 -->
      <aside class="archive-side">
        <ArchiveSidebar :articles="articles"></ArchiveSidebar>
      </aside>
    </div>

    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import MyAside from '../components/common/MyAside.vue'
  import ArchiveSidebar from '../components/common/ArchiveSidebar.vue'
  import MyFooter from '../components/common/MyFooter.vue'
  import { fetchAllArticles } from '../utils/article'

  export default {
    name: 'ArchiveView',
    components: {
      MyAside,
      ArchiveSidebar,
      MyFooter
    },
    data() {
      return {
        loading: true,
        error: '',
        articles: [],
        openYears: {}
      }
    },
    computed: {
      // 与参考站同形态：/archive?tag=标签名，重复参数时取并集
      tagNames() {
        const tag = this.$route.query.tag
        if (tag === undefined || tag === null || tag === '') {
          return []
        }
        return (Array.isArray(tag) ? tag : [tag]).filter((name) => name !== '')
      },
      // 分类页整卡跳转：/archive?category=分类名
      categoryName() {
        const category = this.$route.query.category
        if (category === undefined || category === null || category === '') {
          return ''
        }
        return Array.isArray(category) ? (category[0] || '') : String(category)
      },
      filteredArticles() {
        let list = this.articles.slice()
        if (this.categoryName) {
          list = list.filter((article) => this.getSortName(article) === this.categoryName)
        }
        if (this.tagNames.length) {
          list = list.filter((article) => {
            const names = this.getLabelNames(article)
            return this.tagNames.some((name) => names.includes(name))
          })
        }
        return list.sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
      },
      yearGroups() {
        const years = new Map()

        this.filteredArticles.forEach((article) => {
          const year = String(article.createTime || '').split('-')[0]
          if (!year) {
            return
          }
          if (!years.has(year)) {
            years.set(year, { key: year, count: 0, articles: [] })
          }
          const group = years.get(year)
          group.articles.push(article)
          group.count += 1
        })

        return Array.from(years.values()).sort((a, b) => b.key.localeCompare(a.key))
      }
    },
    watch: {
      // 年份折叠状态跟随筛选结果：默认只展开第一个年份（与参考站一致）
      yearGroups: {
        immediate: true,
        handler(groups) {
          const next = {}
          groups.forEach((group, index) => {
            next[group.key] = index === 0
          })
          this.openYears = next
        }
      }
    },
    created() {
      this.loadArticles()
    },
    methods: {
      loadArticles() {
        this.loading = true
        this.error = ''
        fetchAllArticles(this.$http, this.$constant)
          .then((articles) => {
            this.articles = articles
          })
          .catch((error) => {
            this.error = error.message || '归档加载失败'
          })
          .finally(() => {
            this.loading = false
          })
      },
      getLabelNames(article) {
        if (Array.isArray(article.labels) && article.labels.length) {
          return article.labels.map((label) => label.labelName).filter(Boolean)
        }
        return article.label && article.label.labelName ? [article.label.labelName] : []
      },
      getSortName(article) {
        return article.sort ? article.sort.sortName : ''
      },
      isYearOpen(year) {
        return this.openYears[year] === true
      },
      toggleYear(year) {
        this.openYears = {
          ...this.openYears,
          [year]: !this.isYearOpen(year)
        }
      },
      // 参考站条目只显示 MM-DD
      formatMonthDay(createTime) {
        const date = String(createTime || '').split(' ')[0]
        const parts = date.split('-')
        return parts.length >= 3 ? `${parts[1]}-${parts[2]}` : date
      },
      selectSort(sort) {
        this.$router.push({path: '/archive', query: {category: sort.sortName}})
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: '/'})
        }
      }
    }
  }
</script>

<style scoped>
  .archive-page {
    /* 参考站非首页不显示横幅，内容从顶栏下方 5.5rem 处开始 */
    /* 参考站栅格：92vw（上限 112rem），<768 单列；768–1279 左+主；≥1280 三栏 280/1fr/280 */
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--ui-page-gap);
    align-items: start;
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top) var(--ui-page-gutter) var(--ui-page-bottom);
    background: var(--ui-page-bg);
    /* 参考站的文字层级：75% / 50% / 30% */
    --arch-75: rgba(0, 0, 0, 0.75);
    --arch-50: rgba(0, 0, 0, 0.5);
    --arch-30: rgba(0, 0, 0, 0.3);
    --arch-line: rgba(0, 0, 0, 0.1);
    --arch-hover: rgba(0, 0, 0, 0.03);
    --arch-chip-border: rgba(120, 120, 120, 0.17);
  }

  html.dark .archive-page {
    --arch-75: rgba(255, 255, 255, 0.75);
    --arch-50: rgba(255, 255, 255, 0.5);
    --arch-30: rgba(255, 255, 255, 0.3);
    --arch-line: rgba(255, 255, 255, 0.12);
    --arch-hover: rgba(255, 255, 255, 0.06);
    --arch-chip-border: rgba(224, 224, 224, 0.22);
  }

  .archive-aside {
    display: none;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    align-content: start;
    min-width: 0;
  }

  .archive-side {
    display: none;
    min-width: 0;
  }

  .archive-main {
    min-width: 0;
  }

  /* 卡片：24px 上下 / 32px 左右内边距，16px 圆角，1px 描边，无阴影 */
  .archive-card {
    padding: var(--ui-card-padding-y) var(--ui-card-padding-x);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  .sr-only {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
  }

  /* 顶部筛选摘要：14px 面包屑 + 右侧 12px 计数，下方 20px 间距 */
  .archive-breadcrumb {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 20px;
  }

  .crumb-left {
    display: flex;
    align-items: center;
    min-width: 0;
    font-size: 14px;
    line-height: 20px;
  }

  .crumb-root {
    flex: none;
    color: var(--arch-50);
    text-decoration: none;
  }

  .crumb-root:hover {
    color: var(--ui-accent);
  }

  .crumb-sep {
    margin: 0 8px;
    color: var(--arch-30);
  }

  .crumb-tag {
    overflow: hidden;
    color: var(--ui-accent);
    font-weight: 500;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-decoration: none;
  }

  .crumb-tag:hover {
    opacity: 0.7;
  }

  .crumb-count {
    flex: none;
    font-size: 12px;
    line-height: 16px;
    color: var(--arch-50);
  }

  .archive-state {
    padding: 40px 0;
    text-align: center;
    font-size: 14px;
    color: var(--arch-50);
  }

  .state-tip {
    margin-top: 6px;
    font-size: 13px;
    opacity: 0.8;
  }

  /* 年份行：60px 高、8px 圆角，10% / 10% / 80% 三列 */
  .year-head {
    display: flex;
    align-items: center;
    width: 100%;
    height: 60px;
    padding: 0;
    border: 0;
    border-radius: 8px;
    background: transparent;
    font-family: inherit;
    text-align: left;
    cursor: pointer;
    transition: background 0.2s ease;
  }

  .year-head:hover {
    background: var(--arch-hover);
  }

  .col-year {
    width: 10%;
    flex: none;
    text-align: right;
  }

  .col-axis {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    align-self: stretch;
    width: 10%;
    flex: none;
  }

  .col-summary {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    width: 80%;
    min-width: 0;
    padding-right: 4px;
  }

  .year-num {
    font-size: 24px;
    font-weight: 700;
    line-height: 32px;
    color: var(--arch-75);
    transition: color 0.2s ease;
  }

  .year-head:hover .year-num {
    color: var(--ui-accent);
  }

  /* 年份节点：12px 圆环（3px 主色内描边） */
  .year-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: var(--cardBg);
    box-shadow: inset 0 0 0 3px var(--ui-accent);
    transition: background 0.2s ease;
  }

  .year-head:hover .year-dot {
    background: var(--ui-accent);
  }

  .year-count {
    font-size: 16px;
    line-height: 24px;
    color: var(--arch-50);
  }

  .year-arrow {
    flex: none;
    color: var(--arch-50);
    transition: transform 0.2s ease;
  }

  .year-arrow.is-closed {
    transform: rotate(-90deg);
  }

  /* 文章行：40px 高，标题 65%、右侧标签 15% */
  .archive-row {
    display: flex;
    align-items: center;
    height: 40px;
    border-radius: 8px;
    color: var(--arch-75);
    text-decoration: none;
  }

  .row-date {
    font-size: 14px;
    line-height: 20px;
    color: var(--arch-50);
    transition: color 0.2s ease;
  }

  .archive-row:hover .row-date {
    color: var(--ui-accent);
  }

  /* 时间轴虚线：2px dashed，贯穿整行 */
  .dash-line {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 50%;
    border-left: 2px dashed var(--arch-line);
    transform: translateX(-1px);
  }

  /* 文章节点：4px 主色圆点，外圈用卡片底色垫出 4px 留白 */
  .row-dot {
    position: relative;
    z-index: 1;
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: var(--ui-accent);
    box-shadow: 0 0 0 4px var(--cardBg);
    transition: transform 0.2s ease;
  }

  .archive-row:hover .row-dot {
    transform: scale(1.5);
  }

  .col-title {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 65%;
    min-width: 0;
    padding-right: 32px;
  }

  .cat-chip {
    flex: none;
    padding: 2px 6px;
    border: 1px solid var(--arch-chip-border);
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--ui-accent);
  }

  .row-title {
    overflow: hidden;
    font-size: 16px;
    font-weight: 700;
    line-height: 24px;
    color: var(--arch-75);
    text-overflow: ellipsis;
    white-space: nowrap;
    transition: color 0.2s ease, transform 0.2s ease;
  }

  .archive-row:hover .row-title {
    color: var(--ui-accent);
    transform: translateX(1px);
  }

  .col-tags {
    display: flex;
    gap: 4px;
    width: 15%;
    min-width: 0;
    overflow: hidden;
    font-size: 14px;
    line-height: 20px;
    color: var(--arch-30);
    white-space: nowrap;
  }

  .row-tag {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* 768 起：左栏 + 主栏（参考站 md 断点） */
  @media (min-width: 768px) {
    .archive-page {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr);
    }

    .archive-aside {
      display: grid;
    }
  }

  /* 1280 起：左 280 / 主栏自适应 / 右 280（参考站 xl 断点） */
  @media (min-width: 1280px) {
    .archive-page {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
    }

    .archive-side {
      display: grid;
    }
  }

  /* 参考站在 md 以下隐藏右侧标签列 */
  @media (max-width: 767px) {
    .archive-card {
      padding: 18px 16px;
    }

    .col-tags {
      display: none;
    }

    .col-title {
      width: 80%;
      padding-right: 8px;
    }

    .row-title {
      font-size: 15px;
    }

    .col-summary {
      padding-right: 0;
    }
  }
</style>
