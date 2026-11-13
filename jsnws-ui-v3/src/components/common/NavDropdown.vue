<template>
  <div class="nav-dropdown"
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
      go(path) {
        this.close()
        if (this.$route.path !== path) {
          this.$router.push(path)
        }
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
</style>
