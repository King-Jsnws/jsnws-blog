<template>
  <div class="nav-dropdown"
       :class="[variant ? 'nav-dropdown--' + variant : '', { 'is-inline-root': inline }]"
       ref="root"
       @mouseenter="handleMouseEnter"
       @mouseleave="handleMouseLeave">
    <button type="button"
            class="nav-dropdown-trigger"
            :class="{ 'is-open': visible, 'is-active': active }"
            @click="handleTriggerClick">
      <el-icon class="trigger-main-icon"><component :is="triggerIcon" /></el-icon>
      <span class="trigger-text">{{ label }}</span>
      <el-icon class="trigger-arrow"><ArrowUp /></el-icon>
    </button>

    <transition name="nav-dropdown-fade">
      <div v-show="visible"
           class="nav-dropdown-panel"
           :class="{ 'is-inline': inline }">
        <span class="panel-arrow"></span>
        <div v-for="item in items"
             :key="item.to"
             class="nav-dropdown-item"
             :class="{ 'is-current': isCurrent(item.to) }"
             @click="go(item.to)">
          <span class="item-icon"><el-icon><component :is="item.icon" /></el-icon></span>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
  import { ArrowUp } from '@element-plus/icons-vue'

  export default {
    name: 'NavDropdown',
    components: {
      ArrowUp
    },
    props: {
      // 触发器文案，如「文章」「关于」
      label: {
        type: String,
        required: true
      },
      // 触发器左侧图标
      triggerIcon: {
        type: Object,
        required: true
      },
      // 面板项：{ label, to, icon }
      items: {
        type: Array,
        default: () => []
      },
      // 抽屉里使用行内面板，避免绝对定位被抽屉的滚动容器裁掉
      inline: {
        type: Boolean,
        default: false
      },
      // 当前路由命中面板任一项时高亮触发器
      active: {
        type: Boolean,
        default: false
      },
      // 面板变体：article = 文章下拉的大圆角卡片样式，其余保持默认
      variant: {
        type: String,
        default: ''
      }
    },
    data() {
      return {
        visible: false,
        // 移出后的收起定时器：留出从胶囊移到面板的过渡时间，避免闪断
        closeTimer: null
      }
    },
    mounted() {
      document.addEventListener('click', this.handleOutsideClick)
      document.addEventListener('keydown', this.handleKeydown)
    },
    beforeUnmount() {
      clearTimeout(this.closeTimer)
      document.removeEventListener('click', this.handleOutsideClick)
      document.removeEventListener('keydown', this.handleKeydown)
    },
    methods: {
      // 桌面端悬停触发；抽屉里的 inline 面板保持点击展开
      handleMouseEnter() {
        if (this.inline) {
          return
        }
        this.open()
      },
      handleMouseLeave() {
        if (this.inline) {
          return
        }
        this.scheduleClose()
      },
      handleTriggerClick() {
        if (this.inline) {
          this.toggle()
          return
        }
        // 桌面端已由悬停展开，点击不再反向关闭
        if (!this.visible) {
          this.open()
        }
      },
      open() {
        clearTimeout(this.closeTimer)
        this.visible = true
      },
      scheduleClose() {
        clearTimeout(this.closeTimer)
        this.closeTimer = setTimeout(() => {
          this.visible = false
        }, 160)
      },
      toggle() {
        this.visible = !this.visible
      },
      close() {
        clearTimeout(this.closeTimer)
        this.visible = false
      },
      handleOutsideClick(event) {
        if (this.$refs.root && !this.$refs.root.contains(event.target)) {
          this.close()
        }
      },
      handleKeydown(event) {
        if (event.key === 'Escape') {
          this.close()
        }
      },
      // to 支持带 hash 的站内地址，按完整路径比对避免重复跳转
      go(path) {
        this.close()
        if (this.$route.fullPath !== path) {
          this.$router.push(path)
        }
      },
      // 当前页面是否命中该菜单项，用于「当前项」浅蓝底高亮
      isCurrent(path) {
        if (!path) {
          return false
        }
        return this.$route.path === path
      }
    }
  }
</script>

<style scoped>
  .nav-dropdown {
    position: relative;
    display: inline-flex;
    align-items: center;
  }

  /* 与导航 .nav-pill 同规格：透明底 + 36px 高 + 14px 横向内边距 + 12px 圆角 + 白字 */
  .nav-dropdown-trigger {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 36px;
    padding: 0 14px;
    border: none;
    border-radius: 12px;
    background: transparent;
    color: #ffffff;
    font-size: 14px;
    font-weight: 500;
    letter-spacing: 1px;
    line-height: 1;
    cursor: pointer;
    transition: background 0.2s ease, color 0.2s ease;
  }

  /* 悬停与展开态统一用白色半透明底 */
  .nav-dropdown-trigger:hover,
  .nav-dropdown-trigger.is-open {
    background: rgba(255, 255, 255, 0.16);
  }

  /* 当前页面属于该下拉时高亮：更亮的白色半透明底 */
  .nav-dropdown-trigger.is-active {
    background: rgba(255, 255, 255, 0.26);
    color: #ffffff;
  }

  .nav-dropdown-trigger.is-active:hover,
  .nav-dropdown-trigger.is-active.is-open {
    background: rgba(255, 255, 255, 0.34);
  }

  .trigger-main-icon {
    font-size: 15px;
    color: inherit;
  }

  .trigger-text {
    color: inherit;
  }

  .trigger-arrow {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    opacity: 0.92;
    transform: rotate(0deg);
    transform-origin: center center;
    transition: transform 0.2s ease;
  }

  /* 展开时箭头朝上变朝下，收起自动回到 0° */
  .nav-dropdown-trigger.is-open .trigger-arrow {
    transform: rotate(180deg);
  }

  .nav-dropdown-panel {
    position: absolute;
    top: 44px;
    left: 0;
    z-index: 120;
    width: 196px;
    padding: 8px;
    background: var(--panelBg);
    /* 按参考图：单层 1px 近黑描边，无外发光/投影；保留 1px 边框宽度，尺寸不跳 */
    border: 1px solid var(--panelBorder);
    border-radius: 10px;
    box-shadow: none;
  }

  /* 抽屉内：外层撑满一行，行内面板才能拿到足够宽度（否则菜单项会逐字换行） */
  .nav-dropdown.is-inline-root {
    display: block;
    width: 100%;
  }

  .nav-dropdown-panel.is-inline {
    position: relative;
    top: 10px;
    left: 0;
    width: 100%;
  }

  .panel-arrow {
    position: absolute;
    top: -7px;
    left: 26px;
    width: 12px;
    height: 12px;
    background: var(--panelBg);
    /* 缺口同样用近黑描边，和面板外框连成一条线（参考图顶部小三角） */
    border-top: 1px solid var(--panelBorder);
    border-left: 1px solid var(--panelBorder);
    transform: rotate(45deg);
    border-top-left-radius: 2px;
  }

  .nav-dropdown-item {
    display: flex;
    align-items: center;
    gap: 10px;
    height: 42px;
    padding: 0 10px;
    border-radius: 9px;
    color: var(--panelTextSecondary);
    font-size: 14px;
    letter-spacing: 0.5px;
    cursor: pointer;
    transition: background 0.18s ease, color 0.18s ease;
  }

  .nav-dropdown-item:hover {
    background: var(--panelHoverBg);
    color: var(--panelText);
  }

  .item-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 7px;
    background: var(--panelIconBg);
    color: var(--panelTextSecondary);
    font-size: 14px;
  }

  .nav-dropdown-item:hover .item-icon {
    background: var(--panelIconHoverBg);
    color: var(--panelText);
  }

  .nav-dropdown-fade-enter-active,
  .nav-dropdown-fade-leave-active {
    transition: opacity 0.18s ease, transform 0.18s ease;
  }

  .nav-dropdown-fade-enter-from,
  .nav-dropdown-fade-leave-to {
    opacity: 0;
    transform: translateY(-4px);
  }

  /* ---------- 文章下拉：白色大圆角卡片 + 实心图标 + 浅蓝悬停/当前项 ---------- */
  .nav-dropdown--article .nav-dropdown-panel {
    top: 46px;
    width: 216px;
    padding: 12px;
    border: 1px solid color-mix(in srgb, var(--panelBorder) 18%, transparent);
    border-radius: 20px;
    box-shadow: 0 16px 40px rgba(15, 23, 42, 0.14), 0 3px 10px rgba(15, 23, 42, 0.06);
  }

  /* 大圆角卡片不需要顶部小三角 */
  .nav-dropdown--article .panel-arrow {
    display: none;
  }

  .nav-dropdown--article .nav-dropdown-item {
    gap: 12px;
    height: 46px;
    padding: 0 12px;
    border-radius: 12px;
    color: var(--panelText);
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 0.2px;
  }

  .nav-dropdown--article .nav-dropdown-item:hover,
  .nav-dropdown--article .nav-dropdown-item.is-current {
    background: var(--panelActiveBg);
    color: var(--panelText);
  }

  /* 实心图标：去掉默认灰底方块，直接以字形色呈现 */
  .nav-dropdown--article .item-icon {
    width: 22px;
    height: 22px;
    border-radius: 0;
    background: transparent;
    color: var(--panelText);
    font-size: 21px;
  }

  .nav-dropdown--article .nav-dropdown-item:hover .item-icon,
  .nav-dropdown--article .nav-dropdown-item.is-current .item-icon {
    background: transparent;
    color: var(--panelText);
  }

  /* 抽屉内的行内面板：同款大圆角卡片 */
  .nav-dropdown--article .nav-dropdown-panel.is-inline {
    top: 10px;
    padding: 12px;
  }

  /* 深色主题下投影加深，浅色面板阴影在深底上几乎不可见 */
  :global(html.dark) .nav-dropdown--article .nav-dropdown-panel {
    box-shadow: 0 18px 44px rgba(0, 0, 0, 0.46), 0 3px 10px rgba(0, 0, 0, 0.3);
  }
</style>
