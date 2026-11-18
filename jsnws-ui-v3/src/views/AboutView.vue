<template>
  <div>
    <!-- 两句诗 -->
    <div class="my-animation-slide-top">
      <TwoPoem :isHitokoto="false"></TwoPoem>
    </div>

    <div style="background: var(--ui-page-bg)" class="my-animation-slide-bottom">
      <div class="about-container">
        <!-- 左侧整列：复用首页侧栏（个人卡、音乐等）+ 分类与标签索引；日历按参考站放在右栏 -->
        <aside class="about-aside">
          <!-- 访客与浏览量并回个人卡内部，由 SiteInfoCard 按传入的真实数据渲染 -->
          <MyAside hide-calendar
                   @selectSort="selectSort"
                   @selectArticle="selectArticle"></MyAside>
        </aside>

        <main class="about-main">
          <!-- 个人头图 -->
          <section class="about-card about-profile">
            <el-avatar class="profile-avatar" shape="circle" :src="ownerAvatar"></el-avatar>

            <div class="profile-body">
              <div class="profile-kicker">
                <span class="profile-kicker-dot" aria-hidden="true"></span>
                <span>PUBLIC PROFILE</span>
                <template v-if="siteHost">
                  <span class="profile-kicker-sep" aria-hidden="true">/</span>
                  <span class="profile-kicker-host">{{ siteHost }}</span>
                </template>
              </div>

              <h1 class="profile-name">{{ heroTitle }}</h1>
              <p class="profile-subtitle">{{ identityLine }}</p>
              <p v-if="tagline" class="profile-tagline">{{ tagline }}</p>

              <div v-if="lastActiveText" class="profile-status">
                <span class="profile-status-dot" aria-hidden="true"></span>
                <span>{{ lastActiveText }}</span>
              </div>
            </div>

            <div class="profile-actions">
              <a v-if="githubUrl"
                 class="ui-btn ui-btn--primary profile-action"
                 :href="githubUrl"
                 target="_blank"
                 rel="noopener noreferrer">
                GitHub
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor"
                     stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"
                     aria-hidden="true">
                  <path d="M7 17 17 7"/>
                  <path d="M9 7h8v8"/>
                </svg>
              </a>
              <button type="button" class="ui-btn profile-action" @click="$router.push('/message')">
                去留言
              </button>
            </div>
          </section>

          <!-- 站内导航条：支持同页锚点跳转 -->
          <nav class="about-nav" aria-label="关于页导航">
            <button v-for="item in navItems"
                    :key="item.key"
                    type="button"
                    class="about-tab"
                    :class="{ 'about-tab--active': isTabActive(item) }"
                    @click="goTab(item)">
              <svg v-if="item.icon === 'user'" viewBox="0 0 24 24" width="16" height="16"
                   fill="currentColor" aria-hidden="true">
                <path d="M12 12.4c2.2 0 4-1.8 4-4s-1.8-4-4-4-4 1.8-4 4 1.8 4 4 4Zm0 1.8c-2.7 0-8 1.35-8 4v1.6h16v-1.6c0-2.65-5.3-4-8-4Z"/>
              </svg>
              <svg v-else-if="item.icon === 'archive'" viewBox="0 0 24 24" width="16" height="16"
                   fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                   stroke-linejoin="round" aria-hidden="true">
                <rect x="3.4" y="4.6" width="17.2" height="4.4" rx="1.2"/>
                <path d="M5.2 9v9.4a1 1 0 0 0 1 1h11.6a1 1 0 0 0 1-1V9"/>
                <path d="M10 12.6h4"/>
              </svg>
              <svg v-else-if="item.icon === 'moments'" viewBox="0 0 24 24" width="16" height="16"
                   fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                   stroke-linejoin="round" aria-hidden="true">
                <circle cx="8.2" cy="15.8" r="1.5"/>
                <path d="M8.2 10.6a5.2 5.2 0 0 1 5.2 5.2"/>
                <path d="M8.2 5.6a10.2 10.2 0 0 1 10.2 10.2"/>
              </svg>
              <svg v-else-if="item.icon === 'clock'" viewBox="0 0 24 24" width="16" height="16"
                   fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                   stroke-linejoin="round" aria-hidden="true">
                <path d="M3.6 12a8.4 8.4 0 1 0 2.5-6"/>
                <path d="M3.6 4.6v4h4"/>
                <path d="M12 8v4.2l2.8 1.7"/>
              </svg>
              <svg v-else-if="item.icon === 'log'" viewBox="0 0 24 24" width="16" height="16"
                   fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                   stroke-linejoin="round" aria-hidden="true">
                <path d="M5.4 4.6h13.2v14.8H5.4z"/>
                <path d="M8.6 8.4h6.8M8.6 12h6.8M8.6 15.6h4.2"/>
              </svg>
              <svg v-else-if="item.icon === 'link'" viewBox="0 0 24 24" width="16" height="16"
                   fill="currentColor" aria-hidden="true">
                <path d="M16 11.2c1.6 0 2.9-1.3 2.9-2.9S17.6 5.4 16 5.4s-2.9 1.3-2.9 2.9 1.3 2.9 2.9 2.9Zm-8 0c1.6 0 2.9-1.3 2.9-2.9S9.6 5.4 8 5.4 5.1 6.7 5.1 8.3 6.4 11.2 8 11.2Zm0 1.7c-2.3 0-6.9 1.15-6.9 3.4v1.7h13.8v-1.7c0-2.25-4.6-3.4-6.9-3.4Zm8 0c-.3 0-.6 0-.95.05 1.15.8 1.95 1.9 1.95 3.35v1.7h5.9v-1.7c0-2.25-4.6-3.4-6.9-3.4Z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none"
                   stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                   stroke-linejoin="round" aria-hidden="true">
                <path d="M4 6.2h16v10.4H9.4L5.2 20v-3.4H4z"/>
                <path d="M8.4 10h7.2M8.4 13h4.6"/>
              </svg>
              <span class="about-tab-label">{{ item.label }}</span>
              <span v-if="item.key === 'friends' && friendCount"
                    class="about-tab-count">{{ friendCount }}</span>
            </button>
          </nav>

          <!-- 朋友圈占位：纯前端状态，不新增路由与接口 -->
          <section v-if="showMoments" class="about-card moments-card" aria-live="polite">
            <span class="moments-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="8.2" cy="15.8" r="1.5"/>
                <path d="M8.2 10.6a5.2 5.2 0 0 1 5.2 5.2"/>
                <path d="M8.2 5.6a10.2 10.2 0 0 1 10.2 10.2"/>
              </svg>
            </span>
            <h2 class="moments-title">正在紧急制作中</h2>
            <p class="moments-desc">朋友圈还在搭建，稍后再来看看，也可以先回到个人资料。</p>
            <button type="button" class="ui-btn moments-back" @click="showMoments = false">
              返回个人资料
            </button>
          </section>

          <div v-else class="about-columns">
            <!-- 主列：个人说明 + 作品 -->
            <div class="about-col about-col--main">
              <section id="about-me" class="about-card readme">
                <div class="readme-head">
                  <span class="readme-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linejoin="round">
                      <path d="M6 3.4h7.2L18.6 8v12.6H6z"/>
                      <path d="M13.2 3.4V8h5.4"/>
                    </svg>
                  </span>
                  <span class="readme-name">README.md</span>
                  <span class="readme-badge">公开</span>
                </div>

                <div class="readme-section">
                  <h2 class="readme-h2">关于我</h2>
                  <p v-if="tagline" class="readme-p">{{ tagline }}</p>
                  <p v-else class="about-empty">站长还没有填写个人简介。</p>
                </div>

                <div class="readme-section">
                  <h2 class="readme-h2">关于本站</h2>
                  <p v-if="siteIntroLine" class="readme-p">{{ siteIntroLine }}</p>
                  <div class="readme-facts">
                    <div v-if="siteName" class="readme-fact">
                      <div class="readme-fact-label">站点</div>
                      <div class="readme-fact-value">{{ siteName }}</div>
                    </div>
                    <div v-if="siteHost" class="readme-fact">
                      <div class="readme-fact-label">域名</div>
                      <div class="readme-fact-value">{{ siteHost }}</div>
                    </div>
                    <div v-if="siteCreated" class="readme-fact">
                      <div class="readme-fact-label">建于</div>
                      <div class="readme-fact-value">{{ siteCreated }}</div>
                    </div>
                    <div class="readme-fact">
                      <div class="readme-fact-label">维护者</div>
                      <div class="readme-fact-value">{{ ownerName }}</div>
                    </div>
                  </div>
                </div>

                <div class="readme-section">
                  <h2 class="readme-h2">这个站点的技术栈</h2>
                  <p class="readme-p">{{ techStackText }}</p>
                </div>
              </section>

              <!-- 本人项目：来自公开配置 aboutProjects，空值不渲染 -->
              <section v-if="projectItems.length" class="about-card">
                <div class="about-card-head">
                  <div class="about-head-icon">
                    <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                      <path d="M4.4 7.6h6l1.6 2h7.6v8.8H4.4z"/>
                      <path d="M4.4 7.6V5.4h5l1.6 2.2"/>
                    </svg>
                  </div>
                  <div>
                    <div class="about-card-title">本人项目</div>
                  </div>
                </div>

                <ul class="project-list">
                  <li v-for="(project, index) in projectItems" :key="'project-' + index">
                    <component :is="project.url ? 'a' : 'div'"
                               class="project-card"
                               :href="project.url || undefined"
                               :target="project.url ? '_blank' : undefined"
                               rel="noopener noreferrer">
                      <span class="project-head">
                        <span class="project-name">{{ project.name }}</span>
                        <span v-if="project.status" class="project-status">{{ project.status }}</span>
                      </span>
                      <span v-if="project.description" class="project-desc">{{ project.description }}</span>
                      <span v-if="project.tags.length" class="work-meta">
                        <em v-for="(tag, tagIndex) in project.tags" :key="'project-tag-' + tagIndex">{{ tag }}</em>
                      </span>
                    </component>
                  </li>
                </ul>
              </section>

              <!-- 开源致谢：来自公开配置 aboutCredits，空值不渲染 -->
              <section v-if="creditItems.length" class="about-card">
                <div class="about-card-head">
                  <div class="about-head-icon">
                    <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                      <path d="M12 19.4s-7.2-4.3-7.2-9A4.2 4.2 0 0 1 12 7.6a4.2 4.2 0 0 1 7.2 2.8c0 4.7-7.2 9-7.2 9z"/>
                    </svg>
                  </div>
                  <div>
                    <div class="about-card-title">开源致谢</div>
                  </div>
                </div>

                <ul class="credit-list">
                  <li v-for="(item, index) in creditItems" :key="'credit-' + index">
                    <component :is="item.url ? 'a' : 'div'"
                               class="credit-item"
                               :href="item.url || undefined"
                               :target="item.url ? '_blank' : undefined"
                               rel="noopener noreferrer">
                      <span class="credit-main">
                        <span class="credit-name">{{ item.name }}</span>
                        <span v-if="item.description" class="credit-desc">{{ item.description }}</span>
                      </span>
                      <svg v-if="item.url" class="credit-arrow" viewBox="0 0 24 24" width="14" height="14"
                           fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"
                           stroke-linejoin="round" aria-hidden="true">
                        <path d="M7 17 17 7"/>
                        <path d="M9 7h8v8"/>
                      </svg>
                    </component>
                  </li>
                </ul>
              </section>
            </div>

            <!-- 侧列：关注、联系、近况 -->
            <div class="about-col about-col--side">
              <section class="about-card">
                <div class="about-card-head">
                  <div class="about-head-icon">
                    <svg viewBox="0 0 24 24" width="17" height="17" aria-hidden="true">
                      <path d="M12 3.2l2.3 4.7 5.2.8-3.8 3.7.9 5.2-4.6-2.5-4.6 2.5.9-5.2L4.5 8.7l5.2-.8z"
                            fill="currentColor"/>
                    </svg>
                  </div>
                  <div>
                    <div class="about-card-title">正在关注</div>
                  </div>
                </div>

                <div v-if="hasFocus">
                  <div v-if="focusCategories.length" class="focus-group">
                    <div class="focus-label">内容分类</div>
                    <div class="chip-row">
                      <button v-for="sort in focusCategories"
                              :key="'sort-' + sort.id"
                              type="button"
                              class="chip"
                              @click="openSort(sort)">
                        {{ sort.sortName }}
                      </button>
                    </div>
                  </div>
                  <div v-if="focusLabels.length" class="focus-group">
                    <div class="focus-label">文章标签</div>
                    <div class="chip-row">
                      <button v-for="label in focusLabels"
                              :key="'label-' + label.sortId + '-' + label.id"
                              type="button"
                              class="chip"
                              @click="openLabel(label)">
                        {{ label.labelName }}
                      </button>
                    </div>
                  </div>
                </div>
                <p v-else class="about-empty">还没有可展示的分类与标签，发布文章后这里会自动汇总。</p>
              </section>

              <!-- 技术轨迹：来自公开配置 aboutTechStack，空值不渲染 -->
              <section v-if="techStackItems.length" class="about-card">
                <div class="about-card-head">
                  <div class="about-head-icon">
                    <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                      <path d="M8.6 8.2 4.8 12l3.8 3.8M15.4 8.2 19.2 12l-3.8 3.8"/>
                    </svg>
                  </div>
                  <div>
                    <div class="about-card-title">技术轨迹</div>
                  </div>
                </div>

                <div class="chip-row">
                  <span v-for="(tech, index) in techStackItems" :key="tech + '-' + index"
                        class="chip chip--static">{{ tech }}</span>
                </div>
              </section>

              <!-- 保持联系：参考站真实模块，只列公开联系方式 -->
              <section v-if="socials.length" class="about-card">
                <div class="about-card-head">
                  <div class="about-head-icon">
                    <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                      <rect x="3" y="5.2" width="18" height="13.6" rx="3"/>
                      <path d="M4.4 7.4 12 12.9l7.6-5.5"/>
                    </svg>
                  </div>
                  <div>
                    <div class="about-card-title">保持联系</div>
                  </div>
                </div>

                <ul class="contact-list">
                  <li v-for="item in socials" :key="item.key">
                    <a class="contact-item" :href="item.href" target="_blank" rel="noopener noreferrer"
                       :title="item.label + '：' + item.value">
                      <span class="contact-icon">
                        <svg v-if="item.key === 'github'" viewBox="0 0 16 16" width="15" height="15"
                             fill="currentColor" aria-hidden="true">
                          <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0 0 16 8c0-4.42-3.58-8-8-8z"/>
                        </svg>
                        <svg v-else-if="item.key === 'gitee'" viewBox="0 0 16 16" width="15" height="15"
                             fill="currentColor" fill-rule="evenodd" aria-hidden="true">
                          <path d="M0 8a8 8 0 1 0 16 0a8 8 0 1 0-16 0ZM3.82 2.82H12.18A1 1 0 0 1 13.18 3.82V4.21A0.5 0.5 0 0 1 12.68 4.71H5.21A0.5 0.5 0 0 0 4.71 5.21V10.79A0.5 0.5 0 0 0 5.21 11.29H10.79A0.5 0.5 0 0 0 11.29 10.79V9.91A0.5 0.5 0 0 0 10.79 9.41H8.03A0.5 0.5 0 0 1 7.53 8.91V8.03A0.5 0.5 0 0 1 8.03 7.53H12.68A0.5 0.5 0 0 1 13.18 8.03V12.18A1 1 0 0 1 12.18 13.18H3.82A1 1 0 0 1 2.82 12.18V3.82A1 1 0 0 1 3.82 2.82Z"/>
                        </svg>
                        <svg v-else-if="item.key === 'bilibili'" viewBox="3 3 18 17" width="15" height="15"
                             fill="currentColor" aria-hidden="true">
                          <path d="M7.2 3.2 9 5h6l1.8-1.8 1.4 1.4L17 6h1.5A2.5 2.5 0 0 1 21 8.5v9A2.5 2.5 0 0 1 18.5 20h-13A2.5 2.5 0 0 1 3 17.5v-9A2.5 2.5 0 0 1 5.5 6H7L5.8 4.6l1.4-1.4zM5.5 8a.5.5 0 0 0-.5.5v9c0 .28.22.5.5.5h13a.5.5 0 0 0 .5-.5v-9a.5.5 0 0 0-.5-.5h-13z"/>
                          <circle cx="9" cy="12" r="1.2"/>
                          <circle cx="15" cy="12" r="1.2"/>
                        </svg>
                        <i v-else class="fa fa-envelope-o" aria-hidden="true"></i>
                      </span>
                      <span class="contact-label">{{ item.label }}</span>
                      <span class="contact-value">{{ item.value }}</span>
                    </a>
                  </li>
                </ul>
              </section>

              <!-- 服务器状态：数据来自后端 5 秒缓存采样，只展示真实容量与使用率 -->
              <section class="about-card">
                <div class="about-card-head">
                  <div class="about-head-icon">
                    <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                      <path d="M7.4 18.2h9.8a4 4 0 0 0 .5-7.97A6 6 0 0 0 6.2 9.4a4.4 4.4 0 0 0 1.2 8.8z"/>
                    </svg>
                  </div>
                  <div>
                    <div class="about-card-title">服务器状态</div>
                  </div>
                  <div class="server-state" role="status">
                    <span class="server-dot" :class="'server-dot--' + serverStatus" aria-hidden="true"></span>
                    <span class="server-state-text">{{ serverStatusText }}</span>
                  </div>
                </div>

                <ul v-if="serverMetrics" class="server-metric-list">
                  <li v-if="hasCpuMetric" class="server-metric">
                    <span class="server-metric-icon" aria-hidden="true">
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
                           stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="6.2" y="6.2" width="11.6" height="11.6" rx="2.4"/>
                        <rect x="9.6" y="9.6" width="4.8" height="4.8" rx="1"/>
                        <path d="M9.4 3.2v3M14.6 3.2v3M9.4 17.8v3M14.6 17.8v3M3.2 9.4h3M3.2 14.6h3M17.8 9.4h3M17.8 14.6h3"/>
                      </svg>
                    </span>
                    <div class="server-metric-body">
                      <div class="server-metric-top">
                        <div class="server-metric-text">
                          <div class="server-metric-title">处理器</div>
                          <div class="server-metric-sub">{{ cpuSubtitle }}</div>
                        </div>
                        <span class="server-metric-percent">{{ percentText(serverMetrics.cpuUsage) }}</span>
                      </div>
                      <div class="server-metric-bar">
                        <span :style="{width: barWidth(serverMetrics.cpuUsage)}"></span>
                      </div>
                    </div>
                  </li>

                  <li class="server-metric">
                    <span class="server-metric-icon" aria-hidden="true">
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
                           stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3.4" y="7" width="17.2" height="10" rx="2"/>
                        <path d="M7.2 10.4h.01M7.2 13.6h.01M10.2 10.4h6.6M10.2 13.6h6.6"/>
                        <path d="M6.4 17v2.4M12 17v2.4M17.6 17v2.4"/>
                      </svg>
                    </span>
                    <div class="server-metric-body">
                      <div class="server-metric-top">
                        <div class="server-metric-text">
                          <div class="server-metric-title">内存</div>
                          <div class="server-metric-sub">{{ memorySubtitle }}</div>
                        </div>
                        <span class="server-metric-percent">{{ percentText(serverMetrics.memUsage) }}</span>
                      </div>
                      <div class="server-metric-bar">
                        <span :style="{width: barWidth(serverMetrics.memUsage)}"></span>
                      </div>
                    </div>
                  </li>

                  <li class="server-metric">
                    <span class="server-metric-icon" aria-hidden="true">
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
                           stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
                        <ellipse cx="12" cy="6.4" rx="7.2" ry="2.9"/>
                        <path d="M4.8 6.4v11.2c0 1.6 3.2 2.9 7.2 2.9s7.2-1.3 7.2-2.9V6.4"/>
                        <path d="M4.8 12c0 1.6 3.2 2.9 7.2 2.9s7.2-1.3 7.2-2.9"/>
                      </svg>
                    </span>
                    <div class="server-metric-body">
                      <div class="server-metric-top">
                        <div class="server-metric-text">
                          <div class="server-metric-title">存储</div>
                          <div class="server-metric-sub">{{ diskSubtitle }}</div>
                        </div>
                        <span class="server-metric-percent">{{ percentText(serverMetrics.diskUsage) }}</span>
                      </div>
                      <div class="server-metric-bar">
                        <span :style="{width: barWidth(serverMetrics.diskUsage)}"></span>
                      </div>
                    </div>
                  </li>

                  <li class="server-metric server-metric--plain">
                    <span class="server-metric-icon" aria-hidden="true">
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
                           stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3.4" y="5.2" width="17.2" height="13.6" rx="2"/>
                        <path d="M3.4 9.2h17.2"/>
                        <path d="M7 7.2h.01M9.6 7.2h.01"/>
                      </svg>
                    </span>
                    <div class="server-metric-body">
                      <div class="server-metric-top">
                        <div class="server-metric-text">
                          <div class="server-metric-title">系统</div>
                          <div class="server-metric-sub">{{ systemSubtitle }}</div>
                        </div>
                      </div>
                    </div>
                  </li>
                </ul>

                <div class="server-meta">
                  <span>实时刷新 · 最近更新 {{ serverCheckedAt || "--:--:--" }} · 每 {{ pollSeconds }} 秒检查一次</span>
                </div>
              </section>

              <!-- 魔改日记：取博客日志里分类为重大更新/功能迭代的重要条目，取不到不渲染 -->
              <section v-if="diaryItems.length" id="blog-log" class="about-card">
                <div class="about-card-head">
                  <div class="about-head-icon">
                    <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                      <path d="M5.4 4.6h13.2v14.8H5.4z"/>
                      <path d="M8.6 8.4h6.8M8.6 12h6.8M8.6 15.6h4.2"/>
                    </svg>
                  </div>
                  <div>
                    <div class="about-card-title">魔改日记</div>
                  </div>
                </div>

                <ul class="timeline">
                  <li v-for="(item, index) in diaryItems" :key="'log-' + index">
                    <div v-if="item.date" class="timeline-date">{{ item.date }}</div>
                    <div class="timeline-title">{{ item.title }}</div>
                    <p v-if="item.description" class="timeline-desc">{{ item.description }}</p>
                  </li>
                </ul>
              </section>

            </div>
          </div>

          <!-- 收尾卡：对应参考站 profile-footer -->
          <section class="about-card profile-footer">
            <div class="profile-footer-eyebrow">OPEN SOURCE, OPEN CHANNEL</div>
            <p class="profile-footer-text">如果你也在把想法变成东西，欢迎来坐坐。</p>
            <div class="profile-footer-actions">
              <button type="button" class="ui-btn ui-btn--primary" @click="$router.push('/favorite')">
                认识友链
              </button>
            </div>
          </section>
        </main>

        <!-- 右栏：与参考页一样只在宽屏出现；与归档页同一套模块，日历卡按参考站放这里 -->
        <aside class="about-rail" aria-label="关于页右栏">
          <ArchiveSidebar :articles="articles"></ArchiveSidebar>
        </aside>
      </div>

      <!-- 页脚 -->
      <MyFooter></MyFooter>
    </div>
  </div>
</template>

<script>
  import TwoPoem from "../components/common/TwoPoem.vue";
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import ArchiveSidebar from "../components/common/ArchiveSidebar.vue";
  import { fetchAllArticles } from "../utils/article";

  export default {
    components: {
      TwoPoem,
      MyFooter,
      MyAside,
      ArchiveSidebar
    },
    data() {
      return {
        articles: [],
        serverStatus: "checking",
        serverCheckedAt: "",
        serverMetrics: null,
        // 前端每 5 秒刷新一次，后端本身也有 5 秒缓存，不会把系统采样打成高频请求
        pollSeconds: 5,
        serverTimer: null,
        navItems: [
          {key: "about", label: "个人资料", path: "/about", icon: "user"},
          {key: "blog", label: "博客日志", path: "/blog-log", icon: "clock"},
          {key: "friends", label: "友链", path: "/favorite", icon: "link"},
          {key: "moments", label: "朋友圈", path: "", icon: "moments"}
        ],
        // 朋友圈尚未上线：点击后在本页展示站内样式的占位卡片
        showMoments: false,
        friendCount: 0,
        // 博客日志原始数据，供「魔改日记」挑选重要条目
        blogLogList: []
      }
    },
    computed: {
      webInfo() {
        return this.$store.state.webInfo || {};
      },
      bossInfo() {
        return this.$store.state.bossInfo || {};
      },
      sortInfo() {
        return this.$store.state.sortInfo || [];
      },
      sysConfig() {
        return this.$store.state.sysConfig || {};
      },
      ownerName() {
        if (!this.$common.isEmpty(this.bossInfo.username)) {
          return this.bossInfo.username;
        }
        return this.$common.isEmpty(this.webInfo.webName) ? "站长" : this.webInfo.webName;
      },
      ownerAvatar() {
        if (!this.$common.isEmpty(this.bossInfo.avatar)) {
          return this.bossInfo.avatar;
        }
        if (!this.$common.isEmpty(this.webInfo.avatar)) {
          return this.webInfo.avatar;
        }
        return "/images/default-avatar.webp";
      },
      siteName() {
        return this.$common.isEmpty(this.webInfo.webName) ? "" : this.webInfo.webName;
      },
      // 大标题优先用站点名，与参考站的品牌标题位置对应
      heroTitle() {
        return this.siteName || this.ownerName;
      },
      siteHost() {
        return typeof window !== "undefined" && window.location ? window.location.host : "";
      },
      identityLine() {
        return this.ownerName + " · 主理人";
      },
      // 一句话定位：优先站长资料里的自我介绍，其次站点简介
      tagline() {
        if (!this.$common.isEmpty(this.bossInfo.introduction)) {
          return this.bossInfo.introduction;
        }
        return this.$common.isEmpty(this.webInfo.description) ? "" : this.webInfo.description;
      },
      // 站点简介与站点名重复时不重复展示
      siteIntroLine() {
        const description = this.webInfo.description;
        if (this.$common.isEmpty(description) || description === this.webInfo.webName) {
          return "";
        }
        return description;
      },
      siteCreated() {
        return this.dateOnly(this.webInfo.createTime);
      },
      techStackText() {
        return "前端使用 Vue 3 与 Element Plus，后端使用 Spring Boot 2.7 与 MyBatis-Plus，数据存储使用 MySQL 8。";
      },
      latestArticle() {
        return this.articles.length ? this.articles[0] : null;
      },
      // 状态行：取最近一篇文章的更新时间
      lastActiveText() {
        if (!this.latestArticle || this.$common.isEmpty(this.latestArticle.createTime)) {
          return "";
        }
        return "最近活跃于 " + this.dateOnly(this.latestArticle.createTime);
      },
      // 只在后台配置了合法 GitHub 地址时才渲染主按钮
      githubUrl() {
        return this.isUrl(this.webInfo.github) ? this.webInfo.github.trim() : "";
      },
      focusCategories() {
        return this.sortInfo
          .filter((sort) => Number(sort.countOfSort) > 0)
          .map((sort) => ({
            id: sort.id,
            sortName: sort.sortName,
            countOfSort: sort.countOfSort
          }));
      },
      focusLabels() {
        const labels = [];
        this.sortInfo.forEach((sort) => {
          if (!Array.isArray(sort.labels)) {
            return;
          }
          sort.labels.forEach((label) => {
            if (Number(label.countOfLabel) > 0) {
              labels.push({
                id: label.id,
                sortId: sort.id,
                labelName: label.labelName
              });
            }
          });
        });
        return labels;
      },
      hasFocus() {
        return this.focusCategories.length > 0 || this.focusLabels.length > 0;
      },
      // aboutTechStack：JSON 数组或每行一项的纯文本
      techStackItems() {
        return this.parseConfigList("aboutTechStack")
          .map((item) => {
            if (typeof item === "string") {
              return item.trim();
            }
            if (item && typeof item === "object" && !this.$common.isEmpty(item.name)) {
              return String(item.name).trim();
            }
            return "";
          })
          .filter(Boolean);
      },
      // aboutCredits：每行「名称|说明|链接」，也兼容 JSON 数组
      creditItems() {
        return this.parseConfigList("aboutCredits")
          .map((item) => {
            if (typeof item === "string") {
              const parts = item.split("|").map((part) => part.trim()).filter(Boolean);
              if (!parts.length) {
                return null;
              }
              let description = "";
              let url = "";
              parts.slice(1).forEach((part) => {
                if (this.isUrl(part)) {
                  url = part;
                } else if (!description) {
                  description = part;
                }
              });
              return {name: parts[0], description, url};
            }
            if (item && typeof item === "object" && !this.$common.isEmpty(item.name)) {
              return {
                name: String(item.name).trim(),
                description: this.$common.isEmpty(item.description) ? "" : String(item.description).trim(),
                url: this.isUrl(item.url) ? String(item.url).trim() : ""
              };
            }
            return null;
          })
          .filter(Boolean);
      },
      // 魔改日记：从博客日志（changelog 表）里挑重大更新/功能迭代的重要条目，最多 5 条
      diaryItems() {
        return this.blogLogList
          .filter((item) => {
            const kind = String(item.kind || "").trim();
            return kind === "重大更新" || kind === "功能迭代";
          })
          .slice(0, 5)
          .map((item) => ({
            date: String(item.logDate || "").slice(0, 10).replace(/-/g, "."),
            title: item.title || "",
            description: item.summary || ""
          }))
          .filter((item) => item.title);
      },
      // aboutProjects：每行「名称|说明|标签1/标签2|链接|状态」，也兼容 JSON 数组
      projectItems() {
        return this.parseConfigList("aboutProjects").map((item) => {
          if (typeof item === "string") {
            const parts = item.split("|").map((part) => part.trim());
            if (!parts[0]) {
              return null;
            }
            const tags = (parts[2] || "")
              .split(/[\/、,，]/)
              .map((tag) => tag.trim())
              .filter(Boolean);
            const url = parts.slice(3).find((part) => this.isUrl(part)) || "";
            const status = parts.slice(3).find((part) => part && part !== url) || "";
            return {name: parts[0], description: parts[1] || "", tags, url, status};
          }
          if (item && typeof item === "object" && !this.$common.isEmpty(item.name)) {
            let tags = [];
            if (Array.isArray(item.tags)) {
              tags = item.tags.map((tag) => String(tag).trim()).filter(Boolean);
            } else if (!this.$common.isEmpty(item.tags)) {
              tags = String(item.tags).split(/[\/、,，]/).map((tag) => tag.trim()).filter(Boolean);
            }
            return {
              name: String(item.name).trim(),
              description: this.$common.isEmpty(item.description) ? "" : String(item.description).trim(),
              tags,
              url: this.isUrl(item.url) ? String(item.url).trim() : "",
              status: this.$common.isEmpty(item.status) ? "" : String(item.status).trim()
            };
          }
          return null;
        }).filter(Boolean);
      },
      serverStatusText() {
        return {checking: "检查中", online: "在线", offline: "离线"}[this.serverStatus] || "未知";
      },
      // 处理器：核数与使用率都拿不到时整行不渲染，不显示「--」或「未知」占位
      hasCpuMetric() {
        if (!this.serverMetrics) {
          return false;
        }
        return this.serverMetrics.cpuCores !== null || this.serverMetrics.cpuUsage !== null;
      },
      // 处理器：核数 + 型号，型号过长时由 CSS 省略
      cpuSubtitle() {
        if (!this.serverMetrics) {
          return "";
        }
        const cores = this.isNumber(this.serverMetrics.cpuCores) ? this.serverMetrics.cpuCores : null;
        const model = this.serverMetrics.cpuModel || "";
        if (cores === null) {
          return model;
        }
        return model ? cores + " vCPU · " + model : cores + " vCPU";
      },
      // 内存：已用 / 总量 · 可用
      memorySubtitle() {
        const metric = this.serverMetrics;
        if (!metric || !this.isNumber(metric.memTotal)) {
          return "";
        }
        const text = this.formatBytes(metric.memUsed) + " / " + this.formatBytes(metric.memTotal);
        return this.isNumber(metric.memAvailable)
          ? text + " · 可用 " + this.formatBytes(metric.memAvailable)
          : text;
      },
      // 存储：已用 / 总量
      diskSubtitle() {
        const metric = this.serverMetrics;
        if (!metric || !this.isNumber(metric.diskTotal)) {
          return "";
        }
        return this.formatBytes(metric.diskUsed) + " / " + this.formatBytes(metric.diskTotal);
      },
      systemSubtitle() {
        return this.serverMetrics && this.serverMetrics.osName
          ? this.serverMetrics.osName
          : "";
      },
      // 只把确实可用的公开链接展示出来，配置为空或格式不对的字段不渲染
      socials() {
        const items = [];
        const push = (key, label, value) => {
          if (this.isUrl(value)) {
            items.push({key, label, value: this.displayHost(value), href: value.trim()});
          }
        };
        push("github", "GitHub", this.webInfo.github);
        push("bilibili", "Bilibili", this.webInfo.bilibili);
        if (this.isEmail(this.webInfo.email)) {
          items.push({
            key: "email",
            label: "邮箱",
            value: this.webInfo.email.trim(),
            href: "mailto:" + this.webInfo.email.trim()
          });
        }
        return items;
      }
    },
    created() {
      this.getArticles();
      this.getFriendCount();
      this.getBlogLog();
      this.checkServerStatus();
    },
    watch: {
      // 已在关于页时，路由 hash 变化要滚到对应区块
      "$route.hash"(hash) {
        if (this.$route.path === "/about" && hash) {
          this.scrollToAnchor(hash.replace(/^#/, ""));
        }
      },
      // 离开关于页后收起朋友圈占位，回来仍是默认的个人资料
      "$route.path"() {
        this.showMoments = false;
      }
    },
    mounted() {
      // 深链：/about#blog-log 进来时滚到对应区块
      if (this.$route.hash) {
        this.scrollToAnchor(this.$route.hash.replace(/^#/, ""));
      }
      // 页面在后台标签页时不轮询，回来时立即补一次
      this.serverTimer = setInterval(() => {
        if (!document.hidden) {
          this.checkServerStatus(true);
        }
      }, this.pollSeconds * 1000);
    },
    beforeUnmount() {
      if (this.serverTimer) {
        clearInterval(this.serverTimer);
        this.serverTimer = null;
      }
    },
    methods: {
      // 友链数量：复用公开的友链列表接口，失败时不显示计数
      getFriendCount() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listFriend")
          .then((res) => {
            this.friendCount = Array.isArray(res.data) ? res.data.length : 0;
          })
          .catch(() => {
            this.friendCount = 0;
          });
      },
      // 魔改日记的数据源：博客日志（changelog 表），取不到时保持为空、卡片不渲染
      getBlogLog() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listChangelog")
          .then((res) => {
            this.blogLogList = Array.isArray(res.data) ? res.data : [];
          })
          .catch(() => {
            this.blogLogList = [];
          });
      },
      // 页签：普通路由跳转；带 hash 的做同页锚点滚动
      goTab(item) {
        // 朋友圈尚未上线：留在关于页展示占位卡片，不新增路由
        if (item.key === "moments") {
          this.showMoments = true;
          return;
        }
        this.showMoments = false;
        if (this.$route.path === item.path && !this.$route.hash) {
          return;
        }
        if (item.hash) {
          if (this.$route.path !== item.path) {
            this.$router.push({path: item.path, hash: "#" + item.hash});
          } else {
            this.scrollToAnchor(item.hash);
          }
          return;
        }
        this.$router.push(item.path);
      },
      isTabActive(item) {
        if (item.key === "moments") {
          return this.showMoments;
        }
        if (this.showMoments) {
          return false;
        }
        if (item.hash) {
          return this.$route.path === item.path && this.$route.hash === "#" + item.hash;
        }
        return this.$route.path === item.path && !this.$route.hash;
      },
      scrollToAnchor(id) {
        this.$nextTick(() => {
          const element = document.getElementById(id);
          if (element) {
            element.scrollIntoView({behavior: "smooth", block: "start"});
          }
        });
      },
      // 配置支持 JSON 数组，或按行分隔的纯文本
      parseConfigList(key) {
        const raw = this.sysConfig[key];
        if (this.$common.isEmpty(raw)) {
          return [];
        }
        const text = String(raw).trim();
        if (text.startsWith("[")) {
          try {
            const list = JSON.parse(text);
            return Array.isArray(list) ? list : [];
          } catch (e) {
            return [];
          }
        }
        return text.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
      },
      // 用只读公开接口取服务器状态；silent=true 时用于定时刷新，不闪「检查中」
      checkServerStatus(silent) {
        if (!silent) {
          this.serverStatus = "checking";
        }
        this.$http.get(this.$constant.baseURL + "/webInfo/serverStatus")
          .then((res) => {
            const data = res && res.data ? res.data : null;
            this.serverStatus = "online";
            this.serverMetrics = this.normalizeServerMetrics(data);
            this.serverCheckedAt = this.displayCheckedAt(data && data.checkedAt);
          })
          .catch(() => {
            this.serverStatus = "offline";
            this.serverMetrics = null;
            this.serverCheckedAt = this.nowTime();
          });
      },
      // 只接受真实数值，字段缺失或类型不对就当作没有，不用估算值兜底
      normalizeServerMetrics(data) {
        if (!data || typeof data !== "object") {
          return null;
        }
        const cpuModel = typeof data.cpuModel === "string" ? data.cpuModel.trim() : "";
        const osName = typeof data.osName === "string" ? data.osName.trim() : "";
        const metrics = {
          cpuCores: this.isNumber(data.cpuCores) && data.cpuCores > 0 ? data.cpuCores : null,
          cpuModel: cpuModel === "未知" ? "" : cpuModel,
          cpuUsage: this.isNumber(data.cpuUsage) ? data.cpuUsage : null,
          memTotal: this.isNumber(data.memTotal) ? data.memTotal : null,
          memUsed: this.isNumber(data.memUsed) ? data.memUsed : null,
          memAvailable: this.isNumber(data.memAvailable) ? data.memAvailable : null,
          memUsage: this.isNumber(data.memUsage) ? data.memUsage : null,
          diskTotal: this.isNumber(data.diskTotal) ? data.diskTotal : null,
          diskUsed: this.isNumber(data.diskUsed) ? data.diskUsed : null,
          diskUsage: this.isNumber(data.diskUsage) ? data.diskUsage : null,
          osName: osName === "未知" ? "" : osName
        };
        if (metrics.cpuCores === null && metrics.memTotal === null && metrics.diskTotal === null && !metrics.osName) {
          return null;
        }
        return metrics;
      },
      isNumber(value) {
        return typeof value === "number" && Number.isFinite(value);
      },
      // 百分比文案：没有采样值时不显示，避免出现 NaN
      percentText(value) {
        return this.isNumber(value) ? value.toFixed(1) + "%" : "--";
      },
      barWidth(value) {
        if (!this.isNumber(value)) {
          return "0%";
        }
        return Math.max(0, Math.min(100, value)) + "%";
      },
      formatBytes(value) {
        const bytes = Number(value);
        if (!Number.isFinite(bytes) || bytes < 0) {
          return "--";
        }
        const gib = bytes / (1024 * 1024 * 1024);
        if (gib >= 1) {
          return gib.toFixed(1) + " GiB";
        }
        return (bytes / (1024 * 1024)).toFixed(1) + " MiB";
      },
      // 服务端返回的检查时间取时分秒；格式不认识就回落到本地检查时间
      displayCheckedAt(value) {
        const text = typeof value === "string" ? value.trim() : "";
        if (text.length >= 19) {
          return text.slice(11, 19);
        }
        return text || this.nowTime();
      },
      nowTime() {
        const date = new Date();
        const pad = (value) => String(value).padStart(2, "0");
        return pad(date.getHours()) + ":" + pad(date.getMinutes()) + ":" + pad(date.getSeconds());
      },
      getArticles() {
        fetchAllArticles(this.$http, this.$constant)
          .then((articles) => {
            this.articles = articles;
          })
          .catch(() => {
            this.articles = [];
          });
      },
      dateOnly(value) {
        const text = String(value || "");
        return text.length >= 10 ? text.slice(0, 10) : text;
      },
      isUrl(value) {
        return typeof value === "string" && /^https?:\/\/\S+$/i.test(value.trim());
      },
      isEmail(value) {
        return typeof value === "string" && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
      },
      displayHost(value) {
        try {
          return new URL(value.trim()).host.replace(/^www\./i, "");
        } catch (e) {
          return value.trim();
        }
      },
      openSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      openLabel(label) {
        this.$router.push({path: "/archive", query: {tag: label.labelName}});
      },
      selectSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: "/"});
        }
      }
    }
  }
</script>

<style scoped>
  /* 尺寸按参考站 about 页样式表实测换算（桌面根字号 16px） */

  .about-container {
    display: grid;
    grid-template-columns: var(--ui-aside-width) minmax(0, 1fr);
    gap: var(--ui-page-gap);
    align-items: start;
    width: min(var(--ui-page-inline), var(--ui-page-width));
    max-width: var(--ui-page-width);
    margin: 0 auto;
    padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
  }

  .about-aside {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--ui-page-gap);
    align-content: start;
    min-width: 0;
  }

  /* 页签锚点跳转时给吸顶导航留出位置 */
  #about-me,
  #blog-log {
    scroll-margin-top: 90px;
  }

  /* 右栏默认隐藏，宽屏才显示为第三列 */
  .about-rail {
    display: none;
    min-width: 0;
  }

  @media (min-width: 1280px) {
    .about-container {
      grid-template-columns: var(--ui-aside-width) minmax(0, 1fr) var(--ui-aside-width);
    }

    .about-rail {
      display: grid;
      grid-template-columns: minmax(0, 1fr);
      gap: var(--ui-page-gap);
      align-content: start;
    }
  }

  .about-main {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--ui-page-gap);
    min-width: 0;
  }

  /* 卡片：1rem 圆角、1px 描边、0 1px 2px 的弱阴影（深浅模式同一套，参考站也是如此） */
  .about-card {
    min-width: 0;
    padding: var(--ui-card-padding-y);
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  /* 个人头图 */
  .about-profile {
    display: grid;
    grid-template-columns: auto minmax(0, 1fr) auto;
    align-items: center;
    gap: 18.4px;
    padding: clamp(17.6px, 2.5vw, 26.4px);
  }

  .profile-avatar {
    flex: none;
    width: 100px;
    height: 100px;
    padding: 3px;
    border: 1px solid var(--cardBorder);
    border-color: color-mix(in srgb, var(--ui-accent) 32%, var(--cardBorder));
    background: var(--subtleBg);
    box-shadow: none;
  }

  .profile-body {
    min-width: 0;
  }

  .profile-kicker {
    display: flex;
    align-items: center;
    gap: 6.72px;
    font-size: 11.26px;
    font-weight: 780;
    letter-spacing: 0.12em;
    line-height: 1.3;
    text-transform: uppercase;
    color: var(--maxGreyFont);
  }

  .profile-kicker-dot {
    flex: none;
    width: 7.68px;
    height: 7.68px;
    border-radius: 50%;
    background: #34c759;
    box-shadow: 0 0 0 3.52px rgba(52, 199, 89, 0.14);
  }

  .profile-kicker > span {
    white-space: nowrap;
  }

  .profile-kicker-sep {
    color: var(--greyFontColor);
  }

  .profile-kicker-host {
    max-width: 18rem;
    overflow: hidden;
    letter-spacing: 0.04em;
    text-overflow: ellipsis;
    text-transform: none;
    color: var(--greyFontColor);
  }

  .profile-name {
    margin: 8.8px 0 0;
    font-size: clamp(29.92px, 3.52vw, 41.36px);
    font-weight: 800;
    letter-spacing: -0.04em;
    line-height: 1.1;
    color: var(--fontColor);
    overflow-wrap: anywhere;
  }

  .profile-subtitle {
    margin: 4.8px 0 0;
    font-size: 15.84px;
    line-height: 1.5;
    color: var(--maxGreyFont);
  }

  .profile-tagline {
    max-width: 42rem;
    margin: 12px 0 0;
    font-size: 17.07px;
    line-height: 1.7;
    color: var(--maxGreyFont);
  }

  .profile-status {
    display: flex;
    align-items: center;
    gap: 7.68px;
    margin-top: 12px;
    font-size: 13.38px;
    line-height: 1.5;
    color: var(--maxGreyFont);
  }

  .profile-status-dot {
    flex: none;
    width: 6.08px;
    height: 6.08px;
    border-radius: 50%;
    background: #34c759;
  }

  .profile-actions {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    gap: 8.8px;
    width: 147.2px;
    min-width: 147.2px;
  }

  .profile-action {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6.4px;
    min-height: 37.6px;
    padding: 8.8px 12.8px;
    border: 1px solid var(--cardBorder);
    border-radius: 10.4px;
    background: transparent;
    color: var(--ui-accent);
    font-family: inherit;
    font-size: 13.73px;
    font-weight: 720;
    line-height: 1.2;
    text-decoration: none;
    transition: background-color 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
  }

  .profile-action:hover {
    border-color: var(--ui-accent);
    background: var(--hoverBg);
    transform: translateY(-1px);
  }

  .profile-action.ui-btn--primary {
    border-color: var(--ui-accent);
    background: transparent;
    color: var(--ui-accent);
  }

  /* 站内导航：胶囊容器，页签按内容宽度左对齐，选中为短下划线 */
  .about-nav {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 2px;
    min-height: 52px;
    padding: 6px 10px;
    border: 1px solid var(--cardBorder);
    border-radius: 999px;
    background: var(--cardBg);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    overflow-x: auto;
    scrollbar-width: none;
  }

  .about-nav::-webkit-scrollbar {
    display: none;
  }

  .about-tab {
    position: relative;
    flex: 0 0 auto;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 7px;
    min-width: max-content;
    min-height: 40px;
    padding: 0 12px;
    border: 0;
    border-radius: 999px;
    background: transparent;
    color: var(--maxGreyFont);
    font-family: inherit;
    font-size: 14px;
    font-weight: 600;
    line-height: 1.2;
    white-space: nowrap;
    cursor: pointer;
    transition: color 0.18s ease, background-color 0.18s ease;
  }

  .about-tab:hover {
    color: var(--ui-accent);
    background: color-mix(in srgb, var(--ui-accent) 8%, transparent);
  }

  /* 选中：淡蓝胶囊底 + 蓝色图标与文字 */
  .about-tab--active {
    color: var(--ui-accent);
    background: color-mix(in srgb, var(--ui-accent) 14%, transparent);
  }

  .about-tab--active:hover {
    background: color-mix(in srgb, var(--ui-accent) 18%, transparent);
  }

  .about-tab svg {
    flex: none;
  }

  /* 友链数量：浅蓝胶囊计数 */
  .about-tab-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 22px;
    height: 20px;
    padding: 0 7px;
    border-radius: 999px;
    background: color-mix(in srgb, var(--ui-accent) 14%, transparent);
    color: var(--ui-accent);
    font-size: 11.5px;
    font-weight: 600;
    line-height: 1;
  }

  /* 朋友圈占位卡片 */
  .moments-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    min-height: 320px;
    padding: 48px 24px;
    text-align: center;
  }

  .moments-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: color-mix(in srgb, var(--ui-accent) 12%, transparent);
    color: var(--ui-accent);
  }

  .moments-title {
    margin: 0;
    color: var(--fontColor);
    font-size: 18px;
    font-weight: 700;
  }

  .moments-desc {
    margin: 0;
    color: var(--maxGreyFont);
    font-size: 13px;
    line-height: 1.7;
  }

  .moments-back {
    margin-top: 4px;
  }

  /* 主列 + 侧列：1fr / minmax(15rem, 18rem) */
  .about-columns {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(240px, 288px);
    gap: var(--ui-page-gap);
    align-items: start;
  }

  .about-col {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--ui-page-gap);
    min-width: 0;
  }

  .about-col--main {
    grid-column: 1;
  }

  .about-col--side {
    grid-column: 2;
  }

  .about-col--side .about-card {
    padding: 16px;
  }

  /* README */
  .readme-head {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--cardBorder);
  }

  .readme-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: var(--ui-accent);
  }

  .readme-name {
    font-size: 14.08px;
    font-weight: 720;
    letter-spacing: 0.01em;
    color: var(--fontColor);
  }

  .readme-badge {
    margin-left: auto;
    padding: 2px 7.2px;
    border: 1px solid var(--cardBorder);
    border-radius: 999px;
    font-size: 10.91px;
    letter-spacing: 0.04em;
    color: var(--maxGreyFont);
  }

  .readme-section {
    margin-top: 17.6px;
  }

  .readme-section + .readme-section {
    margin-top: 23.2px;
  }

  .readme-h2 {
    margin: 0 0 8.8px;
    font-size: 20.42px;
    font-weight: 780;
    letter-spacing: 0.01em;
    line-height: 1.55;
    color: var(--fontColor);
  }

  .readme-p {
    margin: 0;
    font-size: 16.19px;
    line-height: 1.9;
    color: var(--maxGreyFont);
    overflow-wrap: anywhere;
  }

  .readme-p + .readme-p {
    margin-top: 13.6px;
  }

  .readme-facts {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 8px;
    margin-top: 13.6px;
  }

  .readme-fact {
    padding: 8.8px 11.2px;
    border-radius: 8px;
    background: var(--subtleBg);
  }

  .readme-fact-label {
    font-size: 10.91px;
    letter-spacing: 0.08em;
    color: var(--greyFontColor);
  }

  .readme-fact-value {
    margin-top: 2.4px;
    font-size: 13.02px;
    font-weight: 680;
    color: var(--fontColor);
    overflow-wrap: anywhere;
  }

  /* 卡片标题：标题下一条分隔线 */
  .about-card-head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12.8px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--cardBorder);
  }

  .about-head-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex: none;
    width: auto;
    height: auto;
    border-radius: 0;
    background: none;
    color: var(--ui-accent);
  }

  .about-head-icon svg {
    width: 17.28px;
    height: 17.28px;
  }

  .about-card-title {
    font-size: 17.6px;
    font-weight: 780;
    letter-spacing: -0.015em;
    line-height: 1.3;
    color: var(--fontColor);
  }

  /* 关注领域 */
  .focus-group + .focus-group {
    margin-top: 13.6px;
  }

  .focus-label {
    font-size: 11.44px;
    font-weight: 720;
    letter-spacing: 0.08em;
    color: var(--greyFontColor);
  }

  .chip-row {
    display: flex;
    flex-wrap: wrap;
    gap: 5.6px;
    margin-top: 7.2px;
  }

  .chip {
    display: inline-flex;
    align-items: center;
    gap: 4.8px;
    padding: 4.48px 8px;
    border: 1px solid var(--cardBorder);
    border-color: color-mix(in srgb, var(--ui-accent) 16%, var(--cardBorder));
    border-radius: 7.2px;
    background: color-mix(in srgb, var(--ui-accent) 6%, transparent);
    color: var(--maxGreyFont);
    font-family: inherit;
    font-size: 11.44px;
    line-height: 1.4;
    cursor: pointer;
    transition: border-color 0.18s ease, color 0.18s ease;
  }

  .chip:hover {
    border-color: var(--ui-accent);
    color: var(--ui-accent);
  }

  .chip--static {
    cursor: default;
  }

  .chip--static:hover {
    border-color: color-mix(in srgb, var(--ui-accent) 16%, var(--cardBorder));
    color: var(--maxGreyFont);
  }

  /* 服务器状态 */
  .server-state {
    display: inline-flex;
    align-items: center;
    gap: 4.8px;
    margin-left: auto;
    font-size: 11.62px;
    font-weight: 720;
    line-height: 1.4;
    white-space: nowrap;
    color: var(--maxGreyFont);
  }

  .server-dot {
    flex: none;
    width: 6.72px;
    height: 6.72px;
    border-radius: 50%;
    background: var(--greyFontColor);
    box-shadow: 0 0 0 3.2px rgba(120, 120, 120, 0.14);
  }

  .server-dot--online {
    background: #34c759;
    box-shadow: 0 0 0 3.2px rgba(52, 199, 89, 0.14);
  }

  .server-dot--offline {
    background: #e19a45;
    box-shadow: 0 0 0 3.2px rgba(225, 154, 69, 0.16);
  }

  .server-state-text {
    color: var(--fontColor);
  }

  .server-metric-list {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 6.4px;
    margin: 12.8px 0 0;
    padding: 0;
    list-style: none;
  }

  /* 每行：左侧图标 + 标题/副标题 + 右侧百分比 + 下方进度条 */
  .server-metric {
    display: flex;
    align-items: flex-start;
    gap: 8.8px;
    min-width: 0;
    padding: 9px 9.6px;
    border: 1px solid color-mix(in srgb, var(--ui-accent) 12%, var(--cardBorder));
    border-radius: 9.6px;
    background: color-mix(in srgb, var(--ui-accent) 4%, transparent);
  }

  .server-metric-icon {
    flex: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 25.6px;
    height: 25.6px;
    border-radius: 8px;
    color: var(--ui-accent);
    background: color-mix(in srgb, var(--ui-accent) 12%, transparent);
  }

  .server-metric-body {
    flex: 1;
    min-width: 0;
  }

  .server-metric-top {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 8px;
  }

  .server-metric-text {
    min-width: 0;
  }

  .server-metric-title {
    font-size: 12.16px;
    font-weight: 760;
    line-height: 1.35;
    color: var(--fontColor);
  }

  .server-metric-sub {
    margin-top: 1.6px;
    font-size: 10.75px;
    line-height: 1.4;
    color: var(--greyFontColor);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .server-metric-percent {
    flex: none;
    font-size: 12.67px;
    font-weight: 780;
    font-variant-numeric: tabular-nums;
    color: var(--fontColor);
  }

  .server-metric-bar {
    height: 3.6px;
    margin-top: 6.4px;
    border-radius: 999px;
    background: color-mix(in srgb, var(--fontColor) 8%, transparent);
    overflow: hidden;
  }

  .server-metric-bar span {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg,
      color-mix(in srgb, var(--ui-accent) 72%, #fff),
      var(--ui-accent));
    transition: width .45s ease;
  }

  /* 系统这一行没有使用率，不需要进度条 */
  .server-metric--plain {
    align-items: center;
  }

  .server-metric--plain .server-metric-sub {
    white-space: normal;
    overflow-wrap: anywhere;
  }

  .server-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px 8px;
    margin-top: 11.2px;
    font-size: 10.91px;
    line-height: 1.5;
    color: var(--greyFontColor);
  }

  /* 魔改日记时间线 */
  .timeline {
    display: grid;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .timeline li {
    position: relative;
    padding: 0 0 12px 18.4px;
  }

  .timeline li:last-child {
    padding-bottom: 0;
  }

  .timeline li::before {
    content: "";
    position: absolute;
    top: 4px;
    left: 0;
    box-sizing: border-box;
    width: 8.32px;
    height: 8.32px;
    border: 2px solid var(--cardBg);
    border-radius: 50%;
    background: var(--ui-accent);
    box-shadow: 0 0 0 1px color-mix(in srgb, var(--ui-accent) 34%, transparent);
  }

  .timeline li:not(:last-child)::after {
    content: "";
    position: absolute;
    top: 14px;
    bottom: 2px;
    left: 3.68px;
    width: 1px;
    background: color-mix(in srgb, var(--ui-accent) 30%, var(--cardBorder));
  }

  .timeline-date {
    font-size: 10.91px;
    font-weight: 730;
    line-height: 1.5;
    color: var(--ui-accent);
  }

  .timeline-title {
    margin-top: 1.6px;
    font-size: 12.67px;
    font-weight: 720;
    line-height: 1.45;
    color: var(--fontColor);
    overflow-wrap: anywhere;
  }

  .timeline-desc {
    margin: 2.4px 0 0;
    font-size: 11.62px;
    line-height: 1.55;
    color: var(--maxGreyFont);
    overflow-wrap: anywhere;
  }

  /* 开源致谢 */
  .credit-list {
    display: grid;
    gap: 8px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .credit-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8.8px 9.6px;
    border: 1px solid var(--cardBorder);
    border-radius: 8.8px;
    color: inherit;
    text-decoration: none;
    transition: background-color 0.18s ease, border-color 0.18s ease;
  }

  .credit-item:hover {
    border-color: var(--ui-accent);
    background: var(--hoverBg);
  }

  .credit-main {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  .credit-name {
    font-size: 13.02px;
    font-weight: 720;
    line-height: 1.4;
    color: var(--fontColor);
  }

  .credit-desc {
    font-size: 10.91px;
    line-height: 1.5;
    color: var(--greyFontColor);
    overflow-wrap: anywhere;
  }

  .credit-arrow {
    flex: none;
    margin-left: auto;
    color: var(--greyFontColor);
  }

  .credit-item:hover .credit-arrow {
    color: var(--ui-accent);
  }

  /* 联系方式 */
  .contact-list {
    display: grid;
    gap: 5.6px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .contact-item {
    display: grid;
    grid-template-columns: 17.6px minmax(0, 1fr) auto;
    align-items: center;
    gap: 7.2px;
    padding: 7.52px 7.2px;
    border-radius: 7.2px;
    color: inherit;
    text-decoration: none;
    transition: background-color 0.18s ease;
  }

  .contact-item:hover {
    background: color-mix(in srgb, var(--ui-accent) 8%, transparent);
  }

  .contact-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 17.6px;
    height: 17.6px;
    color: var(--maxGreyFont);
  }

  .contact-item:hover .contact-icon {
    color: var(--ui-accent);
  }

  .contact-label {
    font-size: 13.02px;
    font-weight: 680;
    color: var(--fontColor);
  }

  .contact-value {
    overflow: hidden;
    font-size: 10.91px;
    text-align: right;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--greyFontColor);
  }

  .more-link {
    display: inline-flex;
    align-items: center;
    gap: 3.2px;
    padding: 0;
    border: 0;
    background: none;
    color: var(--ui-accent);
    font-family: inherit;
    font-size: 12.32px;
    font-weight: 680;
    line-height: 1.4;
    white-space: nowrap;
    cursor: pointer;
  }

  .more-link:hover {
    opacity: 0.78;
  }

  /* 项目卡片标签 */
  .work-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }

  .work-meta em {
    padding: 2.88px 5.44px;
    border: 1px solid var(--cardBorder);
    border-color: color-mix(in srgb, var(--ui-accent) 16%, var(--cardBorder));
    border-radius: 5.6px;
    background: color-mix(in srgb, var(--ui-accent) 5%, transparent);
    font-size: 10.56px;
    font-style: normal;
    line-height: 1.4;
    color: var(--maxGreyFont);
  }

  .about-empty {
    margin: 0;
    padding: 4px 0;
    font-size: 12.67px;
    line-height: 1.7;
    color: var(--greyFontColor);
  }

  /* ===== 本人项目 ===== */
  .project-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 11.2px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .project-card {
    display: grid;
    gap: 5.6px;
    padding: 13.12px;
    border: 1px solid var(--cardBorder);
    border-color: color-mix(in srgb, var(--ui-accent) 24%, var(--cardBorder));
    border-radius: 12.48px;
    color: inherit;
    text-decoration: none;
    transition: transform 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
  }

  .project-card:hover {
    border-color: var(--ui-accent);
    background: var(--hoverBg);
    transform: translateY(-2px);
  }

  .project-head {
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
  }

  .project-name {
    min-width: 0;
    overflow: hidden;
    font-size: 15.49px;
    font-weight: 760;
    line-height: 1.4;
    color: var(--fontColor);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .project-status {
    flex: none;
    margin-left: auto;
    padding: 2px 5.6px;
    border: 1px solid var(--cardBorder);
    border-radius: 999px;
    font-size: 10.21px;
    line-height: 1.4;
    white-space: nowrap;
    color: var(--greyFontColor);
  }

  .project-desc {
    font-size: 11.62px;
    line-height: 1.55;
    color: var(--maxGreyFont);
    overflow-wrap: anywhere;
  }

  /* ===== 收尾卡 ===== */
  .profile-footer {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .profile-footer-eyebrow {
    font-size: 11.26px;
    font-weight: 780;
    letter-spacing: 0.12em;
    color: var(--ui-accent);
  }

  .profile-footer-text {
    margin: 0;
    font-size: 14.96px;
    line-height: 1.7;
    color: var(--maxGreyFont);
    overflow-wrap: anywhere;
  }

  .profile-footer-actions {
    display: flex;
    margin-top: 4px;
  }

  .profile-footer-actions .ui-btn {
    min-height: 37.6px;
    padding: 8.8px 14.4px;
    border-radius: 10.4px;
    font-size: 13.73px;
    font-weight: 720;
  }

  /* ===== 右栏 ===== */
  .rail-kicker {
    font-size: 11.26px;
    font-weight: 780;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--greyFontColor);
  }

  .rail-clock {
    margin-top: 6px;
    font-size: 31.68px;
    font-weight: 800;
    letter-spacing: -0.03em;
    line-height: 1.1;
    font-variant-numeric: tabular-nums;
    color: var(--fontColor);
  }

  .rail-greeting {
    margin-top: 2px;
    font-size: 14.96px;
    color: var(--maxGreyFont);
  }

  .rail-date {
    margin-top: 6px;
    font-size: 11.97px;
    color: var(--greyFontColor);
  }

  .rail-stat-list {
    display: grid;
    gap: 5.6px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .rail-stat {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 8px;
    padding: 8px 7.2px;
    border: 1px solid var(--cardBorder);
    border-radius: 8.8px;
  }

  .rail-stat-label {
    flex: none;
    font-size: 10.91px;
    color: var(--greyFontColor);
  }

  .rail-stat-value {
    min-width: 0;
    font-size: 12.67px;
    font-weight: 780;
    text-align: right;
    font-variant-numeric: tabular-nums;
    color: var(--fontColor);
    overflow-wrap: anywhere;
  }

  .progress-list {
    display: grid;
    gap: 10px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .progress-head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 8px;
    font-size: 11.44px;
  }

  .progress-label {
    font-weight: 680;
    color: var(--fontColor);
  }

  .progress-value {
    color: var(--greyFontColor);
  }

  .progress-track {
    height: 5px;
    margin-top: 5px;
    border-radius: 999px;
    background: var(--subtleBg);
    overflow: hidden;
  }

  .progress-fill {
    height: 100%;
    border-radius: 999px;
    background: var(--ui-accent);
    transition: width 0.4s ease;
  }

  /* 参考站在 900 / 640 两级收拢 */
  @media (max-width: 1000px) {
    .about-container {
      grid-template-columns: minmax(0, 1fr);
    }

    .about-aside {
      order: 2;
    }

    .about-main {
      order: 1;
    }
  }

  @media (max-width: 900px) {
    .about-profile {
      grid-template-columns: auto minmax(0, 1fr);
      gap: 24px 18.4px;
    }

    .profile-actions {
      grid-column: 1 / -1;
      flex-direction: row;
      width: 100%;
      min-width: 0;
    }

    .profile-action {
      flex: 1 1 0;
    }

    .about-columns {
      grid-template-columns: minmax(0, 1fr);
    }

    /* 单列后必须清掉左右列定位，否则 grid-column: 2 会生成隐式列把主列挤成 0 宽 */
    .about-col--main,
    .about-col--side {
      grid-column: 1;
    }

    .about-col--side {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .about-col--side > .about-card:last-child {
      grid-column: 1 / -1;
    }
  }

  @media (max-width: 767px) {
    .about-container {
      width: var(--ui-page-inline);
      max-width: var(--ui-page-width);
      padding: var(--ui-page-top-banner) var(--ui-page-gutter) var(--ui-page-bottom);
    }

    .about-main {
      gap: var(--ui-page-gap);
    }

    .about-card,
    .about-col--side .about-card {
      padding: 18px 16px;
      border-radius: var(--ui-card-radius);
    }

    .about-profile {
      gap: 14px 11.9px;
      padding: 18px 16px;
    }

    .profile-avatar {
      width: 70px;
      height: 70px;
    }

    .profile-kicker {
      gap: 5.2px;
      font-size: 8.62px;
      letter-spacing: 0.08em;
    }

    .profile-kicker-dot {
      width: 6.72px;
      height: 6.72px;
      box-shadow: 0 0 0 2.8px rgba(52, 199, 89, 0.14);
    }

    .profile-kicker-sep,
    .profile-kicker-host {
      display: none;
    }

    .profile-name {
      margin-top: 5.6px;
      font-size: 23.87px;
    }

    .profile-subtitle {
      font-size: 12.01px;
    }

    .profile-tagline {
      margin-top: 8.4px;
      font-size: 12.94px;
      line-height: 1.65;
    }

    .profile-status {
      margin-top: 8.4px;
      font-size: 10.47px;
    }

    .profile-actions {
      gap: 6.3px;
    }

    .profile-action {
      min-height: 30.8px;
      padding: 7px 7.7px;
      border-radius: 9.1px;
      font-size: 11.09px;
    }

    .about-nav {
      min-height: 44px;
      padding: 0 6px;
    }

    .about-tab {
      gap: 5px;
      padding: 0 9px;
      font-size: 12px;
    }

    .about-tab--active::after {
      right: 9px;
      left: 9px;
    }

    .about-tab-count {
      min-width: 19px;
      height: 17px;
      padding: 0 5px;
      font-size: 10px;
    }

    .moments-card {
      min-height: 240px;
      padding: 32px 16px;
    }

    .moments-title {
      font-size: 16px;
    }

    .moments-desc {
      font-size: 12px;
    }

    .about-columns,
    .about-col,
    .about-col--side {
      grid-template-columns: minmax(0, 1fr);
    }

    .about-card-title {
      font-size: 15.84px;
    }

    .readme-h2 {
      font-size: 17.86px;
    }

    .readme-p {
      font-size: 13.24px;
      line-height: 1.8;
    }

    .readme-facts {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .chip {
      font-size: 11.09px;
    }

    .project-list {
      grid-template-columns: minmax(0, 1fr);
      gap: 8.12px;
    }

    .project-card {
      padding: 10.08px;
      border-radius: 10.1px;
    }

    .server-meta {
      font-size: 10.56px;
    }

    .timeline-title {
      font-size: 12.32px;
    }

    .timeline-desc {
      font-size: 11.09px;
    }
  }
</style>
