<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>留言列表</h2>
      <p>查看站点收到的留言，可删除不当内容。</p>
    </div>
    <el-table :data="treeHoles" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column prop="message" label="留言内容" align="center"></el-table-column>
      <el-table-column prop="createTime" label="创建时间" align="center"></el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button type="text" style="color: var(--orangeRed)" @click="handleDelete(scope.row)">
            <el-icon><Delete /></el-icon>
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
        pagination: {
          current: 1,
          size: 10,
          total: 0
        },
        treeHoles: []
      }
    },

    computed: {},

    watch: {},

    created() {
      this.getTreeHoles()
    },

    mounted() {
    },

    methods: {
      getTreeHoles() {
        this.$http.post(this.$constant.baseURL + "/admin/treeHole/boss/list", this.pagination, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.treeHoles = res.data.records;
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
        this.getTreeHoles();
      },
      handleDelete(item) {
        this.$confirm('确认删除？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/deleteTreeHole", {id: item.id}, true)
            .then((res) => {
              this.pagination.current = 1;
              this.getTreeHoles();
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
</style>
