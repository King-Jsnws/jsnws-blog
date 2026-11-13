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
        // 每项 isBoss 为 true 时仅站长可见
        groups: [{
          title: "概览",
          items: [{
            icon: "DataBoard",
            path: "/welcome",
            title: "仪表盘"
          }, {
            icon: "Histogram",
            path: "/main",
            title: "数据统计",
            isBoss: true
          }]
        }, {
          title: "内容",
          items: [{
            icon: "Tickets",
            path: "/postList",
            title: "文章管理"
          }, {
            icon: "Collection",
            path: "/sortList",
            title: "分类管理",
            isBoss: true
          }, {
            icon: "Paperclip",
            path: "/resourceList",
            title: "资源管理",
            isBoss: true
          }]
        }, {
          title: "互动",
          items: [{
            icon: "ChatDotRound",
            path: "/commentList",
            title: "评论管理"
          }, {
            icon: "Message",
            path: "/treeHoleList",
            title: "留言管理",
            isBoss: true
          }, {
            icon: "Connection",
            path: "/friendList",
            title: "友链管理",
            isBoss: true
          }, {
            icon: "CreditCard",
            path: "/sponsorList",
            title: "赞助记录",
            isBoss: true
          }]
        }, {
          title: "娱乐",
          items: [{
            icon: "VideoCamera",
            path: "/animeList",
            title: "追番管理",
            isBoss: true
          }, {
            icon: "Headset",
            path: "/musicList",
            title: "音乐管理",
            isBoss: true
          }, {
            icon: "Picture",
            path: "/photoList",
            title: "相片管理",
            isBoss: true
          }]
        }, {
          title: "系统",
          items: [{
            icon: "User",
            path: "/userList",
            title: "用户管理",
            isBoss: true
          }, {
            icon: "Setting",
            path: "/webEdit",
            title: "网站设置",
            isBoss: true
          }, {
            icon: "Message",
            path: "/emailConfig",
            title: "邮箱配置",
            isBoss: true
          }, {
            icon: "Operation",
            path: "/configList",
            title: "配置管理",
            isBoss: true
          }]
        }]
      }
    },

    computed: {
      isBoss() {
        return this.$store.state.currentAdmin.isBoss;
      },

      siteName() {
        let webName = this.$store.state.webInfo.webName;
        return this.$common.isEmpty(webName) ? "King" : webName;
      },

      visibleGroups() {
        return this.groups
          .map(group => ({
            title: group.title,
            items: group.items.filter(item => this.isBoss || !item.isBoss)
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
