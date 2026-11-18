<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>追番管理</h2>
      <p>维护前台追番页展示的番剧信息，按评分从高到低排序。</p>
    </div>

    <div class="handle-box">
      <el-button type="primary" @click="openAdd()">新增番剧</el-button>
      <el-button :loading="syncing" @click="syncAnime()">立即同步</el-button>
      <el-button @click="openConfig()">同步设置</el-button>
      <span class="sync-status">{{syncStatusText}}</span>
    </div>
    <div v-if="syncStatus.message" class="sync-message">{{syncStatus.message}}</div>

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
      <el-table-column label="类型" width="100" align="center">
        <template #default="scope">
          {{seasonTypeLabel(scope.row.seasonType)}}
        </template>
      </el-table-column>
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
      <el-table-column label="维护" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.seasonId" size="small" type="success">B站</el-tag>
          <el-tag v-else size="small" type="info">手工</el-tag>
          <el-tag v-if="scope.row.visible === 0" size="small" type="warning"
                  style="margin-left: 4px">已隐藏</el-tag>
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
            <el-select v-model="form.seasonType" style="width: 100%" placeholder="选择类型">
              <el-option v-for="option in seasonTypeOptions" :key="option.value"
                         :label="option.label" :value="option.value"></el-option>
            </el-select>
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
          <el-form-item label="前台展示">
            <el-switch v-model="form.visible" :active-value="1" :inactive-value="0"></el-switch>
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

    <el-dialog class="admin-dialog"
               title="B 站同步设置"
               v-model="configVisible"
               width="620px"
               destroy-on-close>
      <el-form :model="configForm" label-position="top" class="anime-form">
        <div class="form-grid">
          <el-form-item label="启用追番同步">
            <el-switch v-model="configForm.enabled"></el-switch>
          </el-form-item>
          <el-form-item label="B 站 UID">
            <el-input v-model="configForm.uid" maxlength="20" placeholder="留空则跳过同步"></el-input>
          </el-form-item>
          <el-form-item label="同步间隔（小时）">
            <el-input-number v-model="configForm.intervalHours" :min="0.5" :step="0.5"
                             :precision="1" style="width: 100%"></el-input-number>
          </el-form-item>
          <el-form-item label="拉取类型">
            <el-select v-model="configForm.type" style="width: 100%">
              <el-option label="全部（追番 + 追剧）" :value="0"></el-option>
              <el-option label="追番（番剧 / 国创）" :value="1"></el-option>
              <el-option label="追剧（电影 / 纪录片 / 电视剧）" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="补全评分与题材">
            <el-switch v-model="configForm.detailEnrich"></el-switch>
          </el-form-item>
        </div>
        <el-form-item label="SESSDATA">
          <el-input v-model="configForm.sessdata" type="password" show-password maxlength="200"
                    :placeholder="sessdataPlaceholder"></el-input>
          <div class="config-tip">
            当前来源：{{configInfo.sessdataSource}}。留空表示不修改；接口只返回脱敏值，不会回传明文。
          </div>
        </el-form-item>
        <div class="config-tip">
          生效说明：启用状态、UID、SESSDATA、拉取类型、补全开关在下一次同步（定时或「立即同步」）时生效；
          同步间隔在下一轮排期时按新值执行，当前这一轮计时不变。
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeConfig()">取 消</el-button>
          <el-button type="primary" :loading="configSaving" @click="saveConfig()">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import UploadPicture from "../../components/common/UploadPicture.vue";
  import { SEASON_TYPE_OPTIONS, seasonTypeLabel as formatSeasonType } from "../../constants/anime.js";

  export default {
    name: "AnimeListView",
    components: {
      UploadPicture
    },
    data() {
      return {
        animeList: [],
        seasonTypeOptions: SEASON_TYPE_OPTIONS,
        dialogVisible: false,
        syncing: false,
        // B 站同步状态：来源、最后成功时间、最后一次结果
        syncStatus: {
          enabled: true,
          uidConfigured: true,
          syncing: false,
          status: "never",
          message: "",
          lastSuccessTime: ""
        },
        // B 站同步配置弹窗（SESSDATA 只显示脱敏值）
        configVisible: false,
        configSaving: false,
        configInfo: {
          sessdataMasked: "",
          sessdataConfigured: false,
          sessdataSource: "未配置"
        },
        configForm: this.emptyConfigForm(),
        form: this.emptyForm()
      }
    },
    computed: {
      sessdataPlaceholder() {
        if (this.configInfo.sessdataConfigured) {
          return "已配置（" + this.configInfo.sessdataMasked + "），留空表示不修改";
        }
        return "账号追番隐私未公开时需要填写";
      },
      syncStatusText() {
        if (!this.syncStatus.enabled) {
          return "B 站同步已关闭";
        }
        if (!this.syncStatus.uidConfigured) {
          return "未配置 B 站 UID，同步已跳过";
        }
        const statusMap = {
          success: "同步成功",
          failed: "同步失败",
          skipped: "已跳过",
          running: "同步中",
          never: "尚未同步"
        };
        const status = statusMap[this.syncStatus.status] || "尚未同步";
        if (this.syncStatus.lastSuccessTime) {
          return "最后同步：" + this.syncStatus.lastSuccessTime + " · " + status;
        }
        return status;
      }
    },
    created() {
      this.getAnime();
      this.getSyncStatus();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          title: "",
          cover: "",
          score: "",
          type: "",
          seasonType: null,
          source: "",
          status: "",
          description: "",
          url: "",
          releaseDate: "",
          visible: 1
        }
      },
      emptyConfigForm() {
        return {
          enabled: true,
          uid: "",
          intervalHours: 6,
          type: 0,
          detailEnrich: true,
          sessdata: ""
        }
      },
      openConfig() {
        this.configVisible = true;
        this.getBilibiliConfig();
      },
      getBilibiliConfig() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getBilibiliConfig", {}, true)
          .then((res) => {
            const data = res.data || {};
            this.configInfo = {
              sessdataMasked: data.sessdataMasked || "",
              sessdataConfigured: !!data.sessdataConfigured,
              sessdataSource: data.sessdataSource || "未配置"
            };
            this.configForm = {
              enabled: data.enabled !== false,
              uid: data.uid || "",
              intervalHours: data.intervalMs ? Number((data.intervalMs / 3600000).toFixed(1)) : 6,
              type: Number(data.type) || 0,
              detailEnrich: data.detailEnrich !== false,
              sessdata: ""
            };
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      saveConfig() {
        const uid = (this.configForm.uid || "").trim();
        if (uid && !/^\d+$/.test(uid)) {
          this.$message({
            message: "B 站 UID 只能是数字！",
            type: "warning"
          });
          return;
        }
        const hours = Number(this.configForm.intervalHours);
        if (!hours || hours < 0.5) {
          this.$message({
            message: "同步间隔不能小于 0.5 小时！",
            type: "warning"
          });
          return;
        }
        this.configSaving = true;
        // SESSDATA 留空表示不修改，不把脱敏值回传给后端
        const data = {
          enabled: this.configForm.enabled,
          uid: uid,
          intervalMs: Math.round(hours * 3600000),
          type: this.configForm.type,
          detailEnrich: this.configForm.detailEnrich,
          sessdata: this.configForm.sessdata ? this.configForm.sessdata.trim() : ""
        };
        this.$http.post(this.$constant.baseURL + "/webInfo/saveBilibiliConfig", data, true)
          .then((res) => {
            const result = res.data || {};
            this.configInfo = {
              sessdataMasked: result.sessdataMasked || "",
              sessdataConfigured: !!result.sessdataConfigured,
              sessdataSource: result.sessdataSource || "未配置"
            };
            this.configForm.sessdata = "";
            this.closeConfig();
            this.getSyncStatus();
            this.$message({
              message: "同步配置已保存，将在下一次同步时生效",
              type: "success"
            });
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          })
          .then(() => {
            this.configSaving = false;
          });
      },
      closeConfig() {
        this.configVisible = false;
        this.configForm = this.emptyConfigForm();
      },
      formatScore(score) {
        if (score === null || score === undefined || score === "") {
          return "—";
        }
        return Number(score).toFixed(1);
      },
      seasonTypeLabel(seasonType) {
        return formatSeasonType(seasonType);
      },
      getAnime() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listAdminAnime", {}, true)
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
      getSyncStatus() {
        this.$http.get(this.$constant.baseURL + "/webInfo/getAnimeSyncStatus", {}, true)
          .then((res) => {
            this.syncStatus = Object.assign({}, this.syncStatus, res.data || {});
          })
          .catch(() => {
            // 状态接口失败不影响列表维护
          });
      },
      syncAnime() {
        this.syncing = true;
        this.$http.get(this.$constant.baseURL + "/webInfo/syncAnime", {}, true)
          .then((res) => {
            const data = res.data || {};
            let type = "success";
            if (data.status === "failed") {
              type = "error";
            } else if (data.status === "skipped" || data.status === "running") {
              type = "warning";
            }
            this.$message({
              message: data.message || "同步完成",
              type: type
            });
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          })
          .then(() => {
            this.syncing = false;
            this.getAnime();
            this.getSyncStatus();
          });
      },
      openAdd() {
        this.form = this.emptyForm();
        this.dialogVisible = true;
      },
      openEdit(anime) {
        this.form = Object.assign(this.emptyForm(), anime, {
          score: anime.score === null || anime.score === undefined ? "" : Number(anime.score).toFixed(1),
          releaseDate: anime.releaseDate ? String(anime.releaseDate).substring(0, 10) : "",
          visible: anime.visible === 0 ? 0 : 1
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
        let data = {
          id: this.form.id,
          title: this.form.title,
          cover: this.form.cover,
          type: this.form.type,
          seasonType: this.form.seasonType,
          source: this.form.source,
          status: this.form.status,
          description: this.form.description,
          url: this.form.url,
          releaseDate: this.form.releaseDate,
          visible: this.form.visible === 0 ? 0 : 1
        };
        if (this.$common.isEmpty(this.form.score) || isNaN(Number(this.form.score))) {
          data.score = null;
        } else {
          data.score = Number(this.form.score);
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
  .handle-box {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }

  .sync-status {
    font-size: 13px;
    color: var(--admin-text-secondary);
  }

  .sync-message {
    margin: -6px 0 12px;
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .config-tip {
    margin-top: 6px;
    font-size: 12px;
    line-height: 1.7;
    color: var(--admin-text-secondary);
  }

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
