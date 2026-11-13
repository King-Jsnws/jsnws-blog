<template>
  <div>
    <!-- el过渡动画 -->
    <transition name="el-fade-in-linear">
      <!-- 导航栏 -->
      <div v-show="toolbar.visible || ($common.mobile() || mobile)"
           class="toolbar-content myBetween">
        <!-- 网站名称（导航只显示站点名称，与头像卡片上的站长名称分开） -->
        <div class="toolbar-title">
          <h2 @click="$router.push({path: '/'})">{{ siteName }}</h2>
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
            <li class="nav-pill-cell" @click="$router.push({path: '/'})">
              <div class="nav-pill" :class="{ 'is-active': $route.path === '/' }">
                <i class="fa fa-home" aria-hidden="true"></i> <span>主页</span>
              </div>
            </li>

            <!-- 文章：文章列表 / 图谱 / 归档 -->
            <li class="article-menu-cell">
              <NavDropdown label="文章"
                           :trigger-icon="icons.list"
                           :items="articleMenuItems"></NavDropdown>
            </li>

            <!-- 追番 -->
            <li class="nav-pill-cell" @click="$router.push({path: '/anime'})">
              <div class="nav-pill" :class="{ 'is-active': $route.path === '/anime' }">
                <svg class="anime-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                  <rect x="1.5" y="5" width="21" height="14" rx="2.5" fill="currentColor"></rect>
                  <path d="M9.6 8.8 16 12l-6.4 3.2V8.8Z" fill="#1f1f1f"></path>
                </svg>
                <span>追番</span>
              </div>
            </li>

            <!-- 动态：随笔 / 相册 / 留言 -->
            <li class="article-menu-cell">
              <NavDropdown label="动态"
                           :trigger-icon="icons.dynamic"
                           :items="dynamicMenuItems"
                           :active="dynamicActive"></NavDropdown>
            </li>

            <!-- 关于：关于我 / 友链 / 打赏 -->
            <li class="article-menu-cell">
              <NavDropdown label="关于"
                           :trigger-icon="icons.info"
                           :items="aboutMenuItems"></NavDropdown>
            </li>

            <!-- 后台（仅管理员登录后显示） -->
            <li v-if="adminLoggedIn" class="nav-pill-cell" @click="goAdmin()">
              <div class="nav-pill">
                <span>💻️</span> <span>后台</span>
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
                <el-avatar ref="navAvatar" class="nav-avatar" :size="40" :src="userAvatar"
                           :alt="userName + '的头像'" fit="inherit"></el-avatar>

                <template #dropdown>
                  <div class="nav-user-card">
                    <div class="nav-user-head">
                      <el-avatar class="nav-user-avatar" :size="64" :src="userAvatar"></el-avatar>
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

    <div id="main-container">
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
               size="65%"
               class="toolbarDrawer"
               title="欢迎光临"
               direction="ltr">
      <div>
        <ul class="small-menu">
          <li class="nav-pill-cell" @click="smallMenu({path: '/'})">
            <div class="nav-pill" :class="{ 'is-active': $route.path === '/' }">
              <i class="fa fa-home" aria-hidden="true"></i> <span>主页</span>
            </div>
          </li>

          <li class="drawer-article-menu">
            <NavDropdown label="文章" inline
                         :trigger-icon="icons.list"
                         :items="articleMenuItems"></NavDropdown>
          </li>

          <!-- 追番 -->
          <li class="nav-pill-cell" @click="smallMenu({path: '/anime'})">
            <div class="nav-pill" :class="{ 'is-active': $route.path === '/anime' }">
              <svg class="anime-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                <rect x="1.5" y="5" width="21" height="14" rx="2.5" fill="currentColor"></rect>
                <path d="M9.6 8.8 16 12l-6.4 3.2V8.8Z" fill="#1f1f1f"></path>
              </svg>
              <span>追番</span>
            </div>
          </li>

          <!-- 动态：随笔 / 相册 / 留言 -->
          <li class="drawer-article-menu">
            <NavDropdown label="动态" inline
                         :trigger-icon="icons.dynamic"
                         :items="dynamicMenuItems"
                         :active="dynamicActive"></NavDropdown>
          </li>

          <!-- 关于：关于我 / 友链 / 打赏 -->
          <li class="drawer-article-menu">
            <NavDropdown label="关于" inline
                         :trigger-icon="icons.info"
                         :items="aboutMenuItems"></NavDropdown>
          </li>

          <!-- 后台（仅管理员登录后显示） -->
          <li v-if="adminLoggedIn" class="nav-pill-cell" @click="goAdmin()">
            <div class="nav-pill">
              <span>💻️</span> <span>后台</span>
            </div>
          </li>

          <template v-if="$common.isEmpty($store.state.currentUser)">
            <li class="drawer-login" @click="smallMenu({path: '/user'})">
              <span class="login-circle">登录</span>
            </li>
          </template>
          <template v-else>
            <li class="drawer-user-head">
              <el-avatar class="drawer-user-avatar" :size="56" :src="userAvatar"
                         :alt="userName + '的头像'" fit="inherit"></el-avatar>
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
  import { allowAdminEntry, clearAdminEntry } from '../utils/adminEntry'
  import NavDropdown from '../components/common/NavDropdown.vue'
  import ThemeToggle from '../components/common/ThemeToggle.vue'
  import { ArrowRight, Box, Camera, ChatDotRound, Connection, EditPen, InfoFilled, List, Picture, Present, Share, SwitchButton, User } from '@element-plus/icons-vue'

  // 下拉项与图标放在模块作用域，避免图标组件被 Vue 的响应式包装
  const NAV_ICONS = {
    list: List,
    info: InfoFilled,
    dynamic: Camera
  }

  const ARTICLE_MENU_ITEMS = [
    {label: '文章列表', to: '/sort', icon: List},
    {label: '图谱', to: '/graph', icon: Share},
    {label: '归档', to: '/archive', icon: Box}
  ]

  const ABOUT_MENU_ITEMS = [
    {label: '关于我', to: '/about', icon: User},
    {label: '友链', to: '/favorite', icon: Connection},
    {label: '打赏', to: '/sponsor', icon: Present}
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
        toolButton: false,
        mouseAnimation: false,
        scrollTop: 0,
        toolbarDrawer: false,
        mobile: false,
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
    },
    unmounted() {
      window.removeEventListener('scroll', this.onScrollPage)
      if (this.resizeHandler) {
        window.removeEventListener('resize', this.resizeHandler)
      }
    },
    watch: {
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
      // 导航里代表当前登录用户（头像 alt 等）
      userName() {
        const user = this.$store.state.currentUser
        return this.$common.isEmpty(user.username) ? '用户' : user.username
      },
      // 导航标题：只取站点字段（网站名称 webName，缺失退回网站标题 webTitle）
      siteName() {
        const info = this.$store.state.webInfo || {}
        if (!this.$common.isEmpty(info.webName)) {
          return info.webName
        }
        const title = Array.isArray(info.webTitle) ? info.webTitle.join('') : info.webTitle
        return this.$common.isEmpty(title) ? 'King' : title
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
        // 仅此处放行：先写入本次导航进入标记，再打开后台
        allowAdminEntry()
        // 跟随当前访问地址，避免 dev（8080）/ 生产环境的端口差异
        window.open(window.location.origin + '/admin')
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
        root.style.setProperty('--bannerWave1', 'url(' + webStaticResourcePrefix + 'assets/bannerWave1.png) repeat-x')
        root.style.setProperty('--bannerWave2', 'url(' + webStaticResourcePrefix + 'assets/bannerWave2.png) repeat-x')
        root.style.setProperty('--backgroundPicture', 'url(' + webStaticResourcePrefix + 'assets/backgroundPicture.jpg)')
        root.style.setProperty('--toolbar', 'url(' + webStaticResourcePrefix + 'assets/toolbar.jpg)')
        const font = new FontFace('King-font', 'url(' + webStaticResourcePrefix + 'assets/font.woff2)')
        font.load()
        document.fonts.add(font)
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
    /* 用文字投影代替底色，保证白字压在亮色内容上仍可读 */
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55), 0 0 8px rgba(0, 0, 0, 0.3);
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
    background: #409eff;
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
    z-index: 10;
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
</style>
