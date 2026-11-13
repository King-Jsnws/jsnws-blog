<template>
  <div>
    <!-- 登陆和注册 -->
    <div v-if="$common.isEmpty(currentUser)"
         class="login-container myCenter my-animation-hideToShow">
      <!-- 背景图片 -->
      <el-image class="login-bg"
                v-once
                lazy
                :src="loginCover"
                fit="cover">
        <template #error>
          <div class="image-slot"></div>
        </template>
      </el-image>
      <div class="login-mask"></div>

      <div class="login-card" :class="{ 'right-panel-active': isSignUp }">
        <!-- 登录 -->
        <div class="form-container sign-in-container">
          <div class="auth-form">
            <h1 class="auth-title">欢迎回来</h1>
            <p class="auth-subtitle">登录 {{ webName }}，继续未读完的故事</p>
            <el-input class="auth-input"
                      v-model="account"
                      maxlength="30"
                      placeholder="账号 / 邮箱 / 手机号"
                      @keyup.enter="login()"/>
            <el-input class="auth-input"
                      v-model="password"
                      type="password"
                      maxlength="30"
                      show-password
                      placeholder="密码"
                      @keyup.enter="login()"/>
            <a class="auth-link auth-link-forgot" @click="changeDialog('找回密码')">忘记密码？</a>
            <el-button class="auth-submit" :loading="loginLoading" @click="login()">登 录</el-button>
            <div class="auth-switch">还没有账号？<a class="auth-link" @click="isSignUp = true">立即注册</a></div>
          </div>
        </div>

        <!-- 注册 -->
        <div class="form-container sign-up-container">
          <div class="auth-form">
            <h1 class="auth-title">加入我们</h1>
            <p class="auth-subtitle">注册一个账号，开始记录与分享</p>
            <el-input class="auth-input" v-model="username" maxlength="30" placeholder="账号"/>
            <el-input class="auth-input" v-model="nickname" maxlength="30" placeholder="昵称（展示用）"/>
            <el-input class="auth-input" v-model="email" type="email" placeholder="邮箱"/>
            <div class="auth-code">
              <el-input v-model="code" maxlength="6" placeholder="邮箱验证码" @keyup.enter="regist()"/>
              <el-button class="auth-code-btn"
                         :disabled="registCodeText !== '验证码'"
                         @click="getRegistCode()">
                {{ registCodeText === '验证码' ? '获取验证码' : registCodeText + 's' }}
              </el-button>
            </div>
            <el-input class="auth-input"
                      v-model="password"
                      type="password"
                      maxlength="30"
                      show-password
                      placeholder="密码"
                      @keyup.enter="regist()"/>
            <el-button class="auth-submit" :loading="registLoading" @click="regist()">注 册</el-button>
            <div class="auth-switch">已有账号？<a class="auth-link" @click="isSignUp = false">返回登录</a></div>
          </div>
        </div>

        <!-- 右侧背景面板（文字引导已移除，仅保留背景图作纯装饰） -->
        <div class="overlay-container">
          <div class="overlay">
            <div class="overlay-panel overlay-left"></div>
            <div class="overlay-panel overlay-right"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户信息 -->
    <div v-else class="user-container my-animation-hideToShow">
      <!-- 背景图片 -->
      <el-image class="my-el-image"
                style="position: absolute"
                v-once
                lazy
                :src="$store.state.webInfo.randomCover[Math.floor(Math.random() * $store.state.webInfo.randomCover.length)]"
                fit="cover">
        <template #error>
          <div class="image-slot"></div>
        </template>
      </el-image>
      <div class="user-info">
        <div class="user-header">
          <el-avatar class="user-avatar" @click="changeDialog('修改头像')" :size="64"
                     :src="currentUser.avatar"></el-avatar>
          <h1 class="user-title">个人中心</h1>
          <p class="user-subtitle">管理你的账号资料与联系方式</p>
        </div>

        <div class="user-form">
          <div class="user-field">
            <div class="user-label">账号</div>
            <div class="user-value user-account">{{ currentUser.username }}</div>
          </div>
          <div class="user-field">
            <div class="user-label">昵称</div>
            <el-input maxlength="30" v-model="currentUser.nickname"></el-input>
          </div>
          <div class="user-field">
            <div class="user-label">手机号</div>
            <div class="user-value" v-if="!$common.isEmpty(currentUser.phoneNumber)">
              <span>{{ currentUser.phoneNumber }}</span>
              <span class="changeInfo" @click="changeDialog('修改手机号')">修改（功能未接入）</span>
            </div>
            <div class="user-value" v-else>
              <span class="changeInfo changeInfo-disabled" aria-disabled="true">绑定手机号（功能未接入）</span>
            </div>
          </div>
          <div class="user-field">
            <div class="user-label">邮箱</div>
            <div class="user-value" v-if="!$common.isEmpty(currentUser.email)">
              <span>{{ currentUser.email }}</span>
              <span class="changeInfo" @click="changeDialog('修改邮箱')">修改</span>
            </div>
            <div class="user-value" v-else>
              <span class="changeInfo" @click="changeDialog('绑定邮箱')">绑定邮箱</span>
            </div>
          </div>
          <div class="user-field">
            <div class="user-label">性别</div>
            <el-radio-group v-model="currentUser.gender">
              <el-radio :label="0">薛定谔的猫</el-radio>
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
            </el-radio-group>
          </div>
          <div class="user-field user-field-top">
            <div class="user-label">简介</div>
            <el-input v-model="currentUser.introduction"
                      maxlength="60"
                      type="textarea"
                      show-word-limit></el-input>
          </div>
        </div>

        <div class="user-actions">
          <el-button class="user-submit" @click="submitUserInfo()">提交</el-button>
        </div>
      </div>
    </div>


    <el-dialog :title="dialogTitle"
               v-model="showDialog"
               width="460"
               class="user-dialog"
               :before-close="clearDialog"
               :append-to-body="true"
               :close-on-click-modal="false">
      <div class="dialog-body">
        <div v-if="dialogTitle === '修改手机号' || dialogTitle === '绑定手机号'">
          <div class="dialog-label">手机号：</div>
          <el-input v-model="phoneNumber"></el-input>
          <div class="dialog-label">验证码：</div>
          <el-input v-model="code"></el-input>
          <div class="dialog-label">密码：</div>
          <el-input v-model="password"></el-input>
        </div>
        <div v-else-if="dialogTitle === '修改邮箱' || dialogTitle === '绑定邮箱'">
          <div class="dialog-label">邮箱：</div>
          <el-input v-model="email"></el-input>
          <div class="dialog-label">验证码：</div>
          <el-input v-model="code"></el-input>
          <div class="dialog-label">密码：</div>
          <el-input v-model="password"></el-input>
        </div>
        <div v-else-if="dialogTitle === '修改头像'">
          <UploadPicture :prefix="'userAvatar'" @addPicture="addPicture" :maxSize="1"
                         :maxNumber="1"></UploadPicture>
        </div>
        <div v-else-if="dialogTitle === '找回密码'">
          <div class="dialog-radio">
            <el-radio-group v-model="passwordFlag">
              <el-radio :label="1">手机号</el-radio>
              <el-radio :label="2">邮箱</el-radio>
            </el-radio-group>
          </div>
          <div v-if="passwordFlag === 1">
            <div class="dialog-label">手机号：</div>
            <el-input v-model="phoneNumber"></el-input>
            <div class="dialog-label">验证码：</div>
            <el-input v-model="code"></el-input>
            <div class="dialog-label">新密码：</div>
            <el-input maxlength="30" v-model="password"></el-input>
          </div>
          <div v-else-if="passwordFlag === 2">
            <div class="dialog-label">邮箱：</div>
            <el-input v-model="email"></el-input>
            <div class="dialog-label">验证码：</div>
            <el-input v-model="code"></el-input>
            <div class="dialog-label">新密码：</div>
            <el-input maxlength="30" v-model="password"></el-input>
          </div>
        </div>
        <div v-else-if="dialogTitle === '邮箱验证码'">
          <div class="dialog-label">邮箱：</div>
          <el-input v-model="email"></el-input>
          <div class="dialog-label">验证码：</div>
          <el-input v-model="code"></el-input>
        </div>

        <div class="dialog-actions" v-show="dialogTitle !== '修改头像'">
          <el-button class="dialog-btn dialog-btn-secondary"
                     v-show="dialogTitle === '修改手机号' || dialogTitle === '绑定手机号' || dialogTitle === '修改邮箱' || dialogTitle === '绑定邮箱' || dialogTitle === '找回密码' || dialogTitle === '邮箱验证码'"
                     :disabled="codeString !== '验证码'"
                     @click="getCode()">{{ codeString }}</el-button>
          <el-button class="dialog-btn dialog-btn-primary" @click="submitDialog()">提交</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import UploadPicture from "../components/common/UploadPicture.vue";

  export default {
    components: {
      UploadPicture
    },
    data() {
      return {
        currentUser: this.$store.state.currentUser,
        username: "",
        nickname: "",
        account: "",
        password: "",
        phoneNumber: "",
        email: "",
        avatar: "",
        showDialog: false,
        code: "",
        dialogTitle: "",
        codeString: "验证码",
        passwordFlag: null,
        intervalCode: null,
        isSignUp: false,
        loginLoading: false,
        registLoading: false,
        registCodeText: "验证码",
        registInterval: null,
        loginCover: "/images/home-default-bg.jpg"
      }
    },
    computed: {
      webName() {
        return this.$store.state.webInfo.webName || "jsnws 的博客";
      }
    },
    created() {
      const webInfo = this.$store.state.webInfo || {};
      const covers = webInfo.randomCover || [];
      if (covers.length > 0) {
        this.loginCover = covers[Math.floor(Math.random() * covers.length)];
      } else if (!this.$common.isEmpty(webInfo.backgroundImage)) {
        this.loginCover = webInfo.backgroundImage;
      }
    },
    beforeUnmount() {
      clearInterval(this.intervalCode);
      clearInterval(this.registInterval);
    },
    methods: {
      addPicture(res) {
        this.avatar = res;
        this.submitDialog()
      },
      getRegistCode() {
        if (this.registCodeText !== "验证码") {
          this.$message({
            message: "请稍后再试！",
            type: "warning"
          });
          return;
        }
        if (this.$common.isEmpty(this.email)) {
          this.$message({
            message: "请输入邮箱！",
            type: "error"
          });
          return;
        }
        if (!(/^\w+@[a-zA-Z0-9]{2,10}(?:\.[a-z]{2,4}){1,3}$/.test(this.email))) {
          this.$message({
            message: "邮箱格式有误！",
            type: "error"
          });
          return;
        }

        this.$http.get(this.$constant.baseURL + "/user/getCodeForForgetPassword", {
          flag: 2,
          place: this.email.trim()
        })
          .then(() => {
            this.$message({
              message: "验证码已发送，请注意查收！",
              type: "success"
            });
            this.registCodeText = "60";
            this.registInterval = setInterval(() => {
              if (this.registCodeText === "0") {
                clearInterval(this.registInterval);
                this.registCodeText = "验证码";
              } else {
                this.registCodeText = (parseInt(this.registCodeText) - 1) + "";
              }
            }, 1000);
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
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
          password: this.$common.encrypt(this.password.trim())
        };

        this.loginLoading = true;
        this.$http.post(this.$constant.baseURL + "/user/login", user, false, false)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.$store.commit("loadCurrentUser", res.data);
              localStorage.setItem("userToken", res.data.accessToken);
              // 管理员账号登录后同步后台登录态，导航栏才会出现「后台」入口
              if (res.data.userType === 1) {
                this.$store.commit("loadCurrentAdmin", res.data);
                localStorage.setItem("adminToken", res.data.accessToken);
              } else {
                this.$store.commit("loadCurrentAdmin", {});
                localStorage.removeItem("adminToken");
              }
              this.account = "";
              this.password = "";
              this.$router.push({path: '/'});
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          })
          .finally(() => {
            this.loginLoading = false;
          });
      },
      regist() {
        if (this.$common.isEmpty(this.username) || this.$common.isEmpty(this.password)) {
          this.$message({
            message: "请输入账号或密码！",
            type: "error"
          });
          return;
        }

        if (this.$common.isEmpty(this.email)) {
          this.$message({
            message: "请输入邮箱！",
            type: "error"
          });
          return;
        }

        if (!(/^\w+@[a-zA-Z0-9]{2,10}(?:\.[a-z]{2,4}){1,3}$/.test(this.email))) {
          this.$message({
            message: "邮箱格式有误！",
            type: "error"
          });
          return;
        }

        if (this.$common.isEmpty(this.code)) {
          this.$message({
            message: "请输入验证码！",
            type: "error"
          });
          return;
        }

        if (this.username.indexOf(" ") !== -1 || this.password.indexOf(" ") !== -1) {
          this.$message({
            message: "账号或密码不能包含空格！",
            type: "error"
          });
          return;
        }

        let user = {
          username: this.username.trim(),
          nickname: this.nickname.trim(),
          code: this.code.trim(),
          password: this.$common.encrypt(this.password.trim()),
          email: this.email.trim()
        };

        this.registLoading = true;
        this.$http.post(this.$constant.baseURL + "/user/regist", user)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.$store.commit("loadCurrentUser", res.data);
              localStorage.setItem("userToken", res.data.accessToken);
              this.username = "";
              this.nickname = "";
              this.password = "";
              this.code = "";
              this.$router.push({path: '/'});
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          })
          .finally(() => {
            this.registLoading = false;
          });
      },
      submitUserInfo() {
        if (!this.checkParameters()) {
          return;
        }

        let user = {
          nickname: this.currentUser.nickname ? this.currentUser.nickname.trim() : "",
          gender: this.currentUser.gender
        };

        if (!this.$common.isEmpty(this.currentUser.introduction)) {
          user.introduction = this.currentUser.introduction.trim();
        }

        this.$confirm('确认保存？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.post(this.$constant.baseURL + "/user/updateUserInfo", user)
            .then((res) => {
              if (!this.$common.isEmpty(res.data)) {
                this.$store.commit("loadCurrentUser", res.data);
                this.currentUser = this.$store.state.currentUser;
                this.$message({
                  message: "修改成功！",
                  type: "success"
                });
              }
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
            message: '已取消保存!'
          });
        });
      },
      checkParams(params) {
        if (this.dialogTitle === "修改手机号" || this.dialogTitle === "绑定手机号" || (this.dialogTitle === "找回密码" && this.passwordFlag === 1)) {
          params.flag = 1;
          if (this.$common.isEmpty(this.phoneNumber)) {
            this.$message({
              message: "请输入手机号！",
              type: "error"
            });
            return false;
          }
          if (!(/^1[345789]\d{9}$/.test(this.phoneNumber))) {
            this.$message({
              message: "手机号格式有误！",
              type: "error"
            });
            return false;
          }
          params.place = this.phoneNumber;
          return true;
        } else if (this.dialogTitle === "修改邮箱" || this.dialogTitle === "绑定邮箱" || this.dialogTitle === "邮箱验证码" || (this.dialogTitle === "找回密码" && this.passwordFlag === 2)) {
          params.flag = 2;
          if (this.$common.isEmpty(this.email)) {
            this.$message({
              message: "请输入邮箱！",
              type: "error"
            });
            return false;
          }
          if (!(/^\w+@[a-zA-Z0-9]{2,10}(?:\.[a-z]{2,4}){1,3}$/.test(this.email))) {
            this.$message({
              message: "邮箱格式有误！",
              type: "error"
            });
            return false;
          }
          params.place = this.email;
          return true;
        }
        return false;
      },
      checkParameters() {
        if (!this.$common.isEmpty(this.currentUser.nickname)
          && this.currentUser.nickname.indexOf(" ") !== -1) {
          this.$message({
            message: "昵称不能包含空格！",
            type: "error"
          });
          return false;
        }
        return true;
      },
      changeDialog(value) {
        if (value === "邮箱验证码") {
          if (this.$common.isEmpty(this.email)) {
            this.$message({
              message: "请输入邮箱！",
              type: "error"
            });
            return false;
          }
          if (!(/^\w+@[a-zA-Z0-9]{2,10}(?:\.[a-z]{2,4}){1,3}$/.test(this.email))) {
            this.$message({
              message: "邮箱格式有误！",
              type: "error"
            });
            return false;
          }
        }

        this.dialogTitle = value;
        this.showDialog = true;
      },
      submitDialog() {
        if (this.dialogTitle === "修改头像") {
          if (this.$common.isEmpty(this.avatar)) {
            this.$message({
              message: "请上传头像！",
              type: "error"
            });
          } else {
            let user = {
              avatar: this.avatar.trim()
            };

            this.$http.post(this.$constant.baseURL + "/user/updateUserInfo", user)
              .then((res) => {
                if (!this.$common.isEmpty(res.data)) {
                  this.$store.commit("loadCurrentUser", res.data);
                  this.currentUser = this.$store.state.currentUser;
                  this.clearDialog();
                  this.$message({
                    message: "修改成功！",
                    type: "success"
                  });
                }
              })
              .catch((error) => {
                this.$message({
                  message: error.message,
                  type: "error"
                });
              });
          }
        } else if (this.dialogTitle === "修改手机号" || this.dialogTitle === "绑定手机号" || this.dialogTitle === "修改邮箱" || this.dialogTitle === "绑定邮箱") {
          this.updateSecretInfo();
        } else if (this.dialogTitle === "找回密码") {
          if (this.passwordFlag !== 1 && this.passwordFlag !== 2) {
            this.$message({
              message: "请选择找回方式！",
              type: "error"
            });
          } else {
            this.updateSecretInfo();
          }
        } else if (this.dialogTitle === "邮箱验证码") {
          this.showDialog = false;
        }
      },
      updateSecretInfo() {
        if (this.$common.isEmpty(this.code)) {
          this.$message({
            message: "请输入验证码！",
            type: "error"
          });
          return;
        }
        if (this.$common.isEmpty(this.password)) {
          this.$message({
            message: "请输入密码！",
            type: "error"
          });
          return;
        }
        let params = {
          code: this.code.trim(),
          password: this.$common.encrypt(this.password.trim())
        };
        if (!this.checkParams(params)) {
          return;
        }

        if (this.dialogTitle === "找回密码") {
          this.$http.post(this.$constant.baseURL + "/user/updateForForgetPassword", params, false, false)
            .then((res) => {
              this.clearDialog();
              this.$message({
                message: "修改成功，请重新登陆！",
                type: "success"
              });
            })
            .catch((error) => {
              this.$message({
                message: error.message,
                type: "error"
              });
            });
        } else {
          this.$http.post(this.$constant.baseURL + "/user/updateSecretInfo", params, false, false)
            .then((res) => {
              if (!this.$common.isEmpty(res.data)) {
                this.$store.commit("loadCurrentUser", res.data);
                this.currentUser = this.$store.state.currentUser;
                this.clearDialog();
                this.$message({
                  message: "修改成功！",
                  type: "success"
                });
              }
            })
            .catch((error) => {
              this.$message({
                message: error.message,
                type: "error"
              });
            });
        }
      },
      getCode() {
        if (this.codeString === "验证码") {
          // 获取验证码
          let params = {};
          if (!this.checkParams(params)) {
            return;
          }

          let url;
          if (this.dialogTitle === "找回密码" || this.dialogTitle === "邮箱验证码") {
            url = "/user/getCodeForForgetPassword";
          } else {
            url = "/user/getCodeForBind";
          }

          this.$http.get(this.$constant.baseURL + url, params)
            .then((res) => {
              this.$message({
                message: "验证码已发送，请注意查收！",
                type: "success"
              });
            })
            .catch((error) => {
              this.$message({
                message: error.message,
                type: "error"
              });
            });
          this.codeString = "30";
          this.intervalCode = setInterval(() => {
            if (this.codeString === "0") {
              clearInterval(this.intervalCode)
              this.codeString = "验证码";
            } else {
              this.codeString = (parseInt(this.codeString) - 1) + "";
            }
          }, 1000);
        } else {
          this.$message({
            message: "请稍后再试！",
            type: "warning"
          });
        }
      },
      clearDialog() {
        this.password = "";
        this.phoneNumber = "";
        this.email = "";
        this.avatar = "";
        this.showDialog = false;
        this.code = "";
        this.dialogTitle = "";
        this.passwordFlag = null;
      }
    }
  }
</script>

<style scoped>

  .login-container {
    width: 100vw;
    height: 100vh;
    position: relative;
    overflow: hidden;
    background: #0f172a;
  }

  .login-bg,
  .login-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }

  .login-bg :deep(img) {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .login-mask {
    background: linear-gradient(135deg, rgba(15, 23, 42, 0.55) 0%, rgba(15, 23, 42, 0.25) 55%, rgba(15, 23, 42, 0.5) 100%);
  }

  .login-card {
    position: relative;
    width: 860px;
    max-width: calc(100% - 32px);
    min-height: 520px;
    border-radius: 20px;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.35);
    box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
  }

  .form-container {
    position: absolute;
    top: 0;
    height: 100%;
    width: 50%;
    transition: all 0.5s ease-in-out;
  }

  .sign-in-container {
    left: 0;
    z-index: 2;
  }

  .sign-up-container {
    left: 0;
    opacity: 0;
    z-index: 1;
  }

  .auth-form {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 48px 46px;
    background: var(--glassBg);
  }

  .auth-title {
    margin: 0;
    font-size: 1.7rem;
    color: var(--fontColor);
    letter-spacing: 1px;
  }

  .auth-subtitle {
    margin: 10px 0 28px;
    font-size: 0.85rem;
    color: var(--maxGreyFont);
    line-height: 1.6;
  }

  .auth-input {
    margin-bottom: 16px;
  }

  .auth-form :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 4px 14px;
    background: var(--glassInputBg);
    box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.35) inset;
  }

  .auth-form :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #409eff inset;
  }

  .auth-form :deep(.el-input__inner) {
    height: 40px;
  }

  .auth-code {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
  }

  .auth-code .el-input {
    flex: 1;
  }

  .auth-code-btn {
    margin-left: 10px;
    height: 48px;
    border-radius: 10px;
    border: 1px solid rgba(64, 158, 255, 0.5);
    color: #409eff;
    background: rgba(64, 158, 255, 0.08);
  }

  .auth-link-forgot {
    display: block;
    width: 100%;
    margin-bottom: 18px;
    text-align: right;
    font-size: 0.85rem;
  }

  .auth-link {
    color: #409eff;
    cursor: pointer;
    user-select: none;
  }

  .auth-link:hover {
    text-decoration: underline;
  }

  .auth-submit {
    width: 100%;
    height: 44px;
    border: none;
    border-radius: 10px;
    font-size: 1rem;
    letter-spacing: 4px;
    color: #fff;
    background: linear-gradient(135deg, #409eff, #337ecc);
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.35);
    transition: all 0.3s ease;
  }

  .auth-submit:hover {
    background: linear-gradient(135deg, #337ecc, #2b6cb0);
    transform: translateY(-1px);
  }

  .auth-switch {
    margin-top: 20px;
    text-align: center;
    font-size: 0.85rem;
    color: #6b7280;
  }

  /* 右侧装饰背景面板：只保留背景图，不再叠蓝色渐变蒙版 */
  .overlay-container {
    position: absolute;
    left: 50%;
    width: 50%;
    height: 100%;
    overflow: hidden;
    transition: all 0.5s ease-in-out;
    z-index: 3;
  }

  .overlay {
    position: relative;
    left: -100%;
    width: 200%;
    height: 100%;
    color: #fff;
    background: url('/images/login-bg.webp') center / cover no-repeat;
  }

  .overlay-panel {
    position: absolute;
    top: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 50%;
    padding: 0 40px;
    text-align: center;
    transition: all 0.5s ease-in-out;
  }

  .overlay-left {
    transform: translateY(-20%);
  }

  .overlay-right {
    right: 0;
    transform: translateY(0);
  }

  .login-card.right-panel-active .sign-in-container {
    transform: translateY(100%);
  }

  .login-card.right-panel-active .sign-up-container {
    transform: translateX(100%);
    opacity: 1;
  }

  .login-card.right-panel-active .overlay-container {
    transform: translateX(-100%);
  }

  .login-card.right-panel-active .overlay {
    transform: translateX(50%);
  }

  .login-card.right-panel-active .overlay-left {
    transform: translateY(0);
  }

  .login-card.right-panel-active .overlay-right {
    transform: translateY(20%);
  }

  /* 个人中心：与登录卡片共用同一套卡片与表单规格 */
  .user-container {
    width: 100%;
    min-height: 100vh;
    position: relative;
    padding: 88px 16px 32px;
  }

  /* 背景图铺满容器，避免绝对定位按内容盒偏移造成横向溢出 */
  .user-container .my-el-image {
    top: 0;
    left: 0;
  }

  .user-info {
    position: relative;
    z-index: 10;
    width: 860px;
    max-width: 100%;
    margin: 0 auto;
    padding: 48px 46px;
    border-radius: 20px;
    border: 1px solid var(--cardBorder);
    background: var(--glassBg);
    box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
  }

  .user-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding-bottom: 28px;
    margin-bottom: 28px;
    border-bottom: 1px solid var(--cardBorder);
  }

  .user-avatar {
    background: var(--subtleBg);
    color: var(--greyFont);
    border: 2px solid rgba(64, 158, 255, 0.5);
    box-shadow: 0 6px 18px rgba(64, 158, 255, 0.25);
  }

  .user-title {
    margin: 14px 0 0;
    font-size: 1.7rem;
    font-weight: 700;
    line-height: 1.3;
    letter-spacing: 1px;
    color: var(--fontColor);
  }

  .user-subtitle {
    margin: 8px 0 0;
    font-size: 0.85rem;
    line-height: 1.6;
    color: var(--maxGreyFont);
  }

  .user-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .user-field {
    display: grid;
    grid-template-columns: 72px minmax(0, 1fr);
    align-items: center;
    column-gap: 16px;
    min-height: 40px;
  }

  .user-field-top {
    align-items: start;
  }

  .user-field-top .user-label {
    padding-top: 2px;
    line-height: 1.6;
  }

  .user-label {
    font-size: 0.9rem;
    font-weight: 600;
    line-height: 1.5;
    color: var(--maxGreyFont);
  }

  .user-value {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    font-size: 0.9rem;
    line-height: 1.6;
    color: var(--fontColor);
    overflow-wrap: anywhere;
  }

  .user-account {
    color: var(--greyFont);
  }

  /* 输入框：与登录卡片同一规格 */
  .user-form :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 4px 14px;
    background: var(--glassInputBg);
    box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.35) inset;
    transition: box-shadow 0.2s ease;
  }

  .user-form :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #409eff inset;
  }

  .user-form :deep(.el-input__inner) {
    height: 40px;
    font-size: 0.9rem;
    color: var(--fontColor);
  }

  .user-form :deep(.el-input__inner::placeholder),
  .user-form :deep(.el-textarea__inner::placeholder) {
    color: var(--greyFont);
  }

  .user-form :deep(.el-textarea__inner) {
    min-height: 96px !important;
    padding: 10px 14px;
    border-radius: 10px;
    background: var(--glassInputBg);
    box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.35) inset;
    color: var(--fontColor);
    font-family: inherit;
    font-size: 0.9rem;
    line-height: 1.6;
    transition: box-shadow 0.2s ease;
  }

  .user-form :deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 1px #409eff inset;
  }

  .user-form :deep(.el-input__count) {
    background: transparent;
    color: var(--greyFont);
    user-select: none;
  }

  .user-form :deep(.el-radio) {
    margin-right: 20px;
  }

  .user-form :deep(.el-radio:last-child) {
    margin-right: 0;
  }

  .user-form :deep(.el-radio__inner) {
    border-color: rgba(148, 163, 184, 0.6);
    background: var(--glassInputBg);
  }

  .user-form :deep(.el-radio__label) {
    padding-left: 6px;
    font-size: 0.9rem;
    color: var(--fontColor);
  }

  .user-form :deep(.el-radio__input.is-checked .el-radio__inner) {
    background: #409eff;
    border-color: #409eff;
  }

  .user-form :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: #409eff;
  }

  .changeInfo {
    display: inline-flex;
    align-items: center;
    height: 26px;
    padding: 0 10px;
    border-radius: 6px;
    border: 1px solid rgba(64, 158, 255, 0.35);
    font-size: 0.8rem;
    line-height: 1;
    color: #409eff;
    background: rgba(64, 158, 255, 0.08);
    cursor: pointer;
    user-select: none;
    transition: all 0.2s ease;
  }

  .changeInfo:hover {
    color: #409eff;
    border-color: #409eff;
    background: rgba(64, 158, 255, 0.16);
  }

  /* 功能未接入的入口：明确禁用态，不可点击 */
  .changeInfo-disabled,
  .changeInfo-disabled:hover {
    color: var(--maxGreyFont);
    background: var(--subtleBg);
    border-color: var(--cardBorder);
    cursor: not-allowed;
  }

  .user-actions {
    margin-top: 32px;
  }

  .user-submit {
    width: 100%;
    height: 44px;
    border: none;
    border-radius: 10px;
    font-size: 1rem;
    letter-spacing: 4px;
    color: #fff;
    background: linear-gradient(135deg, #409eff, #337ecc);
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.35);
    transition: all 0.3s ease;
  }

  .user-submit:hover {
    color: #fff;
    background: linear-gradient(135deg, #337ecc, #2b6cb0);
    transform: translateY(-1px);
  }

  .user-submit.is-disabled,
  .user-submit.is-disabled:hover {
    color: #fff;
    opacity: 0.55;
    box-shadow: none;
    transform: none;
    cursor: not-allowed;
  }

  /* 找回密码 / 修改信息弹窗：弹窗本体与输入框样式统一在 index.css（弹窗 teleport 到 body，scoped 选择器命中不到） */
  .dialog-body {
    text-align: left;
  }

  .dialog-label {
    margin: 16px 0 8px;
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--fontColor);
  }

  .dialog-label:first-child {
    margin-top: 0;
  }

  .dialog-radio {
    display: flex;
    justify-content: flex-start;
    margin: 4px 0 16px;
  }

  .dialog-radio :deep(.el-radio) {
    margin-right: 22px;
    color: var(--fontColor);
  }

  .dialog-radio :deep(.el-radio__input.is-checked .el-radio__inner) {
    background: #409eff;
    border-color: #409eff;
  }

  .dialog-radio :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: #409eff;
  }

  .dialog-actions {
    display: flex;
    gap: 12px;
    margin-top: 24px;
  }

  .dialog-actions :deep(.el-button + .el-button) {
    margin-left: 0;
  }

  .dialog-btn {
    flex: 1;
    height: 44px;
    border-radius: 10px;
    font-size: 0.95rem;
    transition: all 0.3s ease;
  }

  .dialog-btn-secondary {
    color: #409eff;
    border: 1px solid rgba(64, 158, 255, 0.5);
    background: rgba(64, 158, 255, 0.08);
  }

  .dialog-btn-secondary:hover:not(.is-disabled) {
    color: #409eff;
    border-color: #409eff;
    background: rgba(64, 158, 255, 0.16);
  }

  .dialog-btn-secondary.is-disabled {
    color: var(--greyFont);
    border-color: var(--cardBorder);
    background: var(--subtleBg);
    opacity: 1;
  }

  .dialog-btn-primary {
    color: #fff;
    border: none;
    background: linear-gradient(135deg, #409eff, #337ecc);
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.35);
  }

  .dialog-btn-primary:hover:not(.is-disabled) {
    color: #fff;
    background: linear-gradient(135deg, #337ecc, #2b6cb0);
    transform: translateY(-1px);
  }

  .dialog-btn-primary.is-disabled {
    color: #fff;
    opacity: 0.55;
    box-shadow: none;
  }

  @media screen and (max-width: 560px) {
    .user-container {
      padding: 76px 12px 24px;
    }

    .user-info {
      padding: 30px 20px;
      border-radius: 16px;
    }

    .user-header {
      padding-bottom: 22px;
      margin-bottom: 22px;
    }

    .user-title {
      font-size: 1.5rem;
    }

    .user-form {
      gap: 14px;
    }

    .user-field {
      grid-template-columns: 1fr;
      row-gap: 6px;
      min-height: 0;
    }

    .user-field-top .user-label {
      padding-top: 0;
    }

    .user-value {
      min-width: 0;
    }
  }
</style>
