<template>
  <div>
    <!-- 登陆和注册 -->
    <div v-if="$common.isEmpty(currentUser)"
         class="login-container myCenter">
      <!-- 背景图片 -->
      <el-image class="login-bg my-animation-hero-fade"
                v-once
                lazy
                :src="loginCover"
                fit="cover">
        <template #error>
          <div class="image-slot"></div>
        </template>
      </el-image>
      <div class="login-mask"></div>

      <div class="login-card my-animation-slide-bottom" :class="{ 'right-panel-active': isSignUp }">
        <!-- 登录 -->
        <div class="form-container sign-in-container">
          <div class="auth-form">
            <h1 class="auth-title">欢迎回来</h1>
            <p class="auth-subtitle">登录 {{ webName }}，继续未读完的故事</p>
            <FloatInput input-id="login-account"
                        v-model="account"
                        label="账号 / 邮箱 / 手机号"
                        maxlength="30"
                        fill
                        @keyup.enter="login()"/>
            <FloatInput input-id="login-password"
                        v-model="password"
                        label="密码"
                        type="password"
                        maxlength="30"
                        show-password
                        fill
                        @keyup.enter="login()"/>
            <el-button class="auth-submit" :loading="loginLoading" @click="login()">登 录</el-button>
            <div class="auth-switch">
              <span class="auth-switch-item">还没有账号？<a class="auth-link" @click="isSignUp = true">立即注册</a></span>
              <span class="auth-switch-divider" aria-hidden="true">·</span>
              <a class="auth-link auth-link-forgot" @click="changeDialog('找回密码')">忘记密码？</a>
            </div>
          </div>
        </div>

        <!-- 注册 -->
        <div class="form-container sign-up-container">
          <div class="auth-form">
            <h1 class="auth-title">加入我们</h1>
            <p class="auth-subtitle">注册一个账号，开始记录与分享</p>
            <FloatInput input-id="reg-username" v-model="username" label="账号" maxlength="30" fill/>
            <FloatInput input-id="reg-nickname" v-model="nickname" label="昵称（展示用）" maxlength="30" fill/>
            <FloatInput input-id="reg-email" v-model="email" label="邮箱" type="email" fill/>
            <div class="auth-code">
              <FloatInput input-id="reg-code" v-model="code" label="邮箱验证码" maxlength="6"
                          fill
                          @keyup.enter="regist()"/>
              <el-button class="auth-code-btn"
                         :disabled="registCodeText !== '验证码'"
                         @click="getRegistCode()">
                {{ registCodeText === '验证码' ? '获取验证码' : registCodeText + 's' }}
              </el-button>
            </div>
            <FloatInput input-id="reg-password"
                        v-model="password"
                        label="密码"
                        type="password"
                        maxlength="30"
                        show-password
                        fill
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
    <div v-else class="user-container">
      <!-- 背景图片 -->
      <el-image class="my-el-image my-animation-hero-fade"
                style="position: absolute"
                v-once
                lazy
                :src="$store.state.webInfo.randomCover[Math.floor(Math.random() * $store.state.webInfo.randomCover.length)]"
                fit="cover">
        <template #error>
          <div class="image-slot"></div>
        </template>
      </el-image>
      <div class="user-info my-animation-slide-bottom">
        <div class="user-header">
          <span class="identity-v-wrap">
            <el-avatar class="user-avatar" @click="changeDialog('修改头像')" :size="64"
                       :src="currentUser.avatar"></el-avatar>
            <span class="identity-v-badge" v-if="isBoss || isAiOwner || isAdmin" title="认证身份">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
            </span>
          </span>
          <h1 class="user-title">个人中心</h1>
          <p class="user-subtitle">管理你的账号资料与联系方式</p>
        </div>

        <div class="user-form">
          <div class="user-field">
            <div class="user-label">账号</div>
            <div class="user-value user-account">
              <span :class="{ 'user-name-author': isAuthor, 'user-name-admin': isAdmin, 'user-name-member': isMember, 'user-name-ai': isAiOwner }">{{ currentUser.username }}</span>
              <span class="user-site-badge" v-if="isBoss && siteName" :title="siteName">
                <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                {{ siteName }}
              </span>
              <span class="user-boss-badge" v-if="isBoss" title="本站主理人">
                <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M5 16 3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm0 2h14v2H5z"/></svg>
                主理人
              </span>
              <span class="user-member-badge" v-if="isMember" title="正式会员">正式会员</span>
              <span class="user-author-badge" v-if="isAuthor" title="作者">
                <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
                作　者
              </span>
              <span class="user-admin-badge" v-if="isAdmin" title="管理员">
                <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="currentColor" d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
                管理员
              </span>
              <span class="user-ai-badge" v-if="isAiOwner" title="AI 站长">
                <svg viewBox="0 0 24 24" width="12" height="12" aria-hidden="true"><path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" d="M12 21v-8"/><path fill="currentColor" d="M12 13C7.6 13 5.2 10.7 5.2 6.6 9.6 6.6 12 8.9 12 13Z"/><path fill="currentColor" d="M12 11.4c3.5 0 6-2.3 6-6.2-3.5 0-6 2.5-6 6.2Z"/></svg>
                AI站长
              </span>
            </div>
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
              <el-radio :label="2">邮箱</el-radio>
              <!-- 短信通道未接入：手机号找回置灰禁用，键盘也无法选中 -->
              <el-radio :label="1" disabled>手机号</el-radio>
            </el-radio-group>
          </div>
          <div v-if="passwordFlag === 1">
            <FloatInput input-id="find-phone" v-model="phoneNumber" label="手机号" fill/>
            <FloatInput input-id="find-code-phone" v-model="code" label="验证码" maxlength="6" fill/>
            <FloatInput input-id="find-password-phone" v-model="password" label="新密码"
                        type="password" maxlength="30" show-password fill/>
          </div>
          <div v-else-if="passwordFlag === 2">
            <FloatInput input-id="find-email" v-model="email" label="邮箱" fill/>
            <FloatInput input-id="find-code-email" v-model="code" label="验证码" maxlength="6" fill/>
            <FloatInput input-id="find-password-email" v-model="password" label="新密码"
                        type="password" maxlength="30" show-password fill/>
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
  import FloatInput from "../components/common/FloatInput.vue";

  export default {
    components: {
      UploadPicture,
      FloatInput
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
        loginCover: "/images/home-default-bg.webp"
      }
    },
    computed: {
      webName() {
        return this.$store.state.webInfo.webName || "jsnws 的博客";
      },
      // 站长判定：与评论区同一来源——当前用户 id 与站点主理人 id（constant.userId，对应后端 jsnws.boss-user-id）比较；
      // 本地缓存的 id 可能是字符串，统一转成数值后再比较，避免 "1" !== 1 导致徽章丢失
      isBoss() {
        const user = this.currentUser || {};
        if (this.$common.isEmpty(user)) {
          return false;
        }
        if (user.id !== null && user.id !== undefined && user.id !== '') {
          return Number(user.id) === Number(this.$constant.userId);
        }
        // 缓存里没有 id 时，退回后端登录接口返回的主理人标记（同样是后端权威判定，不作为前端猜测）
        return user.isBoss === true;
      },
      // 正式会员徽章：仅 4 级普通用户展示；历史数据 0/空同样按普通用户处理，与后端 UserRole.normalize 一致
      isMember() {
        if (this.isBoss) {
          return false;
        }
        const type = this.currentUser ? this.currentUser.userType : null;
        return type === 4 || type === 0 || type === null || type === undefined;
      },
      // 作者徽章：仅 3 级作者展示
      isAuthor() {
        return !this.isBoss && !!this.currentUser && this.currentUser.userType === 3;
      },
      // 管理员徽章：仅 2 级管理员展示
      isAdmin() {
        return !this.isBoss && !!this.currentUser && this.currentUser.userType === 2;
      },
      // AI 站长徽章：仅 5 级 AI 站长展示（与其专属淡紫徽章/紫名绑定）
      isAiOwner() {
        return !this.isBoss && !!this.currentUser && Number(this.currentUser.userType) === 5;
      },
      // 站点名称：取现有配置 webName，缺失退回 webTitle
      siteName() {
        const info = this.$store.state.webInfo || {};
        if (!this.$common.isEmpty(info.webName)) {
          return info.webName;
        }
        const title = Array.isArray(info.webTitle) ? info.webTitle.join('') : info.webTitle;
        return this.$common.isEmpty(title) ? '' : title;
      }
    },
    created() {
      const webInfo = this.$store.state.webInfo || {};
      const covers = webInfo.randomCover || [];
      if (covers.length > 0) {
        this.loginCover = covers[Math.floor(Math.random() * covers.length)];
      } else if (!this.$common.isEmpty(webInfo.backgroundImage)) {
        // 老配置存的是未压缩的默认图路径，统一换成体积更小的 webp 版本
        this.loginCover = webInfo.backgroundImage === '/images/home-default-bg.jpg'
          ? '/images/home-default-bg.webp'
          : webInfo.backgroundImage;
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
              // 1 主理人 / 2 管理员 / 3 作者 / 5 AI 站长可进入后台，导航栏才会出现「后台」入口
              if (res.data.isBoss || [1, 2, 3, 5].includes(res.data.userType)) {
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

        // 找回密码只支持邮箱通道，打开弹窗时默认选中邮箱，避免出现两个都没选的状态
        if (value === "找回密码") {
          this.passwordFlag = 2;
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
    margin-bottom: 0;
  }

  /* 浮动标签输入框内部的 Element Plus 输入框保持原规格；边框由 FloatInput 自绘 */
  .auth-form :deep(.float-input) {
    width: 100%;
  }

  .auth-form :deep(.float-field) {
    --float-radius: 10px;
    --float-notch-left: 14px;
    --float-border-color: rgba(148, 163, 184, 0.35);
  }

  /* 深色下输入框底色与卡片底色只差约 1.2:1，0.35 透明度的边框只有 1.9:1；
     抬到 0.65（对输入框底色约 3.3:1）保证静止态能看清输入框边界 */
  html.dark .auth-form :deep(.float-field) {
    --float-border-color: rgba(148, 163, 184, 0.65);
  }

  .auth-form :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 4px 14px;
    background: var(--glassInputBg);
  }

  .auth-form :deep(.el-input__inner) {
    height: 40px;
  }

  .auth-code {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
  }

  .auth-code .float-field {
    flex: 1;
    min-width: 0;
    margin-bottom: 0;
  }

  .auth-code-btn {
    margin-left: 10px;
    height: 44px;
    padding: 6px 12px;
    border: 1px dashed var(--ui-btn-border);
    border-radius: var(--ui-btn-radius);
    box-shadow: var(--ui-btn-shadow);
    color: var(--ui-accent);
    font-size: 14px;
    font-weight: 500;
    background: var(--ui-btn-bg);
    transition: var(--ui-btn-transition);
  }

  .auth-code-btn:hover,
  .auth-code-btn:focus-visible {
    color: var(--ui-accent);
    border-style: dashed;
    border-color: var(--ui-btn-border-hover);
    background: var(--ui-btn-bg);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  .auth-code-btn:active {
    border-style: dashed;
    border-color: var(--ui-btn-border-hover);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }

  /* 底部一行：注册入口在前、忘记密码在后，中间用小圆点分隔；窄屏可整体换行 */
  .auth-switch {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: 4px 8px;
    margin-top: 20px;
    text-align: center;
    font-size: 0.85rem;
    color: #6b7280;
  }

  .auth-switch-item,
  .auth-switch-divider,
  .auth-link-forgot {
    white-space: nowrap;
  }

  .auth-switch-divider {
    color: #9ca3af;
    user-select: none;
  }

  .auth-link-forgot {
    font-size: 0.85rem;
  }

  .auth-link {
    color: var(--themeBackground);
    cursor: pointer;
    user-select: none;
  }

  .auth-link:hover {
    text-decoration: underline;
  }

  .auth-submit {
    width: 100%;
    height: 44px;
    padding: 12px 20px;
    border: 1px solid transparent;
    border-radius: var(--ui-btn-radius);
    box-shadow: var(--ui-btn-shadow);
    font-size: 15px;
    font-weight: 500;
    letter-spacing: normal;
    color: #fff;
    background: var(--ui-accent-strong);
    transition: var(--ui-btn-transition);
  }

  .auth-submit:hover,
  .auth-submit:focus-visible {
    color: #fff;
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    background: var(--ui-accent-strong-hover);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  /* 按压瞬时缩放，与参考站 active:scale-95 一致 */
  .auth-submit:active {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
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
    background: url('/images/login-bg.4f8a16.webp') center / cover no-repeat;
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

  /* 管理员 / 作者 / 正式会员用户名跟随各自徽章配色，参数与评论区主理人一致（100deg、250%、3s 线性循环） */
  .user-name-author,
  .user-name-admin,
  .user-name-member {
    color: transparent;
    -webkit-text-fill-color: transparent;
    background-size: 250% 100%;
    background-repeat: no-repeat;
    background-position: 100% 0;
    -webkit-background-clip: text;
    background-clip: text;
    animation: user-name-shimmer 3s linear infinite;
  }

  .user-name-author {
    background-image: linear-gradient(100deg,
      rgb(23, 139, 192) 0%,
      rgb(23, 139, 192) 42%,
      rgb(190, 226, 255) 50%,
      rgb(23, 139, 192) 58%,
      rgb(23, 139, 192) 100%);
  }

  .user-name-admin {
    background-image: linear-gradient(100deg,
      rgb(150, 112, 20) 0%,
      rgb(150, 112, 20) 42%,
      rgb(255, 224, 138) 50%,
      rgb(150, 112, 20) 58%,
      rgb(150, 112, 20) 100%);
  }

  .user-name-member {
    background-image: linear-gradient(100deg,
      rgb(54, 72, 88) 0%,
      rgb(54, 72, 88) 42%,
      rgb(198, 207, 215) 50%,
      rgb(54, 72, 88) 58%,
      rgb(54, 72, 88) 100%);
  }

  /* AI 站长用户名：淡紫扁平色（取自参考图），不做扫光，仅 5 级角色 */
  .user-name-ai {
    color: #5c49c7;
  }

  html.dark .user-name-ai {
    color: #cfc7ff;
  }

  @keyframes user-name-shimmer {
    0% {
      background-position: 100% 0;
    }
    100% {
      background-position: 0% 0;
    }
  }

  /* 正式会员徽章：沿用普通友链档（tier-0）中性配色，尺寸与圆角对齐评论区主理人徽章 */
  .user-member-badge {
    --badge-sheen: rgb(198, 207, 215);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 8px;
    border: 1px solid rgba(90, 106, 122, 0.46);
    border-radius: 5px;
    overflow: hidden;
    background: rgba(90, 106, 122, 0.14);
    color: rgb(54, 72, 88);
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(54, 72, 88, 0.12);
  }

  .user-member-badge svg {
    color: rgb(54, 72, 88);
    filter: drop-shadow(0 0 1.92px rgba(54, 72, 88, 0.2));
  }

  /* 作者徽章：沿用优秀友链档（tier-3）蓝色配色，固定宽度与主理人徽章对齐 */
  .user-author-badge {
    --badge-sheen: rgb(150, 211, 255);
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 2px;
    width: 66px;
    padding: 1px 0;
    border: 1px solid color-mix(in srgb, var(--themeBackground) 46%, transparent);
    border-radius: 5px;
    overflow: hidden;
    background: color-mix(in srgb, var(--themeBackground) 14%, transparent);
    color: rgb(23, 139, 192);
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    white-space: nowrap;
    user-select: none;
    box-shadow: 0 1px 3px rgba(23, 139, 192, 0.12);
  }

  .user-author-badge svg {
    color: rgb(23, 139, 192);
    filter: drop-shadow(0 0 1.92px rgba(23, 139, 192, 0.2));
  }

  /* 管理员徽章：沿用推荐友链档（tier-1）金色配色 */
  .user-admin-badge {
    --badge-sheen: rgb(255, 224, 138);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 8px;
    border: 1px solid rgba(232, 173, 37, 0.46);
    border-radius: 5px;
    overflow: hidden;
    background: rgba(232, 173, 37, 0.14);
    color: rgb(150, 112, 20);
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(150, 112, 20, 0.12);
  }

  .user-admin-badge svg {
    color: rgb(150, 112, 20);
    filter: drop-shadow(0 0 1.92px rgba(150, 112, 20, 0.2));
  }

  /* AI 站长徽章：淡紫底 + 紫字 + 全圆角胶囊 + 浅紫细描边（色值取自参考图），扁平不扫光 */
  .user-ai-badge {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 9px;
    border: 1px solid #c3b9ff;
    border-radius: 999px;
    background: #e5e2ff;
    color: #5c49c7;
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    white-space: nowrap;
    user-select: none;
  }

  .user-ai-badge svg {
    flex: none;
    color: #5c49c7;
  }

  html.dark .user-ai-badge {
    border-color: #6e5fd0;
    background: #2b2456;
    color: #cfc7ff;
  }

  html.dark .user-ai-badge svg {
    color: #cfc7ff;
  }

  /* 站点名称徽章：复用留言页同款主理人色系徽章（含白色高光扫光） */
  .user-site-badge {
    --badge-sheen: rgba(255, 255, 255, 0.62);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 8px;
    border: 1px solid rgba(230, 0, 18, 0.46);
    border-radius: 5px;
    overflow: hidden;
    background: rgba(230, 0, 18, 0.12);
    color: rgb(230, 0, 18);
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    max-width: 180px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.12);
  }

  .user-site-badge svg {
    flex: none;
    color: rgb(230, 0, 18);
    filter: drop-shadow(0 0 1.92px rgba(230, 0, 18, 0.25));
  }

  /* 主理人徽章：与评论区主理人徽章同款红色配色，尺寸节奏对齐个人中心其他角色徽章 */
  .user-boss-badge {
    --badge-sheen: rgba(255, 255, 255, 0.62);
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 1px 8px;
    border: 1px solid rgba(230, 0, 18, 0.52);
    border-radius: 5px;
    overflow: hidden;
    background: rgba(230, 0, 18, 0.18);
    color: rgb(230, 0, 18);
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    user-select: none;
    box-shadow: 0 1px 3px rgba(230, 0, 18, 0.16);
  }

  .user-boss-badge svg {
    flex: none;
    color: rgb(230, 0, 18);
    filter: drop-shadow(0 0 1.92px rgba(230, 0, 18, 0.25));
  }

  /* 管理员 / 作者 / 正式会员徽章流光：与主理人名字同款 115deg 遮罩扫光（mask-size 238%、mask-position 100%→0%、3s 线性循环），颜色由 --badge-sheen 提供 */
  /* 站点名称与主理人徽章共用同一套扫光，保证两枚徽章同时起扫 */
  .user-member-badge::after,
  .user-author-badge::after,
  .user-admin-badge::after,
  .user-site-badge::after,
  .user-boss-badge::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: var(--badge-sheen);
    -webkit-mask-image: linear-gradient(115deg,
      transparent 42%,
      rgba(255, 255, 255, 0.5) 46%,
      #fff 48%,
      #fff 52%,
      rgba(255, 255, 255, 0.5) 54%,
      transparent 58%);
    mask-image: linear-gradient(115deg,
      transparent 42%,
      rgba(255, 255, 255, 0.5) 46%,
      #fff 48%,
      #fff 52%,
      rgba(255, 255, 255, 0.5) 54%,
      transparent 58%);
    -webkit-mask-size: 238% 100%;
    mask-size: 238% 100%;
    -webkit-mask-repeat: no-repeat;
    mask-repeat: no-repeat;
    -webkit-mask-position: 100% 0;
    mask-position: 100% 0;
    opacity: 0.55;
    pointer-events: none;
    animation: user-member-shimmer 3s linear 0s infinite;
  }

  @keyframes user-member-shimmer {
    0% {
      -webkit-mask-position: 100% 0;
      mask-position: 100% 0;
    }
    100% {
      -webkit-mask-position: 0% 0;
      mask-position: 0% 0;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .user-member-badge::after,
    .user-author-badge::after,
    .user-admin-badge::after,
    .user-site-badge::after,
    .user-boss-badge::after {
      animation: none;
      opacity: 0;
    }
  }

  /* 深色正式会员徽章：取友链普通档深色中性配色 */
  html.dark .user-member-badge {
    --badge-sheen: rgb(188, 197, 206);
    border-color: rgba(170, 178, 186, 0.46);
    background: rgba(170, 178, 186, 0.14);
    color: rgb(230, 232, 234);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);
  }

  html.dark .user-member-badge svg {
    color: rgb(230, 232, 234);
    filter: drop-shadow(0 0 1.92px rgba(230, 232, 234, 0.2));
  }

  /* 深色作者徽章：取友链优秀档深色蓝色配色 */
  html.dark .user-author-badge {
    --badge-sheen: rgb(214, 240, 255);
    border-color: rgba(99, 179, 255, 0.46);
    background: rgba(99, 179, 255, 0.14);
    color: rgb(147, 205, 255);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);
  }

  html.dark .user-author-badge svg {
    color: rgb(147, 205, 255);
    filter: drop-shadow(0 0 1.92px rgba(147, 205, 255, 0.2));
  }

  /* 深色管理员徽章：取友链推荐档深色金色配色 */
  html.dark .user-admin-badge {
    --badge-sheen: rgb(255, 236, 186);
    border-color: rgba(232, 173, 37, 0.46);
    background: rgba(232, 173, 37, 0.14);
    color: rgb(240, 210, 140);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);
  }

  html.dark .user-admin-badge svg {
    color: rgb(240, 210, 140);
    filter: drop-shadow(0 0 1.92px rgba(240, 210, 140, 0.2));
  }

  /* 深色角色用户名流光：基底按各档提亮，高光取白色，参数与主理人一致 */
  html.dark .user-name-author {
    background-image: linear-gradient(100deg,
      rgb(147, 205, 255) 0%,
      rgb(147, 205, 255) 42%,
      rgb(255, 255, 255) 50%,
      rgb(147, 205, 255) 58%,
      rgb(147, 205, 255) 100%);
  }

  html.dark .user-name-admin {
    background-image: linear-gradient(100deg,
      rgb(240, 210, 140) 0%,
      rgb(240, 210, 140) 42%,
      rgb(255, 255, 255) 50%,
      rgb(240, 210, 140) 58%,
      rgb(240, 210, 140) 100%);
  }

  html.dark .user-name-member {
    background-image: linear-gradient(100deg,
      rgb(230, 232, 234) 0%,
      rgb(230, 232, 234) 42%,
      rgb(255, 255, 255) 50%,
      rgb(230, 232, 234) 58%,
      rgb(230, 232, 234) 100%);
  }

  /* 深色站点名称徽章：与留言页同款深色主理人色系 */
  html.dark .user-site-badge {
    border-color: rgba(255, 59, 74, 0.46);
    background: rgba(255, 59, 74, 0.14);
    color: rgb(255, 120, 130);
    box-shadow: 0 1px 3px rgba(255, 59, 74, 0.18);
  }

  html.dark .user-site-badge svg {
    color: rgb(255, 120, 130);
  }

  /* 深色主理人徽章：与评论区主理人徽章深色值一致 */
  html.dark .user-boss-badge {
    border-color: rgba(255, 120, 130, 0.52);
    background: rgba(255, 120, 130, 0.18);
    color: rgb(255, 120, 130);
    box-shadow: 0 1px 3px rgba(255, 59, 74, 0.18);
  }

  html.dark .user-boss-badge svg {
    color: rgb(255, 120, 130);
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
    box-shadow: 0 0 0 1px var(--themeBackground) inset;
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
    box-shadow: 0 0 0 1px var(--themeBackground) inset;
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
    background: var(--themeBackground);
    border-color: var(--themeBackground);
  }

  .user-form :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: var(--themeBackground);
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
    color: var(--themeBackground);
    background: rgba(64, 158, 255, 0.08);
    cursor: pointer;
    user-select: none;
    transition: all 0.2s ease;
  }

  .changeInfo:hover {
    color: var(--themeBackground);
    border-color: var(--themeBackground);
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
    padding: 12px 20px;
    border: 1px solid transparent;
    border-radius: var(--ui-btn-radius);
    box-shadow: var(--ui-btn-shadow);
    font-size: 15px;
    font-weight: 500;
    letter-spacing: normal;
    color: #fff;
    background: var(--ui-accent-strong);
    transition: var(--ui-btn-transition);
  }

  .user-submit:hover {
    color: #fff;
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    background: var(--ui-accent-strong-hover);
    box-shadow: var(--ui-btn-shadow-hover);
    transform: none;
  }

  .user-submit:active {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
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

  /* 弹窗输入框沿用 Element Plus 默认 4px 圆角，缺口起点略靠内避免压到圆角 */
  .dialog-body :deep(.float-field) {
    --float-radius: 4px;
    --float-notch-left: 8px;
  }

  /* 与登录/注册一致的深色边框可辨识度 */
  html.dark .dialog-body :deep(.float-field) {
    --float-border-color: rgba(148, 163, 184, 0.65);
  }

  /* 弹窗内最后一个浮动标签输入框不再追加下边距，保持底部按钮位置不变 */
  .dialog-body :deep(.float-field:last-child) {
    margin-bottom: 0;
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
    background: var(--themeBackground);
    border-color: var(--themeBackground);
  }

  .dialog-radio :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: var(--themeBackground);
  }

  /* 禁用项文字同样置灰，避免主题色覆盖后看起来仍可选 */
  .dialog-radio :deep(.el-radio.is-disabled .el-radio__label) {
    color: var(--greyFont);
    cursor: not-allowed;
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
    padding: 12px 20px;
    border-radius: var(--ui-btn-radius);
    font-size: 15px;
    font-weight: 500;
    box-shadow: var(--ui-btn-shadow);
    transition: var(--ui-btn-transition);
  }

  .dialog-btn-secondary {
    color: var(--ui-accent);
    border: 1px dashed var(--ui-btn-border);
    background: var(--ui-btn-bg);
  }

  .dialog-btn-secondary:hover:not(.is-disabled) {
    color: var(--ui-accent);
    border-style: dashed;
    border-color: var(--ui-btn-border-hover);
    background: var(--ui-btn-bg);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  .dialog-btn-secondary:active:not(.is-disabled) {
    border-style: dashed;
    border-color: var(--ui-btn-border-hover);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }

  .dialog-btn-secondary.is-disabled {
    color: var(--greyFont);
    border-color: var(--cardBorder);
    background: var(--subtleBg);
    box-shadow: none;
    opacity: 1;
  }

  .dialog-btn-primary {
    color: #fff;
    border: 1px solid transparent;
    background: var(--ui-accent-strong);
    box-shadow: var(--ui-btn-shadow);
  }

  .dialog-btn-primary:hover:not(.is-disabled) {
    color: #fff;
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    background: var(--ui-accent-strong-hover);
    box-shadow: var(--ui-btn-shadow-hover);
    transform: none;
  }

  .dialog-btn-primary:active:not(.is-disabled) {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }

  .dialog-btn-primary.is-disabled {
    color: #fff;
    background: var(--ui-accent-strong);
    opacity: 0.55;
    box-shadow: none;
  }

  @media screen and (max-width: 640px) {
    /* 窄屏：隐藏右侧装饰图，让表单占满整张卡片，避免标签与页脚被挤到半栏而截断 */
    .login-card {
      width: 100%;
      max-width: calc(100% - 24px);
      min-height: 520px;
    }

    /* 注册字段多，窄屏下给卡片足够高度，避免内容被 overflow 裁掉 */
    .login-card.right-panel-active {
      min-height: 620px;
    }

    .overlay-container {
      display: none;
    }

    .form-container {
      width: 100%;
    }

    .sign-in-container {
      z-index: 2;
    }

    .login-card.right-panel-active .sign-in-container {
      z-index: 1;
    }

    .login-card.right-panel-active .sign-up-container {
      transform: none;
      opacity: 1;
      z-index: 3;
    }

    .auth-form {
      padding: 32px 22px;
    }

    .auth-subtitle {
      margin-bottom: 20px;
    }

    /* 弹窗固定 460px 会在 390 视口下溢出 */
    .user-dialog {
      width: calc(100% - 32px) !important;
    }
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
