<template>
  <div class="poem-container myCenter my-animation-hideToShow"
       v-if="!$common.isEmpty(guShi.origin) || !$common.isEmpty(hitokoto.hitokoto)">
    <!-- 背景图片 -->
    <el-image class="my-el-image poem-image"
              style="position: absolute;margin-top: -50px"
              lazy
              :src="poemBackground"
              fit="cover">
      <template #error>
        <div class="image-slot"></div>
      </template>
    </el-image>
    <div class="poem-wrap">
      <div v-if="isShehui"><span>鬼畜全明星</span></div>
      <div v-else><span>{{isHitokoto?hitokoto.from:guShi.origin}}</span></div>
      <p class="poem">{{isHitokoto?hitokoto.hitokoto:guShi.content}}</p>
      <p class="info" v-if="!isShehui && (!isHitokoto || (isHitokoto && !$common.isEmpty(hitokoto.from_who)))">
        {{isHitokoto?hitokoto.from_who:guShi.author}}
      </p>
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      isHitokoto: {
        type: Boolean,
        default: true
      },
      isShehui: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        guShi: {
          "content": "...",
          "origin": "...",
          "author": "...",
          "category": "..."
        },
        hitokoto: {
          "hitokoto": "...",
          "from": "...",
          "from_who": "..."
        }
      };
    },
    created() {
      if (!this.isShehui) {
        if (this.isHitokoto) {
          this.getHitokoto();
        } else {
          this.getGuShi();
        }
      } else {
        this.hitokoto.from = "";
        this.hitokoto.from_who = "";
        this.sendShehui();
      }
    },

    computed: {
      // 与首页保持一致：后台指定的背景图 > 后台随机封面 > 本地默认背景图
      poemBackground() {
        const webInfo = this.$store.state.webInfo || {};
        if (!this.$common.isEmpty(webInfo.backgroundImage)) {
          // 老配置存的是未压缩的默认图路径，统一换成体积更小的 webp 版本
          return webInfo.backgroundImage === '/images/home-default-bg.jpg'
            ? '/images/home-default-bg.webp'
            : webInfo.backgroundImage;
        }
        if (!this.$common.isEmpty(webInfo.randomCover)) {
          return webInfo.randomCover[Math.floor(Math.random() * webInfo.randomCover.length)];
        }
        return '/images/home-default-bg.webp';
      }
    },

    methods: {
      sendShehui() {
        let that = this;
        let xhr = new XMLHttpRequest();
        xhr.open('get', this.$constant.shehui);
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4) {
            let shehui = xhr.responseText;
            that.hitokoto.hitokoto = shehui.substring(1, shehui.length - 1);
          }
        };
        xhr.send();
      },
      getGuShi() {
        let that = this;
        let xhr = new XMLHttpRequest();
        xhr.open('get', this.$constant.jinrishici);
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4) {
            that.guShi = JSON.parse(xhr.responseText);
          }
        };
        xhr.send();
      },
      getHitokoto() {
        let that = this;
        let xhr = new XMLHttpRequest();
        xhr.open('get', this.$constant.hitokoto);
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4) {
            that.hitokoto = JSON.parse(xhr.responseText);
          }
        }
        xhr.send();
      }
    }
  };
</script>
<style scoped>

  .poem-container {
    padding: 90px 0 40px;
    position: relative;
  }

  .poem-wrap {
    border-radius: 10px;
    z-index: 10;
    text-align: center;
    letter-spacing: 4px;
    font-weight: 300;
    width: 100%;
    max-width: 800px;
  }

  .poem-wrap div span {
    padding: 5px 10px;
    color: var(--white);
    font-size: 2em;
    border-radius: 5px;
  }

  .poem-wrap p {
    width: 100%;
    max-width: 800px;
    color: var(--white);
  }

  .poem-wrap p.poem {
    margin: 40px auto;
    font-size: 1.5em;
  }

  .poem-wrap p.info {
    margin: 20px auto 40px;
    font-size: 1.1em;
  }
</style>
