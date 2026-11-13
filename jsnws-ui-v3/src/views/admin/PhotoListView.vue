<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>相片管理</h2>
      <p>维护前台照片墙的相片，按相册分类展示。</p>
    </div>

    <div class="handle-box">
      <el-select v-model="classifyFilter" placeholder="全部相册" clearable size="default"
                 style="width: 180px; margin-right: 12px" @change="searchPhotos()">
        <el-option v-for="item in classifyList" :key="item.classify" :label="item.classify + '（' + item.count + '）'"
                   :value="item.classify"></el-option>
      </el-select>
      <el-button type="primary" @click="openAdd()">新增相片</el-button>
    </div>

    <el-table :data="photoList" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column label="图片" width="90" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.cover" lazy class="table-td-thumb" :src="scope.row.cover"
                    :preview-src-list="[scope.row.cover]" fit="cover"></el-image>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" align="center"></el-table-column>
      <el-table-column prop="classify" label="相册" width="140" align="center">
        <template #default="scope">
          <span v-if="scope.row.classify">{{ scope.row.classify }}</span>
          <span v-else class="empty-text">未分类</span>
        </template>
      </el-table-column>
      <el-table-column prop="introduction" label="简介" min-width="200" align="center">
        <template #default="scope">
          <span v-if="scope.row.introduction" class="intro-text" :title="scope.row.introduction">
            {{ scope.row.introduction }}
          </span>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status ? 'success' : 'info'" size="small">
            {{ scope.row.status ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="165" align="center">
        <template #default="scope">
          <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="text" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" icon="Delete" style="color: var(--orangeRed)"
                     @click="removePhoto(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination background
                     layout="total, prev, pager, next"
                     :current-page="pagination.current"
                     :page-size="pagination.size"
                     :total="pagination.total"
                     @current-change="pagePhotos"></el-pagination>
    </div>

    <el-dialog class="admin-dialog"
               :title="form.id ? '编辑相片' : '新增相片'"
               v-model="dialogVisible"
               width="620px"
               :before-close="handleClose"
               destroy-on-close>
      <el-form :model="form" label-position="top" class="photo-form">
        <div class="form-grid">
          <el-form-item label="标题">
            <el-input maxlength="60" v-model="form.title" placeholder="例如：海边日落"></el-input>
          </el-form-item>
          <el-form-item label="相册">
            <el-input maxlength="30" v-model="form.classify" placeholder="例如：旅行 / 日常"
                      list="classify-options"></el-input>
            <datalist id="classify-options">
              <option v-for="item in classifyList" :key="item.classify" :value="item.classify"></option>
            </datalist>
          </el-form-item>
        </div>
        <el-form-item label="简介">
          <el-input maxlength="1000" v-model="form.introduction" type="textarea" :rows="2"
                    placeholder="这张相片的说明，可留空"></el-input>
        </el-form-item>
        <el-form-item label="图片">
          <div class="media-row">
            <el-input v-model="form.cover" placeholder="图片地址，留空则不显示"></el-input>
            <el-image v-if="form.cover" class="media-thumb" :preview-src-list="[form.cover]"
                      :src="form.cover" fit="cover"></el-image>
          </div>
          <UploadPicture :isAdmin="true" :prefix="'lovePhoto'" style="margin-top: 10px"
                         @addPicture="addCover" :maxSize="5" :maxNumber="1"></UploadPicture>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-text="显示" inactive-text="隐藏"></el-switch>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="savePhoto()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import UploadPicture from "../../components/common/UploadPicture.vue";

  export default {
    name: "PhotoListView",
    components: {
      UploadPicture
    },
    data() {
      return {
        photoList: [],
        classifyList: [],
        classifyFilter: "",
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
      this.getPhotos();
      this.getClassifyList();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          title: "",
          classify: "",
          introduction: "",
          cover: "",
          status: true
        }
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
      getClassifyList() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listAdminLovePhoto", {}, true)
          .then((res) => {
            this.classifyList = this.$common.isEmpty(res.data) ? [] : res.data;
          })
          .catch(() => {
          });
      },
      getPhotos() {
        let params = {
          current: this.pagination.current,
          size: this.pagination.size,
          resourceType: "lovePhoto"
        };
        if (this.classifyFilter !== "") {
          params.classify = this.classifyFilter;
        }
        this.$http.post(this.$constant.baseURL + "/webInfo/listResourcePath", params, true)
          .then((res) => {
            if (this.$common.isEmpty(res.data)) {
              this.photoList = [];
              this.pagination.total = 0;
              return;
            }
            this.photoList = res.data.records || [];
            this.pagination.total = res.data.total || 0;
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      searchPhotos() {
        this.pagination.current = 1;
        this.getPhotos();
      },
      pagePhotos(current) {
        this.pagination.current = current;
        this.getPhotos();
      },
      openAdd() {
        this.form = this.emptyForm();
        if (this.classifyFilter !== "") {
          this.form.classify = this.classifyFilter;
        }
        this.dialogVisible = true;
      },
      openEdit(photo) {
        this.form = Object.assign(this.emptyForm(), photo, {
          status: photo.status === undefined || photo.status === null ? true : !!photo.status
        });
        this.dialogVisible = true;
      },
      addCover(picture) {
        this.form.cover = picture;
      },
      savePhoto() {
        if (this.$common.isEmpty(this.form.title)) {
          this.$message({
            message: "请填写相片标题！",
            type: "warning"
          });
          return;
        }
        if (this.$common.isEmpty(this.form.classify)) {
          this.$message({
            message: "请填写相册分类！",
            type: "warning"
          });
          return;
        }
        if (this.$common.isEmpty(this.form.cover)) {
          this.$message({
            message: "请上传相片图片！",
            type: "warning"
          });
          return;
        }
        let data = Object.assign({}, this.form, {type: "lovePhoto"});
        let url = data.id ? "/webInfo/updateResourcePath" : "/webInfo/saveResourcePath";
        this.$http.post(this.$constant.baseURL + url, data, true)
          .then(() => {
            this.handleClose();
            this.getPhotos();
            this.getClassifyList();
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
      removePhoto(photo) {
        this.$confirm('确认删除「' + photo.title + '」？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/deleteResourcePath", {id: photo.id}, true)
            .then(() => {
              this.getPhotos();
              this.getClassifyList();
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
  .table-td-thumb {
    display: block;
    margin: auto;
    width: 56px;
    height: 42px;
    border-radius: 8px;
  }

  .empty-text {
    color: var(--admin-text-secondary);
  }

  .intro-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .time-text {
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 18px;
  }

  .photo-form :deep(.el-form-item__label) {
    padding-bottom: 6px;
    font-size: 13px;
    font-weight: 500;
    color: var(--admin-text-soft);
  }

  .media-row {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
  }

  .media-thumb {
    flex-shrink: 0;
    width: 56px;
    height: 42px;
    border-radius: 8px;
  }
</style>
