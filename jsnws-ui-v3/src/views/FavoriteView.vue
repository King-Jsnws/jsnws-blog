<template>
  <div>
    <div class="favorite-container">
      <!-- 内容 -->
      <div class="favorite-content">
        <!-- 友链 -->
        <div class="my-animation-hideToShow">
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

  import MyFooter from "../components/common/MyFooter.vue";

  export default {
    components: {
      MyFooter,
      Friend
    }
  }
</script>

<style scoped>
  .favorite-container {
    /* 顶部留白与归档/随笔/图谱一致：内容区从固定导航栏下方 60px 开始 */
    padding: 80px 25px 25px;
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
    border-radius: 20px;
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
    margin: 0 auto;
    max-width: 1200px;
  }

  @media screen and (max-width: 906px) {
    .card-container {
      margin-top: 0;
    }
  }

  @media screen and (max-width: 600px) {
    .favorite-container {
      /* 与随笔页移动端留白一致 */
      padding: 40px 25px 25px;
    }
  }
</style>
