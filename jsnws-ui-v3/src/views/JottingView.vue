<template>
  <div>
    <div id="jotting" class="jotting-page">
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
            <el-avatar shape="square" :size="38" :src="authorAvatar"></el-avatar>
            <div class="meta-text">
              <span class="author-name">{{ authorName }}</span>
              <span class="jotting-time">{{ jotting.createTime }}</span>
            </div>
          </div>

          <div class="jotting-content" v-html="jotting.html"></div>

          <div class="jotting-foot">
            <span class="jotting-date">😃 {{ jotting.createTime }}</span>
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
        const webInfo = this.$store.state.webInfo
        return this.$common.isEmpty(webInfo.webName) ? '站长' : webInfo.webName
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

  /* 站长发布入口：与随笔卡片同色系，描边 + 悬停主题色 */
  .jotting-compose-btn {
    display: inline-flex;
    align-items: center;
    flex-shrink: 0;
    margin-top: 2px;
    margin-left: 16px;
    padding: 8px 16px;
    border: 1px solid var(--borderColor);
    border-radius: 3rem;
    background: var(--background);
    color: var(--fontColor);
    font-size: 13px;
    line-height: 1;
    cursor: pointer;
    transition: all 0.25s ease;
  }

  .jotting-compose-btn .el-icon {
    margin-right: 6px;
  }

  .jotting-compose-btn:hover {
    border-color: var(--themeBackground);
    color: var(--themeBackground);
    box-shadow: 0 0 5px var(--themeBackground);
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

  .composer-cancel {
    margin-right: 16px;
    font-size: 13px;
    color: var(--greyFontColor);
    cursor: pointer;
  }

  .composer-cancel:hover {
    color: var(--fontColor);
  }

  .composer-submit {
    padding: 6px 20px;
    border: none;
    border-radius: 3rem;
    background: var(--themeBackground);
    color: var(--white);
    font-size: 13px;
    line-height: 1.6;
    cursor: pointer;
    transition: opacity 0.25s ease;
  }

  .composer-submit:hover {
    opacity: 0.85;
  }

  .composer-submit:disabled {
    opacity: 0.55;
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
    border-radius: 4px;
  }

  .meta-text {
    display: flex;
    flex-direction: column;
    margin-left: 10px;
  }

  .author-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--fontColor);
    line-height: 1.4;
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

  .jotting-delete {
    cursor: pointer;
    color: #f56c6c;
  }

  .jotting-delete:hover {
    text-decoration: underline;
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
      padding: 7px 12px;
      font-size: 12px;
    }

    .jotting-composer {
      padding: 12px 12px 10px;
    }
  }
</style>
