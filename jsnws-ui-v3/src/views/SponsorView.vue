<template>
  <div>
    <div class="sponsor-container">
      <!-- 左侧栏（与主页一致），与右侧内容同处一行、顶边齐平 -->
      <div class="sponsor-aside">
        <MyAside @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
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
        <div class="sponsor-page my-animation-slide-bottom">

      <!-- 收款码 -->
      <div class="sponsor-pay">
        <div class="pay-card">
          <div class="pay-head">
            <svg class="pay-icon" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
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
            <svg class="pay-icon" viewBox="0 0 24 24" width="26" height="26" aria-hidden="true">
              <path fill="currentColor"
                    d="M9.2 3C5.1 3 1.8 5.7 1.8 9c0 1.9 1.1 3.6 2.8 4.7l-.7 2.1 2.4-1.2c.9.2 1.9.4 2.9.4h.5a5.6 5.6 0 0 1-.2-1.5c0-3.2 3.1-5.8 6.9-5.8h.4C16.1 4.8 12.9 3 9.2 3z"/>
              <path fill="currentColor"
                    d="M22.2 13.4c0-2.7-2.7-4.9-6-4.9s-6 2.2-6 4.9 2.7 4.9 6 4.9c.7 0 1.4-.1 2.1-.3l2.2 1.1-.6-1.8c1.4-.9 2.3-2.3 2.3-3.9z"/>
            </svg>
            <span class="pay-name">微信</span>
          </div>
          <div class="pay-tip">使用 微信 扫码赞助</div>
          <div class="pay-qr">
            <img v-if="webInfo.wechat" :src="webInfo.wechat" alt="微信收款码">
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
                <span class="sponsor-amount">￥{{ item.amount }}</span>
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
    </div>

    <!-- 页脚 -->
    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";

  export default {
    components: {
      MyFooter,
      MyAside
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
        this.$router.push({path: "/sort", query: {sortId: sort.id}});
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
    grid-template-columns: 300px minmax(0, 1fr);
    column-gap: 30px;
    align-items: start;
    width: 90%;
    max-width: 1400px;
    margin: 0 auto;
    padding: 70px 20px 50px;
  }

  .sponsor-main {
    grid-column: 2;
    min-width: 0;
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
    font-size: 15px;
    color: var(--maxGreyFont);
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
    border-radius: 10px;
    background: #1f1f1f;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;
  }

  /* 黑夜模式下黑色图标底色与卡片过近，换成低透明度白底保留方块观感 */
  html.dark .sponsor-title-icon,
  html.dark .sponsor-list-icon {
    background: rgba(255, 255, 255, 0.1);
  }

  .sponsor-aside {
    grid-column: 1;
  }

  .sponsor-page {
    min-width: 0;
    animation: hideToShow 1s ease-in-out;
  }

  .sponsor-title {
    font-size: 22px;
    font-weight: 700;
    color: var(--fontColor);
    line-height: 1.2;
  }

  .sponsor-pay {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px;
  }

  .pay-card {
    background: var(--cardBg);
    border: 1px solid rgba(128, 128, 128, 0.16);
    border-radius: 18px;
    padding: 34px 24px 40px;
    text-align: center;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.04);
    transition: all 0.3s;
  }

  .pay-card:hover {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
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
    padding: 22px 24px 26px;
    background: var(--cardBg);
    border: 1px solid rgba(128, 128, 128, 0.16);
    border-radius: 18px;
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
    border-radius: 10px;
    background: #1f1f1f;
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

  .sponsor-amount {
    font-size: 14px;
    font-weight: 700;
    color: var(--fontColor);
    margin-left: 6px;
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
      width: 100%;
      padding: 40px 16px 40px;
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
  }
</style>
