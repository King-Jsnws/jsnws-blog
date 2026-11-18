<template>
  <div>
    <div class="message-hero">
      <!-- 背景层：与首页导航容器使用同一张 --toolbar 图、同样的 cover 居中与同一套遮罩变量 -->
      <div class="message-background my-animation-hero-fade" aria-hidden="true"></div>
      <!-- 输入框：发射按钮内嵌在胶囊右端，聚焦/有内容时淡入 -->
      <div class="message-in my-animation-hero-fade">
        <div class="message-input-wrap">
          <!-- 复用登录/注册页那套浮动标签：标签上浮压在上边框、缺口按标签宽度真实挖空 -->
          <FloatInput input-id="message-input"
                      v-model="messageContent"
                      label="留下点什么啦~">
            <input id="message-input"
                   class="message-input"
                   type="text"
                   v-model="messageContent"
                   @click="show = true"
                   @focus="show = true"
                   @input="onMessageInput"
                   @blur="onMessageBlur"
                   @keyup.enter="submitMessage"
                   maxlength="60"/>
          </FloatInput>
          <button class="message-send"
                  :class="{ 'is-visible': show }"
                  @mousedown.prevent
                  @click="submitMessage">发射
          </button>
        </div>
      </div>
      <!-- 弹幕 -->
      <div class="barrage-container">
        <Barrage :barrage-list="barrageList" :loop="true" />
      </div>
    </div>
    <div class="comment-wrap my-animation-slide-bottom">
      <div class="comment-content">
        <!-- 留言规则 + 留言输入栏：共用一个外框合成一整块（上两角在规则卡、下两角在输入区），
             接缝处用一条细分隔线区分层级，两块不再各自画四边框 -->
        <div class="message-panel">
          <div class="message-rules">
            <div class="message-rules-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="17" height="17" aria-hidden="true">
                <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="2"/>
                <path fill="currentColor" d="M11 10.5h2V17h-2zM11 7h2v2.2h-2z"/>
              </svg>
            </div>
            <div class="message-rules-body">
              <div class="message-rules-title">留言规则</div>
              <ul class="message-rules-list">
                <li>请文明发言，禁止发布违法、违规、广告类内容</li>
                <li>留言内容支持 300 字以内，可回复他人留言</li>
                <li>系统会自动记录留言 IP 及地理位置信息用于安全审核</li>
                <li>遵守社区规范，共同维护友好的交流环境</li>
              </ul>
            </div>
          </div>
          <!-- 留言输入栏：复用 Comment 内部的留言提交逻辑 -->
          <div class="message-bar">
            <span class="identity-v-wrap">
              <div class="message-bar-avatar">
                <img v-if="userAvatar" :src="userAvatar" alt=""/>
                <span v-else>匿</span>
              </div>
              <span class="identity-v-badge" v-if="userHasIdentity" title="认证身份">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
              </span>
            </span>
            <!-- 输入框：表情按钮绝对定位在胶囊内部右端，与树洞输入框的「发射」按钮同一套做法 -->
            <div class="message-bar-input-wrap">
              <FloatInput input-id="message-bar-input"
                          v-model="messageText"
                          label="说点什么，留下你的足迹...">
                <input id="message-bar-input"
                       ref="messageInput"
                       class="message-bar-input"
                       v-model="messageText"
                       maxlength="300"
                       @keyup.enter="publishMessage"/>
              </FloatInput>
              <!-- 表情：复用站点已内置的 Font Awesome 笑脸图标与 CommentBox 的点选交互 -->
              <el-popover v-model:visible="showEmoji"
                          :width="316"
                          placement="bottom-end"
                          trigger="click"
                          :show-arrow="false"
                          :offset="10"
                          popper-class="message-emoji-popper">
                <template #reference>
                  <button class="message-bar-emoji" type="button" aria-label="表情"
                          @click="rememberCaret">
                    <i class="fa fa-smile-o"></i>
                  </button>
                </template>
                <EmojiPanel @select="addEmoji"/>
              </el-popover>
            </div>
            <div class="message-bar-actions">
              <span v-if="!isLogin" class="message-bar-login" @click="$router.push('/user')">登录</span>
              <span class="message-bar-count">{{ messageText.length }}/300</span>
              <button class="message-bar-publish" @click="publishMessage">发送</button>
            </div>
          </div>
          <!-- 全部留言：Comment 内部已用 v-if="comments.length > 0" 控制空态显隐，
               外层不再额外加 v-show 避免与异步加载形成竞态 -->
          <div class="message-comments">
            <Comment ref="comment" :source="$constant.source" :type="'message'" :userId="$constant.userId"
                     :hideCommentInput="true" :cardList="true" @changeTotal="commentTotal = $event"></Comment>
          </div>
        </div>
      </div>
      <MyFooter></MyFooter>
    </div>
  </div>
</template>

<script>
  import Barrage from "../components/common/Barrage.vue";
  import FloatInput from "../components/common/FloatInput.vue";

  import Comment from "../components/comment/Comment.vue";
  import EmojiPanel from "../components/comment/EmojiPanel.vue";
  import MyFooter from "../components/common/MyFooter.vue";

  export default {
    components: {
      Barrage,
      Comment,
      EmojiPanel,
      FloatInput,
      MyFooter
    },
    data() {
      return {
        show: false,
        messageContent: "",
        // 留言输入栏内容
        messageText: "",
        // 表情面板开关与插入位置（点击表情按钮时记录，面板关闭后继续在末尾追加）
        showEmoji: false,
        emojiCaret: null,
        // 留言总数，由 Comment 组件同步，用于把列表并入卡片时控制显隐
        commentTotal: 0,
        // background: {"background": "url(" + this.$store.state.webInfo.backgroundImage + ") center center / cover no-repeat"},
        barrageList: []
      };
    },
    computed: {
      isLogin() {
        return !this.$common.isEmpty(this.$store.state.currentUser);
      },
      userAvatar() {
        if (!this.isLogin) {
          return "";
        }
        return this.$store.state.currentUser.avatar || "";
      },
      // 头像认证角标：主理人 / AI 站长 / 管理员才显示
      userHasIdentity() {
        if (!this.isLogin) {
          return false;
        }
        const user = this.$store.state.currentUser;
        const type = Number(user.userType);
        return user.isBoss === true || type === 1 || type === 2 || type === 5;
      }
    },
    created() {
      this.getTreeHole();
    },
    methods: {
      // 打开表情面板前记录光标位置，表情插入到光标处
      rememberCaret() {
        const input = this.$refs.messageInput;
        this.emojiCaret = input ? input.selectionStart : this.messageText.length;
      },
      // 插入表情：面板抛出的就是 Waline 语法 token，走 v-model 同步，字数统计与 300 字上限一并生效
      addEmoji(token) {
        const length = this.messageText.length;
        const caret = this.emojiCaret === null || this.emojiCaret > length
          ? length : this.emojiCaret;
        const next = this.messageText.slice(0, caret) + token + this.messageText.slice(caret);
        if (next.length > 300) {
          this.$message({
            message: "留言内容不能超过300字！",
            type: "warning"
          });
          return;
        }
        this.messageText = next;
        this.emojiCaret = caret + token.length;
      },
      // 发布留言：未登录也可直接发送，服务端会按匿名规则写入昵称与随机头像
      publishMessage() {
        if (this.messageText.trim() === "") {
          this.$message({
            message: "你还没写呢~",
            type: "warning"
          });
          return;
        }

        this.$refs.comment.submitComment(this.messageText.trim());
        this.messageText = "";
      },
      getTreeHole() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listTreeHole")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              res.data.forEach(m => {
                this.barrageList.push({
                  id: m.id,
                  avatar: m.avatar,
                  nickname: m.nickname,
                  msg: m.message,
                  time: Math.floor(Math.random() * 5 + 10)
                });
              });
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      // 树洞输入框：有内容才显示内嵌的发射按钮
      onMessageInput() {
        this.show = this.messageContent.trim().length > 0;
      },
      // 失焦时若没有内容则收起按钮，避免空输入时按钮常驻
      onMessageBlur() {
        if (this.messageContent.trim() === "") {
          this.show = false;
        }
      },
      submitMessage() {
        if (this.messageContent.trim() === "") {
          this.$message({
            message: "你还没写呢~",
            type: "warning"
          });
          return;
        }

        let treeHole = {
          message: this.messageContent.trim()
        };

        if (!this.$common.isEmpty(this.$store.state.currentUser) && !this.$common.isEmpty(this.$store.state.currentUser.avatar)) {
          treeHole.avatar = this.$store.state.currentUser.avatar;
        }


        this.$http.post(this.$constant.baseURL + "/webInfo/saveTreeHole", treeHole)
          .then((res) => {
            this.$message({
              type: "success",
              message: "发送成功"
            });
            if (!this.$common.isEmpty(res.data)) {
              this.barrageList.push({
                id: res.data.id,
                avatar: res.data.avatar,
                nickname: res.data.nickname,
                msg: res.data.message,
                time: Math.floor(Math.random() * 5 + 10)
              });
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });

        this.messageContent = "";
        this.show = false;
      }
    }
  }
</script>

<style scoped>

  /* 首屏背景区域：高度与背景层一致，作为 message-in 的定位参照 */
  .message-hero {
    position: relative;
    height: 100vh;
    height: 100dvh;
  }

  .message-background {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    height: 100dvh;
    z-index: -1;
    /* 与首页导航栏 ::before 完全一致：同一张 --toolbar 图、cover 居中 + 主题遮罩变量；
       末尾一层是首页默认背景图，静态资源前缀未配置时兜底，保证深浅模式下都有底图可读 */
    background: linear-gradient(var(--heroMask), var(--heroMask)),
                var(--toolbar, url('/images/home-default-bg.webp')) center center / cover no-repeat,
                url('/images/home-default-bg.webp') center center / cover no-repeat;
  }

  /* 贴在背景区域偏下位置：距下沿留出空隙，水平居中，随页面滚动而非固定悬浮 */
  .message-in {
    position: absolute;
    left: 50%;
    bottom: 70px;
    transform: translateX(-50%);
    color: var(--white);
    width: min(360px, calc(100% - 32px));
    z-index: 10;
  }

  /* 输入胶囊：保持原宽度（外层容器 70%），发射按钮绝对定位嵌在右端 */
  .message-input-wrap {
    position: relative;
    width: 70%;
    margin: 0 auto;
  }

  /* 浮动标签：复用 FloatInput 的结构与 mask 缺口，只按树洞胶囊的规格覆盖变量 */
  .message-input-wrap :deep(.float-field) {
    /* 胶囊端头：圆角取到高度一半，形成完整半圆 */
    --float-radius: 999px;
    /* 缺口起点让开左侧半圆（约为半高 + 余量），保证缺口落在平直的上边框段上 */
    --float-notch-left: 20px;
    --float-border-color: var(--white);
    --float-label-color: rgba(255, 255, 255, 0.92);
    --float-label-active-color: var(--white);
    --float-accent-color: var(--white);
    --float-margin-bottom: 0;
  }

  .message-input {
    display: block;
    box-sizing: border-box;
    width: 100%;
    outline: none;
    /* 边框交给 FloatInput 自绘（.float-border），原生边框会在标签处透出线 */
    border: none;
    border-radius: 999px;
    color: var(--white);
    background: var(--transparent);
    /* 左侧留出半圆端头的空间，文字不贴圆弧；右侧预留按钮空间，按钮出现时文字不被遮挡 */
    padding: 10px 72px 10px 26px;
  }

  /* 半透明浅色胶囊按钮：默认隐藏，聚焦/有内容时淡入 */
  .message-send {
    position: absolute;
    top: 50%;
    right: 5px;
    transform: translateY(-50%);
    padding: 6px 12px;
    /* 描边与圆角取自参考图：1px 实线纯白、两端全圆角胶囊，底色与文字样式保持不变 */
    border: 1px solid #fff;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.26);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.22);
    color: var(--white);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    backdrop-filter: blur(6px);
    -webkit-backdrop-filter: blur(6px);
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    transition: opacity 0.22s ease, visibility 0.22s ease,
                border-color 0.22s ease, box-shadow 0.22s ease, background-color 0.22s ease;
  }

  .message-send.is-visible {
    opacity: 1;
    visibility: visible;
    pointer-events: auto;
  }

  /* 弹幕发射键不参与前台按钮统一：悬停只提亮底色，描边保持纯白，不做虚线描边与按压缩放 */
  .message-send:hover {
    border-color: #fff;
    background: rgba(255, 255, 255, 0.42);
  }

  .barrage-container {
    position: absolute;
    top: 50px;
    left: 0;
    right: 0;
    bottom: 0;
    height: calc(100% - 50px);
    width: 100%;
    user-select: none;
    overflow: hidden;
  }

  .comment-wrap {
    background: var(--ui-page-bg);
    position: absolute;
    top: 100vh;
    width: 100%;
  }

  /* 规则卡 + 输入栏共用的外框：改用全站统一卡片外壳 */
  .message-panel {
    width: 100%;
    margin-bottom: var(--ui-page-gap);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
    overflow: hidden;
  }

  /* 留言输入栏：底色交给共用外框（透明），只用一条细分隔线与上面的规则卡分层；
     色值取自设计图采样，变量仅在此卡内生效 */
  .message-bar {
    --bar-login: #0db7be;
    --bar-avatar: #0f9f91;
    --bar-count: #64748b;
    display: flex;
    align-items: center;
    gap: 12px;
    box-sizing: border-box;
    width: 100%;
    /* 骑线标签盒上沿在 field 顶边之上约 6.4px，14px 内边距仍留 7.6px 净空，不压顶边 */
    padding: 14px 18px;
    border-top: 1px solid rgba(0, 0, 0, 0.12);
    background: transparent;
  }

  html.dark .message-bar {
    --bar-count: #a8c4bf;
    border-top-color: rgba(255, 255, 255, 0.12);
  }

  .message-bar-avatar {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    overflow: hidden;
    background: var(--bar-avatar);
    color: #fff;
    font-size: 13px;
    line-height: 36px;
    text-align: center;
    user-select: none;
  }

  .message-bar-avatar img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  /* 输入框容器：作为表情按钮的定位参照 */
  .message-bar-input-wrap {
    position: relative;
    display: flex;
    flex: 1 1 0;
    min-width: 0;
  }

  /* 表情按钮：嵌在输入胶囊内部右端，垂直居中，不占用外部横向空间
     视觉对齐友链页 .friend-btn：透明底 + 中性灰实线，hover 转虚线 */
  .message-bar-emoji {
    position: absolute;
    top: 50%;
    right: 5px;
    transform: translateY(-50%);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 50%;
    background: transparent;
    color: var(--themeBackground);
    font-size: 17px;
    line-height: 1;
    cursor: pointer;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .message-bar-emoji:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    background: transparent;
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  /* 圆形图标按钮保留圆形，按压统一瞬时缩放 */
  .message-bar-emoji:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .message-bar-emoji {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .message-bar-emoji:hover,
  html.dark .message-bar-emoji:active {
    border-color: rgba(255, 255, 255, 0.4);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  .message-bar-input {
    display: block;
    box-sizing: border-box;
    width: 100%;
    height: 38px;
    /* 左侧留出半圆端头空间，文字不贴圆弧；右侧留出表情按钮宽度 + 间距 */
    padding: 0 42px 0 26px;
    border: none;
    border-radius: 999px;
    outline: none;
    background: #fff;
    color: #333;
    font-size: 14px;
  }

  /* 浮动标签：同一套 FloatInput，按留言输入栏的胶囊规格覆盖变量 */
  .message-bar-input-wrap :deep(.float-field) {
    flex: 1 1 0;
    min-width: 0;
    --float-radius: 999px;
    /* 缺口比标签盒左右各外扩 2px，起点后移 2px 保持实际开口不压到左侧半圆 */
    --float-notch-left: 22px;
    --float-border-color: rgba(17, 48, 44, 0.12);
    --float-label-color: #757575;
    --float-label-active-color: #333;
    --float-accent-color: #0db7be;
    --float-margin-bottom: 0;
  }

  html.dark .message-bar-input-wrap :deep(.float-field) {
    /* 胶囊输入框在深色下仍是白底，边框叠白底之上；
       用白色 alpha 会白叠白变成不可见，改成深色 alpha 才能看见静止态描边 */
    --float-border-color: rgba(17, 48, 44, 0.16);
    /* 上浮后标签落在深色输入栏底上，需要浅色文字与强调色 */
    --float-label-active-color: #e2e8f0;
    --float-accent-color: #7fe3d8;
  }

  /* 上浮标签：胶囊是白底实心块，原来的 fill 挖空会在白胶囊上开一个露出页面底的缺口；
     这里改为给标签铺一块与胶囊同色的白底，标签成为胶囊顶边向上延伸的一小截，
     白底本身不透明，不会再出现「挖掉一块不存在的底色」的破口 */
  .message-bar-input-wrap :deep(.float-field:focus-within .float-label),
  .message-bar-input-wrap :deep(.float-field.is-filled .float-label) {
    background: #fff;
    border-radius: 6px 6px 0 0;
    color: #4b5563;
  }

  .message-bar-actions {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    gap: 12px;
  }

  /* 未登录「登录」：透明底 + 中性灰实线，颜色与描边沿用友链页那一套 */
  .message-bar-login {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    height: 34px;
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    color: var(--themeBackground);
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

  .message-bar-login:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  .message-bar-login:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark .message-bar-login {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .message-bar-login:hover,
  html.dark .message-bar-login:active {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  .message-bar-count {
    color: var(--bar-count);
    font-size: 12px;
    white-space: nowrap;
    user-select: none;
  }

  /* 「发送」：去掉实心蓝，统一成透明底 + 中性灰实线 + 蓝字（hover 转虚线） */
  .message-bar-publish {
    box-sizing: border-box;
    height: 34px;
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    user-select: none;
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .message-bar-publish:hover:not(:disabled) {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    background: transparent;
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  /* 按压瞬时缩放 */
  .message-bar-publish:active:not(:disabled) {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  .message-bar-publish:disabled {
    background: transparent;
    border-color: rgba(120, 120, 120, 0.16);
    color: var(--themeBackground);
    opacity: 0.55;
    box-shadow: none;
    cursor: not-allowed;
  }

  html.dark .message-bar-publish {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark .message-bar-publish:hover:not(:disabled),
  html.dark .message-bar-publish:active:not(:disabled) {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  html.dark .message-bar-publish:disabled {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
  }

  @media screen and (max-width: 768px) {
    .message-bar {
      flex-wrap: wrap;
      padding: 12px 14px;
      gap: 10px;
    }

    .message-bar-input-wrap {
      flex: 1 1 calc(100% - 46px);
    }

    .message-bar-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }

  .comment-content {
    box-sizing: border-box;
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: 40px var(--ui-page-gutter) var(--ui-page-bottom);
  }

  /* 全部留言段：并入共用外框，底色透明，只用一条细分隔线与输入区分层 */
  .message-comments {
    box-sizing: border-box;
    width: 100%;
    padding: 16px 20px 20px;
    border-top: 1px solid rgba(0, 0, 0, 0.12);
    background: transparent;
  }

  html.dark .message-comments {
    border-top-color: rgba(255, 255, 255, 0.12);
  }

  /* 标题区与列表之间沿用同一条细分隔线，维持层次；
     用负外边距抵消父容器左右内边距，让分隔线通栏铺满到卡片边缘 */
  .message-comments :deep(.msg-list-title) {
    margin-left: -20px;
    margin-right: -20px;
    padding: 0 20px 10px;
    margin-bottom: 16px;
    border-bottom: 1px solid rgba(17, 48, 44, 0.07);
  }

  html.dark .message-comments :deep(.msg-list-title) {
    border-bottom-color: rgba(255, 255, 255, 0.08);
  }

  /* 留言规则：不再自带外框，四边描边与圆角交给外层 .message-panel；
     文字（标题 / 四条规则 / 图标里的「!」）统一用全站正文黑（--fontColor，浅色为 black），
     字号、字重、行高与内边距沿用原值。 */
  .message-rules {
    --rules-title: var(--fontColor);
    --rules-text: var(--fontColor);
    --rules-icon-bg: #e48608;
    --rules-icon-color: #000;
    display: flex;
    align-items: flex-start;
    box-sizing: border-box;
    width: 100%;
    padding: 15px 20px;
    background: transparent;
  }

  html.dark .message-rules {
    /* 深色主题正文亮色 #e6e6e6 */
    --rules-title: #e6e6e6;
    --rules-text: #e6e6e6;
    --rules-icon-bg: #e8912f;
    --rules-icon-color: #ffab4d;
  }

  .message-rules-icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    margin-top: 1px;
    color: var(--rules-icon-color);
  }

  .message-rules-body {
    min-width: 0;
    margin-left: 10px;
  }

  .message-rules-title {
    color: var(--rules-title);
    font-size: 17px;
    font-weight: 700;
    line-height: 22px;
  }

  .message-rules-list {
    margin: 6px 0 0;
    padding: 0;
    list-style: none;
    color: var(--rules-text);
    font-size: 14.5px;
    line-height: 1.8;
  }

  @media screen and (max-width: 768px) {
    .message-rules {
      padding: 14px 16px;
    }

    .message-rules-list {
      /* 窄屏再降半档：14.5 → 13.5，保证最长一条在 390 下整句单行不出现单字换行 */
      font-size: 13.5px;
      line-height: 1.75;
    }

    .message-comments {
      padding: 16px 14px 18px;
    }

    /* 窄屏下父容器左右内边距变小，负外边距同步调整 */
    .message-comments :deep(.msg-list-title) {
      margin-left: -14px;
      margin-right: -14px;
      padding-left: 14px;
      padding-right: 14px;
    }
  }
</style>

<!-- 表情面板通过 el-popover 传送到 body，需用非 scoped 样式贴合留言卡配色 -->
<style>
  /* 面板宽度由 el-popover 的 :width 控制（宽度是内联样式，CSS 里的 width 会被覆盖）；
     这里只兜住窄屏上限，格子用弹性列，宽度被压缩时也不会溢出底板 */
  .message-emoji-popper.el-popper {
    box-sizing: border-box;
    max-width: calc(100vw - 24px);
    padding: 10px;
    border: 1px solid #d8f1eb;
    border-radius: 14px;
    background: #f0fdfb;
    box-shadow: 0 12px 30px rgba(17, 48, 44, 0.14);
  }

  html.dark .message-emoji-popper.el-popper {
    background: #11302c;
    border-color: rgba(255, 255, 255, 0.10);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.45);
  }

  .message-emoji-popper .el-popper__arrow {
    display: none;
  }

  /* ===== 留言页局部覆盖：列表卡片按钮对齐友链页 .friend-btn =====
     Comment.vue 是站内公用组件，这里只在留言页的 .message-comments 容器内覆盖，
     用 #app 前缀提权（非 scoped 块里 :deep 不会被编译）；文章页评论卡片不受影响，
     也不改动全局 token 与 Friend.vue 自身样式。行内回复条是整块合并样式，不在此覆盖。 */
  #app .message-comments .msg-card-top-btn,
  #app .message-comments .msg-card-reply-btn {
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  #app .message-comments .msg-card-top-btn:hover,
  #app .message-comments .msg-card-reply-btn:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    background: transparent;
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  #app .message-comments .msg-card-top-btn:active,
  #app .message-comments .msg-card-reply-btn:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark #app .message-comments .msg-card-top-btn,
  html.dark #app .message-comments .msg-card-reply-btn {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark #app .message-comments .msg-card-top-btn:hover,
  html.dark #app .message-comments .msg-card-reply-btn:hover,
  html.dark #app .message-comments .msg-card-top-btn:active,
  html.dark #app .message-comments .msg-card-reply-btn:active {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  /* ===== 留言列表并入共用外框：去掉列表卡自身的底色 / 描边 / 圆角 =====
     整片列表透出页面背景，单条留言之间改用细分隔线区分；
     楼中楼同样透明，只保留一条细描边维持层级，避免「浅色块浮在透明底上」。 */
  #app .message-comments .msg-card-list {
    border: 0;
    border-radius: 0;
    background: transparent;
    overflow: visible;
  }

  #app .message-comments .msg-card {
    padding: 16px 0;
    border-bottom-color: rgba(0, 0, 0, 0.08);
  }

  #app .message-comments .msg-child {
    border-color: rgba(0, 0, 0, 0.06);
    background: transparent;
  }

  html.dark #app .message-comments .msg-card {
    border-bottom-color: rgba(255, 255, 255, 0.08);
  }

  html.dark #app .message-comments .msg-child {
    border-color: rgba(255, 255, 255, 0.09);
  }

  /* 楼中楼折叠入口：与同卡片上的「回复」按钮完全同款（同一套 1px 实线描边、圆角 10、
     6px 12px 内边距、14px/500 字重、蓝字、透明底与同套柔光阴影，悬停虚线加深、按压 scale .95）。
     用 flex 排版让文字在框内垂直居中；保留原有 width:fit-content + margin:2px auto 的水平居中，
     故用 block 级 flex 而非 inline-flex（inline 级会让 auto 外边距失效、入口左移）。 */
  #app .message-comments .msg-child-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    width: fit-content;
    margin: 2px auto 0;
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, 0.16);
    border-radius: 10px;
    background: transparent;
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, 0.07),
      0 1px 2px rgba(38, 38, 38, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8),
      inset 0 -1px 0 rgba(112, 112, 112, 0.1);
    transition: color 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  #app .message-comments .msg-child-toggle:hover {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    background: transparent;
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
  }

  #app .message-comments .msg-child-toggle:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, 0.42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, 0.1),
      0 1px 2px rgba(38, 38, 38, 0.05),
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(112, 112, 112, 0.16);
    scale: 0.95;
  }

  html.dark #app .message-comments .msg-child-toggle {
    border-color: rgba(255, 255, 255, 0.16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, 0.3),
      0 1px 2px rgba(0, 0, 0, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.06),
      inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  }

  html.dark #app .message-comments .msg-child-toggle:hover,
  html.dark #app .message-comments .msg-child-toggle:active {
    border-color: rgba(255, 255, 255, 0.4);
    color: var(--themeBackground);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.38),
      0 1px 2px rgba(0, 0, 0, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.34);
  }

  @media (prefers-reduced-motion: reduce) {
    .message-bar-publish,
    .message-bar-login,
    .message-bar-emoji,
    #app .message-comments .msg-card-top-btn,
    #app .message-comments .msg-card-reply-btn {
      transition: none;
    }

    .message-bar-publish:active:not(:disabled),
    .message-bar-login:active,
    .message-bar-emoji:active,
    #app .message-comments .msg-card-top-btn:active,
    #app .message-comments .msg-card-reply-btn:active {
      scale: 1;
    }
  }
</style>
