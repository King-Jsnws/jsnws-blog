<template>
  <div>
    <div class="admin-card">
      <div class="admin-page-head">
        <h2>用户列表</h2>
        <p>管理站点用户、账号状态与快捷赞赏登记。</p>
      </div>
      <div class="handle-box">
        <el-select v-model="pagination.userType" placeholder="用户类型" class="handle-select mrb10">
          <el-option key="1" label="站长" :value="0"></el-option>
          <el-option key="2" label="管理员" :value="1"></el-option>
          <el-option key="3" label="普通用户" :value="2"></el-option>
        </el-select>
        <el-select v-model="pagination.userStatus" placeholder="用户状态" class="handle-select mrb10">
          <el-option key="1" label="启用" :value="true"></el-option>
          <el-option key="2" label="禁用" :value="false"></el-option>
        </el-select>
        <el-input v-model="pagination.searchKey" placeholder="账号/昵称/手机号/邮箱" class="handle-input mrb10"></el-input>
        <el-button type="primary" icon="Search" @click="searchUser()">搜索</el-button>
        <el-button type="danger" @click="clearSearch()">清除参数</el-button>
      </div>
      <div class="table-scroll">
        <el-table :data="users" border class="table" header-cell-class-name="table-header">
          <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
          <el-table-column prop="username" label="账号" align="center">
            <template #default="scope">
              <span v-if="isAdmin" class="username-cell" title="点击修改账号" @click="editUsername(scope.row)">
                <span>{{ scope.row.username }}</span>
                <el-icon class="username-edit"><Edit/></el-icon>
              </span>
              <span v-else>{{ scope.row.username }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="昵称" align="center"></el-table-column>
          <el-table-column prop="phoneNumber" label="手机号" align="center"></el-table-column>
          <el-table-column prop="email" label="邮箱" align="center"></el-table-column>
          <el-table-column label="赞赏" width="190" align="center">
            <template #default="scope">
              <div class="admire-cell">
                <el-input v-model="scope.row.admireInput" size="small" maxlength="10" placeholder="金额"
                          @keyup.enter="addAdmire(scope.row)"></el-input>
                <el-button type="text" :loading="scope.row.admireSaving"
                           @click="addAdmire(scope.row)">记录</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="用户状态" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.userStatus === false ? 'danger' : 'success'"
                      disable-transitions>
                {{scope.row.userStatus === false ? '禁用' : '启用'}}
              </el-tag>
              <el-switch v-if="scope.row.id !== $store.state.currentAdmin.id" @click="changeUserStatus(scope.row)" v-model="scope.row.userStatus"></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="头像" align="center">
            <template #default="scope">
              <el-image lazy class="table-td-thumb" :src="scope.row.avatar" fit="cover"></el-image>
            </template>
          </el-table-column>
          <el-table-column label="性别" align="center">
            <template #default="scope">
              <el-tag type="success"
                      v-if="scope.row.gender === 1"
                      disable-transitions>
                男
              </el-tag>
              <el-tag type="success"
                      v-else-if="scope.row.gender === 2"
                      disable-transitions>
                女
              </el-tag>
              <el-tag type="success"
                      v-else
                      disable-transitions>
                保密
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="introduction" label="简介" align="center"></el-table-column>
          <el-table-column label="用户类型" width="100" align="center">
            <template #default="scope">
              <el-tag type="success"
                      v-if="scope.row.userType === 0"
                      disable-transitions>
                站长
              </el-tag>
              <el-tag type="success"
                      v-else-if="scope.row.userType === 1"
                      style="cursor: pointer"
                      @click="editUser(scope.row)"
                      disable-transitions>
                管理员
              </el-tag>
              <el-tag type="success"
                      v-else
                      style="cursor: pointer"
                      @click="editUser(scope.row)"
                      disable-transitions>
                普通用户
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="170" align="center"></el-table-column>
        </el-table>
      </div>
      <div class="pagination">
        <el-pagination background layout="total, prev, pager, next"
                       :current-page="pagination.current"
                       :page-size="pagination.size"
                       :total="pagination.total"
                       @current-change="handlePageChange">
        </el-pagination>
      </div>
    </div>

    <!-- 编辑弹出框 -->
    <el-dialog class="admin-dialog"
               title="修改用户类型"
               v-model="editVisible"
               width="30%"
               :before-close="handleClose"
               :append-to-body="true"
               destroy-on-close
               center>
      <div class="myCenter">
        <el-radio-group v-model="changeUser.userType">
          <el-radio-button :label="1">管理员</el-radio-button>
          <el-radio-button :label="2">普通用户</el-radio-button>
        </el-radio-group>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveEdit()">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改账号弹窗：只改 username 一个字段 -->
    <el-dialog class="admin-dialog"
               title="修改账号"
               v-model="usernameVisible"
               width="30%"
               :before-close="handleUsernameClose"
               :append-to-body="true"
               destroy-on-close
               center>
      <div class="username-form">
        <el-input v-model="usernameForm.username"
                  maxlength="50"
                  show-word-limit
                  placeholder="请输入新账号"
                  @keyup.enter="saveUsername()"/>
        <p class="username-form-tip">账号用于登录，不能为空、不能含空格，最多 50 个字符</p>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleUsernameClose()">取 消</el-button>
          <el-button type="primary" :loading="usernameSaving" @click="saveUsername()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
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
          searchKey: "",
          userStatus: null,
          userType: null
        },
        users: [],
        changeUser: {
          id: null,
          userType: null
        },
        editVisible: false,
        usernameVisible: false,
        usernameSaving: false,
        usernameForm: {
          id: null,
          username: ""
        }
      }
    },

    computed: {
      // 账号编辑入口仅管理员可见（后台页面本身已受路由与后端双重鉴权）
      isAdmin() {
        return !this.$common.isEmpty(this.$store.state.currentAdmin) && Boolean(localStorage.getItem("adminToken"));
      }
    },

    watch: {},

    created() {
      this.getUsers();
    },

    mounted() {
    },

    methods: {
      clearSearch() {
        this.pagination = {
          current: 1,
          size: 10,
          total: 0,
          searchKey: "",
          userStatus: null,
          userType: null
        }
        this.getUsers();
      },
      getUsers() {
        this.$http.post(this.$constant.baseURL + "/admin/user/list", this.pagination, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.users = res.data.records;
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
      changeUserStatus(user) {
        this.$http.get(this.$constant.baseURL + "/admin/user/changeUserStatus", {
          userId: user.id,
          flag: user.userStatus
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
      addAdmire(user) {
        let amount = Number(user.admireInput);
        if (this.$common.isEmpty(user.admireInput) || isNaN(amount) || amount <= 0) {
          this.$message({
            message: "请输入正确的赞赏金额！",
            type: "warning"
          });
          return;
        }
        user.admireSaving = true;
        this.$http.post(this.$constant.baseURL + "/admin/sponsor/save", {
          name: this.$common.isEmpty(user.nickname) ? user.username : user.nickname,
          avatar: user.avatar,
          amount: amount,
          remark: "来自用户列表赞赏"
        }, true)
          .then(() => {
            user.admireInput = "";
            user.admireSaving = false;
            this.$message({
              message: "已记录到赞助记录！",
              type: "success"
            });
          })
          .catch((error) => {
            user.admireSaving = false;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      editUser(user) {
        this.changeUser.id = user.id;
        this.changeUser.userType = user.userType;
        this.editVisible = true;
      },
      editUsername(user) {
        if (!this.isAdmin) {
          return;
        }
        this.usernameForm = {
          id: user.id,
          username: user.username
        };
        this.usernameVisible = true;
      },
      handleUsernameClose() {
        this.usernameForm = {
          id: null,
          username: ""
        };
        this.usernameSaving = false;
        this.usernameVisible = false;
      },
      saveUsername() {
        if (this.usernameSaving) {
          return;
        }
        const value = String(this.usernameForm.username || "").trim();
        if (this.$common.isEmpty(value)) {
          this.$message({
            message: "账号不能为空！",
            type: "warning"
          });
          return;
        }
        if (value.length > 50) {
          this.$message({
            message: "账号长度不能超过 50 个字符！",
            type: "warning"
          });
          return;
        }
        if (/[\s\u3000]/.test(value)) {
          this.$message({
            message: "账号不能包含空格！",
            type: "warning"
          });
          return;
        }
        const userId = this.usernameForm.id;
        this.usernameSaving = true;
        this.$http.get(this.$constant.baseURL + "/admin/user/updateUsername", {
          userId: userId,
          username: value
        }, true)
          .then(() => {
            const user = this.users.find(item => item.id === userId);
            if (user) {
              user.username = value;
            }
            // 改的是当前登录管理员自己的账号时，同步内存与本地缓存，避免顶部信息仍显示旧账号
            if (this.$store.state.currentAdmin && this.$store.state.currentAdmin.id === userId) {
              this.$store.commit("loadCurrentAdmin",
                Object.assign({}, this.$store.state.currentAdmin, {username: value}));
            }
            if (this.$store.state.currentUser && this.$store.state.currentUser.id === userId) {
              this.$store.commit("loadCurrentUser",
                Object.assign({}, this.$store.state.currentUser, {username: value}));
            }
            this.handleUsernameClose();
            this.$message({
              message: "账号修改成功！",
              type: "success"
            });
          })
          .catch((error) => {
            this.usernameSaving = false;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      handlePageChange(val) {
        this.pagination.current = val;
        this.getUsers();
      },
      searchUser() {
        this.pagination.total = 0;
        this.pagination.current = 1;
        this.getUsers();
      },
      handleClose() {
        this.changeUser = {
          id: null,
          userType: null
        };
        this.editVisible = false;
      },
      saveEdit() {
        this.$http.get(this.$constant.baseURL + "/admin/user/changeUserType", {
          userId: this.changeUser.id,
          userType: this.changeUser.userType
        }, true)
          .then((res) => {
            this.handleClose();
            this.getUsers();
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
      }
    }
  }
</script>

<style scoped>

  /* 12 列在窄屏会超出卡片，用横向滚动保证最后一列可见 */
  .table-scroll {
    width: 100%;
    overflow-x: auto;
  }

  .table-scroll :deep(.table) {
    min-width: 1180px;
  }

  .admire-cell {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .username-cell {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
  }

  .username-edit {
    color: var(--admin-primary, #4c6fff);
    cursor: pointer;
    opacity: 0.7;
    transition: opacity 0.2s;
  }

  .username-edit:hover {
    opacity: 1;
  }

  .handle-select {
    width: 120px;
  }

  .handle-input {
    width: 160px;
    display: inline-block;
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

  .pagination {
    margin: 18px 0 0;
    text-align: right;
  }

  .el-switch {
    margin: 5px;
  }
</style>
