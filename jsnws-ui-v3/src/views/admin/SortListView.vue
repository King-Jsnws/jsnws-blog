<template>
  <div>
    <div class="admin-card">
      <div class="admin-page-head">
        <h2>分类管理</h2>
        <p>维护文章分类与标签，优先级数字越小越靠前。</p>
      </div>
      <div class="handle-box">
        <el-button type="primary" @click="sortDialog = true">新增分类</el-button>
      </div>
      <el-table :data="sortInfo" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column prop="sortName" label="分类名称" align="center"></el-table-column>
      <el-table-column prop="sortDescription" label="分类描述" align="center"></el-table-column>
      <el-table-column label="分类类型" align="center">
        <template #default="scope">
          <span v-if="scope.row.sortType === 0">导航栏分类</span>
          <span v-else-if="scope.row.sortType === 1">普通分类</span>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="分类优先级" align="center"></el-table-column>
      <el-table-column prop="countOfSort" label="文章总数" align="center"></el-table-column>
      <el-table-column label="操作" width="380" align="center">
        <template #default="scope">
          <el-button type="text" @click="editSort(scope.row)">
            <el-icon><Edit /></el-icon>
            编辑分类
          </el-button>
          <el-button type="text" @click="sayLabel(scope.row)">
            <el-icon><Edit /></el-icon>
            查看标签
          </el-button>
          <el-button type="text" @click="insertLabel(scope.row)">
            <el-icon><Edit /></el-icon>
            新增标签
          </el-button>
          <el-button type="text" style="color: var(--orangeRed)"
                     @click="deleteHandle(scope.row.id, 1)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <div v-if="!$common.isEmpty(sort)" class="admin-card">
      <div class="admin-page-head">
        <h2>标签列表 · {{ sort.sortName }}</h2>
      </div>
      <el-table :data="sort.labels" border class="table"
              header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column label="分类名称" align="center">
        <span>{{sort.sortName}}</span>
      </el-table-column>
      <el-table-column prop="labelName" label="标签名称" align="center"></el-table-column>
      <el-table-column prop="labelDescription" label="标签描述" align="center"></el-table-column>
      <el-table-column prop="countOfLabel" label="文章总数" align="center"></el-table-column>
      <el-table-column label="操作" width="320" align="center">
        <template #default="scope">
          <el-button type="text" @click="editLabel(scope.row)">
            <el-icon><Edit /></el-icon>
            编辑标签
          </el-button>
          <el-button type="text" style="color: var(--orangeRed)"
                     @click="deleteHandle(scope.row.id, 2)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog class="admin-dialog"
               title="分类"
               v-model="sortDialog"
               width="30%"
               :before-close="handleClose"
               :append-to-body="true"
               destroy-on-close
               center>
      <div class="my-dialog">
        <div class="myCenter">
          <el-radio-group v-model="sortForHttp.sortType">
            <el-radio-button :label="0">导航栏分类</el-radio-button>
            <el-radio-button :label="1">普通分类</el-radio-button>
          </el-radio-group>
        </div>
        <el-input placeholder="请输入分类名称" v-model="sortForHttp.sortName">
          <template #prepend>分类名称</template>
        </el-input>
        <el-input placeholder="请输入分类描述" v-model="sortForHttp.sortDescription">
          <template #prepend>分类描述</template>
        </el-input>
        <el-input type="number" placeholder="请输入整数，数字小的在前面"
                  v-model="sortForHttp.priority">
          <template #prepend>分类优先级</template>
        </el-input>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveSortEdit()">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog class="admin-dialog"
               title="标签"
               v-model="labelDialog"
               width="30%"
               :before-close="handleClose"
               :append-to-body="true"
               destroy-on-close
               center>
      <div class="my-dialog">
        <el-input placeholder="请输入标签名称" v-model="labelForHttp.labelName">
          <template #prepend>标签名称</template>
        </el-input>
        <el-input placeholder="请输入标签描述" v-model="labelForHttp.labelDescription">
          <template #prepend>标签描述</template>
        </el-input>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveLabelEdit()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        sortDialog: false,
        labelDialog: false,
        sortInfo: [],
        sort: {},
        sortForHttp: {
          id: null,
          sortName: "",
          sortDescription: "",
          sortType: null,
          priority: null
        },
        labelForHttp: {
          id: null,
          sortId: null,
          labelName: "",
          labelDescription: ""
        }
      }
    },

    computed: {},

    watch: {},

    created() {
      this.getSortInfo();
    },

    mounted() {

    },

    methods: {
      deleteHandle(id, flag) {
        let url;
        if (flag === 1) {
          url = "/webInfo/deleteSort";
        } else if (flag === 2) {
          url = "/webInfo/deleteLabel";
        } else {
          return;
        }
        this.$confirm('确认删除？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + url, {id: id}, true)
            .then((res) => {
              this.$message({
                message: "删除成功！",
                type: "success"
              });
              this.getSortInfo();
              this.sort = {};
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
      saveSortEdit() {
        if (this.$common.isEmpty(this.sortForHttp.sortType) ||
          this.$common.isEmpty(this.sortForHttp.priority) ||
          this.$common.isEmpty(this.sortForHttp.sortName) ||
          this.$common.isEmpty(this.sortForHttp.sortDescription)) {
          this.$message({
            message: "请完善所有分类信息！",
            type: "error"
          });
          return;
        }

        let url;
        if (this.$common.isEmpty(this.sortForHttp.id)) {
          url = "/webInfo/saveSort";
        } else {
          url = "/webInfo/updateSort";
        }
        this.$http.post(this.$constant.baseURL + url, this.sortForHttp, true)
          .then((res) => {
            this.$message({
              message: "保存成功！",
              type: "success"
            });
            this.getSortInfo();
            this.handleClose();
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      saveLabelEdit() {
        if (this.$common.isEmpty(this.labelForHttp.labelName) ||
          this.$common.isEmpty(this.labelForHttp.labelDescription)) {
          this.$message({
            message: "请完善所有标签信息！",
            type: "error"
          });
          return;
        }

        let url;
        if (this.$common.isEmpty(this.labelForHttp.id)) {
          url = "/webInfo/saveLabel";
        } else {
          url = "/webInfo/updateLabel";
        }
        this.$http.post(this.$constant.baseURL + url, this.labelForHttp, true)
          .then((res) => {
            this.$message({
              message: "保存成功！",
              type: "success"
            });
            this.getSortInfo();
            this.handleClose();
            this.sort = {};
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      editSort(sort) {
        this.sortDialog = true;
        this.sortForHttp.id = sort.id;
        this.sortForHttp.sortName = sort.sortName;
        this.sortForHttp.sortDescription = sort.sortDescription;
        this.sortForHttp.sortType = sort.sortType;
        this.sortForHttp.priority = sort.priority;
      },
      editLabel(label) {
        this.labelDialog = true;
        this.labelForHttp.id = label.id;
        this.labelForHttp.sortId = label.sortId;
        this.labelForHttp.labelName = label.labelName;
        this.labelForHttp.labelDescription = label.labelDescription;
      },
      insertLabel(sort) {
        this.labelForHttp.sortId = sort.id;
        this.labelDialog = true;
      },
      handleClose() {
        this.labelForHttp = {
          id: null,
          sortId: null,
          labelName: "",
          labelDescription: ""
        };
        this.sortForHttp = {
          id: null,
          sortName: "",
          sortDescription: "",
          sortType: null,
          priority: null
        };
        this.sortDialog = false;
        this.labelDialog = false;
      },
      sayLabel(sort) {
        this.sort = sort;
      },
      getSortInfo() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getSortInfo")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.sortInfo = res.data;
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

  .my-dialog > div {
    margin: 12px;
  }

  .my-dialog :deep(input::-webkit-inner-spin-button) {
    appearance: none;
  }
</style>
