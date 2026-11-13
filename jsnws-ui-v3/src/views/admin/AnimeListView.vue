<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>追番管理</h2>
      <p>维护前台追番页展示的番剧信息，按评分从高到低排序。</p>
    </div>

    <div class="handle-box">
      <el-button type="primary" @click="openAdd()">新增番剧</el-button>
    </div>

    <el-table :data="animeList" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column label="封面" width="80" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.cover" lazy class="table-td-thumb" :src="scope.row.cover"
                    :preview-src-list="[scope.row.cover]" fit="cover"></el-image>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="番剧名称" min-width="150" align="center"></el-table-column>
      <el-table-column prop="type" label="类型" width="100" align="center"></el-table-column>
      <el-table-column prop="source" label="来源" width="100" align="center"></el-table-column>
      <el-table-column label="评分" width="80" align="center">
        <template #default="scope">
          {{ formatScore(scope.row.score) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="更新状态" width="130" align="center"></el-table-column>
      <el-table-column prop="releaseDate" label="上映日期" width="110" align="center"></el-table-column>
      <el-table-column prop="description" label="简介" min-width="180" align="center"
                       show-overflow-tooltip></el-table-column>
      <el-table-column label="详情链接" width="100" align="center">
        <template #default="scope">
          <a v-if="scope.row.url" :href="scope.row.url" target="_blank" class="link-btn">访问</a>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="text" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" icon="Delete" style="color: var(--orangeRed)"
                     @click="removeAnime(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog class="admin-dialog"
               :title="form.id ? '编辑番剧' : '新增番剧'"
               v-model="dialogVisible"
               width="620px"
               :before-close="handleClose"
               destroy-on-close>
      <el-form :model="form" label-position="top" class="anime-form">
        <div class="form-grid">
          <el-form-item label="番剧名称">
            <el-input maxlength="100" v-model="form.title" placeholder="例如：葬送的芙莉莲"></el-input>
          </el-form-item>
          <el-form-item label="评分">
            <el-input v-model="form.score" placeholder="0.0 ~ 10.0"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-input maxlength="50" v-model="form.type" placeholder="例如：奇幻 / 冒险"></el-input>
          </el-form-item>
          <el-form-item label="来源">
            <el-input maxlength="50" v-model="form.source" placeholder="例如：B站 / 爱奇艺"></el-input>
          </el-form-item>
          <el-form-item label="更新状态">
            <el-input maxlength="50" v-model="form.status" placeholder="例如：更新至12话 / 全24话"></el-input>
          </el-form-item>
          <el-form-item label="上映日期">
            <el-date-picker v-model="form.releaseDate" type="date" value-format="YYYY-MM-DD"
                            placeholder="选择日期" style="width: 100%"></el-date-picker>
          </el-form-item>
        </div>
        <el-form-item label="封面">
          <div class="media-row">
            <el-input v-model="form.cover" placeholder="封面图片地址，留空则不显示"></el-input>
            <el-image v-if="form.cover" class="media-thumb" :preview-src-list="[form.cover]"
                      :src="form.cover" fit="cover"></el-image>
          </div>
          <UploadPicture :isAdmin="true" :prefix="'animeCover'" style="margin-top: 10px"
                         @addPicture="addCover" :maxSize="2" :maxNumber="1"></UploadPicture>
        </el-form-item>
        <el-form-item label="详情链接">
          <el-input maxlength="500" v-model="form.url" placeholder="番剧详情页地址，留空则不显示"></el-input>
        </el-form-item>
        <el-form-item label="简介">
          <el-input maxlength="500" v-model="form.description" type="textarea" :rows="3"
                    placeholder="一句话介绍这部番剧"></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveAnime()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import UploadPicture from "../../components/common/UploadPicture.vue";

  export default {
    name: "AnimeListView",
    components: {
      UploadPicture
    },
    data() {
      return {
        animeList: [],
        dialogVisible: false,
        form: this.emptyForm()
      }
    },
    created() {
      this.getAnime();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          title: "",
          cover: "",
          score: "",
          type: "",
          source: "",
          status: "",
          description: "",
          url: "",
          releaseDate: ""
        }
      },
      formatScore(score) {
        if (score === null || score === undefined || score === "") {
          return "—";
        }
        return Number(score).toFixed(1);
      },
      getAnime() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listAnime", {}, true)
          .then((res) => {
            this.animeList = this.$common.isEmpty(res.data) ? [] : res.data;
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      openAdd() {
        this.form = this.emptyForm();
        this.dialogVisible = true;
      },
      openEdit(anime) {
        this.form = Object.assign(this.emptyForm(), anime, {
          score: anime.score === null || anime.score === undefined ? "" : Number(anime.score).toFixed(1),
          releaseDate: anime.releaseDate ? String(anime.releaseDate).substring(0, 10) : ""
        });
        this.dialogVisible = true;
      },
      addCover(picture) {
        this.form.cover = picture;
      },
      saveAnime() {
        if (this.$common.isEmpty(this.form.title)) {
          this.$message({
            message: "请填写番剧名称！",
            type: "warning"
          });
          return;
        }
        let data = Object.assign({}, this.form);
        if (this.$common.isEmpty(data.score) || isNaN(Number(data.score))) {
          data.score = null;
        } else {
          data.score = Number(data.score);
        }
        let url = data.id ? "/webInfo/updateAnime" : "/webInfo/saveAnime";
        this.$http.post(this.$constant.baseURL + url, data, true)
          .then(() => {
            this.handleClose();
            this.getAnime();
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
      removeAnime(anime) {
        this.$confirm('确认删除《' + anime.title + '》？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/deleteAnime", {id: anime.id}, true)
            .then(() => {
              this.getAnime();
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
    width: 40px;
    height: 54px;
    border-radius: 6px;
  }

  .empty-text {
    color: var(--admin-text-secondary);
  }

  .link-btn {
    font-size: 13px;
    color: var(--admin-primary, #4c6fff);
    text-decoration: none;
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 18px;
  }

  .anime-form :deep(.el-form-item__label) {
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
    width: 40px;
    height: 54px;
    border-radius: 6px;
  }
</style>
