<template>
  <!-- 站点信息卡：首页及各页面左侧栏共用，保持结构与样式唯一 -->
  <div class="card-content1 background-opacity">
    <div class="card-banner"></div>
    <el-avatar class="user-avatar" :size="90" :src="webInfo.avatar || '/images/default-avatar.webp'"></el-avatar>
    <div class="web-name">{{ownerName}}</div>
    <div class="web-desc">{{ webInfo.description || webInfo.footer }}</div>
    <div class="web-info">
      <div class="blog-info-box">
        <span>文章</span>
        <span class="blog-info-num">{{ $store.getters.articleTotal }}</span>
      </div>
      <div class="blog-info-box">
        <span>分类</span>
        <span class="blog-info-num">{{ sortInfo.length }}</span>
      </div>
      <div class="blog-info-box">
        <span>总浏览量</span>
        <span class="blog-info-num">{{ viewTotal || 0 }}</span>
      </div>
    </div>
    <div class="web-social" v-if="hasSocial">
      <a v-if="webInfo.wechat" href="javascript:void(0)" title="微信" @click="wechatDialog = true">
        <i class="fa fa-weixin"></i>
      </a>
      <a v-if="webInfo.qq" :href="webInfo.qq" target="_blank" title="QQ">
        <i class="fa fa-qq"></i>
      </a>
      <a v-if="webInfo.github" :href="webInfo.github" target="_blank" title="GitHub">
        <!-- GitHub 官方 mark 图标（Octicon mark-github），currentColor 跟随主题/hover 色 -->
        <svg class="github-icon" viewBox="0 0 16 16" width="17" height="17" fill="currentColor"
             style="vertical-align: -2px" aria-hidden="true">
          <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0 0 16 8c0-4.42-3.58-8-8-8z"/>
        </svg>
      </a>
      <a v-if="webInfo.gitee" :href="webInfo.gitee" target="_blank" title="Gitee">
        <!-- Gitee 官方标志：实心红圆 + 方 G 镂空。G 外框 11px、笔画 2px、内圈 7px、
             右上开口 3px、内伸横杠 2px（与开口留 3px 间隙），几何按 17px 像素网格对齐、转角圆弧收口 -->
        <svg class="gitee-icon" viewBox="0 0 16 16" width="17" height="17"
             style="vertical-align: -2px" aria-hidden="true">
          <path fill="currentColor" fill-rule="evenodd"
                d="M0 8a8 8 0 1 0 16 0a8 8 0 1 0-16 0ZM3.82 2.82H12.18A1 1 0 0 1 13.18 3.82V4.21A0.5 0.5 0 0 1 12.68 4.71H5.21A0.5 0.5 0 0 0 4.71 5.21V10.79A0.5 0.5 0 0 0 5.21 11.29H10.79A0.5 0.5 0 0 0 11.29 10.79V9.91A0.5 0.5 0 0 0 10.79 9.41H8.03A0.5 0.5 0 0 1 7.53 8.91V8.03A0.5 0.5 0 0 1 8.03 7.53H12.68A0.5 0.5 0 0 1 13.18 8.03V12.18A1 1 0 0 1 12.18 13.18H3.82A1 1 0 0 1 2.82 12.18V3.82A1 1 0 0 1 3.82 2.82Z"/>
        </svg>
      </a>
      <a v-if="webInfo.email" :href="'mailto:' + webInfo.email" title="邮箱">
        <i class="fa fa-envelope-o"></i>
      </a>
      <a v-if="webInfo.bilibili" :href="webInfo.bilibili" target="_blank" title="Bilibili">
        <!-- viewBox 裁到图形实际边界（x 3~21、y 3~20），与企鹅同为 17px 视觉尺寸 -->
        <svg class="bili-icon" viewBox="3 3 18 17" width="17" height="17" fill="currentColor" style="vertical-align: -2px">
          <path d="M7.2 3.2 9 5h6l1.8-1.8 1.4 1.4L17 6h1.5A2.5 2.5 0 0 1 21 8.5v9A2.5 2.5 0 0 1 18.5 20h-13A2.5 2.5 0 0 1 3 17.5v-9A2.5 2.5 0 0 1 5.5 6H7L5.8 4.6l1.4-1.4zM5.5 8a.5.5 0 0 0-.5.5v9c0 .28.22.5.5.5h13a.5.5 0 0 0 .5-.5v-9a.5.5 0 0 0-.5-.5h-13z"/>
          <circle cx="9" cy="12" r="1.2"/>
          <circle cx="15" cy="12" r="1.2"/>
        </svg>
      </a>
    </div>

    <!-- 微信二维码 -->
    <el-dialog title="微信"
               v-model="wechatDialog"
               width="300px"
               :append-to-body="true"
               destroy-on-close
               center>
      <img class="wechat-qrcode" :src="webInfo.wechat" alt="微信二维码">
    </el-dialog>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        wechatDialog: false,
        // 各页面共用：由本组件统一请求访客汇总，取不到时为 null
        visitSummary: null
      }
    },
    created() {
      this.getVisitSummary();
    },
    computed: {
      viewTotal() {
        return this.formatStatNumber(this.visitSummary && this.visitSummary.page_view_total)
          || this.formatStatNumber(this.webInfo.historyAllCount);
      },
      webInfo() {
        return this.$store.state.webInfo;
      },
      // 卡片上的名字是站长名称：取站长账号昵称，缺失时兜底站点名称（不回落到登录用户名）
      ownerName() {
        let boss = this.$store.state.bossInfo || {};
        if (!this.$common.isEmpty(boss.username)) {
          return boss.username;
        }
        return this.$common.isEmpty(this.webInfo.webName) ? "站长" : this.webInfo.webName;
      },
      sortInfo() {
        return this.$store.getters.navigationBar;
      },
      hasSocial() {
        let w = this.webInfo;
        return !!w && !!(w.wechat || w.qq || w.github || w.gitee || w.email || w.bilibili);
      }
    },
    methods: {
      getVisitSummary() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getVisitSummary")
          .then((res) => {
            this.visitSummary = res && res.data && typeof res.data === "object" ? res.data : null;
          })
          .catch(() => {
            this.visitSummary = null;
          });
      },
      formatStatNumber(value) {
        if (value === null || value === undefined || value === "") {
          return "";
        }
        const num = Number(value);
        return Number.isFinite(num) ? num.toLocaleString("en-US") : "";
      }
    }
  };
</script>

<style scoped>
  /* 与分类 / 标签卡同一套外壳：卡片底色 + 1px 描边 + 16px 圆角 + 1px 阴影 */
  .card-content1 {
    box-sizing: border-box;
    background: var(--ui-card-bg);
    display: flex;
    flex-direction: column;
    align-items: center;
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    box-shadow: var(--ui-card-shadow);
    position: relative;
    overflow: hidden;
  }

  .card-banner {
    width: 100%;
    height: 110px;
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0) 55%, var(--cardBg) 100%),
                url('/images/site-card-bg-sakura.2c9f74.webp') center 35% / cover no-repeat;
  }

  .user-avatar {
    margin-top: -45px;
    border: 4px solid var(--cardBg);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    z-index: 1;
  }

  .web-name {
    font-size: 20px;
    font-weight: 600;
    margin: 12px 0 6px;
    color: var(--fontColor);
  }

  .web-desc {
    max-width: 88%;
    font-size: 13px;
    color: var(--greyFont);
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .web-info {
    width: 88%;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    gap: 8px;
    margin: 16px 0 12px;
  }

  /* 统计项（文章 / 分类 / 总浏览量）各自加描边：与站内浅色边框同一套语言 */
  .blog-info-box {
    flex: 1 1 0;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 8px 4px 6px;
    font-size: 12px;
    color: var(--greyFont);
    /* 1px 浅灰描边 + 10px 圆角，无外发光/阴影；边框宽度恒定，尺寸不跳 */
    border: 1px solid var(--statBorder);
    border-radius: 10px;
    transition: border-color 0.18s ease, background-color 0.18s ease;
  }

  /* 悬停：边框由实线变虚线（线宽/圆角/颜色不变，border-style 不可动画，故只过渡颜色） */
  .blog-info-box:hover {
    border-style: dashed;
  }

  @media (prefers-reduced-motion: reduce) {
    .blog-info-box {
      transition: none;
    }
  }

  .blog-info-num {
    margin-top: 6px;
    font-size: 16px;
    font-weight: 600;
    color: var(--fontColor);
  }

  .web-social {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    font-size: 17px;
    margin-bottom: 18px;
  }

  .web-social a {
    display: flex;
    align-items: center;
    justify-content: center;
    /* 统一成企鹅图标的实际渲染框：17×17，图标在同一方形框内居中，基线一致 */
    width: 17px;
    height: 17px;
    margin: 0 9px;
    color: var(--greyFont);
    text-decoration: none;
    transition: color 0.3s;
  }

  /* 内联 SVG 图标（GitHub / bilibili）：尺寸与字体图标一致 */
  .web-social svg {
    width: 17px;
    height: 17px;
  }

  .web-social a:hover {
    color: var(--themeBackground);
  }

  /* 微信/QQ/B站图标单独用各自品牌色；仅在支持 hover 的设备生效，触屏不残留悬停色 */
  .web-social .fa-weixin,
  .web-social .fa-qq,
  .web-social svg {
    transition: color 0.2s ease;
  }

  @media (hover: hover) {
    .web-social a:hover .fa-weixin {
      color: #07c160;
    }

    .web-social a:hover .fa-qq {
      color: #12b7f5;
    }

    /* B站是 fill="currentColor" 的内联 SVG，改 color 即可生效；GitHub 不参与，保持主题 hover 色 */
    .web-social a:hover .bili-icon {
      color: #fb7299;
    }

    /* GitHub 官方 mark：悬停用官方深色 #1f2328；深色模式下深色会融入深底，换成官方高对比浅色 #f0f6fc */
    .web-social a:hover .github-icon {
      color: #1f2328;
    }

    html.dark .web-social a:hover .github-icon {
      color: #f0f6fc;
    }

    /* Gitee：常态跟随社交栏中性灰，hover 才显品牌红 */
    .web-social .gitee-icon {
      color: var(--greyFont);
    }

    /* Gitee 品牌红 */
    .web-social a:hover .gitee-icon {
      color: #c71d23;
    }

    html.dark .web-social a:hover .gitee-icon {
      color: #e5484d;
    }
  }

  .wechat-qrcode {
    display: block;
    width: 100%;
    max-width: 220px;
    margin: 0 auto;
  }
</style>
