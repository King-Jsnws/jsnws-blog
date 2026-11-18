<template>
  <div>
    <div id="jotting" class="jotting-page my-animation-slide-bottom">
      <div class="jotting-head">
        <div class="jotting-head-row">
          <div class="jotting-head-text">
            <h1>随笔</h1>
            <p v-if="!loading && !error && jottingList.length > 0">
              共 {{ pagination.total }} 条随笔，记录生活里的碎片
            </p>
          </div>
          <!-- 站长专用发布入口：登录判断与导航栏「后台」入口一致 -->
          <button class="jotting-compose-btn"
                  type="button"
                  v-if="adminLoggedIn"
                  @click="toggleComposer">
            <el-icon><EditPen /></el-icon>
            <span>{{ composerVisible ? '收起' : '写随笔' }}</span>
          </button>
        </div>

        <!-- 内嵌编辑器：沿用随笔卡片的深色圆角卡片样式 -->
        <div class="jotting-composer shadow-box" v-if="adminLoggedIn && composerVisible">
          <textarea class="jotting-textarea"
                    v-model="draft"
                    rows="4"
                    maxlength="1000"
                    placeholder="记录此刻的想法…"></textarea>
          <div class="composer-foot">
            <span class="composer-count">{{ draft.length }}/1000</span>
            <div class="composer-actions">
              <span class="composer-cancel" @click="closeComposer">取消</span>
              <button class="composer-submit"
                      type="button"
                      :disabled="publishing"
                      @click="publishJotting">{{ publishing ? '发布中…' : '发布' }}</button>
            </div>
          </div>
        </div>
      </div>

      <div class="jotting-state shadow-box" v-if="loading">
        正在加载随笔…
      </div>

      <div class="jotting-state shadow-box" v-else-if="error">
        <p>{{ error }}</p>
        <p class="state-tip">请检查网络或稍后重试</p>
      </div>

      <div class="jotting-state shadow-box" v-else-if="jottingList.length === 0">
        <el-icon class="state-icon"><EditPen /></el-icon>
        <p>还没有随笔</p>
        <p class="state-tip">这里会按时间顺序展示发布的随笔</p>
      </div>

      <div class="jotting-list" v-else>
        <article class="jotting-item shadow-box"
                 v-for="jotting in jottingList"
                 :key="jotting.id">
          <div class="jotting-meta">
            <el-avatar shape="circle" :size="38" :src="authorAvatar"></el-avatar>
            <div class="meta-text">
              <div class="author-row">
                <span class="author-name">{{ authorName }}</span>
                <span class="site-badge" v-if="siteName" :title="siteName">
                  <svg viewBox="0 0 24 24" width="11" height="11" aria-hidden="true"><path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                  {{ siteName }}
                </span>
                <span class="boss-badge" title="本站主理人">
                  <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M5 16 3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm0 2h14v2H5z"/></svg>
                  主理人
                </span>
              </div>
              <span class="jotting-time">{{ jotting.createTime }}</span>
            </div>
          </div>

          <div class="jotting-content" v-html="jotting.html"></div>

          <div class="jotting-foot">
            <span class="jotting-date">{{ jotting.createTime }}</span>
            <span class="jotting-delete"
                  v-if="canDelete(jotting)"
                  @click="deleteJotting(jotting.id)">删除</span>
          </div>
        </article>
      </div>

      <div class="pagination-wrap" v-if="!loading && !error && jottingList.length > 0">
        <div class="pagination"
             @click="nextPage()"
             v-if="pagination.total > jottingList.length">
          {{ loadingMore ? '加载中…' : '下一页' }}
        </div>
        <div class="pagination-end" v-else>
          ~~到底啦~~
        </div>
      </div>
    </div>

    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import { EditPen } from '@element-plus/icons-vue'
  import MyFooter from '../components/common/MyFooter.vue'

  export default {
    name: 'JottingView',
    components: {
      EditPen,
      MyFooter
    },
    data() {
      return {
        loading: true,
        loadingMore: false,
        error: '',
        jottingList: [],
        // 站长发布：编辑器展开状态、草稿、提交中状态
        composerVisible: false,
        draft: '',
        publishing: false,
        pagination: {
          current: 1,
          size: 10,
          total: 0
        }
      }
    },
    computed: {
      // 随笔作者即站点：头像优先用站点头像，缺失时用当前用户头像兜底
      authorAvatar() {
        const webInfo = this.$store.state.webInfo
        if (!this.$common.isEmpty(webInfo.avatar)) {
          return webInfo.avatar
        }
        const user = this.$store.state.currentUser
        return this.$common.isEmpty(user) ? '' : user.avatar
      },
      authorName() {
        // 随笔作者展示站长昵称（无昵称时后端回退账号），缺失再兜底站点名称
        const boss = this.$store.state.bossInfo || {}
        if (!this.$common.isEmpty(boss.username)) {
          return boss.username
        }
        const webInfo = this.$store.state.webInfo
        return this.$common.isEmpty(webInfo.webName) ? '站长' : webInfo.webName
      },
      // 站点名称徽章：取 webName，缺失退回 webTitle
      siteName() {
        const info = this.$store.state.webInfo || {}
        if (!this.$common.isEmpty(info.webName)) {
          return info.webName
        }
        const title = Array.isArray(info.webTitle) ? info.webTitle.join('') : info.webTitle
        return this.$common.isEmpty(title) ? '' : title
      },
      // 站长登录判断：与 HomeLayout 导航栏「后台」入口完全一致
      adminLoggedIn() {
        return !this.$common.isEmpty(this.$store.state.currentAdmin) && Boolean(localStorage.getItem('adminToken'))
      }
    },
    created() {
      this.getJotting()
    },
    methods: {
      getJotting(append = false) {
        this.loading = !append
        this.error = ''

        this.$http.post(this.$constant.baseURL + '/weiYan/listWeiYan', {
          current: this.pagination.current,
          size: this.pagination.size
        }).then((res) => {
          if (this.$common.isEmpty(res.data)) {
            this.pagination.total = 0
            if (!append) {
              this.jottingList = []
            }
            return
          }

          const records = (res.data.records || []).map((item) => {
            return {
              ...item,
              html: this.formatContent(item.content)
            }
          })

          this.jottingList = append ? this.jottingList.concat(records) : records
          this.pagination.total = res.data.total || 0

          this.$nextTick(() => {
            this.$common.imgShow('#jotting .pictureReg')
          })
        }).catch((error) => {
          if (append) {
            this.$message({
              message: error.message,
              type: 'error'
            })
          } else {
            this.error = '随笔加载失败：' + error.message
          }
        }).finally(() => {
          this.loading = false
          this.loadingMore = false
        })
      },
      // 与旧微言页一致：换行、表情、[alt,url] 图片标记转成 HTML
      formatContent(content) {
        if (this.$common.isEmpty(content)) {
          return ''
        }
        let html = content.replace(/\n{2,}/g, '<div style="height: 12px"></div>')
        html = html.replace(/\n/g, '<br/>')
        html = this.$common.faceReg(html)
        html = this.$common.pictureReg(html)
        return html
      },
      nextPage() {
        if (this.loadingMore) {
          return
        }
        this.loadingMore = true
        this.pagination.current = this.pagination.current + 1
        this.getJotting(true)
      },
      toggleComposer() {
        this.composerVisible = !this.composerVisible
      },
      closeComposer() {
        this.composerVisible = false
      },
      // 后端按 yyyy-MM-dd HH:mm 解析创建时间，这里手动补零
      formatCreateTime(date) {
        const pad = (value) => (value < 10 ? '0' + value : '' + value)
        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) +
          ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes())
      },
      publishJotting() {
        if (this.publishing) {
          return
        }
        const content = this.draft.trim()
        if (this.$common.isEmpty(content)) {
          this.$message({
            message: '随笔内容不能为空',
            type: 'warning'
          })
          return
        }

        this.publishing = true
        // 复用微言新增接口：isAdmin 为 true 时使用管理员 token，userId 由后端取当前登录者
        this.$http.post(this.$constant.baseURL + '/weiYan/saveNews', {
          content: content,
          createTime: this.formatCreateTime(new Date())
        }, true).then(() => {
          this.$message({
            message: '发布成功',
            type: 'success'
          })
          this.draft = ''
          this.composerVisible = false
          // 回到第一页重新拉取，新随笔按时间倒序出现在列表顶部
          this.pagination.current = 1
          this.getJotting()
        }).catch((error) => {
          this.$message({
            message: '发布失败：' + error.message,
            type: 'error'
          })
        }).finally(() => {
          this.publishing = false
        })
      },
      canDelete(jotting) {
        const user = this.$store.state.currentUser
        return !this.$common.isEmpty(user) && user.id === jotting.userId
      },
      deleteJotting(id) {
        this.$confirm('确认删除？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + '/weiYan/deleteWeiYan', {id: id})
            .then(() => {
              this.$message({
                type: 'success',
                message: '删除成功!'
              })
              this.pagination.current = 1
              this.getJotting()
            })
            .catch((error) => {
              this.$message({
                message: error.message,
                type: 'error'
              })
            })
        }).catch(() => {
          this.$message({
            type: 'success',
            message: '已取消删除!'
          })
        })
      }
    }
  }
</script>

<style scoped>
  .jotting-page {
    min-height: calc(100vh - 60px);
    padding: 120px 20px 60px;
    background: var(--background);
  }

  .jotting-head {
    max-width: 760px;
    margin: 0 auto 28px;
  }

  .jotting-head h1 {
    margin: 0 0 8px;
    font-size: 26px;
    letter-spacing: 2px;
    color: var(--fontColor);
  }

  .jotting-head p {
    margin: 0;
    font-size: 14px;
    color: var(--greyFontColor);
  }

  .jotting-head-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
  }

  .jotting-head-text {
    min-width: 0;
  }

  /* 随笔页局部覆盖：对齐友链页 .friend-btn（透明底 + 中性灰 1px 实线，hover 才转虚线），不动全局 token */
  .jotting-compose-btn {
    display: inline-flex;
    align-items: center;
    flex-shrink: 0;
    margin-top: 2px;
    margin-left: 16px;
    padding: 6px 12px;
    min-height: 34px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background-color: transparent;
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .jotting-compose-btn .el-icon {
    margin-right: 6px;
  }

  .jotting-compose-btn:hover,
  .jotting-compose-btn:focus-visible {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  /* 按压瞬时缩放（transition 不含 scale） */
  .jotting-compose-btn:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .jotting-compose-btn {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .jotting-compose-btn:hover,
  html.dark .jotting-compose-btn:focus-visible,
  html.dark .jotting-compose-btn:active {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  /* 内嵌编辑器：与随笔列表卡片同一套圆角与底色 */
  .jotting-composer {
    margin-top: 16px;
    padding: 16px 18px 12px;
    border-radius: 14px;
    background: var(--background);
  }

  .jotting-textarea {
    display: block;
    box-sizing: border-box;
    width: 100%;
    min-height: 96px;
    padding: 12px 14px;
    border: 1px solid var(--borderColor);
    border-radius: 10px;
    background: transparent;
    color: var(--articleFontColor);
    font-family: inherit;
    font-size: 15px;
    line-height: 1.8;
    resize: vertical;
    outline: none;
    transition: border-color 0.25s ease;
  }

  .jotting-textarea::placeholder {
    color: var(--greyFontColor);
  }

  .jotting-textarea:focus {
    border-color: var(--themeBackground);
  }

  .composer-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
  }

  .composer-count {
    font-size: 12px;
    color: var(--greyFontColor);
  }

  .composer-actions {
    display: flex;
    align-items: center;
  }

  /* 编辑器「取消」：与「删除」「发布」同一套描边与几何，文字沿用原来的灰色 */
  .composer-cancel {
    display: inline-flex;
    align-items: center;
    margin-right: 16px;
    padding: 6px 12px;
    min-height: 34px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background-color: transparent;
    color: var(--greyFontColor);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .composer-cancel:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    color: var(--fontColor);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .composer-cancel:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .composer-cancel {
    border-color: rgba(255, 255, 255, 0.16);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .composer-cancel:hover,
  html.dark .composer-cancel:active {
    border-color: rgba(255, 255, 255, 0.4);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  /* 编辑区「发布」：与「收起」同属友链页那一套（透明底 + 中性灰实线，hover 转虚线），几何节奏一致 */
  .composer-submit {
    padding: 6px 12px;
    min-height: 34px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background-color: transparent;
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .composer-submit:hover:not(:disabled),
  .composer-submit:focus-visible:not(:disabled) {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  /* 按压瞬时缩放（transition 不含 scale） */
  .composer-submit:active:not(:disabled) {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .composer-submit {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .composer-submit:hover:not(:disabled),
  html.dark .composer-submit:focus-visible:not(:disabled),
  html.dark .composer-submit:active:not(:disabled) {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  .composer-submit:disabled {
    opacity: 0.55;
    box-shadow: none;
    cursor: not-allowed;
  }

  .jotting-list {
    max-width: 760px;
    margin: 0 auto;
  }

  .jotting-item {
    margin-bottom: 18px;
    padding: 18px 20px 12px;
    border-radius: 14px;
    background: var(--background);
  }

  .jotting-meta {
    display: flex;
    align-items: center;
  }

  .jotting-meta .el-avatar {
    border-radius: 50%;
  }

  .meta-text {
    display: flex;
    flex-direction: column;
    margin-left: 10px;
    min-width: 0;
  }

  .author-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  /* 主理人用户名流光：与留言区完全一致的扫光节奏与色值 */
  .author-name {
    font-size: 14px;
    font-weight: 700;
    line-height: 1.4;
    word-break: break-all;
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-image: linear-gradient(100deg,
      rgb(230, 0, 18) 0%,
      rgb(230, 0, 18) 42%,
      rgb(255, 255, 255) 50%,
      rgb(230, 0, 18) 58%,
      rgb(230, 0, 18) 100%);
    background-size: 250% 100%;
    background-repeat: no-repeat;
    background-position: 100% 0;
    -webkit-background-clip: text;
    background-clip: text;
    animation: jotting-name-shimmer 3s linear infinite;
  }

  @keyframes jotting-name-shimmer {
    0% {
      background-position: 100% 0;
    }
    100% {
      background-position: 0% 0;
    }
  }

  /* 主理人徽章与站点名称徽章：复用留言区主理人正红色系 */
  .boss-badge,
  .site-badge {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 7px;
    border-radius: 4px;
    overflow: hidden;
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
    user-select: none;
  }

  .boss-badge {
    border: 1px solid rgba(230, 0, 18, 0.52);
    background: rgba(230, 0, 18, 0.18);
    color: rgb(230, 0, 18);
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.16);
  }

  .site-badge {
    border: 1px solid rgba(230, 0, 18, 0.46);
    background: rgba(230, 0, 18, 0.12);
    color: rgb(230, 0, 18);
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.12);
  }

  .boss-badge svg,
  .site-badge svg {
    color: rgb(230, 0, 18);
    filter: drop-shadow(0 0 1.92px rgba(230, 0, 18, 0.25));
  }

  /* 徽章流光：白色高光从左滑到右 */
  .boss-badge::before,
  .site-badge::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 50%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
    animation: jotting-badge-shine 3s ease-in-out infinite;
    pointer-events: none;
  }

  @keyframes jotting-badge-shine {
    to {
      left: 150%;
    }
  }

  html.dark .author-name {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-image: linear-gradient(100deg,
      rgb(255, 120, 130) 0%,
      rgb(255, 120, 130) 42%,
      rgb(255, 255, 255) 50%,
      rgb(255, 120, 130) 58%,
      rgb(255, 120, 130) 100%);
  }

  html.dark .boss-badge {
    border-color: rgba(255, 120, 130, 0.52);
    background: rgba(255, 120, 130, 0.18);
    color: rgb(255, 120, 130);
    box-shadow: 0 1px 3px rgba(255, 59, 74, 0.24);
  }

  html.dark .site-badge {
    border-color: rgba(255, 59, 74, 0.46);
    background: rgba(255, 59, 74, 0.14);
    color: rgb(255, 120, 130);
    box-shadow: 0 1px 3px rgba(255, 59, 74, 0.18);
  }

  html.dark .boss-badge svg,
  html.dark .site-badge svg {
    color: rgb(255, 120, 130);
    filter: drop-shadow(0 0 1.92px rgba(255, 120, 130, 0.25));
  }

  .jotting-time {
    font-size: 12px;
    color: var(--greyFontColor);
    line-height: 1.4;
  }

  .jotting-content {
    margin: 14px 0 10px;
    font-size: 15px;
    line-height: 1.9;
    letter-spacing: 0.5px;
    color: var(--articleFontColor);
    word-break: break-word;
  }

  .jotting-content :deep(.pictureReg) {
    max-width: 100% !important;
    border-radius: 8px !important;
    margin: 6px 0;
  }

  .jotting-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 10px;
    border-top: 1px dashed var(--borderColor);
    font-size: 13px;
    color: var(--greyFontColor);
  }

  /* 随笔「删除」：补按钮外观（友链那一套描边与几何），文字沿用原来的警示红 */
  .jotting-delete {
    display: inline-flex;
    align-items: center;
    padding: 6px 12px;
    min-height: 34px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background-color: transparent;
    color: #f56c6c;
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .jotting-delete:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    color: #f56c6c;
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .jotting-delete:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .jotting-delete {
    border-color: rgba(255, 255, 255, 0.16);
    color: #f56c6c;
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .jotting-delete:hover,
  html.dark .jotting-delete:active {
    border-color: rgba(255, 255, 255, 0.4);
    color: #f56c6c;
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  .jotting-state {
    max-width: 760px;
    margin: 0 auto;
    padding: 46px 24px;
    border-radius: 14px;
    text-align: center;
    font-size: 15px;
    color: var(--greyFontColor);
    background: var(--background);
  }

  .jotting-state .state-icon {
    font-size: 26px;
    color: var(--greyFontColor);
  }

  .jotting-state .state-tip {
    margin: 8px 0 0;
    font-size: 13px;
    color: var(--greyFontColor);
  }

  .pagination-wrap {
    display: flex;
    justify-content: center;
    margin: 26px 0 10px;
  }

  .pagination {
    padding: 6px 20px;
    border: 1px solid var(--lightGray);
    border-radius: 3rem;
    color: var(--greyFont);
    user-select: none;
    cursor: pointer;
    text-align: center;
    font-size: 12px;
  }

  .pagination:hover {
    border: 1px solid var(--themeBackground);
    color: var(--themeBackground);
    box-shadow: 0 0 5px var(--themeBackground);
  }

  .pagination-end {
    font-size: 12px;
    color: var(--greyFont);
    user-select: none;
  }

  @media screen and (max-width: 600px) {
    .jotting-page {
      padding: 100px 14px 40px;
    }

    .jotting-item {
      padding: 14px 14px 10px;
    }

    .jotting-compose-btn {
      margin-left: 10px;
      padding: 6px 12px;
      font-size: 12px;
    }

    .jotting-composer {
      padding: 12px 12px 10px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .jotting-compose-btn,
    .composer-submit,
    .composer-cancel,
    .jotting-delete {
      transition: none;
    }

    .jotting-compose-btn:active,
    .composer-submit:active:not(:disabled),
    .composer-cancel:active,
    .jotting-delete:active {
      scale: 1;
    }
  }
</style>
