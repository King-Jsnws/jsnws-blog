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
                      placeholder="客户端专用密码/授权码；保持 **** 则不改动"></el-input>
            <div class="form-tip">已保存的授权码只回显末四位；保持 **** 原样保存不会改动，输入新值则覆盖。</div>
          </el-form-item>
          <el-form-item label="发件人名称">
            <el-input v-model="mail.from" placeholder="例如：jsnws 的博客"></el-input>
          </el-form-item>
          <el-form-item label="加密方式">
            <el-select v-model="mail.encrypt" placeholder="请选择加密方式" style="width: 100%"
                       @change="onEncryptChange">
              <el-option label="SSL（推荐，端口 465）" value="ssl"></el-option>
              <el-option label="TLS / STARTTLS（端口 587）" value="tls"></el-option>
              <el-option label="不加密（端口 25，不推荐）" value="none"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="启用邮件服务">
            <el-switch v-model="mail.enabled" active-value="true" inactive-value="false"
                       active-text="开启" inactive-text="关闭"></el-switch>
            <div class="form-tip">关闭后验证码、找回密码等邮件功能会被拒绝；环境变量 JSNWS_MAIL_ENABLED 可作为兜底。</div>
          </el-form-item>
        </div>
      </el-form>

      <div class="mail-actions">
        <el-button type="primary" @click="saveMailConfig()">保存邮箱配置</el-button>
      </div>

      <div class="mail-test">
        <div class="mail-test-title">发送测试邮件</div>
        <div class="mail-test-desc">测试使用已保存的配置，请先保存再发送。</div>
        <div class="mail-test-row">
          <el-input v-model="testMail.to" placeholder="收件邮箱，例如：you@example.com"
                    style="max-width: 360px"></el-input>
          <el-button :loading="testMail.loading" @click="sendTestMail()">发送测试邮件</el-button>
        </div>
        <div v-if="testMail.result" class="mail-test-result"
             :class="testMail.result.success ? 'is-success' : 'is-error'">
          {{ testMail.result.message }}
        </div>
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
          encrypt: "ssl",
          enabled: "true"
        },
        testMail: {
          to: "",
          loading: false,
          result: null
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
          mailEncrypt: "encrypt",
          mailEnabled: "enabled"
        };
        const mail = {
          host: "",
          port: "465",
          username: "",
          password: "",
          from: "",
          encrypt: "ssl",
          enabled: "true"
        };
        const ids = {};
        this.configList.forEach((item) => {
          const field = map[item.configKey];
          if (field) {
            if (field === "enabled") {
              // 兼容历史值 1/0 与 true/false
              mail.enabled = (item.configValue === "true" || item.configValue === "1") ? "true" : "false";
            } else {
              mail[field] = item.configValue || mail[field];
            }
            ids[item.configKey] = item.id;
          }
        });
        this.mail = mail;
        this.mailIds = ids;
      },
      onEncryptChange(value) {
        const portMap = {ssl: "465", tls: "587", none: "25"};
        if (portMap[value]) {
          this.mail.port = portMap[value];
        }
      },
      validatePort() {
        const expected = {ssl: "465", tls: "587", none: "25"}[this.mail.encrypt];
        const port = String(this.mail.port || "").trim();
        if (!port) {
          this.$message({
            message: "请填写邮件端口！",
            type: "error"
          });
          return false;
        }
        if (expected && port !== expected) {
          this.$message({
            message: `加密方式与端口不匹配：SSL 应使用 465，TLS 应使用 587，不加密应使用 25。当前为 ${port}。`,
            type: "error"
          });
          return false;
        }
        return true;
      },
      saveMailConfig() {
        if (this.mail.port && !/^\d+$/.test(String(this.mail.port).trim())) {
          this.$message({
            message: "邮件端口必须是数字！",
            type: "error"
          });
          return;
        }
        if (!this.validatePort()) {
          return;
        }
        const items = [
          ["mailHost", "邮件服务器", this.mail.host],
          ["mailPort", "邮件端口", this.mail.port],
          ["mailUsername", "发件邮箱", this.mail.username],
          ["mailPassword", "邮箱授权码", this.mail.password],
          ["mailFrom", "发件人名称", this.mail.from],
          ["mailEncrypt", "加密方式", this.mail.encrypt],
          ["mailEnabled", "邮件服务启用", this.mail.enabled]
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
      },
      sendTestMail() {
        const to = (this.testMail.to || "").trim();
        if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(to)) {
          this.$message({
            message: "请输入正确的收件邮箱地址！",
            type: "error"
          });
          return;
        }
        this.testMail.loading = true;
        this.testMail.result = null;
        this.$http.post(this.$constant.baseURL + "/sysConfig/sendTestMail", {to: to}, true)
          .then((res) => {
            const data = res.data || {};
            this.testMail.result = {
              success: true,
              message: data.message || "测试邮件已发送，请检查收件箱（若未收到请查看垃圾箱）"
            };
          })
          .catch((error) => {
            this.testMail.result = {
              success: false,
              message: error.message || "发送失败，请检查邮箱配置"
            };
          })
          .finally(() => {
            this.testMail.loading = false;
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

  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.5;
    color: var(--admin-text-soft);
  }

  .mail-actions {
    margin-top: 8px;
  }

  .mail-test {
    max-width: 920px;
    margin-top: 18px;
    padding-top: 16px;
    border-top: 1px dashed var(--admin-border, rgba(128, 128, 128, 0.3));
  }

  .mail-test-title {
    font-size: 14px;
    font-weight: 600;
  }

  .mail-test-desc {
    margin: 4px 0 10px;
    font-size: 12px;
    color: var(--admin-text-soft);
  }

  .mail-test-row {
    display: flex;
    gap: 10px;
    align-items: center;
    flex-wrap: wrap;
  }

  .mail-test-result {
    margin-top: 10px;
    font-size: 13px;
    line-height: 1.6;
  }

  .mail-test-result.is-success {
    color: #2e9b5b;
  }

  .mail-test-result.is-error {
    color: #d9534f;
  }

  @media (max-width: 900px) {
    .form-grid {
      grid-template-columns: 1fr;
    }
  }

</style>
