<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>友链管理</h2>
      <p>维护前台友链页展示的站点，并审核访客提交的友链申请。</p>
    </div>

    <div class="handle-box">
      <el-radio-group v-model="statusFilter" style="margin-right: 12px">
        <el-radio-button :label="0">待审核（{{ pendingCount }}）</el-radio-button>
        <el-radio-button :label="1">已通过（{{ approvedCount }}）</el-radio-button>
        <el-radio-button :label="2">已拒绝（{{ rejectedCount }}）</el-radio-button>
        <el-radio-button :label="''">全部（{{ allFriends.length }}）</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="openAdd()">新增友链</el-button>
    </div>

    <el-table :data="friendList" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column label="图标" width="80" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.cover" lazy class="table-td-thumb" :src="scope.row.cover"
                    :preview-src-list="[scope.row.cover]" fit="cover"></el-image>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="名称" min-width="130" align="center"></el-table-column>
      <el-table-column prop="introduction" label="简介" min-width="200" align="center">
        <template #default="scope">
          <span v-if="scope.row.introduction" class="intro-text" :title="scope.row.introduction">
            {{ scope.row.introduction }}
          </span>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="链接" min-width="170" align="center">
        <template #default="scope">
          <a v-if="scope.row.url" :href="scope.row.url" target="_blank" class="link-btn">{{ scope.row.url }}</a>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)" size="small">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="165" align="center">
        <template #default="scope">
          <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.status !== 1" type="text"
                     style="color: #12b76a" @click="audit(scope.row, 1)">通过</el-button>
          <el-button v-if="scope.row.status !== 2" type="text"
                     style="color: var(--orangeRed)" @click="audit(scope.row, 2)">拒绝</el-button>
          <el-button type="text" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" icon="Delete" style="color: var(--orangeRed)"
                     @click="removeFriend(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog class="admin-dialog"
               :title="form.id ? '编辑友链' : '新增友链'"
               v-model="dialogVisible"
               width="600px"
               :before-close="handleClose"
               destroy-on-close>
      <el-form :model="form" label-position="top" class="friend-form">
        <div class="form-grid">
          <el-form-item label="友链名称">
            <el-input maxlength="50" v-model="form.title" placeholder="例如：King 的博客"></el-input>
          </el-form-item>
          <el-form-item label="友链地址">
            <el-input maxlength="500" v-model="form.url" placeholder="例如：https://example.com"></el-input>
          </el-form-item>
        </div>
        <el-form-item label="简介">
          <el-input maxlength="255" v-model="form.introduction" placeholder="一句话介绍这个站点"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <div class="media-row">
            <el-input v-model="form.cover" placeholder="图标地址，留空则不显示"></el-input>
            <el-image v-if="form.cover" class="media-thumb" :preview-src-list="[form.cover]"
                      :src="form.cover" fit="cover"></el-image>
          </div>
          <UploadPicture :isAdmin="true" :prefix="'friendCover'" style="margin-top: 10px"
                         @addPicture="addCover" :maxSize="2" :maxNumber="1"></UploadPicture>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveFriend()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import UploadPicture from "../../components/common/UploadPicture.vue";

  export default {
    name: "FriendListView",
    components: {
      UploadPicture
    },
    data() {
      return {
        allFriends: [],
        statusFilter: 0,
        dialogVisible: false,
        form: this.emptyForm()
      }
    },
    computed: {
      friendList() {
        if (this.statusFilter === "") {
          return this.allFriends;
        }
        return this.allFriends.filter((friend) => friend.status === this.statusFilter);
      },
      pendingCount() {
        return this.allFriends.filter((friend) => friend.status === 0).length;
      },
      approvedCount() {
        return this.allFriends.filter((friend) => friend.status === 1).length;
      },
      rejectedCount() {
        return this.allFriends.filter((friend) => friend.status === 2).length;
      }
    },
    created() {
      this.getFriends();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          title: "",
          introduction: "",
          cover: "",
          url: "",
          status: 1
        }
      },
      statusText(status) {
        if (status === 1) {
          return "已通过";
        }
        return status === 2 ? "已拒绝" : "待审核";
      },
      statusType(status) {
        if (status === 1) {
          return "success";
        }
        return status === 2 ? "danger" : "warning";
      },
      formatTime(time) {
        if (!time) {
          return "—";
        }
        let date = new Date(time);
        if (isNaN(date.getTime())) {
          return time;
        }
        const pad = (num) => (num < 10 ? "0" + num : num);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate()) + " " +
          pad(date.getHours()) + ":" + pad(date.getMinutes());
      },
      getFriends() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listAdminFriend", {}, true)
          .then((res) => {
            this.allFriends = this.$common.isEmpty(res.data) ? [] : res.data;
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      audit(friend, status) {
        const action = status === 1 ? "通过" : "拒绝";
        this.$confirm('确认' + action + '友链「' + friend.title + '」？', '友链审核', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: status === 1 ? 'success' : 'warning',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/auditFriend",
            {id: friend.id, status: status}, true)
            .then(() => {
              this.getFriends();
              this.$message({
                message: action + '成功！',
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
        });
      },
      openAdd() {
        this.form = this.emptyForm();
        this.dialogVisible = true;
      },
      openEdit(friend) {
        this.form = Object.assign(this.emptyForm(), friend);
        this.dialogVisible = true;
      },
      addCover(picture) {
        this.form.cover = picture;
      },
      saveFriend() {
        if (this.$common.isEmpty(this.form.title)) {
          this.$message({
            message: "请填写友链名称！",
            type: "warning"
          });
          return;
        }
        if (this.$common.isEmpty(this.form.url)) {
          this.$message({
            message: "请填写友链地址！",
            type: "warning"
          });
          return;
        }
        let data = Object.assign({}, this.form);
        data.avatar = data.cover;
        let url = data.id ? "/webInfo/updateFriend" : "/webInfo/saveFriend";
        this.$http.post(this.$constant.baseURL + url, data, true)
          .then(() => {
            this.handleClose();
            this.getFriends();
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
      removeFriend(friend) {
        this.$confirm('确认删除友链「' + friend.title + '」？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/deleteFriend", {id: friend.id}, true)
            .then(() => {
              this.getFriends();
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

  .friend-form :deep(.el-form-item__label) {
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
