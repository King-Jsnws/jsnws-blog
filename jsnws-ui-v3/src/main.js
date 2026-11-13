import { createApp } from 'vue'
import {
  ElAvatar,
  ElButton,
  ElCollapse,
  ElCollapseItem,
  ElDatePicker,
  ElDialog,
  ElDrawer,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElForm,
  ElFormItem,
  ElIcon,
  ElImage,
  ElInput,
  ElInputNumber,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElPagination,
  ElPopover,
  ElRadio,
  ElRadioButton,
  ElRadioGroup,
  ElResult,
  ElSelect,
  ElSubMenu,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
  ElTooltip,
  ElUpload,
  ElMessage,
  ElMessageBox,
  ElNotification,
  provideGlobalConfig
} from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn.mjs'
import {
  ArrowDown,
  ArrowRight,
  Calendar,
  ChatDotRound,
  Collection,
  Connection,
  CreditCard,
  DataBoard,
  Delete,
  Document,
  Edit,
  EditPen,
  Expand,
  Fold,
  FolderOpened,
  Headset,
  Histogram,
  HomeFilled,
  Link,
  Menu,
  Message,
  Monitor,
  Notebook,
  Operation,
  Paperclip,
  Picture,
  Pointer,
  Search,
  Setting,
  Sugar,
  Sunny,
  SwitchButton,
  Tickets,
  User,
  UserFilled,
  VideoCamera
} from '@element-plus/icons-vue'

// 按需样式：每个入口会带上自身的依赖样式（base / popper / overlay 等）
import 'element-plus/es/components/avatar/style/css.mjs'
import 'element-plus/es/components/button/style/css.mjs'
import 'element-plus/es/components/collapse/style/css.mjs'
import 'element-plus/es/components/collapse-item/style/css.mjs'
import 'element-plus/es/components/date-picker/style/css.mjs'
import 'element-plus/es/components/dialog/style/css.mjs'
import 'element-plus/es/components/drawer/style/css.mjs'
import 'element-plus/es/components/dropdown/style/css.mjs'
import 'element-plus/es/components/dropdown-item/style/css.mjs'
import 'element-plus/es/components/dropdown-menu/style/css.mjs'
import 'element-plus/es/components/form/style/css.mjs'
import 'element-plus/es/components/form-item/style/css.mjs'
import 'element-plus/es/components/icon/style/css.mjs'
import 'element-plus/es/components/image/style/css.mjs'
import 'element-plus/es/components/input/style/css.mjs'
import 'element-plus/es/components/input-number/style/css.mjs'
import 'element-plus/es/components/menu/style/css.mjs'
import 'element-plus/es/components/menu-item/style/css.mjs'
// 全局方法 $message / $notify / $confirm 用到的样式
import 'element-plus/es/components/message/style/css.mjs'
import 'element-plus/es/components/message-box/style/css.mjs'
import 'element-plus/es/components/notification/style/css.mjs'
import 'element-plus/es/components/option/style/css.mjs'
import 'element-plus/es/components/pagination/style/css.mjs'
import 'element-plus/es/components/popover/style/css.mjs'
import 'element-plus/es/components/radio/style/css.mjs'
import 'element-plus/es/components/radio-button/style/css.mjs'
import 'element-plus/es/components/radio-group/style/css.mjs'
import 'element-plus/es/components/result/style/css.mjs'
import 'element-plus/es/components/select/style/css.mjs'
import 'element-plus/es/components/sub-menu/style/css.mjs'
import 'element-plus/es/components/switch/style/css.mjs'
import 'element-plus/es/components/table/style/css.mjs'
import 'element-plus/es/components/table-column/style/css.mjs'
import 'element-plus/es/components/tag/style/css.mjs'
import 'element-plus/es/components/tooltip/style/css.mjs'
import 'element-plus/es/components/upload/style/css.mjs'

// Element Plus 官方暗色变量（html.dark 生效），与项目自定义变量共用同一套切换开关
import 'element-plus/theme-chalk/dark/css-vars.css'

// 项目自定义样式放在 Element Plus 之后，保证覆盖顺序与旧项目一致
import './assets/css/animation.css'
import './assets/css/index.css'
import './assets/css/admin.css'
import './assets/css/tocbot.css'
import './assets/css/color.css'
import './assets/css/markdown-highlight.css'
import './assets/css/font-awesome.min.css'

import App from './App.vue'
import router from './router'
import store from './store'
import http from './utils/request'
import common from './utils/common'
import constant from './utils/constant'
import { applyTheme } from './utils/theme'
import './utils/title'

const app = createApp(App)

// 项目实际用到的 Element Plus 组件（按需注册）
const components = [
  ElAvatar,
  ElButton,
  ElCollapse,
  ElCollapseItem,
  ElDatePicker,
  ElDialog,
  ElDrawer,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElForm,
  ElFormItem,
  ElIcon,
  ElImage,
  ElInput,
  ElInputNumber,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElPagination,
  ElPopover,
  ElRadio,
  ElRadioButton,
  ElRadioGroup,
  ElResult,
  ElSelect,
  ElSubMenu,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
  ElTooltip,
  ElUpload
]

components.forEach(component => {
  app.component(component.name, component)
})

// 项目实际用到的图标（含侧栏用字符串动态取用的那几个）
const icons = {
  ArrowDown,
  ArrowRight,
  Calendar,
  ChatDotRound,
  Collection,
  Connection,
  CreditCard,
  DataBoard,
  Delete,
  Document,
  Edit,
  EditPen,
  Expand,
  Fold,
  FolderOpened,
  Headset,
  Histogram,
  HomeFilled,
  Link,
  Menu,
  Message,
  Monitor,
  Notebook,
  Operation,
  Paperclip,
  Picture,
  Pointer,
  Search,
  Setting,
  Sugar,
  Sunny,
  SwitchButton,
  Tickets,
  User,
  UserFilled,
  VideoCamera
}

Object.keys(icons).forEach(name => {
  app.component(name, icons[name])
})

app.use(store)
app.use(router)

// 按 store 中解析好的主题同步一次 html.dark，保证首屏与后续路由一致
applyTheme(store.state.theme)

// 等价于 app.use(ElementPlus, { locale: zhCn }) 的全局配置，但不引入整包
provideGlobalConfig({ locale: zhCn }, app, true)

// 对应旧项目的 Vue.prototype.$http / $common / $constant
app.config.globalProperties.$http = http
app.config.globalProperties.$common = common
app.config.globalProperties.$constant = constant

// Element Plus 的全局方法（等价于全量安装时挂载的那几个）
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$notify = ElNotification
app.config.globalProperties.$msgbox = ElMessageBox
app.config.globalProperties.$alert = ElMessageBox.alert
app.config.globalProperties.$confirm = ElMessageBox.confirm
app.config.globalProperties.$prompt = ElMessageBox.prompt

app.mount('#app')
