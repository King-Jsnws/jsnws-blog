<template>
  <div class="admin-shell"
       :class="{ 'is-collapsed': collapsed, 'is-mobile-open': mobileOpen }">
    <div class="admin-overlay" @click="mobileOpen = false"></div>
    <AppSidebar :collapsed="collapsed" @navigate="mobileOpen = false"/>
    <div class="admin-body">
      <MyHeader :title="pageTitle" :collapsed="collapsed" @toggle="toggleSidebar"/>
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="admin-fade" mode="out-in">
            <component :is="Component"/>
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script>
  import MyHeader from "../components/admin/MyHeader.vue";
  // Sidebar 与 Element Plus 内置组件重名，重命名后注册
  import AppSidebar from "../components/admin/Sidebar.vue";

  // 顶栏面包屑标题
  const PAGE_TITLES = {
    "/welcome": "仪表盘",
    "/main": "数据统计",
    "/postList": "文章管理",
    "/postEdit": "文章编辑",
    "/sortList": "分类管理",
    "/resourceList": "资源管理",
    "/animeList": "追番管理",
    "/musicList": "音乐管理",
    "/friendList": "友链管理",
    "/sponsorList": "赞助记录",
    "/photoList": "相片管理",
    "/commentList": "评论管理",
    "/treeHoleList": "留言管理",
    "/userList": "用户管理",
    "/webEdit": "网站设置",
    "/configList": "配置管理",
    "/emailConfig": "邮箱配置"
  };

  export default {
    name: "AdminLayout",
    components: {
      MyHeader,
      AppSidebar
    },

    data() {
      return {
        collapsed: localStorage.getItem("adminSidebarCollapsed") === "1",
        mobileOpen: false,
        mediaQuery: null
      }
    },

    computed: {
      pageTitle() {
        return PAGE_TITLES[this.$route.path] || "后台管理";
      }
    },

    created() {
      let sysConfig = this.$store.state.sysConfig;
      if (!this.$common.isEmpty(sysConfig) && !this.$common.isEmpty(sysConfig['webStaticResourcePrefix'])) {
        let root = document.querySelector(":root");
        let webStaticResourcePrefix = sysConfig['webStaticResourcePrefix'];
        root.style.setProperty("--backgroundPicture", "url(" + webStaticResourcePrefix + "assets/backgroundPicture.jpg)");
        const font = new FontFace("King-font", "url(" + webStaticResourcePrefix + "assets/font.woff2)");
        font.load();
        document.fonts.add(font);
      }

      this.mediaQuery = window.matchMedia("(max-width: 991px)");
      this.mediaQuery.addEventListener("change", this.handleMediaChange);
    },

    beforeUnmount() {
      if (this.mediaQuery) {
        this.mediaQuery.removeEventListener("change", this.handleMediaChange);
      }
    },

    methods: {
      handleMediaChange(event) {
        if (!event.matches) {
          this.mobileOpen = false;
        }
      },

      toggleSidebar() {
        if (this.mediaQuery && this.mediaQuery.matches) {
          this.mobileOpen = !this.mobileOpen;
          return;
        }
        this.collapsed = !this.collapsed;
        localStorage.setItem("adminSidebarCollapsed", this.collapsed ? "1" : "0");
      }
    }
  }
</script>
