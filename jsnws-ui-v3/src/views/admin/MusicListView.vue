<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>音乐管理</h2>
      <p>维护前台侧栏音乐播放器的曲目，按排序值从小到大播放。</p>
    </div>

    <div class="handle-box">
      <el-button type="primary" @click="openAdd()">新增歌曲</el-button>
    </div>

    <el-table :data="musicList" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column label="封面" width="80" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.cover" lazy class="table-td-thumb" :src="scope.row.cover"
                    :preview-src-list="[scope.row.cover]" fit="cover"></el-image>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="歌曲名称" min-width="150" align="center"></el-table-column>
      <el-table-column prop="artist" label="歌手" width="120" align="center"></el-table-column>
      <el-table-column label="播放地址" width="100" align="center">
        <template #default="scope">
          <a v-if="scope.row.url" :href="scope.row.url" target="_blank" class="link-btn">试听</a>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="歌词" min-width="200" align="center">
        <template #default="scope">
          <span v-if="scope.row.lyrics" class="lyrics-preview" :title="scope.row.lyrics">
            {{ lyricsPreview(scope.row.lyrics) }}
          </span>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" align="center"></el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="text" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" icon="Delete" style="color: var(--orangeRed)"
                     @click="removeMusic(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog class="admin-dialog"
               :title="form.id ? '编辑歌曲' : '新增歌曲'"
               v-model="dialogVisible"
               width="620px"
               :before-close="handleClose"
               destroy-on-close>
      <el-form :model="form" label-position="top" class="music-form">
        <div class="form-grid">
          <el-form-item label="歌曲名称">
            <el-input maxlength="100" v-model="form.name" placeholder="例如：晴天"></el-input>
          </el-form-item>
          <el-form-item label="歌手">
            <el-input maxlength="100" v-model="form.artist" placeholder="例如：周杰伦"></el-input>
          </el-form-item>
          <el-form-item label="排序">
            <el-input v-model="form.sort" placeholder="数字越小越靠前，留空按 0 处理"></el-input>
          </el-form-item>
        </div>
        <el-form-item label="封面">
          <div class="media-row">
            <el-input v-model="form.cover" placeholder="封面图片地址，留空则不显示"></el-input>
            <el-image v-if="form.cover" class="media-thumb" :preview-src-list="[form.cover]"
                      :src="form.cover" fit="cover"></el-image>
          </div>
          <UploadPicture :isAdmin="true" :prefix="'musicCover'" style="margin-top: 10px"
                         @addPicture="addCover" :maxSize="2" :maxNumber="1"></UploadPicture>
        </el-form-item>
        <el-form-item label="播放地址">
          <el-input maxlength="500" v-model="form.url"
                    placeholder="音频地址，例如 http://localhost:8080/xxx.mp3"></el-input>
          <UploadPicture :isAdmin="true" :prefix="'musicUrl'" style="margin-top: 10px"
                         :listType="'text'" :accept="'audio/*'" @addPicture="addAudio"
                         :maxSize="20" :maxNumber="1"></UploadPicture>
        </el-form-item>
        <el-form-item label="歌词">
          <el-input maxlength="5000" v-model="form.lyrics" type="textarea" :rows="6"
                    placeholder="每行一句歌词；如需滚动高亮可写成 [00:12.00]歌词，时间标签可省略"></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveMusic()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import UploadPicture from "../../components/common/UploadPicture.vue";

  export default {
    name: "MusicListView",
    components: {
      UploadPicture
    },
    data() {
      return {
        musicList: [],
        dialogVisible: false,
        form: this.emptyForm()
      }
    },
    created() {
      this.getMusic();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          name: "",
          artist: "",
          cover: "",
          url: "",
          lyrics: "",
          sort: ""
        }
      },
      lyricsPreview(lyrics) {
        const lines = String(lyrics).split("\n");
        for (let i = 0; i < lines.length; i++) {
          const text = lines[i].replace(/^\[\d{1,2}:\d{1,2}(\.\d{1,3})?\]\s*/, "").trim();
          if (text !== "") {
            return text;
          }
        }
        return "—";
      },
      getMusic() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listMusic", {}, true)
          .then((res) => {
            this.musicList = this.$common.isEmpty(res.data) ? [] : res.data;
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
      openEdit(music) {
        this.form = Object.assign(this.emptyForm(), music, {
          sort: music.sort === null || music.sort === undefined ? "" : music.sort
        });
        this.dialogVisible = true;
      },
      addCover(picture) {
        this.form.cover = picture;
      },
      addAudio(file) {
        this.form.url = file;
      },
      saveMusic() {
        if (this.$common.isEmpty(this.form.name)) {
          this.$message({
            message: "请填写歌曲名称！",
            type: "warning"
          });
          return;
        }
        if (this.$common.isEmpty(this.form.url)) {
          this.$message({
            message: "请填写播放地址！",
            type: "warning"
          });
          return;
        }
        let data = Object.assign({}, this.form);
        data.sort = this.$common.isEmpty(data.sort) || isNaN(Number(data.sort)) ? 0 : Number(data.sort);
        let url = data.id ? "/webInfo/updateMusic" : "/webInfo/saveMusic";
        this.$http.post(this.$constant.baseURL + url, data, true)
          .then(() => {
            this.handleClose();
            this.getMusic();
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
      removeMusic(music) {
        this.$confirm('确认删除《' + music.name + '》？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/deleteMusic", {id: music.id}, true)
            .then(() => {
              this.getMusic();
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
    width: 44px;
    height: 44px;
    border-radius: 8px;
  }

  .empty-text {
    color: var(--admin-text-secondary);
  }

  .link-btn {
    font-size: 13px;
    color: var(--admin-primary, #4c6fff);
    text-decoration: none;
  }

  .lyrics-preview {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 18px;
  }

  .music-form :deep(.el-form-item__label) {
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
    width: 44px;
    height: 44px;
    border-radius: 8px;
  }
</style>
