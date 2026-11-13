<template>
  <div>
    <div>
      <!-- 背景层：与首页导航容器使用同一张 --toolbar 图、同样的 cover 居中与同一套遮罩变量 -->
      <div class="message-background" aria-hidden="true"></div>
      <!-- 输入框 -->
      <div class="message-in" style="text-align: center">
        <h2 class="message-title">树洞</h2>
        <div>
          <input class="message-input"
                 type="text"
                 style="outline: none;width: 70%"
                 placeholder="留下点什么啦~"
                 v-model="messageContent"
                 @click="show = true"
                 maxlength="60"/>
          <button v-show="show"
                  style="margin-left: 12px;cursor: pointer;width: 20%"
                  @click="submitMessage"
                  class="message-input">发射
          </button>
        </div>
      </div>
      <!-- 弹幕 -->
      <div class="barrage-container">
        <Barrage :barrage-list="barrageList" :loop="true" />
      </div>
    </div>
    <div class="comment-wrap">
      <div class="comment-content">
        <Comment :source="$constant.source" :type="'message'" :userId="$constant.userId"></Comment>
      </div>
      <MyFooter></MyFooter>
    </div>
  </div>
</template>

<script>
  import Barrage from "../components/common/Barrage.vue";

  import Comment from "../components/comment/Comment.vue";
  import MyFooter from "../components/common/MyFooter.vue";

  export default {
    components: {
      Barrage,
      Comment,
      MyFooter
    },
    data() {
      return {
        show: false,
        messageContent: "",
        // background: {"background": "url(" + this.$store.state.webInfo.backgroundImage + ") center center / cover no-repeat"},
        barrageList: []
      };
    },
    created() {
      this.getTreeHole();
    },
    methods: {
      getTreeHole() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listTreeHole")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              res.data.forEach(m => {
                this.barrageList.push({
                  id: m.id,
                  avatar: m.avatar,
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
            if (!this.$common.isEmpty(res.data)) {
              this.barrageList.push({
                id: res.data.id,
                avatar: res.data.avatar,
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
                var(--toolbar, url('/images/home-default-bg.jpg')) center center / cover no-repeat,
                url('/images/home-default-bg.jpg') center center / cover no-repeat;
  }

  .message-in {
    position: absolute;
    left: 50%;
    top: 40%;
    transform: translate(-50%, -50%);
    color: var(--white);
    animation: hideToShow 2.5s;
    width: 360px;
    z-index: 10;
  }

  .message-title {
    user-select: none;
    text-align: center;
  }

  .message-input {
    border-radius: 1.2rem;
    border: var(--white) 1px solid;
    color: var(--white);
    background: var(--transparent);
    padding: 10px 10px;
  }

  .message-input::-webkit-input-placeholder {
    color: var(--white);
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
    background: var(--background);
    position: absolute;
    top: 100vh;
    width: 100%;
  }

  .comment-content {
    max-width: 800px;
    margin: 0 auto;
    padding: 40px 20px;
  }
</style>
