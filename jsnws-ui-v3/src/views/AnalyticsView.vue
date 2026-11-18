<template>
  <div>
    <!-- 诗句横幅：与关于我、友链页共用同一组件，背景图取图规则一致 -->
    <div class="my-animation-slide-top">
      <TwoPoem :isHitokoto="false"></TwoPoem>
    </div>

    <!-- 两栏：左侧复用其它带侧栏页面的卡片栏，右侧为统计面板 -->
    <div class="analytics-container my-animation-slide-bottom">
      <!-- 左栏：日历按参考站放在右栏，这里关闭同款日历卡 -->
      <aside class="analytics-aside">
        <MyAside hide-calendar @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
      </aside>

      <main class="analytics-main">
        <AnalyticsPanel></AnalyticsPanel>
      </main>

      <!-- 右栏：与归档页同一套，日历卡按参考站放这里 -->
      <aside class="analytics-side">
        <ArchiveSidebar></ArchiveSidebar>
      </aside>
    </div>

    <!-- 页脚 -->
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
  import AnalyticsPanel from "../components/about/AnalyticsPanel.vue";

  export default {
    name: "AnalyticsView",
    components: {
      TwoPoem,
      MyFooter,
      MyAside,
      ArchiveSidebar,
      AnalyticsPanel
    },
    methods: {
      selectSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: "/"});
        }
      }
    }
  }
</script>

<style scoped>
  /* 与关于页同一套两栏：280px 侧栏 + 自适应主栏，16px 间距，容器同宽同留白 */
  .analytics-container {
    display: grid;
    grid-template-columns: var(--ui-aside-width) minmax(0, 1fr);
    gap: var(--ui-page-gap);
    align-items: start;
    width: min(var(--ui-page-inline), var(--ui-page-width));
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    background: var(--ui-page-bg);
  }

  .analytics-aside {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--ui-page-gap);
    align-content: start;
    min-width: 0;
  }

  .analytics-main {
    min-width: 0;
  }

  /* 右栏默认隐藏，与归档页同一断点：≥1280 才出现第三列 */
  .analytics-side {
    display: none;
    min-width: 0;
  }

  @media (min-width: 1280px) {
    .analytics-container {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
    }

    .analytics-side {
      display: block;
    }
  }

  /* 面板自带整页宽度（min(92vw, 112rem)），放进右栏后改为占满栏宽，避免横向溢出 */
  .analytics-main :deep(.analytics-page) {
    width: 100%;
    max-width: 100%;
  }

  @media (max-width: 1000px) {
    .analytics-container {
      grid-template-columns: minmax(0, 1fr);
    }

    /* 与关于页一致：窄屏主内容在前，卡片栏落到下方 */
    .analytics-aside {
      order: 2;
    }

    .analytics-main {
      order: 1;
    }
  }

  @media (max-width: 767px) {
    .analytics-container {
      width: 100%;
      max-width: 100%;
      padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    }
  }
</style>
