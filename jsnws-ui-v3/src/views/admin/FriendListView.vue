<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>友链管理</h2>
      <p>维护前台友链页展示的站点，并审核访客提交的友链申请。</p>
    </div>

    <!-- 本站友链信息：前台「友链」页展示、供访客复制的本站资料，存 sys_config -->
    <section class="site-info-card">
      <div class="site-info-head">
        <h3>本站友链信息</h3>
        <p>配置前台「友链」页展示的本站资料，访客可一键复制这些内容添加本站友链。</p>
      </div>
      <el-form :model="siteForm" label-position="top" class="site-info-form">
        <div class="form-grid">
          <el-form-item label="站点名称">
            <el-input maxlength="50" v-model="siteForm.friendWebName" placeholder="例如：King"></el-input>
          </el-form-item>
          <el-form-item label="站点描述">
            <el-input maxlength="120" v-model="siteForm.friendIntroduction" placeholder="一句话介绍本站"></el-input>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="站点链接">
            <el-input maxlength="200" v-model="siteForm.friendUrl" placeholder="例如：https://example.com"></el-input>
          </el-form-item>
          <el-form-item label="头像链接">
            <div class="media-row">
              <el-input maxlength="500" v-model="siteForm.friendAvatar"
                        placeholder="头像图片地址，留空则使用默认头像"></el-input>
              <el-image v-if="siteForm.friendAvatar" class="media-thumb" :src="siteForm.friendAvatar"
                        :preview-src-list="[siteForm.friendAvatar]" fit="cover"></el-image>
            </div>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="RSS 地址">
            <el-input maxlength="500" v-model="siteForm.friendRssUrl"
                      placeholder="例如：https://example.com/atom.xml，留空则前台不显示"></el-input>
          </el-form-item>
          <el-form-item label="主页截图">
            <div class="media-row">
              <el-input maxlength="500" v-model="siteForm.friendScreenshotUrl"
                        placeholder="站点首页截图地址，留空则前台不显示"></el-input>
              <el-image v-if="siteForm.friendScreenshotUrl" class="media-thumb" :src="siteForm.friendScreenshotUrl"
                        :preview-src-list="[siteForm.friendScreenshotUrl]" fit="cover"></el-image>
            </div>
          </el-form-item>
        </div>
      </el-form>
      <div class="site-info-foot">
        <el-button type="primary" @click="saveSiteInfo()">保存本站信息</el-button>
      </div>
    </section>

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
          <a v-if="scope.row.url" :href="scope.row.url" target="_blank" class="link-btn"
             :title="scope.row.url">{{ scope.row.url }}</a>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="封面地址" min-width="170" align="center">
        <template #default="scope">
          <a v-if="scope.row.cover" :href="scope.row.cover" target="_blank" class="link-btn"
             :title="scope.row.cover">{{ scope.row.cover }}</a>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="头像" min-width="170" align="center">
        <template #default="scope">
          <a v-if="scope.row.avatar" :href="scope.row.avatar" target="_blank" class="link-btn"
             :title="scope.row.avatar">{{ scope.row.avatar }}</a>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="RSS" min-width="170" align="center">
        <template #default="scope">
          <a v-if="scope.row.rssUrl" :href="scope.row.rssUrl" target="_blank" class="link-btn"
             :title="scope.row.rssUrl">{{ scope.row.rssUrl }}</a>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="档次" width="110" align="center">
        <template #default="scope">
          <el-tag :type="tierTagType(scope.row.tier)" size="small">{{ tierText(scope.row.tier) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="推荐" width="90" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.recommended === 1" type="danger" size="small">推荐中</el-tag>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="140" align="center">
        <template #default="scope">
          <span v-if="scope.row.tags" class="intro-text" :title="scope.row.tags">{{ scope.row.tags }}</span>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="响应时间" width="95" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.latencyMs != null" :type="latencyTagType(scope.row.latencyMs)" size="small">
            {{ scope.row.latencyMs }} ms
          </el-tag>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column label="最近更新" width="115" align="center">
        <template #default="scope">
          <span v-if="scope.row.rssUpdatedAt" class="time-text" :title="scope.row.rssTitle || ''">
            {{ shortDate(scope.row.rssUpdatedAt) }}
          </span>
          <span v-else class="empty-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="clickCount" label="点击量" width="80" align="center">
        <template #default="scope">
          <span>{{ scope.row.clickCount == null ? 0 : scope.row.clickCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="可用状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="healthTagType(scope.row.healthStatus)" size="small">
            {{ healthText(scope.row.healthStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="95" align="center">
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
      <el-table-column label="操作" width="370" align="center">
        <template #default="scope">
          <div class="op-cell">
            <el-button v-if="scope.row.status !== 1" class="op-btn" type="success" plain
                       @click="audit(scope.row, 1)">通过</el-button>
            <el-button v-if="scope.row.status === 0" class="op-btn" type="warning" plain
                       @click="audit(scope.row, 2)">拒绝</el-button>
            <el-button v-if="scope.row.recommended !== 1" class="op-btn" type="primary" plain
                       @click="openRecommend(scope.row)">推荐</el-button>
            <el-button v-else class="op-btn" plain
                       @click="cancelRecommend(scope.row)">取消推荐</el-button>
            <el-button class="op-btn" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button class="op-btn" type="danger" plain
                       @click="removeFriend(scope.row)">删除</el-button>
          </div>
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
        </el-form-item>
        <el-form-item label="头像">
          <el-input maxlength="500" v-model="form.avatar" placeholder="头像图片地址，留空则不显示"></el-input>
        </el-form-item>
        <el-form-item label="档次">
          <el-select v-model="form.tier" :disabled="!form.id" style="width: 100%">
            <el-option :value="2" label="本站主理人"></el-option>
            <el-option :value="3" label="优秀友链"></el-option>
            <el-option :value="1" label="推荐友链"></el-option>
            <el-option :value="0" label="普通友链"></el-option>
          </el-select>
          <div v-if="!form.id" class="form-tip">新增默认普通友链，保存后可在编辑中调整档次</div>
        </el-form-item>
        <el-form-item label="标签">
          <el-input maxlength="200" v-model="form.tags" placeholder="多个标签用英文逗号分隔，例如：AI,效率工具,跨境电商"></el-input>
          <div class="form-tip">前台最多展示 3 个；留空则不显示标签行</div>
        </el-form-item>
        <el-form-item label="RSS 地址">
          <el-input maxlength="500" v-model="form.rssUrl" placeholder="例如：https://example.com/atom.xml，留空则不显示最近更新"></el-input>
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="可用状态">
            <el-select v-model="form.healthStatus" style="width: 100%">
              <el-option :value="0" label="正常"></el-option>
              <el-option :value="1" label="短暂失联（探测任务维护）"></el-option>
              <el-option :value="2" label="违规友链"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="整改期限">
            <el-date-picker v-model="form.rectifyDeadline" type="datetime" style="width: 100%"
                            value-format="YYYY-MM-DD HH:mm" placeholder="违规时填写，前台显示倒计时"></el-date-picker>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveFriend()">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 推荐：只叠加徽标与动效，不改变档次分组 -->
    <el-dialog class="admin-dialog" title="设置推荐" v-model="recommendVisible" width="440px">
      <el-form :model="recommendForm" label-position="top">
        <el-form-item label="推荐文案">
          <el-input maxlength="40" v-model="recommendForm.label"
                    placeholder="留空则前台按月生成，例如「11月份推荐友链」"></el-input>
        </el-form-item>
        <p class="form-tip">
          将推荐「{{ recommendForm.title }}」。推荐只叠加徽标、扫光与标题辉光，不改变档次分组：
          该友链仍归入「{{ tierText(recommendForm.tier) }}」组。
        </p>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="recommendVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitRecommend()">确认推荐</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "FriendListView",
    data() {
      return {
        allFriends: [],
        statusFilter: 0,
        dialogVisible: false,
        form: this.emptyForm(),
        // 推荐弹窗：只改推荐标记与文案
        recommendVisible: false,
        recommendForm: {
          id: null,
          title: "",
          tier: 0,
          label: ""
        },
        // 本站友链信息：键名与 sys_config 一一对应
        siteForm: {
          friendWebName: "",
          friendIntroduction: "",
          friendUrl: "",
          friendAvatar: "",
          friendRssUrl: "",
          friendScreenshotUrl: ""
        },
        siteConfigIds: {
          friendWebName: null,
          friendIntroduction: null,
          friendUrl: null,
          friendAvatar: null,
          friendRssUrl: null,
          friendScreenshotUrl: null
        }
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
      this.getSiteInfo();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          title: "",
          introduction: "",
          cover: "",
          avatar: "",
          url: "",
          tier: 0,
          status: 1,
          tags: "",
          rssUrl: "",
          healthStatus: 0,
          rectifyDeadline: null,
          recommended: 0,
          recommendLabel: ""
        }
      },
      healthText(status) {
        if (status === 1) {
          return "短暂失联";
        }
        return status === 2 ? "违规" : "正常";
      },
      healthTagType(status) {
        if (status === 1) {
          return "warning";
        }
        return status === 2 ? "danger" : "success";
      },
      latencyTagType(latency) {
        if (latency == null) {
          return "info";
        }
        if (latency < 300) {
          return "success";
        }
        return latency < 800 ? "warning" : "danger";
      },
      shortDate(time) {
        if (!time) {
          return "";
        }
        let date = new Date(time);
        if (isNaN(date.getTime())) {
          return time;
        }
        const pad = (num) => (num < 10 ? "0" + num : num);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate());
      },
      tierText(tier) {
        if (tier === 2) {
          return "本站主理人";
        }
        if (tier === 3) {
          return "优秀友链";
        }
        return tier === 1 ? "推荐友链" : "普通友链";
      },
      tierTagType(tier) {
        if (tier === 2) {
          return "warning";
        }
        if (tier === 3) {
          return "";
        }
        return tier === 1 ? "success" : "info";
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
      // 本站友链信息：读 sys_config，缺省回落到前端常量（保持原有展示不变）
      getSiteInfo() {
        this.$http.get(this.$constant.baseURL + "/sysConfig/listFriendConfig", {}, true)
          .then((res) => {
            const list = this.$common.isEmpty(res.data) ? [] : res.data;
            const map = {};
            list.forEach((item) => {
              map[item.configKey] = item;
            });
            const valueOf = (key, fallback) => {
              const config = map[key];
              return config && config.configValue ? config.configValue : fallback;
            };
            this.siteConfigIds = {
              friendWebName: map.friendWebName ? map.friendWebName.id : null,
              friendIntroduction: map.friendIntroduction ? map.friendIntroduction.id : null,
              friendUrl: map.friendUrl ? map.friendUrl.id : null,
              friendAvatar: map.friendAvatar ? map.friendAvatar.id : null,
              friendRssUrl: map.friendRssUrl ? map.friendRssUrl.id : null,
              friendScreenshotUrl: map.friendScreenshotUrl ? map.friendScreenshotUrl.id : null
            };
            this.siteForm = {
              friendWebName: valueOf("friendWebName", this.$constant.friendWebName),
              friendIntroduction: valueOf("friendIntroduction", this.$constant.friendIntroduction),
              friendUrl: valueOf("friendUrl", this.$constant.friendUrl),
              friendAvatar: valueOf("friendAvatar", this.$constant.friendAvatar),
              // 新增的两个地址没有默认值，未配置时为空
              friendRssUrl: valueOf("friendRssUrl", ""),
              friendScreenshotUrl: valueOf("friendScreenshotUrl", "")
            };
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      saveSiteInfo() {
        const items = [
          ["friendWebName", "友链站点名称", this.siteForm.friendWebName],
          ["friendIntroduction", "友链站点描述", this.siteForm.friendIntroduction],
          ["friendUrl", "友链站点链接", this.siteForm.friendUrl],
          ["friendAvatar", "友链头像链接", this.siteForm.friendAvatar],
          ["friendRssUrl", "友链 RSS 地址", this.siteForm.friendRssUrl],
          ["friendScreenshotUrl", "友链站点主页截图地址", this.siteForm.friendScreenshotUrl]
        ];
        const tasks = items.map((item) => this.$http.post(this.$constant.baseURL + "/sysConfig/saveFriendConfig", {
          id: this.siteConfigIds[item[0]] || null,
          configName: item[1],
          configKey: item[0],
          configValue: item[2] == null ? "" : String(item[2]).trim(),
          // 2 = 公开配置，前台通过 /sysConfig/listSysConfig 读取
          configType: "2"
        }, true));
        Promise.all(tasks).then(() => {
          this.getSiteInfo();
          this.refreshStoreSysConfig();
          this.$message({
            message: "保存成功！",
            type: "success"
          });
        }).catch((error) => {
          this.$message({
            message: error.message,
            type: "error"
          });
        });
      },
      // 同步到 Vuex，前台友链页无需刷新即可拿到最新配置
      refreshStoreSysConfig() {
        this.$http.get(this.$constant.baseURL + "/sysConfig/listSysConfig")
          .then((res) => {
            this.$store.commit("loadSysConfig", this.$common.isEmpty(res.data) ? {} : res.data);
          })
          .catch(() => {
          });
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
      openRecommend(friend) {
        this.recommendForm = {
          id: friend.id,
          title: friend.title,
          tier: friend.tier,
          label: friend.recommendLabel || ""
        };
        this.recommendVisible = true;
      },
      submitRecommend() {
        this.$http.get(this.$constant.baseURL + "/webInfo/recommendFriend",
          {id: this.recommendForm.id, recommended: 1, label: this.recommendForm.label || ""}, true)
          .then(() => {
            this.recommendVisible = false;
            this.getFriends();
            this.$message({
              message: "已推荐「" + this.recommendForm.title + "」！",
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
      cancelRecommend(friend) {
        this.$confirm('确认取消推荐「' + friend.title + '」？', '取消推荐', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/webInfo/recommendFriend",
            {id: friend.id, recommended: 0}, true)
            .then(() => {
              this.getFriends();
              this.$message({
                message: "已取消推荐！",
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
      openEdit(friend) {
        this.form = Object.assign(this.emptyForm(), friend);
        this.dialogVisible = true;
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
  .site-info-card {
    margin-bottom: 18px;
    padding: 16px 18px 8px;
    border: 1px solid var(--admin-border);
    border-radius: 12px;
    background: var(--admin-subtle);
  }

  .site-info-head h3 {
    margin: 0 0 4px;
    font-size: 15px;
    font-weight: 600;
    color: var(--admin-text);
  }

  .site-info-head p {
    margin: 0 0 14px;
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .site-info-form :deep(.el-form-item__label) {
    padding-bottom: 6px;
    font-size: 13px;
    font-weight: 500;
    color: var(--admin-text-soft);
  }

  .site-info-foot {
    display: flex;
    justify-content: flex-end;
    padding-bottom: 8px;
  }

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

  .form-tip {
    margin-top: 6px;
    font-size: 12px;
    line-height: 1.4;
    color: var(--admin-text-secondary);
  }

  .link-btn {
    display: inline-block;
    max-width: 100%;
    font-size: 13px;
    color: var(--admin-primary, #4c6fff);
    text-decoration: none;
    /* 长地址在列内截断，悬停title可看完整地址 */
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: middle;
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

  /* 操作列：通过/拒绝改为真正的按钮，与编辑/删除共用同一套尺寸 */
  .op-cell {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .op-cell .op-btn {
    height: 28px;
    margin-left: 0;
    padding: 0 12px;
    border-radius: 8px;
    font-size: 13px;
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
