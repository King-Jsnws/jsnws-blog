<template>
  <div :style="{ '--home-bg-zoom': backgroundZoom }">
    <Loader :loading="loading">
      <!-- 加载页面 -->
      <template #loader>
        <div>
          <Zombie></Zombie>
        </div>
      </template>
      <!-- 内容页面 -->
      <template #body>
        <!-- 首页图片 -->
        <el-image class="background-image-index my-animation-hero-fade"
                  v-once
                  lazy
                  :src="indexBackground"
                  fit="cover">
          <template #error>
            <div class="image-slot background-image-index-error"></div>
          </template>
        </el-image>
        <!-- 首页文字 -->
        <div class="signature-wall myCenter my-animation-slide-top">
          <h1 class="playful" v-if="titleEnabled">
            <span v-for="(a, index) in $store.state.webInfo.webTitle" :key="index">{{a}}</span>
          </h1>
          <div class="printer" v-if="printerEnabled" @click="getGuShi()">
            <Printer :printerInfo="printerInfo">
              <template #paper="scope">
                <h3>
                  {{ scope.content }}<span class="cursor">|</span>
                </h3>
              </template>
            </Printer>
          </div>
          <!-- 首屏底部水波：内联 SVG，不依赖任何图片资源，填充跟随主题页面底色 -->
          <div class="banner-waves" aria-hidden="true">
            <svg class="banner-waves-svg"
                 viewBox="0 24 150 28"
                 preserveAspectRatio="none"
                 shape-rendering="geometricPrecision">
              <defs>
                <path id="banner-wave-path"
                      d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18v48h-352z"/>
              </defs>
              <g class="banner-waves-parallax">
                <use href="#banner-wave-path" x="48" y="0"></use>
                <use href="#banner-wave-path" x="48" y="3"></use>
                <use href="#banner-wave-path" x="48" y="5"></use>
                <use href="#banner-wave-path" x="48" y="7"></use>
              </g>
            </svg>
          </div>
          <el-icon class="el-icon-arrow-down" @click="navigation('.page-container-wrap')">
            <ArrowDown />
          </el-icon>
        </div>
        <!-- 首页内容 -->
        <div class="page-container-wrap my-animation-slide-bottom">
          <div class="page-container">
            <div class="aside-content" v-if="showAside">
              <!-- 日历按参考站放在右栏，左栏关闭同款日历卡 -->
              <MyAside hide-calendar @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
            </div>
            <div class="recent-posts">
              <div class="announcement background-opacity">
                <i class="fa fa-volume-up" aria-hidden="true"></i>
                <div>
                  <div v-for="(notice, index) in $common.pushNotification($store.state.webInfo.notices, true)" :key="index">
                    {{ notice }}
                  </div>
                </div>
              </div>

              <div v-show="indexType === 1">
                <div v-for="(sort, index) in sortInfo" :key="index">
                  <div v-if="!$common.isEmpty(sortArticles[sort.id])">
                    <SortArticle :articleList="sortArticles[sort.id]"></SortArticle>
                  </div>
                </div>
              </div>

              <div v-show="indexType === 2">
                <ArticleList :articleList="articles"></ArticleList>
                <div class="pagination-wrap">
                  <div @click="pageArticles()" class="pagination" v-if="pagination.total !== articles.length">
                    下一页
                  </div>
                  <div v-else style="user-select: none">
                    ~~到底啦~~
                  </div>
                </div>
              </div>
            </div>

            <!-- 右栏：与归档页同一套模块，日历卡按参考站放这里 -->
            <aside v-if="showAside" class="index-side">
              <ArchiveSidebar :articles="articles"></ArchiveSidebar>
            </aside>
          </div>
        </div>
        <!-- 页脚 -->
        <div style="background: var(--background)">
          <MyFooter></MyFooter>
        </div>
      </template>
    </Loader>

    <el-dialog title="每日推荐"
               v-model="pushDialogVisible"
               width="60%"
               :append-to-body="true"
               :close-on-click-modal="false"
               modal-class="index-push"
               center>
      <div style="display: flex;align-items: center;flex-direction: column">
        <div class="push-title">
          {{ push['标题'] }}
        </div>

        <el-image class="push-el-image"
                  lazy
                  :src="push['封面']"
                  fit="cover">
        </el-image>

        <div class="push-button" @click="pushUrl(push['链接'])">
          <span class="push-button-title">立即前往</span>
          <span class="push-button-car">
            <svg viewBox="0 0 1024 1024" width="40" height="40">
                <path
                  d="M399.502 655.103c0 7.902-6.665 14.311-14.88 14.311H72.188c-8.215 0-14.875-6.407-14.875-14.311v-28.634c0-7.913 6.66-14.315 14.875-14.315h312.435c8.217 0 14.88 6.402 14.88 14.315l-0.001 28.634zM968.167 655.103c0 7.902-6.664 14.311-14.882 14.311H640.851c-8.216 0-14.877-6.407-14.877-14.311v-28.634c0-7.913 6.661-14.315 14.877-14.315h312.436c8.218 0 14.882 6.402 14.882 14.315l-0.002 28.634z"
                  fill="#EA0606"></path><path
              d="M968.097 624.008c0 11.563-17.723 20.937-39.583 20.937H97.263c-21.858 0-39.579-9.372-39.579-20.937v-41.876c0-11.562 17.72-20.935 39.579-20.935h831.25c21.86 0 39.583 9.373 39.583 20.935v41.876zM855.003 526.553h-12c0-161.793-151.025-293.421-336.66-293.421-185.633 0-336.656 131.628-336.656 293.421h-12c0-41.334 9.261-81.425 27.527-119.161 17.612-36.384 42.807-69.046 74.886-97.079 65.813-57.509 153.264-89.181 246.243-89.181 92.981 0 180.434 31.672 246.247 89.181 32.079 28.032 57.274 60.693 74.887 97.079 18.264 37.734 27.526 77.826 27.526 119.161z"
              fill="#EA0606"></path><path
              d="M1001.996 588.091c-121.146 13.91-980.875 0-980.875 0s-30.62-203.887 241.944-144.555c171.281-1.178 273.436 0 489.644 0 193.07-59.332 283.186 108.642 249.287 144.555z"
              fill="#EA0606"></path><path
              d="M500.343 214.379h12v330.342h-12z"
              fill="#EA0606"></path><path
              d="M333.217 657.192c0 46.174-38.961 83.602-87.029 83.602-48.056 0-87.021-37.428-87.021-83.602 0-46.172 38.963-83.588 87.021-83.588 48.067 0 87.029 37.417 87.029 83.588z"
              fill="#FFFFFF"></path><path
              d="M246.188 743.794c-49.638 0-90.021-38.85-90.021-86.602 0-47.745 40.383-86.588 90.021-86.588 49.642 0 90.029 38.843 90.029 86.588 0 47.752-40.387 86.602-90.029 86.602z m0-167.19c-46.329 0-84.021 36.151-84.021 80.588 0 44.444 37.692 80.602 84.021 80.602 46.333 0 84.029-36.157 84.029-80.602 0-44.436-37.696-80.588-84.029-80.588z"
              fill="#440A0A"></path><path
              d="M309.872 656.757c0 33.159-27.986 60.035-62.491 60.035-34.51 0-62.487-26.876-62.487-60.035 0-33.16 27.977-60.022 62.487-60.022 34.505 0.001 62.491 26.862 62.491 60.022z"
              fill="#440A0A"></path><path
              d="M271.322 657.558c0 11.747-9.918 21.282-22.151 21.282-12.237 0-22.152-9.535-22.152-21.282 0-11.758 9.916-21.277 22.152-21.277 12.233-0.002 22.151 9.519 22.151 21.277z"
              fill="#FFFFFF"></path><path
              d="M875.521 642.811c0 46.175-38.963 83.603-87.027 83.603-48.061 0-87.021-37.428-87.021-83.603 0-46.173 38.962-83.587 87.021-83.587 48.066 0 87.027 37.414 87.027 83.587z"
              fill="#FFFFFF"></path><path
              d="M788.494 729.413c-49.638 0-90.021-38.85-90.021-86.603 0-47.744 40.384-86.587 90.021-86.587 49.642 0 90.027 38.843 90.027 86.587 0 47.753-40.385 86.603-90.027 86.603z m0-167.189c-46.33 0-84.021 36.151-84.021 80.587 0 44.444 37.691 80.603 84.021 80.603 46.333 0 84.027-36.158 84.027-80.603 0-44.436-37.694-80.587-84.027-80.587z"
              fill="#440A0A"></path><path
              d="M852.174 642.374c0 33.159-27.979 60.037-62.486 60.037-34.512 0-62.487-26.878-62.487-60.037 0-33.161 27.977-60.023 62.487-60.023 34.506-0.001 62.486 26.862 62.486 60.023z"
              fill="#440A0A"></path><path
              d="M813.628 643.173c0 11.75-9.919 21.278-22.153 21.278-12.233 0-22.151-9.528-22.151-21.278 0-11.759 9.919-21.275 22.151-21.275 12.235-0.002 22.153 9.516 22.153 21.275z"
              fill="#FFFFFF"></path><path
              d="M518.135 469.838h56.847v12.556h-56.847z"
              fill="#FFFFFF"></path><path
              d="M522.818 468.694c0 7.902-0.648 14.309-1.445 14.309h-30.37c-0.799 0-1.446-6.406-1.446-14.309V440.06c0-7.912 0.647-14.315 1.446-14.315h30.37c0.797 0 1.445 6.403 1.445 14.315v28.634z"
              fill="#EA0606">
                </path>
            </svg>
          </span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
  // Vue 3 中组件必须同步可用（或显式 defineAsyncComponent），此处统一静态导入
  import Loader from "../components/common/Loader.vue";
  import Zombie from "../components/common/Zombie.vue";
  import Printer from "../components/common/Printer.vue";
  import ArticleList from "../components/common/ArticleList.vue";
  import SortArticle from "../components/common/SortArticle.vue";
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import ArchiveSidebar from "../components/common/ArchiveSidebar.vue";

  export default {
    components: {
      Loader,
      Zombie,
      Printer,
      ArticleList,
      SortArticle,
      MyFooter,
      MyAside,
      ArchiveSidebar
    },

    data() {
      return {
        pushDialogVisible: false,
        push: {},
        loading: false,
        showAside: true,
        indexType: 1,
        printerInfo: "你看对面的青山多漂亮",
        pagination: {
          current: 1,
          size: 10,
          total: 0,
          searchKey: "",
          sortId: null,
          articleSearch: ""
        },
        guShi: {
          "content": "",
          "origin": "",
          "author": "",
          "category": ""
        },
        articles: [],
        sortArticles: {}
      };
    },

    watch: {},

    created() {
      this.getGuShi();
      this.getSortArticles();
    },

    computed: {
      sortInfo() {
        return this.$store.state.sortInfo;
      },
      // 首页背景：后台指定的背景 > 后台随机封面 > 本地默认图
      indexBackground() {
        const webInfo = this.$store.state.webInfo;
        if (!this.$common.isEmpty(webInfo.backgroundImage)) {
          // 老配置存的是未压缩的默认图路径，统一换成体积更小的 webp 版本
          return webInfo.backgroundImage === '/images/home-default-bg.jpg'
            ? '/images/home-default-bg.webp'
            : webInfo.backgroundImage;
        }
        if (!this.$common.isEmpty(webInfo.randomCover)) {
          return webInfo.randomCover[Math.floor(Math.random() * webInfo.randomCover.length)];
        }
        return '/images/home-default-bg.webp';
      },
      // 首页打字机诗句开关，默认开启
      printerEnabled() {
        return this.$store.state.sysConfig.printerEnabled !== '0';
      },
      // 首页首屏标题开关，默认开启
      titleEnabled() {
        return this.$store.state.sysConfig.titleEnabled !== '0';
      },
      // 首页背景图尺寸：公开配置 homeBackgroundSize 为整数百分比（50-200），空值/格式错误/超范围按默认 100% 处理
      backgroundZoom() {
        const value = Number(this.$store.state.sysConfig.homeBackgroundSize);
        if (!Number.isFinite(value) || value < 50 || value > 200) {
          return 1;
        }
        return value / 100;
      }
    },

    mounted() {
      setTimeout(() => {
        this.push = this.$common.pushNotification(this.$store.state.webInfo.notices, false);
        if(!this.$common.isEmpty(this.push)) {
          if("0" !== localStorage.getItem("showPushNotification_" + this.push['链接'])) {
            this.pushDialogVisible = true;
            localStorage.setItem("showPushNotification_" + this.push['链接'], "0");
          }
        }
      }, 2000);
    },

    methods: {
      async selectSort(sort) {
        this.pagination = {
          current: 1,
          size: 10,
          total: 0,
          searchKey: "",
          sortId: sort.id,
          articleSearch: ""
        };
        this.articles = [];
        await this.getArticles();
        this.$nextTick(() => {
          this.indexType = 2;
          $(".announcement").css("max-width", "780px");
          document.querySelector('.recent-posts').scrollIntoView({
            behavior: "smooth",
            block: "start",
            inline: "nearest"
          });
        });
      },
      async selectArticle(articleSearch) {
        this.pagination = {
          current: 1,
          size: 10,
          total: 0,
          searchKey: "",
          sortId: null,
          articleSearch: articleSearch
        };
        this.articles = [];
        await this.getArticles();
        this.$nextTick(() => {
          this.indexType = 2;
          $(".announcement").css("max-width", "780px");
          document.querySelector('.recent-posts').scrollIntoView({
            behavior: "smooth",
            block: "start",
            inline: "nearest"
          });
        });
      },
      pageArticles() {
        this.pagination.current = this.pagination.current + 1;
        this.getArticles();
      },

      async getArticles() {
        await this.$http.post(this.$constant.baseURL + "/article/listArticle", this.pagination)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.articles = this.articles.concat(res.data.records);
              this.pagination.total = res.data.total;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      getSortArticles() {
        this.$http.get(this.$constant.baseURL + "/article/listSortArticle")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.sortArticles = res.data;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      navigation(selector) {
        let pageId = document.querySelector(selector);
        window.scrollTo({
          top: pageId.offsetTop,
          behavior: "smooth"
        });
      },
      pushUrl(url) {
        window.open(url);
      },
      getGuShi() {
        let that = this;
        let xhr = new XMLHttpRequest();
        xhr.open('get', this.$constant.jinrishici);
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4) {
            that.guShi = JSON.parse(xhr.responseText);
            that.printerInfo = that.guShi.content;
          }
        };
        xhr.send();
      }
    }
  }
</script>

<style scoped>

  .background-image-index {
    width: 100vw;
    /* 首屏铺满：dvh 兜底移动端地址栏吃掉的高度，不支持的浏览器回落到 vh */
    height: 100vh;
    height: 100dvh;
    position: fixed;
    z-index: -1;
  }

  /* 人像头部靠近画面上方，居中裁切容易切头，整体略微下移 */
  .background-image-index :deep(img) {
    object-position: center 40%;
    /* 后台可调背景图尺寸：缩放聚焦点与裁切聚焦点保持一致，默认 100% 时为 1，不影响现有观感 */
    transform: scale(var(--home-bg-zoom, 1));
    transform-origin: center 40%;
  }

  .background-image-index::before {
    position: absolute;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, .2);
    content: '';
  }

  .background-image-index-error {
    background-color: var(--lightGreen);
    width: 100vw;
    height: 100vh;
    height: 100dvh;
    position: fixed;
    z-index: -1;
  }

  .signature-wall {
    /* 向下排列 */
    display: flex;
    flex-direction: column;
    position: relative;
    user-select: none;
    height: 100vh;
    height: 100dvh;
    overflow: hidden;
  }

  .playful {
    color: var(--white);
    font-size: 40px;
  }

  .printer {
    cursor: pointer;
    color: var(--white);
    background: var(--translucent);
    border-radius: 10px;
    padding-left: 10px;
    padding-right: 10px;
  }

  /* 首屏底部水波容器：贴底平铺，高度与参考站一致（移动端 10vh，桌面 15vh） */
  .banner-waves {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 100%;
    height: 10vh;
    min-height: 50px;
    max-height: 150px;
    z-index: 10;
    overflow: hidden;
    pointer-events: none;
    line-height: 0;
    transform: translateZ(0);
  }

  .banner-waves-svg {
    display: block;
    width: 100%;
    height: 100%;
  }

  /* 四层同一路径依次下移，透明度递减，叠加出深浅错落的水波 */
  .banner-waves-parallax > use {
    fill: var(--background);
    animation: banner-wave-move 25s cubic-bezier(.55, .5, .45, .5) infinite;
  }

  .banner-waves-parallax > use:nth-child(1) {
    opacity: 0.25;
    animation-delay: -2s;
    animation-duration: 7s;
  }

  .banner-waves-parallax > use:nth-child(2) {
    opacity: 0.5;
    animation-delay: -3s;
    animation-duration: 10s;
  }

  .banner-waves-parallax > use:nth-child(3) {
    opacity: 0.65;
    animation-delay: -4s;
    animation-duration: 13s;
  }

  .banner-waves-parallax > use:nth-child(4) {
    opacity: 0.75;
    animation-delay: -5s;
    animation-duration: 20s;
  }

  @keyframes banner-wave-move {
    0% {
      transform: translate3d(-90px, 0, 0);
    }
    100% {
      transform: translate3d(85px, 0, 0);
    }
  }

  @media screen and (min-width: 768px) {
    .banner-waves {
      height: 15vh;
    }
  }

  /* 关闭动效偏好下水波保持静止，高度、层级与叠层关系不变 */
  @media (prefers-reduced-motion: reduce) {
    .banner-waves-parallax > use {
      animation: none;
    }
  }

  /* 光标 */
  .cursor {
    margin-left: 1px;
    animation: hideToShow 0.7s infinite;
    font-weight: 200;
  }

  .el-icon-arrow-down {
    font-size: 40px;
    font-weight: bold;
    color: var(--white);
    position: absolute;
    bottom: 60px;
    animation: my-shake 1.5s ease-out infinite;
    z-index: 15;
    cursor: pointer;
  }

  .page-container-wrap {
    background: var(--ui-page-bg);
    position: relative;
  }

  .page-container {
    display: flex;
    justify-content: center;
    gap: var(--ui-page-gap);
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    padding: 0 var(--ui-page-gutter) var(--ui-page-bottom);
    margin: 0 auto;
    flex-direction: row;
  }

  .recent-posts {
    width: 70%;
  }

  .announcement {
    padding: 22px;
    border: 1px dashed var(--lightGray);
    color: var(--greyFont);
    border-radius: 10px;
    display: flex;
    margin: 40px auto 40px;
  }

  .announcement i {
    color: var(--iconColor);
    font-size: 22px;
    margin: auto 0;
    animation: scale 0.8s ease-in-out infinite;
  }

  .announcement div div {
    margin-left: 20px;
    line-height: 30px;
  }

  .aside-content {
    width: calc(30% - 40px);
    user-select: none;
    margin-top: 40px;
    margin-right: 40px;
    max-width: 300px;
    float: right;
  }

  /* 右栏默认隐藏，与归档页同一断点：≥1280 才出现第三列 */
  .index-side {
    display: none;
    min-width: 0;
  }

  @media screen and (min-width: 1280px) {
    .page-container {
      display: grid;
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
      gap: var(--ui-page-gap);
      align-items: start;
    }

    .aside-content {
      width: auto;
      max-width: none;
      margin-right: 0;
      float: none;
    }

    .recent-posts {
      width: auto;
      min-width: 0;
    }

    .index-side {
      display: block;
      margin-top: 40px;
    }
  }

  .pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 40px;
  }

  .pagination {
    padding: 8px 14px;
    min-height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px dashed var(--ui-btn-border);
    border-radius: var(--ui-btn-radius);
    background: var(--ui-btn-bg);
    box-shadow: var(--ui-btn-shadow);
    color: var(--greyFont);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    width: 100px;
    user-select: none;
    cursor: pointer;
    text-align: center;
    transition: var(--ui-btn-transition);
  }

  .pagination:hover {
    border-style: dashed;
    border-color: var(--ui-btn-border-hover);
    box-shadow: var(--ui-btn-shadow-hover);
    color: var(--ui-accent);
  }

  /* 按压瞬时缩放，与参考站 active:scale-95 一致 */
  .pagination:active {
    border-style: dashed;
    border-color: var(--ui-btn-border-hover);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }

  .push-title {
    font-weight: bold;
    font-size: 20px;
  }

  .push-el-image {
    width: 80%;
    min-height: 100px;
    max-height: 400px;
    border-radius: 15px;
    margin-top: 20px;
    margin-bottom: 30px;
  }

  .push-button {
    position: relative;
    background: var(--ui-accent-strong);
    cursor: pointer;
    width: 230px;
    min-height: 36px;
    padding: 8px 14px;
    border: 1px solid transparent;
    border-radius: var(--ui-btn-radius);
    box-shadow: var(--ui-btn-shadow);
    line-height: 20px;
    color: var(--white);
    font-size: 14px;
    font-weight: 500;
    transition: var(--ui-btn-transition);
  }

  .push-button:hover {
    background: var(--ui-accent-strong-hover);
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  /* 按压瞬时缩放，与参考站 active:scale-95 一致 */
  .push-button:active {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }

  .push-button-title {
    margin-left: 20px;
    font-weight: bold;
  }

  .push-button-car {
    position: absolute;
    margin-left: 55px;
    animation: passing 4s linear infinite;
  }

  @media screen and (max-width: 1100px) {
    .recent-posts {
      width: 100%;
    }

    .page-container {
      width: var(--ui-page-inline);
    }
  }

  @media screen and (max-width: 1000px) {

    .page-container {
      /* 文章栏与侧标栏垂直排列 */
      flex-direction: column;
    }

    .aside-content {
      width: 100%;
      max-width: unset;
      float: unset;
      margin: 40px auto 0;
    }
  }

  @media screen and (max-width: 768px) {

    h1 {
      font-size: 35px;
    }
  }
</style>
