<template>
  <div>
    <!-- 网站灰度：到设定时间后整体去色，后台页面不受影响 -->
    <div v-if="siteGrayActive" class="site-gray-overlay" aria-hidden="true"></div>
    <!-- el过渡动画 -->
    <transition name="el-fade-in-linear">
      <!-- 导航栏 -->
      <div v-show="toolbar.visible || ($common.mobile() || mobile)"
           class="toolbar-content myBetween">
        <!-- 站点标识：后台「网站设置 → 站点 LOGO」独立配置，配置了就显示图片，否则回退文字站名 -->
        <div class="toolbar-title" @click="$router.push({path: '/'})">
          <img v-if="siteLogo && !logoError"
               :src="siteLogo"
               :alt="siteName"
               class="toolbar-logo"
               :class="{ 'toolbar-logo--mobile': $common.mobile() || mobile }"
               @error="logoError = true">
          <h2 v-else>{{ siteName }}</h2>
        </div>

        <!-- 手机导航按钮 -->
        <div v-if="$common.mobile() || mobile"
             class="toolbar-mobile-menu"
             @click="toolbarDrawer = !toolbarDrawer">
          <el-icon><Operation /></el-icon>
        </div>

        <!-- 导航列表 -->
        <div v-else>
          <ul class="scroll-menu">
            <!-- 导航项由后台「导航管理」控制显示与排序 -->
            <template v-for="navKey in visibleNavKeys" :key="navKey">
              <!-- 主页 -->
              <li v-if="navKey === 'home'" class="nav-pill-cell" @click="$router.push({path: '/'})">
                <div class="nav-pill" :class="{ 'is-active': $route.path === '/' }">
                  <i class="fa fa-home" aria-hidden="true"></i> <span>主页</span>
                </div>
              </li>

              <!-- 文章：文章列表 / 分类 / 图谱 / 归档 -->
              <li v-else-if="navKey === 'article'" class="article-menu-cell">
                <NavDropdown label="文章"
                             variant="article"
                             :trigger-icon="icons.list"
                             :items="articleMenuItems"></NavDropdown>
              </li>

              <!-- 追番 -->
              <li v-else-if="navKey === 'anime'" class="nav-pill-cell" @click="$router.push({path: '/anime'})">
                <div class="nav-pill" :class="{ 'is-active': $route.path === '/anime' }">
                  <svg class="anime-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <rect x="1.5" y="5" width="21" height="14" rx="2.5" fill="currentColor"></rect>
                    <path d="M9.6 8.8 16 12l-6.4 3.2V8.8Z" fill="#1f1f1f"></path>
                  </svg>
                  <span>追番</span>
                </div>
              </li>

              <!-- 动态：随笔 / 相册 / 留言 -->
              <li v-else-if="navKey === 'dynamic'" class="article-menu-cell">
                <NavDropdown label="动态"
                             :trigger-icon="icons.dynamic"
                             :items="dynamicMenuItems"
                             :active="dynamicActive"></NavDropdown>
              </li>

              <!-- 关于：关于我 / 友链 / 打赏 / 站点统计 -->
              <li v-else-if="navKey === 'about'" class="article-menu-cell">
                <NavDropdown label="关于"
                             :trigger-icon="icons.info"
                             :items="aboutMenuItems"></NavDropdown>
              </li>
            </template>

            <!-- 后台（仅管理员登录后显示） -->
            <li v-if="adminLoggedIn" class="nav-pill-cell" @click="goAdmin()">
              <div class="nav-pill">
                <svg class="admin-nav-icon" viewBox="0 0 1024 1024" aria-hidden="true" focusable="false">
                  <path fill="currentColor" d="M544 768v128h192a32 32 0 1 1 0 64H288a32 32 0 1 1 0-64h192V768H192A128 128 0 0 1 64 640V256a128 128 0 0 1 128-128h640a128 128 0 0 1 128 128v384a128 128 0 0 1-128 128zM192 192a64 64 0 0 0-64 64v384a64 64 0 0 0 64 64h640a64 64 0 0 0 64-64V256a64 64 0 0 0-64-64z"/>
                </svg>
                <span>后台</span>
              </div>
            </li>

            <!-- 未登录：圆形登录按钮（参考 poetize.cn，色值用项目蓝）；已登录：头像 + 下拉 -->
            <li v-if="$common.isEmpty($store.state.currentUser)" class="login-cell">
              <button type="button" class="login-circle"
                      @click="$router.push({path: '/user'})">登录</button>
            </li>
            <li v-else class="avatar-cell">
              <el-dropdown placement="bottom-end"
                           popper-class="nav-user-popper"
                           :show-arrow="false"
                           :hide-timeout="280"
                           :popper-options="navUserPopperOptions"
                           @visible-change="onAvatarCardVisibleChange">
                <span class="identity-v-wrap">
                  <el-avatar ref="navAvatar" class="nav-avatar" :size="40" :src="userAvatar"
                             :alt="userName + '的头像'" fit="inherit"></el-avatar>
                  <span class="identity-v-badge" v-if="userHasIdentity" title="认证身份">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
                  </span>
                </span>

                <template #dropdown>
                  <div class="nav-user-card">
                    <div class="nav-user-head">
                      <span class="identity-v-wrap">
                        <el-avatar class="nav-user-avatar" :size="64" :src="userAvatar"></el-avatar>
                        <span class="identity-v-badge" v-if="userHasIdentity" title="认证身份">
                          <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
                        </span>
                      </span>
                      <div class="nav-user-name">{{ ownerName }}</div>
                    </div>

                    <div class="nav-user-item" @click="$router.push({path: '/user'})">
                      <el-icon class="nav-user-icon"><User /></el-icon>
                      <span>个人中心</span>
                      <el-icon class="nav-user-arrow"><ArrowRight /></el-icon>
                    </div>

                    <div class="nav-user-divider"></div>

                    <div class="nav-user-item" @click="logout()">
                      <el-icon class="nav-user-icon"><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </div>
                  </div>
                </template>
              </el-dropdown>
            </li>
          </ul>
        </div>
      </div>
    </transition>

    <div id="main-container" :class="{ 'page-stage--instant': pageOpenInstant }">
      <router-view></router-view>
    </div>

    <div class="toolButton">
      <div class="backTop" v-if="toolButton" @click="toTop()">
        <!-- 回到顶部按钮 -->
        <svg viewBox="0 0 1024 1024" width="50" height="50">
          <path
            d="M696.741825 447.714002c2.717387-214.485615-173.757803-312.227566-187.33574-320.371729-10.857551 5.430775-190.050127 103.168727-187.33274 320.371729-35.297037 24.435488-73.306463 65.1623-67.875688 135.752376 5.430775 70.589076 76.018851 119.460051 103.168726 116.745664 27.152875-2.716387 19.004713-21.7221 19.004713-21.7221l8.148162-38.011425s40.721814 59.732525 51.583363 59.732525h146.609927c13.574938 0 51.585363-59.732525 51.585363-59.732525l8.147162 38.011425s-8.147162 19.005713 19.004713 21.7221c27.148876 2.714388 97.738951-46.156588 103.168727-116.745664s-32.57965-111.316888-67.876688-135.752376z m-187.33574-2.713388c-5.426776 0-70.589076-2.717387-78.733239-78.737238 2.713388-73.306463 73.306463-78.733239 78.733239-81.450626 5.430775 0 76.02385 8.144163 78.736238 81.450626-8.143163 76.019851-73.305463 78.737238-78.736238 78.737238z m0 0"
            fill="#000000"></path>
          <path
            d="M423.602441 746.060699c6.47054-6.297579 12.823107-7.017417 21.629121-2.784372 34.520213 16.582259 70.232157 19.645568 107.031855 9.116944 8.118169-2.323476 15.974396-5.475765 23.598677-9.22392 13.712907-6.73648 26.003134 0.8878 26.080116 16.13936 0.109975 22.574907-0.024994 45.142816 0.080982 67.709725 0.031993 7.464316-2.277486 13.322995-9.44387 16.608254-7.277358 3.333248-13.765895 1.961558-19.526595-3.264264-3.653176-3.313253-7.063407-6.897444-10.634601-10.304675-6.563519-6.259588-6.676494-6.25259-10.625603 1.603638-8.437097 16.80121-16.821205 33.623415-25.257302 50.423625-2.489438 4.953882-5.706713 9.196925-11.411426 10.775569-8.355115 2.315478-15.772442-1.070758-20.272427-9.867774-8.774021-17.15313-17.269104-34.453228-25.918153-51.669344-3.750154-7.469315-3.9891-7.479313-10.141712-1.514658-3.715162 3.602187-7.31435 7.326347-11.142486 10.800563-5.571743 5.060858-11.934308 6.269586-18.936728 3.207277-6.82746-2.984327-9.869774-8.483086-9.892769-15.685462-0.070984-23.506697-0.041991-47.018393-0.020995-70.532089 0.007998-4.679944 1.46467-8.785018 4.803916-11.538397z"
            fill="#000000"></path>
        </svg>
      </div>

      <el-popover placement="left"
                  :close-delay="500"
                  trigger="hover">
        <template #reference>
          <div>
            <i class="fa fa-cog iconRotate" style="color: var(--fontColor)" aria-hidden="true"></i>
          </div>
        </template>
        <div class="my-setting">
          <div>
            <!-- 主题切换：与后台共用 ThemeToggle + Vuex 中同一份主题状态 -->
            <ThemeToggle spin></ThemeToggle>
          </div>
          <div>
            <i class="fa fa-snowflake-o" aria-hidden="true" @click="changeMouseAnimation()"></i>
          </div>
        </div>
      </el-popover>
    </div>

    <!-- 点击动画 -->
    <canvas v-if="mouseAnimation" id="mousedown"
            style="position:fixed;left:0;top:0;pointer-events:none;z-index: 1000">
    </canvas>

    <!-- 图片预览 -->
    <div id="outerImg">
      <div id="innerImg" style="position:absolute">
        <img id="bigImg" src=""/>
      </div>
    </div>

    <el-drawer v-model="toolbarDrawer"
               :show-close="false"
               size="86%"
               class="toolbarDrawer"
               title="欢迎光临"
               direction="ltr">
      <div>
        <ul class="small-menu">
          <!-- 导航项由后台「导航管理」控制显示与排序 -->
          <template v-for="navKey in visibleNavKeys" :key="navKey">
            <li v-if="navKey === 'home'" class="nav-pill-cell" @click="smallMenu({path: '/'})">
              <div class="nav-pill" :class="{ 'is-active': $route.path === '/' }">
                <i class="fa fa-home" aria-hidden="true"></i> <span>主页</span>
              </div>
            </li>

            <li v-else-if="navKey === 'article'" class="drawer-article-menu">
              <NavDropdown label="文章" inline
                           variant="article"
                           :trigger-icon="icons.list"
                           :items="articleMenuItems"></NavDropdown>
            </li>

            <!-- 追番 -->
            <li v-else-if="navKey === 'anime'" class="nav-pill-cell" @click="smallMenu({path: '/anime'})">
              <div class="nav-pill" :class="{ 'is-active': $route.path === '/anime' }">
                <svg class="anime-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                  <rect x="1.5" y="5" width="21" height="14" rx="2.5" fill="currentColor"></rect>
                  <path d="M9.6 8.8 16 12l-6.4 3.2V8.8Z" fill="#1f1f1f"></path>
                </svg>
                <span>追番</span>
              </div>
            </li>

            <!-- 动态：随笔 / 相册 / 留言 -->
            <li v-else-if="navKey === 'dynamic'" class="drawer-article-menu">
              <NavDropdown label="动态" inline
                           :trigger-icon="icons.dynamic"
                           :items="dynamicMenuItems"
                           :active="dynamicActive"></NavDropdown>
            </li>

            <!-- 关于：关于我 / 友链 / 打赏 -->
            <li v-else-if="navKey === 'about'" class="drawer-article-menu">
              <NavDropdown label="关于" inline
                           :trigger-icon="icons.info"
                           :items="aboutMenuItems"></NavDropdown>
            </li>
          </template>

          <!-- 后台（仅管理员登录后显示） -->
          <li v-if="adminLoggedIn" class="nav-pill-cell" @click="goAdmin()">
            <div class="nav-pill">
              <svg class="admin-nav-icon" viewBox="0 0 1024 1024" aria-hidden="true" focusable="false">
                <path fill="currentColor" d="M544 768v128h192a32 32 0 1 1 0 64H288a32 32 0 1 1 0-64h192V768H192A128 128 0 0 1 64 640V256a128 128 0 0 1 128-128h640a128 128 0 0 1 128 128v384a128 128 0 0 1-128 128zM192 192a64 64 0 0 0-64 64v384a64 64 0 0 0 64 64h640a64 64 0 0 0 64-64V256a64 64 0 0 0-64-64z"/>
              </svg>
              <span>后台</span>
            </div>
          </li>

          <template v-if="$common.isEmpty($store.state.currentUser)">
            <li class="drawer-login" @click="smallMenu({path: '/user'})">
              <span class="login-circle">登录</span>
            </li>
          </template>
          <template v-else>
            <li class="drawer-user-head">
              <span class="identity-v-wrap">
                <el-avatar class="drawer-user-avatar" :size="56" :src="userAvatar"
                           :alt="userName + '的头像'" fit="inherit"></el-avatar>
                <span class="identity-v-badge" v-if="userHasIdentity" title="认证身份">
                  <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#ffffff" d="M6.2 4.5h3.3l2.5 9.6 2.5-9.6h3.3L13.9 20h-3.8z"/></svg>
                </span>
              </span>
              <div class="nav-user-name">{{ ownerName }}</div>
            </li>
            <li class="nav-user-item" @click="smallMenu({path: '/user'})">
              <el-icon class="nav-user-icon"><User /></el-icon>
              <span>个人中心</span>
              <el-icon class="nav-user-arrow"><ArrowRight /></el-icon>
            </li>
            <li class="nav-user-divider"></li>
            <li class="nav-user-item" @click="smallMenuLogout()">
              <el-icon class="nav-user-icon"><SwitchButton /></el-icon>
              <span>退出登录</span>
            </li>
          </template>
        </ul>
      </div>
    </el-drawer>
  </div>
</template>

<script>
  import mousedown from '../utils/mousedown'
  import { h } from 'vue'
  import { allowAdminEntry, clearAdminEntry } from '../utils/adminEntry'
  import { pageOpenState } from '../utils/pageOpen'
  import NavDropdown from '../components/common/NavDropdown.vue'
  import ThemeToggle from '../components/common/ThemeToggle.vue'
  import { ArrowRight, Camera, ChatDotRound, DataAnalysis, EditPen, InfoFilled, List, Picture, Present, SwitchButton, User } from '@element-plus/icons-vue'

  // 下拉项与图标放在模块作用域，避免图标组件被 Vue 的响应式包装
  const NAV_ICONS = {
    list: List,
    info: InfoFilled,
    dynamic: Camera
  }

  // 文章下拉的实心图标：归档（圆角方块 + 下载箭头）、分类（实心文件夹）、标签（#）
  const ArchiveSolidIcon = {
    name: 'ArchiveSolidIcon',
    render() {
      return h('svg', {
        viewBox: '0 0 24 24',
        width: '1em',
        height: '1em',
        fill: 'none',
        'aria-hidden': 'true'
      }, [
        h('rect', {x: 3, y: 3, width: 18, height: 18, rx: 5, fill: 'currentColor'}),
        h('path', {
          d: 'M12 6.8v5.6',
          stroke: 'var(--panelBg, #ffffff)',
          'stroke-width': 1.9,
          'stroke-linecap': 'round'
        }),
        h('path', {
          d: 'm9.3 10 2.7 2.7L14.7 10',
          stroke: 'var(--panelBg, #ffffff)',
          'stroke-width': 1.9,
          'stroke-linecap': 'round',
          'stroke-linejoin': 'round'
        }),
        h('path', {
          d: 'M8.6 16.2h6.8',
          stroke: 'var(--panelBg, #ffffff)',
          'stroke-width': 1.9,
          'stroke-linecap': 'round'
        })
      ])
    }
  }

  const FolderSolidIcon = {
    name: 'FolderSolidIcon',
    render() {
      return h('svg', {
        viewBox: '0 0 24 24',
        width: '1em',
        height: '1em',
        fill: 'none',
        'aria-hidden': 'true'
      }, [
        h('path', {
          d: 'M3.2 7.2A2.2 2.2 0 0 1 5.4 5h3.9l2.1 2.5h7.2a2.2 2.2 0 0 1 2.2 2.2v7.6a2.2 2.2 0 0 1-2.2 2.2H5.4a2.2 2.2 0 0 1-2.2-2.2z',
          fill: 'currentColor'
        })
      ])
    }
  }

  const HashSolidIcon = {
    name: 'HashSolidIcon',
    render() {
      return h('svg', {
        viewBox: '0 0 24 24',
        width: '1em',
        height: '1em',
        fill: 'none',
        'aria-hidden': 'true'
      }, [
        h('path', {
          d: 'M9.4 4.2 7.8 19.8M16.2 4.2l-1.6 15.6M4.8 9.4h14.6M4.2 14.6h14.6',
          stroke: 'currentColor',
          'stroke-width': 2.2,
          'stroke-linecap': 'round'
        })
      ])
    }
  }

  // 文章：按参考图只保留 归档 / 分类 / 标签，图标用实心风格
  const ARTICLE_MENU_ITEMS = [
    {label: '归档', to: '/archive', icon: ArchiveSolidIcon},
    {label: '分类', to: '/categories', icon: FolderSolidIcon},
    {label: '标签', to: '/tags', icon: HashSolidIcon}
  ]

  // 关于下拉：博客日志用「时钟 + 逆时针箭头」的历史回溯图标
  const HistoryIcon = {
    name: 'HistoryIcon',
    render() {
      return h('svg', {
        viewBox: '0 0 24 24',
        width: '1em',
        height: '1em',
        fill: 'none',
        stroke: 'currentColor',
        'stroke-width': 1.8,
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'aria-hidden': 'true'
      }, [
        h('path', {d: 'M3.6 12a8.4 8.4 0 1 0 2.5-6'}),
        h('path', {d: 'M3.6 4.6v4h4'}),
        h('path', {d: 'M12 8v4.2l2.8 1.7'})
      ])
    }
  }

  // 项目更新日志：卷轴 + 笔
  const ScrollPenIcon = {
    name: 'ScrollPenIcon',
    render() {
      return h('svg', {
        viewBox: '0 0 24 24',
        width: '1em',
        height: '1em',
        fill: 'none',
        stroke: 'currentColor',
        'stroke-width': 1.8,
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'aria-hidden': 'true'
      }, [
        h('path', {d: 'M6.4 3.8h8.2l3.6 3.8v12.6H6.4z'}),
        h('path', {d: 'M14.6 3.8v3.8h3.6'}),
        h('path', {d: 'm3.4 20.4.7-3 6.5-6.5 2.3 2.3-6.5 6.5z'}),
        h('path', {d: 'm9.9 12.1 2.3 2.3'})
      ])
    }
  }

  // 关于下拉：单人实心（关于我）与双人实心（友链），与关于页 tab 同一套图形
  const UserSolidIcon = {
    name: 'UserSolidIcon',
    render() {
      return h('svg', {
        viewBox: '0 0 24 24',
        width: '1em',
        height: '1em',
        fill: 'currentColor',
        'aria-hidden': 'true'
      }, [
        h('path', {
          d: 'M12 12.4c2.2 0 4-1.8 4-4s-1.8-4-4-4-4 1.8-4 4 1.8 4 4 4Zm0 1.8c-2.7 0-8 1.35-8 4v1.6h16v-1.6c0-2.65-5.3-4-8-4Z'
        })
      ])
    }
  }

  const UsersSolidIcon = {
    name: 'UsersSolidIcon',
    render() {
      return h('svg', {
        viewBox: '0 0 24 24',
        width: '1em',
        height: '1em',
        fill: 'currentColor',
        'aria-hidden': 'true'
      }, [
        h('path', {
          d: 'M16 11.2c1.6 0 2.9-1.3 2.9-2.9S17.6 5.4 16 5.4s-2.9 1.3-2.9 2.9 1.3 2.9 2.9 2.9Zm-8 0c1.6 0 2.9-1.3 2.9-2.9S9.6 5.4 8 5.4 5.1 6.7 5.1 8.3 6.4 11.2 8 11.2Zm0 1.7c-2.3 0-6.9 1.15-6.9 3.4v1.7h13.8v-1.7c0-2.25-4.6-3.4-6.9-3.4Zm8 0c-.3 0-.6 0-.95.05 1.15.8 1.95 1.9 1.95 3.35v1.7h5.9v-1.7c0-2.25-4.6-3.4-6.9-3.4Z'
        })
      ])
    }
  }

  const ABOUT_MENU_ITEMS = [
    {label: '关于我', to: '/about', icon: UserSolidIcon},
    {label: '友链', to: '/favorite', icon: UsersSolidIcon},
    {label: '打赏', to: '/sponsor', icon: Present},
    {label: '站点统计', to: '/analytics', icon: DataAnalysis},
    {label: '项目更新日志', to: '/changelog', icon: ScrollPenIcon},
    {label: '博客日志', to: '/blog-log', icon: HistoryIcon}
  ]

  const DYNAMIC_MENU_ITEMS = [
    {label: '随笔', to: '/jotting', icon: EditPen},
    {label: '相册', to: '/travel', icon: Picture},
    {label: '留言', to: '/message', icon: ChatDotRound}
  ]

  export default {
    name: 'HomeLayout',
    components: {
      NavDropdown,
      ThemeToggle
    },
    data() {
      return {
        grayNow: Date.now(),
        toolButton: false,
        mouseAnimation: false,
        scrollTop: 0,
        toolbarDrawer: false,
        mobile: false,
        // 站点 LOGO 加载失败时回退文字站名，避免出现破图
        logoError: false,
        resizeHandler: null,
        // 导航头像 40px 居中在 60px 导航里，底边在 50px；默认 offset 12 会让卡片顶边落在 62px，紧贴导航底边。
        // 改成 32px 后卡片顶边稳定落在 82px（导航底边 60px 以下 22px），卡片本体不再与导航重叠。
        navUserPopperOptions: {
          modifiers: [{ name: 'offset', options: { offset: [0, 32] } }]
        }
      }
    },
    mounted() {
      if (this.mouseAnimation) {
        mousedown()
      }
      window.addEventListener('scroll', this.onScrollPage)
      // 记录首次进入的页面访问
      this.reportVisit()
      // 定时刷新当前时间，使灰度在设定时间点自动生效
      this.grayTimer = setInterval(() => {
        this.grayNow = Date.now()
      }, 30000)
    },
    unmounted() {
      window.removeEventListener('scroll', this.onScrollPage)
      clearInterval(this.grayTimer)
      if (this.resizeHandler) {
        window.removeEventListener('resize', this.resizeHandler)
      }
      // 离开公开页时清除光环类，避免后台等页面残留动画
      document.documentElement.classList.remove('avatar-glow')
    },
    watch: {
      // 前台路由切换时上报一次访问（站点统计的页面排行、设备与来源依赖该数据）
      '$route.path'() {
        this.reportVisit()
      },
      // 站点 LOGO 地址变更后重新尝试加载图片
      siteLogo() {
        this.logoError = false
      },
      // 头像呼吸光环：后台开关默认关闭，只有开启时才在 html 上挂类
      avatarGlowEnabled: {
        immediate: true,
        handler(enabled) {
          document.documentElement.classList.toggle('avatar-glow', !!enabled)
        }
      },
      // 浏览器标签标题：后台可单独配置（web_info.tab_title），不与站点名称共用
      documentTitle: {
        immediate: true,
        handler(title) {
          if (!this.$common.isEmpty(title)) {
            document.title = title
          }
        }
      },
      scrollTop(scrollTop, oldScrollTop) {
        //如果滑动距离超过屏幕高度三分之一视为进入页面，背景改为白色
        let enter = scrollTop > window.innerHeight / 2
        const top = scrollTop - oldScrollTop < 0
        let isShow = scrollTop - window.innerHeight > 30
        this.toolButton = isShow
        if (isShow && !this.$common.mobile()) {
          if (window.innerHeight > 950) {
            $('.cd-top').css('top', '0')
          } else {
            $('.cd-top').css('top', window.innerHeight - 950 + 'px')
          }
        } else if (!isShow && !this.$common.mobile()) {
          $('.cd-top').css('top', '-900px')
        }

        //导航栏显示与颜色
        let toolbarStatus = {
          enter: enter,
          visible: top
        }
        this.$store.commit('changeToolbarStatus', toolbarStatus)
      }
    },
    created() {
      let toolbarStatus = {
        enter: false,
        visible: true
      }
      this.$store.commit('changeToolbarStatus', toolbarStatus)
      this.getWebInfo()
      this.getBossInfo()
      this.getSysConfig()
      this.getSortInfo()

      this.mobile = document.body.clientWidth < 1100

      this.resizeHandler = () => {
        let docWidth = document.body.clientWidth
        if (docWidth < 810) {
          this.mobile = true
        } else {
          this.mobile = false
        }
      }
      window.addEventListener('resize', this.resizeHandler)
    },
    computed: {
      // 本次导航是否跳过页面级入场动画（回退/前进、同页锚点）
      pageOpenInstant() {
        return pageOpenState.instant
      },
      // 导航项：默认展示全部并保持原有顺序，后台「导航管理」可调整显示与顺序
      navItems() {
        const meta = [
          {key: "home", title: "主页"},
          {key: "article", title: "文章"},
          {key: "anime", title: "追番"},
          {key: "dynamic", title: "动态"},
          {key: "about", title: "关于"}
        ];
        const config = {};
        const raw = this.$store.state.sysConfig.navConfig;
        if (!this.$common.isEmpty(raw)) {
          try {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) {
              parsed.forEach((item, index) => {
                if (item && item.key) {
                  config[item.key] = {visible: item.visible !== false, order: index};
                }
              });
            }
          } catch (e) {
            // 配置格式异常时回落到默认导航，不影响前台展示
          }
        }
        return meta.map((item) => {
          const saved = config[item.key];
          return {
            key: item.key,
            title: item.title,
            visible: saved ? saved.visible : true,
            order: saved ? saved.order : meta.length
          };
        }).sort((a, b) => a.order - b.order);
      },
      visibleNavKeys() {
        return this.navItems.filter((item) => item.visible).map((item) => item.key);
      },
      // 网站灰度：仅首页生效；到设定日期（当天 0 点）后自动变灰
      siteGrayActive() {
        if (this.$route.path !== "/") {
          return false;
        }
        const raw = this.$store.state.sysConfig.grayMode;
        if (this.$common.isEmpty(raw)) {
          return false;
        }
        let config;
        try {
          config = JSON.parse(raw);
        } catch (e) {
          return false;
        }
        if (!config.enabled) {
          return false;
        }
        let dates = [];
        if (Array.isArray(config.dates)) {
          // 保存的是「月-日」，每年循环生效
          dates = config.dates.map((date) => String(date).slice(-5));
        } else if (config.startTime) {
          // 兼容旧版单日期配置
          dates = [String(config.startTime).slice(-5)];
        }
        // 未设置日期时，开启开关即立即灰度
        if (!dates.length) {
          return true;
        }
        const now = new Date(this.grayNow);
        const pad = (value) => (value < 10 ? "0" + value : "" + value);
        const today = pad(now.getMonth() + 1) + "-" + pad(now.getDate());
        return dates.indexOf(today) > -1;
      },
      // 头像呼吸光环开关（sys_config.avatarGlowEnabled）：缺省/关闭时为 false
      avatarGlowEnabled() {
        const raw = this.$store.state.sysConfig.avatarGlowEnabled
        return raw === '1' || raw === 'true' || raw === true
      },
      toolbar() {
        return this.$store.state.toolbar
      },
      // 导航下拉：触发器图标与面板项
      icons() {
        return NAV_ICONS
      },
      articleMenuItems() {
        return ARTICLE_MENU_ITEMS
      },
      aboutMenuItems() {
        return ABOUT_MENU_ITEMS
      },
      dynamicMenuItems() {
        return DYNAMIC_MENU_ITEMS
      },
      // 「动态」激活态：随笔 / 相册 / 留言 任一页面
      dynamicActive() {
        return ['/travel', '/jotting', '/message'].includes(this.$route.path)
      },
      // 导航头像：优先当前用户，缺失时用站点头像兜底
      userAvatar() {
        const user = this.$store.state.currentUser
        if (!this.$common.isEmpty(user) && !this.$common.isEmpty(user.avatar)) {
          return user.avatar
        }
        return this.$store.state.webInfo.avatar
      },
      // 头像认证角标：仅主理人 / AI 站长 / 管理员显示
      userHasIdentity() {
        const user = this.$store.state.currentUser
        if (this.$common.isEmpty(user)) {
          return false
        }
        const type = Number(user.userType)
        return user.isBoss === true || type === 1 || type === 2 || type === 5
      },
      // 导航里代表当前登录用户（头像 alt 等）
      userName() {
        const user = this.$store.state.currentUser
        return this.$common.isEmpty(user.username) ? '用户' : user.username
      },
      // 浏览器标签页标题：独立配置 tabTitle → 站点名称 webName → 通用名
      documentTitle() {
        const info = this.$store.state.webInfo || {}
        if (!this.$common.isEmpty(info.tabTitle)) {
          return info.tabTitle
        }
        if (!this.$common.isEmpty(info.webName)) {
          return info.webName
        }
        return '博客'
      },
      // 导航标题：只取站点字段（网站名称 webName，缺失退回网站标题 webTitle，再退回通用名称）
      siteName() {
        const info = this.$store.state.webInfo || {}
        if (!this.$common.isEmpty(info.webName)) {
          return info.webName
        }
        const title = Array.isArray(info.webTitle) ? info.webTitle.join('') : info.webTitle
        return this.$common.isEmpty(title) ? '博客' : title
      },
      // 站点 LOGO：优先用后台「网站设置 → 站点 LOGO」（web_info.logo），与头像 avatar 分开存储；
      // 未配置时回退到内置 C 版品牌图标，按当前主题取深/浅两版；图片加载失败仍由 logoError 回退文字站名
      siteLogo() {
        const info = this.$store.state.webInfo || {}
        if (!this.$common.isEmpty(info.logo)) {
          return String(info.logo).trim()
        }
        return this.$store.state.theme === 'dark'
          ? '/images/brand/jsnws-icon-dark.svg'
          : '/images/brand/jsnws-icon-light.svg'
      },
      // 卡片里代表站长（站点主人）：取站长账号（sys_user 中 bossUserId 对应用户）的名称，
      // 站点信息表 web_info 没有这个字段；缺失时兜底站点名称，绝不回落到登录用户名
      ownerName() {
        const boss = this.$store.state.bossInfo || {}
        if (!this.$common.isEmpty(boss.username)) {
          return boss.username
        }
        return this.siteName
      },
      // 后台入口判断：仅管理员可见，既要有管理员信息，也要有本地 adminToken
      adminLoggedIn() {
        return !this.$common.isEmpty(this.$store.state.currentAdmin) && Boolean(localStorage.getItem('adminToken'))
      }
    },
    methods: {
      smallMenu(data) {
        this.$router.push(data)
        this.toolbarDrawer = false
      },

      smallMenuLogout() {
        this.logout()
        this.toolbarDrawer = false
      },

      goAdmin() {
        // 仅此处放行：先写入本次导航进入标记，再进入后台
        allowAdminEntry()
        // 必须同标签页跳转：sessionStorage 不会随 window.open 的新标签页继承，
        // 新标签页里拿不到进入标记会被路由守卫打回首页（表现为点了没反应）
        this.toolbarDrawer = false
        this.$router.push({path: '/admin'})
      },

      // 头像卡片展开/收起：展开时导航小头像「飞入」卡片头像位，收起时飞回
      onAvatarCardVisibleChange(visible) {
        if (visible) {
          // 重新打开时卡片内容还挂在 body 上，先藏住卡片头像，等飞行起点写好后由 flyAvatarToCard 显形
          this.setCardAvatarVisible(false)
          this.tryFlyAvatarToCard(0)
        } else {
          this.flyAvatarBack()
        }
      },

      isReducedMotion() {
        return Boolean(window.matchMedia) && window.matchMedia('(prefers-reduced-motion: reduce)').matches
      },

      getNavAvatarEl() {
        const ref = this.$refs.navAvatar
        return ref ? (ref.$el || ref) : null
      },

      clearAvatarTimer() {
        if (this._avatarTimer) {
          window.clearTimeout(this._avatarTimer)
          this._avatarTimer = null
        }
      },

      // 导航头像淡出/淡入：时长与飞行保持一致，避免重影或空档
      fadeNavAvatar(opacity, duration, easing) {
        const trigger = this.getNavAvatarEl()
        if (!trigger) {
          return
        }
        trigger.style.transition = `opacity ${duration}s ${easing}`
        trigger.style.opacity = opacity
      },

      clearNavAvatarTransition(delay) {
        window.setTimeout(() => {
          const trigger = this.getNavAvatarEl()
          if (trigger) {
            trigger.style.transition = ''
          }
        }, delay)
      },

      // 头部 popper 刚挂载时尺寸是 0（Element Plus 还有缩放展开动画），等卡片头像布局稳定再飞
      tryFlyAvatarToCard(attempt) {
        if (this.isReducedMotion()) {
          return
        }
        this.$nextTick(() => {
          window.requestAnimationFrame(() => {
            if (!this.flyAvatarToCard()) {
              if (attempt < 20) {
                this.tryFlyAvatarToCard(attempt + 1)
              } else {
                // 一直没量到稳定尺寸：兜底显示卡片头像，避免它被默认样式藏住
                this.setCardAvatarVisible()
              }
            }
          })
        })
      },

      setCardAvatarVisible(visible = true) {
        const target = document.querySelector('.nav-user-popper .nav-user-avatar')
        if (target) {
          target.style.visibility = visible ? 'visible' : ''
        }
      },

      // 量出导航头像与卡片头像的中心点/尺寸差，只用于 transform，不触发重排
      getAvatarFlight() {
        const trigger = this.getNavAvatarEl()
        const target = document.querySelector('.nav-user-popper .nav-user-avatar')
        if (!trigger || !target || typeof trigger.getBoundingClientRect !== 'function') {
          return null
        }
        const from = trigger.getBoundingClientRect()
        const to = target.getBoundingClientRect()
        // 宽度为 0 或仍处于 popper 缩放过程中（rect 宽和布局宽不一致）时不飞
        if (!from.width || !to.width || Math.abs(to.width - target.offsetWidth) > 1) {
          return null
        }
        return {
          target,
          scale: from.width / to.width,
          dx: (from.left + from.width / 2) - (to.left + to.width / 2),
          dy: (from.top + from.height / 2) - (to.top + to.height / 2)
        }
      },

      flyAvatarToCard() {
        if (this.isReducedMotion()) {
          return true
        }
        const flight = this.getAvatarFlight()
        if (!flight) {
          return false
        }
        const {target, scale, dx, dy} = flight

        // 起点：卡片头像先叠在导航头像处；导航头像用同一缓动淡出，避免两张头像同时在场
        this.clearAvatarTimer()
        target.style.transition = 'none'
        target.style.transformOrigin = 'center center'
        // 先在导航头像位置显形（此时它与导航头像完全重合），再飞向卡片位置
        target.style.visibility = 'visible'
        target.style.transform = `translate(${dx}px, ${dy}px) scale(${scale})`
        this.fadeNavAvatar('0', 0.18, 'cubic-bezier(0.22, 0.61, 0.36, 1)')
        target.getBoundingClientRect()

        // 下一帧再写终点，保证 transition 真正触发（同一帧写起终点会被合并成一步）
        window.requestAnimationFrame(() => {
          target.style.transition = 'transform 0.34s cubic-bezier(0.22, 0.61, 0.36, 1)'
          target.style.transform = 'translate(0, 0) scale(1)'
          this._avatarTimer = window.setTimeout(() => {
            // 飞行结束后清掉内联过渡与 transform，避免覆盖 CSS 里的 hover 过渡/变换
            target.style.transition = ''
            target.style.transform = ''
            // 卡片展开期间导航头像保持隐藏，否则会和卡片头像同时在场（两处都看到头像）；收起时再淡入
            this.clearNavAvatarTransition(0)
            // 记录卡片头像的最终位置，收起时用克隆头像飞回
            this._cardAvatarRect = target.getBoundingClientRect()
          }, 340)
        })
        return true
      },

      // 收起：popper 隐藏时卡片头像已不可见，用导航头像的克隆从卡片位置飞回，结束后恢复原头像
      flyAvatarBack() {
        const trigger = this.getNavAvatarEl()
        const cardRect = this._cardAvatarRect
        if (this.isReducedMotion() || !trigger || !cardRect) {
          if (trigger) {
            trigger.style.transition = ''
            trigger.style.opacity = ''
          }
          return
        }
        this.clearAvatarTimer()
        const from = trigger.getBoundingClientRect()
        const clone = trigger.cloneNode(true)
        clone.style.cssText = `position:fixed;left:${cardRect.left}px;top:${cardRect.top}px;width:${cardRect.width}px;height:${cardRect.height}px;margin:0;pointer-events:none;z-index:3000;transform-origin:center center;transition:transform 0.22s cubic-bezier(0.4, 0, 1, 1)`
        document.body.appendChild(clone)
        // 与飞回同参数淡出导航头像
        this.fadeNavAvatar('0', 0.18, 'cubic-bezier(0.4, 0, 1, 1)')
        const dx = (from.left + from.width / 2) - (cardRect.left + cardRect.width / 2)
        const dy = (from.top + from.height / 2) - (cardRect.top + cardRect.height / 2)
        const scale = from.width / cardRect.width
        window.requestAnimationFrame(() => {
          clone.style.transform = `translate(${dx}px, ${dy}px) scale(${scale})`
        })
        this._avatarTimer = window.setTimeout(() => {
          // 飞回到位（克隆正压在导航头像上）后先淡入导航头像，等它被克隆完全盖住再移除克隆，收尾没有空档
          this.fadeNavAvatar('1', 0.12, 'ease')
          window.setTimeout(() => {
            clone.remove()
            this.clearNavAvatarTransition(160)
          }, 140)
        }, 240)
      },

      logout() {
        this.$http.get(this.$constant.baseURL + '/user/logout')
          .then((res) => {
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: 'error'
            })
          })
        this.$store.commit('loadCurrentUser', {})
        localStorage.removeItem('userToken')
        // 管理员账号从 /user 登录时共用同一 token，退出时一并清理后台登录态
        this.$store.commit('loadCurrentAdmin', {})
        localStorage.removeItem('adminToken')
        clearAdminEntry()
        this.$router.push({ path: '/' })
      },
      // 访问上报：路径 + 站外来源主机名（直接访问留空，由服务端记为「直接」）
      reportVisit() {
        const path = this.$route.path
        if (!path || path.indexOf('/admin') === 0) {
          return
        }
        let referrer = ''
        if (document.referrer) {
          try {
            const host = new URL(document.referrer).host
            if (host && host !== window.location.host) {
              referrer = host
            }
          } catch (e) {
            referrer = ''
          }
        }
        this.$http.post(this.$constant.baseURL + '/webInfo/recordVisit', {path, referrer})
          .catch(() => {
          })
      },
      getWebInfo() {
        this.$http.get(this.$constant.baseURL + '/webInfo/getWebInfo')
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.$store.commit('loadWebInfo', res.data)
              localStorage.setItem('defaultStoreType', res.data.defaultStoreType)
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: 'error'
            })
          })
      },
      // 站长信息（头像卡片上的站长名称）：站点信息里没有站长姓名字段，取站长账号的昵称
      getBossInfo() {
        this.$http.get(this.$constant.baseURL + '/webInfo/getBossInfo')
          .then((res) => {
            this.$store.commit('loadBossInfo', this.$common.isEmpty(res.data) ? {} : res.data)
          })
          .catch(() => {
            // 接口不可用时保持空对象，卡片名走站点名称兜底
            this.$store.commit('loadBossInfo', {})
          })
      },
      getSysConfig() {
        this.$http.get(this.$constant.baseURL + '/sysConfig/listSysConfig')
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.$store.commit('loadSysConfig', res.data)
              this.buildCssPicture()
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: 'error'
            })
          })
      },
      buildCssPicture() {
        let root = document.querySelector(':root')
        let webStaticResourcePrefix = this.$store.state.sysConfig['webStaticResourcePrefix']
        root.style.setProperty('--commentURL', 'url(' + webStaticResourcePrefix + 'assets/commentURL.png)')
        root.style.setProperty('--springBg', 'url(' + webStaticResourcePrefix + 'assets/springBg.png)')
        root.style.setProperty('--admireImage', 'url(' + webStaticResourcePrefix + 'assets/admireImage.jpg)')
        root.style.setProperty('--toTop', 'url(' + webStaticResourcePrefix + 'assets/toTop.png)')
        // 首页首屏两层波浪改用项目内本地 SVG（color.css 中按深浅模式切换），不再从静态资源前缀取图
        root.style.setProperty('--backgroundPicture', 'url(' + webStaticResourcePrefix + 'assets/backgroundPicture.jpg)')
        root.style.setProperty('--toolbar', 'url(' + webStaticResourcePrefix + 'assets/toolbar.jpg)')
        // 全站字体已改为自托管的 LXGW WenKai Screen（见 public/index.html 与 index.css 的 --font-sans），
        // 不再从静态资源加载旧的 King-font，避免多下载一份用不到的字库
      },
      getSortInfo() {
        this.$http.get(this.$constant.baseURL + '/webInfo/getSortInfo')
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              this.$store.commit('loadSortInfo', res.data)
            }
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: 'error'
            })
          })
      },
      toTop() {
        window.scrollTo({
          top: 0,
          behavior: 'smooth'
        })
      },
      onScrollPage() {
        this.scrollTop = document.documentElement.scrollTop || document.body.scrollTop
      },
      changeMouseAnimation() {
        this.mouseAnimation = !this.mouseAnimation
        if (this.mouseAnimation) {
          this.$nextTick(() => {
            mousedown()
          })
        }
      }
    }
  }
</script>

<style scoped>

  .toolbar-content {
    width: 100%;
    height: 60px;
    color: var(--white);
    /* 固定位置，不随滚动条滚动 */
    position: fixed;
    z-index: 100;
    /* 禁止选中文字 */
    user-select: none;
    transition: color 0.3s ease-in-out;
  }

  /* 独立背景层：与移动抽屉同一张 --toolbar 图，cover 居中，叠主题遮罩保证白字对比度 */
  .toolbar-content::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(var(--heroMask), var(--heroMask)),
                var(--toolbar) center center / cover no-repeat;
    z-index: 0;
    pointer-events: none;
  }

  /* 内容层压在背景层之上 */
  .toolbar-content > * {
    position: relative;
    z-index: 1;
  }

  .toolbar-title {
    margin-left: 30px;
    cursor: pointer;
    display: flex;
    align-items: center;
    /* 用文字投影代替底色，保证白字压在亮色内容上仍可读 */
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55), 0 0 8px rgba(0, 0, 0, 0.3);
  }

  /* 顶部站点 LOGO：高度固定、宽度等比，contain 保证不裁切不拉伸；flex 不参与压缩 */
  .toolbar-logo {
    display: block;
    height: 32px;
    width: auto;
    max-width: 180px;
    max-height: 32px;
    flex: 0 0 auto;
    object-fit: contain;
    object-position: center;
    overflow: visible;
    border-radius: 8px;
    filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.35));
  }

  /* 顶栏是半透明遮罩，首页横幅又是深色图：浅色主题的深色图标下半部会和深底糊在一起，
     看起来像被裁掉；加一圈浅色描边把整枚图形勾出来（移动端有白色药丸底，不参与） */
  @media screen and (min-width: 768px) {
    html:not(.dark) .toolbar-logo {
      filter: drop-shadow(0 0 1px rgba(255, 255, 255, 0.95))
              drop-shadow(0 0 2px rgba(255, 255, 255, 0.6))
              drop-shadow(0 1px 4px rgba(0, 0, 0, 0.4));
    }
  }

  /* 移动端（与导航折叠同一判断）LOGO 高度降为 26px，宽度 auto 等比不变形 */
  .toolbar-logo--mobile {
    height: 26px;
  }

  .toolbar-mobile-menu {
    font-size: 30px;
    margin-right: 15px;
    cursor: pointer;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55), 0 0 8px rgba(0, 0, 0, 0.3);
  }

  .scroll-menu {
    margin: 0 25px 0 0;
    display: flex;
    justify-content: flex-end;
    padding: 0;
    /* 导航全透明，白字靠投影提高可读性 */
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55), 0 0 8px rgba(0, 0, 0, 0.3);
  }

  .scroll-menu li {
    list-style: none;
    margin: 0 12px;
    font-size: 17px;
    height: 60px;
    line-height: 60px;
    position: relative;
    cursor: pointer;
  }

  /* 追番图标：深底圆角矩形 + 白色播放三角，跟随文字颜色 */
  .anime-nav-icon {
    width: 18px;
    height: 18px;
    display: block;
  }

  /* 后台入口图标：显示器轮廓，尺寸与 14px 文字等高，flex 居中即与文字居中对齐 */
  .admin-nav-icon {
    width: 16px;
    height: 16px;
    display: block;
    flex-shrink: 0;
  }

  .article-menu-cell,
  .login-cell,
  .avatar-cell {
    display: flex;
    align-items: center;
    height: 60px;
  }

  .scroll-menu .nav-pill-cell {
    display: flex;
    align-items: center;
    height: 60px;
  }

  .small-menu .nav-pill-cell {
    padding: 3px 0;
    line-height: normal;
  }

  /* 导航项统一规格：透明底 + 12px 圆角 + 14px 横向内边距，白字；悬停/激活用白色透明底区分 */
  .nav-pill {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 36px;
    padding: 0 14px;
    border-radius: 12px;
    background: transparent;
    color: #ffffff;
    font-size: 14px;
    font-weight: 500;
    letter-spacing: 1px;
    line-height: 1;
    cursor: pointer;
    transition: background 0.2s ease, color 0.2s ease;
  }

  .nav-pill i,
  .nav-pill svg {
    font-size: 15px;
    color: #ffffff;
  }

  .nav-pill:hover {
    background: rgba(255, 255, 255, 0.16);
  }

  .nav-pill.is-active {
    background: rgba(255, 255, 255, 0.26);
    color: #ffffff;
  }

  .nav-pill.is-active i,
  .nav-pill.is-active svg {
    color: #ffffff;
  }

  .nav-pill.is-active:hover {
    background: rgba(255, 255, 255, 0.34);
  }

  /* 与相邻菜单项间距 16px（poetize 实测）：相邻 li 自带 12px 右间距，这里补 4px */
  .login-cell {
    margin: 0 0 0 4px;
  }

  .login-circle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    padding: 0;
    border: none;
    border-radius: 50%;
    background: var(--themeBackground);
    color: #ffffff;
    font-size: 15px;
    font-weight: 400;
    line-height: 1;
    cursor: pointer;
    transition: background 0.2s ease, box-shadow 0.2s ease;
  }

  .login-circle:hover {
    background: #337ecc;
    /* 与头像 hover 同一套「浅色外发光」语言 */
    box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.16);
  }

  /* 导航头像：圆形 + 约 2px 白色描边 + 柔和外发光（视频关键帧：白环清晰、外沿有轻微白光） */
  .nav-avatar,
  .drawer-user-avatar {
    background: transparent;
    border: 2px solid rgba(255, 255, 255, 0.85);
    box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.14), 0 4px 14px rgba(0, 0, 0, 0.18);
    cursor: pointer;
    user-select: none;
    overflow: hidden;
    transition: border-color 0.22s ease, box-shadow 0.22s ease;
  }

  .nav-avatar:hover,
  .drawer-user-avatar:hover {
    border-color: #ffffff;
    box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.22), 0 4px 16px rgba(0, 0, 0, 0.2);
  }

  /* 悬停缩放放在内部 img 上：触发器盒子不变，不会把鼠标"甩出"导致 hover 抖动或消失 */
  .nav-avatar :deep(img),
  .drawer-user-avatar :deep(img) {
    width: 100%;
    height: 100%;
    display: block;
    transition: transform 0.22s cubic-bezier(0.22, 0.61, 0.36, 1);
  }

  .nav-avatar:hover :deep(img),
  .drawer-user-avatar:hover :deep(img) {
    transform: scale(1.05);
  }

  .drawer-article-menu {
    padding: 6px 0;
    line-height: normal;
  }

  .drawer-login {
    display: flex;
    align-items: center;
    padding: 6px 0;
    line-height: normal;
  }

  /* 头像悬停卡片：按参考图 —— 宽 300 / 圆角 12 / 头像探出卡片顶部 / 条目 46 高 */
  .nav-user-card {
    width: 300px;
    padding: 0 0 12px;
    background: var(--panelBg);
    border-radius: 12px;
    /* 只淡入，不做位移/缩放，避免干扰共享元素飞行的起止位置测量 */
    animation: navUserCardFade 0.16s ease-out both;
  }

  @keyframes navUserCardFade {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }

  .nav-user-head {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    /* 顶部留出 32px 给探出卡片的大头像 */
    padding: 40px 0 8px;
  }

  .nav-user-avatar {
    position: absolute;
    top: -32px;
    left: 50%;
    margin-left: -32px;
    background: var(--panelBg);
    box-shadow: 0 0 0 3px var(--panelBg), 0 0 0 4px rgba(31, 31, 31, 0.08);
  }

  .nav-user-name {
    margin-top: 0;
    max-width: 220px;
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
    color: var(--panelText);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .nav-user-item {
    display: flex;
    align-items: center;
    gap: 12px;
    height: 46px;
    padding: 0 22px;
    font-size: 15px;
    color: var(--panelTextSecondary);
    cursor: pointer;
    transition: background 0.2s ease, color 0.2s ease;
  }

  .nav-user-item:hover {
    background: var(--panelHoverBg);
    color: var(--panelText);
  }

  .nav-user-icon {
    font-size: 18px;
    color: var(--panelTextSecondary);
    transition: color 0.2s ease;
  }

  .nav-user-item:hover .nav-user-icon {
    color: var(--panelText);
  }

  .nav-user-arrow {
    margin-left: auto;
    font-size: 14px;
    color: var(--greyFont);
  }

  .nav-user-divider {
    margin: 6px 22px;
    border-top: 1px solid var(--panelDivider);
  }

  .drawer-user-head {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px 0 16px;
  }

  .el-dropdown {
    font-size: unset;
    color: unset;
  }

  .el-dropdown-menu {
    padding: 5px 0;
  }

  .el-dropdown-menu__item {
    font-size: unset;
  }

  .toolButton {
    position: fixed;
    right: 3vh;
    bottom: 3vh;
    animation: slide-bottom 0.5s ease-in-out both;
    z-index: 100;
    cursor: pointer;
    font-size: 25px;
    width: 30px;
  }

  .my-setting {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-around;
    cursor: pointer;
    font-size: 20px;
  }

  .my-setting i {
    padding: 5px;
  }

  .my-setting i:hover {
    color: var(--themeBackground);
  }

  .cd-top {
    background: var(--toTop) no-repeat center;
    position: fixed;
    right: 5vh;
    top: -900px;
    z-index: 99;
    width: 70px;
    height: 900px;
    background-size: contain;
    transition: all 0.5s ease-in-out;
    cursor: pointer;
  }

  .backTop {
    transition: all 0.3s ease-in;
    position: relative;
    top: 0;
    left: -13px;
  }

  .backTop:hover {
    top: -10px;
  }

  /* 回到顶部图标是黑色描边图，黑夜模式下反色保证可见 */
  html.dark .backTop svg path {
    fill: #e6e6e6;
  }

  #outerImg {
    position: fixed;
    top: 0;
    left: 0;
    background: rgba(0, 0, 0, 0.6);
    /* 与 fancybox 灯箱同层级：必须盖住头部导航（z-index:100）与侧栏 */
    z-index: 99999;
    width: 100%;
    height: 100%;
    display: none;
  }

  @media screen and (max-width: 400px) {
    .toolButton {
      right: 0.5vh;
    }
  }

  /* 尊重系统「减少动态效果」：关闭悬停过渡与头像飞行，卡片保留普通淡入 */
  @media (prefers-reduced-motion: reduce) {
    .login-circle,
    .nav-avatar,
    .drawer-user-avatar,
    .nav-avatar :deep(img),
    .drawer-user-avatar :deep(img) {
      transition: none;
    }

    .nav-user-avatar {
      transition: none !important;
      transform: none !important;
    }

    .nav-avatar:hover :deep(img),
    .drawer-user-avatar:hover :deep(img) {
      transform: none;
    }
  }
</style>

<style>
  /* 头像下拉卡片 teleport 到 body，popper 外框需用非 scoped 样式重置 */
  .nav-user-popper.el-popper {
    padding: 0;
    border: none;
    border-radius: 12px;
    background: var(--panelBg);
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.14);
  }

  .nav-user-popper .el-popper__arrow {
    display: none;
  }

  /* 卡片头像要探出卡片顶部：Element Plus 下拉内容外层的 el-scrollbar（overflow:hidden）会裁掉它 */
  .nav-user-popper .el-scrollbar {
    overflow: visible !important;
  }

  .nav-user-popper .el-scrollbar__wrap {
    overflow: visible !important;
  }

  .nav-user-popper .el-scrollbar__bar {
    display: none;
  }

  /* 关掉 Element Plus 自带的缩放展开：整卡缩放会盖住「头像飞入」的位移，也导致测量到缩放中的尺寸 */
  .nav-user-popper.el-zoom-in-top-enter-active,
  .nav-user-popper.el-zoom-in-top-leave-active {
    transition: none !important;
  }

  .nav-user-popper.el-zoom-in-top-enter-from,
  .nav-user-popper.el-zoom-in-top-leave-to {
    opacity: 1 !important;
    transform: none !important;
  }

  /* 共享元素飞行开始前先藏住卡片头像：否则卡片挂载的第一帧会和导航头像同时出现（开局重影） */
  @media (prefers-reduced-motion: no-preference) {
    .nav-user-popper .nav-user-avatar {
      visibility: hidden;
    }
  }

  /* 网站灰度遮罩：用 backdrop-filter 去色，避免 filter 影响页面内 fixed 背景定位 */
  .site-gray-overlay {
    position: fixed;
    inset: 0;
    z-index: 2147483000;
    pointer-events: none;
    -webkit-backdrop-filter: grayscale(1);
    backdrop-filter: grayscale(1);
  }
</style>
