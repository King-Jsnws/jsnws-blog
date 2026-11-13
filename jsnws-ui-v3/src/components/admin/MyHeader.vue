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
      <a class="admin-header-link" href="/" target="_blank" rel="noopener">
        <el-icon>
          <Monitor/>
        </el-icon>
        <span>查看站点</span>
      </a>
      <div class="admin-header-divider"></div>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="admin-user">
          <el-avatar class="admin-user-avatar" :size="32" :src="admin.avatar">{{ initial }}</el-avatar>
          <span class="admin-user-name">{{ adminName }}</span>
          <el-icon class="admin-user-caret">
            <ArrowDown/>
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="home">返回首页</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
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

    computed: {
      admin() {
        return this.$store.state.currentAdmin || {};
      },

      adminName() {
        return this.$common.isEmpty(this.admin.username) ? "管理员" : this.admin.username;
      },

      initial() {
        return this.adminName.slice(0, 1).toUpperCase();
      }
    },

    methods: {
      handleCommand(command) {
        if (command === "logout") {
          this.logout();
        } else if (command === "home") {
          clearAdminEntry();
          window.location.href = "/";
        }
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
</style>
