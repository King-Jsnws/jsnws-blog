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
                class="friend-chip"
                :class="{'friend-chip-active': activeCategory === ''}"
                @click="activeCategory = ''">全部
        </button>
        <button type="button"
                class="friend-chip"
                v-for="(category, index) in categories"
                :key="index"
                :class="{'friend-chip-active': activeCategory === category}"
                @click="activeCategory = category">{{category}}
        </button>
      </div>

      <span class="friend-count">{{filteredFriends.length}} 个友链</span>

      <!-- 友链列表 -->
      <div class="friend-grid" v-if="!$common.isEmpty(filteredFriends)">
        <a class="friend-card"
           v-for="(item, index) in filteredFriends"
           :key="index"
           :href="item.url"
           target="_blank"
           rel="noopener noreferrer"
           :aria-label="item.title">
          <span class="friend-card-arrow" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor"
                    d="m16 8.4l-8.9 8.9q-.275.275-.7.275t-.7-.275t-.275-.7t.275-.7L14.6 7H7q-.425 0-.712-.288T6 6t.288-.712T7 5h10q.425 0 .713.288T18 6v10q0 .425-.288.713T17 17t-.712-.288T16 16z"/>
            </svg>
          </span>
          <span class="friend-card-avatar-wrap">
            <el-avatar class="friend-card-avatar" :size="56" shape="square" :src="item.cover"></el-avatar>
          </span>
          <div class="friend-card-info">
            <span class="friend-card-title">{{item.title}}</span>
            <p class="friend-card-desc">{{item.introduction}}</p>
          </div>
        </a>
      </div>
      <div class="friend-empty" v-else>暂无友链</div>

      <!-- 本站信息 / 申请友链 -->
      <div class="friend-pair">
      <!-- 本站信息 -->
      <div class="friend-box">
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
            <div class="site-name">{{$constant.friendWebName}}</div>
            <div class="site-desc">{{$constant.friendIntroduction}}</div>
          </div>
        </div>
        <div class="site-field" v-for="(field, index) in siteFields" :key="index">
          <div class="site-field-text">
            <span class="site-field-label">{{field.label}}</span>
            <span class="site-field-value">{{field.value}}</span>
          </div>
          <button type="button" class="site-copy" title="复制" @click="copyText(field.value)">
            <svg viewBox="0 0 24 24" width="15" height="15">
              <path fill="currentColor"
                    d="M9 18q-.825 0-1.412-.587T7 16V4q0-.825.588-1.412T9 2h9q.825 0 1.413.588T20 4v12q0 .825-.587 1.413T18 18zm0-2h9V4H9zm-4 6q-.825 0-1.412-.587T3 20V6h2v14h11v2z"/>
            </svg>
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
              <p class="friend-step-desc">在下方填写您的名称、简介、封面与网址后提交</p>
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
            <label>名称</label>
            <el-input maxlength="30" v-model="friend.title" placeholder="您的站点名称"></el-input>
          </div>
          <div class="friend-form-row">
            <label>简介</label>
            <el-input maxlength="120" v-model="friend.introduction" placeholder="一句话介绍您的站点"></el-input>
          </div>
          <div class="friend-form-row">
            <label>封面</label>
            <el-input maxlength="200" v-model="friend.cover" placeholder="头像或封面图片地址"></el-input>
          </div>
          <div class="friend-form-row">
            <label>网址</label>
            <el-input maxlength="200" v-model="friend.url" placeholder="https://"></el-input>
          </div>
          <div class="friend-form-submit">
            <el-button type="primary" round @click="submitFriend()">提交申请</el-button>
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
        friend: {
          title: "",
          introduction: "",
          cover: "",
          url: ""
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
      siteAvatar() {
        let avatar = this.$store.state.webInfo.avatar;
        if (this.$common.isEmpty(avatar)) {
          avatar = this.$constant.friendAvatar;
        }
        return this.$common.isEmpty(avatar) ? '/images/default-avatar.jpg' : avatar;
      },
      siteFields() {
        return [
          {label: "站点名称", value: this.$constant.friendWebName},
          {label: "站点描述", value: this.$constant.friendIntroduction},
          {label: "站点链接", value: this.$constant.friendUrl},
          {label: "头像链接", value: this.$constant.friendAvatar}
        ];
      }
    },

    created() {
      this.getFriends();
    },

    methods: {
      selectSort(sort) {
        this.$router.push({path: "/sort", query: {sortId: sort.id}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: "/"});
        }
      },
      copyText(text) {
        if (this.$common.isEmpty(text)) {
          return;
        }
        if (navigator.clipboard && window.isSecureContext) {
          navigator.clipboard.writeText(text).then(() => {
            this.$message({message: "已复制", type: "success"});
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
        this.$message({message: "已复制", type: "success"});
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
    grid-template-columns: 300px minmax(0, 1fr);
    column-gap: 30px;
    align-items: start;
    margin: 40px auto;
    color: var(--fontColor);
  }

  .friend-aside {
    min-width: 0;
    align-self: stretch;
  }

  .friend-main {
    max-width: none;
    margin: 0;
    border-radius: 10px;
    padding: 30px 40px 40px;
    background: var(--cardBg);
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
    font-size: 26px;
    font-weight: 700;
    line-height: 1.4;
  }

  .friend-head-subtitle {
    margin: 6px 0 0;
    font-size: 14px;
    color: var(--maxGreyFont);
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
    margin-bottom: 14px;
    font-size: 13px;
    color: var(--maxGreyFont);
  }

  .friend-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }

  .friend-chip {
    padding: 7px 18px;
    border-radius: 12px;
    border: 1px solid var(--panelBorder);
    background: var(--cardBg);
    color: var(--panelText);
    font-size: 13px;
    line-height: 1.6;
    cursor: pointer;
    transition: all .25s;
  }

  .friend-chip:hover {
    background: var(--hoverBg);
  }

  .friend-chip-active {
    background: var(--fontColor);
    border-color: var(--fontColor);
    color: var(--background);
  }

  .friend-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 14px;
  }

  .friend-card {
    position: relative;
    display: flex;
    align-items: center;
    gap: 14px;
    min-height: 96px;
    padding: 14px;
    border-radius: 16px;
    border: 1px solid var(--cardBorder);
    background: var(--cardBg);
    color: inherit;
    overflow: hidden;
    text-decoration: none;
    transition: border-color .25s, box-shadow .25s, background-color .25s;
  }

  .friend-card:hover {
    border-color: var(--themeBackground);
    background: var(--hoverBg);
    box-shadow: 0 6px 18px var(--mask);
  }

  .friend-card-arrow {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 2;
    display: inline-flex;
    color: var(--themeBackground);
    opacity: 0;
    transition: opacity .25s;
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

  .friend-card-avatar {
    border-radius: 12px;
    background: var(--subtleBg);
    color: var(--greyFont);
    transition: transform .25s;
  }

  .friend-card:hover .friend-card-avatar {
    transform: scale(1.05);
  }

  .friend-card-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    flex: 1;
    min-width: 0;
  }

  .friend-card-title {
    display: block;
    padding-right: 20px;
    overflow: hidden;
    font-size: 14px;
    font-weight: 700;
    line-height: 1.3;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .friend-card-desc {
    margin: 0;
    overflow: hidden;
    font-size: 12px;
    line-height: 1.5;
    color: var(--maxGreyFont);
    white-space: nowrap;
    text-overflow: ellipsis;
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
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    margin-left: 12px;
    border: none;
    border-radius: 50%;
    background: var(--subtleBg);
    color: var(--maxGreyFont);
    cursor: pointer;
    transition: all .25s;
  }

  .site-copy:hover {
    background: var(--themeBackground);
    color: var(--white);
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

  @media screen and (max-width: 1100px) {
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
      margin: 20px auto;
      padding: 20px 15px 30px;
    }

    .friend-head-title {
      font-size: 22px;
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
