<template>
  <div class="myCenter verify-container">
    <div class="verify-content">
      <div>
        <el-avatar :size="50" :src="$store.state.webInfo.avatar"></el-avatar>
      </div>
      <div>
        <el-input v-model="account">
          <template #prepend>账号</template>
        </el-input>
      </div>
      <div>
        <el-input v-model="password" type="password">
          <template #prepend>密码</template>
        </el-input>
      </div>
      <div>
        <ProButton :info="'提交'"
                   @click="login()"
                   :before="$constant.before_color_2"
                   :after="$constant.after_color_2">
        </ProButton>
      </div>
    </div>
  </div>
</template>

<script>
  import ProButton from "../../components/common/ProButton.vue";

  export default {
    name: "VerifyView",
    components: {
      ProButton
    },

    data() {
      return {
        redirect: this.$route.query.redirect,
        account: "",
        password: ""
      }
    },

    methods: {
      login() {
        if (this.$common.isEmpty(this.account) || this.$common.isEmpty(this.password)) {
          this.$message({
            message: "请输入账号或密码！",
            type: "error"
          });
          return;
        }

        let user = {
          account: this.account.trim(),
          password: this.$common.encrypt(this.password.trim()),
          isAdmin: true
        };

        this.$http.post(this.$constant.baseURL + "/user/login", user, true, false)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              localStorage.setItem("adminToken", res.data.accessToken);
              this.$store.commit("loadCurrentAdmin", res.data);
              this.account = "";
              this.password = "";
              this.$router.push({path: this.redirect});
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      }
    }
  }
</script>

<style scoped>

  .verify-container {
    height: 100vh;
    /* 背景图变量缺失时也保留主题底色；图片加载失败时颜色仍在 */
    background-color: var(--background);
    background-image: var(--backgroundPicture, url('/images/login-bg.webp'));
    background-position: center center;
    background-size: cover;
    background-repeat: repeat;
  }

  .verify-content {
    background: var(--maxWhiteMask);
    padding: 30px 40px 5px;
    position: relative;
  }

  /* 输入框前缀块跟随主题，避免深色下深底配灰字发闷 */
  .verify-content :deep(.el-input-group__prepend) {
    background: var(--subtleBg);
    color: var(--maxGreyFont);
    box-shadow: 0 0 0 1px var(--borderColor) inset;
  }

  .verify-content > div:first-child {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    top: -25px;
  }

  .verify-content > div:not(:first-child) {
    margin: 25px 0;
  }

  .verify-content > div:last-child > div {
    margin: 0 auto;
  }

</style>
