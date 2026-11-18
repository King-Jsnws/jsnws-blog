<template>
  <div class="admin-card">
    <div class="admin-page-head">
      <h2>更新日志</h2>
      <p>维护前台 /changelog 的更新记录，列表按日期倒序展示。</p>
    </div>

    <div class="handle-box">
      <el-input v-model="searchKey" placeholder="标题/分类/版本号" class="handle-input mrb10" clearable
                @keyup.enter="searchRecords()"></el-input>
      <el-button type="primary" icon="Search" @click="searchRecords()">搜索</el-button>
      <el-button type="primary" @click="openAdd()">新增记录</el-button>
    </div>

    <el-table :data="recordList" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center"></el-table-column>
      <el-table-column prop="version" label="版本" width="100" align="center"></el-table-column>
      <el-table-column prop="logDate" label="日期" width="115" align="center"></el-table-column>
      <el-table-column label="类型" width="105" align="center">
        <template #default="scope">
          <el-tag :type="toneTagType(scope.row.tone)" effect="light" size="small">
            {{ toneLabel(scope.row.tone) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="kind" label="分类" width="150" align="center"></el-table-column>
      <el-table-column label="标题" min-width="240">
        <template #default="scope">
          <span class="changelog-title" :title="scope.row.title">{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button type="text" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" icon="Delete" style="color: var(--orangeRed)"
                     @click="removeRecord(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination background
                     layout="total, prev, pager, next"
                     :current-page="pagination.current"
                     :page-size="pagination.size"
                     :total="pagination.total"
                     @current-change="pageRecords"></el-pagination>
    </div>

    <el-dialog class="admin-dialog"
               :title="form.id ? '编辑更新记录' : '新增更新记录'"
               v-model="dialogVisible"
               width="720px"
               :before-close="handleClose"
               destroy-on-close>
      <el-form :model="form" label-position="top" class="changelog-form">
        <div class="form-grid">
          <el-form-item label="版本号">
            <el-input maxlength="30" v-model="form.version" placeholder="如 V3.1.0"></el-input>
          </el-form-item>
          <el-form-item label="更新日期">
            <el-date-picker v-model="form.logDate" type="date" placeholder="选择更新日期"
                            value-format="YYYY-MM-DD" style="width: 100%"></el-date-picker>
          </el-form-item>
          <el-form-item label="更新类型">
            <el-select v-model="form.tone" placeholder="选择类型" style="width: 100%">
              <el-option v-for="item in toneOptions" :key="item.value" :label="item.label"
                         :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="分类文案">
          <el-input maxlength="60" v-model="form.kind" placeholder="如 功能迭代"></el-input>
        </el-form-item>
        <el-form-item label="标题">
          <el-input maxlength="200" v-model="form.title" placeholder="一句话概括本次更新"></el-input>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input maxlength="500" v-model="form.summary" type="textarea" :rows="2"
                    placeholder="展示在卡片上的简短说明"></el-input>
        </el-form-item>
        <el-form-item label="涉及内容（每行一个）">
          <el-input v-model="topicsText" type="textarea" :rows="3"
                    placeholder="如：更新日志&#10;编辑器工具栏"></el-input>
        </el-form-item>
        <el-form-item label="更新详情（每行一条）">
          <el-input v-model="detailsText" type="textarea" :rows="7"
                    placeholder="每条详情单独一行，前台按顺序编号展示"></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose()">取 消</el-button>
          <el-button type="primary" @click="saveRecord()">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  const TONE_OPTIONS = [
    {value: "major", label: "重大更新", tag: "danger"},
    {value: "feature", label: "功能迭代", tag: "primary"},
    {value: "minor", label: "优化维护", tag: "success"},
    {value: "fix", label: "问题修复", tag: "warning"},
    {value: "content", label: "内容更新", tag: "info"}
  ];

  export default {
    name: "ChangelogListView",
    data() {
      return {
        recordList: [],
        searchKey: "",
        toneOptions: TONE_OPTIONS,
        pagination: {
          current: 1,
          size: 10,
          total: 0
        },
        dialogVisible: false,
        topicsText: "",
        detailsText: "",
        form: this.emptyForm()
      }
    },
    created() {
      this.getRecords();
    },
    methods: {
      emptyForm() {
        return {
          id: null,
          version: "",
          logDate: this.today(),
          tone: "feature",
          kind: "",
          title: "",
          summary: "",
          topics: "[]",
          details: "[]"
        }
      },
      today() {
        let date = new Date();
        const pad = (num) => (num < 10 ? "0" + num : num);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate());
      },
      toneLabel(tone) {
        let item = TONE_OPTIONS.find((option) => option.value === tone);
        return item ? item.label : tone;
      },
      toneTagType(tone) {
        let item = TONE_OPTIONS.find((option) => option.value === tone);
        return item ? item.tag : "info";
      },
      parseLines(value) {
        try {
          const parsed = JSON.parse(value || "[]");
          return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
          return [];
        }
      },
      getRecords() {
        let params = {
          current: this.pagination.current,
          size: this.pagination.size,
          searchKey: this.searchKey
        };
        this.$http.post(this.$constant.baseURL + "/admin/changelog/list", params, true)
          .then((res) => {
            if (this.$common.isEmpty(res.data)) {
              this.recordList = [];
              this.pagination.total = 0;
              return;
            }
            this.recordList = res.data.records || [];
            this.pagination.total = res.data.total || 0;
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      searchRecords() {
        this.pagination.current = 1;
        this.getRecords();
      },
      pageRecords(current) {
        this.pagination.current = current;
        this.getRecords();
      },
      openAdd() {
        this.form = this.emptyForm();
        this.topicsText = "";
        this.detailsText = "";
        this.dialogVisible = true;
      },
      openEdit(record) {
        this.form = Object.assign(this.emptyForm(), record);
        this.topicsText = this.parseLines(record.topics).join("\n");
        this.detailsText = this.parseLines(record.details).join("\n");
        this.dialogVisible = true;
      },
      saveRecord() {
        if (this.$common.isEmpty(this.form.title)) {
          this.$message({
            message: "请填写标题！",
            type: "warning"
          });
          return;
        }
        if (this.$common.isEmpty(this.form.logDate)) {
          this.$message({
            message: "请选择更新日期！",
            type: "warning"
          });
          return;
        }
        let data = Object.assign({}, this.form);
        data.topics = JSON.stringify(this.topicsText.split("\n").map((item) => item.trim()).filter(Boolean));
        data.details = JSON.stringify(this.detailsText.split("\n").map((item) => item.trim()).filter(Boolean));
        let url = data.id ? "/admin/changelog/update" : "/admin/changelog/save";
        this.$http.post(this.$constant.baseURL + url, data, true)
          .then(() => {
            this.handleClose();
            this.getRecords();
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
      removeRecord(record) {
        this.$confirm('确认删除「' + record.title + '」这条更新记录？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }).then(() => {
          this.$http.get(this.$constant.baseURL + "/admin/changelog/delete", {id: record.id}, true)
            .then(() => {
              this.getRecords();
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
        this.topicsText = "";
        this.detailsText = "";
      }
    }
  }
</script>

<style scoped>
  .changelog-title {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 13px;
  }

  .handle-input {
    width: 160px;
    display: inline-block;
  }

  .mrb10 {
    margin-right: 10px;
    margin-bottom: 10px;
  }
</style>
