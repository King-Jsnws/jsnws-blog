<template>
  <div>
    <div class="archive-page">
      <div class="archive-head">
        <h1>文章归档</h1>
        <p v-if="!loading && !error && articles.length > 0">
          共 {{ articles.length }} 篇文章，横跨 {{ yearGroups.length }} 个年份
        </p>
      </div>

      <div class="archive-state shadow-box" v-if="loading">
        正在加载归档…
      </div>

      <div class="archive-state shadow-box" v-else-if="error">
        <p>{{ error }}</p>
        <p class="state-tip">请检查网络或稍后重试</p>
      </div>

      <div class="archive-state shadow-box" v-else-if="yearGroups.length === 0">
        <el-icon class="state-icon"><Box /></el-icon>
        <p>暂无归档文章</p>
        <p class="state-tip">站点发布文章后，这里会按年份和月份自动归拢</p>
      </div>

      <div class="archive-list" v-else>
        <div v-for="year in yearGroups" :key="year.key" class="archive-year shadow-box">
          <div class="archive-year-head" @click="toggleYear(year.key)">
            <span class="year-name">{{ year.key }}</span>
            <span class="year-count">{{ year.count }} 篇</span>
            <el-icon class="year-arrow" :class="{ 'is-open': isYearOpen(year.key) }">
              <ArrowDown />
            </el-icon>
          </div>

          <div class="archive-months" v-show="isYearOpen(year.key)">
            <div v-for="month in year.months" :key="month.key" class="archive-month">
              <div class="archive-month-head">
                <span class="month-name">{{ month.key }} 月</span>
                <span class="month-count">{{ month.articles.length }}</span>
              </div>
              <ul class="archive-articles">
                <li v-for="article in month.articles"
                    :key="article.id"
                    @click="openArticle(article)">
                  <span class="article-day">{{ formatDay(article.createTime) }}</span>
                  <span class="article-title">{{ article.articleTitle }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import { ArrowDown, Box } from '@element-plus/icons-vue'
  import MyFooter from '../components/common/MyFooter.vue'
  import { fetchAllArticles } from '../utils/article'

  export default {
    name: 'ArchiveView',
    components: {
      ArrowDown,
      Box,
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
      yearGroups() {
        const years = new Map()

        this.articles.forEach((article) => {
          const date = String(article.createTime || '').split(' ')[0]
          const parts = date.split('-')
          if (parts.length < 2) {
            return
          }
          const year = parts[0]
          const month = parts[1]
          if (!years.has(year)) {
            years.set(year, { key: year, count: 0, months: new Map() })
          }
          const yearGroup = years.get(year)
          if (!yearGroup.months.has(month)) {
            yearGroup.months.set(month, { key: month, articles: [] })
          }
          yearGroup.months.get(month).articles.push(article)
          yearGroup.count += 1
        })

        return Array.from(years.values())
          .sort((a, b) => b.key.localeCompare(a.key))
          .map((yearGroup) => ({
            key: yearGroup.key,
            count: yearGroup.count,
            months: Array.from(yearGroup.months.values())
              .sort((a, b) => b.key.localeCompare(a.key))
              .map((month) => ({
                key: month.key,
                articles: month.articles.slice().sort((a, b) => {
                  return String(b.createTime || '').localeCompare(String(a.createTime || ''))
                })
              }))
          }))
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
            this.openYears = articles.reduce((result, article) => {
              const year = String(article.createTime || '').split('-')[0]
              if (year) {
                result[year] = true
              }
              return result
            }, {})
          })
          .catch((error) => {
            this.error = error.message || '归档加载失败'
          })
          .finally(() => {
            this.loading = false
          })
      },
      isYearOpen(year) {
        return this.openYears[year] !== false
      },
      toggleYear(year) {
        this.openYears = {
          ...this.openYears,
          [year]: !this.isYearOpen(year)
        }
      },
      formatDay(createTime) {
        const date = String(createTime || '').split(' ')[0]
        const parts = date.split('-')
        return parts.length >= 3 ? parts[2] : date
      },
      openArticle(article) {
        this.$router.push({path: `/article/${article.id}`})
      }
    }
  }
</script>

<style scoped>
  .archive-page {
    min-height: calc(100vh - 60px);
    padding: 120px 20px 60px;
    background: var(--background);
  }

  .archive-head {
    max-width: 760px;
    margin: 0 auto 28px;
  }

  .archive-head h1 {
    margin: 0 0 8px;
    font-size: 26px;
    letter-spacing: 2px;
    color: var(--fontColor);
  }

  .archive-head p {
    margin: 0;
    font-size: 14px;
    color: var(--greyFontColor);
  }

  .archive-list {
    max-width: 760px;
    margin: 0 auto;
  }

  .archive-year {
    margin-bottom: 18px;
    padding: 6px 22px 10px;
    border-radius: 14px;
  }

  .archive-year-head {
    display: flex;
    align-items: center;
    height: 54px;
    cursor: pointer;
    user-select: none;
  }

  .year-name {
    font-size: 20px;
    font-weight: 600;
    letter-spacing: 2px;
    color: var(--fontColor);
  }

  .year-count {
    margin-left: 12px;
    font-size: 13px;
    color: var(--greyFontColor);
  }

  .year-arrow {
    margin-left: auto;
    font-size: 14px;
    color: var(--greyFontColor);
    transition: transform 0.25s ease;
  }

  .year-arrow.is-open {
    transform: rotate(180deg);
  }

  .archive-months {
    padding-bottom: 6px;
  }

  .archive-month {
    padding: 8px 0 12px 14px;
    border-left: 2px solid rgba(64, 158, 255, 0.35);
  }

  .archive-month-head {
    display: flex;
    align-items: center;
    margin-bottom: 6px;
  }

  .month-name {
    font-size: 15px;
    font-weight: 600;
    color: var(--fontColor);
  }

  .month-count {
    margin-left: 8px;
    padding: 0 7px;
    font-size: 12px;
    line-height: 18px;
    border-radius: 9px;
    color: #409eff;
    background: rgba(64, 158, 255, 0.12);
  }

  .archive-articles {
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .archive-articles li {
    display: flex;
    align-items: center;
    padding: 7px 8px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.18s ease;
  }

  .archive-articles li:hover {
    background: rgba(64, 158, 255, 0.08);
  }

  .archive-articles li:hover .article-title {
    color: #409eff;
  }

  .article-day {
    width: 34px;
    flex: none;
    font-size: 13px;
    color: var(--greyFontColor);
  }

  .article-title {
    overflow: hidden;
    font-size: 14px;
    color: var(--fontColor);
    text-overflow: ellipsis;
    white-space: nowrap;
    transition: color 0.18s ease;
  }

  .archive-state {
    max-width: 760px;
    margin: 0 auto;
    padding: 70px 20px;
    border-radius: 14px;
    text-align: center;
    color: var(--greyFontColor);
  }

  .state-icon {
    font-size: 34px;
    color: rgba(64, 158, 255, 0.6);
  }

  .state-tip {
    margin-top: 6px;
    font-size: 13px;
    opacity: 0.75;
  }
</style>
