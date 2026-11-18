<template>
  <div class="barrage-container">
    <div v-for="(item, index) in barrageList"
         :key="item.id"
         class="barrage-item"
         :style="itemStyle(index)">
      <img v-if="item.avatar" class="barrage-avatar" :src="item.avatar" alt="">
      <span v-else class="barrage-avatar barrage-avatar-fallback">匿</span>
      <span class="barrage-name">{{ item.nickname }}</span>
      <span class="barrage-msg">{{ item.msg }}</span>
    </div>
  </div>
</template>

<script>
  // vue-baberrage 的轻量替代：按轨道循环滚动，保留原组件 barrageList / loop 两个入参
  export default {
    name: 'Barrage',
    props: {
      barrageList: {
        type: Array,
        default: () => []
      },
      loop: {
        type: Boolean,
        default: false
      }
    },
    methods: {
      itemStyle(index) {
        const item = this.barrageList[index] || {}
        const lanes = 5
        // 胶囊高度 40px，轨道间距放大到 48px，保证滚动时上下不重叠、不被裁切
        const laneHeight = 48
        return {
          top: (index % lanes) * laneHeight + 10 + 'px',
          animationDuration: (item.time || 10) + 's',
          animationDelay: index * 0.6 + 's',
          animationIterationCount: this.loop ? 'infinite' : '1'
        }
      }
    }
  }
</script>

<style scoped>
  .barrage-container {
    --barrage-bg: rgba(20, 58, 86, 0.78);
    --barrage-border: rgba(176, 208, 231, 0.35);
    --barrage-text: #ffffff;
    --barrage-name: #c9ecf8;
    --barrage-accent: #2ed573;
    --barrage-shadow: 0 2px 10px rgba(6, 24, 38, 0.35);
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    pointer-events: none;
  }

  /* 深色模式：底色更深、描边减弱，避免在深色背景上发灰 */
  html.dark .barrage-container {
    --barrage-bg: rgba(8, 28, 44, 0.82);
    --barrage-border: rgba(255, 255, 255, 0.16);
    --barrage-name: #b9dcea;
    --barrage-shadow: 0 2px 10px rgba(0, 0, 0, 0.45);
  }

  /* 胶囊：半透明深蓝底 + 轻微模糊 + 浅蓝细描边，透出背景图同时保证文字可读 */
  .barrage-item {
    position: absolute;
    left: 100%;
    display: inline-flex;
    align-items: center;
    gap: 9px;
    box-sizing: border-box;
    max-width: calc(100vw - 24px);
    height: 40px;
    padding: 0 18px 0 5px;
    border: 1px solid var(--barrage-border);
    border-radius: 20px;
    background: var(--barrage-bg);
    backdrop-filter: blur(6px) saturate(120%);
    -webkit-backdrop-filter: blur(6px) saturate(120%);
    box-shadow: var(--barrage-shadow);
    color: var(--barrage-text);
    font-size: 15px;
    font-weight: 600;
    line-height: 1;
    white-space: nowrap;
    animation-name: barrage-move;
    animation-timing-function: linear;
    animation-fill-mode: both;
  }

  /* 头像：24px 圆形，外圈 2px 取自参考图的绿色描边 */
  .barrage-avatar {
    flex: 0 0 auto;
    box-sizing: border-box;
    width: 24px;
    height: 24px;
    border: 2px solid var(--barrage-accent);
    border-radius: 50%;
    object-fit: cover;
  }

  /* 无头像时与参考图一致：绿色圆 + 白色「匿」字 */
  .barrage-avatar-fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: var(--barrage-accent);
    color: #fff;
    font-size: 11px;
    font-weight: 700;
    line-height: 1;
  }

  /* 昵称：比正文小一号、更浅，与内容形成层级；过长时省略，不撑出屏幕 */
  .barrage-name {
    flex: 0 1 auto;
    min-width: 0;
    max-width: 9em;
    overflow: hidden;
    color: var(--barrage-name);
    font-size: 13px;
    font-weight: 500;
    text-overflow: ellipsis;
  }

  .barrage-msg {
    flex: 0 1 auto;
    min-width: 0;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  @media screen and (max-width: 768px) {
    .barrage-item {
      height: 34px;
      padding: 0 14px 0 4px;
      gap: 7px;
      border-radius: 17px;
      font-size: 13px;
    }

    .barrage-avatar {
      width: 20px;
      height: 20px;
      border-width: 1.5px;
    }

    .barrage-avatar-fallback {
      font-size: 10px;
    }

    .barrage-name {
      max-width: 6em;
      font-size: 12px;
    }
  }

  @keyframes barrage-move {
    from {
      transform: translateX(0);
    }
    to {
      transform: translateX(calc(-100vw - 100%));
    }
  }
</style>
