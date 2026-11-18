<template>
  <div class="friend-wrap">
    <!-- 左侧卡片 -->
    <div class="friend-aside">
      <MyAside @selectSort="selectSort" @selectArticle="selectArticle"></MyAside>
    </div>

    <div class="friend-main">
      <!-- 说明 -->
      <div class="friend-head">
        <div class="friend-head-row">
          <svg class="friend-head-icon" viewBox="0 0 24 24" width="26" height="26" fill="none"
               stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
               aria-hidden="true">
            <path d="m11 17 2 2a1 1 0 1 0 3-3"/>
            <path d="m14 14 2.5 2.5a1 1 0 1 0 3-3l-3.88-3.88a3 3 0 0 0-4.24 0l-.88.88a1 1 0 1 1-3-3l2.81-2.81a5.79 5.79 0 0 1 7.06-.87l.47.28a2 2 0 0 0 1.42.25L21 4"/>
            <path d="m21 3 1 11h-2"/>
            <path d="M3 3 2 14l6.5 6.5a1 1 0 1 0 3-3"/>
            <path d="M3 4h8"/>
          </svg>
          <h1 class="friend-head-title">友链</h1>
        </div>
        <p class="friend-head-subtitle">欢迎交换友链，申请方式见下方说明。</p>
      </div>

      <!-- 搜索 -->
      <div class="friend-search">
        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
          <path fill="currentColor"
                d="M9.5 16q-2.725 0-4.612-1.888T3 9.5t1.888-4.612T9.5 3t4.613 1.888T16 9.5q0 1.1-.35 2.075T14.7 13.3l5.6 5.6q.275.275.275.7t-.275.7-.7.275-.7-.275l-5.6-5.6q-.75.6-1.725.95T9.5 16m0-2q1.875 0 3.188-1.312T14 9.5t-1.312-3.187T9.5 5T6.313 6.313T5 9.5t1.313 3.188T9.5 14"/>
        </svg>
        <input v-model="keyword" maxlength="32" type="search" placeholder="搜索" aria-label="搜索">
      </div>

      <!-- 分类筛选 -->
      <div class="friend-chips" v-if="categories.length > 1">
        <button type="button"
                class="friend-chip friend-btn"
                :class="{'friend-chip-active': activeCategory === '', 'friend-btn--strong': activeCategory === ''}"
                @click="activeCategory = ''">全部
        </button>
        <button type="button"
                class="friend-chip friend-btn"
                v-for="(category, index) in categories"
                :key="index"
                :class="{'friend-chip-active': activeCategory === category, 'friend-btn--strong': activeCategory === category}"
                @click="activeCategory = category">{{category}}
        </button>
      </div>

      <span class="friend-count">{{filteredFriends.length}} 个友链</span>

      <!-- 友链列表：全部友链共用一条连续栅格，顺序为 主理人 → 推荐 → 优秀 → 普通 -->
      <div class="friend-grid" v-if="!$common.isEmpty(filteredFriends)">
            <a class="friend-card"
               :class="['tier-' + (item.tier || 0), {'friend-card--hue': avatarStyle(item) !== null, 'friend-card--policy': item.healthStatus === 2}]"
               v-for="(item, index) in sortedFriends"
               :key="index"
               :href="item.url"
               target="_blank"
               rel="noopener noreferrer"
               :aria-label="item.title"
               :data-friend-recommended="item.recommended === 1"
               :data-friend-health="item.healthStatus || 0"
               :style="avatarStyle(item) || undefined"
               @mouseenter="onCardEnter($event)"
               @click="onCardClick(item)">
              <!-- 卡片底图装饰层：取该友链头像（与参考站一致），左侧 40% 渐隐，内容层在其上方 -->
              <span class="friend-card-surface" aria-hidden="true">
                <img v-if="friendBackdrop(item)"
                     class="friend-card-backdrop"
                     :src="friendBackdrop(item)"
                     alt=""
                     loading="lazy"
                     decoding="async"
                     @error="markCoverError(friendBackdrop(item))">
                <span class="friend-card-tint"></span>
                <!-- 背景动效按档次绑定：主理人档花瓣飘落、普通档流星、其余星点流；减少动态时静止 -->
                <span class="friend-recommended-flow" aria-hidden="true">
                  <canvas class="friend-recommended-canvas" :ref="el => setStarCanvas(el, item.id)"></canvas>
                </span>
              </span>
              <!-- 悬停预览：无封面或加载失败时不渲染，避免空白弹框 -->
              <figure class="friend-site-preview" :class="{'friend-site-preview--below': previewBelow}"
                      v-if="friendCover(item)" aria-hidden="true">
                <img :src="friendCover(item)" alt="" loading="lazy" decoding="async"
                     @error="markCoverError(friendCover(item))">
              </figure>
              <span class="friend-card-arrow" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="18" height="18">
                  <path fill="currentColor"
                        d="m16 8.4l-8.9 8.9q-.275.275-.7.275t-.7-.275t-.275-.7t.275-.7L14.6 7H7q-.425 0-.712-.288T6 6t.288-.712T7 5h10q.425 0 .713.288T18 6v10q0 .425-.288.713T17 17t-.712-.288T16 16z"/>
                </svg>
              </span>
              <span class="friend-card-avatar-wrap">
                <!-- 头像取该友链自己的头像地址，未填时沿用封面 -->
                <el-avatar class="friend-card-avatar" :size="64" shape="circle" :src="item.avatar || item.cover"></el-avatar>
              </span>
              <div class="friend-card-info">
                <div class="friend-card-heading">
                  <span class="friend-card-title" :data-title="item.title">{{item.title}}</span>
                  <!-- 响应时间：快/中/慢分档，探测节点测得，非访客本机延迟 -->
                  <span class="friend-latency" :class="latencyClass(item)"
                        v-if="hasLatency(item)" :title="latencyTitle(item)" :aria-label="latencyTitle(item)">
                    <span class="friend-latency-dot" aria-hidden="true"></span>
                    <span>{{latencyText(item)}}</span>
                    <span class="friend-click-count-unit" aria-hidden="true">{{item.healthStatus === 1 ? "" : "ms"}}</span>
                  </span>
                </div>
                <p class="friend-card-desc">{{item.introduction}}</p>
                <!-- 标签 chips：无标签时整行不渲染 -->
                <div class="friend-card-tags" v-if="friendTags(item).length">
                  <span class="friend-card-tag" v-for="tag in friendTags(item)" :key="tag">{{tag}}</span>
                </div>
              </div>
              <!-- 底行：推荐徽标（如有）+ RSS 最近更新 + 点击量；三者都为空时不渲染 -->
              <div class="friend-card-footer" v-if="showMeta(item, item.tier || 0)">
                <div class="friend-card-footer__identity">
                  <!-- 档次徽标：仅「优秀友链」档，与推荐徽标相互独立 -->
                  <span class="friend-card-badge friend-card-badge--tier" v-if="(item.tier || 0) === 3">
                    <svg viewBox="0 0 24 24" role="presentation" aria-hidden="true">
                      <path fill="currentColor"
                            d="M12 3.1c.7 0 1.32.42 1.6 1.06l1.41 3.32c.18.43.58.73 1.04.77l3.59.32c.7.06 1.29.54 1.49 1.22.2.67-.03 1.4-.59 1.82l-2.73 2.08c-.37.28-.53.76-.4 1.2l.84 3.49c.17.69-.09 1.41-.68 1.79-.58.39-1.35.39-1.94 0l-3.02-1.96a1.37 1.37 0 0 0-1.5 0l-3.02 1.96c-.59.39-1.36.39-1.94 0-.59-.38-.85-1.1-.68-1.79l.84-3.49c.13-.44-.03-.92-.4-1.2l-2.73-2.08c-.56-.42-.79-1.15-.59-1.82.2-.68.79-1.16 1.49-1.22l3.59-.32c.46-.04.86-.34 1.04-.77l1.41-3.32A1.73 1.73 0 0 1 12 3.1Z"/>
                    </svg>
                    <span>优秀友链</span>
                  </span>
                  <!-- 主理人档专属：今生网络（站点名）与本站主理人，图标取自留言列表徽标 -->
                  <template v-if="(item.tier || 0) === 2">
                    <span class="friend-card-badge friend-card-badge--site" v-if="webSiteName" :title="webSiteName">
                      <svg viewBox="0 0 24 24" role="presentation" aria-hidden="true">
                        <path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
                      </svg>
                      <span>{{webSiteName}}</span>
                    </span>
                    <span class="friend-card-badge friend-card-badge--boss" title="本站主理人">
                      <svg viewBox="0 0 24 24" role="presentation" aria-hidden="true">
                        <path fill="currentColor" d="M5 16 3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm0 2h14v2H5z"/>
                      </svg>
                      <span>主理人</span>
                    </span>
                  </template>
                  <!-- 参考站同款：圆角大拇指 + 文案，与最近更新同一行 -->
                  <span class="friend-card-badge" v-if="item.recommended === 1">
                    <svg viewBox="0 0 24 24" role="presentation" aria-hidden="true">
                      <path fill="currentColor"
                            d="M5 10.4H4.1A1.5 1.5 0 0 0 2.6 11.9v7a1.5 1.5 0 0 0 1.5 1.5H5a1.2 1.2 0 0 0 1.2-1.2v-7.6A1.2 1.2 0 0 0 5 10.4ZM7.5 10.4c1.9-1.9 4.1-4.1 4.9-5.2 1.1-1.6 3-1.6 3.9 0 .5.9.6 2 .1 3.1 1.6-.3 3.4.3 4.1 1.4.5.8.5 1.6.4 2.5l-.5 5.4c-.2 1.6-1.5 2.8-3.1 2.8H9.1c-1.2 0-2.1-.9-2.1-2.1v-6.9c0-.4.2-.7.5-1Z"/>
                    </svg>
                    <span>{{recommendLabel(item)}}</span>
                  </span>
                  <time class="friend-rss-updated" v-if="rssDate(item)" :datetime="item.rssUpdatedAt"
                        :title="rssTitle(item)" :aria-label="rssTitle(item)">最近更新于 {{rssDate(item)}}</time>
                </div>
                <div class="friend-card-footer__metrics" v-if="clickTotal(item) > 0">
                  <span class="friend-click-count" :title="'从本站点击 ' + clickTotal(item) + ' 次'"
                        :aria-label="'从本站点击 ' + clickTotal(item) + ' 次'">
                    <svg viewBox="0 0 24 24" width="12.8" height="12.8" aria-hidden="true">
                      <path fill="currentColor"
                            d="M12 8q-1.65 0-2.825 1.175T8 12q0 1.125.563 2.075t1.562 1.475q.4.2.563.587t-.013.788q-.175.35-.525.525t-.7 0q-1.575-.75-2.512-2.225T6 12q0-2.5 1.75-4.25T12 6q1.775 0 3.263.938T17.475 9.5q.15.35-.012.7t-.513.5q-.4.175-.8 0t-.6-.575q-.525-1-1.475-1.562T12 8m0-4Q8.65 4 6.325 6.325T4 12q0 3.15 2.075 5.4t5.2 2.55q.425.05.737.375t.288.75t-.313.7t-.712.25q-1.95-.125-3.638-.975t-2.95-2.213t-1.975-3.125T2 12q0-2.075.788-3.9t2.137-3.175T8.1 2.788T12 2q3.925 0 6.838 2.675t3.187 6.6q.05.4-.237.688t-.713.312t-.762-.275t-.388-.725q-.375-3-2.612-5.137T12 4m7.55 17.5l-3.3-3.275l-.75 2.275q-.125.35-.475.338t-.475-.363L12.275 12.9q-.1-.275.125-.5t.5-.125l7.575 2.275q.35.125.363.475t-.338.475l-2.275.75l3.3 3.3q.425.425.425.975t-.425.975t-.987.425t-.988-.425"/>
                    </svg>
                    <span aria-live="polite">{{clickTotal(item)}}</span>
                    <span class="friend-click-count-unit" aria-hidden="true">次</span>
                  </span>
                </div>
              </div>
              <!-- 可用性/违规覆盖层：失联与违规时显示，不拦截点击 -->
              <div class="friend-card-status-overlay" v-if="item.healthStatus === 1 || item.healthStatus === 2"
                   aria-hidden="true">
                <div class="friend-card-remediation-overlay">
                  <span class="friend-card-remediation-title" :title="item.title">{{item.title}}</span>
                  <div class="friend-card-status-badge" v-if="item.healthStatus === 1">
                    <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
                      <path fill="currentColor"
                            d="M6.5 20q-2.3 0-3.9-1.6T1 14.5q0-1.925 1.188-3.425T5.25 9.15q.075-.2.15-.387t.15-.413L2.1 4.9q-.275-.275-.275-.7t.275-.7t.7-.275t.7.275l17 17q.275.275.288.688t-.288.712q-.275.275-.687.288t-.713-.263L17.15 20zm15.1-1.25L8.05 5.225q.875-.6 1.863-.912T12 4q2.925 0 4.963 2.038T19 11q1.725.2 2.863 1.488T23 15.5q0 .975-.375 1.813T21.6 18.75"/>
                    </svg>
                    <span>短暂失联</span>
                  </div>
                  <template v-else>
                    <span class="friend-card-policy-label">
                      <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
                        <path fill="currentColor"
                              d="M2.725 21q-.275 0-.5-.137t-.35-.363t-.137-.488t.137-.512l9.25-16q.15-.25.388-.375T12 3t.488.125t.387.375l9.25 16q.15.25.138.513t-.138.487t-.35.363t-.5.137zm9.988-3.287Q13 17.425 13 17t-.288-.712T12 16t-.712.288T11 17t.288.713T12 18t.713-.288m0-3Q13 14.425 13 14v-3q0-.425-.288-.712T12 10t-.712.288T11 11v3q0 .425.288.713T12 15t.713-.288"/>
                      </svg>
                      <span>违规友链</span>
                    </span>
                    <time class="friend-card-remediation-countdown" v-if="remediationText(item)"
                          aria-live="off">{{remediationText(item)}}</time>
                  </template>
                </div>
              </div>
            </a>
      </div>
      <div class="friend-empty" v-else>暂无友链</div>

      <!-- 本站信息 / 申请友链 -->
      <div class="friend-pair">
      <!-- 本站信息 -->
      <div class="friend-box">
        <div class="box-title">
          <span class="box-title-icon">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <circle cx="12" cy="12" r="8.2" fill="none" stroke="currentColor" stroke-width="1.8"/>
              <ellipse cx="12" cy="12" rx="3.6" ry="8.2" fill="none" stroke="currentColor"
                       stroke-width="1.8"/>
              <path d="M3.8 12h16.4" fill="none" stroke="currentColor" stroke-width="1.8"/>
            </svg>
          </span>
          <span>本站信息</span>
        </div>
        <div class="site-head">
          <div class="site-avatar-wrap">
            <el-avatar class="site-avatar" :size="76" shape="square" :src="siteAvatar"></el-avatar>
            <span class="site-badge" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="12" height="12">
                <path fill="currentColor" d="m9.55 17.3-4.3-4.3 1.4-1.4 2.9 2.9 7.9-7.9 1.4 1.4z"/>
              </svg>
            </span>
          </div>
          <div class="site-meta">
            <div class="site-name">{{siteName}}</div>
            <div class="site-desc">{{siteDesc}}</div>
          </div>
        </div>
        <div class="site-field" v-for="(field, index) in siteFields" :key="index">
          <div class="site-field-text">
            <span class="site-field-label">{{field.label}}</span>
            <span class="site-field-value">{{field.value}}</span>
          </div>
          <button type="button" class="site-copy friend-btn friend-btn--icon" title="复制" @click="copyText(field.value)">
            <svg viewBox="0 0 24 24" width="15" height="15">
              <path fill="currentColor"
                    d="M9 18q-.825 0-1.412-.587T7 16V4q0-.825.588-1.412T9 2h9q.825 0 1.413.588T20 4v12q0 .825-.587 1.413T18 18zm0-2h9V4H9zm-4 6q-.825 0-1.412-.587T3 20V6h2v14h11v2z"/>
            </svg>
          </button>
        </div>
        <!-- 一键复制：按「标签：值」逐行拼接当前有值的字段 -->
        <div class="site-copy-all">
          <button type="button" class="site-copy-all-btn friend-btn" @click="copyAllSiteFields()">
            <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
              <path fill="currentColor"
                    d="M9 18q-.825 0-1.412-.587T7 16V4q0-.825.588-1.412T9 2h9q.825 0 1.413.588T20 4v12q0 .825-.587 1.413T18 18zm0-2h9V4H9zm-4 6q-.825 0-1.412-.587T3 20V6h2v14h11v2z"/>
            </svg>
            <span>一键复制</span>
          </button>
        </div>
      </div>

      <!-- 申请友链 -->
      <div class="friend-box">
        <div class="box-title">
          <span class="box-title-icon">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <rect x="5" y="4.5" width="14" height="16.5" rx="2.5" fill="none" stroke="currentColor"
                    stroke-width="1.8"/>
              <path d="M9.5 3h5a1 1 0 0 1 1 1v2h-7V4a1 1 0 0 1 1-1z" fill="none" stroke="currentColor"
                    stroke-width="1.8"/>
              <path d="M8.8 11h6.4M8.8 15h4.6" fill="none" stroke="currentColor" stroke-width="1.8"
                    stroke-linecap="round"/>
            </svg>
          </span>
          <span>申请友链</span>
        </div>
        <ol class="friend-steps">
          <li class="friend-step">
            <span class="friend-step-index">1</span>
            <div class="friend-step-body">
              <p class="friend-step-title">添加本站友链</p>
              <p class="friend-step-desc">请先在您的网站友链页面添加本站信息，可直接复制上方各字段</p>
            </div>
          </li>
          <li class="friend-step">
            <span class="friend-step-index">2</span>
            <div class="friend-step-body">
              <p class="friend-step-title">填写申请</p>
              <p class="friend-step-desc">在下方填写您的名称、简介、封面、头像、网址与 RSS 地址后提交</p>
            </div>
          </li>
          <li class="friend-step">
            <span class="friend-step-index">3</span>
            <div class="friend-step-body">
              <p class="friend-step-title">等待审核</p>
              <p class="friend-step-desc">管理员审核通过后，您的站点会展示在上方列表中</p>
            </div>
          </li>
        </ol>

        <div class="friend-form">
          <div class="friend-form-row">
            <label>站点名称</label>
            <el-input maxlength="30" v-model="friend.title" placeholder="您的站点名称"></el-input>
          </div>
          <div class="friend-form-row">
            <label>站点描述</label>
            <el-input maxlength="120" v-model="friend.introduction" placeholder="一句话介绍您的站点"></el-input>
          </div>
          <div class="friend-form-row">
            <label>主页截图</label>
            <el-input maxlength="200" v-model="friend.cover" placeholder="头像或封面图片地址"></el-input>
          </div>
          <div class="friend-form-row">
            <label>头像链接</label>
            <el-input maxlength="500" v-model="friend.avatar" placeholder="头像图片地址"></el-input>
          </div>
          <div class="friend-form-row">
            <label>站点链接</label>
            <el-input maxlength="200" v-model="friend.url" placeholder="https://"></el-input>
          </div>
          <div class="friend-form-row">
            <label>RSS 地址</label>
            <el-input maxlength="500" v-model="friend.rssUrl" placeholder="您的 RSS 地址"></el-input>
          </div>
          <div class="friend-form-submit">
            <el-button class="friend-btn friend-submit-link" @click="submitFriend()">
              <svg class="friend-submit-link__icon" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                <path fill="currentColor"
                      d="M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z"/>
              </svg>
              <span>提交申请</span>
            </el-button>
          </div>
        </div>
      </div>
      </div>

      <!-- 注意事项 -->
      <div class="friend-box">
        <div class="notes-title">
          <svg viewBox="0 0 24 24" width="17" height="17" aria-hidden="true">
            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="2"/>
            <path fill="currentColor" d="M11 10.5h2V17h-2zM11 7h2v2.2h-2z"/>
          </svg>
          <span>注意事项</span>
        </div>
        <ul class="notes-list">
          <li><strong>互换原则</strong>请先将本站添加到您的友链页面，确认后会添加您的友链</li>
          <li><strong>链接维护</strong>友链网站长期无法访问或内容违规，将会被移除</li>
          <li><strong>内容要求</strong>内容积极向上，不含有任何违法违规内容</li>
          <li><strong>站点要求</strong>支持 HTTPS，以原创内容为主，能够正常访问且有持续更新</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
  import MyAside from "./MyAside.vue";

  export default {
    components: {
      MyAside
    },

    data() {
      return {
        friendList: {},
        keyword: "",
        activeCategory: "",
        badCovers: {},
        previewBelow: false,
        // 点击量回填：id -> 最新真实值（接口返回后覆盖）
        clickOverrides: {},
        friend: {
          title: "",
          introduction: "",
          cover: "",
          avatar: "",
          url: "",
          rssUrl: ""
        }
      }
    },

    computed: {
      categories() {
        let categories = [];
        for (let key in this.friendList) {
          if (!this.$common.isEmpty(this.friendList[key])) {
            categories.push(key);
          }
        }
        return categories;
      },
      allFriends() {
        let list = [];
        for (let key in this.friendList) {
          let friends = this.friendList[key];
          if (!this.$common.isEmpty(friends)) {
            friends.forEach((item) => {
              list.push(Object.assign({category: key}, item));
            });
          }
        }
        return list;
      },
      filteredFriends() {
        let list = this.allFriends;
        if (this.activeCategory !== "") {
          list = list.filter((item) => item.category === this.activeCategory);
        }
        let keyword = this.keyword.trim().toLowerCase();
        if (keyword === "") {
          return list;
        }
        return list.filter((item) => {
          let title = item.title ? item.title.toLowerCase() : "";
          let introduction = item.introduction ? item.introduction.toLowerCase() : "";
          return title.indexOf(keyword) !== -1 || introduction.indexOf(keyword) !== -1;
        });
      },
      // 全量友链排序：本站主理人(2) → 推荐友链(1) → 优秀友链(3) → 普通友链(0)
      sortedFriends() {
        const order = {2: 0, 1: 1, 3: 2, 0: 3};
        return [...this.filteredFriends].sort((a, b) => {
          return (order[a.tier || 0] ?? 4) - (order[b.tier || 0] ?? 4);
        });
      },
      // 本站友链信息：后台「友链管理」写入 sys_config，未配置时回落到常量默认值
      siteConfig() {
        return this.$store.state.sysConfig || {};
      },
      siteName() {
        return this.$common.isEmpty(this.siteConfig.friendWebName)
          ? this.$constant.friendWebName : this.siteConfig.friendWebName;
      },
      siteDesc() {
        return this.$common.isEmpty(this.siteConfig.friendIntroduction)
          ? this.$constant.friendIntroduction : this.siteConfig.friendIntroduction;
      },
      siteAvatar() {
        // 「头像链接」行：有真实头像（当前用户 / 站长上传过的头像）就显示该地址；
        // 内置默认图（.../images/default-avatar.xxx）视为未上传，统一回落到站点默认地址
        const isRealAvatar = (value) => !this.$common.isEmpty(value)
          && String(value).indexOf("/images/default-avatar.") === -1;
        const boss = this.$store.state.bossInfo || {};
        if (isRealAvatar(boss.avatar)) {
          return boss.avatar;
        }
        const info = this.$store.state.webInfo || {};
        if (isRealAvatar(info.avatar)) {
          return info.avatar;
        }
        return this.$constant.friendAvatar;
      },
      siteRss() {
        // 「RSS 地址」行：配置里是有效 http(s) 地址时以配置为准，否则显示默认站点地址
        const raw = this.siteConfig.friendRssUrl;
        return !this.$common.isEmpty(raw) && /^https?:\/\//i.test(String(raw).trim())
          ? raw : this.$constant.friendRss;
      },
      siteUrl() {
        // 「站点链接」行：配置是有效公网地址时以配置为准，为空或本机地址时显示默认站点地址
        const raw = this.$common.isEmpty(this.siteConfig.friendUrl)
          ? "" : String(this.siteConfig.friendUrl).trim();
        const isPublic = /^https?:\/\//i.test(raw)
          && !/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?(\/|$)/i.test(raw);
        return isPublic ? raw : "https://jsnws.com";
      },
      // 站点名称：与留言列表徽标同源，取后台 web_info.web_name，缺失时退回站点标题
      webSiteName() {
        const info = this.$store.state.webInfo || {};
        if (!this.$common.isEmpty(info.webName)) {
          return info.webName;
        }
        const title = Array.isArray(info.webTitle) ? info.webTitle.join("") : info.webTitle;
        return this.$common.isEmpty(title) ? "" : title;
      },
      siteFields() {
        // 新增的两个地址来自后台「友链管理 - 本站友链信息」，未配置时为空、不展示
        const fields = [
          {label: "站点名称", value: this.siteName},
          {label: "站点描述", value: this.siteDesc},
          {label: "站点链接", value: this.siteUrl},
          {label: "头像链接", value: this.siteAvatar},
          {label: "RSS 地址", value: this.siteRss},
          {label: "主页截图", value: this.siteConfig.friendScreenshotUrl}
        ];
        // 值为空的字段连同复制按钮一起隐藏
        return fields.filter((field) => !this.$common.isEmpty(field.value));
      }
    },

    created() {
      // canvas 节点不放进响应式数据，避免 Vue 深度代理影响绘制
      this.starCanvases = {};
      this.starRaf = 0;
      this.getFriends();
    },

    beforeUnmount() {
      if (this.starRaf) {
        cancelAnimationFrame(this.starRaf);
        this.starRaf = 0;
      }
    },

    methods: {
      // 悬停预览只取封面（cover 字段）
      // 卡片底图取头像（参考站一致）：头像为空/非法/加载失败时回退封面，两者都没有则不渲染底图
      friendCover(item) {
        const url = (item.cover || "").trim();
        if (!/^https?:\/\//i.test(url)) {
          return "";
        }
        return this.badCovers[url] ? "" : url;
      },
      friendBackdrop(item) {
        const avatar = (item.avatar || "").trim();
        if (/^https?:\/\//i.test(avatar) && !this.badCovers[avatar]) {
          return avatar;
        }
        return this.friendCover(item);
      },
      markCoverError(url) {
        if (url) {
          this.badCovers[url] = true;
        }
      },
      // 推荐文案：后台可自定义，留空按月生成
      recommendLabel(item) {
        const label = (item.recommendLabel || "").trim();
        return label || (new Date().getMonth() + 1) + "月份推荐友链";
      },
      // 标签：后台按英文逗号存储，兼容中文逗号，后台约定前台最多展示 3 个
      friendTags(item) {
        const raw = (item.tags || "").trim();
        if (raw === "") {
          return [];
        }
        return raw.split(/[,，]/).map((tag) => tag.trim()).filter((tag) => tag !== "").slice(0, 3);
      },
      // 响应时间：失联时显示 down 态；其余按 <300ms 快、<800ms 中、其余慢分档
      hasLatency(item) {
        return item.healthStatus === 1 || (item.latencyMs !== null && item.latencyMs !== undefined);
      },
      latencyClass(item) {
        if (item.healthStatus === 1) {
          return "friend-latency--down";
        }
        const latency = Number(item.latencyMs);
        if (latency < 300) {
          return "friend-latency--fast";
        }
        return latency < 800 ? "friend-latency--normal" : "friend-latency--slow";
      },
      latencyText(item) {
        return item.healthStatus === 1 ? "无法访问" : item.latencyMs;
      },
      latencyTitle(item) {
        if (item.healthStatus === 1) {
          const since = this.shortDateTime(item.offlineSince);
          return "探测节点连续无法访问" + (since === "" ? "" : "，自 " + since + " 起") + "；不是访客本机延迟。";
        }
        const at = this.shortDateTime(item.latencyCheckedAt);
        return "HTTP GET 首次响应时间 " + item.latencyMs + " 毫秒" + (at === "" ? "" : "，探测于 " + at) +
          "；探测节点测得，非访客本机延迟。";
      },
      shortDateTime(value) {
        if (!value) {
          return "";
        }
        return String(value).slice(0, 16).replace("T", " ");
      },
      // RSS 最近更新时间：后端返回 yyyy-MM-dd HH:mm，只展示日期
      rssDate(item) {
        return item.rssUpdatedAt ? String(item.rssUpdatedAt).slice(0, 10) : "";
      },
      rssTitle(item) {
        const date = this.rssDate(item);
        const title = (item.rssTitle || "").trim();
        return "RSS 最近文章时间：" + date + (title === "" ? "" : "，" + title);
      },
      // 点击量：本地乐观 +1，接口返回真实值后回填
      clickTotal(item) {
        const override = this.clickOverrides[item.id];
        if (override !== undefined) {
          return override;
        }
        return item.clickCount === null || item.clickCount === undefined ? 0 : item.clickCount;
      },
      // 底行整块：档次徽标、推荐徽标、RSS、点击量任一存在就渲染，避免徽标被隐藏
      showMeta(item, tier) {
        return tier === 2 || tier === 3 || item.recommended === 1 || this.rssDate(item) !== "" || this.clickTotal(item) > 0;
      },
      onCardClick(item) {
        if (!item.id) {
          return;
        }
        this.clickOverrides[item.id] = this.clickTotal(item) + 1;
        const url = this.$constant.baseURL + "/webInfo/friendClick?id=" + encodeURIComponent(item.id);
        // keepalive 保证跳转新标签页后请求仍会发出；成功后用接口真实值回填
        fetch(url, {method: "POST", keepalive: true})
          .then((response) => response.json())
          .then((res) => {
            if (res && res.code === 200 && typeof res.data === "number") {
              this.clickOverrides[item.id] = res.data;
            }
          })
          .catch(() => {
          });
      },
      // 头像主色相/饱和度：仅当后台填了两个值时才启用滤镜
      avatarStyle(item) {
        if (item.avatarHue === null || item.avatarHue === undefined ||
          item.avatarSaturation === null || item.avatarSaturation === undefined) {
          return null;
        }
        return {
          "--friend-avatar-hue": item.avatarHue,
          "--friend-avatar-saturation": item.avatarSaturation + "%"
        };
      },
      // 违规整改倒计时：按后台整改期限实时换算出剩余时间
      remediationText(item) {
        if (item.healthStatus !== 2 || !item.rectifyDeadline) {
          return "";
        }
        const deadline = this.parseDate(item.rectifyDeadline);
        if (!deadline) {
          return "";
        }
        const diff = deadline.getTime() - Date.now();
        if (diff <= 0) {
          return "整改期限已到";
        }
        const days = Math.floor(diff / 86400000);
        const hours = Math.floor((diff % 86400000) / 3600000);
        const minutes = Math.floor((diff % 3600000) / 60000);
        if (days > 0) {
          return "整改期限：剩 " + days + " 天 " + hours + " 小时";
        }
        return hours > 0 ? "整改期限：剩 " + hours + " 小时 " + minutes + " 分钟"
          : "整改期限：剩 " + minutes + " 分钟";
      },
      parseDate(value) {
        if (!value) {
          return null;
        }
        const date = value instanceof Date ? value : new Date(String(value).replace(/-/g, "/"));
        return isNaN(date.getTime()) ? null : date;
      },
      // 推荐态星点流：canvas 由统一 rAF 循环绘制，减少动态 / 不支持时静态
      setStarCanvas(el, id) {
        if (!el) {
          delete this.starCanvases[id];
          return;
        }
        this.starCanvases[id] = el;
        if (!this.motionAllowed()) {
          // 减少动态：等布局完成（下一帧）再画一帧静止画面，否则 canvas 尺寸还是 0 会画空
          // 取 7100ms 这一帧：流星/花瓣在画面里的分布最完整，仍能看出多条轨迹
          requestAnimationFrame(() => {
            if (el.isConnected) {
              this.drawStars(el, 0, 7100);
            }
          });
          return;
        }
        this.startStars();
      },
      startStars() {
        if (this.starRaf) {
          return;
        }
        let last = 0;
        const step = (timestamp) => {
          // 先把下一帧排上，单个画布绘制异常也不会让整个循环停摆
          this.starRaf = requestAnimationFrame(step);
          const dt = last ? Math.min(42, timestamp - last) : 0;
          last = timestamp;
          Object.keys(this.starCanvases).forEach((key) => {
            try {
              this.drawStars(this.starCanvases[key], dt);
            } catch (error) {
              // 忽略单帧异常，保留循环
            }
          });
        };
        this.starRaf = requestAnimationFrame(step);
      },
      // 星形/拖尾/光晕配色统一从卡片所在档次的 CSS 变量读取，深浅模式各自取对应值
      starPalette(canvas) {
        const theme = document.documentElement.classList.contains("dark") ? "dark" : "light";
        if (canvas._starPalette && canvas._starPalette.theme === theme) {
          return canvas._starPalette;
        }
        const styles = getComputedStyle(canvas);
        const toRgb = (value) => {
          const match = String(value).match(/(\d+(?:\.\d+)?)\s*,\s*(\d+(?:\.\d+)?)\s*,\s*(\d+(?:\.\d+)?)/);
          return match ? [Number(match[1]), Number(match[2]), Number(match[3])] : null;
        };
        const accent = toRgb(styles.getPropertyValue("--fc-accent"));
        const core = toRgb(styles.getPropertyValue("--fc-star-core")) || accent || [255, 255, 255];
        const halo = toRgb(styles.getPropertyValue("--fc-star-halo")) || accent || [255, 255, 255];
        // 参考站三层星点由浅到深；这里把浅层换成本档 halo 与白色混合，深层用本档 core
        const lighten = (rgb, ratio) => rgb.map((channel) =>
          Math.round(channel + (255 - channel) * ratio)).join(", ");
        canvas._starPalette = {
          theme: theme,
          core: core.join(", "),
          halo: halo.join(", "),
          fills: [lighten(halo, 0.52), lighten(halo, 0.24), core.join(", ")],
          stroke: lighten(core, 0.55),
          // 流星头部的高亮色：深色下更接近白，保证暗底有明确亮点（普通档复用）
          meteorHead: lighten(core, theme === "dark" ? 0.82 : 0.74),
          // 花瓣尖端的高亮色：浅色下偏粉，深色下更接近白，保证暗底能认出花形
          petalTip: lighten(core, theme === "dark" ? 0.34 : 0.5)
        };
        return canvas._starPalette;
      },
      // 参考站 recommended-star-flow 的粒子表：固定层级/基线，用 LCG 生成稳定随机量
      createFlowParticles(areaRatio) {
        const layerSlots = [0, 1, 0, 2, 1, 0, 1, 2, 0, 1, 0, 2, 1, 0, 1, 2];
        const baseY = [.2, .7, .43, .77, .17, .57, .32, .64, .82, .5, .25, .73, .16, .84, .39, .58];
        const layerParams = [
          {radius: [3.1, 4.35], alpha: [.56, .7], float: [1.9, 3.5], curve: [2.1, 3.9], glow: [4, 7]},
          {radius: [4.5, 6.25], alpha: [.72, .88], float: [2.9, 5], curve: [3.5, 6], glow: [7, 11]},
          {radius: [6.3, 8.35], alpha: [.88, 1], float: [4, 6.4], curve: [4.8, 7.8], glow: [11, 16]}
        ];
        let seed = 1369948382;
        const random = () => {
          seed = seed * 1664525 + 1013904223 >>> 0;
          return seed / 4294967296;
        };
        const range = (pair) => pair[0] + (pair[1] - pair[0]) * random();
        // 参考站宽卡实测 640×148 用 16 颗；按画布面积等比增减，保证宽卡密度不稀
        const count = Math.min(28, Math.max(16, Math.round(16 * areaRatio)));
        return Array.from({length: count}, (value, index) => {
          const slot = index % 16;
          const layer = layerSlots[slot];
          const params = layerParams[layer];
          return {
            layer: layer,
            phase: index / count,
            baseY: baseY[slot],
            radius: range(params.radius),
            baseAlpha: range(params.alpha),
            floatAmplitude: range(params.float),
            curveAmplitude: range(params.curve),
            glow: range(params.glow),
            xDrift: .8 + random() * (1.3 + layer * .85),
            driftFrequency: .62 + random() * .72,
            curvePhase: random() * Math.PI * 2,
            twinklePhase: random() * Math.PI * 2,
            twinkleSpeed: .88 + random() * 1.05,
            rotation: random() * Math.PI * 2,
            rotationSpeed: (.2 + random() * (.18 + layer * .09)) * (random() > .5 ? 1 : -1),
            hasTrail: layer > 0 || slot % 4 === 0,
            tailScale: 3.2 + random() * 1.7 + layer * .75
          };
        }).sort((a, b) => a.layer - b.layer);
      },
      drawStars(canvas, dt = 0, forceElapsed = null) {
        if (!canvas || !canvas.isConnected) {
          return;
        }
        const rect = canvas.getBoundingClientRect();
        const width = Math.max(1, Math.round(rect.width));
        const height = Math.max(1, Math.round(rect.height));
        if (rect.width === 0 || rect.height === 0) {
          return;
        }
        const ctx = canvas.getContext("2d");
        if (!ctx) {
          return;
        }
        // 参考站 DPR 上限 2，避免高分屏下拖尾模糊
        const dpr = Math.min(window.devicePixelRatio || 1, 2);
        if (canvas.width !== Math.round(width * dpr) || canvas.height !== Math.round(height * dpr)) {
          canvas.width = Math.round(width * dpr);
          canvas.height = Math.round(height * dpr);
          canvas._starFlow = null;
          canvas._dots = null;
        }
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        ctx.clearRect(0, 0, width, height);
        canvas._frames = (canvas._frames || 0) + 1;
        const palette = this.starPalette(canvas);
        // 优秀友链档沿用最早那版圆点星点流；其余档次使用参考站星形流星
        if (canvas.closest(".friend-card.tier-3")) {
          this.drawDotStars(canvas, ctx, width, height, dt, palette);
          return;
        }
        // 本站主理人档：花瓣自上而下飘落，不再使用流星轨迹
        if (canvas.closest(".friend-card.tier-2")) {
          this.drawPetals(canvas, ctx, width, height, dt, forceElapsed, palette);
          return;
        }
        // 普通档：单独一条分支复用原本给主理人档做的 12 条流星，配色跟随该档中性色
        if (canvas.closest(".friend-card.tier-0")) {
          this.drawMeteors(canvas, ctx, width, height, dt, forceElapsed, palette);
          return;
        }
        let flow = canvas._starFlow;
        if (!flow || flow.width !== width || flow.height !== height) {
          flow = canvas._starFlow = {
            width: width,
            height: height,
            particles: this.createFlowParticles(width * height / (640 * 148)),
            elapsed: 0,
            hover: 0
          };
        }
        if (forceElapsed !== null) {
          flow.elapsed = forceElapsed;
        } else {
          const hoverTarget = canvas.closest(".friend-card:hover") ? 1 : 0;
          flow.hover += (hoverTarget - flow.hover) * Math.min(1, dt / 180);
          flow.elapsed += dt * (1 + flow.hover * .14);
        }
        const time = flow.elapsed / 1000;
        // 横向光带：3800ms 扫过一次，星点靠近光带时更亮更大
        const bandProgress = flow.elapsed % 3800 / 3800;
        const bandX = (-.16 + bandProgress * bandProgress * (3 - 2 * bandProgress) * 1.32) * width;
        const bandRadius = Math.max(24, width * .13);
        flow.particles.forEach((particle) => {
          // 4600ms 一条完整行程：左进右出，两端淡入淡出
          const n = (particle.phase + flow.elapsed / 4600) % 1;
          const radius = particle.radius;
          const fadeIn = Math.max(0, Math.min(1, n / .14));
          const fadeOut = Math.max(0, Math.min(1, (1 - n) / .2));
          const fade = fadeIn * fadeIn * (3 - 2 * fadeIn) * (fadeOut * fadeOut * (3 - 2 * fadeOut));
          const drift = Math.sin(time * particle.driftFrequency + particle.twinklePhase);
          const wave = n * Math.PI * 2 + particle.curvePhase;
          const rawX = radius + n * Math.max(1, width - radius * 2) + drift * particle.xDrift;
          const x = Math.max(radius, Math.min(width - radius, rawX));
          const rawY = particle.baseY * height + Math.sin(wave) * particle.curveAmplitude +
            Math.sin(n * Math.PI * 2 - time * .72) * (1.6 + particle.layer * 1.15) + drift * particle.floatAmplitude;
          const margin = radius + Math.min(5, particle.glow * .34) + 2;
          const y = Math.max(margin, Math.min(height - margin, rawY));
          const distance = x - bandX;
          const bandFactor = Math.exp(-(distance * distance) / (2 * bandRadius * bandRadius));
          const twinkle = .9 + Math.sin(time * particle.twinkleSpeed + particle.twinklePhase) * .08;
          const scale = .94 + twinkle * .06 + bandFactor * .24 + flow.hover * .08;
          const alpha = Math.min(1, particle.baseAlpha * fade * (twinkle + bandFactor * .38 + flow.hover * .13));
          const rotation = particle.rotation + time * particle.rotationSpeed + Math.sin(wave) * .08;
          if (particle.hasTrail) {
            this.drawStarTrail(ctx, particle, x, y, radius * scale, wave, alpha, bandFactor, palette, width);
          }
          this.drawFlowStar(ctx, particle, x, y, radius * scale, rotation, alpha, bandFactor, palette);
        });
      },
      // 参考站拖尾：沿运动反方向的渐隐线，长度随层级 3.2~5.65 倍半径
      drawStarTrail(ctx, particle, x, y, radius, wave, alpha, bandFactor, palette, width) {
        const length = radius * particle.tailScale;
        const slope = particle.curveAmplitude * Math.PI * 2 * Math.cos(wave) / Math.max(width, 1);
        const startX = x - length;
        const startY = y - slope * length;
        const gradient = ctx.createLinearGradient(startX, startY, x, y);
        gradient.addColorStop(0, "rgba(" + palette.halo + ", 0)");
        gradient.addColorStop(1, "rgba(" + palette.core + ", " +
          Math.min(.7, alpha * (.45 + bandFactor * .22)).toFixed(3) + ")");
        ctx.save();
        ctx.globalCompositeOperation = "source-over";
        ctx.strokeStyle = gradient;
        ctx.lineWidth = Math.max(.9, radius * .48);
        ctx.lineCap = "round";
        ctx.shadowColor = "rgba(" + palette.halo + ", " + (.38 + bandFactor * .3).toFixed(3) + ")";
        ctx.shadowBlur = 5 + bandFactor * 7;
        ctx.beginPath();
        ctx.moveTo(startX, startY);
        ctx.quadraticCurveTo(x - length * .45, y - slope * length * .24, x - radius * .7, y);
        ctx.stroke();
        ctx.restore();
      },
      // 参考站星形：外半径 = 粒子半径、内半径 0.48、圆角 0.18
      traceRoundedStar(ctx, outer, rotation, round) {
        const inner = outer * .48;
        const points = Array.from({length: 10}, (value, index) => {
          const radius = index % 2 === 0 ? outer : inner;
          const angle = rotation - Math.PI / 2 + index * Math.PI / 5;
          return {x: Math.cos(angle) * radius, y: Math.sin(angle) * radius};
        });
        const first = points[0];
        const last = points[points.length - 1];
        ctx.beginPath();
        ctx.moveTo(first.x + (last.x - first.x) * round, first.y + (last.y - first.y) * round);
        points.forEach((point, index) => {
          const previous = points[(index + points.length - 1) % points.length];
          const next = points[(index + 1) % points.length];
          const inX = point.x + (previous.x - point.x) * round;
          const inY = point.y + (previous.y - point.y) * round;
          const outX = point.x + (next.x - point.x) * round;
          const outY = point.y + (next.y - point.y) * round;
          if (index > 0) {
            ctx.lineTo(inX, inY);
          }
          ctx.quadraticCurveTo(point.x, point.y, outX, outY);
        });
        ctx.closePath();
      },
      drawFlowStar(ctx, particle, x, y, radius, rotation, alpha, bandFactor, palette) {
        ctx.save();
        ctx.translate(x, y);
        ctx.globalCompositeOperation = "source-over";
        ctx.shadowColor = "rgba(" + palette.halo + ", " + (.48 + bandFactor * .3).toFixed(3) + ")";
        ctx.shadowBlur = particle.glow + bandFactor * 10;
        this.traceRoundedStar(ctx, radius, rotation, .18);
        ctx.fillStyle = "rgba(" + palette.fills[particle.layer] + ", " + alpha.toFixed(3) + ")";
        ctx.fill();
        ctx.strokeStyle = "rgba(" + palette.stroke + ", " + Math.min(1, alpha + .16).toFixed(3) + ")";
        ctx.lineWidth = .72 + particle.layer * .18;
        ctx.stroke();
        ctx.restore();
      },
      // 最早那版圆点星点流：小圆点 + 光晕 + 短拖尾，缓慢漂移（仅优秀友链档）
      drawDotStars(canvas, ctx, width, height, dt, palette) {
        if (!canvas._dots || canvas._dots.width !== width || canvas._dots.height !== height) {
          const count = Math.min(90, Math.max(30, Math.round(width * height / 1800)));
          canvas._dots = {
            width: width,
            height: height,
            list: Array.from({length: count}, () => this.createDot(width, height))
          };
        }
        const step = Math.min(3, dt / 16.7);
        const time = performance.now() / 1000;
        canvas._dots.list.forEach((dot) => {
          dot.x += dot.vx * step;
          dot.y += dot.vy * step;
          if (dot.y > height + 8) {
            dot.y = -8;
            dot.x = Math.random() * width;
          }
          if (dot.x < -8) {
            dot.x = width + 8;
            dot.y = Math.random() * height;
          }
          const alpha = Math.min(1, dot.a * (0.62 + 0.38 * Math.sin(time * 1.2 + dot.p)));
          ctx.beginPath();
          ctx.moveTo(dot.x - dot.vx * 5, dot.y - dot.vy * 5);
          ctx.lineTo(dot.x, dot.y);
          ctx.strokeStyle = "rgba(" + palette.halo + ", " + (alpha * 0.28).toFixed(3) + ")";
          ctx.lineWidth = dot.r * 0.9;
          ctx.lineCap = "round";
          ctx.stroke();
          ctx.beginPath();
          ctx.arc(dot.x, dot.y, dot.r * 3.1, 0, Math.PI * 2);
          ctx.fillStyle = "rgba(" + palette.halo + ", " + (alpha * 0.14).toFixed(3) + ")";
          ctx.fill();
          ctx.beginPath();
          ctx.arc(dot.x, dot.y, dot.r, 0, Math.PI * 2);
          ctx.fillStyle = "rgba(" + palette.core + ", " + alpha.toFixed(3) + ")";
          ctx.fill();
        });
      },
      // 主理人档花瓣参数：数量按画布面积自适应，横向铺满整张卡片，疏密仍不均
      createPetals(width, height) {
        // 密度：每约 1900 平方像素一片，限制在 14–34 片；宽卡多、窄卡少，体感密度一致
        const count = Math.max(14, Math.min(34, Math.round((width * height) / 1900)));
        // 尺寸随卡片宽度轻度缩放：窄卡不显拥挤、宽卡不显零碎
        const scale = Math.max(.72, Math.min(1.08, width / 500));
        const petals = [];
        for (let i = 0; i < count; i += 1) {
          // 黄金分割序列做相位，横向等分后再加正负抖动，避免整齐列队
          const phase = (i * .61803398875) % 1;
          const at = (i + .5) / count + (phase - .5) * (.9 / count);
          const size = (10.5 + ((i * 7) % 5) * 1.9 + (i % 2) * .8) * scale;
          petals.push({
            centerX: size * .5 + Math.max(0, Math.min(1, at)) * (width - size),
            size: size,
            width: size * .68,
            duration: 9200 + ((i * 1367) % 9000),
            phase: phase,
            sway: 6 + ((i * 5) % 4) * 2.4,
            swaySpeed: .62 + ((i * 3) % 5) * .14,
            spin: (i % 2 ? 1 : -1) * (.55 + ((i * 3) % 4) * .18),
            tilt: i * 1.37 % (Math.PI * 2),
            alphaScale: .46 + ((i * 3) % 4) * .07
          });
        }
        return petals;
      },
      drawPetals(canvas, ctx, width, height, dt, forceElapsed, palette) {
        const state = canvas._petals || (canvas._petals = {width: 0, height: 0, elapsed: 0, list: null});
        if (state.width !== width || state.height !== height || !state.list) {
          state.width = width;
          state.height = height;
          state.list = this.createPetals(width, height);
        }
        // 减少动态时 forceElapsed 固定，画出静止的一帧：各片停在不同高度、不同角度
        state.elapsed = forceElapsed !== null ? forceElapsed : state.elapsed + dt;
        const seconds = state.elapsed / 1000;
        state.list.forEach((petal) => {
          // 纵向从卡片上沿之外落进、贯穿到下沿之外，横向只做小幅摆动
          const progress = (state.elapsed / petal.duration + petal.phase) % 1;
          const fade = Math.min(1, progress / .1) * Math.min(1, (1 - progress) / .16);
          const alpha = fade * fade * (3 - 2 * fade) * petal.alphaScale;
          if (alpha < .02) {
            return;
          }
          const sway = Math.sin(seconds * petal.swaySpeed + petal.phase * Math.PI * 2);
          // 整卡铺满：左右边缘各留半个花瓣，摆动也不越出画布
          const x = Math.max(petal.size * .5, Math.min(width - petal.size * .5, petal.centerX + sway * petal.sway));
          const y = -petal.size + progress * (height + petal.size * 2);
          // 旋转：整圈自转 + 随摆动轻微回摆；横向压缩模拟花瓣翻面的厚度变化
          const rotation = petal.tilt + seconds * petal.spin + sway * .32;
          const squeeze = .68 + .32 * Math.abs(Math.cos(seconds * petal.swaySpeed * .9 + petal.phase * 5));
          this.drawPetal(ctx, petal, x, y, rotation, squeeze, alpha, palette);
        });
      },
      drawPetal(ctx, petal, x, y, rotation, squeeze, alpha, palette) {
        const half = petal.size / 2;
        const wid = petal.width * squeeze;
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(rotation);
        // 花形：尖端带小缺口的花瓣，根部收窄、两侧对称外鼓，不是圆点
        ctx.beginPath();
        ctx.moveTo(0, half);
        ctx.bezierCurveTo(wid * .7, half * .5, wid * .62, -half * .34, wid * .18, -half);
        ctx.bezierCurveTo(wid * .08, -half * .68, -wid * .08, -half * .68, -wid * .18, -half);
        ctx.bezierCurveTo(-wid * .62, -half * .34, -wid * .7, half * .5, 0, half);
        ctx.closePath();
        const fill = ctx.createLinearGradient(0, -half, 0, half);
        fill.addColorStop(0, "rgba(" + palette.petalTip + ", " + (alpha * .95).toFixed(3) + ")");
        fill.addColorStop(.5, "rgba(" + palette.core + ", " + (alpha * .82).toFixed(3) + ")");
        fill.addColorStop(1, "rgba(" + palette.halo + ", " + (alpha * .5).toFixed(3) + ")");
        ctx.fillStyle = fill;
        ctx.fill();
        // 花瓣中线：一条更亮的细脉，浅底上也能一眼认出是花瓣
        ctx.strokeStyle = "rgba(" + palette.petalTip + ", " + (alpha * .7).toFixed(3) + ")";
        ctx.lineWidth = Math.max(.5, petal.size * .045);
        ctx.beginPath();
        ctx.moveTo(0, half * .86);
        ctx.quadraticCurveTo(wid * .1, 0, 0, -half * .58);
        ctx.stroke();
        ctx.restore();
      },
      // 普通档流星参数：数量按画布面积自适应，起始点铺满整卡全宽
      createMeteors(width, height) {
        // 密度：每约 2100 平方像素一条，限制在 14–22 条；宽窄屏体感一致
        const count = Math.max(14, Math.min(22, Math.round((width * height) / 2100)));
        const scale = Math.max(.55, Math.min(1.12, width / 838));
        const list = [];
        for (let i = 0; i < count; i += 1) {
          // 前 4 条为主流星：更亮更长，负责辨识度；其余为细流星补节奏
          const hero = i < 4;
          const phase = (i * .61803398875) % 1;
          // 起始 x 全宽铺开：等分后再用黄金分割相位抖动，避免整齐列队
          const at = Math.max(0, Math.min(.94,
            (i + .5) / count + (phase - .5) * (.9 / count)));
          const startAt = at * width;
          // 拖尾长度随卡宽收缩：窄卡不糊成一片，宽卡仍能看出飞行方向
          const ratio = hero ? .2 + (i % 3) * .02 : .1 + ((i * 3) % 4) * .018;
          const length = Math.min(width * .34, Math.max(26, ratio * width));
          const travelX = width + length * .5 - startAt;
          const slope = (hero ? .16 : .18) * (i % 2 ? 1 : -1);
          list.push({
            startAt: startAt,
            lane: .12 + ((i * .61803398875 * 1.7) % 1) * .76,
            duration: (hero ? 2800 : 3600) + ((i * 1367) % 4200),
            length: length,
            travelX: travelX,
            width: (hero ? 2.6 + (i % 3) * .3 : 1.4 + ((i * 3) % 4) * .16) * scale,
            head: (hero ? 3.2 : 2) * scale,
            dy: slope * height,
            slopeRatio: slope * height / travelX,
            phase: phase,
            glint: hero ? .95 : .6,
            alphaScale: hero ? 1 : .58,
            duty: hero ? .64 : .44 + (i % 3) * .03
          });
        }
        return list;
      },
      drawMeteors(canvas, ctx, width, height, dt, forceElapsed, palette) {
        const state = canvas._meteors || (canvas._meteors = {width: 0, height: 0, elapsed: 0, list: null});
        if (state.width !== width || state.height !== height || !state.list) {
          state.width = width;
          state.height = height;
          state.list = this.createMeteors(width, height);
        }
        // 减少动态时 forceElapsed 固定，画出静止的一帧（流星停在半途，仍可见轨迹）
        state.elapsed = forceElapsed !== null ? forceElapsed : state.elapsed + dt;
        // 只在卡片可视区内绘制，拖尾不会越出卡面
        ctx.save();
        ctx.beginPath();
        ctx.rect(0, 0, width, height);
        ctx.clip();
        state.list.forEach((meteor) => {
          const cycle = (state.elapsed / meteor.duration + meteor.phase) % 1;
          // 每条只占自己周期的一段，剩余时间是空档，避免同时在场糊成一片
          if (cycle > meteor.duty) {
            return;
          }
          const progress = cycle / meteor.duty;
          const fade = Math.min(1, progress / .18) * Math.min(1, (1 - progress) / .24);
          const alpha = fade * fade * (3 - 2 * fade) * meteor.alphaScale;
          if (alpha < .02) {
            return;
          }
          const headX = meteor.startAt + progress * meteor.travelX;
          const headY = Math.max(6, Math.min(height - 6,
            meteor.lane * height + (progress - .5) * meteor.dy));
          this.drawMeteor(ctx, meteor, headX, headY, alpha, palette);
        });
        ctx.restore();
      },
      drawMeteor(ctx, meteor, x, y, alpha, palette) {
        const tailX = x - meteor.length;
        const tailY = y - meteor.slopeRatio * meteor.length;
        const controlX = (tailX + x) / 2;
        const controlY = (tailY + y) / 2 + meteor.width * 2.6;
        const trail = ctx.createLinearGradient(tailX, tailY, x, y);
        trail.addColorStop(0, "rgba(" + palette.halo + ", 0)");
        trail.addColorStop(.55, "rgba(" + palette.halo + ", " + (alpha * .2).toFixed(3) + ")");
        trail.addColorStop(1, "rgba(" + palette.core + ", " + (alpha * .78).toFixed(3) + ")");
        ctx.save();
        ctx.lineCap = "round";
        ctx.strokeStyle = trail;
        ctx.shadowColor = "rgba(" + palette.halo + ", " + (alpha * .6).toFixed(3) + ")";
        // 外层柔光拖尾
        ctx.globalAlpha = .32;
        ctx.lineWidth = meteor.width * 3.6;
        ctx.shadowBlur = 14;
        ctx.beginPath();
        ctx.moveTo(tailX, tailY);
        ctx.quadraticCurveTo(controlX, controlY, x, y);
        ctx.stroke();
        // 内层实心光刃
        ctx.globalAlpha = 1;
        ctx.lineWidth = meteor.width;
        ctx.shadowBlur = 8;
        ctx.beginPath();
        ctx.moveTo(tailX, tailY);
        ctx.quadraticCurveTo(controlX, controlY, x, y);
        ctx.stroke();
        // 头部：白心 + 本档红色光晕
        const radius = meteor.head * 3.8;
        const glow = ctx.createRadialGradient(x, y, 0, x, y, radius);
        glow.addColorStop(0, "rgba(255, 255, 255, " + (alpha * .95).toFixed(3) + ")");
        glow.addColorStop(.28, "rgba(" + palette.meteorHead + ", " + (alpha * .9).toFixed(3) + ")");
        glow.addColorStop(.62, "rgba(" + palette.halo + ", " + (alpha * .32).toFixed(3) + ")");
        glow.addColorStop(1, "rgba(" + palette.halo + ", 0)");
        ctx.fillStyle = glow;
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        ctx.fill();
        // 头部十字微光：沿飞行方向拉长，和推荐档的星形粒子区分开
        if (meteor.glint > 0) {
          ctx.globalAlpha = alpha * .55 * meteor.glint;
          ctx.strokeStyle = "rgba(255, 255, 255, 1)";
          ctx.lineWidth = Math.max(.6, meteor.width * .5);
          ctx.shadowBlur = 6;
          ctx.beginPath();
          ctx.moveTo(x - meteor.head * 2.8, y - meteor.head * 2.8 * meteor.slopeRatio);
          ctx.lineTo(x + meteor.head * 1.6, y + meteor.head * 1.6 * meteor.slopeRatio);
          ctx.moveTo(x - meteor.head * .45, y + meteor.head * 1.9);
          ctx.lineTo(x + meteor.head * .45, y - meteor.head * 1.9);
          ctx.stroke();
        }
        ctx.restore();
      },
      createDot(width, height) {
        return {
          x: Math.random() * width,
          y: Math.random() * height,
          r: 0.9 + Math.random() * 1.7,
          vx: -0.9 - Math.random() * 0.9,
          vy: 0.8 + Math.random() * 0.8,
          a: 0.5 + Math.random() * 0.5,
          p: Math.random() * Math.PI * 2
        };
      },
      motionAllowed() {
        return !(window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches);
      },
      // 卡片贴近视口顶部时把预览翻到卡片下方，避免越出视口
      onCardEnter(event) {
        const rect = event.currentTarget.getBoundingClientRect();
        const previewHeight = rect.width * 9 / 16 + 13;
        this.previewBelow = rect.top < previewHeight + 16;
      },
      selectSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: "/"});
        }
      },
      copyText(text, successMessage = "已复制") {
        if (this.$common.isEmpty(text)) {
          return;
        }
        if (navigator.clipboard && window.isSecureContext) {
          navigator.clipboard.writeText(text).then(() => {
            this.$message({message: successMessage, type: "success"});
          });
          return;
        }
        let input = document.createElement("textarea");
        input.value = text;
        input.style.position = "fixed";
        input.style.opacity = "0";
        document.body.appendChild(input);
        input.select();
        document.execCommand("copy");
        document.body.removeChild(input);
        this.$message({message: successMessage, type: "success"});
      },
      copyAllSiteFields() {
        // 只拼接当前有值的字段，一行一个「标签：值」
        const text = this.siteFields.map((field) => field.label + "：" + field.value).join("\n");
        this.copyText(text, "已复制全部友链信息");
      },
      submitFriend() {
        if (this.$common.isEmpty(this.$store.state.currentUser)) {
          this.$message({
            message: "请先登录！",
            type: "error"
          });
          return;
        }

        if (this.friend.title.trim() === "") {
          this.$message({
            message: "你还没写名称呢~",
            type: "warning"
          });
          return;
        }

        if (this.friend.introduction.trim() === "") {
          this.$message({
            message: "你还没写简介呢~",
            type: "warning"
          });
          return;
        }

        if (this.friend.cover.trim() === "") {
          this.$message({
            message: "你还没设置封面呢~",
            type: "warning"
          });
          return;
        }

        if (this.friend.url.trim() === "") {
          this.$message({
            message: "你还没写网址呢~",
            type: "warning"
          });
          return;
        }

        this.$http.post(this.$constant.baseURL + "/webInfo/saveFriend", this.friend)
          .then((res) => {
            this.$message({
              type: 'success',
              message: '提交成功，待管理员审核！'
            });
          })
          .catch((error) => {
            this.$message({
              message: error.message,
              type: "error"
            });
          });
      },
      getFriends() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listFriend")
          .then((res) => {
            if (!this.$common.isEmpty(res.data)) {
              // 后台友链管理返回的是平铺列表，按单一分组展示
              this.friendList = Array.isArray(res.data) ? {"全部友链": res.data} : res.data;
            }
          })
          .catch((error) => {
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
  .friend-wrap {
    display: grid;
    grid-template-columns: var(--ui-aside-width) minmax(0, 1fr);
    column-gap: var(--ui-page-gap);
    align-items: start;
    /* 顶部 40px 留白由页面内容容器提供，这里不再重复 */
    margin: 0 auto;
    color: var(--fontColor);
  }

  .friend-aside {
    min-width: 0;
    align-self: stretch;
  }

  .friend-main {
    max-width: none;
    margin: 0;
    border-radius: var(--ui-card-radius);
    border: var(--ui-card-border);
    padding: var(--ui-card-padding-y) var(--ui-card-padding-x);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
  }

  .friend-head {
    padding: 10px 0 4px;
  }

  .friend-head-row {
    display: flex;
    align-items: center;
  }

  .friend-head-icon {
    flex-shrink: 0;
    margin-right: 8px;
  }

  .friend-head-title {
    margin: 0;
    font-size: var(--ui-title-size);
    font-weight: var(--ui-title-weight);
    color: var(--ui-title-color);
    line-height: 1.4;
  }

  .friend-head-subtitle {
    margin: 6px 0 0;
    font-size: var(--ui-subtitle-size);
    color: var(--ui-subtitle-color);
  }

  .friend-search {
    display: flex;
    align-items: center;
    max-width: 640px;
    height: 48px;
    margin: 22px 0 14px;
    padding: 0 18px;
    border-radius: 16px;
    border: 1px solid var(--cardBorder);
    background: var(--subtleBg);
    color: var(--greyFont);
  }

  .friend-search input {
    width: 100%;
    height: 100%;
    margin-left: 10px;
    border: none;
    outline: none;
    background: transparent;
    color: var(--fontColor);
    font-size: 15px;
  }

  .friend-count {
    display: block;
    margin-bottom: 16px;
    font-size: 13px;
    color: var(--maxGreyFont);
  }

  .friend-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }

  /* 分类胶囊的视觉与按压反馈统一走全局 .ui-btn，选中态用 .ui-btn--primary 区分 */

  /* 档次配色取自参考站实测：本站主理人/优秀友链=蓝、推荐=暖金、普通=白（深色下为低透明白） */
  /* 本站主理人：正红系（#E60012 档），底色保持低透明度 */
  .friend-card.tier-2 {
    --fc-bg: rgba(230, 0, 18, .08);
    --fc-bg-hover: rgba(230, 0, 18, .14);
    --fc-accent: rgb(230, 0, 18);
    --fc-title: rgb(230, 0, 18);
    --fc-desc: rgb(150, 60, 66);
    --fc-star-core: rgb(230, 0, 18);
    --fc-star-halo: rgb(230, 0, 18);
    /* 标题流光：本档红色的亮化值，扫过文字时仍属红色系 */
    --fc-shimmer: rgb(255, 196, 202);
    /* 徽标扫光：本档正红的高饱和档，浅底徽标近乎白，需靠这层着色才看得见 */
    --fc-badge-sheen: rgb(240, 92, 112);
  }

  /* 优秀友链：蓝系 */
  .friend-card.tier-3 {
    --fc-bg: rgba(83, 163, 242, .12);
    --fc-bg-hover: rgba(83, 163, 242, .18);
    --fc-accent: rgb(83, 163, 242);
    --fc-title: rgb(23, 139, 192);
    --fc-desc: rgb(63, 113, 133);
    /* 动效色（星点/拖尾/扫光）：取自本档强调色与标题色，JS 读这两个变量绘制 */
    --fc-star-core: rgb(23, 139, 192);
    --fc-star-halo: rgb(83, 163, 242);
    --fc-shimmer: rgb(150, 211, 255);
    --fc-badge-sheen: rgb(58, 148, 220);
  }

  .friend-card.tier-1 {
    --fc-bg: rgba(255, 241, 184, .25);
    --fc-bg-hover: rgba(255, 241, 184, .31);
    --fc-accent: rgb(232, 173, 37);
    --fc-title: rgb(150, 112, 20);
    --fc-desc: rgb(122, 103, 52);
    --fc-star-core: rgb(150, 112, 20);
    --fc-star-halo: rgb(232, 173, 37);
    --fc-shimmer: rgb(255, 224, 138);
    --fc-badge-sheen: rgb(226, 164, 36);
  }

  .friend-card.tier-0 {
    --fc-bg: rgba(255, 255, 255, .26);
    --fc-bg-hover: rgba(255, 255, 255, .36);
    --fc-accent: rgba(255, 246, 219, .9);
    --fc-title: rgb(54, 72, 88);
    --fc-desc: rgb(90, 106, 122);
    --fc-star-core: rgb(54, 72, 88);
    --fc-star-halo: rgb(90, 106, 122);
    --fc-shimmer: rgb(198, 207, 215);
    --fc-badge-sheen: rgb(128, 150, 172);
  }

  /* 深色主理人：提亮正红，暗底可见 */
  html.dark .friend-card.tier-2 {
    --fc-bg: rgba(255, 59, 74, .14);
    --fc-bg-hover: rgba(255, 59, 74, .20);
    --fc-accent: rgb(255, 59, 74);
    --fc-title: rgb(255, 120, 130);
    --fc-desc: rgb(226, 170, 175);
    --fc-star-core: rgb(255, 120, 130);
    --fc-star-halo: rgb(255, 59, 74);
    --fc-shimmer: rgb(255, 206, 210);
    /* 深色徽标扫光：暗底不用压饱和，取亮一档的红才不会发灰 */
    --fc-badge-sheen: rgb(255, 146, 162);
  }

  html.dark .friend-card.tier-3 {
    --fc-bg: rgba(99, 179, 255, .16);
    --fc-bg-hover: rgba(99, 179, 255, .22);
    --fc-accent: rgb(99, 179, 255);
    --fc-title: rgb(147, 205, 255);
    --fc-desc: rgb(176, 196, 212);
    /* 深色用本档亮化色，保证暗底可见 */
    --fc-star-core: rgb(147, 205, 255);
    --fc-star-halo: rgb(99, 179, 255);
    --fc-shimmer: rgb(214, 240, 255);
    --fc-badge-sheen: rgb(126, 198, 255);
  }

  html.dark .friend-card.tier-1 {
    --fc-bg: rgba(255, 241, 184, .14);
    --fc-bg-hover: rgba(255, 241, 184, .20);
    --fc-accent: rgb(232, 173, 37);
    --fc-title: rgb(240, 210, 140);
    --fc-desc: rgb(206, 190, 150);
    --fc-star-core: rgb(240, 210, 140);
    --fc-star-halo: rgb(232, 173, 37);
    --fc-shimmer: rgb(255, 236, 186);
    --fc-badge-sheen: rgb(255, 204, 106);
  }

  html.dark .friend-card.tier-0 {
    --fc-bg: rgba(255, 255, 255, .06);
    --fc-bg-hover: rgba(255, 255, 255, .10);
    --fc-accent: rgba(255, 246, 219, .9);
    --fc-title: rgb(230, 232, 234);
    --fc-desc: rgb(170, 178, 186);
    --fc-star-core: rgb(230, 232, 234);
    --fc-star-halo: rgb(170, 178, 186);
    --fc-shimmer: rgb(188, 197, 206);
    --fc-badge-sheen: rgb(176, 196, 218);
  }

  /* 栅格：卡片最小宽 240，按容器宽度自动分栏 */
  .friend-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 16px;
  }

  /* 响应式分档：小屏单列铺满，中屏两列，大屏三列（列宽随容器均分） */
  @media screen and (max-width: 639px) {
    .friend-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }

  @media screen and (min-width: 640px) {
    .friend-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media screen and (min-width: 1280px) {
    .friend-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  .friend-card {
    position: relative;
    display: grid;
    /* 卡片内栅格：64px 头像列 + 文本列，间距 10px */
    grid-template-columns: 64px minmax(0, 1fr);
    align-items: center;
    gap: 10px;
    min-height: 88px;
    padding: 12px 14px;
    /* 参照站实测：四角不等圆角 10 / 7 / 12 / 8 */
    border-radius: 10px 7px 12px 8px;
    /* 参照站常态：1px 实线 rgba(255,246,219,.28)，底色为档次色 12%~26% */
    border: 1px solid color-mix(in srgb, var(--fc-accent, rgba(255, 246, 219, .9)) 40%, transparent);
    background: var(--fc-bg, rgba(255, 255, 255, .26));
    /* 参照站常态：两层同色柔光 0.22/6.72/18.88 + 0.12/0/16.8 */
    box-shadow: 0 6.72px 18.88px color-mix(in srgb, var(--fc-accent, rgba(255, 246, 219, .9)) 22%, transparent),
                0 0 16.8px color-mix(in srgb, var(--fc-accent, rgba(255, 246, 219, .9)) 12%, transparent);
    color: inherit;
    /* 悬停预览要越出卡片边界，卡片本身不能裁切，裁切交给底图层 */
    overflow: visible;
    z-index: 1;
    text-decoration: none;
    transition: border-color .3s, box-shadow .3s, background-color .3s;
  }

  /* 悬停时抬高层级，保证预览压过相邻卡片与分组标题 */
  .friend-card:hover,
  .friend-card:focus-visible {
    z-index: 10;
  }

  /* 参照站 hover：底色加深、描边变虚线、阴影加大，无位移 */
  .friend-card:hover {
    border-style: dashed;
    border-color: color-mix(in srgb, var(--fc-accent, rgba(255, 246, 219, .9)) 47%, transparent);
    background: var(--fc-bg-hover, rgba(255, 255, 255, .36));
    box-shadow: 0 9.28px 22.72px color-mix(in srgb, var(--fc-accent, rgba(255, 246, 219, .9)) 29%, transparent),
                0 0 21.6px color-mix(in srgb, var(--fc-accent, rgba(255, 246, 219, .9)) 17%, transparent);
  }

  /* 主理人档整卡：左侧淡红 → 右侧稍浓，右端与房子/星芒自然衔接；描边和阴影各收一档，避免糊成一整片红 */
  .friend-card.tier-2 {
    background:
      radial-gradient(120% 180% at 92% 50%, color-mix(in srgb, var(--fc-accent) 9%, transparent), transparent 62%),
      linear-gradient(96deg, var(--fc-bg), color-mix(in srgb, var(--fc-bg) 58%, transparent) 56%, var(--fc-bg));
    border-color: color-mix(in srgb, var(--fc-accent) 34%, transparent);
    box-shadow:
      0 8px 22px color-mix(in srgb, var(--fc-accent) 14%, transparent),
      0 0 0 1px color-mix(in srgb, #fff 22%, transparent) inset;
  }

  .friend-card.tier-2:hover {
    background:
      radial-gradient(120% 180% at 92% 50%, color-mix(in srgb, var(--fc-accent) 13%, transparent), transparent 62%),
      linear-gradient(96deg, var(--fc-bg-hover), color-mix(in srgb, var(--fc-bg-hover) 58%, transparent) 56%, var(--fc-bg-hover));
    border-color: color-mix(in srgb, var(--fc-accent) 42%, transparent);
    box-shadow:
      0 10px 26px color-mix(in srgb, var(--fc-accent) 18%, transparent),
      0 0 18px color-mix(in srgb, var(--fc-accent) 11%, transparent),
      0 0 0 1px color-mix(in srgb, #fff 26%, transparent) inset;
  }

  html.dark .friend-card.tier-2 {
    background:
      radial-gradient(120% 180% at 92% 50%, color-mix(in srgb, var(--fc-accent) 12%, transparent), transparent 64%),
      linear-gradient(96deg, var(--fc-bg), color-mix(in srgb, var(--fc-bg) 55%, transparent) 56%, var(--fc-bg));
    border-color: color-mix(in srgb, var(--fc-accent) 40%, transparent);
    box-shadow:
      0 10px 24px rgba(0, 0, 0, .34),
      0 0 0 1px color-mix(in srgb, #fff 6%, transparent) inset;
  }

  html.dark .friend-card.tier-2:hover {
    background:
      radial-gradient(120% 180% at 92% 50%, color-mix(in srgb, var(--fc-accent) 16%, transparent), transparent 64%),
      linear-gradient(96deg, var(--fc-bg-hover), color-mix(in srgb, var(--fc-bg-hover) 55%, transparent) 56%, var(--fc-bg-hover));
    border-color: color-mix(in srgb, var(--fc-accent) 48%, transparent);
    box-shadow:
      0 12px 28px rgba(0, 0, 0, .38),
      0 0 20px color-mix(in srgb, var(--fc-accent) 14%, transparent),
      0 0 0 1px color-mix(in srgb, #fff 8%, transparent) inset;
  }

  /* 底图层：绝对定位铺满、圆角 12px（参照站 surface 覆盖卡片不等圆角）、不接收鼠标事件 */
  .friend-card-surface {
    position: absolute;
    inset: 0;
    z-index: 0;
    overflow: hidden;
    border-radius: 12px;
    pointer-events: none;
  }

  /* 参照站实测：底图占卡片宽 40%、object-fit:cover、居中、opacity .13，右侧渐隐 */
  .friend-card-backdrop {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    width: 40%;
    height: 100%;
    object-fit: cover;
    object-position: 50% 50%;
    opacity: .13;
    -webkit-mask-image: linear-gradient(to right, #000 20%, transparent 100%);
    mask-image: linear-gradient(to right, #000 20%, transparent 100%);
    pointer-events: none;
  }

  /* 主理人档底图：再淡一档、降饱和，渐隐提前到 86%，从左侧溶进红底而不是留一条硬边 */
  .friend-card.tier-2 .friend-card-backdrop {
    width: 38%;
    opacity: .1;
    filter: saturate(.7) contrast(.96);
    -webkit-mask-image: linear-gradient(to right, rgba(0, 0, 0, .92) 2%, rgba(0, 0, 0, .5) 40%, transparent 86%);
    mask-image: linear-gradient(to right, rgba(0, 0, 0, .92) 2%, rgba(0, 0, 0, .5) 40%, transparent 86%);
  }

  html.dark .friend-card.tier-2 .friend-card-backdrop {
    opacity: .085;
    filter: saturate(.6) brightness(.9);
  }

  /* 参照站实测：hover 叠加主色 5% 遮罩，0.3s cubic-bezier(.4,0,.2,1) */
  .friend-card-tint {
    position: absolute;
    inset: 0;
    background: var(--themeBackground);
    opacity: 0;
    transition: opacity .3s cubic-bezier(.4, 0, .2, 1);
    pointer-events: none;
  }

  .friend-card:hover .friend-card-tint {
    opacity: .05;
  }

  /* 背景装饰：按档次取不同线稿（--fc-decor），颜色取所在档次的 --fc-star-* */
  .friend-card-surface::after {
    content: "";
    position: absolute;
    top: 50%;
    right: -.2rem;
    z-index: 0;
    width: 7.2rem;
    height: 7.2rem;
    background: linear-gradient(138deg, var(--fc-star-halo),
                color-mix(in srgb, var(--fc-star-halo) 55%, #fff) 70%, var(--fc-star-core));
    --fc-decor: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 120 120'%3E%3Cg fill='none' stroke='white' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M60 14c4 0 7 3 9 7l10 21 24 4c4 1 7 3 8 7 1 3 0 7-3 10L90 80l4 24c1 4-1 8-4 10-3 2-7 2-11 0L60 102l-19 12c-4 2-8 2-11 0-3-2-5-6-4-10l4-24-18-17c-3-3-4-7-3-10 1-4 4-6 8-7l24-4 10-21c2-4 5-7 9-7Z' stroke-width='4'/%3E%3Cpath d='m23 26 2 6 6 2-6 2-2 6-2-6-6-2 6-2 2-6Zm79 65 1.5 4 4 1.5-4 1.5-1.5 4-1.5-4-4-1.5 4-1.5-4Z' stroke-width='2.5'/%3E%3C/g%3E%3C/svg%3E");
    -webkit-mask-image: var(--fc-decor);
    mask-image: var(--fc-decor);
    mask-position: center;
    mask-repeat: no-repeat;
    mask-size: contain;
    opacity: .24;
    transform: translateY(-50%) rotate(-8deg);
    transition: opacity .3s, transform .5s cubic-bezier(.2, .8, .2, 1);
    pointer-events: none;
  }

  .friend-card:hover .friend-card-surface::after {
    opacity: .36;
    transform: translate(-.2rem, -50%) rotate(-12deg) scale(1.05);
  }

  /* 优秀友链档：通用星形线稿换成月亮。画法与星一致——描边线稿、圆头圆角、
     遮罩填本档蓝系渐变；尺寸、定位、右侧留白、hover 与通用层完全相同，只替换画稿 */
  .friend-card.tier-3 .friend-card-surface::after {
    --fc-decor: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 120 120'%3E%3Cg fill='none' stroke='white' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M57.2 20.1A40 40 0 1 0 99.2 68.1A32 32 0 1 1 57.2 20.1Z' stroke-width='4'/%3E%3Cpath d='m23 26 2 6 6 2-6 2-2 6-2-6-6-2 6-2 2-6Zm79 65 1.5 4 4 1.5-4 1.5-1.5 4-1.5-4-4-1.5 4-1.5 4Z' stroke-width='2.5'/%3E%3C/g%3E%3C/svg%3E");
  }

  /* 本站主理人档卡面扫光：与通用档完全同参数（45% 带宽、3s 线性、0 延迟、同一行程），
     只保留红系粉带与更紧的渐变停靠点，避免在淡粉卡面上摊成一片粉色雾 */
  .friend-card.tier-2 .friend-card-surface::before {
    display: block;
    width: 45%;
    background: linear-gradient(90deg,
      transparent 0,
      transparent 30%,
      color-mix(in srgb, var(--fc-shimmer) 38%, transparent) 41%,
      color-mix(in srgb, var(--fc-shimmer) 66%, #fff) 50%,
      color-mix(in srgb, var(--fc-shimmer) 38%, transparent) 59%,
      transparent 70%,
      transparent 100%);
    transform: translate(-80%) skew(-25deg);
    animation: friend-own-sheen 3s linear infinite;
  }

  /* 与通用扫光同一套行程：四档同刻进入、同刻离开卡面 */
  @keyframes friend-own-sheen {
    0% {
      transform: translate(-80%) skew(-25deg);
    }
    100% {
      transform: translate(205%) skew(-25deg);
    }
  }

  /* 深色单独取值：峰值改用带透明度的红系高光，避免在暗底上发白 */
  html.dark .friend-card.tier-2 .friend-card-surface::before {
    opacity: .58;
    background: linear-gradient(90deg,
      transparent 0,
      transparent 28%,
      color-mix(in srgb, var(--fc-shimmer) 20%, transparent) 40%,
      color-mix(in srgb, var(--fc-shimmer) 48%, transparent) 50%,
      color-mix(in srgb, var(--fc-shimmer) 20%, transparent) 60%,
      transparent 72%,
      transparent 100%);
  }

  .friend-card.tier-2 .friend-card-surface::after {
    right: -.35rem;
    /* 高度取「7rem 与卡片 92%」的较小值，短卡片不会上下裁切房子 */
    width: auto;
    height: min(7rem, 92%);
    aspect-ratio: 160 / 156;
    opacity: .2;
    transform: translateY(-50%) rotate(-2deg);
    transition: opacity .3s ease, transform .3s ease;
    /* 房子画稿：按用户图重绘的线稿（人字屋顶 + 右侧烟囱 + 阁楼心形 + 两扇方窗 + 拱门），
       全部描边、无填充；viewBox 与元素 160/156 同比例，遮罩不留白边 */
    --fc-decor: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 160 156'%3E%3Cg fill='none' stroke='white' stroke-width='6' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M3 75 80 4 157 72'/%3E%3Cpath d='M123 3v38'/%3E%3Cpath d='M18.8 72v69a9 9 0 0 0 9 9h104.4a9 9 0 0 0 9-9V72'/%3E%3Cpath d='M82 75c-9.4-8.6-18-15-18-20.2 0-4.4 3.4-7.8 7.6-7.8 3.5 0 7 2 10.4 5.4 3.4-3.4 6.9-5.4 10.4-5.4 4.2 0 7.6 3.4 7.6 7.8 0 5.2-8.6 11.6-18 20.2Z'/%3E%3Crect x='38' y='79' width='21' height='24' rx='2'/%3E%3Crect x='104' y='79' width='21' height='24' rx='2'/%3E%3Cpath d='M64.7 150V100.9a17.4 17.4 0 0 1 34.8 0V150'/%3E%3C/g%3E%3C/svg%3E");
  }

  /* hover：与参考站一致，整块画稿轻微放大到 1.05，0.3s 过渡；不 hover 时完全静止 */
  .friend-card.tier-2:hover .friend-card-surface::after {
    opacity: .25;
    transform: translateY(-50%) rotate(-2deg) scale(1.05);
  }

  html.dark .friend-card.tier-2 .friend-card-surface::after {
    background: linear-gradient(138deg, color-mix(in srgb, var(--fc-star-halo) 92%, #fff),
                color-mix(in srgb, var(--fc-star-halo) 62%, #fff) 68%, var(--fc-star-core));
  }

  /* ===== 档次装饰层已移除：主理人档背景只保留 头像底图 + 房子 + 花瓣 + 扫光 ===== */

  /* 推荐档：太阳，画法与月亮/星同一套——线稿描边、圆头圆角、遮罩叠本档金色渐变；
     圆盘与八道光芒之间留缝，光芒四正长、四斜短，避免读成齿轮或圆环 */
  .friend-card.tier-1 .friend-card-surface::after {
    --fc-decor: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 120 120'%3E%3Cg fill='none' stroke='white' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='60' cy='60' r='24' stroke-width='4.5'/%3E%3Cpath d='M60 26V8M60 94v18M26 60H8M94 60h18M36 36l-7.8-7.8M84 84l7.8 7.8M84 36l7.8-7.8M36 84l-7.8 7.8' stroke-width='4.5'/%3E%3C/g%3E%3C/svg%3E");
  }

  /* 扫光：四个档次都渲染，参考站实测 45% 宽、-25deg 斜切、3s 线性、translate(-80%)→translate(205%) */
  .friend-card-surface::before {
    content: "";
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: 2;
    width: 45%;
    background: linear-gradient(90deg, transparent 0, transparent 20%,
                color-mix(in srgb, var(--fc-star-halo) 10%, transparent) 35%,
                color-mix(in srgb, var(--fc-star-halo) 48%, transparent) 50%,
                color-mix(in srgb, var(--fc-star-halo) 10%, transparent) 65%,
                transparent 80%, transparent 100%);
    transform: translate(-80%) skew(-25deg);
    animation: friend-recommended-sweep 3s linear infinite;
    will-change: transform;
    pointer-events: none;
  }

  @keyframes friend-recommended-sweep {
    from {
      transform: translate(-80%) skew(-25deg);
    }
    to {
      transform: translate(205%) skew(-25deg);
    }
  }

  /* 推荐态星点流画布：铺满底图层，位于底图之上、内容之下 */
  .friend-recommended-flow {
    position: absolute;
    inset: 0;
    z-index: 1;
    display: block;
    overflow: hidden;
    pointer-events: none;
    isolation: isolate;
    contain: paint;
  }

  .friend-recommended-canvas {
    position: absolute;
    inset: 0;
    z-index: 1;
    display: block;
    width: 100%;
    height: 100%;
    background: transparent;
    opacity: .98;
  }

  /* 推荐态标题辉光：只保留静态文字阴影，呼吸动画交给标题流光，避免两层动效互相干扰 */
  .friend-card[data-friend-recommended="true"] .friend-card-title {
    font-weight: 850;
    letter-spacing: .008em;
    text-shadow: color-mix(in srgb, var(--fc-star-core) 22%, white) 0 1px 0,
                 color-mix(in srgb, var(--fc-star-halo) 55%, transparent) 0 0 .28rem,
                 color-mix(in srgb, var(--fc-star-halo) 34%, transparent) 0 0 .72rem;
  }

  html.dark .friend-card[data-friend-recommended="true"] .friend-card-title {
    text-shadow: color-mix(in srgb, var(--fc-star-core) 30%, white) 0 1px 0,
                 color-mix(in srgb, var(--fc-star-halo) 45%, transparent) 0 0 .32rem,
                 color-mix(in srgb, var(--fc-star-halo) 28%, transparent) 0 0 .8rem;
  }

  /* 深色下底图适当提亮，保证可见性 */
  html.dark .friend-card-backdrop {
    opacity: .18;
  }

  /* 参照站实测：宽=卡片宽-2、16:9、圆角 14px、淡蓝 1px 描边、0 14px 32px 阴影，置于卡片上方 11px */
  .friend-site-preview {
    position: absolute;
    left: 1px;
    right: 1px;
    bottom: calc(100% + 11px);
    z-index: 90;
    margin: 0;
    aspect-ratio: 16 / 9;
    border-radius: 14px;
    border: 1px solid rgba(213, 233, 252, .9);
    background: #fff;
    box-shadow: 0 14px 32px rgba(35, 65, 92, .22);
    overflow: hidden;
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    transition: opacity .22s, visibility .22s;
  }

  .friend-site-preview img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
    /* 参照站实测：object-position 50% 0%，优先露出页面顶部 */
    object-position: 50% 0%;
  }

  /* 卡片贴近视口顶部时翻到下方 */
  .friend-site-preview--below {
    bottom: auto;
    top: calc(100% + 11px);
  }

  .friend-card:hover .friend-site-preview,
  .friend-card:focus-visible .friend-site-preview {
    opacity: 1;
    visibility: visible;
  }

  html.dark .friend-site-preview {
    background: #1f1f22;
    border-color: rgba(255, 255, 255, .16);
    box-shadow: 0 14px 32px rgba(0, 0, 0, .55);
  }

  .friend-card-arrow {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 2;    display: inline-flex;
    color: var(--fc-accent, var(--themeBackground));
    opacity: 0;
    transition: opacity .3s;
  }

  .friend-card:hover .friend-card-arrow {
    opacity: 1;
  }

  .friend-card-avatar-wrap {
    position: relative;
    z-index: 1;
    display: inline-flex;
    flex-shrink: 0;
  }

  /* 头像：64x64 正圆、1px 黑色 5% 描边、object-fit: cover；底图裁切不受影响 */
  .friend-card-avatar {
    border-radius: 50%;
    border: 1px solid rgba(0, 0, 0, .05);
    background: #f4f4f5;
    color: var(--greyFont);
    overflow: hidden;
    transition: border-color .3s;
  }

  html.dark .friend-card-avatar {
    border-color: rgba(255, 255, 255, .05);
    background: #27272a;
  }

  .friend-card:hover .friend-card-avatar {
    border-color: rgba(0, 0, 0, .1);
  }

  html.dark .friend-card-avatar:hover {
    border-color: rgba(255, 255, 255, .12);
  }

  /* 头像色相/饱和度：后台填了 avatar_hue / avatar_saturation 才加滤镜（只改视觉，不改图片） */
  .friend-card--hue .friend-card-avatar :deep(img),
  .friend-card--hue .friend-card-backdrop {
    filter: hue-rotate(calc(var(--friend-avatar-hue, 0) * 1deg)) saturate(var(--friend-avatar-saturation, 100%));
  }

  .friend-card-info {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 2px;
    min-width: 0;
  }

  /* 参照站实测：13px/700/18.2px */
  .friend-card-title {
    position: relative;
    display: block;
    padding-right: 20px;
    overflow: hidden;
    font-size: 13px;
    font-weight: 700;
    line-height: 18.2px;
    /* 名称与星点/扫光同组变量：档次强调色（浅色取深值、深色取亮值） */
    color: var(--fc-star-core, var(--fc-title, var(--fontColor)));
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  /* 标题流光：复制一层标题文字，用 115deg 遮罩把亮带裁到字形上；
     与参考站同一行程（mask-position 100% → 0%）、3s 线性、无延迟 */
  .friend-card-title::after {
    content: attr(data-title);
    position: absolute;
    /* 与标题盒子完全重合，标题多长扫光就覆盖多长，宽度变化不留空白 */
    inset: 0;
    overflow: hidden;
    color: var(--fc-shimmer);
    -webkit-text-fill-color: var(--fc-shimmer);
    /* 覆盖继承来的文字辉光，避免和下层标题叠成两层光 */
    text-shadow: none;
    -webkit-mask-image: linear-gradient(115deg,
      transparent 42%,
      rgba(255, 255, 255, .5) 46%,
      #fff 48%,
      #fff 52%,
      rgba(255, 255, 255, .5) 54%,
      transparent 58%);
    mask-image: linear-gradient(115deg,
      transparent 42%,
      rgba(255, 255, 255, .5) 46%,
      #fff 48%,
      #fff 52%,
      rgba(255, 255, 255, .5) 54%,
      transparent 58%);
    -webkit-mask-size: 238% 100%;
    mask-size: 238% 100%;
    -webkit-mask-repeat: no-repeat;
    mask-repeat: no-repeat;
    -webkit-mask-position: 100% 0;
    mask-position: 100% 0;
    opacity: .9;
    pointer-events: none;
    animation: friend-title-shimmer 3s linear infinite;
  }

  /* 与参考站一致：遮罩从 100% 移到 0%，3s 线性无延迟，与卡面扫光同拍 */
  @keyframes friend-title-shimmer {
    0% {
      -webkit-mask-position: 100% 0;
      mask-position: 100% 0;
    }
    100% {
      -webkit-mask-position: 0% 0;
      mask-position: 0% 0;
    }
  }

  /* hover 只提亮，不改周期、不做位移 */
  .friend-card:hover .friend-card-title::after {
    opacity: 1;
  }

  /* 底行推荐徽标：参考站实测 10px 文字、3px 6px 内边距、20px 高、3.84px 星文间距 */
  .friend-card-badge {
    position: relative;
    display: inline-flex;
    align-items: center;
    flex: 0 1 auto;
    gap: 3.84px;
    min-width: 0;
    min-height: 20px;
    padding: 3px 6px;
    overflow: hidden;
    border: 1px solid color-mix(in srgb, var(--fc-star-halo) 46%, transparent);
    border-radius: 6px;
    background: color-mix(in srgb, var(--fc-star-halo) 14%, transparent);
    font-size: 10px;
    font-weight: 600;
    line-height: 12px;
    color: var(--fc-star-core);
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  /* 优秀友链档次徽标：与推荐徽标同款，文案短，不参与压缩以免被截断 */
  .friend-card-badge--tier {
    flex: 0 0 auto;
  }

  /* 主理人档专属徽标：今生网络取站点名，过长时截断；本站主理人常显 */
  .friend-card-badge--site {
    flex: 0 1 auto;
    min-width: 0;
    max-width: 92px;
  }

  /* 两个徽标统一由本档主题色驱动：图标、文字、扫光同源，深浅两套自动跟随 */
  .friend-card.tier-2 .friend-card-badge--site,
  .friend-card.tier-2 .friend-card-badge--boss {
    --fc-badge-sheen: color-mix(in srgb, var(--fc-star-core) 62%, #fff);
  }

  /* 本站主理人：同一套红，底色与描边提一档，和站点徽标区分 */
  .friend-card.tier-2 .friend-card-footer .friend-card-badge--boss {
    flex: 0 0 auto;
    border-color: color-mix(in srgb, var(--fc-star-core) 52%, transparent);
    background: color-mix(in srgb, var(--fc-star-core) 18%, transparent);
  }

  /* 图标跟随本档主题色变量，与徽标文字同色，不再写死白色 */
  .friend-card-badge.friend-card-badge--site svg,
  .friend-card-badge.friend-card-badge--boss svg {
    color: var(--fc-star-core, currentColor);
    filter: drop-shadow(0 0 1.92px color-mix(in srgb, var(--fc-star-core, currentColor) 25%, transparent));
  }

  /* 文案过长时优先截断徽标，不挤压右侧最近更新时间 */
  .friend-card-badge > span {
    position: relative;
    z-index: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .friend-card-badge svg {
    position: relative;
    z-index: 1;
    flex-shrink: 0;
    width: 13.11px;
    height: 13.11px;
    /* 参考站实测：图标取强调色本体，带 1.92px 同色柔光 */
    color: var(--fc-star-halo);
    filter: drop-shadow(0 0 1.92px color-mix(in srgb, var(--fc-star-halo) 25%, transparent));
  }

  /* 徽标扫光：与标题流光同一套节奏与方向（3s、线性、延迟 0、遮罩 100% → 0%）；
     徽标只有 20px 高，遮罩带宽与停靠点等比收窄（42–58% 收成 46–56%，238% 收成 200%），
     光带更窄更利落，不会糊住 10px 文字 */
  .friend-card-badge::after {
    content: "";
    position: absolute;
    inset: 0;
    z-index: 0;
    border-radius: inherit;
    background: var(--fc-badge-sheen, var(--fc-shimmer));
    -webkit-mask-image: linear-gradient(115deg,
      transparent 46%,
      rgba(255, 255, 255, .5) 48.5%,
      #fff 50%,
      #fff 51.5%,
      rgba(255, 255, 255, .5) 54%,
      transparent 56%);
    mask-image: linear-gradient(115deg,
      transparent 46%,
      rgba(255, 255, 255, .5) 48.5%,
      #fff 50%,
      #fff 51.5%,
      rgba(255, 255, 255, .5) 54%,
      transparent 56%);
    -webkit-mask-size: 200% 100%;
    mask-size: 200% 100%;
    -webkit-mask-repeat: no-repeat;
    mask-repeat: no-repeat;
    -webkit-mask-position: 100% 0;
    mask-position: 100% 0;
    /* 光带压在文字之下，只做柔和高光，不抢 10px 小字的对比度 */
    opacity: .7;
    animation: friend-badge-sheen 3s linear infinite;
    pointer-events: none;
  }

  html.dark .friend-card-badge::after {
    opacity: .6;
  }

  /* 与标题流光同拍：同一 3s 周期、同一线性缓动、同一遮罩行程，名字与徽标同时起扫 */
  @keyframes friend-badge-sheen {
    0% {
      -webkit-mask-position: 100% 0;
      mask-position: 100% 0;
    }
    100% {
      -webkit-mask-position: 0% 0;
      mask-position: 0% 0;
    }
  }

  html.dark .friend-card-badge {
    border-color: color-mix(in srgb, var(--fc-star-halo) 42%, transparent);
    background: color-mix(in srgb, var(--fc-star-halo) 16%, transparent);
    color: var(--fc-star-core);
  }

  /* 参照站实测：11.5px/400/16.1px，单行截断 */
  .friend-card-desc {
    margin: 0;
    overflow: hidden;
    font-size: 11.5px;
    font-weight: 400;
    line-height: 16.1px;
    letter-spacing: .0575px;
    color: var(--fc-desc, var(--maxGreyFont));
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  /* 标题 + 响应时间同一行：标题截断，徽标不压缩 */
  .friend-card-heading {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 6px;
    min-width: 0;
  }

  .friend-card-heading .friend-card-title {
    flex: 0 1 auto;
    min-width: 0;
    padding-right: 0;
  }

  /* 响应时间徽标：10px/500、6px 圆点、2.8px 间距，四档颜色取自参考站 */
  .friend-latency {
    display: inline-flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: flex-end;
    gap: 2.8px;
    min-height: 20px;
    color: var(--friend-latency-color, var(--maxGreyFont));
    font-size: 10px;
    font-weight: 500;
    line-height: 1;
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }

  .friend-latency-dot {
    width: 6px;
    height: 6px;
    flex: 0 0 auto;
    border-radius: 50%;
    background: currentColor;
  }

  .friend-latency--fast {
    --friend-latency-color: #32c574;
  }

  .friend-latency--normal {
    --friend-latency-color: #4b9df5;
  }

  .friend-latency--slow {
    --friend-latency-color: #e8ad25;
  }

  .friend-latency--down {
    --friend-latency-color: #f1637b;
  }

  html.dark .friend-latency--fast {
    --friend-latency-color: #72e3a8;
  }

  html.dark .friend-latency--normal {
    --friend-latency-color: #83bdff;
  }

  html.dark .friend-latency--slow {
    --friend-latency-color: #ffd164;
  }

  html.dark .friend-latency--down {
    --friend-latency-color: #ff91a4;
  }

  /* 标签 chips：10px/500、2px 6px、3.5px 圆角、档次色虚线描边 */
  .friend-card-tags {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
    margin-top: 3px;
  }

  .friend-card-tag {
    padding: 2px 6px;
    border: 1px dashed color-mix(in srgb, var(--fc-accent, rgba(255, 246, 219, .9)) 24%, transparent);
    border-radius: 3.5px;
    font-size: 10px;
    font-weight: 500;
    line-height: 15px;
    color: var(--fc-desc, var(--maxGreyFont));
    white-space: nowrap;
  }

  .friend-card--hue .friend-card-tag {
    border-color: rgba(120, 120, 120, .24);
  }

  /* 元信息行：跨两列，左侧 RSS 最近更新、右侧点击量 */
  .friend-card-footer {
    position: relative;
    z-index: 2;
    grid-column: 1 / -1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 6px 8px;
    min-width: 0;
    margin-top: 2px;
  }

  .friend-card-footer__identity,
  .friend-card-footer__metrics {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    min-width: 0;
  }

  .friend-card-footer__identity {
    flex-wrap: nowrap;
  }

  /* 主理人档底行多出两个徽标：窄屏允许换行，不挤压右侧点击量、不冲出卡片 */
  .friend-card.tier-2 .friend-card-footer__identity {
    flex-wrap: wrap;
    row-gap: 4px;
  }

  .friend-card-footer__metrics {
    margin-left: auto;
  }

  .friend-rss-updated {
    flex: 0 0 auto;
    font-size: 10px;
    font-weight: 400;
    line-height: 20px;
    color: color-mix(in srgb, var(--fc-desc, var(--maxGreyFont)) 78%, transparent);
    white-space: nowrap;
  }

  .friend-click-count {
    display: inline-flex;
    align-items: center;
    gap: 2.8px;
    min-height: 20px;
    font-size: 10px;
    font-weight: 500;
    line-height: 1;
    font-variant-numeric: tabular-nums;
    color: color-mix(in srgb, var(--fc-desc, var(--maxGreyFont)) 88%, var(--fontColor));
    white-space: nowrap;
  }

  .friend-click-count svg {
    width: 12.8px;
    height: 12.8px;
    flex: 0 0 auto;
    color: var(--fc-accent, var(--themeBackground));
  }

  .friend-click-count-unit {
    font-size: 9.6px;
    font-weight: 700;
    color: color-mix(in srgb, currentColor 72%, transparent);
  }

  .friend-card[data-friend-recommended="true"] .friend-click-count {
    color: rgb(154, 106, 11);
  }

  .friend-card[data-friend-recommended="true"] .friend-click-count svg {
    color: rgb(214, 151, 18);
  }

  html.dark .friend-card[data-friend-recommended="true"] .friend-click-count {
    color: #f0c26a;
  }

  /* 可用性/违规覆盖层：参照站实测的毛玻璃层与胶囊徽标 */
  .friend-card-status-overlay {
    position: absolute;
    inset: 0;
    z-index: 20;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 10.4px;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, .18);
    border-radius: inherit;
    background: radial-gradient(110% 120% at 8% -20%, rgba(255, 255, 255, .42), transparent 50%),
                linear-gradient(112deg, rgba(255, 255, 255, .1), rgba(223, 238, 245, .18) 48%, rgba(248, 252, 254, .08)),
                rgba(225, 238, 244, .04);
    box-shadow: rgba(255, 255, 255, .76) 0 1px inset, rgba(112, 145, 163, .14) 0 -1px inset;
    backdrop-filter: blur(4.5px) saturate(1.04) brightness(1.03);
    pointer-events: none;
  }

  /* 静态高光，避免覆盖层闪烁 */
  .friend-card-status-overlay::before {
    content: "";
    position: absolute;
    inset: -45% -18%;
    z-index: 0;
    background: linear-gradient(116deg, transparent 31%, rgba(255, 255, 255, .52) 46%, transparent 63%);
    transform: translate(-22%);
    opacity: .18;
    pointer-events: none;
  }

  .friend-card-status-overlay::after {
    content: "";
    position: absolute;
    inset: 1.9px;
    z-index: 0;
    border: 1px solid rgba(255, 255, 255, .22);
    border-radius: 10px;
    box-shadow: rgba(255, 255, 255, .12) 0 2.4px 12.8px inset;
    pointer-events: none;
  }

  /* 覆盖层：给状态卡片预留足够高度，避免标题/倒计时被 flex 压缩 */
  .friend-card[data-friend-health="1"],
  .friend-card[data-friend-health="2"] {
    min-height: 118px;
  }

  .friend-card-remediation-overlay > * {
    flex-shrink: 0;
  }

  .friend-card-remediation-overlay {
    position: relative;
    z-index: 1;
    display: flex;
    width: 100%;
    height: 100%;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    gap: 5.6px;
  }

  .friend-card-remediation-title {
    max-width: 100%;
    padding: 3.2px 9.6px;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, .56);
    border-radius: 999px;
    background: rgba(255, 255, 255, .54);
    color: rgb(54, 72, 88);
    font-size: 12.2px;
    font-weight: 800;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .friend-card-status-badge {
    position: relative;
    z-index: 1;
    display: inline-flex;
    align-items: center;
    gap: 9.3px;
    min-height: 39.2px;
    padding: 7.2px 16px;
    border: 1px solid rgba(255, 255, 255, .6);
    border-radius: 999px;
    background: linear-gradient(135deg, rgba(255, 255, 255, .58), rgba(235, 247, 251, .3));
    box-shadow: rgba(70, 105, 123, .14) 0 6.4px 17.6px, rgba(255, 255, 255, .88) 0 1px inset,
                rgba(98, 137, 154, .12) 0 -1px inset;
    color: rgb(86, 113, 132);
    font-size: 14.4px;
    font-weight: 800;
    letter-spacing: .02em;
    white-space: nowrap;
    backdrop-filter: blur(6.7px) saturate(1.12);
  }

  .friend-card-policy-label {
    display: inline-flex;
    align-items: center;
    gap: 7.2px;
    min-height: 39.2px;
    padding: 7.2px 16px;
    border: 1px solid rgba(255, 255, 255, .66);
    border-radius: 999px;
    background: rgba(255, 239, 199, .72);
    color: rgb(138, 90, 18);
    font-size: 14.4px;
    font-weight: 850;
    white-space: nowrap;
  }

  .friend-card-remediation-countdown {
    max-width: 100%;
    padding: 2.9px 9px;
    border-radius: 999px;
    background: rgba(255, 255, 255, .5);
    color: rgb(74, 99, 117);
    font-size: 11px;
    font-weight: 750;
    line-height: 1.35;
    font-variant-numeric: tabular-nums;
    text-align: center;
  }

  .friend-card--policy .friend-card-remediation-countdown {
    color: rgb(119, 82, 30);
  }

  html.dark .friend-card-status-overlay {
    border-color: rgba(255, 255, 255, .1);
    background: radial-gradient(110% 120% at 8% -20%, rgba(255, 255, 255, .16), transparent 50%),
                linear-gradient(112deg, rgba(255, 255, 255, .04), rgba(120, 150, 170, .12) 48%, rgba(180, 210, 230, .04)),
                rgba(20, 30, 40, .18);
    box-shadow: rgba(255, 255, 255, .18) 0 1px inset, rgba(0, 0, 0, .18) 0 -1px inset;
  }

  html.dark .friend-card-remediation-title {
    border-color: rgba(255, 255, 255, .18);
    background: rgba(255, 255, 255, .12);
    color: rgb(220, 230, 236);
  }

  html.dark .friend-card-status-badge {
    border-color: rgba(255, 255, 255, .2);
    background: linear-gradient(135deg, rgba(255, 255, 255, .14), rgba(90, 120, 140, .18));
    color: rgb(214, 228, 236);
    box-shadow: rgba(0, 0, 0, .22) 0 6.4px 17.6px, rgba(255, 255, 255, .12) 0 1px inset;
  }

  html.dark .friend-card-policy-label {
    border-color: rgba(255, 220, 150, .24);
    background: rgba(120, 88, 30, .38);
    color: rgb(248, 214, 150);
  }

  html.dark .friend-card-remediation-countdown {
    background: rgba(255, 255, 255, .1);
    color: rgb(206, 220, 228);
  }

  html.dark .friend-card--policy .friend-card-remediation-countdown {
    color: rgb(246, 210, 144);
  }

  /* 减少动态：扫光、标题流光、星点流全部静止（canvas 只画一帧） */
  @media (prefers-reduced-motion: reduce) {
    .friend-card-surface::before {
      animation: none;
      opacity: 0;
    }

    .friend-card-surface::after {
      transition: none;
    }

    /* hover 放大同样不播：退回静止姿态（hover 规则更具体，必须显式覆盖） */
    .friend-card.tier-2:hover .friend-card-surface::after {
      transform: translateY(-50%) rotate(-2deg);
    }

    /* 主理人档卡面扫光整层隐藏：静止时不留一条停在卡上的光带 */
    .friend-card.tier-2 .friend-card-surface::before {
      display: none;
    }

    /* 标题流光整层隐藏：文字回到本档纯色，保持清晰可读 */
    .friend-card-title::after {
      display: none;
    }

    /* 徽标扫光同样隐藏：徽标只有 20px 高，静止光带会变成压住文字的色块 */
    .friend-card-badge::after {
      display: none;
    }
  }

  .friend-empty {
    padding: 40px 0;
    text-align: center;
    font-size: 13px;
    color: var(--maxGreyFont);
  }

  .friend-pair {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px;
    margin-top: 26px;
    align-items: stretch;
  }

  .friend-pair .friend-box {
    margin-top: 0;
  }

  .friend-box {
    margin-top: 26px;
    padding: 20px;
    border-radius: 16px;
    background: var(--cardBg);
    border: 1px solid var(--cardBorder);
    box-shadow: 0 4px 14px rgba(0, 0, 0, .04);
  }

  .box-title {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    font-size: 20px;
    font-weight: 700;
    line-height: 1.3;
  }

  .box-title-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    margin-right: 10px;
    border-radius: 12px;
    background: var(--subtleBg);
  }

  .site-head {
    display: flex;
    align-items: center;
    margin-bottom: 18px;
  }

  .site-avatar-wrap {
    position: relative;
    flex-shrink: 0;
    margin-right: 14px;
  }

  .site-avatar {
    border-radius: 16px;
  }

  .site-badge {
    position: absolute;
    right: -5px;
    bottom: -5px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #2b2b2b;
    color: var(--white);
    border: 2px solid var(--white);
  }

  .site-meta {
    min-width: 0;
  }

  .site-name {
    font-size: 20px;
    font-weight: 700;
    line-height: 1.3;
  }

  .site-desc {
    margin-top: 6px;
    font-size: 13px;
    line-height: 1.5;
    color: var(--maxGreyFont);
  }

  .site-field {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    padding: 10px 12px 10px 14px;
    border-radius: 12px;
    background: var(--hoverBg);
  }

  .site-field:last-child {
    margin-bottom: 0;
  }

  .site-field-text {
    min-width: 0;
  }

  .site-field-label {
    display: block;
    font-size: 11px;
    color: var(--greyFont);
  }

  .site-field-value {
    display: block;
    margin-top: 4px;
    font-size: 13px;
    color: var(--fontColor);
    word-break: break-all;
  }

  .site-copy {
    flex-shrink: 0;
    margin-left: 12px;
  }

  .site-copy-all {
    display: flex;
    justify-content: flex-end;
    margin-top: 14px;
  }

  /* 友链页四个按钮共用：统一按钮「第一版」观感，局部实现，不改 button.css 全局 token */
  .friend-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    min-height: 34px;
    padding: 6px 12px;
    border: 1px solid rgba(120, 120, 120, .16);
    border-radius: 10px;
    background-color: transparent;
    color: var(--themeBackground);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
    cursor: pointer;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, .07),
      0 1px 2px rgba(38, 38, 38, .04),
      inset 0 1px 0 rgba(255, 255, 255, .8),
      inset 0 -1px 0 rgba(112, 112, 112, .1);
    transition: color .22s ease, box-shadow .22s ease,
                border-color .22s ease, background-color .22s ease;
  }

  .friend-btn:hover,
  .friend-btn:focus-visible {
    border-style: dashed;
    border-color: rgba(120, 120, 120, .42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, .1),
      0 1px 2px rgba(38, 38, 38, .05),
      inset 0 1px 0 rgba(255, 255, 255, .9),
      inset 0 -1px 0 rgba(112, 112, 112, .16);
  }

  /* 按压位移瞬时生效（transition 不含 scale） */
  .friend-btn:active {
    border-style: dashed;
    border-color: rgba(120, 120, 120, .42);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, .1),
      0 1px 2px rgba(38, 38, 38, .05),
      inset 0 1px 0 rgba(255, 255, 255, .9),
      inset 0 -1px 0 rgba(112, 112, 112, .16);
    scale: .95;
  }

  /* 图标按钮保持 34×34 方形 */
  .friend-btn--icon {
    width: 34px;
    padding: 0;
  }

  /* 选中胶囊 / 主按钮：不加实心底色，用强调描边 + 外圈柔光 + 字重区分 */
  .friend-btn--strong {
    border-color: color-mix(in srgb, var(--themeBackground) 55%, transparent);
    font-weight: 600;
    box-shadow:
      0 2px 5px rgba(38, 38, 38, .07),
      0 1px 2px rgba(38, 38, 38, .04),
      inset 0 1px 0 rgba(255, 255, 255, .8),
      inset 0 -1px 0 rgba(112, 112, 112, .1),
      0 0 0 2px color-mix(in srgb, var(--themeBackground) 14%, transparent);
  }

  .friend-btn--strong:hover,
  .friend-btn--strong:active {
    border-color: color-mix(in srgb, var(--themeBackground) 85%, transparent);
    box-shadow:
      0 3px 6px rgba(38, 38, 38, .1),
      0 1px 2px rgba(38, 38, 38, .05),
      inset 0 1px 0 rgba(255, 255, 255, .9),
      inset 0 -1px 0 rgba(112, 112, 112, .16),
      0 0 0 2px color-mix(in srgb, var(--themeBackground) 20%, transparent);
  }

  html.dark .friend-btn {
    border-color: rgba(255, 255, 255, .16);
    color: var(--themeBackground);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, .3),
      0 1px 2px rgba(0, 0, 0, .24),
      inset 0 1px 0 rgba(255, 255, 255, .06),
      inset 0 -1px 0 rgba(0, 0, 0, .3);
  }

  html.dark .friend-btn:hover,
  html.dark .friend-btn:focus-visible,
  html.dark .friend-btn:active {
    border-style: dashed;
    border-color: rgba(255, 255, 255, .4);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, .38),
      0 1px 2px rgba(0, 0, 0, .28),
      inset 0 1px 0 rgba(255, 255, 255, .1),
      inset 0 -1px 0 rgba(0, 0, 0, .34);
  }

  html.dark .friend-btn--strong {
    border-color: rgba(99, 179, 255, .55);
    box-shadow:
      0 2px 5px rgba(0, 0, 0, .3),
      0 1px 2px rgba(0, 0, 0, .24),
      inset 0 1px 0 rgba(255, 255, 255, .06),
      inset 0 -1px 0 rgba(0, 0, 0, .3),
      0 0 0 2px rgba(99, 179, 255, .18);
  }

  html.dark .friend-btn--strong:hover,
  html.dark .friend-btn--strong:active {
    border-color: rgba(99, 179, 255, .9);
    box-shadow:
      0 3px 6px rgba(0, 0, 0, .38),
      0 1px 2px rgba(0, 0, 0, .28),
      inset 0 1px 0 rgba(255, 255, 255, .1),
      inset 0 -1px 0 rgba(0, 0, 0, .34),
      0 0 0 2px rgba(99, 179, 255, .24);
  }

  /* 提交申请：直接套用 .friend-btn（透明底 + 1px 实线 → hover 虚线），
     这里只补 Element Plus 按钮自身的外边距与图标-文字间距，不重复视觉值 */
  .el-button.friend-submit-link {
    margin: 0;
  }

  .friend-submit-link__icon {
    margin-right: 6px;
    vertical-align: middle;
  }

  @media (prefers-reduced-motion: reduce) {
    .friend-btn {
      transition: none;
    }

    .friend-btn:active {
      scale: 1;
    }
  }

  .friend-steps {
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .friend-step {
    display: flex;
    margin-bottom: 20px;
  }

  .friend-step-index {
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    margin-right: 14px;
    border-radius: 50%;
    background: var(--fontColor);
    color: var(--background);
    font-size: 18px;
    font-weight: 700;
    line-height: 40px;
    text-align: center;
  }

  .friend-step-body {
    padding-top: 8px;
  }

  .friend-step-title {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    line-height: 1.4;
  }

  .friend-step-desc {
    margin: 6px 0 0;
    font-size: 13px;
    line-height: 1.7;
    color: var(--maxGreyFont);
  }

  .friend-form {
    margin-top: 18px;
    padding-top: 18px;
    border-top: 1px dashed rgba(0, 0, 0, .08);
  }

  .friend-form-row {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
  }

  .friend-form-row label {
    flex-shrink: 0;
    width: 60px;
    font-size: 13px;
    color: var(--maxGreyFont);
  }

  .friend-form-row :deep(.el-input__inner) {
    border-radius: 10px;
  }

  .friend-form-submit {
    text-align: right;
  }

  .notes-title {
    display: flex;
    align-items: center;
    margin-bottom: 18px;
    font-size: 17px;
    font-weight: 700;
  }

  .notes-title svg {
    flex-shrink: 0;
    margin-right: 6px;
  }

  .notes-list {
    margin: 0;
    padding-left: 22px;
    font-size: 15px;
    line-height: 1.9;
    color: var(--fontColor);
  }

  .notes-list li {
    margin-bottom: 8px;
  }

  .notes-list li:last-child {
    margin-bottom: 0;
  }

  .notes-list li strong {
    margin-right: 6px;
    font-weight: 700;
  }

  @media screen and (max-width: 1000px) {
    /* 单列断点与打赏、追番页保持一致，避免同宽度下左栏有无不一致 */
    .friend-wrap {
      grid-template-columns: minmax(0, 1fr);
    }

    .friend-aside {
      display: none;
    }
  }

  @media screen and (max-width: 800px) {
    .friend-pair {
      grid-template-columns: minmax(0, 1fr);
      gap: 0;
    }

    .friend-pair .friend-box {
      margin-top: 26px;
    }
  }

  @media screen and (max-width: 560px) {
    .friend-main {
      /* 顶部留白只来自页面容器，避免窄屏再叠 20px */
      margin: 0 auto;
      padding: 18px 16px;
    }

    .friend-head-title {
      font-size: var(--ui-title-size);
    }

    .friend-form-row {
      align-items: flex-start;
      flex-direction: column;
    }

    .friend-form-row label {
      margin-bottom: 6px;
    }
  }
</style>
