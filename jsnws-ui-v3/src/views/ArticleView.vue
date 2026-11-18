<template>
  <div>
    <div v-if="!$common.isEmpty(article)">
      <div class="post-page my-animation-slide-bottom">
        <div class="post-main">
          <!-- 面包屑 -->
          <nav class="post-crumb">
            <span class="post-crumb-icon">
              <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
                <path fill="currentColor" d="M4 5h5.2l2 2H20v12H4z" opacity=".85"/>
              </svg>
            </span>
            <span class="post-crumb-link" @click="$router.push('/archive')">文章列表</span>
            <span class="post-crumb-sep">›</span>
            <span class="post-crumb-pill"
                  @click="$router.push({path: '/archive', query: {category: article.sort && article.sort.sortName}})">
              {{ article.sort.sortName }}
              <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true">
                <path fill="currentColor" d="m7 10 5 5 5-5z"/>
              </svg>
            </span>
            <span class="post-crumb-sep">›</span>
            <span class="post-crumb-current">此文章</span>
          </nav>

          <!-- 标题与元信息 -->
          <header class="post-hero">
            <h1 class="post-hero-title">{{ article.articleTitle }}</h1>
            <div class="post-hero-meta">
              <span>{{ article.createTime }}</span>
              <span class="post-meta-dot">·</span>
              <span class="post-meta-tag post-meta-tag--link"
                    @click="$router.push({path: '/archive', query: {tag: article.label.labelName}})">#{{ article.label.labelName }}</span>
              <span class="post-meta-tag">{{ article.sort.sortName }}</span>
              <span class="post-meta-dot">·</span>
              <span>{{ wordCount }} 字 {{ readMinutes }} 分钟</span>
            </div>
            <div class="post-hero-extra">
              <span class="post-views">浏览量 {{ article.viewCount }}</span>
              <span class="post-meta-dot">·</span>
              <span class="post-comments-count">{{ article.commentCount }} 评论</span>
            </div>
          </header>

          <!-- 简介卡：更新提示 + AI 摘要 + 封面图 -->
          <div class="post-intro-card"
               v-if="article.updateTime || articleSummary || (!coverError && !$common.isEmpty(article.articleCover))">
            <div class="post-intro-outdated" v-if="article.updateTime">
              <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
                <path fill="currentColor"
                      d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 15h-2v-6h2zm0-8h-2V7h2z"/>
              </svg>
              <span class="post-intro-outdated-text">最后更新于 {{ article.updateTime }}，部分内容可能已过时</span>
            </div>

            <div class="post-intro-divider" v-if="article.updateTime && articleSummary"></div>

            <details class="post-intro-details" v-if="articleSummary" open>
              <summary class="post-intro-summary">
                <span class="post-intro-summary-title">
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path fill="currentColor"
                          d="M12 2l1.9 5.6L19.5 9.5l-5.6 1.9L12 17l-1.9-5.6L4.5 9.5l5.6-1.9z"/>
                    <path fill="currentColor" d="M18.5 15l.9 2.6 2.6.9-2.6.9-.9 2.6-.9-2.6-2.6-.9 2.6-.9z"/>
                  </svg>
                  AI 摘要<span class="post-intro-model">- 自动提取</span>
                </span>
                <svg class="post-intro-arrow" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="currentColor" d="M6.7 9.3a1 1 0 0 1 1.4 0L12 13.2l3.9-3.9a1 1 0 1 1 1.4 1.4l-4.6 4.6a1 1 0 0 1-1.4 0L6.7 10.7a1 1 0 0 1 0-1.4z"/>
                </svg>
              </summary>
              <div class="post-intro-section">
                <p class="post-intro-desc">{{ articleSummary }}</p>
              </div>
            </details>

            <div class="post-intro-divider"
                 v-if="articleSummary && !coverError && !$common.isEmpty(article.articleCover)"></div>

            <details class="post-intro-details"
                     v-if="!coverError && !$common.isEmpty(article.articleCover)">
              <summary class="post-intro-summary">
                <span class="post-intro-summary-title">
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path fill="currentColor"
                          d="M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm0 2v14h14V5H5zm2 12 3-4 2 2.6 3-4.1L19 17H7z"/>
                  </svg>
                  封面图
                </span>
                <svg class="post-intro-arrow" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="currentColor" d="M6.7 9.3a1 1 0 0 1 1.4 0L12 13.2l3.9-3.9a1 1 0 1 1 1.4 1.4l-4.6 4.6a1 1 0 0 1-1.4 0L6.7 10.7a1 1 0 0 1 0-1.4z"/>
                </svg>
              </summary>
              <div class="post-intro-section post-intro-section--cover">
                <div class="post-intro-cover">
                  <el-image class="post-intro-cover-img"
                            :src="article.articleCover"
                            fit="cover"
                            @error="coverError = true">
                    <template #error>
                      <div class="post-intro-cover-fallback"></div>
                    </template>
                  </el-image>
                </div>
              </div>
            </details>
          </div>

          <!-- 正文 -->
          <article class="post-body">
            <div v-if="!$common.isEmpty(article.videoUrl)" class="post-video">
              <VideoPlayer :url="{src: $common.decrypt(article.videoUrl)}"
                           :cover="article.articleCover">
              </VideoPlayer>
            </div>

            <!-- 最新进展 -->
            <div v-if="!$common.isEmpty(treeHoleList)" class="process-wrap">
              <el-collapse accordion model-value="1">
                <el-collapse-item title="最新进展" name="1">
                  <Process :treeHoleList="treeHoleList" @deleteTreeHole="deleteTreeHole"></Process>
                </el-collapse-item>
              </el-collapse>

              <hr>
            </div>

            <!-- 文章内容 -->
            <div v-html="articleContentHtml" class="entry-content"></div>

            <!-- 作者与版权 -->
            <div class="post-end">
              <div class="post-end-info">
                <span>作者：{{ article.username }}</span>
                <span class="post-end-link" @click="copyrightDialogVisible = true">版权声明</span>
              </div>
              <button class="post-subscribe"
                      :class="{'post-subscribe--active': subscribe}"
                      type="button"
                      @click="subscribeLabel()">
                <el-icon>
                  <Pointer/>
                </el-icon>
                <span>{{ subscribe ? '已订阅专栏' : '订阅专栏' }}</span>
              </button>
            </div>
          </article>

          <!-- 相关文章 -->
          <section class="post-related" v-if="relatedArticles.length > 0" aria-label="相关文章">
            <h2 class="post-related-head">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path fill="currentColor"
                      d="M11 14h2q.425 0 .713-.288T14 13t-.288-.712T13 12h-2q-.425 0-.712.288T10 13t.288.713T11 14m0-3h6q.425 0 .713-.288T18 10t-.288-.712T17 9h-6q-.425 0-.712.288T10 10t.288.713T11 11m0-3h6q.425 0 .713-.288T18 7t-.288-.712T17 6h-6q-.425 0-.712.288T10 7t.288.713T11 8M8 18q-.825 0-1.412-.587T6 16V4q0-.825.588-1.412T8 2h12q.825 0 1.413.588T22 4v12q0 .825-.587 1.413T20 18zm0-2h12V4H8zm-4 6q-.825 0-1.412-.587T2 20V7q0-.425.288-.712T3 6t.713.288T4 7v13h13q.425 0 .713.288T18 21t-.288.713T17 22zM8 4v12z"/>
              </svg>
              <span>相关文章</span>
            </h2>
            <div class="post-related-grid">
              <div class="post-related-item" v-for="item in relatedArticles" :key="item.id">
                <div class="post-related-card" @click="openArticle(item.id)">
                  <div class="post-related-media">
                    <img v-if="!$common.isEmpty(item.articleCover)"
                         :src="item.articleCover"
                         :alt="item.articleTitle"
                         @error="$event.target.style.display = 'none'"/>
                    <span class="post-related-scrim"></span>
                  </div>
                  <div class="post-related-body">
                    <div class="post-related-name">{{ item.articleTitle }}</div>
                    <div class="post-related-meta">
                      {{ (item.sort && item.sort.sortName) || (item.label && item.label.labelName) }} ·
                      {{ formatDate(item.createTime) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 剪刀分割线 -->
          <div class="post-cut">
            <span>✂</span>
          </div>

          <!-- 评论：组件与样式保持原样 -->
          <div class="post-comments" v-if="article.commentStatus === true">
            <Comment :type="'article'" :source="article.id" :userId="article.userId"></Comment>
          </div>
        </div>

        <!-- 目录抽屉 -->
        <aside class="post-aside" v-if="hasHeadings">
          <div id="toc" class="toc"></div>
        </aside>
        <div class="post-float" v-if="hasHeadings">
          <button class="post-float-btn" type="button" title="目录" @click="clickTocButton()">
            <i class="fa fa-align-justify" aria-hidden="true"></i>
          </button>
          <button class="post-float-btn" type="button" title="回到顶部" @click="scrollToTop">
            <i class="fa fa-angle-up" aria-hidden="true"></i>
          </button>
        </div>
      </div>

      <div style="background: var(--background)">
        <MyFooter></MyFooter>
      </div>
    </div>

    <el-dialog title="版权声明"
               v-model="copyrightDialogVisible"
               width="80%"
               :append-to-body="true"
               modal-class="article-copy"
               center>
      <div style="display: flex;align-items: center;flex-direction: column">
        <el-avatar shape="square" :size="35" :src="$store.state.webInfo.avatar"></el-avatar>
        <div class="copyright-container">
          <p>
            {{ $store.state.webInfo.webName }}是指运行在{{ $constant.host }}域名及相关子域名上的网站，本条款描述了{{ $store.state.webInfo.webName }}的网站版权声明：
          </p>
          <ul>
            <li>
              {{ $store.state.webInfo.webName }}提供的所有文章、展示的图片素材等内容部分来源于互联网平台，仅供学习参考。如有侵犯您的版权，请联系{{ $store.state.webInfo.webName }}负责人，{{ $store.state.webInfo.webName }}承诺将在一个工作日内改正。
            </li>
            <li>
              {{ $store.state.webInfo.webName }}不保证网站内容的全部准确性、安全性和完整性，请您在阅读、下载及使用过程中自行确认，{{ $store.state.webInfo.webName }}亦不承担上述资源对您造成的任何形式的损失或伤害。
            </li>
            <li>未经{{ $store.state.webInfo.webName }}允许，不得盗链、盗用本站内容和资源。</li>
            <li>
              {{ $store.state.webInfo.webName }}旨在为广大用户提供更多的信息；{{ $store.state.webInfo.webName }}不保证向用户提供的外部链接的准确性和完整性，该外部链接指向的不由本站实际控制的任何网页上的内容，{{ $store.state.webInfo.webName }}对其合法性亦概不负责，亦不承担任何法律责任。
            </li>
            <li>
              {{ $store.state.webInfo.webName }}中的文章/视频（包括转载文章/视频）的版权仅归原作者所有，若作者有版权声明或文章从其它网站转载而附带有原所有站的版权声明者，其版权归属以附带声明为准；文章仅代表作者本人的观点，与{{ $store.state.webInfo.webName }}立场无关。
            </li>
            <li>
              {{ $store.state.webInfo.webName }}自行编写排版的文章均采用
              <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/" style="color: #38f;text-decoration: none;">
                知识共享署名-非商业性使用-相同方式共享 4.0 国际许可协议
              </a>
            </li>
            <li>
              许可协议标识：
              <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/">
                <img alt="知识共享许可协议"
                     src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png"
                     style="margin-top: 5px">
              </a>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="最新进展"
               v-model="weiYanDialogVisible"
               width="40%"
               :append-to-body="true"
               :close-on-click-modal="false"
               destroy-on-close
               center>
      <div>
        <div class="myCenter" style="margin-bottom: 20px">
          <el-date-picker
            v-model="newsTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetime"
            placeholder="选择日期时间">
          </el-date-picker>
        </div>
        <CommentBox :disableGraffiti="true"
                    @submitComment="submitWeiYan">
        </CommentBox>
      </div>
    </el-dialog>

    <!-- 微信 -->
    <el-dialog title="密码"
               :modal="false"
               v-model="showPasswordDialog"
               width="25%"
               :append-to-body="true"
               :close-on-click-modal="false"
               destroy-on-close
               center>
      <div>
        <div>
          <div class="password-content">{{tips}}</div>
        </div>
        <div style="margin: 20px auto">
          <el-input maxlength="30" v-model="password"></el-input>
        </div>
        <div style="display: flex;justify-content: center">
          <ProButton :info="'提交'"
                     :unified="true"
                     @click="submitPassword()"
                     :before="$constant.before_color_2"
                     :after="$constant.after_color_2">
          </ProButton>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import MarkdownIt from 'markdown-it';
  import multimdTable from 'markdown-it-multimd-table';

  import MyFooter from "../components/common/MyFooter.vue";
  import Comment from "../components/comment/Comment.vue";
  import Process from "../components/common/Process.vue";
  import CommentBox from "../components/comment/CommentBox.vue";
  import ProButton from "../components/common/ProButton.vue";
  import VideoPlayer from "../components/common/VideoPlayer.vue";
  import { emojiPlugin } from "../utils/emoji";

  // markdown-it 未内置任务列表语法（- [ ] / - [x]），补一个最小实现：
  // 列表项加 task-list-item、所属列表加 contains-task-list，行首插入只读复选框，
  // 与 md-editor 预览里的任务列表保持一致（样式见 markdown-highlight.css）。
  function taskListPlugin(md) {
    md.core.ruler.after('inline', 'task-lists', (state) => {
      const tokens = state.tokens;
      for (let i = 2; i < tokens.length; i++) {
        if (tokens[i].type !== 'inline') {
          continue;
        }
        if (tokens[i - 1].type !== 'paragraph_open' || tokens[i - 2].type !== 'list_item_open') {
          continue;
        }
        const match = /^\[([ xX])\][ \t]+/.exec(tokens[i].content);
        if (!match) {
          continue;
        }
        tokens[i].content = tokens[i].content.slice(match[0].length);
        // 去掉前缀后重新解析行内内容，避免残留 "[x] " 文本
        tokens[i].children.length = 0;
        state.md.inline.parse(tokens[i].content, state.md, state.env, tokens[i].children);
        const checkbox = new state.Token('html_inline', '', 0);
        checkbox.content = `<input type="checkbox" disabled${match[1].toLowerCase() === 'x' ? ' checked' : ''}>`;
        tokens[i].children.unshift(checkbox);
        tokens[i - 2].attrJoin('class', 'task-list-item');
        for (let j = i - 3; j >= 0; j--) {
          if (tokens[j].type === 'bullet_list_open' || tokens[j].type === 'ordered_list_open') {
            tokens[j].attrJoin('class', 'contains-task-list');
            break;
          }
        }
      }
      return true;
    });
  }


  export default {
    components: {
      MyFooter,
      Comment,
      CommentBox,
      ProButton,
      Process,
      VideoPlayer
    },

    data() {
      return {
        id: this.$route.params.id,
        subscribe: false,
        article: {},
        articleContentHtml: "",
        treeHoleList: [],
        weiYanDialogVisible: false,
        copyrightDialogVisible: false,
        newsTime: "",
        showPasswordDialog: false,
        password: "",
        tips: "",
        scrollTop: 0,
        coverError: false,
        relatedArticles: []
      };
    },
    computed: {
      wordCount() {
        const content = this.article.articleContent || "";
        const chineseCount = (content.match(/[\u4e00-\u9fa5]/g) || []).length;
        const wordCount = (content.replace(/[\u4e00-\u9fa5]/g, " ").match(/[a-zA-Z0-9]+/g) || []).length;
        return chineseCount + wordCount;
      },
      readMinutes() {
        return Math.max(1, Math.round(this.wordCount / 400));
      },
      hasHeadings() {
        return /^#{1,5}\s+\S/m.test(this.article.articleContent || "");
      },
      articleSummary() {
        let content = this.article.articleContent || "";
        content = content
          .replace(/```[\s\S]*?```/g, " ")
          .replace(/!\[[^\]]*]\([^)]*\)/g, " ")
          .replace(/\[([^\]]*)]\([^)]*\)/g, "$1")
          .replace(/<[^>]+>/g, " ")
          .replace(/^\s{0,3}#{1,6}\s+/gm, "")
          .replace(/^\s{0,3}>\s?/gm, "")
          .replace(/^\s{0,3}([-*+]|\d+\.)\s+/gm, "")
          .replace(/[*_`~]/g, "")
          .replace(/\|/g, " ")
          .replace(/\s+/g, " ")
          .trim();
        return content.length > 140 ? content.slice(0, 140) + "…" : content;
      }
    },
    watch: {
      '$route.params.id'(val) {
        if (!val || val === this.id) {
          return;
        }
        this.id = val;
        this.article = {};
        this.articleContentHtml = "";
        this.treeHoleList = [];
        this.relatedArticles = [];
        this.coverError = false;
        this.subscribe = false;
        window.scrollTo(0, 0);
        this.getArticle(localStorage.getItem("article_password_" + val));
      }
    },
    created() {
      if (!this.$common.isEmpty(this.id)) {
        this.getArticle(localStorage.getItem("article_password_" + this.id));

        if ("0" !== localStorage.getItem("showSubscribe")) {
          this.$notify({
            title: '文章订阅',
            type: 'success',
            message: '点击文章下方小手 - 订阅/取消订阅专栏（标签）',
            duration: 0,
            onClose: () => localStorage.setItem("showSubscribe", "0")
          });
        }
      }
    },
    mounted() {
      window.addEventListener("scroll", this.onScrollPage);
    },
    unmounted() {
      window.removeEventListener("scroll", this.onScrollPage);
    },
    watch: {},
    methods: {
      clickTocButton() {
        let display = $(".post-aside");
        if ("none" === display.css("display")) {
          $(".toc").css("display", "block");
          display.css("display", "block");
        } else {
          display.css("display", "none");
        }
      },
      scrollToTop() {
        window.scrollTo({top: 0, behavior: "smooth"});
      },
      formatDate(value) {
        return value ? String(value).slice(0, 10) : "";
      },
      getRelatedArticles() {
        if (this.$common.isEmpty(this.article.id)) {
          return;
        }
        this.relatedArticles = [];
        const merge = (records) => {
          (records || []).forEach((item) => {
            if (this.relatedArticles.length >= 3) {
              return;
            }
            if (item.id === this.article.id) {
              return;
            }
            if (this.relatedArticles.some((article) => article.id === item.id)) {
              return;
            }
            this.relatedArticles.push(item);
          });
        };
        const params = {current: 1, size: 4};
        if (!this.$common.isEmpty(this.article.labelId)) {
          params.labelId = this.article.labelId;
        }
        this.$http.post(this.$constant.baseURL + "/article/listArticle", params)
          .then((res) => {
            merge(res.data && res.data.records);
            if (this.relatedArticles.length < 3 && !this.$common.isEmpty(this.article.sortId)) {
              return this.$http.post(this.$constant.baseURL + "/article/listArticle", {
                current: 1,
                size: 6,
                sortId: this.article.sortId
              });
            }
            return null;
          })
          .then((res) => {
            if (res) {
              merge(res.data && res.data.records);
            }
          })
          .catch(() => {
          });
      },
      openArticle(id) {
        if (id === this.article.id) {
          return;
        }
        this.$router.push("/article/" + id);
      },
      subscribeLabel() {
        if (this.$common.isEmpty(this.$store.state.currentUser)) {
          this.$message({
            message: "请先登录！",
            type: "error"
          });
          return;
        }

        this.$confirm('确认' + (this.subscribe ? '取消订阅' : '订阅') + '专栏【' + this.article.label.labelName + '】？' + (this.subscribe ? "" : "订阅专栏后，该专栏发布新文章将通过邮件通知订阅用户。"), this.subscribe ? "取消订阅" : "文章订阅", {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/user/subscribe", {
            labelId: this.article.labelId,
            flag: !this.subscribe
          })
            .then((res) => {
              if (!this.$common.isEmpty(res.data)) {
                this.$store.commit("loadCurrentUser", res.data);
              }
              this.subscribe = !this.subscribe;
            })
            .catch((error) => {
              this.$message({
                message: error.message,
                type: "error"
              });
            });
        }).catch(() => {
          this.$message({
            type: 'success',
            message: '已取消!'
          });
        });
      },
      submitPassword() {
        if (this.$common.isEmpty(this.password)) {
          this.$message({
            message: "请先输入密码！",
            type: "error"
          });
          return;
        }

        this.getArticle(this.password);
      },
      deleteTreeHole(id) {
        if (this.$common.isEmpty(this.$store.state.currentUser)) {
          this.$message({
            message: "请先登录！",
            type: "error"
          });
          return;
        }

        this.$confirm('确认删除？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/weiYan/deleteWeiYan", {id: id})
            .then((res) => {
              this.$message({
                type: 'success',
                message: '删除成功!'
              });
              this.getNews();
            })
            .catch((error) => {
              this.$message({
                message: error.message,
                type: "error"
              });
            });
        }).catch(() => {
          this.$message({
            type: 'success',
            message: '已取消删除!'
          });
        });
      },
      submitWeiYan(content) {
        let weiYan = {
          content: content,
          createTime: this.newsTime,
          source: this.article.id
        };

        this.$http.post(this.$constant.baseURL + "/weiYan/saveNews", weiYan)
          .then((res) => {
            this.weiYanDialogVisible = false;
            this.newsTime = "";
            this.getNews();
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      getNews() {
        this.$http.post(this.$constant.baseURL + "/weiYan/listNews", {
          current: 1,
          size: 9999,
          source: this.article.id
        })
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              res.data.records.forEach(c => {
                c.content = c.content.replace(/\n{2,}/g, '<div style="height: 12px"></div>');
                c.content = c.content.replace(/\n/g, '<br/>');
                c.content = this.$common.faceReg(c.content);
                c.content = this.$common.pictureReg(c.content);
              });
              this.treeHoleList = res.data.records;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      onScrollPage() {
        this.scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        if (this.scrollTop < (window.innerHeight / 4)) {
          $(".toc").css("top", window.innerHeight / 4);
        } else {
          $(".toc").css("top", "90px");
        }
      },
      getTocbot() {
        let script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = this.$constant.tocbot;
        document.getElementsByTagName('head')[0].appendChild(script);

        // 引入成功
        script.onload = function () {
          tocbot.init({
            tocSelector: '#toc',
            contentSelector: '.entry-content',
            headingSelector: 'h1, h2, h3, h4, h5',
            scrollSmooth: true,
            fixedSidebarOffset: 'auto',
            scrollSmoothOffset: -100,
            hasInnerContainers: false
          });
        }
        if (this.$common.mobile()) {
          $(".toc").css("display", "none");
        }
      },
      addId() {
        let headings = $(".entry-content").find("h1, h2, h3, h4, h5, h6");
        headings.attr('id', (i, id) => id || 'toc-' + i);
      },
      getArticle(password) {
        this.$http.get(this.$constant.baseURL + "/article/getArticleById", {id: this.id, password: password})
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.article = res.data;
              this.coverError = false;
              this.getNews();
              // html:true 让正文里的 <audio>/<video>/<u>/<sub>/<sup> 等按标签渲染，
              // 渲染结果随后统一走 sanitizeHtml 白名单过滤（含音视频地址协议校验）
              const md = new MarkdownIt({breaks: true, html: true}).use(multimdTable).use(taskListPlugin).use(emojiPlugin);
              // 渲染结果统一净化后再交给 v-html，避免正文里的 HTML 注入
              this.articleContentHtml = this.$common.sanitizeHtml(md.render(this.article.articleContent));
              this.$nextTick(() => {
                this.$common.imgShow(".entry-content img");
                this.highlight();
                this.addId();
                this.getTocbot();
              });
              this.getRelatedArticles();
              if (!this.$common.isEmpty(password)) {
                localStorage.setItem("article_password_" + this.id, password);
              }
              this.showPasswordDialog = false;
              if (!this.$common.isEmpty(this.$store.state.currentUser) && !this.$common.isEmpty(this.$store.state.currentUser.subscribe)) {
                this.subscribe = JSON.parse(this.$store.state.currentUser.subscribe).includes(this.article.labelId);
              }
            }
          })
          .catch((error) => {
            if ("密码错误" === error.message.substr(0, 4)) {
              if (!this.$common.isEmpty(password)) {
                localStorage.removeItem("article_password_" + this.id);
                this.$message({
                  message: "密码错误，请重新输入！",
                  type: "error",
                  customClass: "message-index"
                });
              }
              this.tips = error.message.substr(4);
              this.showPasswordDialog = true;
            } else {
              this.$message({
                message: error.message,
                type: "error",
                customClass: "message-index"
              });
              this.tips = error.message;
              this.showPasswordDialog = true;
            }
          });
      },
      highlight() {
        let attributes = {
          autocomplete: "off",
          autocorrect: "off",
          autocapitalize: "off",
          spellcheck: "false",
          contenteditable: "false"
        };

        $("pre").each(function (i, item) {
          let preCode = $(item).children("code");
          let classNameStr = preCode[0].className;
          let classNameArr = classNameStr.split(" ");

          let lang = "";
          classNameArr.some(function (className) {
            if (className.indexOf("language-") > -1) {
              lang = className.substring(className.indexOf("-") + 1, className.length);
              return true;
            }
          });

          // 检测语言是否存在，不存在则自动检测
          let language = hljs.getLanguage(lang.toLowerCase());
          if (language === undefined) {
            // 启用自动检测
            let autoLanguage = hljs.highlightAuto(preCode.text());
            preCode.removeClass("language-" + lang);
            lang = autoLanguage.language;
            if (lang === undefined) {
              lang = "java";
            }
            preCode.addClass("language-" + lang);
          } else {
            lang = language.name;
          }

          $(item).addClass("highlight-wrap");
          $(item).attr(attributes);
          preCode.attr("data-rel", lang.toUpperCase()).addClass(lang.toLowerCase());
          // 启用代码高亮
          hljs.highlightBlock(preCode[0]);
          // 启用代码行号
          hljs.lineNumbersBlock(preCode[0]);
        });

        $("pre code").each(function (i, block) {
          $(block).attr({
            id: "hljs-" + i,
          });

          $(block).after(
            '<a class="copy-code" href="javascript:" data-clipboard-target="#hljs-' +
            i +
            '"><i class="fa fa-clipboard" aria-hidden="true"></i></a>'
          );
          new ClipboardJS(".copy-code");
        });

        if ($(".entry-content").children("table").length > 0) {
          $(".entry-content")
            .children("table")
            .wrap("<div class='table-wrapper'></div>");
        }
      }
    }
  }
</script>

<style scoped>

  /* ===== 文章页：白底、无卡片正文、右下浮动目录 ===== */
  .post-page {
    --postPageBg: #ffffff;
    --postText: #364153;
    --postMeta: #808080;
    --postLine: #16181c;
    --postSoftLine: rgba(0, 0, 0, 0.09);
    --postIntroBg: #f6f6f6;
    --postDrawerBg: #ffffff;
    --postSubtleBg: #f3f3f3;
    --postAccent: #16181c;
    --postViews: oklch(62% 0.19 145);
    --postComments: oklch(62% 0.19 250);
    background: var(--postPageBg);
    padding: 90px 20px 60px;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    min-height: 100vh;
  }

  html.dark .post-page {
    --postPageBg: #0d0d0d;
    --postText: #d1d5dc;
    --postMeta: #99a1af;
    --postLine: #ededed;
    --postSoftLine: rgba(255, 255, 255, 0.13);
    --postIntroBg: #1c1c1c;
    --postDrawerBg: #161616;
    --postSubtleBg: #202020;
    --postAccent: #ededed;
  }

  .post-main {
    width: 100%;
    max-width: 1008px;
    min-width: 0;
    padding: 0 40px;
    box-sizing: border-box;
  }

  /* 面包屑 */
  .post-crumb {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 18px;
    font-size: 0.78rem;
    line-height: 1.5;
    color: var(--postMeta);
  }

  .post-crumb-icon {
    display: inline-flex;
    align-items: center;
    color: var(--postMeta);
  }

  .post-crumb-link {
    cursor: pointer;
    transition: color 0.15s;
  }

  .post-crumb-link:hover {
    color: var(--postText);
  }

  .post-crumb-sep {
    opacity: 0.55;
  }

  .post-crumb-pill {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    padding: 1px 8px;
    border: 1px solid var(--postSoftLine);
    border-radius: 999px;
    background: var(--postIntroBg);
    line-height: 1.7;
    color: var(--postText);
    cursor: pointer;
    transition: background 0.2s;
  }

  .post-crumb-pill:hover {
    background: var(--postSubtleBg);
  }

  .post-crumb-current {
    color: var(--postMeta);
  }

  /* 标题与元信息 */
  .post-hero {
    padding: 0.25rem 0 0.75rem;
    text-align: center;
  }

  .post-hero-title {
    margin: 0 0 0.75rem;
    font-size: 1.875rem;
    font-weight: 700;
    line-height: 1.3;
    color: var(--postText);
    word-break: break-word;
  }

  .post-hero-meta,
  .post-hero-extra {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 0.8rem;
    line-height: 1.6;
    color: var(--postMeta);
  }

  .post-hero-extra {
    margin-top: 4px;
  }

  .post-meta-dot {
    opacity: 0.55;
  }

  .post-meta-tag {
    color: var(--postText);
  }

  .post-meta-tag--link {
    cursor: pointer;
    transition: color 0.2s ease;
  }

  .post-meta-tag--link:hover {
    color: var(--ui-accent);
  }

  .post-views {
    color: var(--postViews);
    font-weight: 500;
  }

  .post-comments-count {
    color: var(--postComments);
  }

  /* 简介卡：更新提示 + AI 摘要 + 封面 */
  .post-intro-card {
    margin: 1.25rem 0 1.5rem;
    border: 1px solid var(--postSoftLine);
    border-radius: 0.75rem;
    background: var(--postIntroBg);
    overflow: hidden;
  }

  .post-intro-outdated {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.7rem 1.125rem;
    color: var(--postMeta);
  }

  .post-intro-outdated svg {
    flex: 0 0 auto;
    color: #ef4444;
  }

  .post-intro-outdated-text {
    font-size: 0.8rem;
    line-height: 1.5;
  }

  .post-intro-divider {
    height: 1px;
    background: var(--postSoftLine);
  }

  .post-intro-details {
    display: block;
  }

  .post-intro-summary {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
    min-height: 2.75rem;
    padding: 0.625rem 1.125rem;
    font-size: 0.9rem;
    font-weight: 600;
    line-height: 1.5;
    color: var(--postText);
    cursor: pointer;
    list-style: none;
    user-select: none;
  }

  .post-intro-summary::-webkit-details-marker {
    display: none;
  }

  .post-intro-summary-title {
    display: inline-flex;
    align-items: center;
    gap: 0.45rem;
  }

  .post-intro-summary-title svg {
    width: 1.125rem;
    height: 1.125rem;
    color: var(--postAccent);
  }

  .post-intro-model {
    font-size: 0.75rem;
    font-weight: 500;
    color: var(--postMeta);
  }

  .post-intro-arrow {
    flex: 0 0 auto;
    width: 1.25rem;
    height: 1.25rem;
    color: var(--postMeta);
    transition: transform 0.2s;
  }

  .post-intro-details[open] .post-intro-arrow {
    transform: rotate(180deg);
  }

  .post-intro-section {
    padding: 0 1.125rem 0.875rem;
  }

  .post-intro-desc {
    margin: 0;
    font-size: 0.85rem;
    line-height: 1.6;
    color: var(--postMeta);
  }

  .post-intro-cover {
    overflow: hidden;
    border-radius: 0.5rem;
  }

  .post-intro-cover-img {
    display: block;
    width: 100%;
    max-height: 480px;
    border-radius: 0.5rem;
    overflow: hidden;
  }

  .post-intro-cover-img :deep(img) {
    display: block;
    width: 100%;
    height: auto;
    max-height: 480px;
    object-fit: cover;
  }

  .post-intro-cover-fallback {
    height: 240px;
    background: var(--postSubtleBg);
  }

  /* 正文 */
  .post-body {
    padding-top: 0.5rem;
  }

  .post-video {
    margin-bottom: 20px;
  }

  /* 正文排版统一由 src/assets/css/markdown-highlight.css 提供（照搬 halo-theme-joe2.0），
     本组件不再覆盖 rich text 样式，避免与全局排版规则层叠打架 */

  /* 相关文章 */
  .post-related {
    margin-top: 2.5rem;
  }

  .post-related-head {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin: 0 0 0.75rem;
    padding-bottom: 0.5rem;
    border-bottom: 1px solid var(--postSoftLine);
    font-size: 1.05rem;
    font-weight: 600;
    line-height: 1.4;
    color: var(--postText);
  }

  .post-related-head svg {
    flex-shrink: 0;
    width: 1.25rem;
    height: 1.25rem;
  }

  .post-related-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0.75rem;
    margin: 0;
    padding: 0;
  }

  .post-related-item {
    display: flex;
    min-width: 0;
  }

  .post-related-card {
    position: relative;
    display: block;
    width: 100%;
    overflow: hidden;
    border-radius: 0.75rem;
    background: var(--postSubtleBg);
    cursor: pointer;
  }

  .post-related-media {
    position: relative;
    display: block;
    width: 100%;
    aspect-ratio: 16 / 10;
    max-height: 16rem;
    background: var(--postSubtleBg);
  }

  .post-related-media img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .post-related-scrim {
    position: absolute;
    inset: 0;
    pointer-events: none;
    background: linear-gradient(rgba(0, 0, 0, 0) 45%, rgba(0, 0, 0, 0.55));
  }

  .post-related-body {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    flex-direction: column;
    gap: 0.15rem;
    padding: 0.6rem 0.75rem;
    color: #ffffff;
  }

  .post-related-name {
    font-size: 0.92rem;
    font-weight: 600;
    line-height: 1.35;
  }

  .post-related-meta {
    font-size: 0.74rem;
    color: rgba(255, 255, 255, 0.8);
  }

  .post-related-card:hover .post-related-name {
    text-decoration: underline;
  }

  @media screen and (min-width: 768px) {
    .post-related-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  /* 作者与订阅 */
  .post-end {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    flex-wrap: wrap;
    margin-top: 2.5rem;
    padding-top: 1.25rem;
    border-top: 1px solid var(--postSoftLine);
  }

  .post-end-info {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.4rem 0.9rem;
    font-size: 0.82rem;
    line-height: 1.6;
    color: var(--postMeta);
  }

  .post-end-link {
    color: var(--postText);
    text-decoration: underline;
    text-underline-offset: 3px;
    cursor: pointer;
  }

  .post-subscribe {
    display: inline-flex;
    align-items: center;
    gap: 0.4rem;
    flex-shrink: 0;
    padding: 0.4rem 1rem;
    border: 1px solid var(--postAccent);
    border-radius: 999px;
    background: transparent;
    color: var(--postText);
    font-size: 0.82rem;
    cursor: pointer;
    transition: all 0.2s;
  }

  .post-subscribe:hover,
  .post-subscribe--active {
    background: var(--postAccent);
    color: var(--postPageBg);
  }

  /* 剪刀分割线 */
  .post-cut {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin: 2.5rem 0 1.25rem;
    color: var(--postMeta);
  }

  .post-cut::before,
  .post-cut::after {
    content: '';
    flex: 1;
    border-top: 1px dashed var(--postSoftLine);
  }

  .post-cut span {
    font-size: 1rem;
    line-height: 1;
    opacity: 0.8;
  }

  /* 评论区：组件与样式保持原样；宽度与参考站一致，铺满文章栏（正文左右各缩进 40px） */
  .post-comments {
    margin-top: 0;
    margin-left: -40px;
    margin-right: -40px;
  }

  /* 最新进展 */
  .process-wrap {
    margin: 0 0 32px;
  }

  .process-wrap hr {
    position: relative;
    margin: 10px auto 60px;
    border: 2px dashed var(--lightGreen);
    overflow: visible;
  }

  .process-wrap hr:before {
    position: absolute;
    top: -14px;
    left: 5%;
    color: var(--lightGreen);
    content: '❄';
    font-size: 30px;
    line-height: 1;
    transition: all 1s ease-in-out;
  }

  .process-wrap hr:hover:before {
    left: calc(95% - 20px);
  }

  .process-wrap :deep(.el-collapse-item__header) {
    border-bottom: unset;
    font-size: 20px;
    background-color: transparent;
    color: var(--lightGreen);
  }

  .process-wrap :deep(.el-collapse-item__wrap) {
    background-color: transparent;
  }

  .process-wrap .el-collapse {
    border-top: unset;
    border-bottom: unset;
  }

  .password-content {
    font-size: 13px;
    color: var(--maxGreyFont);
    line-height: 1.5;
  }

  .copyright-container {
    color: var(--fontColor);
    line-height: 2.5;
    padding: 0 30px 10px;
    font-size: 16px;
  }

  /* ===== 右下角目录抽屉 ===== */
  .post-aside {
    position: fixed;
    right: 18px;
    bottom: 144px;
    z-index: 120;
    display: none;
    width: 352px;
    max-width: calc(100vw - 36px);
    max-height: min(70vh, 560px);
    overflow-y: auto;
    padding: 18px 18px 16px;
    background: var(--postDrawerBg);
    border: 1.6px solid var(--postLine);
    border-radius: 12px;
    box-shadow: 6px 6px 0 rgba(0, 0, 0, 0.08);
  }

  html.dark .post-aside {
    box-shadow: 6px 6px 0 rgba(0, 0, 0, 0.45);
  }

  .toc {
    position: static;
    top: auto;
    right: auto;
    width: 100%;
    padding: 0;
    background: transparent;
    border: none;
    box-shadow: none;
    color: var(--postMeta);
    font-size: 0.82rem;
  }

  .toc::before {
    content: '目录';
    display: block;
    margin: 0 0 0.75rem;
    font-size: 0.9rem;
    font-weight: 700;
    line-height: 1.4;
    color: var(--postText);
  }

  .toc :deep(.toc-list) {
    margin: 0;
    padding-left: 0 !important;
    list-style: none;
  }

  .toc :deep(.toc-list .toc-list) {
    margin-top: 0.15rem;
    padding-left: 0.9rem !important;
  }

  .toc :deep(.toc-link) {
    display: block;
    padding: 0.2rem 0;
    border: none;
    line-height: 1.45;
    color: var(--postMeta);
    transition: color 0.15s;
  }

  .toc :deep(.toc-link:hover) {
    color: var(--postText);
  }

  .toc :deep(.is-active-link) {
    padding: 0;
    background: transparent;
    color: var(--postText) !important;
    font-weight: 600;
  }

  .toc :deep(.is-active-link::before) {
    background-color: var(--postText);
  }

  /* 右下角浮动按钮：与参考站 ud-dock 一致 */
  .post-float {
    position: fixed;
    right: 84px;
    bottom: 20px;
    z-index: 121;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 0.75rem;
  }

  .post-float-btn {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 3.125rem;
    height: 3.125rem;
    flex-shrink: 0;
    padding: 0;
    background: var(--postPageBg);
    border: 2px solid var(--postText);
    border-radius: 1.25rem;
    color: var(--postText);
    font-size: 1.5rem;
    line-height: 1;
    cursor: pointer;
    overflow: hidden;
    transition: background-color 0.2s, color 0.2s, border-color 0.2s;
  }

  .post-float-btn:hover {
    background: var(--postText);
    border-color: var(--postPageBg);
    color: var(--postPageBg);
  }

  .post-float-btn:active {
    transform: scale(0.96);
  }

  @media screen and (min-width: 768px) {
    .post-intro-cover-img,
    .post-intro-cover-img :deep(img) {
      max-height: 560px;
    }
  }

  @media screen and (max-width: 767px) {
    .post-page {
      padding: 78px 0 40px;
    }

    .post-main {
      padding: 0 18px;
    }
    .post-comments {
      margin-left: -18px;
      margin-right: -18px;
    }
    .post-hero-title {
      font-size: 1.5rem;
    }

    .post-intro-card {
      margin: 1rem 0 1.25rem;
    }

    .post-end {
      align-items: flex-start;
      flex-direction: column;
    }

    .post-aside {
      right: 12px;
      bottom: 134px;
      width: calc(100vw - 24px);
      max-width: 352px;
    }

    .post-float {
      right: 56px;
      bottom: 12px;
    }
  }
</style>
