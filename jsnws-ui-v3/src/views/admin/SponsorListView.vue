<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>赞助记录</h2>
      <p>登记收到的赞助，前台赞助页按时间倒序展示。</p>
    </div>

    <div class="handle-box">
      <el-input v-model="searchKey" placeholder="赞助人/备注" class="handle-input mrb10" clearable
                @keyup.enter="searchRecords()"></el-input>
      <el-button type="primary" icon="Search" @click="searchRecords()">搜索</el-button>
      <el-button type="primary" @click="openAdd()">新增记录</el-button>
    </div>

    <el-table :data="recordList" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column label="赞助人" min-width="150" align="center">
        <template #default="scope">
          <div class="sponsor-user">
            <el-avatar :size="30" :src="scope.row.avatar">
              {{ scope.row.name ? scope.row.name.charAt(0) : "赏" }}
            </el-avatar>
            <span>{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="110" align="center">
        <template #default="scope">
          <span class="amount-text">￥{{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="220" align="center">
        <template #default="scope">
          <span v-if="scope.row.remark" class="remark-text" :title="scope.row.remark">
            {{ scope.row.remark }}
          </span>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="赞助时间" width="165" align="center">
        <template #default="scope">
          <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="text" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" icon="Delete" style="color: var(--orangeRed)"
                     @click="removeRecord(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination background
                     layout="total, prev, pager, next"
                     :current-page="pagination.current"
                     :page-size="pagination.size"
                     :total="pagination.total"
                     @current-change="pageRecords"></el-pagination>
    </div>

    <el-dialog class="admin-dialog"
               :title="form.id ? '编辑赞助记录' : '新增赞助记录'"
               v-model="dialogVisible"
               width="560px"
               :before-close="handleClose"
               destroy-on-close>
      <el-form :model="form" label-position="top" class="sponsor-form">
        <div class="form-grid">
          <el-form-item label="赞助人">
            <el-input maxlength="50" v-model="form.name" placeholder="昵称或称呼"></el-input>
          </el-form-item>
          <el-form-item label="金额（元）">
            <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1"
                             controls-position="right" style="width: 100%"></el-input-number>
          </el-form-item>
        </div>
        <el-form-item label="头像链接">
          <div class="avatar-row">
            <el-input maxlength="500" v-model="form.avatar" placeholder="头像图片地址，可留空"></el-input>
            <el-avatar :size="38" :src="form.avatar">
              {{ form.name ? form.name.charAt(0) : "赏" }}
            </el-avatar>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input maxlength="255" v-model="form.remark" type="textarea" :rows="2"
                    placeholder="例如：赞助服务器费用 / 留言，可留空"></el-input>
        </el-form-item>
        <el-form-item label="赞助时间">
          <el-date-picker v-model="form.createTime" type="datetime" placeholder="选择赞助时间"
                          value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"></el-date-picker>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveRecord()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "SponsorListView",
    data() {
      return {
        recordList: [],
        searchKey: "",
        pagination: {
          current: 1,
          size: 10,
          total: 0
        },
        dialogVisible: false,
        form: this.emptyForm()
      }
    },
    created() {
      this.getRecords();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          name: "",
          avatar: "",
          amount: 0,
          remark: "",
          createTime: this.now()
        }
      },
      now() {
        let date = new Date();
        const pad = (num) => (num < 10 ? "0" + num : num);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate()) + " " +
          pad(date.getHours()) + ":" + pad(date.getMinutes()) + ":" + pad(date.getSeconds());
      },
      formatTime(time) {
        if (!time) {
          return "—";
        }
        let date = new Date(String(time).replace(/-/g, "/"));
        if (isNaN(date.getTime())) {
          return time;
        }
        const pad = (num) => (num < 10 ? "0" + num : num);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate()) + " " +
          pad(date.getHours()) + ":" + pad(date.getMinutes());
      },
      getRecords() {
        let params = {
          current: this.pagination.current,
          size: this.pagination.size,
          searchKey: this.searchKey
        };
        this.$http.post(this.$constant.baseURL + "/admin/sponsor/list", params, true)
          .then((res) => {
            if (this.$common.isEmpty(res.data)) {
              this.recordList = [];
              this.pagination.total = 0;
              return;
            }
            this.recordList = res.data.records || [];
            this.pagination.total = res.data.total || 0;
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      searchRecords() {
        this.pagination.current = 1;
        this.getRecords();
      },
      pageRecords(current) {
        this.pagination.current = current;
        this.getRecords();
      },
      openAdd() {
        this.form = this.emptyForm();
        this.dialogVisible = true;
      },
      openEdit(record) {
        this.form = Object.assign(this.emptyForm(), record);
        this.dialogVisible = true;
      },
      saveRecord() {
        if (this.$common.isEmpty(this.form.name)) {
          this.$message({
            message: "请填写赞助人！",
            type: "warning"
          });
          return;
        }
        if (this.form.amount === null || this.form.amount === undefined || this.form.amount < 0) {
          this.$message({
            message: "请填写正确的赞助金额！",
            type: "warning"
          });
          return;
        }
        let data = Object.assign({}, this.form);
        let url = data.id ? "/admin/sponsor/update" : "/admin/sponsor/save";
        this.$http.post(this.$constant.baseURL + url, data, true)
          .then(() => {
            this.handleClose();
            this.getRecords();
            this.$message({
              message: "保存成功！",
              type: "success"
            });
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      removeRecord(record) {
        this.$confirm('确认删除「' + record.name + '」的赞助记录？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/admin/sponsor/delete", {id: record.id}, true)
            .then(() => {
              this.getRecords();
              this.$message({
                message: "删除成功！",
                type: "success"
              });
            })
            .catch((error) => {
              this.$message({
                message: error.message,
                type: "error"
              });
            });
        }).catch(() => {
          this.$message({
            type: 'success',
            message: '已取消删除!'
          });
        });
      },
      handleClose() {
        this.dialogVisible = false;
        this.form = this.emptyForm();
      }
    }
  }
</script>

<style scoped>
  .sponsor-user {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .amount-text {
    font-weight: 600;
    color: var(--orangeRed);
  }

  .remark-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .empty-text,
  .time-text {
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 18px;
  }

  .avatar-row {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
  }

  .sponsor-form :deep(.el-form-item__label) {
    padding-bottom: 6px;
    font-size: 13px;
    font-weight: 500;
    color: var(--admin-text-soft);
  }

  .handle-input {
    width: 160px;
    display: inline-block;
  }

  .mrb10 {
    margin-right: 10px;
    margin-bottom: 10px;
  }
</style>
