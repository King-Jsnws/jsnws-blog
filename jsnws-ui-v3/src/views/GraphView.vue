<template>
  <div>
    <div class="graph-page">
      <div class="graph-head">
        <h1>文章图谱</h1>
        <p v-if="!loading && !error && groups.length > 0">
          共 {{ articleCount }} 篇文章，关联 {{ groups.length }} 个分类
        </p>
      </div>

      <div class="graph-state shadow-box" v-if="loading">
        正在加载图谱数据…
      </div>

      <div class="graph-state shadow-box" v-else-if="error">
        <p>{{ error }}</p>
        <p class="state-tip">请检查网络或稍后重试</p>
      </div>

      <div class="graph-state shadow-box" v-else-if="groups.length === 0">
        <el-icon class="state-icon"><Share /></el-icon>
        <p>暂无可视化的文章与分类关系</p>
        <p class="state-tip">站点有文章和分类后，这里会画出它们的关联</p>
      </div>

      <div class="graph-grid" v-else>
        <div class="graph-card shadow-box" v-for="group in groups" :key="group.id">
          <div class="graph-card-head">
            <span class="group-name">{{ group.name }}</span>
            <span class="group-count">{{ group.total }} 篇</span>
          </div>

          <svg class="graph-svg" viewBox="0 0 320 240" role="img"
               :aria-label="group.name + ' 的文章关系图'">
            <line v-for="node in group.nodes"
                  :key="'line-' + node.id"
                  :x1="group.centerX"
                  :y1="group.centerY"
                  :x2="node.x"
                  :y2="node.y"
                  stroke="#d8e3f0"
                  stroke-width="1.2" />
            <circle :cx="group.centerX" :cy="group.centerY" r="36" :fill="group.color" />
            <text :x="group.centerX" :y="group.centerY - 2" text-anchor="middle"
                  fill="#ffffff" font-size="12" font-weight="600">
              {{ group.shortName }}
            </text>
            <text :x="group.centerX" :y="group.centerY + 14" text-anchor="middle"
                  fill="#ffffff" font-size="10" opacity="0.9">
              {{ group.total }} 篇
            </text>
            <g v-for="node in group.nodes" :key="node.id" class="graph-node"
               @click="openArticle(node)">
              <circle :cx="node.x" :cy="node.y" r="7" :fill="group.color" opacity="0.9" />
              <circle :cx="node.x" :cy="node.y" r="11" fill="transparent" />
              <title>{{ node.title }}</title>
            </g>
          </svg>

          <p class="graph-note" v-if="group.total > group.nodes.length">
            显示前 {{ group.nodes.length }} 篇，共 {{ group.total }} 篇
          </p>
        </div>
      </div>
    </div>

    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import { Share } from '@element-plus/icons-vue'
  import MyFooter from '../components/common/MyFooter.vue'
  import { fetchAllArticles } from '../utils/article'

  const MAX_NODES = 10
  const COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#8e79c9', '#4fb3bf']

  export default {
    name: 'GraphView',
    components: {
      MyFooter,
      Share
    },
    data() {
      return {
        loading: true,
        error: '',
        sorts: [],
        articles: []
      }
    },
    computed: {
      articleCount() {
        return this.articles.length
      },
      groups() {
        if (this.sorts.length === 0 || this.articles.length === 0) {
          return []
        }

        const groups = []
        this.sorts.forEach((sort, sortIndex) => {
          const articles = this.articles.filter((article) => {
            return Number(article.sortId) === Number(sort.id)
          })
          if (articles.length === 0) {
            return
          }
          groups.push(this.buildGroup(sort, articles, sortIndex))
        })
        return groups
      }
    },
    created() {
      this.loadData()
    },
    methods: {
      loadData() {
        this.loading = true
        this.error = ''
        Promise.all([this.loadSorts(), fetchAllArticles(this.$http, this.$constant)])
          .then(([sorts, articles]) => {
            this.sorts = sorts
            this.articles = articles
          })
          .catch((error) => {
            this.error = error.message || '图谱数据加载失败'
          })
          .finally(() => {
            this.loading = false
          })
      },
      loadSorts() {
        const sortInfo = this.$store.state.sortInfo
        if (!this.$common.isEmpty(sortInfo)) {
          return Promise.resolve(sortInfo)
        }
        return this.$http.get(this.$constant.baseURL + '/webInfo/getSortInfo')
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.$store.commit('loadSortInfo', res.data)
              return this.$store.state.sortInfo
            }
            return []
          })
      },
      buildGroup(sort, articles, sortIndex) {
        const color = COLORS[sortIndex % COLORS.length]
        const centerX = 160
        const centerY = 120
        const radius = 82
        const visible = articles.slice(0, MAX_NODES)
        const nodes = visible.map((article, index) => {
          const angle = -Math.PI / 2 + (2 * Math.PI * index) / visible.length
          return {
            id: article.id,
            title: article.articleTitle,
            x: Number((centerX + Math.cos(angle) * radius).toFixed(1)),
            y: Number((centerY + Math.sin(angle) * radius).toFixed(1))
          }
        })

        return {
          id: sort.id,
          name: sort.sortName,
          shortName: sort.sortName.length > 5 ? sort.sortName.slice(0, 5) : sort.sortName,
          total: articles.length,
          color,
          centerX,
          centerY,
          nodes
        }
      },
      openArticle(node) {
        this.$router.push({path: `/article/${node.id}`})
      }
    }
  }
</script>

<style scoped>
  .graph-page {
    min-height: calc(100vh - 60px);
    padding: 120px 20px 60px;
    background: var(--background);
  }

  .graph-head {
    max-width: 980px;
    margin: 0 auto 28px;
  }

  .graph-head h1 {
    margin: 0 0 8px;
    font-size: 26px;
    letter-spacing: 2px;
    color: var(--fontColor);
  }

  .graph-head p {
    margin: 0;
    font-size: 14px;
    color: var(--greyFontColor);
  }

  .graph-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    max-width: 980px;
    margin: 0 auto;
  }

  .graph-card {
    padding: 16px 18px 12px;
    border-radius: 14px;
  }

  .graph-card-head {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
  }

  .group-name {
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 1px;
    color: var(--fontColor);
  }

  .group-count {
    margin-left: 8px;
    padding: 0 8px;
    font-size: 12px;
    line-height: 18px;
    border-radius: 9px;
    color: #409eff;
    background: rgba(64, 158, 255, 0.12);
  }

  .graph-svg {
    display: block;
    width: 100%;
    height: auto;
  }

  .graph-node {
    cursor: pointer;
  }

  .graph-node circle {
    transition: opacity 0.18s ease;
  }

  .graph-node:hover circle:first-child {
    opacity: 1;
  }

  .graph-note {
    margin: 0;
    font-size: 12px;
    text-align: center;
    color: var(--greyFontColor);
  }

  .graph-state {
    max-width: 980px;
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
