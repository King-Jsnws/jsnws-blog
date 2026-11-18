<template>
  <div class="maintenance-page">
    <div class="maintenance-card">
      <p class="maintenance-badge">MAINTENANCE</p>
      <h1 class="maintenance-title">{{ webName }}</h1>
      <p class="maintenance-text">站点正在维护中，请稍后再来。</p>
      <p class="maintenance-tip">
        站长可从 <router-link class="maintenance-link" to="/verify">登录入口</router-link> 进入后台恢复站点。
      </p>
    </div>
  </div>
</template>

<script>
  export default {
    name: "MaintenanceView",
    computed: {
      webName() {
        return this.$store.state.webInfo.webName || "站点维护中"
      }
    }
  }
</script>

<style scoped>
  .maintenance-page {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
    box-sizing: border-box;
    background: linear-gradient(135deg, #f7f7fb 0%, #eef1f7 100%);
    color: #303133;
  }

  .maintenance-card {
    width: 100%;
    max-width: 460px;
    padding: 40px 32px;
    border-radius: 16px;
    background: #ffffff;
    text-align: center;
    box-shadow: 0 18px 46px rgba(31, 45, 61, 0.12);
  }

  .maintenance-badge {
    margin: 0 0 14px;
    font-size: 12px;
    letter-spacing: 3px;
    color: #a0a6b4;
  }

  .maintenance-title {
    margin: 0 0 12px;
    font-size: 24px;
    font-weight: 600;
    word-break: break-all;
  }

  .maintenance-text {
    margin: 0;
    font-size: 15px;
    color: #606266;
  }

  .maintenance-tip {
    margin: 22px 0 0;
    font-size: 13px;
    color: #909399;
  }

  .maintenance-link {
    color: #5b7cfa;
    text-decoration: none;
  }

  .maintenance-link:hover {
    text-decoration: underline;
  }
</style>
