<template>
  <div>
    <!-- 两句诗：与关于页、友链页共用同一组件 -->
    <div class="my-animation-slide-top">
      <TwoPoem :isHitokoto="false"></TwoPoem>
    </div>

    <div class="tag-page-container my-animation-slide-bottom">
      <!-- 左侧：其它带侧栏页面同一套卡片栏；日历按参考站放在右栏 -->
      <aside class="tag-page-aside">
        <MyAside hide-calendar @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
      </aside>

      <main class="tag-page-main">
        <!-- 标题卡：与参考站一致，仅标题 + 一行数量说明 -->
        <header class="tag-head-card">
          <h1 class="tag-head-title">标签</h1>
          <p class="tag-head-sub">全部标签 · {{ tags.length }} 个标签</p>
        </header>

        <!-- 标签面板：纯标签流，不额外增加统计条或说明 -->
        <section class="tag-panel">
          <div v-if="tags.length" class="tag-list">
            <button v-for="tag in tags"
                    :key="'tag-' + tag.sortId + '-' + tag.id"
                    type="button"
                    class="tag-item"
                    @click="openTag(tag)">
              <span class="tag-item-name"><span class="tag-item-hash">#</span>{{ tag.labelName }}</span>
              <span class="tag-count-badge">{{ tag.countOfLabel || 0 }}</span>
            </button>
          </div>

          <p v-else-if="!loaded" class="tag-empty">标签加载中…</p>
          <p v-else-if="loadError" class="tag-empty">标签加载失败，请稍后刷新重试。</p>
          <p v-else class="tag-empty">还没有标签。</p>
        </section>
      </main>

      <!-- 右栏：与归档页同一套，日历卡按参考站放这里 -->
      <aside class="tag-page-side">
        <ArchiveSidebar></ArchiveSidebar>
      </aside>
    </div>

    <div style="background: var(--favoriteBg)">
      <MyFooter></MyFooter>
    </div>
  </div>
</template>

<script>
  import TwoPoem from "../components/common/TwoPoem.vue";
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import ArchiveSidebar from "../components/common/ArchiveSidebar.vue";

  export default {
    components: {
      TwoPoem,
      MyFooter,
      MyAside,
      ArchiveSidebar
    },
    data() {
      return {
        loaded: false,
        loadError: false
      };
    },
    computed: {
      sortInfo() {
        return this.$store.state.sortInfo || [];
      },
      // 扁平化真实标签，按标签名升序（与参考站一致）
      tags() {
        const list = [];
        this.sortInfo.forEach((sort) => {
          (sort.labels || []).forEach((label) => {
            list.push({
              ...label,
              sortId: sort.id,
              sortName: sort.sortName
            });
          });
        });
        return list.sort((a, b) => String(a.labelName)
          .localeCompare(String(b.labelName), "zh-CN", {sensitivity: "base"}));
      }
    },
    created() {
      this.loadSortInfo();
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
      // 与参考站一致：跳到归档页并带 tag 参数（参数值用标签名，而非 id）
      openTag(tag) {
        this.$router.push({
          path: "/archive",
          query: {tag: tag.labelName}
        });
      },
      selectSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: "/"});
        }
      }
    }
  };
</script>

<style scoped>
  /* 与参考站同一套栅格：280px 侧栏 + 自适应主栏，16px 间距，最大 112rem */
  .tag-page-container {
    display: grid;
    grid-template-columns: var(--ui-aside-width) minmax(0, 1fr);
    gap: var(--ui-page-gap);
    align-items: start;
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    background: var(--ui-page-bg);
  }

  .tag-page-aside {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    align-content: start;
    min-width: 0;
  }

  .tag-page-main {
    min-width: 0;
  }

  /* 右栏默认隐藏，与归档页同一断点：≥1280 才出现第三列 */
  .tag-page-side {
    display: none;
    min-width: 0;
  }

  @media (min-width: 1280px) {
    .tag-page-container {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
    }

    .tag-page-side {
      display: block;
    }
  }

  /* 标题卡：24px 标题 + 14px 数量说明，8px 下间距，卡片间距 16px */
  .tag-head-card {
    padding: var(--ui-card-padding-y) var(--ui-card-padding-x);
    margin-bottom: var(--ui-page-gap);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  .tag-head-title {
    margin: 0 0 8px;
    font-size: 24px;
    font-weight: 700;
    line-height: 1.2;
    color: var(--ui-accent);
  }

  .tag-head-sub {
    margin: 0;
    font-size: 14px;
    line-height: 20px;
    color: rgba(0, 0, 0, 0.3);
  }

  html.dark .tag-head-sub {
    color: rgba(255, 255, 255, 0.3);
  }

  /* 标签面板：24px 上下、36px 左右内边距 */
  .tag-panel {
    min-height: 128px;
    padding: var(--ui-card-padding-y) var(--ui-card-padding-x);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  /* 标签流：10px 间距，与参考站一致 */
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  /* 标签项：高 32px、左右 12px、圆角 8px、字号 14px，透明底主色文字 */
  .tag-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 32px;
    padding: 0 12px;
    border: 1px solid rgba(120, 120, 120, 0.17);
    border-radius: 8px;
    background: transparent;
    color: var(--ui-accent);
    font-family: inherit;
    font-size: 14px;
    line-height: 1;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }

  html.dark .tag-item {
    border-color: rgba(224, 224, 224, 0.22);
  }

  /* 悬停：放大 1.05、出现阴影、描边变主色 */
  .tag-item:hover {
    border-color: var(--ui-accent);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
    transform: scale(1.05);
  }

  .tag-item:focus-visible {
    outline: 2px solid var(--ui-accent);
    outline-offset: 2px;
  }

  .tag-item-name {
    font-weight: 500;
    white-space: nowrap;
  }

  .tag-item-hash {
    opacity: 0.5;
  }

  /* 计数徽章：最宽 24px 起、左右 6px、圆角 6px、12px 粗体、透明底 */
  .tag-count-badge {
    display: block;
    min-width: 24px;
    padding: 0 6px;
    border-radius: 6px;
    background: transparent;
    font-size: 12px;
    font-weight: 700;
    line-height: 16px;
    text-align: center;
    color: var(--ui-accent);
  }

  .tag-empty {
    margin: 0;
    padding: 16px 0;
    font-size: 14px;
    line-height: 20px;
    color: rgba(0, 0, 0, 0.3);
  }

  html.dark .tag-empty {
    color: rgba(255, 255, 255, 0.3);
  }

  @media screen and (max-width: 767px) {
    .tag-page-container {
      grid-template-columns: minmax(0, 1fr);
    }

    /* 窄屏主内容在前，卡片栏落到下方 */
    .tag-page-aside {
      order: 2;
    }

    .tag-page-main {
      order: 1;
    }

    .tag-head-card,
    .tag-panel {
      padding: 18px 16px;
    }
  }
</style>
