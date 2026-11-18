<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>弹幕管理</h2>
      <p>管理首页顶部滚动的弹幕（树洞）内容，可删除不当弹幕。</p>
    </div>
    <div class="handle-box">
      <el-input class="my-input" style="width: 240px;margin-right: 10px" v-model="pagination.searchKey"
                placeholder="搜索弹幕内容" @keyup.enter="searchBarrage()"></el-input>
      <el-button type="primary" icon="Search" @click="searchBarrage()">搜索</el-button>
      <el-button type="danger" @click="clearSearch()">清除参数</el-button>
    </div>
    <el-table :data="barrageList" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="90" align="center"></el-table-column>
      <el-table-column label="头像" width="70" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.avatar" lazy class="table-td-thumb" :src="scope.row.avatar"
                    :preview-src-list="[scope.row.avatar]" fit="cover"></el-image>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="弹幕内容" min-width="240" align="center">
        <template #default="scope">
          <span class="content-cell" :title="scope.row.message">{{ scope.row.message }}</span>
        </template>
      </el-table-column>
      <el-table-column label="IP地址" width="140" align="center">
        <template #default="scope">
          <span>{{ scope.row.ipAddress || '—' }}</span>
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
        pagination: {
          current: 1,
          size: 10,
          total: 0,
          searchKey: ""
        },
        barrageList: []
      }
    },

    created() {
      this.getBarrage()
    },

    methods: {
      getBarrage() {
        this.$http.post(this.$constant.baseURL + "/admin/treeHole/boss/list", this.pagination, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.barrageList = res.data.records;
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
      clearSearch() {
        this.pagination = {
          current: 1,
          size: 10,
          total: 0,
          searchKey: ""
        }
        this.getBarrage();
      },
      searchBarrage() {
        this.pagination.current = 1;
        this.getBarrage();
      },
      handlePageChange(val) {
        this.pagination.current = val;
        this.getBarrage();
      },
      handleDelete(item) {
        this.$confirm('确认删除该弹幕？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/deleteTreeHole", {id: item.id}, true)
            .then(() => {
              this.pagination.current = 1;
              this.getBarrage();
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

  .table-td-thumb {
    display: block;
    margin: auto;
    width: 36px;
    height: 36px;
    border-radius: 4px;
  }
</style>
