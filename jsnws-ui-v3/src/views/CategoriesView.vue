<template>
  <div>
    <div class="categories-page my-animation-slide-bottom">
      <!-- 左栏：与归档页共用站点侧栏；日历按参考站放在右栏，这里关闭同款日历卡 -->
      <aside class="categories-aside">
        <MyAside hide-calendar
                 @selectSort="selectSort"
                 @selectArticle="selectArticle"></MyAside>
      </aside>

      <main class="categories-main">
        <!-- 页头卡：分类 + 全部分类文章总数（参考站实测 24/32 内边距，标题 24px 主色） -->
        <section class="cat-head-card">
          <div class="cat-head-title">分类</div>
          <p class="cat-head-sub">全部分类 · {{ articleTotal }} 篇文章</p>
        </section>

        <!-- 分类网格：<768 单列，768-1023 两列，≥1024 三列，间距 16px -->
        <div v-if="categories.length" class="category-grid">
          <router-link v-for="sort in categories"
                       :key="'category-' + sort.id"
                       class="category-card"
                       :to="{path: '/archive', query: {category: sort.sortName}}">
            <div class="category-icon">
              <svg viewBox="0 0 24 24" width="30" height="30" aria-hidden="true">
                <path fill="currentColor"
                      d="M4 20q-.825 0-1.412-.587T2 18V6q0-.825.588-1.412T4 4h6l2 2h8q.825 0 1.413.588T22 8H4v10l2.4-8h17.1l-2.575 8.575q-.2.65-.737 1.038T19 20z"/>
              </svg>
            </div>
            <div class="category-body">
              <h2 class="category-name" :title="sort.sortName">{{ sort.sortName }}</h2>
              <p class="category-count">{{ Number(sort.countOfSort) || 0 }} 篇文章</p>
            </div>
            <div class="category-chevron">
              <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path fill="currentColor"
                      d="M12.6 12L8.7 8.1q-.275-.275-.275-.7t.275-.7t.7-.275t.7.275l4.6 4.6q.15.15.213.325t.062.375t-.062.375t-.213.325l-4.6 4.6q-.275.275-.7.275t-.7-.275t-.275-.7t.275-.7z"/>
              </svg>
            </div>
          </router-link>
        </div>

        <p v-else-if="!loaded" class="catalog-state">分类加载中…</p>
        <p v-else-if="loadError" class="catalog-state">分类加载失败，请稍后刷新重试。</p>
        <p v-else class="catalog-state">还没有分类。</p>
      </main>

      <!-- 右栏：与归档页同一套模块，日历卡按参考站放这里 -->
      <aside class="categories-side">
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
    name: 'CategoriesView',
    components: {
      MyAside,
      ArchiveSidebar,
      MyFooter
    },
    data() {
      return {
        loaded: false,
        loadError: false,
        articles: []
      };
    },
    computed: {
      sortInfo() {
        return this.$store.state.sortInfo || [];
      },
      // 参考站顺序：文章数降序，数量相同按分类名升序
      categories() {
        return this.sortInfo.slice().sort((a, b) => {
          const diff = (Number(b.countOfSort) || 0) - (Number(a.countOfSort) || 0);
          if (diff !== 0) {
            return diff;
          }
          return String(a.sortName || '').localeCompare(String(b.sortName || ''), 'zh');
        });
      },
      articleTotal() {
        return this.categories.reduce((sum, sort) => sum + (Number(sort.countOfSort) || 0), 0);
      }
    },
    created() {
      this.loadSortInfo();
      this.loadArticles();
    },
    methods: {
      loadSortInfo() {
        if (this.sortInfo.length) {
          this.loaded = true;
          return;
        }
        this.$http.get(this.$constant.baseURL + "/webInfo/getSortInfo")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.$store.commit("loadSortInfo", res.data);
            }
            this.loaded = true;
          })
          .catch(() => {
            this.loadError = true;
            this.loaded = true;
          });
      },
      // 右栏统计 / 日历 / 贡献图需要全量文章（与归档页同一数据源）
      loadArticles() {
        fetchAllArticles(this.$http, this.$constant)
          .then((articles) => {
            this.articles = articles;
          })
          .catch(() => {
            this.articles = [];
          });
      },
      selectSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: '/'});
        }
      }
    }
  };
</script>

<style scoped>
  .categories-page {
    /* 参考站栅格：<768 单列；768–1279 左 280 + 主栏；≥1280 三栏 280/1fr/280 */
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--ui-page-gap);
    align-items: start;
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top) var(--ui-page-gutter) var(--ui-page-bottom);
    background: var(--ui-page-bg);
    /* 参考站文字层级：90% / 30% */
    --cat-90: rgba(0, 0, 0, 0.9);
    --cat-30: rgba(0, 0, 0, 0.3);
  }

  html.dark .categories-page {
    --cat-90: rgba(255, 255, 255, 0.9);
    --cat-30: rgba(255, 255, 255, 0.3);
  }

  .categories-aside,
  .categories-side {
    display: none;
    min-width: 0;
  }

  .categories-main {
    min-width: 0;
  }

  /* 页头卡：站内统一卡片外壳（底色 / 1px 描边 / 16px 圆角 / 1px 阴影） */
  .cat-head-card {
    margin-bottom: var(--ui-page-gap);
    padding: var(--ui-card-padding-y) var(--ui-card-padding-x);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  .cat-head-title {
    margin-bottom: 8px;
    font-size: 24px;
    font-weight: 700;
    line-height: 32px;
    color: var(--ui-accent);
  }

  .cat-head-sub {
    margin: 0;
    font-size: 14px;
    line-height: 20px;
    color: var(--cat-30);
  }

  .category-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    margin-bottom: 16px;
  }

  /* 卡片：站内统一外壳 + 56px 圆形图标 + 自适应文字列 + 24px 箭头，整卡可点 */
  .category-card {
    display: flex;
    align-items: center;
    gap: var(--ui-page-gap);
    padding: var(--ui-card-padding-y);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
    text-decoration: none;
    transition: border-color 0.22s, background-color 0.15s, box-shadow 0.22s;
  }

  .category-card:hover {
    box-shadow: var(--ui-card-shadow-hover);
  }

  .category-icon {
    display: flex;
    flex: none;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: color-mix(in srgb, var(--ui-accent) 10%, transparent);
    transition: background-color 0.15s;
  }

  .category-icon svg {
    color: var(--ui-accent);
  }

  .category-card:hover .category-icon {
    background: color-mix(in srgb, var(--ui-accent) 20%, transparent);
  }

  .category-body {
    flex: 1;
    min-width: 0;
  }

  .category-name {
    margin: 0;
    overflow: hidden;
    font-size: 18px;
    font-weight: 700;
    line-height: 28px;
    color: var(--cat-90);
    text-overflow: ellipsis;
    white-space: nowrap;
    transition: color 0.15s;
  }

  .category-card:hover .category-name {
    color: var(--ui-accent);
  }

  .category-count {
    margin: 4px 0 0;
    font-size: 14px;
    line-height: 20px;
    color: var(--cat-30);
    transition: color 0.15s;
  }

  .category-card:hover .category-count {
    color: color-mix(in srgb, var(--ui-accent) 60%, transparent);
  }

  .category-chevron {
    display: flex;
    flex: none;
    color: var(--cat-30);
    transition: color 0.15s;
  }

  .category-card:hover .category-chevron {
    color: var(--ui-accent);
  }

  .catalog-state {
    margin: 0;
    padding: 40px 0;
    font-size: 14px;
    line-height: 20px;
    text-align: center;
    color: var(--cat-30);
  }

  /* 768 起：左栏 + 主栏（参考站 md 断点） */
  @media (min-width: 768px) {
    .categories-page {
      grid-template-columns: 280px minmax(0, 1fr);
    }

    .categories-aside {
      display: block;
    }

    .category-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  /* 1024 起：分类卡固定三列（参考站 lg 断点） */
  @media (min-width: 1024px) {
    .category-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  /* 1280 起：左 280 / 主栏自适应 / 右 280，容器宽度 92vw（参考站 xl 断点） */
  @media (min-width: 1280px) {
    .categories-page {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
    }

    .categories-side {
      display: block;
    }
  }

  /* 参考站 md 以下根字号 14px：内边距 / 字号 / 图标同步缩放到 0.875 倍 */
  @media (max-width: 767px) {
    .cat-head-card {
      margin-bottom: var(--ui-page-gap);
      padding: 18px 16px;
      border-radius: var(--ui-card-radius);
    }

    .cat-head-title {
      margin-bottom: 7px;
      font-size: var(--ui-title-size);
      line-height: 28px;
    }

    .cat-head-sub {
      font-size: 12.25px;
      line-height: 17.5px;
    }

    .category-grid {
      gap: var(--ui-page-gap);
      margin-bottom: var(--ui-page-gap);
    }

    .category-card {
      gap: 14px;
      padding: 18px 16px;
      border-radius: var(--ui-card-radius);
    }

    .category-icon {
      width: 49px;
      height: 49px;
    }

    .category-icon svg {
      width: 26.25px;
      height: 26.25px;
    }

    .category-name {
      font-size: 15.75px;
      line-height: 24.5px;
    }

    .category-count {
      margin-top: 3.5px;
      font-size: 12.25px;
      line-height: 17.5px;
    }

    .category-chevron svg {
      width: 21px;
      height: 21px;
    }
  }
</style>
