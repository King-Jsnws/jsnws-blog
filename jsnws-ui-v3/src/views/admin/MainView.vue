<template>
  <div class="stats-page">
    <div class="stats-head">
      <div>
        <h1 class="stats-title">访问统计</h1>
        <p class="stats-sub">站点访问数据概览，按 IP 每日去重统计。</p>
      </div>
      <div class="stats-total">
        <span>总访问量</span>
        <strong>{{ display(historyInfo.ip_history_count) }}</strong>
      </div>
    </div>

    <div class="stats-kpis">
      <div class="stats-kpi">
        <p class="kpi-label">今日访问</p>
        <p class="kpi-value">{{ display(historyInfo.ip_count_today) }}</p>
      </div>
      <div class="stats-kpi">
        <p class="kpi-label">昨日访问</p>
        <p class="kpi-value">{{ display(historyInfo.ip_count_yest) }}</p>
      </div>
      <div class="stats-kpi">
        <p class="kpi-label">今日访问省份</p>
        <p class="kpi-value">{{ (historyInfo.province_today || []).length }}</p>
      </div>
      <div class="stats-kpi">
        <p class="kpi-label">今日访问用户</p>
        <p class="kpi-value">{{ (historyInfo.username_today || []).length }}</p>
      </div>
    </div>

    <div class="stats-grid">
      <section class="stats-card">
        <h2 class="stats-card-title">省份访问 TOP10</h2>
        <el-table :data="historyInfo.ip_history_province || []" empty-text="暂无数据">
          <el-table-column type="index" align="center" width="60"></el-table-column>
          <el-table-column prop="province" align="center" label="省份"></el-table-column>
          <el-table-column prop="num" align="center" label="数量" width="110"></el-table-column>
        </el-table>
      </section>

      <section class="stats-card">
        <h2 class="stats-card-title">IP 访问 TOP10</h2>
        <el-table :data="historyInfo.ip_history_ip || []" empty-text="暂无数据">
          <el-table-column type="index" align="center" width="60"></el-table-column>
          <el-table-column prop="ip" align="center" label="IP"></el-table-column>
          <el-table-column prop="num" align="center" label="数量" width="110"></el-table-column>
        </el-table>
      </section>

      <section class="stats-card">
        <h2 class="stats-card-title">今日访问省份</h2>
        <el-table :data="historyInfo.province_today || []" empty-text="暂无数据">
          <el-table-column type="index" align="center" width="60"></el-table-column>
          <el-table-column prop="province" align="center" label="省份"></el-table-column>
          <el-table-column prop="num" align="center" label="数量" width="110"></el-table-column>
        </el-table>
      </section>

      <section class="stats-card">
        <h2 class="stats-card-title">今日访问用户</h2>
        <el-table :data="historyInfo.username_today || []" empty-text="暂无数据">
          <el-table-column align="center" label="头像" width="80">
            <template #default="scope">
              <el-avatar class="stats-avatar" :size="30" :src="scope.row.avatar">
                {{ (scope.row.username || 'U').slice(0, 1).toUpperCase() }}
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="username" align="center" label="用户"></el-table-column>
        </el-table>
      </section>

      <section class="stats-card">
        <h2 class="stats-card-title">昨日访问用户</h2>
        <el-table :data="historyInfo.username_yest || []" empty-text="暂无数据">
          <el-table-column align="center" label="头像" width="80">
            <template #default="scope">
              <el-avatar class="stats-avatar" :size="30" :src="scope.row.avatar">
                {{ (scope.row.username || 'U').slice(0, 1).toUpperCase() }}
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="username" align="center" label="用户"></el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        historyInfo: {}
      }
    },

    created() {
      this.getHistoryInfo();
    },

    methods: {
      display(value) {
        return value === null || value === undefined || value === "" ? "—" : value;
      },

      getHistoryInfo() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getHistoryInfo", {}, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.historyInfo = res.data;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      }
    }
  }
</script>

<style scoped>

  .stats-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .stats-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    padding: 22px 26px;
    border-radius: 14px;
    background: linear-gradient(120deg, #3a55e8 0%, #4c6fff 60%, #6b5cff 100%);
    color: #fff;
    box-shadow: 0 18px 40px -26px rgba(58, 85, 232, 0.8);
  }

  .stats-title {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
  }

  .stats-sub {
    margin: 8px 0 0;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.78);
  }

  .stats-total {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
    flex-shrink: 0;
  }

  .stats-total span {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.72);
  }

  .stats-total strong {
    font-size: 30px;
    font-weight: 650;
    line-height: 1;
    font-variant-numeric: tabular-nums;
  }

  .stats-kpis {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
  }

  .stats-kpi {
    padding: 18px 20px;
    border-radius: 14px;
    border: 1px solid var(--admin-border);
    background: var(--admin-card);
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
  }

  .kpi-label {
    margin: 0;
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .kpi-value {
    margin: 6px 0 0;
    font-size: 24px;
    font-weight: 650;
    color: var(--admin-text);
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
    align-items: start;
  }

  .stats-card {
    padding: 18px 20px 8px;
    border-radius: 14px;
    border: 1px solid var(--admin-border);
    background: var(--admin-card);
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
    min-width: 0;
  }

  .stats-card-title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: var(--admin-text);
  }

  .stats-card-title::before {
    content: "";
    display: inline-block;
    width: 3px;
    height: 13px;
    margin-right: 8px;
    border-radius: 2px;
    background: #4c6fff;
    vertical-align: -1px;
  }

  .stats-card :deep(.el-table) {
    box-shadow: none;
    border-radius: 0;
  }

  .stats-avatar {
    background: var(--el-color-primary-light-9);
    color: #4c6fff;
    font-size: 12px;
    font-weight: 600;
  }

  @media (max-width: 1100px) {
    .stats-kpis {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .stats-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }

  @media (max-width: 640px) {
    .stats-head {
      flex-direction: column;
      align-items: flex-start;
    }

    .stats-total {
      align-items: flex-start;
    }
  }

</style>
