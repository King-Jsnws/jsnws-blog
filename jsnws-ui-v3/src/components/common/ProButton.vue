<template>
  <div class="myButton" :class="{ 'myButton--ui': unified }">
    <div :style="beforeColor">{{info}}</div>
    <div :style="afterColor">{{info}}</div>
    <div :style="afterColor">{{info}}</div>
  </div>
</template>

<script>
  export default {
    props: {
      info: {
        type: String,
        default: "确定"
      },
      before: {
        type: String
      },
      after: {
        type: String
      },
      // 前台统一按钮外观：仅显式传入时生效，后台原有外观不受影响
      unified: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        beforeColor: {"background": this.before},
        afterColor: {"background": this.after}
      };
    }
  }
</script>

<style scoped>
  .myButton {
    cursor: pointer;
    user-select: none;
    position: relative;
    width: 66px;
    height: 33px;
    border-radius: 4px;
    color: var(--white);
    font-size: 14px;
    overflow: hidden;
  }

  .myButton div {
    width: 66px;
    height: 33px;
    line-height: 33px;
    border-radius: 4px;
    text-align: center;
    position: absolute;
  }

  .myButton div:nth-child(2) {
    width: 100px;
    transition: all 0.3s ease;
    transform: translateX(-120px) skewX(-30deg);
  }

  .myButton div:nth-child(3) {
    transition: all 0.3s ease;
    transform: translateX(-120px);
  }

  .myButton:hover div:nth-child(2) {
    transform: translateX(20px) skewX(-30deg);
  }

  .myButton:hover div:nth-child(3) {
    transform: translateX(0px);
  }

  /* 统一按钮外观（仅 unified 时生效）：与前台按钮同一套尺寸、圆角、阴影与按压反馈 */
  .myButton--ui {
    width: auto;
    min-width: 66px;
    height: 34px;
    border: 1px solid transparent;
    border-radius: var(--ui-btn-radius);
    background: var(--ui-accent-strong);
    box-shadow: var(--ui-btn-shadow);
    color: var(--ui-accent-contrast);
    font-size: 14px;
    font-weight: 500;
    transition: var(--ui-btn-transition);
  }

  .myButton--ui div {
    position: relative;
    width: auto;
    height: 34px;
    padding: 0 12px;
    border-radius: var(--ui-btn-radius);
    line-height: 32px;
    transition: none;
    transform: none;
  }

  .myButton--ui div:nth-child(1) {
    background: transparent !important;
  }

  .myButton--ui div:nth-child(2),
  .myButton--ui div:nth-child(3) {
    display: none;
  }

  .myButton--ui:hover {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    background: var(--ui-accent-strong-hover);
    box-shadow: var(--ui-btn-shadow-hover);
  }

  /* 按压瞬时缩放，与参考站 active:scale-95 一致 */
  .myButton--ui:active {
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.55);
    box-shadow: var(--ui-btn-shadow-hover);
    scale: 0.95;
  }
</style>
