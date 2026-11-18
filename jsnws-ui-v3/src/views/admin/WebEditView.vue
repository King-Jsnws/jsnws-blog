<template>
  <div class="settings-page">
    <!-- 基础信息 -->
    <section class="settings-card">
      <div class="settings-card-head">
        <h2>基础信息</h2>
        <p>站点名称、标题、页脚以及品牌素材。</p>
      </div>

      <el-form :model="webInfo" :rules="rules" ref="ruleForm" label-position="top" class="settings-form">
        <div class="form-grid">
          <el-form-item label="网站名称" prop="webName">
            <el-input v-model="webInfo.webName" placeholder="请输入网站名称"></el-input>
          </el-form-item>

          <el-form-item label="网站标题" prop="webTitle">
            <el-input v-model="webInfo.webTitle" placeholder="请输入网站标题"></el-input>
          </el-form-item>

          <el-form-item label="标签页标题" prop="tabTitle">
            <div class="field-stack">
              <el-input v-model="webInfo.tabTitle" maxlength="50" show-word-limit
                        placeholder="留空则使用网站名称"></el-input>
              <div class="switch-tip">浏览器标签页上显示的文字，与网站名称分开配置；留空时自动回退为网站名称。</div>
            </div>
          </el-form-item>

          <el-form-item label="网站状态" prop="status">
            <div class="switch-row">
              <el-switch @click="changeWebStatus(webInfo)" v-model="webInfo.status"></el-switch>
              <span class="switch-tip">{{ webInfo.status ? "站点已开启" : "站点已关闭" }}</span>
            </div>
          </el-form-item>

          <el-form-item label="首页诗句">
            <div class="switch-row">
              <el-switch v-model="printerEnabled" @change="changePrinterEnabled"></el-switch>
              <span class="switch-tip">{{ printerEnabled ? "显示打字机诗句" : "已隐藏" }}</span>
            </div>
          </el-form-item>

          <el-form-item label="首屏标题">
            <div class="switch-row">
              <el-switch v-model="titleEnabled" @change="changeTitleEnabled"></el-switch>
              <span class="switch-tip">{{ titleEnabled ? "显示首屏标题" : "已隐藏" }}</span>
            </div>
          </el-form-item>

          <el-form-item label="头像呼吸光环">
            <div class="switch-row">
              <el-switch v-model="avatarGlowEnabled" @change="changeAvatarGlowEnabled"></el-switch>
              <span class="switch-tip">
                {{ avatarGlowEnabled ? "已开启：头像呼吸光环 + 悬停旋转放大" : "已关闭：头像保持静态" }}
              </span>
            </div>
          </el-form-item>
        </div>

        <el-form-item label="站点描述" prop="description">
          <el-input v-model="webInfo.description" type="textarea" :rows="3" maxlength="200" show-word-limit
                    placeholder="展示在侧栏个人信息卡片的站点描述"></el-input>
        </el-form-item>

        <h3 class="form-subtitle">品牌素材</h3>
        <div class="upload-grid">
          <el-form-item label="背景图" prop="backgroundImage">
            <div class="media-row">
              <el-input v-model="webInfo.backgroundImage" placeholder="背景图地址"></el-input>
              <el-image lazy class="media-thumb"
                        :preview-src-list="[webInfo.backgroundImage]"
                        :src="webInfo.backgroundImage"
                        fit="cover"></el-image>
            </div>
            <UploadPicture :isAdmin="true" :prefix="'webBackgroundImage'" class="media-upload"
                           @addPicture="addBackgroundImage"
                           :maxSize="3"
                           :maxNumber="1"></UploadPicture>
          </el-form-item>

          <el-form-item label="头像" prop="avatar">
            <div class="media-row">
              <el-input v-model="webInfo.avatar" placeholder="头像图片地址"></el-input>
              <el-image lazy class="media-thumb"
                        :preview-src-list="[webInfo.avatar]"
                        :src="webInfo.avatar"
                        fit="cover"></el-image>
            </div>
            <UploadPicture :isAdmin="true" :prefix="'webAvatar'" class="media-upload" @addPicture="addAvatar"
                           :maxSize="2"
                           :maxNumber="1"></UploadPicture>
          </el-form-item>

          <!-- 站点 LOGO：顶栏独立展示，与头像分开存储；留空时顶栏回退文字站名 -->
          <el-form-item label="站点 LOGO" prop="logo">
            <div class="media-row">
              <el-input v-model="webInfo.logo" placeholder="顶栏 LOGO 图片地址，留空则显示文字站名"></el-input>
              <el-image lazy class="media-thumb"
                        :preview-src-list="[webInfo.logo]"
                        :src="webInfo.logo"
                        fit="contain"></el-image>
            </div>
            <UploadPicture :isAdmin="true" :prefix="'webLogo'" class="media-upload" @addPicture="addLogo"
                           :maxSize="2"
                           :maxNumber="1"></UploadPicture>
          </el-form-item>
        </div>

        <h3 class="form-subtitle">联系方式</h3>
        <div class="form-grid">
          <!-- 微信联系方式：只作用于侧栏社交入口弹出的二维码，与打赏收款码互不影响 -->
          <el-form-item label="微信二维码" prop="wechat">
            <el-input v-model="webInfo.wechat" placeholder="微信二维码图片地址，留空则不显示"></el-input>
          </el-form-item>

          <el-form-item label="QQ" prop="qq">
            <el-input v-model="webInfo.qq" placeholder="QQ 号、QQ 群链接或其他链接，留空则不显示"></el-input>
          </el-form-item>

          <el-form-item label="GitHub" prop="github">
            <el-input v-model="webInfo.github" placeholder="https://github.com/xxx，留空则不显示"></el-input>
          </el-form-item>

          <el-form-item label="Gitee" prop="gitee">
            <el-input v-model="webInfo.gitee" placeholder="https://gitee.com/xxx，留空则不显示"></el-input>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="webInfo.email" placeholder="邮箱地址，留空则不显示"></el-input>
          </el-form-item>

          <el-form-item label="Bilibili" prop="bilibili">
            <el-input v-model="webInfo.bilibili" placeholder="https://space.bilibili.com/xxx，留空则不显示"></el-input>
          </el-form-item>
        </div>
      </el-form>

      <div class="settings-actions">
        <el-button type="primary" @click="submitForm('ruleForm')">保存基本信息</el-button>
      </div>
    </section>

    <!-- 打赏 -->
    <section class="settings-card">
      <div class="settings-card-head">
        <h2>打赏</h2>
        <p>站点打赏入口使用的收款码，支持微信与支付宝。</p>
      </div>

      <el-form :model="webInfo" label-position="top" class="settings-form">
        <div class="form-grid">
          <el-form-item label="微信收款码">
            <div class="media-row">
              <el-input v-model="webInfo.wechatReward" placeholder="微信收款码图片地址，留空则不显示"></el-input>
              <el-image v-if="webInfo.wechatReward" lazy class="media-thumb"
                        :preview-src-list="[webInfo.wechatReward]"
                        :src="webInfo.wechatReward"
                        fit="cover"></el-image>
            </div>
          </el-form-item>

          <el-form-item label="支付宝收款码">
            <div class="media-row">
              <el-input v-model="webInfo.alipay" placeholder="支付宝收款码图片地址，留空则不显示"></el-input>
              <el-image v-if="webInfo.alipay" lazy class="media-thumb"
                        :preview-src-list="[webInfo.alipay]"
                        :src="webInfo.alipay"
                        fit="cover"></el-image>
            </div>
          </el-form-item>
        </div>
      </el-form>

      <div class="settings-actions">
        <el-button type="primary" @click="saveReward()">保存打赏信息</el-button>
      </div>
    </section>

    <!-- 公告 -->
    <section class="settings-card">
      <div class="settings-card-head">
        <h2>公告</h2>
        <p>展示在站点侧栏的公告内容。</p>
      </div>
      <div class="tag-editor">
        <el-tag :key="i"
                v-for="(notice, i) in notices"
                class="item-tag"
                closable
                :disable-transitions="false"
                @close="handleClose(notices, notice)">
          {{notice}}
        </el-tag>
        <el-input class="tag-input"
                  v-if="inputNoticeVisible"
                  v-model="inputNoticeValue"
                  ref="saveNoticeInput"
                  @keyup.enter="handleInputNoticeConfirm"
                  @blur="handleInputNoticeConfirm">
        </el-input>
        <el-button v-else class="tag-add" size="small" @click="showNoticeInput()">+ 公告</el-button>
      </div>
      <div class="settings-actions">
        <el-button type="primary" @click="saveNotice()">保存公告</el-button>
      </div>
    </section>

    <!-- 随机头像 -->
    <section class="settings-card">
      <div class="settings-card-head">
        <h2>随机头像</h2>
        <p>用于游客随机展示的头像图片。</p>
      </div>
      <div v-if="randomAvatar.length" class="media-list">
        <div :key="i" v-for="(avatar, i) in randomAvatar" class="media-item">
          <el-image lazy class="media-thumb"
                    :preview-src-list="[avatar]"
                    :src="avatar"
                    fit="cover"></el-image>
          <span class="media-url" :title="avatar">{{avatar}}</span>
          <button type="button" class="media-remove" @click="handleClose(randomAvatar, avatar)">删除</button>
        </div>
      </div>
      <div class="tag-editor">
        <el-input class="tag-input"
                  v-if="inputRandomAvatarVisible"
                  v-model="inputRandomAvatarValue"
                  ref="saveRandomAvatarInput"
                  @keyup.enter="handleInputRandomAvatarConfirm"
                  @blur="handleInputRandomAvatarConfirm">
        </el-input>
        <el-button v-else class="tag-add" size="small" @click="showRandomAvatarInput">+ 随机头像</el-button>
      </div>
      <UploadPicture :isAdmin="true" :prefix="'randomAvatar'" class="media-upload media-upload-block"
                     @addPicture="addRandomAvatar"
                     :maxSize="1"
                     :maxNumber="5"></UploadPicture>
      <div class="settings-actions">
        <el-button type="primary" @click="saveRandomAvatar()">保存随机头像</el-button>
      </div>
    </section>

    <!-- 网站灰度 -->
    <section class="settings-card">
      <div class="settings-card-head">
        <h2>网站灰度</h2>
        <p>到达设定日期后首页自动转为灰度，用于哀悼日等场景；其他页面和后台不受影响。</p>
      </div>
      <div class="form-grid">
        <el-form-item label="灰度开关">
          <div class="switch-row">
            <el-switch v-model="grayEnabled"></el-switch>
            <span class="switch-tip">{{ grayEnabled ? "已启用灰度" : "已关闭" }}</span>
          </div>
        </el-form-item>
        <el-form-item label="灰度日期">
          <div class="gray-date-editor">
            <el-date-picker v-model="newGrayDate"
                            type="date"
                            format="YYYY-MM-DD"
                            value-format="YYYY-MM-DD"
                            placeholder="选择日期后点添加"></el-date-picker>
            <el-button @click="addGrayDate()">添加日期</el-button>
          </div>
        </el-form-item>
      </div>
      <div v-if="grayDates.length" class="gray-date-list">
        <el-tag v-for="(date, index) in grayDates"
                :key="date"
                closable
                @close="removeGrayDate(index)">{{ date }}</el-tag>
      </div>
      <div class="switch-tip">日期只记录「月-日」，每年自动生效：当天 0 点首页变灰，次日恢复；未添加日期时开启开关会立即变灰。</div>
      <div class="settings-actions">
        <el-button type="primary" @click="saveGrayMode()">保存灰度设置</el-button>
      </div>
    </section>

    <!-- 随机封面 -->
    <section class="settings-card">
      <div class="settings-card-head">
        <h2>随机封面</h2>
        <p>用于文章随机展示的封面图片。</p>
      </div>
      <div v-if="randomCover.length" class="media-list">
        <div :key="i" v-for="(cover, i) in randomCover" class="media-item">
          <el-image lazy class="media-thumb"
                    :preview-src-list="[cover]"
                    :src="cover"
                    fit="cover"></el-image>
          <span class="media-url" :title="cover">{{cover}}</span>
          <button type="button" class="media-remove" @click="handleClose(randomCover, cover)">删除</button>
        </div>
      </div>
      <div class="tag-editor">
        <el-input class="tag-input"
                  v-if="inputRandomCoverVisible"
                  v-model="inputRandomCoverValue"
                  ref="saveRandomCoverInput"
                  @keyup.enter="handleInputRandomCoverConfirm"
                  @blur="handleInputRandomCoverConfirm">
        </el-input>
        <el-button v-else class="tag-add" size="small" @click="showRandomCoverInput">+ 随机封面</el-button>
      </div>
      <UploadPicture :isAdmin="true" :prefix="'randomCover'" class="media-upload media-upload-block"
                     @addPicture="addRandomCover"
                     :maxSize="2"
                     :maxNumber="5"></UploadPicture>
      <div class="settings-actions">
        <el-button type="primary" @click="saveRandomCover()">保存随机封面</el-button>
      </div>
    </section>

    <!-- 底部信息 -->
    <section class="settings-card">
      <div class="settings-card-head">
        <h2>底部信息</h2>
        <p>管理前台页脚展示的站点描述与备案信息。</p>
      </div>

      <el-form :model="webInfo" label-position="top" class="settings-form">
        <el-form-item label="页脚文字">
          <el-input v-model="webInfo.footer" type="textarea" :rows="3" maxlength="200" show-word-limit
                    placeholder="例如：一个热爱技术与生活的个人博客"></el-input>
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="备案号">
            <el-input v-model="beian" maxlength="60" placeholder="例如：桂ICP备2026000000号，留空则不显示"></el-input>
          </el-form-item>
          <el-form-item label="备案链接">
            <el-input v-model="beianUrl" maxlength="200" placeholder="https://beian.miit.gov.cn/"></el-input>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="公安备案号">
            <el-input v-model="policeBeian" maxlength="60" placeholder="例如：桂公网安备 45010302000000号，留空则不显示"></el-input>
          </el-form-item>
          <el-form-item label="公安备案链接">
            <el-input v-model="policeBeianUrl" maxlength="200" placeholder="https://beian.mps.gov.cn/"></el-input>
          </el-form-item>
        </div>
      </el-form>

      <div class="settings-actions">
        <el-button type="primary" @click="saveFooterInfo()">保存底部信息</el-button>
      </div>
    </section>

    <div class="settings-footer">
      <el-button type="danger" plain @click="resetForm('ruleForm')">重置所有修改</el-button>
    </div>
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
        disabled: true,
        types: ['', 'success', 'info', 'danger', 'warning'],
        inputNoticeVisible: false,
        inputNoticeValue: "",
        inputRandomAvatarVisible: false,
        inputRandomAvatarValue: "",
        inputRandomCoverVisible: false,
        inputRandomCoverValue: "",
        webInfo: {
          id: null,
          webName: "",
          webTitle: "",
          tabTitle: "",
          description: "",
          footer: "",
          backgroundImage: "",
          avatar: "",
          logo: "",
          wechat: "",
          wechatReward: "",
          alipay: "",
          qq: "",
          github: "",
          gitee: "",
          email: "",
          bilibili: "",
          status: false
        },
        beian: "",
        beianUrl: "https://beian.miit.gov.cn/",
        beianConfigId: null,
        beianUrlConfigId: null,
        printerEnabled: true,
        printerConfigId: null,
        titleEnabled: true,
        titleConfigId: null,
        avatarGlowEnabled: false,
        avatarGlowConfigId: null,
        grayEnabled: false,
        grayDates: [],
        newGrayDate: "",
        grayConfigId: null,
        policeBeian: "",
        policeBeianUrl: "https://beian.mps.gov.cn/",
        policeBeianConfigId: null,
        policeBeianUrlConfigId: null,
        notices: [],
        randomAvatar: [],
        randomCover: [],
        rules: {
          webName: [
            {required: true, message: '请输入网站名称', trigger: 'blur'},
            {min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'change'}
          ],
          webTitle: [
            {required: true, message: '请输入网站标题', trigger: 'blur'}
          ],
          tabTitle: [
            {max: 50, message: '标签页标题不能超过 50 个字符', trigger: 'change'}
          ],
          backgroundImage: [
            {required: true, message: '请输入背景', trigger: 'change'}
          ],
          status: [
            {required: true, message: '请设置网站状态', trigger: 'change'}
          ],
          avatar: [
            {required: true, message: '请上传头像', trigger: 'change'}
          ]
        }
      }
    },

    computed: {},

    watch: {},

    created() {
      this.getWebInfo();
      this.getSysConfig();
    },

    mounted() {

    },

    methods: {
      addBackgroundImage(res) {
        this.webInfo.backgroundImage = res;
      },
      addAvatar(res) {
        this.webInfo.avatar = res;
      },
      addLogo(res) {
        this.webInfo.logo = res;
      },
      addRandomAvatar(res) {
        this.randomAvatar.push(res);
      },
      addRandomCover(res) {
        this.randomCover.push(res);
      },
      changeWebStatus(webInfo) {
        this.$http.post(this.$constant.baseURL + "/webInfo/updateWebInfo", {
          id: webInfo.id,
          status: webInfo.status
        }, true)
          .then((res) => {
            this.getWebInfo();
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
      changePrinterEnabled(val) {
        this.$http.post(this.$constant.baseURL + "/sysConfig/saveOrUpdateConfig", {
          id: this.printerConfigId,
          configName: "首页诗句",
          configKey: "printerEnabled",
          configValue: val ? "1" : "0",
          configType: "2"
        }, true)
          .then(() => {
            this.getSysConfig();
            this.refreshStoreSysConfig();
            this.$message({
              message: "保存成功！",
              type: "success"
            });
          })
          .catch((error) => {
            this.printerEnabled = !val;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      changeTitleEnabled(val) {
        this.$http.post(this.$constant.baseURL + "/sysConfig/saveOrUpdateConfig", {
          id: this.titleConfigId,
          configName: "首屏标题",
          configKey: "titleEnabled",
          configValue: val ? "1" : "0",
          configType: "2"
        }, true)
          .then(() => {
            this.getSysConfig();
            this.refreshStoreSysConfig();
            this.$message({
              message: "保存成功！",
              type: "success"
            });
          })
          .catch((error) => {
            this.titleEnabled = !val;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      changeAvatarGlowEnabled(val) {
        this.$http.post(this.$constant.baseURL + "/sysConfig/saveOrUpdateConfig", {
          id: this.avatarGlowConfigId,
          configName: "头像呼吸光环",
          configKey: "avatarGlowEnabled",
          configValue: val ? "1" : "0",
          configType: "2"
        }, true)
          .then(() => {
            this.getSysConfig();
            this.refreshStoreSysConfig();
            this.$message({
              message: "保存成功！",
              type: "success"
            });
          })
          .catch((error) => {
            this.avatarGlowEnabled = !val;
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      addGrayDate() {
        if (!this.newGrayDate) {
          this.$message({
            message: "请先选择日期",
            type: "warning"
          });
          return;
        }
        // 只保留月-日，配置的日期每年都会生效
        const monthDay = String(this.newGrayDate).slice(5);
        if (this.grayDates.indexOf(monthDay) > -1) {
          this.$message({
            message: "该日期已添加",
            type: "info"
          });
          return;
        }
        this.grayDates.push(monthDay);
        this.grayDates.sort();
        this.newGrayDate = "";
      },
      removeGrayDate(index) {
        this.grayDates.splice(index, 1);
      },
      saveGrayMode() {
        this.$http.post(this.$constant.baseURL + "/sysConfig/saveOrUpdateConfig", {
          id: this.grayConfigId,
          configName: "网站灰度",
          configKey: "grayMode",
          configValue: JSON.stringify({
            enabled: this.grayEnabled,
            dates: this.grayDates
          }),
          configType: "2"
        }, true)
          .then(() => {
            this.getSysConfig();
            this.refreshStoreSysConfig();
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
      getWebInfo() {        this.$http.get(this.$constant.baseURL + "/admin/webInfo/getAdminWebInfo", {}, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.webInfo.id = res.data.id;
              this.webInfo.webName = res.data.webName;
              this.webInfo.webTitle = res.data.webTitle;
              this.webInfo.tabTitle = res.data.tabTitle || "";
              this.webInfo.description = res.data.description;
              this.webInfo.footer = res.data.footer;
              this.webInfo.backgroundImage = res.data.backgroundImage;
              this.webInfo.avatar = res.data.avatar;
              this.webInfo.logo = res.data.logo || "";
              this.webInfo.wechat = res.data.wechat;
              this.webInfo.wechatReward = res.data.wechatReward;
              this.webInfo.alipay = res.data.alipay;
              this.webInfo.qq = res.data.qq;
              this.webInfo.github = res.data.github;
              this.webInfo.gitee = res.data.gitee;
              this.webInfo.email = res.data.email;
              this.webInfo.bilibili = res.data.bilibili;
              this.webInfo.status = res.data.status;
              this.notices = JSON.parse(res.data.notices);
              this.randomAvatar = JSON.parse(res.data.randomAvatar);
              this.randomCover = JSON.parse(res.data.randomCover);
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      getSysConfig() {
        this.$http.get(this.$constant.baseURL + "/sysConfig/listConfig", {}, true)
          .then((res) => {
            const list = this.$common.isEmpty(res.data) ? [] : res.data;
            const beian = list.find((item) => item.configKey === "beian");
            const beianUrl = list.find((item) => item.configKey === "beianUrl");
            this.beianConfigId = beian ? beian.id : null;
            this.beian = beian ? (beian.configValue || "") : "";
            this.beianUrlConfigId = beianUrl ? beianUrl.id : null;
            this.beianUrl = beianUrl && beianUrl.configValue ? beianUrl.configValue : "https://beian.miit.gov.cn/";
            const policeBeian = list.find((item) => item.configKey === "policeBeian");
            const policeBeianUrl = list.find((item) => item.configKey === "policeBeianUrl");
            this.policeBeianConfigId = policeBeian ? policeBeian.id : null;
            this.policeBeian = policeBeian ? (policeBeian.configValue || "") : "";
            this.policeBeianUrlConfigId = policeBeianUrl ? policeBeianUrl.id : null;
            this.policeBeianUrl = policeBeianUrl && policeBeianUrl.configValue
              ? policeBeianUrl.configValue : "https://beian.mps.gov.cn/";
            const printer = list.find((item) => item.configKey === "printerEnabled");
            this.printerConfigId = printer ? printer.id : null;
            this.printerEnabled = !printer || printer.configValue !== "0";
            const title = list.find((item) => item.configKey === "titleEnabled");
            this.titleConfigId = title ? title.id : null;
            this.titleEnabled = !title || title.configValue !== "0";
            // 头像呼吸光环：默认关闭，仅显式配置为 1/true 时开启
            const avatarGlow = list.find((item) => item.configKey === "avatarGlowEnabled");
            this.avatarGlowConfigId = avatarGlow ? avatarGlow.id : null;
            this.avatarGlowEnabled = !!avatarGlow
              && (avatarGlow.configValue === "1" || avatarGlow.configValue === "true");
            const gray = list.find((item) => item.configKey === "grayMode");
            this.grayConfigId = gray ? gray.id : null;
            let grayConfig = null;
            if (gray && gray.configValue) {
              try {
                grayConfig = JSON.parse(gray.configValue);
              } catch (e) {
                grayConfig = null;
              }
            }
            this.grayEnabled = !!(grayConfig && grayConfig.enabled);
            if (grayConfig && Array.isArray(grayConfig.dates)) {
              this.grayDates = grayConfig.dates.map((date) => String(date).slice(-5)).sort();
            } else if (grayConfig && grayConfig.startTime) {
              // 兼容旧版单日期配置
              this.grayDates = [String(grayConfig.startTime).slice(-5)];
            } else {
              this.grayDates = [];
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      saveFooterInfo() {
        this.$confirm('确认保存底部信息？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          let tasks = [this.$http.post(this.$constant.baseURL + "/webInfo/updateWebInfo", {
            id: this.webInfo.id,
            footer: this.webInfo.footer
          }, true)];
          let beianTask = this.saveSysConfig("beian", "备案信息", this.beianConfigId, this.beian);
          if (beianTask) {
            tasks.push(beianTask);
          }
          let beianUrlTask = this.saveSysConfig("beianUrl", "备案链接", this.beianUrlConfigId, this.beianUrl);
          if (beianUrlTask) {
            tasks.push(beianUrlTask);
          }
          let policeBeianTask = this.saveSysConfig("policeBeian", "公安备案信息", this.policeBeianConfigId, this.policeBeian);
          if (policeBeianTask) {
            tasks.push(policeBeianTask);
          }
          let policeBeianUrlTask = this.saveSysConfig("policeBeianUrl", "公安备案链接", this.policeBeianUrlConfigId, this.policeBeianUrl);
          if (policeBeianUrlTask) {
            tasks.push(policeBeianUrlTask);
          }
          Promise.all(tasks).then(() => {
            this.getWebInfo();
            this.getSysConfig();
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
        }).catch(() => {
          this.$message({
            type: 'success',
            message: '已取消保存!'
          });
        });
      },
      saveSysConfig(key, name, id, value) {
        if (!id && !value) {
          return null;
        }
        return this.$http.post(this.$constant.baseURL + "/sysConfig/saveOrUpdateConfig", {
          id: id,
          configName: name,
          configKey: key,
          configValue: value,
          configType: "2"
        }, true);
      },
      refreshStoreSysConfig() {
        this.$http.get(this.$constant.baseURL + "/sysConfig/listSysConfig")
          .then((res) => {
            this.$store.commit("loadSysConfig", this.$common.isEmpty(res.data) ? {} : res.data);
          });
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.updateWebInfo(this.webInfo)
          } else {
            this.$message({
              message: "请完善必填项！",
              type: "error"
            });
          }
        });
      },
      resetForm(formName) {
        this.$refs[formName].resetFields();
        this.getWebInfo();
      },
      handleClose(array, item) {
        array.splice(array.indexOf(item), 1);
      },
      handleInputNoticeConfirm() {
        if (this.inputNoticeValue) {
          this.notices.push(this.inputNoticeValue);
        }
        this.inputNoticeVisible = false;
        this.inputNoticeValue = '';
      },
      showNoticeInput() {
        this.inputNoticeVisible = true;
        this.$nextTick(() => {
          // Vue 3 中组件实例通过 expose 暴露方法，直接调用 focus
          this.$refs.saveNoticeInput.focus();
        });
      },
      saveNotice() {
        let param = {
          id: this.webInfo.id,
          notices: JSON.stringify(this.notices)
        }
        this.updateWebInfo(param);
      },
      handleInputRandomAvatarConfirm() {
        if (this.inputRandomAvatarValue) {
          this.randomAvatar.push(this.inputRandomAvatarValue);
        }
        this.inputRandomAvatarVisible = false;
        this.inputRandomAvatarValue = '';
      },
      showRandomAvatarInput() {
        this.inputRandomAvatarVisible = true;
        this.$nextTick(() => {
          this.$refs.saveRandomAvatarInput.focus();
        });
      },
      saveRandomAvatar() {
        let param = {
          id: this.webInfo.id,
          randomAvatar: JSON.stringify(this.randomAvatar)
        }
        this.updateWebInfo(param);
      },
      handleInputRandomCoverConfirm() {
        if (this.inputRandomCoverValue) {
          this.randomCover.push(this.inputRandomCoverValue);
        }
        this.inputRandomCoverVisible = false;
        this.inputRandomCoverValue = '';
      },
      showRandomCoverInput() {
        this.inputRandomCoverVisible = true;
        this.$nextTick(() => {
          this.$refs.saveRandomCoverInput.focus();
        });
      },
      saveRandomCover() {
        let param = {
          id: this.webInfo.id,
          randomCover: JSON.stringify(this.randomCover)
        }
        this.updateWebInfo(param);
      },
      saveReward() {
        this.updateWebInfo({
          id: this.webInfo.id,
          wechatReward: this.webInfo.wechatReward,
          alipay: this.webInfo.alipay
        });
      },
      updateWebInfo(value) {
        this.$confirm('确认保存？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.post(this.$constant.baseURL + "/webInfo/updateWebInfo", value, true)
            .then((res) => {
              this.getWebInfo();
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
        }).catch(() => {
          this.$message({
            type: 'success',
            message: '已取消保存!'
          });
        });
      }
    }
  }
</script>

<style scoped>

  .settings-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .settings-card {
    padding: 22px 24px;
    border-radius: 14px;
    border: 1px solid var(--admin-border);
    background: var(--admin-card);
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
  }

  .settings-card-head {
    margin-bottom: 18px;
  }

  .settings-card-head h2 {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: var(--admin-text);
  }

  .settings-card-head h2::before {
    content: "";
    display: inline-block;
    width: 3px;
    height: 14px;
    margin-right: 8px;
    border-radius: 2px;
    background: #4c6fff;
    vertical-align: -2px;
  }

  .settings-card-head p {
    margin: 6px 0 0;
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .form-grid,
  .upload-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 24px;
  }

  .settings-form :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  .settings-form :deep(.el-form-item__label) {
    padding-bottom: 6px;
    font-size: 13px;
    font-weight: 500;
    line-height: 1.4;
    color: var(--admin-text-soft);
  }

  .form-subtitle {
    margin: 6px 0 16px;
    padding-top: 16px;
    border-top: 1px dashed var(--admin-border);
    font-size: 13px;
    font-weight: 600;
    color: var(--admin-text-soft);
  }

  .switch-row {
    display: flex;
    align-items: center;
    gap: 10px;
    min-height: 32px;
  }

  .switch-tip {
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .field-stack {
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;
  }

  .media-row {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
  }

  .media-row .el-input {
    flex: 1;
  }

  .media-thumb {
    flex-shrink: 0;
    width: 56px;
    height: 56px;
    border-radius: 10px;
    border: 1px solid var(--admin-border);
    background: var(--admin-subtle);
  }

  .media-upload {
    display: block;
    width: 100%;
    margin-top: 12px;
  }

  .media-upload-block {
    margin-top: 0;
    margin-bottom: 18px;
  }

  .media-upload :deep(.el-upload) {
    width: 100%;
  }

  .media-upload :deep(.el-upload-dragger) {
    padding: 14px 16px;
    border-radius: 10px;
    border-color: var(--admin-border);
    background: var(--admin-subtle);
  }

  .media-upload :deep(.el-upload-dragger:hover) {
    border-color: #4c6fff;
  }

  .media-upload :deep(.el-upload__text) {
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .media-upload :deep(.el-upload__text svg) {
    width: 26px;
    height: 26px;
  }

  .media-upload :deep(.el-upload__tip) {
    margin-top: 6px;
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .media-upload :deep(.el-button--success) {
    --el-button-bg-color: #4c6fff;
    --el-button-border-color: #4c6fff;
    --el-button-hover-bg-color: #7b93ff;
    --el-button-hover-border-color: #7b93ff;
    --el-button-active-bg-color: #3a58d6;
    --el-button-active-border-color: #3a58d6;
  }

  .tag-editor {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
    min-height: 56px;
    margin-bottom: 18px;
    padding: 12px 14px;
    border: 1px dashed var(--admin-border);
    border-radius: 12px;
    background: var(--admin-subtle);
  }

  .item-tag {
    margin: 0;
    height: 28px;
    border-radius: 8px;
    border-color: var(--el-color-primary-light-8);
    background: var(--el-color-primary-light-9);
    color: var(--admin-primary-text);
    font-size: 12px;
  }

  .tag-input {
    flex: 1 0 100%;
    width: 100%;
    min-width: 0;
  }

  .tag-add {
    border-style: dashed;
    color: var(--admin-text-secondary);
  }

  .media-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
    margin-bottom: 14px;
  }

  .media-item {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
    padding: 8px 10px;
    border: 1px solid var(--admin-border);
    border-radius: 10px;
  }

  .media-item .media-thumb {
    width: 44px;
    height: 44px;
    border-radius: 8px;
  }

  .media-url {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 12px;
    color: var(--admin-text-secondary);
  }

  .media-remove {
    flex-shrink: 0;
    padding: 4px 8px;
    border: none;
    border-radius: 6px;
    background: transparent;
    color: #ef4444;
    font-size: 12px;
    cursor: pointer;
  }

  .media-remove:hover {
    background: var(--admin-danger-bg);
  }

  .settings-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 6px;
    padding-top: 16px;
    border-top: 1px solid var(--admin-border);
  }

  .settings-footer {
    display: flex;
    justify-content: flex-end;
    padding-bottom: 8px;
  }

  @media (max-width: 900px) {
    .form-grid,
    .upload-grid,
    .media-list {
      grid-template-columns: minmax(0, 1fr);
    }
  }

  .gray-date-editor {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
  }

  .gray-date-editor .el-date-editor {
    flex: 1;
    min-width: 0;
  }

  .gray-date-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin: 4px 0 12px;
  }

</style>
