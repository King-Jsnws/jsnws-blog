<template>
  <button type="button"
          class="theme-toggle"
          :title="isDark ? '切换到白天模式' : '切换到黑夜模式'"
          :aria-label="isDark ? '切换到白天模式' : '切换到黑夜模式'"
          @click="toggle">
    <el-icon v-if="isDark" class="theme-toggle-icon" :class="{ 'is-spin': spin }"><Sunny /></el-icon>
    <i v-else class="fa fa-moon-o theme-toggle-icon" :class="{ 'is-spin': spin }" aria-hidden="true"></i>
  </button>
</template>

<script>
  import { Sunny } from '@element-plus/icons-vue'
  import { THEME_DARK, toggleTheme } from '../../utils/theme'

  // 公开站点与后台共用的主题切换入口，状态统一取自 Vuex（store.state.theme）
  export default {
    name: 'ThemeToggle',
    components: {
      Sunny
    },
    props: {
      // 公开站点设置面板里的旋转动画图标
      spin: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      isDark() {
        return this.$store.state.theme === THEME_DARK
      }
    },
    emits: ['change'],
    methods: {
      toggle() {
        this.$store.commit('changeTheme', toggleTheme(this.$store.state.theme))
        this.$emit('change', this.$store.state.theme)
      }
    }
  }
</script>

<style scoped>
  .theme-toggle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    border: none;
    background: transparent;
    color: inherit;
    font-size: inherit;
    line-height: 1;
    cursor: pointer;
  }

  .theme-toggle-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  /* 沿用原有设置面板里太阳/月亮持续旋转的观感 */
  .theme-toggle-icon.is-spin {
    animation: rotate 2s linear infinite;
  }

  @media (prefers-reduced-motion: reduce) {
    .theme-toggle-icon.is-spin {
      animation: none;
    }
  }
</style>
