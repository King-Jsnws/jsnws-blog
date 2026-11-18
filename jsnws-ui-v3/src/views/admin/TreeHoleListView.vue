<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>留言列表</h2>
      <p>查看站点收到的留言，可删除不当内容。</p>
    </div>
    <el-table :data="messages" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column label="留言用户" width="140" align="center">
        <template #default="scope">
          <span v-if="scope.row.isAnonymous === 1">匿名网友</span>
          <span v-else>{{ scope.row.nickname || scope.row.username }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="commentContent" label="留言内容" min-width="200" align="center">
        <template #default="scope">
          <span class="content-cell" :title="scope.row.commentContent">{{ scope.row.commentContent }}</span>
        </template>
      </el-table-column>
      <el-table-column label="IP地址" width="140" align="center">
        <template #default="scope">
          <span>{{ scope.row.ipAddress || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="70" align="center">
        <template #default="scope">
          <span v-if="scope.row.isTop === 1" class="top-tag">置顶</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" align="center"></el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button type="text" style="color: var(--orangeRed)" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="total, prev, pager, next"
                     :current-page="pagination.current"
                     :page-size="pagination.size"
                     :total="pagination.total"
                     @current-change="handlePageChange">
      </el-pagination>
    </div>
  </div>
</template>

<script>

  export default {
    data() {
      return {
        // AI 站长沿用站长的全量视图
        isBoss: this.$store.state.currentAdmin.isBoss ||
          Number(this.$store.state.currentAdmin.userType) === 5,
        pagination: {
          current: 1,
          size: 10,
          total: 0,
          commentType: "message"
        },
        messages: []
      }
    },

    created() {
      this.getMessages()
    },

    methods: {
      getMessages() {
        let url = this.isBoss ? "/admin/comment/boss/list" : "/admin/comment/user/list";
        this.$http.post(this.$constant.baseURL + url, this.pagination, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.messages = res.data.records;
              this.pagination.total = res.data.total;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      handlePageChange(val) {
        this.pagination.current = val;
        this.getMessages();
      },
      handleDelete(item) {
        let url = this.isBoss ? "/admin/comment/boss/deleteComment" : "/admin/comment/user/deleteComment";
        this.$confirm('删除留言后，其所有回复将一并删除。确认删除？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + url, {id: item.id}, true)
            .then(() => {
              this.pagination.current = 1;
              this.getMessages();
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
      }
    }
  }
</script>

<style scoped>
  .pagination {
    margin: 18px 0 0;
    text-align: right;
  }

  .content-cell {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: break-all;
  }

  .top-tag {
    padding: 1px 7px;
    border-radius: 4px;
    background: #ef4444;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
  }
</style>
