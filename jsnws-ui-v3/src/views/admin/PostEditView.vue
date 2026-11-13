<template>
  <div class="admin-card post-edit-page">
    <div class="admin-page-head">
      <h2>文章信息</h2>
      <p>填写文章标题、正文与展示配置，保存后返回文章列表。</p>
    </div>
    <el-form :model="article" :rules="rules" ref="ruleForm" label-position="top"
             class="post-form">
      <el-form-item label="标题" prop="articleTitle">
        <el-input maxlength="30" v-model="article.articleTitle"></el-input>
      </el-form-item>

      <el-form-item label="视频链接" prop="videoUrl">
        <el-input maxlength="1000" v-model="article.videoUrl"></el-input>
      </el-form-item>

      <el-form-item label="内容" prop="articleContent">
        <div class="editor-shell">
          <MdEditor
            v-model="article.articleContent"
            editor-id="jsnws-article-editor"
            language="zh-CN"
            :theme="editorTheme"
            preview-theme="github"
            code-theme="atom"
            :style="{ height: '640px' }"
            :toolbars="toolbars"
            :floating-toolbars="floatingToolbars"
            :footers="footers"
            :show-code-row-number="true"
            :table-shape="[8, 6]"
            :tab-width="2"
            placeholder="开始撰写正文，支持 Markdown 语法、拖拽或粘贴图片上传"
            @onUploadImg="imgAdd"
            @onSave="handleEditorSave"/>
        </div>
        <div class="editor-tip">
          支持 Markdown、代码高亮、表格、公式与流程图；图片可拖拽或粘贴上传；Ctrl / ⌘ + S 快速保存。
        </div>
      </el-form-item>

      <el-form-item label="是否启用评论" prop="commentStatus">
        <el-tag :type="article.commentStatus === false ? 'danger' : 'success'"
                disable-transitions>
          {{article.commentStatus === false ? '否' : '是'}}
        </el-tag>
        <el-switch v-model="article.commentStatus"></el-switch>
      </el-form-item>

      <el-form-item label="是否推荐" prop="recommendStatus">
        <el-tag :type="article.recommendStatus === false ? 'danger' : 'success'"
                disable-transitions>
          {{article.recommendStatus === false ? '否' : '是'}}
        </el-tag>
        <el-switch v-model="article.recommendStatus"></el-switch>
      </el-form-item>

      <el-form-item label="是否可见" prop="viewStatus">
        <el-tag :type="article.viewStatus === false ? 'danger' : 'success'"
                disable-transitions>
          {{article.viewStatus === false ? '否' : '是'}}
        </el-tag>
        <el-switch v-model="article.viewStatus"></el-switch>
      </el-form-item>

      <el-form-item v-if="article.viewStatus === false" label="不可见时的访问密码" prop="password">
        <el-input maxlength="30" v-model="article.password"></el-input>
      </el-form-item>

      <el-form-item v-if="article.viewStatus === false" label="密码提示" prop="tips">
        <el-input maxlength="60" v-model="article.tips"></el-input>
      </el-form-item>

      <el-form-item label="封面" prop="articleCover">
        <div style="display: flex">
          <el-input v-model="article.articleCover"></el-input>
          <el-image class="table-td-thumb"
                    lazy
                    style="margin-left: 10px"
                    :preview-src-list="[article.articleCover]"
                    :src="article.articleCover"
                    fit="cover"></el-image>
        </div>
        <UploadPicture :isAdmin="true" :prefix="'articleCover'" style="margin-top: 10px" @addPicture="addArticleCover"
                       :maxSize="2"
                       :maxNumber="1"></UploadPicture>
      </el-form-item>
      <el-form-item label="分类" prop="sortId">
        <el-select v-model="article.sortId" placeholder="请选择分类">
          <el-option
            v-for="item in sorts"
            :key="item.id"
            :label="item.sortName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="标签" prop="labelId">
        <el-select v-model="article.labelId" placeholder="请选择标签">
          <el-option
            v-for="item in labelsTemp"
            :key="item.id"
            :label="item.labelName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <div class="post-actions">
      <el-button @click="resetForm('ruleForm')">重置所有修改</el-button>
      <el-button type="primary" @click="submitForm('ruleForm')">保存</el-button>
    </div>
  </div>
</template>

<script>
  import { MdEditor } from 'md-editor-v3';
  import 'md-editor-v3/lib/style.css';

  import UploadPicture from "../../components/common/UploadPicture.vue";

  export default {
    name: "PostEditView",
    components: {
      MdEditor,
      UploadPicture
    },
    computed: {
      // md-editor-v3 自带暗色主题，跟随全站主题切换
      editorTheme() {
        return this.$store.state.theme === 'dark' ? 'dark' : 'light'
      }
    },
    data() {
      return {
        id: this.$route.query.id,
        article: {
          articleTitle: "",
          articleContent: "",
          commentStatus: true,
          recommendStatus: false,
          viewStatus: true,
          password: "",
          tips: "",
          articleCover: "",
          videoUrl: "",
          sortId: null,
          labelId: null
        },
        sorts: [],
        labels: [],
        labelsTemp: [],
        toolbars: [
          "bold", "underline", "italic", "strike-through", "-",
          "title", "sub", "sup", "quote", "unordered-list", "ordered-list", "task", "-",
          "code", "code-row", "link", "image", "table", "-",
          "revoke", "next", "=", "prettier", "-",
          "pageFullscreen", "fullscreen", "preview", "preview-html", "catalog", "github", "save"
        ],
        floatingToolbars: [
          "bold", "italic", "strike-through", "title", "link", "image", "code", "table"
        ],
        footers: ["markdownTotal", "=", "scrollSwitch"],
        rules: {
          articleTitle: [
            {required: true, message: '请输入标题', trigger: 'change'}
          ],
          articleContent: [
            {required: true, message: '请输入内容', trigger: 'change'}
          ],
          commentStatus: [
            {required: true, message: '是否启用评论', trigger: 'change'}
          ],
          recommendStatus: [
            {required: true, message: '是否推荐', trigger: 'change'}
          ],
          viewStatus: [
            {required: true, message: '是否可见', trigger: 'change'}
          ],
          articleCover: [
            {required: true, message: '封面', trigger: 'change'}
          ],
          sortId: [
            {required: true, message: '分类', trigger: 'change'}
          ],
          labelId: [
            {required: true, message: '标签', trigger: 'blur'}
          ]
        }
      }
    },

    watch: {
      'article.sortId'(newVal, oldVal) {
        if (oldVal !== null) {
          this.article.labelId = null;
        }
        if (!this.$common.isEmpty(newVal) && !this.$common.isEmpty(this.labels)) {
          this.labelsTemp = this.labels.filter(l => l.sortId === newVal);
        }
      }
    },

    created() {
      this.getSortAndLabel();
    },

    methods: {
      handleEditorSave() {
        this.submitForm('ruleForm');
      },
      // md-editor-v3 的图片上传回调：旧 mavon-editor 是 (pos, file)，这里是 (files, callback)
      imgAdd(files, callback) {
        let urls = new Array(files.length);
        let finished = 0;
        files.forEach((file, index) => {
          this.uploadPicture(file, (url) => {
            urls[index] = url;
            finished++;
            if (finished === files.length) {
              callback(urls);
            }
          });
        });
      },
      uploadPicture(file, done) {
        let suffix = "";
        if (file.name.lastIndexOf('.') !== -1) {
          suffix = file.name.substring(file.name.lastIndexOf('.'));
        }
        let key = "articlePicture" + "/" + this.$store.state.currentAdmin.username.replace(/[^a-zA-Z]/g, '') + this.$store.state.currentAdmin.id + new Date().getTime() + Math.floor(Math.random() * 1000) + suffix;

        let storeType = localStorage.getItem("defaultStoreType") || "local";

        let fd = new FormData();
        fd.append("file", file);
        fd.append("originalName", file.name);
        fd.append("key", key);
        fd.append("relativePath", key);
        fd.append("type", "articlePicture");
        fd.append("storeType", storeType);

        if (storeType === "local") {
          this.saveLocal(fd, done);
        } else if (storeType === "qiniu") {
          this.saveQiniu(fd, done);
        }
      },
      saveLocal(fd, done) {
        this.$http.upload(this.$constant.baseURL + "/resource/upload", fd, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              done(res.data);
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      saveQiniu(fd, done) {
        this.$http.get(this.$constant.baseURL + "/qiniu/getUpToken", {key: fd.get("key")}, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              fd.append("token", res.data);

              this.$http.uploadQiniu(this.$store.state.sysConfig.qiniuUrl, fd)
                .then((res) => {
                  if (!this.$common.isEmpty(res.key)) {
                    let url = this.$store.state.sysConfig['qiniu.downloadUrl'] + res.key;
                    let file = fd.get("file");
                    this.$common.saveResource(this, "articlePicture", url, file.size, file.type, file.name, "qiniu", true);
                    done(url);
                  }
                })
                .catch((error) => {
                  this.$message({
                    message: error.message,
                    type: "error"
                  });
                });
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      addArticleCover(res) {
        this.article.articleCover = res;
      },
      getSortAndLabel() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listSortAndLabel")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.sorts = res.data.sorts;
              this.labels = res.data.labels;
              if (!this.$common.isEmpty(this.id)) {
                this.getArticle();
              }
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      getArticle() {
        this.$http.get(this.$constant.baseURL + "/admin/article/getArticleById", {id: this.id}, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.article = res.data;
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      submitForm(formName) {
        if (this.article.viewStatus === false && this.$common.isEmpty(this.article.password)) {
          this.$message({
            message: "文章不可见时必须输入密码！",
            type: "error"
          });
          return;
        }
        this.$refs[formName].validate((valid) => {
          if (valid) {
            if (this.$common.isEmpty(this.id)) {
              this.saveArticle(this.article, "/article/saveArticle")
            } else {
              this.article.id = this.id;
              this.saveArticle(this.article, "/article/updateArticle")
            }
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
        if (!this.$common.isEmpty(this.id)) {
          this.getArticle();
        }
      },
      saveArticle(value, url) {
        this.$confirm('确认保存？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.post(this.$constant.baseURL + url, value, true)
            .then((res) => {
              this.$message({
                message: "保存成功！",
                type: "success"
              });
              this.$router.push({path: '/postList'});
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

  .post-form :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  .editor-shell {
    width: 100%;
    border: 1px solid var(--admin-border);
    border-radius: 12px;
    overflow: hidden;
    background: var(--admin-card);
  }

  .editor-shell :deep(.md-editor) {
    border: none;
    background: var(--admin-card);
  }

  .editor-shell :deep(.md-editor-toolbar-wrapper) {
    background: var(--admin-subtle);
    border-bottom: 1px solid var(--admin-border);
  }

  .editor-shell :deep(.md-editor-input-wrapper),
  .editor-shell :deep(.md-editor-preview-wrapper) {
    padding: 12px 16px;
  }

  .editor-shell :deep(.md-editor-input) {
    font-size: 15px;
    line-height: 1.8;
    color: var(--admin-text);
  }

  .editor-shell :deep(.md-editor-footer) {
    background: var(--admin-subtle);
    border-top: 1px solid var(--admin-border);
  }

  .editor-tip {
    margin-top: 8px;
    font-size: 12px;
    line-height: 1.6;
    color: var(--admin-text-secondary);
  }

  .post-form :deep(.el-form-item__label) {
    padding-bottom: 6px;
    font-size: 13px;
    font-weight: 500;
    line-height: 1.4;
    color: var(--admin-text-soft);
  }

  .post-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 6px;
    padding-top: 16px;
    border-top: 1px dashed var(--admin-border);
  }

  .table-td-thumb {
    border-radius: 6px;
    width: 40px;
    height: 40px;
  }

  .el-switch {
    margin-left: 10px;
  }
</style>
