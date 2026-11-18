<template>
  <div>
    <div class="admin-card">
      <div class="admin-page-head">
        <h2>资源列表</h2>
        <p>管理站点上传的图片、音视频等资源文件。</p>
      </div>
      <div class="handle-box">
        <el-select clearable v-model="pagination.resourceType" placeholder="资源类型" class="handle-select mrb10">
          <el-option v-for="item in resourceTypes" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
        <el-button type="primary" @click="search()">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button type="primary" @click="addResources()">新增资源</el-button>
      </div>
      <el-table :data="resources" border class="table" header-cell-class-name="table-header">
        <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
        <el-table-column prop="originalName" label="名称" align="center"></el-table-column>
        <el-table-column prop="userId" label="用户ID" align="center"></el-table-column>
        <el-table-column label="资源类型" align="center">
          <template #default="scope">
            {{ getTypeLabel(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === false ? 'danger' : 'success'"
                    disable-transitions>
              {{scope.row.status === false ? '禁用' : '启用'}}
            </el-tag>
            <el-switch @click="changeStatus(scope.row)" v-model="scope.row.status"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="路径" align="center">
          <template #default="scope">
            <template v-if="!$common.isEmpty(scope.row.mimeType) && scope.row.mimeType.includes('image')">
              <el-image lazy :preview-src-list="[scope.row.path]" class="table-td-thumb" :src="scope.row.path"
                        fit="cover"></el-image>
            </template>
            <template v-else>
              {{scope.row.path}}
            </template>
          </template>
        </el-table-column>

        <el-table-column label="大小(KB)" align="center">
          <template #default="scope">
            {{Math.round(scope.row.size / 1024)}}
          </template>
        </el-table-column>
        <el-table-column prop="mimeType" label="类型" align="center"></el-table-column>
        <el-table-column prop="storeType" label="存储平台" align="center"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" align="center"></el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="scope">
            <el-button type="text" @click="copyPath(scope.row.path)">
              <el-icon><Document /></el-icon>
              复制地址
            </el-button>
            <el-button type="text" style="color: var(--orangeRed)"
                       @click="handleDelete(scope.row)">
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

    <el-dialog class="admin-dialog"
               title="文件"
               v-model="resourceDialog"
               width="25%"
               :append-to-body="true"
               :close-on-click-modal="false"
               destroy-on-close
               center>
      <div>
        <div style="display: flex;margin-bottom: 10px">
          <div style="line-height: 40px">存储平台：</div>
          <el-select v-model="storeType" placeholder="存储平台" style="width: 120px">
            <el-option
              v-for="(item, i) in storeTypes"
              :key="i"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </div>
        <UploadPicture :isAdmin="true" :prefix="pagination.resourceType" @addPicture="addFile"
                       :storeType="storeType" :registerResource="true"
                       :listType="'text'" :accept="'image/*, video/*, audio/*'"
                       :maxSize="100" :maxNumber="10"></UploadPicture>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  // 旧项目使用异步组件加载，Vue 3 改为静态导入
  import UploadPicture from "../../components/common/UploadPicture.vue";

  export default {
    components: {
      UploadPicture
    },
    data() {
      return {
        pagination: {
          current: 1,
          size: 10,
          total: 0,
          resourceType: ""
        },
        resources: [],
        resourceTypes: [
          {label: "用户头像", value: "userAvatar"},
          {label: "网站头像", value: "webAvatar"},
          {label: "背景图片", value: "webBackgroundImage"},
          {label: "随机头像", value: "randomAvatar"},
          {label: "随机封面", value: "randomCover"},
          {label: "文章封面", value: "articleCover"},
          {label: "文章图片", value: "articlePicture"},
          {label: "评论图片", value: "commentPicture"},
          {label: "相册相片", value: "lovePhoto"},
          {label: "追番封面", value: "animeCover"},
          {label: "友链头像", value: "friendCover"},
          {label: "音乐封面", value: "musicCover"},
          {label: "音乐文件", value: "musicUrl"},
          {label: "微信图片", value: "wechatPicture"},
          {label: "打赏图片", value: "rewardPicture"}
        ],
        resourceDialog: false,
        storeTypes: [
          {label: "服务器", value: "local"},
          {label: "七牛云", value: "qiniu"}
        ],
        storeType: localStorage.getItem("defaultStoreType")
      }
    },

    computed: {},

    watch: {},

    created() {
      this.getResources();
    },

    mounted() {
    },

    methods: {
      handleDelete(item) {
        this.$confirm('确认删除资源？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.post(this.$constant.baseURL + "/resource/deleteResource", {path: item.path}, true, false)
            .then((res) => {
              this.pagination.current = 1;
              this.getResources();
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

      addFile(res) {
        this.$message({
          message: "上传成功！",
          type: "success"
        });
        this.getResources();
      },

      getTypeLabel(type) {
        let item = this.resourceTypes.find((t) => t.value === type);
        return item ? item.label : type;
      },

      copyPath(path) {
        if (this.$common.isEmpty(path)) {
          return;
        }
        const done = () => {
          this.$message({
            message: "地址已复制！",
            type: "success"
          });
        };
        const fail = () => {
          this.$message({
            message: "复制失败，请手动复制",
            type: "error"
          });
        };
        if (navigator.clipboard && window.isSecureContext) {
          navigator.clipboard.writeText(path).then(done).catch(fail);
          return;
        }
        const textarea = document.createElement("textarea");
        textarea.value = path;
        textarea.style.position = "fixed";
        textarea.style.opacity = "0";
        document.body.appendChild(textarea);
        textarea.select();
        try {
          document.execCommand("copy") ? done() : fail();
        } catch (e) {
          fail();
        }
        document.body.removeChild(textarea);
      },

      addResources() {
        if (this.$common.isEmpty(this.pagination.resourceType)) {
          this.$message({
            message: "请选择资源类型！",
            type: "error"
          });
          return;
        }
        this.resourceDialog = true;
      },
      search() {
        this.pagination.total = 0;
        this.pagination.current = 1;
        this.getResources();
      },
      getResources() {
        this.$http.post(this.$constant.baseURL + "/resource/listResource", this.pagination, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.resources = res.data.records;
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
      changeStatus(item) {
        this.$http.get(this.$constant.baseURL + "/resource/changeResourceStatus", {
          id: item.id,
          flag: item.status
        }, true)
          .then((res) => {
            this.$message({
              message: "修改成功！",
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
      handlePageChange(val) {
        this.pagination.current = val;
        this.getResources();
      }
    }
  }
</script>

<style scoped>

  .handle-select {
    width: 200px;
  }

  .mrb10 {
    margin-right: 10px;
    margin-bottom: 10px;
  }

  .table-td-thumb {
    display: block;
    margin: auto;
    width: 40px;
    height: 40px;
  }

  /* 表格单元格自带 z-index:1 会形成层叠上下文，导致图片预览遮罩被后台顶栏/侧栏盖住 */
  .table :deep(.el-table__cell) {
    z-index: auto;
  }

  .pagination {
    margin: 18px 0 0;
    text-align: right;
  }

  .el-switch {
    margin: 5px;
  }
</style>
