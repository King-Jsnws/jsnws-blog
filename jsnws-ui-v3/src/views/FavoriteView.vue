<template>
  <div>
    <!-- 两句诗：与关于我页共用同一组件，背景图取图规则一致 -->
    <div class="my-animation-slide-top">
      <TwoPoem :isHitokoto="false"></TwoPoem>
    </div>

    <div class="favorite-container">
      <!-- 内容 -->
      <div class="favorite-content">
        <!-- 友链 -->
        <div class="my-animation-slide-bottom">
          <friend></friend>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div style="background: var(--favoriteBg)">
      <MyFooter></MyFooter>
    </div>
  </div>
</template>

<script>
  import Friend from "../components/common/Friend.vue";
  import TwoPoem from "../components/common/TwoPoem.vue";

  import MyFooter from "../components/common/MyFooter.vue";

  export default {
    components: {
      MyFooter,
      Friend,
      TwoPoem
    }
  }
</script>

<style scoped>
  .favorite-container {
    /* 只承担整页底色，宽度与缩进统一交给 .favorite-content，避免与左右留白叠加 */
    padding: 0;
    background: var(--favoriteBg);
  }

  .card-container {
    display: flex;
    flex-wrap: wrap;
    margin-top: 60px;
  }

  .card-item {
    transition: all 0.3s;
    position: relative;
    width: 250px;
    height: 120px;
    border-radius: var(--ui-card-radius);
    animation: hideToShow 1s ease-in-out;
    cursor: pointer;
    overflow: hidden;
    margin: 10px;
    color: var(--white);
  }

  .card-item:hover {
    transform: translateY(-6px);
  }

  .card-name {
    font-weight: bold;
    font-size: 25px;
  }

  .card-name:after {
    top: 50px;
    width: 22px;
    left: 26px;
    height: 2px;
    background: var(--white);
    content: "";
    border-radius: 1px;
    position: absolute;
  }

  .card-desc {
    font-weight: bold;
    margin-top: 15px;
  }

  .favorite-content {
    /* 与全站内容页同一套容器规则：92vw / 112rem 居中，左右 16px，底部统一留白 */
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
  }

  @media screen and (max-width: 906px) {
    .card-container {
      margin-top: 0;
    }
  }

  @media screen and (max-width: 600px) {
    .favorite-content {
      /* 窄屏沿用同一套宽度与内边距变量，不再单独切换到百分比 */
      width: var(--ui-page-inline);
      padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    }
  }
</style>
