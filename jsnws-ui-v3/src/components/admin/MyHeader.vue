<template>
  <header class="admin-header">
    <div class="admin-header-left">
      <button class="admin-icon-btn"
              type="button"
              :title="collapsed ? '展开侧边栏' : '收起侧边栏'"
              @click="$emit('toggle')">
        <el-icon>
          <component :is="collapsed ? 'Expand' : 'Fold'"/>
        </el-icon>
      </button>
      <div class="admin-breadcrumb">
        <span>后台管理</span>
        <el-icon class="sep">
          <ArrowRight/>
        </el-icon>
        <span class="current">{{ title }}</span>
      </div>
    </div>

    <div class="admin-header-right">
      <!-- 主题切换：与公开站点共用 ThemeToggle 与 store.state.theme -->
      <ThemeToggle class="admin-icon-btn"/>
      <a class="admin-header-link" href="/" target="_blank" rel="noopener" @click.prevent="openSite">
        <el-icon>
          <Monitor/>
        </el-icon>
        <span>查看站点</span>
      </a>
      <div class="admin-header-divider"></div>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="admin-user">
          <span class="identity-v-wrap">
            <el-avatar class="admin-user-avatar" :size="32" :src="admin.avatar">{{ initial }}</el-avatar>
            <span class="identity-v-badge" v-if="adminHasIdentity" title="认证身份">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
            </span>
          </span>
          <span class="admin-user-name">{{ adminName }}</span>
          <el-icon class="admin-user-caret">
            <ArrowDown/>
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-if="admin.isBoss" command="account">账号设置</el-dropdown-item>
            <el-dropdown-item command="home">返回首页</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 站长自助修改登录账号 / 密码 -->
    <el-dialog v-model="accountDialog"
               title="账号设置"
               width="460px"
               append-to-body
               :close-on-click-modal="false"
               @closed="resetAccountForm">
      <p class="admin-account-tip">
        用户名需唯一、不能含空格；绑定邮箱后可用邮箱登录，留空则解除绑定。新密码留空表示不修改。密码会加密后提交，不会存储明文。
      </p>
      <el-form label-width="88px" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="accountForm.username" maxlength="50" placeholder="登录用户名"/>
        </el-form-item>
        <el-form-item label="绑定邮箱">
          <el-input v-model="accountForm.email" maxlength="100" placeholder="用于邮箱登录，留空表示解绑"/>
        </el-form-item>
        <el-form-item label="当前密码">
          <el-input v-model="accountForm.oldPassword" type="password" show-password
                    autocomplete="current-password" placeholder="用于验证身份"/>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="accountForm.newPassword" type="password" show-password
                    autocomplete="new-password" placeholder="6-20 位，留空表示不修改"/>
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="accountForm.confirmPassword" type="password" show-password
                    autocomplete="new-password" placeholder="再次输入新密码"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialog = false">取消</el-button>
        <el-button type="primary" :loading="accountSaving" @click="saveBossAccount">保存</el-button>
      </template>
    </el-dialog>
  </header>
</template>

<script>
  import ThemeToggle from '../common/ThemeToggle.vue'
  import { clearAdminEntry } from '../../utils/adminEntry'

  export default {
    name: "MyHeader",
    components: {
      ThemeToggle
    },
    props: {
      title: {
        type: String,
        default: "后台管理"
      },
      collapsed: {
        type: Boolean,
        default: false
      }
    },
    emits: ["toggle"],

    data() {
      return {
        accountDialog: false,
        accountSaving: false,
        accountForm: {
          username: "",
          email: "",
          oldPassword: "",
          newPassword: "",
          confirmPassword: ""
        }
      }
    },

    computed: {
      admin() {
        return this.$store.state.currentAdmin || {};
      },

      adminName() {
        return this.$common.isEmpty(this.admin.username) ? "管理员" : this.admin.username;
      },

      initial() {
        return this.adminName.slice(0, 1).toUpperCase();
      },

      // 头像认证角标：仅主理人 / AI 站长 / 管理员显示
      adminHasIdentity() {
        if (this.$common.isEmpty(this.admin.id)) {
          return false;
        }
        const type = Number(this.admin.userType);
        return this.admin.isBoss === true || type === 1 || type === 2 || type === 5;
      }
    },

    methods: {
      /**
       * 打开站点首页。
       * 不用 <a target="_blank"> 的默认行为：在嵌入式预览/拦截弹窗的环境里，
       * 新标签页会被静默拦掉，表现为「点了没反应」。
       * 这里显式 window.open，失败（返回 null）时退回当前标签页打开。
       */
      openSite() {
        const opened = window.open("/", "_blank");
        if (opened) {
          opened.opener = null;
        } else {
          window.location.href = "/";
        }
      },

      handleCommand(command) {
        if (command === "account") {
          this.openAccountDialog();
        } else if (command === "logout") {
          this.logout();
        } else if (command === "home") {
          clearAdminEntry();
          window.location.href = "/";
        }
      },

      openAccountDialog() {
        this.accountForm = {
          username: this.admin.username || "",
          email: this.admin.email || "",
          oldPassword: "",
          newPassword: "",
          confirmPassword: ""
        };
        this.accountDialog = true;
      },

      resetAccountForm() {
        this.accountForm = {
          username: "",
          email: "",
          oldPassword: "",
          newPassword: "",
          confirmPassword: ""
        };
      },

      saveBossAccount() {
        const form = this.accountForm;
        const username = (form.username || "").trim();
        const email = (form.email || "").trim();
        if (!username) {
          this.$message.warning("请输入用户名！");
          return;
        }
        if (/\s/.test(username)) {
          this.$message.warning("用户名不能包含空格！");
          return;
        }
        if (email && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
          this.$message.warning("邮箱格式不正确！");
          return;
        }
        if (!form.oldPassword) {
          this.$message.warning("请输入当前密码！");
          return;
        }
        const newPassword = form.newPassword || "";
        if (newPassword !== (form.confirmPassword || "")) {
          this.$message.warning("两次输入的新密码不一致！");
          return;
        }
        if (newPassword && (newPassword.length < 6 || newPassword.length > 20)) {
          this.$message.warning("新密码长度需为 6-20 位！");
          return;
        }

        const payload = {
          username: username,
          email: email,
          oldPassword: this.$common.encrypt(form.oldPassword)
        };
        if (newPassword) {
          payload.newPassword = this.$common.encrypt(newPassword);
        }

        this.accountSaving = true;
        this.$http.post(this.$constant.baseURL + "/admin/user/updateBossAccount", payload, true)
          .then(() => {
            this.$store.commit("loadCurrentAdmin", { ...this.admin, username: username, email: email || null });
            this.accountDialog = false;
            this.$message.success(newPassword ? "账号设置已更新，下次请用新密码登录" : "账号设置已更新");
          })
          .catch((error) => {
            this.$message.error(error.message || "保存失败");
          })
          .finally(() => {
            this.accountSaving = false;
          });
      },

      logout() {
        this.$confirm("确定要退出登录吗？", "提示", {
          confirmButtonText: "退出",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/user/logout", {}, true).catch(() => {
          });
          this.$store.commit("loadCurrentAdmin", {});
          localStorage.removeItem("adminToken");
          clearAdminEntry();
          this.$router.push({path: "/"});
        }).catch(() => {
        });
      }
    }
  }
</script>

<style scoped>
  .admin-user-avatar {
    flex-shrink: 0;
    background: linear-gradient(135deg, #5b7cff, #7c5cff);
    color: #fff;
    font-size: 14px;
    font-weight: 600;
  }

  .admin-account-tip {
    margin: -4px 0 16px;
    font-size: 12px;
    line-height: 1.7;
    color: var(--el-text-color-secondary, #909399);
  }
</style>
