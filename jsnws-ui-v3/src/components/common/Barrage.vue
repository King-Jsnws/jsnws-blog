<template>
  <div class="barrage-container">
    <div v-for="(item, index) in barrageList"
         :key="item.id"
         class="barrage-item"
         :style="itemStyle(index)">
      <img v-if="item.avatar" class="barrage-avatar" :src="item.avatar" alt="">
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
        const laneHeight = 40
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
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    pointer-events: none;
  }

  .barrage-item {
    position: absolute;
    left: 100%;
    display: flex;
    align-items: center;
    white-space: nowrap;
    color: #fff;
    font-size: 14px;
    animation-name: barrage-move;
    animation-timing-function: linear;
    animation-fill-mode: both;
  }

  .barrage-avatar {
    width: 24px;
    height: 24px;
    margin-right: 6px;
    border-radius: 50%;
    object-fit: cover;
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
