<template>
  <div>
    <!-- 两句诗：与关于我页共用同一组件，背景图取图规则一致 -->
    <div class="my-animation-slide-top">
      <TwoPoem :isHitokoto="false"></TwoPoem>
    </div>

    <div class="sponsor-container my-animation-slide-bottom">
      <!-- 左侧栏（与主页一致），与右侧内容同处一行、顶边齐平；日历按参考站放在右栏 -->
      <div class="sponsor-aside">
        <MyAside hide-calendar @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
      </div>

      <!-- 右侧内容：标题与正文同在一列，标题顶边自然与左侧卡片齐平 -->
      <div class="sponsor-main">
        <!-- 标题 -->
        <div class="sponsor-header">
          <div class="sponsor-title-row">
            <div class="sponsor-title-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                <path d="M12 20.5S4 15.2 4 9.7A4.6 4.6 0 0 1 12 6.4a4.6 4.6 0 0 1 8 3.3c0 5.5-8 10.8-8 10.8z" fill="#fff"/>
              </svg>
            </div>
            <div class="sponsor-title">赞助支持</div>
          </div>
          <div class="sponsor-subtitle">“ 感谢每一份支持 ”</div>
          <div class="sponsor-tip">
            <svg class="sponsor-tip-icon" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <circle cx="12" cy="12" r="10.5" fill="none" stroke="currentColor" stroke-width="1.4"/>
              <path d="M12 6.8v7.4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              <circle cx="12" cy="17.2" r="1.1" fill="currentColor"/>
            </svg>
            <span>您的赞助将用于服务器维护、内容创作和功能开发，帮助我持续提供优质内容。</span>
          </div>
        </div>

        <!-- 右侧正文 -->
        <div class="sponsor-page">

      <!-- 收款码 -->
      <div class="sponsor-pay">
        <div class="pay-card">
          <div class="pay-head">
            <svg class="pay-icon pay-icon-alipay" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
              <rect x="1.5" y="1.5" width="21" height="21" rx="5" fill="currentColor"/>
              <text x="12" y="17" text-anchor="middle" font-size="13" font-weight="700" fill="#fff">支</text>
            </svg>
            <span class="pay-name">支付宝</span>
          </div>
          <div class="pay-tip">使用 支付宝 扫码赞助</div>
          <div class="pay-qr">
            <img v-if="webInfo.alipay" :src="webInfo.alipay" alt="支付宝收款码">
            <div v-else class="pay-empty">未配置支付宝收款码</div>
          </div>
        </div>

        <div class="pay-card">
          <div class="pay-head">
            <i class="fa fa-weixin pay-icon pay-icon-weixin" aria-hidden="true"></i>
            <span class="pay-name">微信</span>
          </div>
          <div class="pay-tip">使用 微信 扫码赞助</div>
          <div class="pay-qr">
            <img v-if="webInfo.wechatReward" :src="webInfo.wechatReward" alt="微信收款码">
            <div v-else class="pay-empty">未配置微信收款码</div>
          </div>
        </div>
      </div>

      <!-- 赞助列表 -->
      <div class="sponsor-list">
        <div class="sponsor-list-head">
          <div class="sponsor-list-icon">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <circle cx="12" cy="8.2" r="3.4" fill="#fff"/>
              <path d="M5.2 19.4c0-3.4 3-6.1 6.8-6.1s6.8 2.7 6.8 6.1z" fill="#fff"/>
            </svg>
          </div>
          <div class="sponsor-list-title">赞助记录</div>
          <div class="sponsor-count">{{ admires.length }}</div>
        </div>
        <div v-if="!$common.isEmpty(admires)" class="sponsor-grid">
          <div v-for="(item, index) in admires" :key="index" class="sponsor-item">
            <el-avatar :size="36" :src="item.avatar">
              {{ item.name ? item.name.charAt(0) : "赏" }}
            </el-avatar>
            <div class="sponsor-info">
              <div class="sponsor-line">
                <span class="sponsor-name">{{ item.name }}</span>
                <span class="sponsor-amount"><span class="sponsor-currency">￥</span>{{ item.amount }}</span>
              </div>
              <div v-if="item.remark" class="sponsor-remark" :title="item.remark">{{ item.remark }}</div>
              <div v-if="item.createTime" class="sponsor-date">{{ item.createTime }}</div>
            </div>
          </div>
        </div>
        <div v-else class="sponsor-empty">还没有赞助记录，期待你的支持～</div>
        </div>
      </div>
    </div>

      <!-- 右栏：与归档页同一套，日历卡按参考站放这里 -->
      <aside class="sponsor-side">
        <ArchiveSidebar></ArchiveSidebar>
      </aside>
    </div>

    <!-- 页脚 -->
    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import ArchiveSidebar from "../components/common/ArchiveSidebar.vue";
  import TwoPoem from "../components/common/TwoPoem.vue";

  export default {
    components: {
      MyFooter,
      MyAside,
      ArchiveSidebar,
      TwoPoem
    },
    data() {
      return {
        admires: []
      }
    },
    computed: {
      webInfo() {
        return this.$store.state.webInfo;
      }
    },
    created() {
      this.getAdmire();
    },
    methods: {
      selectSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: "/"});
        }
      },
      getAdmire() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getAdmire")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.admires = res.data;
            }
          })
          .catch(() => {
            this.admires = [];
          });
      }
    }
  }
</script>

<style scoped>
  .sponsor-container {
    display: grid;
    grid-template-columns: var(--ui-aside-width) minmax(0, 1fr);
    column-gap: var(--ui-page-gap);
    align-items: start;
    width: var(--ui-page-inline);
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    background: var(--ui-page-bg);
  }

  .sponsor-main {
    grid-column: 2;
    min-width: 0;
  }

  /* 右栏默认隐藏，与归档页同一断点：≥1280 才出现第三列 */
  .sponsor-side {
    display: none;
    min-width: 0;
  }

  @media screen and (min-width: 1280px) {
    .sponsor-container {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
      column-gap: var(--ui-page-gap);
    }

    .sponsor-side {
      display: block;
    }
  }

  .sponsor-header {
    /* 右侧列的第一个子元素，顶边与左侧卡片自动齐平 */
    padding: 0 0 22px;
    animation: hideToShow 1s ease-in-out;
  }

  .sponsor-title-row {
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .sponsor-subtitle {
    margin-top: 14px;
    font-size: var(--ui-subtitle-size);
    color: var(--ui-subtitle-color);
    letter-spacing: 1px;
  }

  .sponsor-tip {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-top: 14px;
    padding: 14px 18px;
    background: var(--subtleBg);
    border: 1px solid var(--cardBorder);
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.7;
    color: var(--fontColor);
  }

  .sponsor-tip-icon {
    flex-shrink: 0;
    margin-right: 10px;
  }

  .sponsor-title-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: var(--ui-accent-strong);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;
  }

  .sponsor-aside {
    grid-column: 1;
  }

  .sponsor-page {
    min-width: 0;
  }

  .sponsor-title {
    font-size: var(--ui-title-size);
    font-weight: var(--ui-title-weight);
    color: var(--ui-title-color);
    line-height: 1.2;
  }

  .sponsor-pay {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px;
  }

  .pay-card {
    background: var(--ui-card-bg);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    padding: 34px 24px 40px;
    text-align: center;
    box-shadow: var(--ui-card-shadow);
    transition: all 0.3s;
  }

  .pay-card:hover {
    box-shadow: var(--ui-card-shadow-hover);
    transform: translateY(-3px);
  }

  .pay-head {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 14px;
  }

  .pay-icon {
    color: var(--fontColor);
    margin-right: 10px;
    flex-shrink: 0;
    font-size: 26px;
  }

  /* 黑夜模式支付宝图标换用品牌蓝，浅色模式与微信图标保持原样 */
  html.dark .pay-icon-alipay {
    color: #1677ff;
  }

  /* 黑夜模式微信图标换用品牌绿 */
  html.dark .pay-icon-weixin {
    color: #07c160;
  }

  .pay-name {
    font-size: 20px;
    font-weight: 700;
    color: var(--fontColor);
  }

  .pay-tip {
    font-size: 15px;
    color: var(--maxGreyFont);
    margin-bottom: 26px;
  }

  .pay-qr {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .pay-qr img {
    width: 230px;
    height: 230px;
    object-fit: contain;
    background: var(--cardBg);
  }

  .pay-empty {
    width: 230px;
    height: 230px;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    border: 1px dashed rgba(128, 128, 128, 0.4);
    font-size: 13px;
    color: var(--maxGreyFont);
    padding: 0 16px;
  }

  .sponsor-list {
    margin-top: 30px;
    padding: var(--ui-card-padding-y) var(--ui-card-padding-x);
    background: var(--ui-card-bg);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    box-shadow: var(--ui-card-shadow);
  }

  .sponsor-list-head {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-bottom: 18px;
  }

  .sponsor-list-icon {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    background: var(--ui-accent-strong);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;
  }

  .sponsor-list-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--fontColor);
  }

  .sponsor-count {
    margin-left: auto;
    min-width: 26px;
    height: 26px;
    padding: 0 8px;
    border-radius: 13px;
    background: var(--subtleBg);
    color: var(--maxGreyFont);
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .sponsor-grid {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 14px;
  }

  .sponsor-item {
    display: flex;
    flex-direction: row;
    align-items: center;
    background: var(--cardBg);
    border: 1px solid rgba(128, 128, 128, 0.18);
    border-radius: 10px;
    padding: 11px 18px;
    transition: all 0.3s;
  }

  .sponsor-item:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  }

  .sponsor-info {
    display: flex;
    flex-direction: column;
    margin-left: 10px;
    min-width: 0;
  }

  .sponsor-line {
    display: flex;
    flex-direction: row;
    align-items: baseline;
  }

  .sponsor-name {
    font-size: 14px;
    font-weight: 700;
    color: var(--fontColor);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 140px;
  }

  /* 金额标签：高度与圆角统一由 --amount-h 推导；相对定位作为币符的挂载点 */
  .sponsor-amount {
    --amount-h: 22px;
    position: relative;
    display: inline-flex;
    align-items: center;
    flex: none;
    box-sizing: border-box;
    height: var(--amount-h);
    /* 圈直径 = 胶囊高度 22px（贴 border-box 左侧），圆右缘在内缘 21px 处，
       再留 5px 给数字，故左内边距 26px */
    padding: 0 8px 0 26px;
    /* 圈与胶囊左端齐平、不探出，外边距保持与用户名的常规间距 */
    margin-left: 6px;
    border: 1px solid var(--cardBorder);
    border-radius: calc(var(--amount-h) / 2);
    font-size: 13px;
    font-weight: 700;
    line-height: 1;
    color: var(--fontColor);
    /* 数字整体不拆行 */
    white-space: nowrap;
  }

  /* 币符：直径直接取胶囊高度变量（不写死，胶囊变高圈跟着变），左上角对齐胶囊 border-box；
     高 22px + 圆角 50% → 外半径 11px，与胶囊左端圆弧同心同半径，
     1px 描边又取胶囊边框同色，左半圈与胶囊左边框完全重合，不会出现双层线或粗细不匀；
     右半圈落在胶囊内部，形成「圆镶在胶囊左端」的观感 */
  .sponsor-currency {
    position: absolute;
    top: -1px;
    left: -1px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    width: var(--amount-h);
    height: var(--amount-h);
    border: 1px solid var(--cardBorder);
    border-radius: 50%;
    font-size: 0.9em;
    line-height: 1;
  }

  .sponsor-date {
    margin-top: 2px;
    font-size: 12px;
    color: var(--maxGreyFont);
  }

  .sponsor-remark {
    margin-top: 2px;
    font-size: 12px;
    color: var(--greyFont);
    max-width: 220px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sponsor-empty {
    padding: 34px 0;
    text-align: center;
    font-size: 14px;
    color: var(--maxGreyFont);
    border: 1px dashed rgba(128, 128, 128, 0.3);
    border-radius: 12px;
  }

  @media (max-width: 1000px) {
    .sponsor-container {
      grid-template-columns: minmax(0, 1fr);
      width: var(--ui-page-inline);
      padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    }

    /* 单列时拆掉右列容器，让标题、侧栏、正文仍按原顺序排列 */
    .sponsor-main {
      display: contents;
    }

    .sponsor-header {
      grid-column: 1;
      grid-row: 1;
    }

    .sponsor-aside {
      grid-column: 1;
      grid-row: 2;
      margin-bottom: 30px;
    }

    .sponsor-page {
      grid-column: 1;
      grid-row: 3;
    }
  }

  @media (max-width: 768px) {
    .sponsor-pay {
      grid-template-columns: minmax(0, 1fr);
    }

    .sponsor-list,
    .pay-card {
      padding: 18px 16px;
    }
  }
</style>
