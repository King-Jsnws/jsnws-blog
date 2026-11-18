<template>
  <aside class="admin-sidebar">
    <router-link class="admin-brand" to="/welcome" @click="$emit('navigate')">
      <span class="admin-brand-mark">K</span>
      <span class="admin-brand-text">
        <strong>{{ siteName }}</strong>
        <small>内容管理后台</small>
      </span>
    </router-link>

    <nav class="admin-nav">
      <template v-for="group in visibleGroups" :key="group.title">
        <div class="admin-nav-group">{{ group.title }}</div>
        <router-link v-for="item in group.items"
                     :key="item.path"
                     :to="item.path"
                     class="admin-nav-item"
                     :class="{ active: isActive(item.path) }"
                     :title="collapsed ? item.title : ''"
                     @click="$emit('navigate')">
          <el-icon class="admin-nav-icon">
            <component :is="item.icon"/>
          </el-icon>
          <span class="admin-nav-label">{{ item.title }}</span>
        </router-link>
      </template>
    </nav>

    <div class="admin-sidebar-footer">
      © {{ year }} {{ siteName }} · Admin v3
    </div>
  </aside>
</template>

<script>
  export default {
    name: "AppSidebar",
    props: {
      collapsed: {
        type: Boolean,
        default: false
      }
    },
    emits: ["navigate"],

    data() {
      return {
        year: new Date().getFullYear(),
        // roles：可访问该菜单的角色等级（1 主理人 2 管理员 3 作者 4 普通用户 5 AI 站长）
        groups: [{
          title: "概览",
          items: [{
            icon: "DataBoard",
            path: "/welcome",
            title: "仪表盘",
            roles: [1, 2, 3, 5]
          }, {
            icon: "Histogram",
            path: "/main",
            title: "数据统计",
            roles: [1]
          }]
        }, {
          title: "内容",
          items: [{
            icon: "Tickets",
            path: "/postList",
            title: "文章管理",
            roles: [1, 3, 5]
          }, {
            icon: "Document",
            path: "/changelogList",
            title: "更新日志",
            roles: [1]
          }, {
            icon: "Collection",
            path: "/sortList",
            title: "分类管理",
            roles: [1, 5]
          }, {
            icon: "Paperclip",
            path: "/resourceList",
            title: "资源管理",
            roles: [1, 2, 5]
          }]
        }, {
          title: "互动",
          items: [{
            icon: "ChatDotRound",
            path: "/commentList",
            title: "评论管理",
            roles: [1, 2, 5]
          }, {
            icon: "Message",
            path: "/treeHoleList",
            title: "留言管理",
            roles: [1, 2, 5]
          }, {
            icon: "ChatLineRound",
            path: "/barrageList",
            title: "弹幕管理",
            roles: [1, 2, 5]
          }, {
            icon: "Connection",
            path: "/friendList",
            title: "友链管理",
            roles: [1, 2]
          }, {
            icon: "CreditCard",
            path: "/sponsorList",
            title: "赞助记录",
            roles: [1, 2]
          }]
        }, {
          title: "娱乐",
          items: [{
            icon: "VideoCamera",
            path: "/animeList",
            title: "追番管理",
            roles: [1, 2, 5]
          }, {
            icon: "Headset",
            path: "/musicList",
            title: "音乐管理",
            roles: [1, 2, 5]
          }, {
            icon: "Picture",
            path: "/photoList",
            title: "相片管理",
            roles: [1, 2, 5]
          }]
        }, {
          title: "系统",
          items: [{
            icon: "User",
            path: "/userList",
            title: "用户管理",
            roles: [1]
          }, {
            icon: "Setting",
            path: "/webEdit",
            title: "网站设置",
            roles: [1, 5]
          }, {
            icon: "Message",
            path: "/emailConfig",
            title: "邮箱配置",
            roles: [1]
          }, {
            icon: "Operation",
            path: "/configList",
            title: "配置管理",
            roles: [1]
          }, {
            icon: "Menu",
            path: "/navManage",
            title: "导航管理",
            roles: [1]
          }]
        }]
      }
    },

    computed: {
      // 当前后台登录者的角色等级：站长固定 1 级，其余按 userType 归一化
      roleLevel() {
        const admin = this.$store.state.currentAdmin || {};
        if (admin.isBoss) {
          return 1;
        }
        // 与后端 resolveRole 兜底一致：站长固定 id=1 且 user_type=1 时按主理人处理，
        // 避免线上 boss-username 与用户名不一致时后台看不到「网站设置」
        const bossId = this.$constant && Number(this.$constant.userId);
        if (Number(admin.id) === bossId && Number(admin.userType) === 1) {
          return 1;
        }
        const type = Number(admin.userType);
        if (type === 5) {
          // AI 站长：独立等级，不走 1-4 的兼容归一化
          return 5;
        }
        if (type === 1) {
          return 2; // 兼容未迁移的旧管理员数据
        }
        return type >= 2 && type <= 4 ? type : 4;
      },

      siteName() {
        let webName = this.$store.state.webInfo.webName;
        return this.$common.isEmpty(webName) ? "King" : webName;
      },

      visibleGroups() {
        return this.groups
          .map(group => ({
            title: group.title,
            items: group.items.filter(item => item.roles.includes(this.roleLevel))
          }))
          .filter(group => group.items.length > 0);
      }
    },

    methods: {
      isActive(path) {
        // 文章编辑页归属文章管理
        if (path === "/postList" && this.$route.path === "/postEdit") {
          return true;
        }
        return this.$route.path === path;
      }
    }
  }
</script>
