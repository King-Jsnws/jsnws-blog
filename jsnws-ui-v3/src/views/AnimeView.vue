<template>
  <div>
    <div class="anime-wrap">
      <!-- 左侧卡片 -->
      <div class="anime-aside">
        <MyAside @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
      </div>

      <div class="anime-main my-animation-slide-bottom">
        <!-- 标题 -->
        <div class="anime-head">
          <div class="anime-title-row">
            <div class="anime-title-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#fff" stroke-width="1.8"
                   stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <rect x="2.8" y="6.5" width="18.4" height="12.5" rx="2.5"/>
                <path d="m8.5 2.8 3.5 3.7 3.5-3.7"/>
                <path d="M10.2 10.8v4.4l3.8-2.2z" fill="#fff" stroke="none"/>
              </svg>
            </div>
            <div class="anime-title">追番</div>
          </div>
          <div class="anime-subtitle">我的追番列表</div>
          <div class="anime-update" v-if="animeList.length > 0">数据更新于 {{updateTime}}</div>
        </div>

        <!-- 数据统计 -->
        <div class="anime-stats">
          <div class="anime-stat" v-for="(stat, index) in stats" :key="index">
            <div class="anime-stat-label">{{stat.label}}</div>
            <div class="anime-stat-value">{{stat.value}}</div>
          </div>
        </div>

        <!-- 筛选与排序 -->
        <div class="anime-toolbar" v-if="animeList.length > 0">
          <div class="anime-chips">
            <button type="button" class="anime-chip" :class="{'anime-chip-active': activeType === ''}"
                    @click="activeType = ''">全部 {{animeList.length}}</button>
            <button type="button" class="anime-chip"
                    v-for="(item, index) in typeFilters" :key="index"
                    :class="{'anime-chip-active': activeType === item.type}"
                    @click="activeType = item.type">{{item.type}} {{item.count}}</button>
          </div>
          <el-select v-model="sortType" size="small" class="anime-sort">
            <el-option label="评分最高" value="scoreDesc"></el-option>
            <el-option label="评分最低" value="scoreAsc"></el-option>
            <el-option label="最新发布" value="newest"></el-option>
            <el-option label="最早发布" value="oldest"></el-option>
          </el-select>
        </div>

        <!-- 番剧列表 -->
        <div class="anime-grid" v-if="!$common.isEmpty(sortedAnime)">
          <div class="anime-card" v-for="(item, index) in sortedAnime" :key="index">
            <div class="anime-cover">
              <img :src="item.cover" :alt="item.title" loading="lazy">
              <span class="anime-score">{{item.score.toFixed(1)}}</span>
            </div>
            <div class="anime-body">
              <div class="anime-meta">
                <span class="anime-type">{{item.type}}</span>
                <span class="anime-source">{{item.source}}</span>
              </div>
              <div class="anime-name-row">
                <h3 class="anime-name">{{item.title}}</h3>
                <span class="anime-status">{{item.status}}</span>
              </div>
              <p class="anime-desc">{{item.description}}</p>
              <a class="anime-more" :href="item.url" target="_blank" rel="noopener noreferrer">
                查看详情
                <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
                  <path fill="currentColor"
                        d="m16 8.4l-8.9 8.9q-.275.275-.7.275t-.7-.275t-.275-.7t.275-.7L14.6 7H7q-.425 0-.712-.288T6 6t.288-.712T7 5h10q.425 0 .713.288T18 6v10q0 .425-.288.713T17 17t-.712-.288T16 16z"/>
                </svg>
              </a>
            </div>
          </div>
        </div>
        <div class="anime-empty" v-else>
          <el-icon class="anime-empty-icon"><VideoCamera /></el-icon>
          <p>还没有追番记录～</p>
          <p class="anime-empty-tip">暂无番剧，可在后台「追番管理」中添加</p>
        </div>
      </div>
    </div>

    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import { VideoCamera } from '@element-plus/icons-vue'

  export default {
    components: {
      MyFooter,
      MyAside,
      VideoCamera
    },

    data() {
      return {
        activeType: "",
        sortType: "scoreDesc",
        updateTime: "",
        // 由后台「追番管理」维护，进入页面时拉取
        animeList: []
      }
    },

    computed: {
      typeFilters() {
        const map = {};
        this.animeList.forEach((item) => {
          map[item.type] = (map[item.type] || 0) + 1;
        });
        return Object.keys(map).map((type) => {
          return {type: type, count: map[type]};
        });
      },
      filteredAnime() {
        if (this.activeType === "") {
          return this.animeList;
        }
        return this.animeList.filter((item) => item.type === this.activeType);
      },
      sortedAnime() {
        const list = this.filteredAnime.slice();
        switch (this.sortType) {
          case "scoreAsc":
            return list.sort((a, b) => a.score - b.score);
          case "newest":
            return list.sort((a, b) => new Date(b.release) - new Date(a.release));
          case "oldest":
            return list.sort((a, b) => new Date(a.release) - new Date(b.release));
          default:
            return list.sort((a, b) => b.score - a.score);
        }
      },
      stats() {
        let total = this.animeList.length;
        let scoreSum = 0;
        let finished = 0;
        this.animeList.forEach((item) => {
          scoreSum += Number(item.score || 0);
          if ((item.status || "").indexOf("全") === 0) {
            finished = finished + 1;
          }
        });
        return [
          {label: "总追番", value: total},
          {label: "平均评分", value: total === 0 ? "0.0" : (scoreSum / total).toFixed(1)},
          {label: "已完结", value: finished},
          {label: "数据来源", value: "后台维护"}
        ];
      }
    },

    created() {
      this.setUpdateTime();
      this.getAnime();
    },

    methods: {
      setUpdateTime() {
        const now = new Date();
        const pad = (num) => (num < 10 ? "0" + num : num);
        this.updateTime = now.getFullYear() + "年" + (now.getMonth() + 1) + "月" + now.getDate() + "日 " +
          pad(now.getHours()) + ":" + pad(now.getMinutes()) + ":" + pad(now.getSeconds());
      },
      getAnime() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listAnime")
          .then((res) => {
            if (this.$common.isEmpty(res.data)) {
              return;
            }
            this.animeList = res.data.map((item) => {
              return Object.assign({}, item, {
                score: Number(item.score || 0),
                release: item.releaseDate || ""
              });
            });
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      selectSort(sort) {
        this.$router.push({path: "/sort", query: {sortId: sort.id}});
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
    grid-template-columns: 300px minmax(0, 1fr);
    column-gap: 30px;
    align-items: start;
    width: 90%;
    max-width: 1400px;
    margin: 0 auto;
    padding: 70px 20px 50px;
  }

  .anime-aside {
    min-width: 0;
    align-self: stretch;
  }

  .anime-main {
    min-width: 0;
  }

  .anime-title-row {
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .anime-title-icon {
    width: 32px;
    height: 32px;
    border-radius: 10px;
    background: #1f1f1f;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;
  }

  .anime-title {
    font-size: 22px;
    font-weight: 700;
    color: var(--fontColor);
    line-height: 1.2;
  }

  .anime-subtitle {
    margin-top: 14px;
    font-size: 15px;
    color: var(--maxGreyFont);
    letter-spacing: 1px;
  }

  .anime-update {
    margin-top: 6px;
    font-size: 13px;
    color: var(--maxGreyFont);
  }

  .anime-stats {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
    margin-top: 22px;
  }

  .anime-stat {
    padding: 16px 18px;
    background: var(--cardBg);
    border: 1px solid rgba(128, 128, 128, 0.16);
    border-radius: 14px;
    transition: all 0.3s;
  }

  .anime-stat:hover {
    box-shadow: 0 8px 22px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }

  .anime-stat-label {
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .anime-stat-value {
    margin-top: 8px;
    font-size: 22px;
    font-weight: 700;
    color: var(--fontColor);
    line-height: 1.1;
  }

  .anime-toolbar {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
    margin: 24px 0 20px;
  }

  .anime-chips {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 10px;
  }

  .anime-chip {
    padding: 7px 16px;
    border: 1px solid rgba(128, 128, 128, 0.22);
    border-radius: 3rem;
    background: transparent;
    color: var(--greyFont);
    font-size: 13px;
    cursor: pointer;
    transition: all 0.3s;
    outline: 0;
    font-family: inherit;
  }

  .anime-chip:hover {
    border-color: var(--fontColor);
    color: var(--fontColor);
  }

  .anime-chip-active {
    background: var(--fontColor);
    border-color: var(--fontColor);
    color: var(--background);
  }

  .anime-chip-active:hover {
    color: var(--background);
  }

  /* 黑夜模式下黑色图标底色与卡片过近，换成低透明度白底保留方块观感 */
  html.dark .anime-title-icon {
    background: rgba(255, 255, 255, 0.1);
  }

  .anime-sort {
    width: 122px;
    flex-shrink: 0;
  }

  .anime-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
    gap: 20px;
  }

  .anime-card {
    background: var(--cardBg);
    border: 1px solid rgba(128, 128, 128, 0.16);
    border-radius: 14px;
    overflow: hidden;
    transition: all 0.3s;
    display: flex;
    flex-direction: column;
  }

  .anime-card:hover {
    box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
    transform: translateY(-4px);
  }

  .anime-cover {
    position: relative;
    width: 100%;
    aspect-ratio: 16 / 9;
    background: var(--hoverBg);
    overflow: hidden;
  }

  .anime-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.5s;
  }

  .anime-card:hover .anime-cover img {
    transform: scale(1.05);
  }

  .anime-score {
    position: absolute;
    top: 10px;
    left: 10px;
    padding: 2px 8px;
    border-radius: 6px;
    background: rgba(0, 0, 0, 0.55);
    color: #ffd75e;
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 0.5px;
  }

  .anime-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 14px 16px 16px;
  }

  .anime-meta {
    display: flex;
    flex-direction: row;
    align-items: center;
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .anime-type {
    padding: 1px 8px;
    margin-right: 8px;
    border-radius: 4px;
    background: var(--blueSoftBg);
    color: #3b82f6;
  }

  .anime-name-row {
    display: flex;
    flex-direction: row;
    align-items: baseline;
    margin-top: 10px;
  }

  .anime-name {
    flex: 1;
    min-width: 0;
    margin: 0;
    font-size: 15px;
    font-weight: 700;
    color: var(--fontColor);
    line-height: 1.4;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .anime-status {
    flex-shrink: 0;
    margin-left: 8px;
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .anime-desc {
    margin: 8px 0 0;
    font-size: 12px;
    line-height: 1.7;
    color: var(--maxGreyFont);
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }

  .anime-more {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-top: auto;
    padding-top: 12px;
    font-size: 13px;
    color: var(--fontColor);
    text-decoration: none;
    transition: color 0.3s;
  }

  .anime-more svg {
    margin-left: 4px;
    transition: transform 0.3s;
  }

  .anime-more:hover {
    color: #3b82f6;
  }

  .anime-more:hover svg {
    transform: translate(2px, -2px);
  }

  .anime-empty {
    padding: 60px 0;
    text-align: center;
    font-size: 14px;
    color: var(--maxGreyFont);
    border: 1px dashed rgba(128, 128, 128, 0.3);
    border-radius: 14px;
  }

  .anime-empty .anime-empty-icon {
    font-size: 26px;
    color: var(--maxGreyFont);
  }

  .anime-empty p {
    margin: 8px 0 0;
  }

  .anime-empty .anime-empty-tip {
    font-size: 13px;
    color: var(--maxGreyFont);
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
    }

    .anime-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }
</style>
