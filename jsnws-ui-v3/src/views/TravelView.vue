<template>
  <div>
    <div class="travel-container">
      <div class="travel-content my-animation-slide-bottom">
        <!-- 分类筛选 -->
        <div class="gallery-tabs" v-if="photoTitleList.length > 0">
          <button type="button"
                  class="gallery-tab"
                  v-for="item in photoTitleList"
                  :key="item.classify"
                  :class="{ 'is-active': photoPagination.classify === item.classify }"
                  @click="changePhotoTitle(item.classify)">
            <span>{{ item.classify }}</span>
            <span class="tab-count">{{ item.count }}</span>
          </button>
        </div>

        <!-- 加载中 -->
        <div class="gallery-state shadow-box" v-if="loading">
          正在加载相册…
        </div>

        <!-- 加载失败 -->
        <div class="gallery-state shadow-box" v-else-if="error">
          <p>{{ error }}</p>
          <p class="state-tip">请检查网络或稍后重试</p>
        </div>

        <!-- 空态 -->
        <div class="gallery-state shadow-box" v-else-if="photoList.length === 0">
          <el-icon class="state-icon"><Picture /></el-icon>
          <p>{{ emptyText }}</p>
          <p class="state-tip">照片会按分类展示在这里</p>
        </div>

        <!-- 照片瀑布流 -->
        <div class="gallery-masonry" v-else>
          <div class="gallery-item shadow-box-mini"
               v-for="(photo, index) in photoList"
               :key="photo.id || index">
            <el-image class="gallery-image"
                      lazy
                      fit="cover"
                      :src="photo.cover"
                      :preview-src-list="previewList"
                      :initial-index="index"
                      preview-teleported>
              <template #error>
                <div class="gallery-image-error"></div>
              </template>
            </el-image>

            <div class="gallery-mask">
              <div class="gallery-title">{{ photo.title }}</div>
              <div class="gallery-time">{{ $common.getDateDiff(photo.createTime) }}</div>
            </div>

            <div class="gallery-zoom">
              <el-icon><ZoomIn /></el-icon>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="!loading && !error && photoList.length > 0">
          <div @click="pagePhotos()" class="pagination"
               v-if="photoPagination.total !== photoList.length">
            {{ loadingMore ? '加载中…' : '下一页' }}
          </div>
          <div v-else style="user-select: none">
            ~~到底啦~~
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div style="background: var(--favoriteBg)">
      <MyFooter></MyFooter>
    </div>
  </div>
</template>

<script>
  import { Picture, ZoomIn } from '@element-plus/icons-vue'
  import MyFooter from "../components/common/MyFooter.vue";

  export default {
    components: {
      Picture,
      ZoomIn,
      MyFooter
    },

    data() {
      return {
        loading: true,
        loadingMore: false,
        error: '',
        photoPagination: {
          current: 1,
          size: 12,
          total: 0,
          resourceType: "lovePhoto",
          classify: ""
        },
        photoTitleList: [],
        photoList: []
      }
    },

    computed: {
      // 灯箱预览用当前已加载的全部照片，初始定位到被点的那张
      previewList() {
        return this.photoList.map((photo) => photo.cover).filter((cover) => cover)
      },
      emptyText() {
        return this.photoTitleList.length === 0 ? '还没有相册' : '这个分类下还没有照片'
      }
    },

    created() {
      this.getPhotoTitles();
    },

    methods: {
      getPhotoTitles() {
        this.loading = true;
        this.error = '';
        this.$http.get(this.$constant.baseURL + "/webInfo/listAdminLovePhoto")
          .then((res) => {
            if (this.$common.isEmpty(res.data)) {
              this.photoTitleList = [];
              this.photoList = [];
              this.loading = false;
              return;
            }
            this.photoTitleList = res.data;
            this.photoPagination = {
              current: 1,
              size: 12,
              total: 0,
              resourceType: "lovePhoto",
              classify: this.photoTitleList[0].classify
            };
            this.changePhoto();
          })
          .catch((error) => {
            this.error = '相册加载失败：' + error.message;
            this.loading = false;
          });
      },
      changePhotoTitle(classify) {
        if (classify !== this.photoPagination.classify) {
          this.photoPagination = {
            current: 1,
            size: 12,
            total: 0,
            resourceType: "lovePhoto",
            classify: classify
          };
          this.photoList = [];
          this.changePhoto();
        }
      },
      pagePhotos() {
        if (this.loadingMore) {
          return;
        }
        this.photoPagination.current = this.photoPagination.current + 1;
        this.changePhoto(true);
      },
      changePhoto(append = false) {
        this.loading = !append;
        this.error = '';
        if (append) {
          this.loadingMore = true;
        }

        this.$http.post(this.$constant.baseURL + "/webInfo/listResourcePath", this.photoPagination)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              const records = res.data.records || [];
              this.photoList = append ? this.photoList.concat(records) : records;
              this.photoPagination.total = res.data.total || 0;
            } else {
              this.photoPagination.total = 0;
              if (!append) {
                this.photoList = [];
              }
            }
          })
          .catch((error) => {
            if (append) {
              this.$message({
                message: error.message,
                type: "error"
              });
            } else {
              this.error = '相册加载失败：' + error.message;
            }
          })
          .finally(() => {
            this.loading = false;
            this.loadingMore = false;
          });
      }
    }
  }
</script>

<style scoped>

  .travel-container {
    padding: 25px;
    background: var(--favoriteBg);
  }

  /* 去掉封面后，内容需自行避开顶部固定导航（60px） */
  .travel-content {
    margin: 90px auto 0;
    max-width: 1200px;
  }

  /* 分类筛选：与导航胶囊同一套观感 */
  .gallery-tabs {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
    margin: 40px auto 30px;
  }

  .gallery-tab {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 36px;
    padding: 0 16px;
    border: none;
    border-radius: 999px;
    background: var(--hoverBg);
    color: var(--fontColor);
    font-size: 14px;
    letter-spacing: 0.5px;
    cursor: pointer;
    transition: background 0.2s ease, color 0.2s ease;
  }

  .gallery-tab:hover {
    background: var(--subtleBg);
  }

  .gallery-tab.is-active {
    background: var(--fontColor);
    color: var(--background);
  }

  .gallery-tab .tab-count {
    font-size: 12px;
    opacity: 0.65;
  }

  /* 瀑布流：三栏，按图片原始比例排布 */
  .gallery-masonry {
    column-count: 3;
    column-gap: 18px;
  }

  .gallery-item {
    position: relative;
    break-inside: avoid;
    margin-bottom: 18px;
    border-radius: 14px;
    overflow: hidden;
    background: var(--background);
    cursor: zoom-in;
  }

  .gallery-image {
    display: block;
    width: 100%;
  }

  .gallery-image :deep(.el-image__inner) {
    display: block;
    width: 100%;
    height: auto;
    transition: transform 0.5s ease;
  }

  .gallery-item:hover .gallery-image :deep(.el-image__inner) {
    transform: scale(1.05);
  }

  .gallery-image-error {
    width: 100%;
    padding-top: 66%;
    background: var(--favoriteBg);
  }

  /* 悬停信息层：不拦截点击，灯箱由 el-image 接管 */
  .gallery-mask {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 26px 14px 12px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.62), rgba(0, 0, 0, 0));
    opacity: 0;
    transform: translateY(8px);
    transition: opacity 0.3s ease, transform 0.3s ease;
    pointer-events: none;
  }

  .gallery-item:hover .gallery-mask {
    opacity: 1;
    transform: translateY(0);
  }

  .gallery-title {
    color: #ffffff;
    font-size: 14px;
    font-weight: 600;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .gallery-time {
    margin-top: 2px;
    color: rgba(255, 255, 255, 0.78);
    font-size: 12px;
  }

  .gallery-zoom {
    position: absolute;
    top: 10px;
    right: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.35);
    color: #ffffff;
    font-size: 16px;
    opacity: 0;
    transition: opacity 0.3s ease;
    pointer-events: none;
  }

  .gallery-item:hover .gallery-zoom {
    opacity: 1;
  }

  .gallery-state {
    max-width: 760px;
    margin: 20px auto;
    padding: 46px 24px;
    border-radius: 14px;
    text-align: center;
    font-size: 15px;
    color: var(--greyFontColor);
    background: var(--background);
  }

  .gallery-state .state-icon {
    font-size: 26px;
    color: var(--greyFontColor);
  }

  .gallery-state .state-tip {
    margin: 8px 0 0;
    font-size: 13px;
    color: var(--greyFontColor);
  }

  .pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 40px;
  }

  .pagination {
    padding: 13px 15px;
    border: 1px solid var(--lightGray);
    border-radius: 3rem;
    color: var(--greyFont);
    width: 100px;
    user-select: none;
    cursor: pointer;
    text-align: center;
  }

  @media screen and (max-width: 1150px) {
    .gallery-masonry {
      column-count: 2;
    }
  }

  @media screen and (max-width: 750px) {
    .gallery-masonry {
      column-count: 1;
    }

    .travel-container {
      padding: 14px;
    }
  }

</style>
