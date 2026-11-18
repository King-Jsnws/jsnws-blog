<template>
  <div>
    <!-- 两句诗：与关于我页共用同一组件，背景图取图规则一致 -->
    <div class="my-animation-slide-top">
      <TwoPoem :isHitokoto="false"></TwoPoem>
    </div>

    <div class="anime-wrap my-animation-slide-bottom">
      <!-- 左侧卡片：日历按参考站放在右栏，这里关闭同款日历卡 -->
      <div class="anime-aside">
        <MyAside hide-calendar @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
      </div>

      <div class="anime-main">
        <!-- 页面标题 -->
        <div class="anime-head">
          <div class="anime-title-row">
            <div class="anime-title-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" aria-hidden="true">
                <path d="m9.5 15.5 7-4.5-7-4.5zM8 21v-2H4q-.825 0-1.412-.587T2 17V5q0-.825.588-1.412T4 3h16q.825 0 1.413.588T22 5v12q0 .825-.587 1.413T20 19h-4v2z"/>
              </svg>
            </div>
            <div class="anime-title">追番</div>
          </div>
          <p class="anime-subtitle">我的追番列表，数据来自 Bilibili 和 TMDB</p>
          <p class="anime-update" v-if="updateTime">数据更新于 {{updateTime}}</p>
        </div>

        <!-- 统计卡片 -->
        <div class="anime-stats">
          <div class="anime-stat">
            <div class="anime-stat-icon stat-primary">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                <path d="m9.5 11-.95 2.05L6.5 14l2.05.95L9.5 17l.95-2.05L12.5 14l-2.05-.95L9.5 11Zm6 0-.65 1.35-1.35.65 1.35.65.65 1.35.65-1.35L17.5 13l-1.35-.65L15.5 11ZM4 4l2 4h3L7 4h2l2 4h3l-2-4h2l2 4h3l-2-4h3q.825 0 1.413.588T22 6v12q0 .825-.588 1.413T20 20H4q-.825 0-1.413-.588T2 18V6q0-.825.588-1.413T4 4Z"/>
              </svg>
            </div>
            <div class="anime-stat-text">
              <div class="anime-stat-label">总追番</div>
              <div class="anime-stat-value">{{animeList.length}}</div>
            </div>
          </div>
          <div class="anime-stat">
            <div class="anime-stat-icon stat-pink">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                <path d="m12 21-1.45-1.3q-2.525-2.275-4.175-3.925T3.75 12.812T2.388 10.4T2 8.15Q2 5.8 3.575 4.225T7.5 2.65q1.3 0 2.475.55T12 4.75q.85-1 2.025-1.55t2.475-.55q2.35 0 3.925 1.575T22 8.15q0 1.15-.387 2.25t-1.363 2.412t-2.625 2.963T13.45 19.7z"/>
              </svg>
            </div>
            <div class="anime-stat-text">
              <div class="anime-stat-label">Bilibili 均分</div>
              <div class="anime-stat-value">{{bilibiliAverage}}</div>
            </div>
          </div>
          <div class="anime-stat">
            <div class="anime-stat-icon stat-yellow">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                <path d="m12 17.27 5.18 3.13-1.64-5.81L20.9 9.9l-5.86-.5L12 3.75 8.96 9.4l-5.86.5 5.36 4.69-1.64 5.81z"/>
              </svg>
            </div>
            <div class="anime-stat-text">
              <div class="anime-stat-label">TMDB 均分</div>
              <div class="anime-stat-value">{{tmdbAverage}}</div>
            </div>
          </div>
          <div class="anime-stat">
            <div class="anime-stat-icon stat-emerald">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                <path d="M12 3C7.58 3 4 4.79 4 7v10c0 2.21 3.58 4 8 4s8-1.79 8-4V7c0-2.21-3.58-4-8-4m0 2c3.87 0 6 1.5 6 2s-2.13 2-6 2-6-1.5-6-2 2.13-2 6-2m6 12c0 .5-2.13 2-6 2s-6-1.5-6-2v-2.23c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23Zm0-4.55c-1.61.78-3.72 1.25-6 1.25s-4.39-.47-6-1.25V9.23C7.61 10 9.72 10.45 12 10.45s4.39-.45 6-1.23Z"/>
              </svg>
            </div>
            <div class="anime-stat-text">
              <div class="anime-stat-label">数据来源</div>
              <div class="anime-stat-value truncate">{{dataSource}}</div>
            </div>
          </div>
        </div>

        <!-- 筛选与列表 -->
        <div class="anime-section">
          <div class="anime-toolbar">
            <div class="anime-search">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" aria-hidden="true">
                <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
              </svg>
              <input v-model="keyword" type="text" placeholder="搜索番剧..."
                     @input="resetPage">
            </div>
            <select v-model="sortType" class="anime-sort" @change="resetPage">
              <option value="scoreDesc">评分最高</option>
              <option value="scoreAsc">评分最低</option>
              <option value="newest">最新发布</option>
              <option value="oldest">最早发布</option>
            </select>
          </div>

          <div class="anime-tabs" v-if="typeTabs.length > 0">
            <button type="button" v-for="tab in typeTabs" :key="tab.type"
                    class="anime-tab" :class="{'anime-tab-active': activeType === tab.type}"
                    @click="selectType(tab.type)">
              {{tab.label}}
              <span class="anime-tab-count">{{tab.count}}</span>
            </button>
          </div>

          <div class="anime-grid" v-if="pageItems.length > 0">
            <div class="anime-card" v-for="item in pageItems" :key="item.id"
                 role="button" tabindex="0" @click="openDetail(item)"
                 @keydown.enter="openDetail(item)">
              <div class="anime-cover">
                <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy" decoding="async"
                     referrerpolicy="no-referrer">
                <div class="anime-cover-empty" v-else>
                  <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor"
                       stroke-width="1.5" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round"
                          d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0 0 22.5 18.75V5.25A2.25 2.25 0 0 0 20.25 3H3.75A2.25 2.25 0 0 0 1.5 5.25v13.5A2.25 2.25 0 0 0 3.75 21Z"/>
                  </svg>
                </div>
                <span class="anime-type-badge" :class="'type-' + (item.seasonType || 1)">
                  {{seasonTypeLabel(item.seasonType)}}
                </span>
                <span class="anime-rating" v-if="scoreOf(item) > 0">
                  <svg viewBox="0 0 20 20" width="12" height="12" fill="currentColor" aria-hidden="true">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  {{scoreOf(item).toFixed(1)}}
                </span>
                <span class="anime-source-badge" v-if="item.origin === 'bili'">Bilibili</span>
                <div class="anime-hover">
                  <div class="anime-hover-inner">
                    <p>{{item.description || '暂无简介'}}</p>
                    <button type="button" @click.stop="openDetail(item)">查看详情</button>
                  </div>
                </div>
              </div>
              <div class="anime-body">
                <h3 class="anime-name" :title="item.title">{{item.title}}</h3>
                <div class="anime-footer">
                  <span>{{item.status || (item.releaseDate || '').slice(0, 4)}}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="anime-empty" v-else>
            <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor"
                 stroke-width="1.5" stroke-linecap="round" aria-hidden="true">
              <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
            <p>没有找到匹配的番剧</p>
          </div>

          <div class="anime-pagination" v-if="totalPages > 1">
            <div class="pagination-mobile">
              <button type="button" class="page-btn" aria-label="上一页"
                      :disabled="currentPage === 1" @click="goPage(currentPage - 1)">
                <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6"/>
                </svg>
              </button>
              <div class="page-info">
                <span class="page-info-current">{{currentPage}}</span>
                <span class="page-info-sep">/</span>
                <span class="page-info-total">{{totalPages}}</span>
              </div>
              <button type="button" class="page-btn" aria-label="下一页"
                      :disabled="currentPage === totalPages" @click="goPage(currentPage + 1)">
                <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M9 18l6-6-6-6"/>
                </svg>
              </button>
            </div>
            <div class="pagination-desktop">
              <button type="button" class="page-btn" aria-label="上一页"
                      :disabled="currentPage === 1" @click="goPage(currentPage - 1)">
                <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6"/>
                </svg>
              </button>
              <template v-for="(page, index) in pageNumbers" :key="index">
                <span v-if="page === '...'" class="page-ellipsis">
                  <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" aria-hidden="true">
                    <path d="M6 10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm12 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-6 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/>
                  </svg>
                </span>
                <button v-else type="button" class="page-btn"
                        :class="{'page-btn-active': page === currentPage}"
                        :aria-current="page === currentPage ? 'page' : undefined"
                        @click="goPage(page)">{{page}}</button>
              </template>
              <button type="button" class="page-btn" aria-label="下一页"
                      :disabled="currentPage === totalPages" @click="goPage(currentPage + 1)">
                <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M9 18l6-6-6-6"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右栏：与归档页同一套，日历卡按参考站放这里 -->
      <aside class="anime-side">
        <ArchiveSidebar></ArchiveSidebar>
      </aside>
    </div>

    <!-- 详情弹窗 -->
    <teleport to="body">
      <div class="anime-modal" v-if="detailAnime" role="dialog" aria-modal="true" tabindex="-1"
           @click.self="closeDetail">
        <div class="anime-modal-panel">
          <button type="button" class="anime-modal-close" aria-label="关闭" @click="closeDetail">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
          <div class="anime-modal-body">
            <div class="anime-modal-poster">
              <img v-if="detailAnime.cover" :src="detailAnime.cover" :alt="detailAnime.title"
                   referrerpolicy="no-referrer">
              <div class="anime-modal-poster-empty" v-else>
                <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="currentColor"
                     stroke-width="1.5" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0 0 22.5 18.75V5.25A2.25 2.25 0 0 0 20.25 3H3.75A2.25 2.25 0 0 0 1.5 5.25v13.5A2.25 2.25 0 0 0 3.75 21Z"/>
                </svg>
              </div>
            </div>
            <div class="anime-modal-info">
              <h2 class="anime-modal-title">{{detailAnime.title}}</h2>
              <div class="anime-modal-chips">
                <span class="anime-modal-type" :class="'type-' + (detailAnime.seasonType || 1)">
                  {{seasonTypeLabel(detailAnime.seasonType)}}
                </span>
                <span class="anime-modal-rating" v-if="scoreOf(detailAnime) > 0">
                  <svg viewBox="0 0 20 20" width="12" height="12" fill="currentColor" aria-hidden="true">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  {{scoreOf(detailAnime).toFixed(1)}}
                </span>
                <span class="anime-modal-ep" v-if="detailAnime.status">{{detailAnime.status}}</span>
                <span class="anime-modal-source" v-if="detailAnime.origin === 'bili'">Bilibili</span>
              </div>
              <div class="anime-modal-synopsis" v-if="detailAnime.description">
                <h3>剧情简介</h3>
                <p>{{detailAnime.description}}</p>
              </div>
              <a class="anime-modal-cta" v-if="detailAnime.url" :href="detailAnime.url"
                 target="_blank" rel="noopener noreferrer">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
                     stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                立即观看
              </a>
            </div>
          </div>
        </div>
      </div>
    </teleport>

    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import ArchiveSidebar from "../components/common/ArchiveSidebar.vue";
  import TwoPoem from "../components/common/TwoPoem.vue";
  import { SEASON_TYPES, seasonTypeLabel as formatSeasonType } from "../constants/anime.js";

  export default {
    components: {
      MyFooter,
      MyAside,
      ArchiveSidebar,
      TwoPoem
    },

    data() {
      return {
        keyword: "",
        activeType: "",
        sortType: "scoreDesc",
        currentPage: 1,
        itemsPerPage: 24,
        detailAnime: null,
        updateTime: "",
        // 同步状态：数据来源与最后成功时间
        syncStatus: {
          dataSource: "后台维护",
          lastSuccessTime: ""
        },
        // 由后台「追番管理」维护或从 B 站同步，进入页面时拉取
        animeList: []
      }
    },

    computed: {
      dataSource() {
        const source = this.syncStatus.dataSource || "后台维护";
        // 仅本页该处展示用 bilibili，其余位置的「B站」文案保持不变
        return source === "B站" ? "bilibili" : source;
      },
      bilibiliAverage() {
        let sum = 0;
        let rated = 0;
        this.animeList.forEach((item) => {
          const score = this.scoreOf(item);
          if (score > 0) {
            sum += score;
            rated = rated + 1;
          }
        });
        return rated === 0 ? "0.0" : (sum / rated).toFixed(1);
      },
      // 当前没有 TMDB 数据源，与参考站在无 TMDB 数据时一致，均分按 0.0 展示
      tmdbAverage() {
        return "0.0";
      },
      typeTabs() {
        // 与参考站一致：按 season_type 聚合，缺失按 1（番剧）处理，数值升序
        const map = {};
        this.animeList.forEach((item) => {
          const type = item.seasonType || 1;
          map[type] = (map[type] || 0) + 1;
        });
        return Object.keys(map)
          .map(Number)
          .sort((a, b) => a - b)
          .map((type) => {
            return {
              type: type,
              label: SEASON_TYPES[type] || "番剧",
              count: map[type]
            };
          });
      },
      filteredAnime() {
        let list = this.animeList.slice();
        const keyword = (this.keyword || "").trim().toLowerCase();
        if (keyword) {
          list = list.filter((item) => (item.title || "").toLowerCase().indexOf(keyword) >= 0);
        }
        if (this.activeType) {
          list = list.filter((item) => (item.seasonType || 1) === this.activeType);
        }
        return list;
      },
      sortedAnime() {
        const list = this.filteredAnime;
        switch (this.sortType) {
          case "scoreAsc":
            return list.sort((a, b) => this.scoreOf(a) - this.scoreOf(b));
          case "newest":
            return list.sort((a, b) => (b.releaseDate || "").localeCompare(a.releaseDate || ""));
          case "oldest":
            return list.sort((a, b) => (a.releaseDate || "").localeCompare(b.releaseDate || ""));
          default:
            return list.sort((a, b) => this.scoreOf(b) - this.scoreOf(a));
        }
      },
      totalPages() {
        return Math.ceil(this.sortedAnime.length / this.itemsPerPage);
      },
      pageItems() {
        const start = (this.currentPage - 1) * this.itemsPerPage;
        return this.sortedAnime.slice(start, start + this.itemsPerPage);
      },
      pageNumbers() {
        const total = this.totalPages;
        const current = this.currentPage;
        if (total <= 7) {
          const pages = [];
          for (let i = 1; i <= total; i++) {
            pages.push(i);
          }
          return pages;
        }
        const pages = [1];
        const from = Math.max(2, current - 2);
        const to = Math.min(total - 1, current + 2);
        if (from > 2) {
          pages.push("...");
        }
        for (let i = from; i <= to; i++) {
          pages.push(i);
        }
        if (to < total - 1) {
          pages.push("...");
        }
        pages.push(total);
        return pages;
      }
    },

    created() {
      this.getSyncStatus();
      this.getAnime();
    },

    mounted() {
      document.addEventListener("keydown", this.handleKeydown);
    },

    beforeUnmount() {
      document.removeEventListener("keydown", this.handleKeydown);
    },

    methods: {
      scoreOf(item) {
        if (!item || item.score === null || item.score === undefined || item.score === "") {
          return 0;
        }
        return Number(item.score);
      },
      seasonTypeLabel(seasonType) {
        return formatSeasonType(seasonType || 1);
      },
      resetPage() {
        this.currentPage = 1;
      },
      selectType(type) {
        // 与参考站一致：再次点击已选中的类型回到全部
        this.activeType = this.activeType === type ? "" : type;
        this.resetPage();
      },
      goPage(page) {
        if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
          this.currentPage = page;
        }
      },
      openDetail(item) {
        this.detailAnime = item;
      },
      closeDetail() {
        this.detailAnime = null;
      },
      handleKeydown(event) {
        if (event.key === "Escape") {
          this.detailAnime = null;
        }
      },
      getSyncStatus() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getAnimeSyncStatus")
          .then((res) => {
            const data = res.data || {};
            this.syncStatus = {
              dataSource: data.dataSource || "后台维护",
              lastSuccessTime: data.lastSuccessTime || ""
            };
            this.updateTime = this.syncStatus.lastSuccessTime;
          })
          .catch(() => {
            this.syncStatus = {dataSource: "后台维护", lastSuccessTime: ""};
            this.updateTime = "";
          });
      },
      getAnime() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listAnime")
          .then((res) => {
            if (this.$common.isEmpty(res.data)) {
              this.animeList = [];
              return;
            }
            this.animeList = res.data.map((item) => {
              return Object.assign({}, item, {
                score: item.score === null || item.score === undefined || item.score === ""
                  ? null
                  : Number(item.score)
              });
            });
            // 与参考站一致：默认选中序号最小的类型
            this.activeType = this.typeTabs.length > 0 ? this.typeTabs[0].type : "";
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
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
  }
</script>

<style scoped>
  .anime-wrap {
    display: grid;
    grid-template-columns: var(--ui-aside-width) minmax(0, 1fr);
    column-gap: var(--ui-page-gap);
    align-items: start;
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    background: var(--ui-page-bg);
  }

  .anime-aside {
    min-width: 0;
    align-self: stretch;
  }

  .anime-main {
    min-width: 0;
  }

  /* 右栏默认隐藏，与归档页同一断点：≥1280 才出现第三列 */
  .anime-side {
    display: none;
    min-width: 0;
  }

  @media screen and (min-width: 1280px) {
    .anime-wrap {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
      column-gap: var(--ui-page-gap);
    }

    .anime-side {
      display: block;
    }
  }

  .anime-head {
    margin-bottom: 32px;
  }

  .anime-title-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }

  .anime-title-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: var(--ui-accent-strong);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .anime-title {
    font-size: var(--ui-title-size);
    font-weight: var(--ui-title-weight);
    color: var(--ui-title-color);
    line-height: 1.2;
  }

  .anime-subtitle {
    margin: 0;
    font-size: var(--ui-subtitle-size);
    line-height: 1.7;
    color: var(--ui-subtitle-color);
  }

  .anime-update {
    margin: 8px 0 0;
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .anime-stats {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 24px;
  }

  .anime-stat {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: var(--ui-card-bg);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    box-shadow: var(--ui-card-shadow);
  }

  .anime-stat-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-primary {
    background: rgba(47, 127, 216, 0.12);
    color: var(--ui-accent-strong);
  }

  .stat-pink {
    background: rgba(236, 72, 153, 0.12);
    color: #ec4899;
  }

  .stat-yellow {
    background: rgba(234, 179, 8, 0.12);
    color: #eab308;
  }

  .stat-emerald {
    background: rgba(16, 185, 129, 0.12);
    color: #10b981;
  }

  .anime-stat-text {
    min-width: 0;
  }

  .anime-stat-label {
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .anime-stat-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--fontColor);
    line-height: 1.25;
  }

  .anime-stat-value.truncate {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .anime-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .anime-toolbar {
    display: flex;
    flex-direction: row;
    gap: 8px;
  }

  .anime-search {
    position: relative;
    flex: 1;
    min-width: 0;
  }

  .anime-search svg {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--maxGreyFont);
    pointer-events: none;
  }

  .anime-search input {
    width: 100%;
    box-sizing: border-box;
    padding: 10px 16px 10px 40px;
    border: 1px solid rgba(128, 128, 128, 0.24);
    border-radius: 12px;
    background: var(--cardBg);
    color: var(--fontColor);
    font-size: 14px;
    font-family: inherit;
    outline: none;
    transition: border-color 0.3s;
  }

  .anime-search input::placeholder {
    color: var(--maxGreyFont);
  }

  .anime-search input:focus {
    border-color: var(--ui-accent-strong);
  }

  .anime-sort {
    flex-shrink: 0;
    padding: 0 12px;
    border: 1px solid rgba(128, 128, 128, 0.24);
    border-radius: 12px;
    background: var(--cardBg);
    color: var(--maxGreyFont);
    font-size: 14px;
    font-family: inherit;
    outline: none;
    cursor: pointer;
  }

  .anime-tabs {
    border-bottom: 1px solid rgba(128, 128, 128, 0.24);
    overflow-x: auto;
  }

  .anime-tabs::-webkit-scrollbar {
    display: none;
  }

  .anime-tab {
    display: inline-flex;
    align-items: center;
    padding: 16px 1px;
    margin-right: 32px;
    border: 0;
    border-bottom: 2px solid transparent;
    background: transparent;
    color: #6b7280;
    font-size: 14px;
    font-weight: 500;
    font-family: inherit;
    white-space: nowrap;
    cursor: pointer;
    transition: color 0.2s, border-color 0.2s;
  }

  .anime-tab:last-child {
    margin-right: 0;
  }

  .anime-tab:hover {
    color: #374151;
    border-bottom-color: #d1d5db;
  }

  .anime-tab-active,
  .anime-tab-active:hover {
    color: var(--ui-accent-strong);
    border-bottom-color: var(--ui-accent-strong);
  }

  .anime-tab-count {
    margin-left: 8px;
    padding: 2px 8px;
    border-radius: 999px;
    background: var(--hoverBg);
    color: var(--greyFont);
    font-size: 12px;
    font-weight: 500;
  }

  .anime-grid {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 16px;
  }

  .anime-card {
    position: relative;
    overflow: hidden;
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
    cursor: pointer;
    transition: box-shadow 0.3s, border-color 0.3s, transform 0.3s;
    outline: none;
  }

  .anime-card:hover {
    box-shadow: var(--ui-card-shadow-hover);
    border-color: var(--ui-accent-strong);
    transform: translateY(-4px);
  }

  .anime-cover {
    position: relative;
    aspect-ratio: 2 / 3;
    overflow: hidden;
    background: var(--hoverBg);
  }

  .anime-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.5s;
  }

  .anime-card:hover .anime-cover img {
    transform: scale(1.1);
  }

  .anime-cover-empty {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
    color: rgba(128, 128, 128, 0.5);
  }

  .anime-type-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    padding: 4px 8px;
    border-radius: 8px;
    color: #fff;
    font-size: 12px;
    font-weight: 700;
    backdrop-filter: blur(4px);
  }

  .anime-rating {
    position: absolute;
    top: 8px;
    right: 8px;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 8px;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;
    font-size: 12px;
    font-weight: 700;
    backdrop-filter: blur(4px);
  }

  .anime-rating svg {
    color: #facc15;
  }

  .anime-source-badge {
    position: absolute;
    bottom: 8px;
    left: 8px;
    padding: 2px 8px;
    border-radius: 6px;
    background: rgba(236, 72, 153, 0.8);
    color: #fff;
    font-size: 10px;
    font-weight: 500;
    backdrop-filter: blur(4px);
  }

  .anime-hover {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0.4), transparent);
    opacity: 0;
    transition: opacity 0.3s;
  }

  .anime-card:hover .anime-hover {
    opacity: 1;
  }

  .anime-hover-inner {
    padding: 12px;
  }

  .anime-hover-inner p {
    margin: 0 0 8px;
    color: rgba(255, 255, 255, 0.9);
    font-size: 12px;
    line-height: 1.7;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    overflow: hidden;
  }

  .anime-hover-inner button {
    width: 100%;
    padding: 6px 12px;
    border: 0;
    border-radius: 8px;
    background: var(--ui-accent-strong);
    color: #fff;
    font-size: 12px;
    font-weight: 500;
    font-family: inherit;
    cursor: pointer;
    transition: background 0.2s;
  }

  .anime-hover-inner button:hover {
    background: var(--ui-accent);
  }

  .anime-body {
    padding: 12px;
  }

  .anime-name {
    margin: 0 0 4px;
    font-size: 14px;
    font-weight: 600;
    color: var(--fontColor);
    line-height: 1.5;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .anime-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .anime-empty {
    padding: 64px 0;
    text-align: center;
    color: var(--maxGreyFont);
  }

  .anime-empty svg {
    margin-bottom: 16px;
    color: rgba(128, 128, 128, 0.4);
  }

  .anime-empty p {
    margin: 0;
    font-size: 14px;
  }

  .anime-pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 32px;
  }

  .pagination-mobile {
    display: none;
    align-items: center;
    gap: 12px;
  }

  .pagination-desktop {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .page-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border: 1px solid rgba(128, 128, 128, 0.16);
    border-radius: 8px;
    background: var(--cardBg);
    color: var(--fontColor);
    font-size: 16px;
    font-weight: 700;
    font-family: inherit;
    cursor: pointer;
    transition: background 0.2s, transform 0.2s;
  }

  .page-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .page-btn:not(:disabled):active {
    transform: scale(0.85);
  }

  .page-btn-active {
    background: var(--ui-accent-strong);
    border-color: var(--ui-accent-strong);
    color: #fff;
  }

  .page-ellipsis {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    color: var(--maxGreyFont);
  }

  .page-info {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 0 16px;
    height: 44px;
    border: 1px solid rgba(128, 128, 128, 0.16);
    border-radius: 8px;
    background: var(--cardBg);
  }

  .page-info-current {
    font-size: 16px;
    font-weight: 700;
    color: var(--ui-accent-strong);
  }

  .page-info-sep {
    font-size: 14px;
    color: var(--maxGreyFont);
  }

  .page-info-total {
    font-size: 16px;
    font-weight: 700;
    color: var(--fontColor);
  }

  /* 类型徽章配色，与参考站 season_type 对应 */
  .type-1 {
    background: #3b82f6;
  }

  .type-2 {
    background: #a855f7;
  }

  .type-3 {
    background: #10b981;
  }

  .type-4 {
    background: #f97316;
  }

  .type-5 {
    background: #ec4899;
  }

  .type-7 {
    background: #eab308;
  }

  .type-99 {
    background: #6b7280;
  }

  /* 详情弹窗 */
  .anime-modal {
    position: fixed;
    inset: 0;
    z-index: 50;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(4px);
    outline: none;
  }

  .anime-modal-panel {
    position: relative;
    width: 100%;
    max-width: 512px;
    max-height: 90vh;
    overflow: hidden;
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
    animation: anime-modal-in 0.2s ease-out;
  }

  @keyframes anime-modal-in {
    from {
      opacity: 0;
      transform: scale(0.9);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }

  .anime-modal-close {
    position: absolute;
    top: 16px;
    right: 16px;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: 0;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    cursor: pointer;
    backdrop-filter: blur(4px);
    transition: background 0.2s;
  }

  .anime-modal-close:hover {
    background: rgba(0, 0, 0, 0.7);
  }

  .anime-modal-body {
    display: flex;
    flex-direction: row;
  }

  .anime-modal-poster {
    width: 256px;
    flex-shrink: 0;
    overflow: hidden;
    background: var(--hoverBg);
  }

  .anime-modal-poster img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .anime-modal-poster-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 200px;
    color: rgba(128, 128, 128, 0.5);
  }

  .anime-modal-info {
    flex: 1;
    min-width: 0;
    padding: 24px;
    overflow-y: auto;
    max-height: 90vh;
  }

  .anime-modal-title {
    margin: 0 0 4px;
    font-size: 22px;
    font-weight: 700;
    color: var(--fontColor);
    line-height: 1.4;
  }

  .anime-modal-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
    margin-top: 4px;
  }

  .anime-modal-type {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: 8px;
    color: #fff;
    font-size: 12px;
    font-weight: 700;
  }

  .anime-modal-rating {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    border: 1px solid rgba(234, 179, 8, 0.2);
    border-radius: 8px;
    background: rgba(234, 179, 8, 0.1);
    color: #ca8a04;
    font-size: 12px;
    font-weight: 700;
  }

  .anime-modal-ep {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border: 1px solid rgba(59, 130, 246, 0.2);
    border-radius: 8px;
    background: rgba(59, 130, 246, 0.1);
    color: #2563eb;
    font-size: 12px;
    font-weight: 700;
  }

  .anime-modal-source {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border: 1px solid rgba(236, 72, 153, 0.2);
    border-radius: 8px;
    background: rgba(236, 72, 153, 0.1);
    color: #db2777;
    font-size: 12px;
    font-weight: 700;
  }

  .anime-modal-synopsis {
    margin-bottom: 24px;
  }

  .anime-modal-synopsis h3 {
    margin: 0 0 8px;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
  }

  .anime-modal-synopsis p {
    margin: 0;
    max-height: 160px;
    overflow-y: auto;
    font-size: 14px;
    line-height: 1.7;
    color: var(--greyFont);
  }

  .anime-modal-cta {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    width: 100%;
    box-sizing: border-box;
    padding: 12px 24px;
    border-radius: 12px;
    background: var(--ui-accent-strong);
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
    transition: background 0.2s, box-shadow 0.2s;
  }

  .anime-modal-cta:hover {
    background: var(--ui-accent);
    box-shadow: 0 10px 24px rgba(47, 127, 216, 0.28);
  }

  @media screen and (max-width: 1400px) {
    .anime-grid {
      grid-template-columns: repeat(4, minmax(0, 1fr));
    }
  }

  @media screen and (max-width: 1100px) {
    .anime-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  @media screen and (max-width: 1000px) {
    .anime-wrap {
      grid-template-columns: minmax(0, 1fr);
    }

    .anime-aside {
      display: none;
    }
  }

  @media screen and (max-width: 768px) {
    .anime-stats {
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px;
    }

    .anime-stat {
      padding: 12px;
      gap: 8px;
    }

    .anime-stat-icon {
      width: 32px;
      height: 32px;
    }

    .anime-stat-value {
      font-size: 18px;
    }

    .anime-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .pagination-desktop {
      display: none;
    }

    .pagination-mobile {
      display: flex;
    }

    .anime-modal-body {
      flex-direction: column;
      max-height: 90vh;
    }

    .anime-modal-poster {
      width: 100%;
      height: 256px;
    }

    .anime-modal-info {
      padding: 16px;
      max-height: 60vh;
    }
  }

  @media screen and (max-width: 480px) {
    .anime-modal-poster {
      height: 192px;
    }
  }
</style>
