<template>
  <div>
    <div class="admin-card">
      <div class="admin-page-head">
        <h2>邮箱配置</h2>
        <p>用于系统邮件通知，留空则对应项不生效。</p>
      </div>

      <el-form :model="mail" label-position="top" class="mail-form">
        <div class="form-grid">
          <el-form-item label="邮件服务器">
            <el-input v-model="mail.host" placeholder="例如：smtp.qq.com"></el-input>
          </el-form-item>
          <el-form-item label="邮件端口">
            <el-input v-model="mail.port" placeholder="例如：465"></el-input>
          </el-form-item>
          <el-form-item label="发件邮箱">
            <el-input v-model="mail.username" placeholder="用于发送邮件的邮箱地址"></el-input>
          </el-form-item>
          <el-form-item label="邮箱授权码">
            <el-input v-model="mail.password" type="password" show-password
                      placeholder="SMTP 授权码或密码"></el-input>
          </el-form-item>
          <el-form-item label="发件人名称">
            <el-input v-model="mail.from" placeholder="例如：jsnws 的博客"></el-input>
          </el-form-item>
          <el-form-item label="加密方式">
            <el-select v-model="mail.encrypt" placeholder="请选择加密方式" style="width: 100%">
              <el-option label="SSL" value="ssl"></el-option>
              <el-option label="TLS" value="tls"></el-option>
              <el-option label="不加密" value="none"></el-option>
            </el-select>
          </el-form-item>
        </div>
      </el-form>

      <div class="mail-actions">
        <el-button type="primary" @click="saveMailConfig()">保存邮箱配置</el-button>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: "EmailConfigView",

    data() {
      return {
        configList: [],
        mail: {
          host: "",
          port: "465",
          username: "",
          password: "",
          from: "",
          encrypt: "ssl"
        },
        mailIds: {}
      }
    },

    created() {
      this.getConfigInfo();
    },

    methods: {
      getConfigInfo() {
        this.$http.get(this.$constant.baseURL + "/sysConfig/listConfig", {}, true)
          .then((res) => {
            this.configList = this.$common.isEmpty(res.data) ? [] : res.data;
            this.loadMailConfig();
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      loadMailConfig() {
        const map = {
          mailHost: "host",
          mailPort: "port",
          mailUsername: "username",
          mailPassword: "password",
          mailFrom: "from",
          mailEncrypt: "encrypt"
        };
        const mail = {
          host: "",
          port: "465",
          username: "",
          password: "",
          from: "",
          encrypt: "ssl"
        };
        const ids = {};
        this.configList.forEach((item) => {
          const field = map[item.configKey];
          if (field) {
            mail[field] = item.configValue || mail[field];
            ids[item.configKey] = item.id;
          }
        });
        this.mail = mail;
        this.mailIds = ids;
      },
      saveMailConfig() {
        if (this.mail.port && !/^\d+$/.test(String(this.mail.port).trim())) {
          this.$message({
            message: "邮件端口必须是数字！",
            type: "error"
          });
          return;
        }
        const items = [
          ["mailHost", "邮件服务器", this.mail.host],
          ["mailPort", "邮件端口", this.mail.port],
          ["mailUsername", "发件邮箱", this.mail.username],
          ["mailPassword", "邮箱授权码", this.mail.password],
          ["mailFrom", "发件人名称", this.mail.from],
          ["mailEncrypt", "加密方式", this.mail.encrypt]
        ];
        const tasks = [];
        items.forEach((item) => {
          const value = item[2] == null ? "" : String(item[2]).trim();
          const id = this.mailIds[item[0]];
          if (!id && !value) {
            return;
          }
          tasks.push(this.$http.post(this.$constant.baseURL + "/sysConfig/saveOrUpdateConfig", {
            id: id || null,
            configName: item[1],
            configKey: item[0],
            configValue: value,
            configType: "1"
          }, true));
        });
        if (tasks.length === 0) {
          this.$message({
            message: "请先填写邮箱配置！",
            type: "error"
          });
          return;
        }
        Promise.all(tasks).then(() => {
          this.$message({
            message: "保存成功！",
            type: "success"
          });
          this.getConfigInfo();
        }).catch((error) => {
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

  .mail-form {
    max-width: 920px;
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 18px;
  }

  .mail-form :deep(.el-form-item__label) {
    font-size: 13px;
    color: var(--admin-text-soft);
  }

  .mail-actions {
    margin-top: 8px;
  }

  @media (max-width: 900px) {
    .form-grid {
      grid-template-columns: 1fr;
    }
  }

</style>
