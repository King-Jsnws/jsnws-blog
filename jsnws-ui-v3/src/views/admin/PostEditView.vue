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
            ref="mdEditor"
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
            @onSave="handleEditorSave">
            <!-- 自定义插入项：顺序与 toolbars 数组里的数字索引一一对应
                 0 音乐 / 1 播放器 / 2 分割线 / 3 表情 / 4 缩进
                 5 bilibili / 6 单曲 / 7 歌单
                 8 组件菜单 / 9 文字颜色 / 10 时间 / 11 特殊符号 / 12 代码段 / 13 清空
                 14 已完成 / 15 下载 / 16 发布 -->
            <template #defToolbars>
              <NormalToolbar title="音乐" @onClick="insertAudio">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M9 18V5l12-2v13"></path>
                    <circle cx="6" cy="18" r="3"></circle>
                    <circle cx="18" cy="16" r="3"></circle>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="播放器" @onClick="insertVideo">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="m16 13 5.2 3.5a.5.5 0 0 0 .8-.5V7.9a.5.5 0 0 0-.8-.4L16 10.5"></path>
                    <rect x="2" y="6" width="14" height="12" rx="2"></rect>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="分割线" @onClick="insertDivider">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" aria-hidden="true">
                    <path d="M5 12h14"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <DropdownToolbar title="表情">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <circle cx="12" cy="12" r="10"></circle>
                    <path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
                    <path d="M9 9h.01"></path>
                    <path d="M15 9h.01"></path>
                  </svg>
                </template>
                <template #overlay>
                  <!-- 复用站内评论的表情面板：三套图片表情，插入 :包名/文件名: token -->
                  <EmojiPanel @select="insertEmoji"/>
                </template>
              </DropdownToolbar>
              <NormalToolbar title="缩进" @onClick="insertIndent">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M21 6H11"></path>
                    <path d="M21 12H11"></path>
                    <path d="M21 18H11"></path>
                    <path d="m3 8 4 4-4 4"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="bilibili" @onClick="insertBilibili">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <rect x="2" y="4" width="20" height="16" rx="4"></rect>
                    <path d="m10 9.5 4.5 2.5L10 14.5z"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="单曲（APlayer）" @onClick="insertMusic">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <circle cx="12" cy="12" r="9"></circle>
                    <path d="m10 8.5 5 3.5-5 3.5z"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="歌单（APlayer）" @onClick="insertPlaylist">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M4 6h9"></path>
                    <path d="M4 12h9"></path>
                    <path d="M4 18h5"></path>
                    <path d="M17 17V8l4-1v9"></path>
                    <circle cx="15.5" cy="17.5" r="1.8"></circle>
                    <circle cx="19.5" cy="16.5" r="1.8"></circle>
                  </svg>
                </template>
              </NormalToolbar>
              <DropdownToolbar title="JOE 组件">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <rect x="3" y="3" width="7" height="7" rx="1.5"></rect>
                    <rect x="14" y="3" width="7" height="7" rx="1.5"></rect>
                    <rect x="3" y="14" width="7" height="7" rx="1.5"></rect>
                    <rect x="14" y="14" width="7" height="7" rx="1.5"></rect>
                  </svg>
                </template>
                <template #overlay>
                  <ul class="md-editor-menu joe-menu">
                    <li v-for="item in joeComponents"
                        :key="item.key"
                        class="md-editor-menu-item"
                        @click="insertJoeComponent(item.key)">{{ item.label }}</li>
                  </ul>
                </template>
              </DropdownToolbar>
              <DropdownToolbar title="文字颜色">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M4 20h16"></path>
                    <path d="m7 16 5-12 5 12"></path>
                    <path d="M9.4 12h5.2"></path>
                  </svg>
                </template>
                <template #overlay>
                  <ul class="md-editor-menu joe-color-menu">
                    <li v-for="color in textColors"
                        :key="color"
                        class="md-editor-menu-item"
                        @click="applyTextColor(color)">
                      <span class="joe-color-dot" :style="{background: color}"></span>{{ color }}
                    </li>
                  </ul>
                </template>
              </DropdownToolbar>
              <NormalToolbar title="时间" @onClick="insertTime">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <circle cx="12" cy="12" r="9"></circle>
                    <path d="M12 7v5l3 2"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <DropdownToolbar title="特殊符号">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="m12 3 2.6 5.6 6.1.8-4.5 4.2 1.2 6-5.4-3-5.4 3 1.2-6L3.3 9.4l6.1-.8z"></path>
                  </svg>
                </template>
                <template #overlay>
                  <ul class="md-editor-menu joe-symbol-menu">
                    <li v-for="symbol in symbols"
                        :key="symbol"
                        class="md-editor-menu-item"
                        @click="insertText(symbol)">{{ symbol }}</li>
                  </ul>
                </template>
              </DropdownToolbar>
              <DropdownToolbar title="代码段（可选语言）">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="m8 8-4 4 4 4"></path>
                    <path d="m16 8 4 4-4 4"></path>
                    <path d="m13 6-2 12"></path>
                  </svg>
                </template>
                <template #overlay>
                  <ul class="md-editor-menu joe-code-menu">
                    <li v-for="lang in codeLanguages"
                        :key="lang.key"
                        class="md-editor-menu-item"
                        @click="insertCodeBlock(lang.key)">{{ lang.label }}</li>
                  </ul>
                </template>
              </DropdownToolbar>
              <NormalToolbar title="清空" @onClick="clearContent">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M4 7h16"></path>
                    <path d="M9 7V5h6v2"></path>
                    <path d="m6 7 1 13h10l1-13"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="已完成" @onClick="insertCheckedTask">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <rect x="3" y="3" width="18" height="18" rx="3"></rect>
                    <path d="m7.5 12.5 3 3 6-6.5"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="下载" @onClick="insertDownload">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M12 4v11"></path>
                    <path d="m7.5 11 4.5 4.5L16.5 11"></path>
                    <path d="M5 20h14"></path>
                  </svg>
                </template>
              </NormalToolbar>
              <NormalToolbar title="发布" @onClick="publishArticle">
                <template #trigger>
                  <svg class="md-editor-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                       stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <circle cx="12" cy="12" r="9"></circle>
                    <path d="M12 8v8"></path>
                    <path d="M8 12h8"></path>
                  </svg>
                </template>
              </NormalToolbar>
            </template>
          </MdEditor>
        </div>
        <div class="editor-tip">
          支持 Markdown、代码高亮、表格、音频/视频、分割线、表情与段落缩进；图片可拖拽或粘贴上传；Ctrl / ⌘ + S 快速保存。
        </div>
      </el-form-item>

      <el-form-item v-if="isBoss" label="是否启用评论" prop="commentStatus">
        <el-tag :type="article.commentStatus === false ? 'danger' : 'success'"
                disable-transitions>
          {{article.commentStatus === false ? '否' : '是'}}
        </el-tag>
        <el-switch v-model="article.commentStatus"></el-switch>
      </el-form-item>

      <el-form-item v-if="isBoss" label="是否推荐" prop="recommendStatus">
        <el-tag :type="article.recommendStatus === false ? 'danger' : 'success'"
                disable-transitions>
          {{article.recommendStatus === false ? '否' : '是'}}
        </el-tag>
        <el-switch v-model="article.recommendStatus"></el-switch>
      </el-form-item>

      <el-form-item v-if="isBoss" label="是否置顶" prop="isTop">
        <el-tag :type="article.isTop === true ? 'success' : 'danger'"
                disable-transitions>
          {{article.isTop === true ? '是' : '否'}}
        </el-tag>
        <el-switch v-model="article.isTop"></el-switch>
      </el-form-item>

      <el-form-item v-if="isBoss" label="是否可见" prop="viewStatus">
        <el-tag :type="article.viewStatus === false ? 'danger' : 'success'"
                disable-transitions>
          {{article.viewStatus === false ? '否' : '是'}}
        </el-tag>
        <el-switch v-model="article.viewStatus"></el-switch>
      </el-form-item>

      <el-form-item v-if="isBoss && article.viewStatus === false" label="不可见时的访问密码" prop="password">
        <el-input maxlength="30" v-model="article.password"></el-input>
      </el-form-item>

      <el-form-item v-if="isBoss && article.viewStatus === false" label="密码提示" prop="tips">
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
        <el-select v-model="article.labelId"
                   class="label-select"
                   filterable
                   :allow-create="isBoss"
                   default-first-option
                   :reserve-keyword="false"
                   :disabled="!article.sortId"
                   :placeholder="labelPlaceholder"
                   @change="handleLabelChange">
          <el-option
            v-for="item in labelsTemp"
            :key="item.id"
            :label="item.labelName"
            :value="item.id">
          </el-option>
        </el-select>
        <div class="field-tip">{{labelTip}}</div>
      </el-form-item>
    </el-form>
    <div class="post-actions">
      <el-button @click="resetForm('ruleForm')">重置所有修改</el-button>
      <el-button type="primary" @click="submitForm('ruleForm')">保存</el-button>
    </div>
  </div>
</template>

<script>
  import { MdEditor, NormalToolbar, DropdownToolbar, config } from 'md-editor-v3';
  import 'md-editor-v3/lib/style.css';

  import UploadPicture from "../../components/common/UploadPicture.vue";
  import EmojiPanel from "../../components/comment/EmojiPanel.vue";
  import { emojiPlugin } from "../../utils/emoji";

  // md-editor-v3 的预览默认关闭正文原生 HTML（html:false），
  // 这里打开，让工具栏插入的 <audio>/<video>/<u>/<sub>/<sup> 在编辑区预览里也能看到；
  // 同时挂上表情插件，`:` 包名/文件名 `:` token 在预览里直接显示成图片
  config({
    markdownItConfig(md) {
      md.set({html: true}).use(emojiPlugin);
    }
  });

  // JOE 组件插入模板：单行、内容容器用 span（避免被 markdown 的 <p> 拆开），插入后可原地改属性
  const JOE_SNIPPETS = {
    mtitle: '<joe-mtitle title="居中标题"></joe-mtitle>',
    abtn: '<joe-abtn color="#409eff" icon="fa-bell" radius="3px" content="多彩按钮" href="#"></joe-abtn>',
    anote: '<joe-anote href="#" type="info" icon="fa-download" content="便条按钮"></joe-anote>',
    cloud: '<joe-cloud type="bd" url="https://" password="" title="资源下载"></joe-cloud>',
    card: '<joe-card-default width="80%" label="卡片标题"><span class="_temp">卡片内容</span></joe-card-default>',
    cardDescribe: '<joe-card-describe title="卡片描述"><span class="_temp">卡片内容</span></joe-card-describe>',
    cardList: '<joe-card-list><span class="_temp">{card-list-item}第一项{/card-list-item}{card-list-item}第二项{/card-list-item}</span></joe-card-list>',
    note: '<joe-note title="备注"><span class="_temp">这里是一段备注内容</span></joe-note>',
    message: '<joe-message type="success" content="成功消息"></joe-message>',
    progress: '<joe-progress percentage="50%" color="#ff6c6c"></joe-progress>',
    tabs: '<joe-tabs><span class="_tpl">{tabs-pane 第一个}内容一{/tabs-pane}{tabs-pane 第二个}内容二{/tabs-pane}</span></joe-tabs>',
    timeline: '<joe-timeline><span class="_tpl">{timeline-item color="#ff6c6c"}2026 年：第一件事{/timeline-item}{timeline-item color="#1989fa"}2026 年：第二件事{/timeline-item}</span></joe-timeline>',
    collapse: '<joe-collapse><span class="_temp">{collapse-item label="折叠标题"}折叠内容，默认收起{/collapse-item}</span></joe-collapse>',
    gird: '<joe-gird column="3" gap="15"><span class="_temp">{gird-item}网格 1{/gird-item}{gird-item}网格 2{/gird-item}{gird-item}网格 3{/gird-item}</span></joe-gird>',
    lamp: '<joe-lamp></joe-lamp>',
    copy: '<joe-copy title="点击复制" content="要复制的内容"></joe-copy>',
    readLimited: '<joe-read-limited></joe-read-limited>',
    dotted: '<joe-dotted startColor="#ff6c6c" endColor="#1989fa"></joe-dotted>'
  };

  export default {
    name: "PostEditView",
    components: {
      MdEditor,
      NormalToolbar,
      DropdownToolbar,
      UploadPicture,
      EmojiPanel
    },
    computed: {
      // md-editor-v3 自带暗色主题，跟随全站主题切换
      editorTheme() {
        return this.$store.state.theme === 'dark' ? 'dark' : 'light'
      },
      // 仅主理人可在发文页直接新增标签（与后端 /webInfo/saveLabel 的权限保持一致）
      isBoss() {
        return !!this.$store.state.currentAdmin.isBoss;
      },
      labelPlaceholder() {
        if (!this.article.sortId) {
          return '请先选择分类';
        }
        return this.isBoss ? '请选择标签，或输入新标签后回车' : '请选择标签';
      },
      labelTip() {
        if (!this.article.sortId) {
          return '标签归属于分类，请先选择分类后再填写标签。';
        }
        if (!this.isBoss) {
          return '标签由主理人在「分类管理」中维护，这里只能选择当前分类已有的标签。';
        }
        return '只能填写 1 个标签，最多 20 个字符；输入新名称后按回车即可创建并选中。';
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
          isTop: false,
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
        labelCreating: false,
        // 按参考站顺序分组：撤销重做 → 行内格式 → 块格式 → 任务/组件/媒体 → 颜色/时间/符号/表情 → 清空/下载/视图/发布
        toolbars: [
          "revoke", "next", "-",
          "bold", "underline", "italic", "strike-through", "sub", "sup", "code-row", 2, "quote", "title", "ordered-list", "-",
          "unordered-list", "link", "image", "table", 12, 9, 10, 4, 11, 3, "-",
          "task", 14, 8, 1, 5, 7, 6, "-",
          0, "-",
          13, 15, "fullscreen", "preview", "prettier", "catalog", "save", 16
        ],
        // JOE 组件菜单：每一项都对应正文已注册并渲染的自定义标签
        joeComponents: [
          {key: "mtitle", label: "居中标题"},
          {key: "abtn", label: "多彩按钮"},
          {key: "anote", label: "便条按钮"},
          {key: "dotted", label: "彩色分割线"},
          {key: "readLimited", label: "隐藏（评论后可见）"},
          {key: "card", label: "卡片1"},
          {key: "message", label: "消息提示"},
          {key: "progress", label: "进度条"},
          {key: "note", label: "备注"},
          {key: "tabs", label: "标签栏"},
          {key: "cardList", label: "列表"},
          {key: "timeline", label: "时间轴"},
          {key: "copy", label: "点击复制"},
          {key: "cardDescribe", label: "卡片二2"},
          {key: "lamp", label: "跑马条"},
          {key: "collapse", label: "折叠内容"},
          {key: "cloud", label: "网盘下载"}
        ],
        textColors: ["#fb6c28", "#e74c3c", "#e67e22", "#f1c40f", "#27ae60",
          "#16a085", "#3498db", "#9b59b6", "#95a5a6"],
        symbols: ["©", "®", "™", "★", "☆", "♥", "♪", "→", "←", "↑", "↓", "↔",
          "√", "×", "÷", "±", "≤", "≥", "≠", "∞", "℃", "°", "№", "§",
          "①", "②", "③", "④", "⑤", "✓", "✗", "⚠"],
        codeLanguages: [
          {key: "js", label: "JavaScript"},
          {key: "ts", label: "TypeScript"},
          {key: "java", label: "Java"},
          {key: "python", label: "Python"},
          {key: "html", label: "HTML"},
          {key: "css", label: "CSS"},
          {key: "scss", label: "SCSS"},
          {key: "json", label: "JSON"},
          {key: "yaml", label: "YAML"},
          {key: "xml", label: "XML"},
          {key: "sql", label: "SQL"},
          {key: "bash", label: "Shell / Bash"},
          {key: "go", label: "Go"},
          {key: "php", label: "PHP"},
          {key: "csharp", label: "C#"},
          {key: "cpp", label: "C / C++"},
          {key: "diff", label: "Diff"},
          {key: "plaintext", label: "纯文本"}
        ],
        // 表情面板：复用站内评论的三套图片表情，插入 :包名/文件名: token
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
        if (oldVal !== null && oldVal !== newVal) {
          // 标签归属于分类，切换分类后旧标签（含回显的标签对象）一并清空
          this.article.labelId = null;
          this.article.label = null;
        }
        this.refreshLabelOptions();
      }
    },

    created() {
      this.getSortAndLabel();
    },

    mounted() {
      this.$nextTick(() => {
        this.layoutToolbarDividers();
        if (window.ResizeObserver) {
          this.toolbarResizeObserver = new ResizeObserver(() => this.layoutToolbarDividers());
          this.$el.querySelectorAll(".md-editor-toolbar").forEach((el) => {
            this.toolbarResizeObserver.observe(el);
          });
        }
      });
    },

    beforeUnmount() {
      if (this.toolbarResizeObserver) {
        this.toolbarResizeObserver.disconnect();
        this.toolbarResizeObserver = null;
      }
    },

    methods: {
      handleEditorSave() {
        this.submitForm('ruleForm');
      },
      // 换行后若分隔线落在行尾或独占一行，隐藏它，避免分隔线脱离所属分组
      layoutToolbarDividers() {
        if (!this.$el || !this.$el.querySelectorAll) {
          return;
        }
        this.$el.querySelectorAll(".md-editor-toolbar-left, .md-editor-toolbar-right").forEach((group) => {
          const children = Array.from(group.children).filter((el) => el.nodeType === 1);
          children.forEach((el, index) => {
            if (!el.classList.contains("md-editor-divider")) {
              return;
            }
            el.style.display = "";
            const rect = el.getBoundingClientRect();
            const centerY = rect.top + rect.height / 2;
            // 垂直中心落在后续按钮的行高内，说明同行后面还有按钮，分隔线保留
            const nextSameRow = children.slice(index + 1).some((sibling) => {
              const siblingRect = sibling.getBoundingClientRect();
              return siblingRect.height > 0 && centerY >= siblingRect.top && centerY <= siblingRect.bottom;
            });
            if (!nextSameRow) {
              el.style.display = "none";
            }
          });
        });
      },
      // 在光标处插入文本（md-editor-v3 暴露的 insert 方法）
      insertText(text) {
        const editor = this.$refs.mdEditor;
        if (!editor || typeof editor.insert !== 'function') {
          return;
        }
        editor.insert(() => ({targetValue: text, select: false}));
      },
      // 表情面板选中的 token，直接插到光标处
      insertEmoji(token) {
        this.insertText(token);
      },
      // 块级内容统一前后补空行，避免和相邻段落粘在一起
      insertBlock(html) {
        this.insertText('\n' + html + '\n');
      },
      // JOE 组件：菜单项 key 对应 JOE_SNIPPETS 里的单行模板
      insertJoeComponent(key) {
        if (JOE_SNIPPETS[key]) {
          this.insertBlock(JOE_SNIPPETS[key]);
        }
      },
      // 文字颜色：有选区就包住选区，否则插入占位文字
      applyTextColor(color) {
        const editor = this.$refs.mdEditor;
        if (!editor || typeof editor.insert !== 'function') {
          return;
        }
        editor.insert((selected) => ({
          targetValue: `<span style="color:${color}">${selected || '彩色文字'}</span>`,
          select: false
        }));
      },
      // 插入当前时间（精确到分钟，正文里作为纯文本）
      insertTime() {
        const now = new Date();
        const pad = (value) => (value < 10 ? '0' + value : '' + value);
        this.insertText(`${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} `
          + `${pad(now.getHours())}:${pad(now.getMinutes())}`);
      },
      // 代码块：先落语言标记，光标不强制选中，直接补正文
      insertCodeBlock(lang) {
        this.insertText('\n```' + lang + '\n在这里输入代码\n```\n');
      },
      // B 站视频：只收 BV 号，避免把任意地址拼进 iframe
      insertBilibili() {
        this.$prompt('请输入 B 站视频 BV 号', '插入 B 站视频', {
          confirmButtonText: '插入',
          cancelButtonText: '取消',
          inputPlaceholder: '例如 BV1GJ411x7h7',
          inputPattern: /^BV[0-9A-Za-z]+$/,
          inputErrorMessage: 'BV 号格式不正确'
        }).then(({value}) => {
          this.insertBlock(`<joe-bilibili bvid="${String(value).trim()}"></joe-bilibili>`);
        }).catch(() => {
        });
      },
      // 单曲 / 歌单：只收网易云数字 ID，地址解析在组件内完成
      insertMusic() {
        this.$prompt('请输入网易云歌曲 ID', '插入单曲', {
          confirmButtonText: '插入',
          cancelButtonText: '取消',
          inputPlaceholder: '例如 186016',
          inputPattern: /^\d+$/,
          inputErrorMessage: '请输入纯数字歌曲 ID'
        }).then(({value}) => {
          this.insertBlock(`<joe-music id="${String(value).trim()}"></joe-music>`);
        }).catch(() => {
        });
      },
      insertPlaylist() {
        this.$prompt('请输入网易云歌单 ID', '插入歌单', {
          confirmButtonText: '插入',
          cancelButtonText: '取消',
          inputPlaceholder: '例如 3778678',
          inputPattern: /^\d+$/,
          inputErrorMessage: '请输入纯数字歌单 ID'
        }).then(({value}) => {
          this.insertBlock(`<joe-mlist id="${String(value).trim()}"></joe-mlist>`);
        }).catch(() => {
        });
      },
      clearContent() {
        if (this.$common.isEmpty(this.article.articleContent)) {
          return;
        }
        this.$confirm('确认清空当前正文？清空后无法恢复。', '清空正文', {
          confirmButtonText: '清空',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.article.articleContent = '';
        }).catch(() => {
        });
      },
      // 已完成：插入一条勾选状态的待办
      insertCheckedTask() {
        this.insertText('\n- [x] 已完成事项');
      },
      // 下载：只收 http(s) 地址，落到 joe-download 组件
      insertDownload() {
        this.$prompt('请输入下载地址', '插入下载按钮', {
          confirmButtonText: '插入',
          cancelButtonText: '取消',
          inputPlaceholder: 'https://example.com/file.zip',
          inputPattern: /^https?:\/\/[^\s"'<>]+$/,
          inputErrorMessage: '请输入 http(s) 开头的地址'
        }).then(({value}) => {
          this.insertBlock(`<joe-download href="${String(value).trim()}" content="点击下载"></joe-download>`);
        }).catch(() => {
        });
      },
      // 发布：复用保存流程，校验通过后提交
      publishArticle() {
        this.handleEditorSave();
      },
      // 音频 / 视频：带地址校验的插入
      insertAudio() {
        this.promptMediaUrl('audio');
      },
      insertVideo() {
        this.promptMediaUrl('video');
      },
      promptMediaUrl(kind) {
        const isAudio = kind === 'audio';
        this.$prompt(
          isAudio ? '请输入音频地址（mp3 / m4a 等）' : '请输入视频地址（mp4 / webm 等）',
          isAudio ? '插入音乐/音频' : '插入视频',
          {
            confirmButtonText: '插入',
            cancelButtonText: '取消',
            inputPlaceholder: isAudio ? 'https://example.com/audio.mp3' : 'https://example.com/video.mp4',
            inputPattern: /^(https?:\/\/|\/)[^\s]+$/i,
            inputErrorMessage: '只支持 http/https 地址或站内路径'
          }
        ).then(({value}) => {
          const url = String(value).trim().replace(/"/g, '%22');
          const html = isAudio
            ? `<audio controls preload="metadata" src="${url}"></audio>`
            : `<video controls preload="metadata" playsinline src="${url}"></video>`;
          this.insertText(`\n${html}\n`);
        }).catch(() => {
        });
      },
      insertDivider() {
        this.insertText('\n\n---\n\n');
      },
      // 段落缩进：对应 joe 的 <p class="indent">，样式在 markdown-highlight.css
      insertIndent() {
        this.insertText('\n<p class="indent">在这里输入缩进段落</p>\n');
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
        this.loadLabels()
          .then(() => {
            if (!this.$common.isEmpty(this.id)) {
              this.getArticle();
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      // 拉取分类与标签全量数据，供标签下拉按分类过滤
      loadLabels() {
        return this.$http.get(this.$constant.baseURL + "/webInfo/listSortAndLabel")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.sorts = res.data.sorts;
              this.labels = res.data.labels;
              this.refreshLabelOptions();
            }
          });
      },
      // 标签候选项 = 当前分类下的标签；编辑回显时保证已选标签一定在下拉中
      refreshLabelOptions() {
        const sortId = this.article.sortId;
        if (this.$common.isEmpty(sortId) || this.$common.isEmpty(this.labels)) {
          this.labelsTemp = [];
          return;
        }
        let list = this.labels.filter(l => l.sortId === sortId);
        const current = this.article.label;
        if (current && current.id && !list.some(l => l.id === current.id)) {
          list = [current, ...list];
        }
        this.labelsTemp = list;
      },
      // 下拉允许输入新标签：已有同名标签直接复用，否则调用既有标签接口创建
      handleLabelChange(value) {
        if (typeof value !== 'string') {
          return;
        }
        const name = value.trim();
        if (this.$common.isEmpty(name)) {
          this.article.labelId = null;
          return;
        }
        if (!this.isBoss) {
          this.article.labelId = null;
          this.$message({
            message: "标签由主理人维护，请选择已有标签！",
            type: "error"
          });
          return;
        }
        if (this.$common.isEmpty(this.article.sortId)) {
          this.article.labelId = null;
          this.$message({
            message: "请先选择分类，再添加标签！",
            type: "error"
          });
          return;
        }
        if (name.length > 20) {
          this.article.labelId = null;
          this.$message({
            message: "标签名称不能超过 20 个字符！",
            type: "error"
          });
          return;
        }
        const exist = this.labelsTemp.find(l => l.labelName === name);
        if (exist) {
          this.article.labelId = exist.id;
          return;
        }
        const sortId = this.article.sortId;
        this.labelCreating = true;
        this.$http.post(this.$constant.baseURL + "/webInfo/saveLabel", {
          sortId: sortId,
          labelName: name,
          labelDescription: ""
        }, true)
          .then(() => this.loadLabels())
          .then(() => {
            const created = this.labels.find(l => l.sortId === sortId && l.labelName === name);
            if (created) {
              this.article.labelId = created.id;
              this.$message({
                message: "标签「" + name + "」已创建！",
                type: "success"
              });
            } else {
              this.article.labelId = null;
              this.$message({
                message: "标签创建后未能读取，请刷新页面重试！",
                type: "error"
              });
            }
          })
          .catch((error) => {
            this.article.labelId = null;
            this.$message({
              message: error.message,
              type: "error"
            });
          })
          .finally(() => {
            this.labelCreating = false;
          });
      },
      getArticle() {
        this.$http.get(this.$constant.baseURL + "/admin/article/getArticleById", {id: this.id}, true)
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.article = res.data;
              this.refreshLabelOptions();
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
        if (this.labelCreating) {
          this.$message({
            message: "标签创建中，请稍候再保存！",
            type: "error"
          });
          return;
        }
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

  .label-select {
    width: 100%;
  }

  .field-tip {
    margin-top: 6px;
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

  /* ==========================================================================
     编辑器工具栏：复刻 joe2.0 的控件视觉语言
     joe 侧依据：theme.less 灰阶变量（--classA~D / --minor / --routine）、
     --radius-inner:4px、--theme 主色、0.35s 过渡、0 0 10px rgba(0,0,0,.15) 浮层阴影
     只作用于本页编辑器，功能、按钮顺序与交互逻辑不变
     ========================================================================== */
  .editor-shell {
    --joe-theme: #fb6c28;
    --joe-bg: #fff;
    --joe-classA: #dcdfe6;
    --joe-classB: #e4e7ed;
    --joe-classC: #ebeef5;
    --joe-classD: #f2f6fc;
    --joe-minor: #909399;
    --joe-routine: #606266;
    --joe-radius-inner: 4px;
    --joe-shadow: 0 0 10px rgba(0, 0, 0, .15);
  }

  html.dark .editor-shell {
    --joe-theme: #9999ff;
    --joe-bg: #232323;
    --joe-classA: #515253;
    --joe-classB: #454545;
    --joe-classC: #414243;
    --joe-classD: #303030;
    --joe-minor: #777;
    --joe-routine: #888;
    --joe-shadow: 0 0 10px rgba(0, 0, 0, .4);
  }

  .editor-shell :deep(.md-editor .md-editor-toolbar-wrapper) {
    padding: 6px 8px;
    overflow-x: hidden;
    background: var(--joe-bg);
    border-block-end-color: var(--joe-classA);
  }

  /* 工具栏放不下时换行，不横向滚动、不压缩按钮 */
  .editor-shell :deep(.md-editor .md-editor-toolbar) {
    flex-wrap: wrap;
    row-gap: 4px;
  }

  .editor-shell :deep(.md-editor .md-editor-toolbar-left) {
    flex: 1 1 0;
    min-width: 0;
    flex-wrap: wrap;
    row-gap: 4px;
  }

  .editor-shell :deep(.md-editor .md-editor-toolbar-right) {
    flex: 0 0 auto;
    flex-wrap: wrap;
    row-gap: 4px;
  }

  /* 按钮：30px 方形热区 + 4px 圆角，图标 16px 居中 */
  .editor-shell :deep(.md-editor .md-editor-toolbar-item) {
    flex: 0 0 30px;
    justify-content: center;
    width: 30px;
    min-width: 30px;
    height: 30px;
    margin: 0 1px;
    padding: 0;
    border-radius: var(--joe-radius-inner);
    color: var(--joe-minor);
    transition: color .35s, background-color .35s;
  }

  .editor-shell :deep(.md-editor svg.md-editor-icon) {
    width: 16px;
    height: 16px;
    padding: 7px;
  }

  /* hover：灰阶浅底 + 主色图标（joe 控件统一反馈） */
  .editor-shell :deep(.md-editor .md-editor-toolbar-item:not([disabled]):hover) {
    color: var(--joe-theme);
    background-color: var(--joe-classD);
  }

  /* active / 展开态：主色图标 + 深一档灰底 */
  .editor-shell :deep(.md-editor .md-editor-toolbar-item.md-editor-toolbar-active) {
    color: var(--joe-theme);
    background-color: var(--joe-classC);
  }

  /* 分组分隔线 */
  .editor-shell :deep(.md-editor .md-editor-toolbar-wrapper .md-editor-divider) {
    flex: 0 0 1px;
    width: 1px;
    height: 16px;
    margin: 0 7px;
    background-color: var(--joe-classB);
  }

  /* 下拉浮层：4px 圆角 + 浅边框 + joe 浮层阴影 */
  .editor-shell :deep(.md-editor .md-editor-dropdown) {
    margin-top: 4px;
    background: var(--joe-bg);
    border: 1px solid var(--joe-classA);
    border-radius: var(--joe-radius-inner);
    box-shadow: var(--joe-shadow);
  }

  .editor-shell :deep(.md-editor .md-editor-menu) {
    border: none;
    border-radius: 0;
    background: transparent;
  }

  .editor-shell :deep(.md-editor .md-editor-menu-item) {
    padding: 6px 12px;
    font-size: 13px;
    line-height: 18px;
    color: var(--joe-routine);
    transition: color .35s, background-color .35s;
  }

  .editor-shell :deep(.md-editor .md-editor-menu-item:hover) {
    color: var(--joe-theme);
    background: var(--joe-classD);
  }

  /* 自定义下拉菜单：固定宽度 + 限高滚动，长列表不把浮层撑破 */
  .joe-menu,
  .joe-color-menu,
  .joe-symbol-menu,
  .joe-code-menu {
    max-height: 264px;
    overflow-y: auto;
    overscroll-behavior: contain;
  }

  .joe-menu,
  .joe-code-menu {
    width: 168px;
  }

  .joe-color-menu {
    width: 148px;
  }

  .joe-symbol-menu {
    display: grid;
    grid-template-columns: repeat(8, 26px);
    width: 232px;
    padding: 4px;
  }

  .joe-symbol-menu .md-editor-menu-item {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    padding: 0;
    font-size: 15px;
    line-height: 1;
    border-radius: var(--joe-radius-inner);
  }

  /* 颜色项的色点：和文字基线对齐，深色下靠内描边区分浅色 */
  .joe-color-dot {
    display: inline-block;
    width: 12px;
    height: 12px;
    margin-right: 8px;
    border-radius: 50%;
    vertical-align: -1px;
    box-shadow: inset 0 0 0 1px rgba(0, 0, 0, .12);
  }

  /* 表情面板：复用评论的表情组件，浮层里给一个固定宽度 */
  .editor-shell :deep(.md-editor .emoji-panel) {
    width: 300px;
    padding: 8px;
  }
</style>
