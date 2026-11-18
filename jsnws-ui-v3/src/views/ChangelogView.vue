<template>
  <div>
    <div class="changelog-page my-animation-slide-bottom">
      <aside class="changelog-aside">
        <MyAside hide-calendar
                 @selectSort="selectSort"
                 @selectArticle="selectArticle"></MyAside>
      </aside>

      <main class="changelog-main">
        <section class="notice-page" v-if="loaded">
          <!-- 左侧：公告信息 / 快速查看 / 历史公告 -->
          <aside class="notice-aside">
            <section class="notice-hero" v-if="active">
              <div class="notice-hero-kicker">
                <svg width="1em" height="1em" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="currentColor"
                        d="M8 20q-.825 0-1.412-.587T6 18v-2q0-.425.288-.712T7 15h2v-2.25q-.75-.05-1.487-.312t-1.288-.788q-.15-.15-.237-.337t-.088-.388v-.675h-.725q-.2 0-.387-.075T4.45 9.95L2.2 7.7q-.3-.3-.3-.7t.3-.7q.725-.725 1.95-1.062T6.4 4.9q.675 0 1.313.1T9 5.375q0-.575.4-.975t.975-.4H19q.825 0 1.413.588T21 6v11q0 1.25-.875 2.125T18 20zm3-5h5q.425 0 .713.288T17 16v1q0 .425.288.713T18 18t.713-.288T19 17V6h-8v.6l5.725 5.725q.225.225.275.513t-.075.562t-.35.437T16 14q-.2 0-.387-.088T15.3 13.7l-2.55-2.55l-.2.2q-.35.35-.737.625T11 12.4zM5.6 8.25h1.3q.425 0 .713.288t.287.712v1.15q.3.2.625.275t.675.075q.575 0 1.038-.175t.912-.625l.2-.2l-1.4-1.4q-.725-.725-1.625-1.088T6.4 6.9q-.5 0-.95.075t-.9.225z"/>
                </svg>
                <span>UPDATE / {{ dotDate(active.logDate) }}</span>
                <span class="notice-new-badge" v-if="activeIndex === 0">NEW</span>
              </div>
              <h1>{{ active.title }}</h1>
              <p>{{ active.summary }}</p>
              <div class="notice-version">
                <span>{{ active.kind }}</span>
                <strong>{{ versionLabel(active) }}</strong>
              </div>
            </section>

            <nav class="notice-nav" aria-label="公告目录" v-if="active">
              <p class="notice-nav-title">快速查看</p>
              <a v-for="section in sections"
                 :key="section.id"
                 :href="'#' + section.id"
                 @click.prevent="scrollToSection(section.id)">
                <i aria-hidden="true"></i>{{ section.label }}
              </a>
            </nav>

            <section class="notice-history" aria-label="历史公告" v-if="entryList.length">
              <div class="notice-history-title">
                <svg width="1em" height="1em" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="currentColor"
                        d="M12 21q-3.15 0-5.575-1.912T3.275 14.2q-.1-.375.15-.687t.675-.363q.4-.05.725.15t.45.6q.6 2.25 2.475 3.675T12 19q2.925 0 4.963-2.037T19 12t-2.037-4.962T12 5q-1.725 0-3.225.8T6.25 8H8q.425 0 .713.288T9 9t-.288.713T8 10H4q-.425 0-.712-.288T3 9V5q0-.425.288-.712T4 4t.713.288T5 5v1.35q1.275-1.6 3.113-2.475T12 3q1.875 0 3.513.713t2.85 1.924t1.925 2.85T21 12t-.712 3.513t-1.925 2.85t-2.85 1.925T12 21m1-9.4l2.5 2.5q.275.275.275.7t-.275.7t-.7.275t-.7-.275l-2.8-2.8q-.15-.15-.225-.337T11 11.975V8q0-.425.288-.712T12 7t.713.288T13 8z"/>
                </svg>
                <span>历史公告</span>
              </div>
              <div class="history-card">
                <button v-for="(entry, index) in entryList"
                        :key="'history-' + entry.id"
                        class="history-entry"
                        :class="{ active: index === activeIndex }"
                        type="button"
                        @click="selectEntry(index)">
                  <strong>{{ versionLabel(entry) }} {{ index === 0 ? '最新公告' : '更新公告' }}
                    <span class="history-new-badge" v-if="index === 0">NEW</span>
                  </strong>
                  <span>{{ entry.summary }}</span>
                </button>
              </div>
            </section>
          </aside>

          <!-- 右侧：当前公告正文 -->
          <div class="notice-content">
            <div class="notice-empty" v-if="!active">
              <h2>暂无更新公告</h2>
              <p>新的更新记录会在这里发布。</p>
            </div>
            <article class="notice-main" v-else>
              <p class="notice-lead">
                <strong>{{ versionLabel(active) }}</strong> {{ active.summary }}
              </p>

              <section class="notice-section" id="cl-summary">
                <div class="section-head">
                  <h2>一、本次更新</h2>
                  <span class="section-index">01</span>
                </div>
                <ol class="notice-list" v-if="active.details.length">
                  <li v-for="(detail, index) in active.details" :key="'detail-' + index">
                    <span class="num">{{ index + 1 }}</span>
                    <span>{{ detail }}</span>
                  </li>
                </ol>
                <p v-else>{{ active.summary }}</p>
              </section>

              <section class="notice-section" id="cl-topics" v-if="active.topics.length">
                <div class="section-head">
                  <h2>二、涉及内容</h2>
                  <span class="section-index">02</span>
                </div>
                <div class="notice-tag-row">
                  <span class="notice-tag" v-for="topic in active.topics" :key="'topic-' + topic">{{ topic }}</span>
                </div>
              </section>
            </article>
          </div>
        </section>
      </main>

      <aside class="changelog-side">
        <ArchiveSidebar></ArchiveSidebar>
      </aside>
    </div>

    <MyFooter></MyFooter>
  </div>
</template>

<script>
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import ArchiveSidebar from "../components/common/ArchiveSidebar.vue";
  import fallbackChangelog from "../assets/data/changelog.json";

  export default {
    name: "ChangelogView",
    components: {
      MyFooter,
      MyAside,
      ArchiveSidebar
    },
    data() {
      return {
        entryList: [],
        activeIndex: 0,
        loaded: false
      };
    },
    computed: {
      active() {
        return this.entryList[this.activeIndex] || null;
      },
      sections() {
        const list = [{id: "cl-summary", label: "本次更新"}];
        if (this.active && this.active.topics.length) {
          list.push({id: "cl-topics", label: "涉及内容"});
        }
        return list;
      }
    },
    created() {
      this.getChangelog();
    },
    methods: {
      getChangelog() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listChangelog")
          .then((res) => {
            const list = Array.isArray(res.data) ? res.data : [];
            this.entryList = this.normalizeList(list);
            this.activeIndex = 0;
          })
          .catch(() => {
            // 纯静态部署时接口不可用，退回构建时随包的同一份数据，保证线上条目一致
            this.entryList = this.normalizeList(fallbackChangelog);
          })
          .finally(() => {
            this.loaded = true;
          });
      },
      normalizeList(list) {
        return (Array.isArray(list) ? list : []).map((item) => {
          return Object.assign({}, item, {
            logDate: String(item.logDate || "").slice(0, 10),
            topics: this.parseList(item.topics),
            details: this.parseList(item.details)
          });
        });
      },
      parseList(value) {
        if (Array.isArray(value)) {
          return value;
        }
        try {
          const parsed = JSON.parse(value || "[]");
          return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
          return [];
        }
      },
      versionLabel(entry) {
        const version = String(entry.version || "");
        return /^v/i.test(version) ? version.toUpperCase() : "V" + version;
      },
      dotDate(date) {
        return String(date || "").replace(/-/g, ".");
      },
      selectEntry(index) {
        this.activeIndex = index;
        window.scrollTo({top: 0});
      },
      scrollToSection(id) {
        const target = this.$el.querySelector("#" + id);
        if (target) {
          target.scrollIntoView({behavior: "smooth", block: "start"});
        }
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
  };
</script>

<style scoped>
  /* ---------- 页面外壳：沿用站内归档页栅格（单列 / 左+主 / 左+主+右） ---------- */
  .changelog-page {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    align-items: start;
    width: 92vw;
    max-width: 112rem;
    margin: 0 auto;
    padding: 76px 16px clamp(48px, 5vw, 72px);
    background: var(--favoriteBg);
  }

  .changelog-aside,
  .changelog-side {
    display: none;
    min-width: 0;
  }

  .changelog-main {
    min-width: 0;
  }

  @media (min-width: 768px) {
    .changelog-page {
      grid-template-columns: 280px minmax(0, 1fr);
    }

    .changelog-aside {
      display: block;
    }
  }

  @media (min-width: 1280px) {
    .changelog-page {
      grid-template-columns: 280px minmax(0, 1fr) 280px;
    }

    .changelog-side {
      display: block;
    }
  }

  /* ---------- 公告页：与参考站 /changelog/ 同一套结构与实测尺寸 ---------- */
  .notice-page {
    --notice-ink: var(--fontColor);
    --notice-muted: var(--greyFont);
    --notice-soft: color-mix(in oklab, var(--fontColor) 42%, transparent);
    --notice-panel: color-mix(in oklab, var(--cardBg) 72%, transparent);
    --notice-panel-strong: color-mix(in oklab, var(--cardBg) 86%, transparent);
    display: grid;
    grid-template-columns: minmax(13rem, 15rem) minmax(0, 1fr);
    gap: 1rem;
    align-items: start;
    width: 100%;
    position: relative;
    z-index: 1;
    font-family: var(--font-sans, inherit);
  }

  .notice-aside {
    position: sticky;
    top: 4.75rem;
    display: grid;
    gap: .75rem;
  }

  .notice-hero,
  .notice-nav,
  .notice-history,
  .notice-main,
  .notice-section {
    border: 1px solid color-mix(in oklab, var(--cardBorder) 75%, transparent);
    background: var(--notice-panel);
    backdrop-filter: blur(18px);
    box-shadow: 0 14px 38px #0000000b;
  }

  .notice-hero,
  .notice-nav,
  .notice-history,
  .notice-main {
    border-radius: 1rem;
  }

  .notice-hero {
    padding: 1rem;
  }

  .notice-hero-kicker {
    display: inline-flex;
    align-items: center;
    gap: .4rem;
    margin-bottom: .8rem;
    padding: .36rem .6rem;
    border-radius: .5rem;
    color: var(--themeBackground);
    background: color-mix(in oklab, var(--themeBackground) 13%, transparent);
    font-size: .76rem;
    font-weight: 850;
  }

  .notice-new-badge,
  .history-new-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 1.38rem;
    padding: .16rem .54rem;
    border-radius: .35rem;
    color: #fff;
    background: linear-gradient(135deg, #00b7ff, #007aff);
    box-shadow: 0 .42rem 1rem #00b7ff4d, inset 0 1px #ffffff61;
    font-size: .7rem;
    font-weight: 950;
    line-height: 1;
    letter-spacing: 0;
  }

  .notice-new-badge {
    position: relative;
    margin-left: .12rem;
    transform: rotate(-3deg);
  }

  .notice-new-badge:after {
    content: "";
    position: absolute;
    right: -.3rem;
    top: 50%;
    width: 0;
    height: 0;
    border-top: .35rem solid #0000;
    border-bottom: .35rem solid #0000;
    border-left: .35rem solid #007aff;
    transform: translateY(-50%);
  }

  .notice-hero h1 {
    margin: 0;
    color: var(--notice-ink);
    font-size: 1.35rem;
    line-height: 1.35;
    font-weight: 900;
    letter-spacing: 0;
  }

  .notice-hero p {
    margin: .65rem 0 0;
    color: var(--notice-muted);
    font-size: .88rem;
    line-height: 1.72;
  }

  .notice-version {
    display: flex;
    justify-content: space-between;
    gap: .6rem;
    margin-top: .85rem;
    padding: .65rem .7rem;
    border-radius: .65rem;
    color: var(--notice-muted);
    background: color-mix(in oklab, var(--themeBackground) 9%, transparent);
    font-size: .76rem;
    font-weight: 800;
  }

  .notice-version strong {
    color: var(--themeBackground);
  }

  .notice-nav {
    padding: .7rem;
  }

  .notice-nav-title,
  .notice-history-title {
    display: flex;
    align-items: center;
    gap: .38rem;
    margin: 0 0 .55rem;
    color: var(--notice-soft);
    font-size: .76rem;
    font-weight: 900;
  }

  .notice-nav a {
    display: flex;
    align-items: center;
    gap: .45rem;
    min-height: 2rem;
    padding: .45rem .55rem;
    border-radius: .5rem;
    color: var(--notice-muted);
    text-decoration: none;
    font-size: .82rem;
    font-weight: 850;
    transition: background .2s, color .2s, transform .2s;
  }

  .notice-nav a:hover {
    color: var(--themeBackground);
    background: color-mix(in oklab, var(--themeBackground) 12%, transparent);
    transform: translate(.12rem);
  }

  .notice-nav i {
    width: .42rem;
    height: .42rem;
    border-radius: 999px;
    background: var(--themeBackground);
    flex: none;
  }

  .notice-history {
    padding: .7rem;
  }

  .history-card {
    display: grid;
    gap: .45rem;
  }

  .history-entry {
    position: relative;
    width: 100%;
    display: grid;
    gap: .25rem;
    padding: .78rem .75rem;
    border: 1px solid #0000;
    border-radius: .55rem;
    background: none;
    color: inherit;
    text-align: left;
    cursor: pointer;
    font: inherit;
    transition: background .2s, border-color .2s, transform .2s;
  }

  .history-entry:hover,
  .history-entry.active {
    border-color: color-mix(in oklab, var(--themeBackground) 24%, var(--cardBorder));
    background: none;
    color: var(--themeBackground);
    transform: translate(.1rem);
  }

  .history-entry strong {
    display: flex;
    align-items: center;
    gap: .38rem;
    color: var(--notice-ink);
    font-size: .83rem;
    line-height: 1.35;
  }

  .history-new-badge {
    position: absolute;
    top: -.5rem;
    right: .45rem;
    min-height: 1.35rem;
    padding: .18rem .5rem;
    border: 2px solid color-mix(in oklab, var(--cardBg) 92%, transparent);
    border-radius: 999px;
    font-size: .66rem;
    transform: rotate(4deg);
  }

  .history-entry .history-new-badge {
    color: #fff !important;
  }

  .history-entry span {
    color: var(--notice-muted);
    font-size: .75rem;
    line-height: 1.48;
  }

  .notice-content {
    min-width: 0;
  }

  .notice-main {
    scroll-margin-top: 6rem;
    padding: 1.15rem;
    min-width: 0;
    color: var(--notice-ink);
  }

  .notice-main .notice-lead {
    margin: 0 0 1rem;
    padding: 1rem;
    border-radius: .85rem;
    color: var(--notice-muted);
    line-height: 1.85;
  }

  .notice-main .notice-tag-row {
    display: flex;
    flex-wrap: wrap;
    gap: .45rem;
    margin: 0;
  }

  .notice-main .notice-tag {
    display: inline-flex;
    align-items: center;
    min-height: 1.75rem;
    padding: .32rem .55rem;
    border-radius: 999px;
    color: var(--themeBackground);
    background: color-mix(in oklab, var(--themeBackground) 12%, transparent);
    font-size: .75rem;
    font-weight: 900;
  }

  .notice-main .notice-section {
    margin-top: .85rem;
    padding: 1rem;
    border-radius: .85rem;
    scroll-margin-top: 6rem;
  }

  .notice-main .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: .75rem;
    margin-bottom: .75rem;
    padding-bottom: .65rem;
    border-bottom: 1px solid color-mix(in oklab, var(--cardBorder) 75%, transparent);
  }

  .notice-main .section-head h2 {
    margin: 0;
    color: var(--notice-ink);
    font-size: 1.05rem;
    line-height: 1.42;
    font-weight: 900;
    letter-spacing: 0;
  }

  .notice-main .section-index {
    color: var(--themeBackground);
    font-size: .78rem;
    font-weight: 900;
  }

  .notice-main p {
    color: var(--notice-muted);
    line-height: 1.82;
  }

  .notice-main .notice-list {
    display: grid;
    gap: .6rem;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .notice-main .notice-list li {
    display: grid;
    grid-template-columns: 1.75rem minmax(0, 1fr);
    gap: .6rem;
    padding: .7rem;
    border-radius: .7rem;
    border: 1px solid color-mix(in oklab, var(--cardBorder) 70%, transparent);
    background: var(--notice-panel-strong);
    color: var(--notice-muted);
    line-height: 1.75;
  }

  .notice-main .num {
    width: 1.75rem;
    height: 1.75rem;
    display: grid;
    place-items: center;
    border-radius: .55rem;
    background: color-mix(in oklab, var(--themeBackground) 12%, transparent);
    color: var(--themeBackground);
    font-size: .75rem;
    font-weight: 900;
  }

  .notice-empty {
    display: grid;
    gap: .4rem;
    padding: 1.15rem;
    border: 1px solid color-mix(in oklab, var(--cardBorder) 75%, transparent);
    border-radius: 1rem;
    background: var(--notice-panel);
  }

  .notice-empty h2,
  .notice-empty p {
    margin: 0;
  }

  .notice-empty h2 {
    color: var(--notice-ink);
    font-size: 1.05rem;
  }

  .notice-empty p {
    color: var(--notice-muted);
  }

  @media (max-width: 1020px) {
    .notice-page {
      grid-template-columns: 1fr;
    }

    .notice-aside {
      position: static;
    }

    .notice-nav {
      display: none;
    }
  }

  @media (max-width: 680px) {
    .notice-main,
    .notice-hero {
      padding: .9rem;
    }

    .notice-main .section-head {
      align-items: flex-start;
      flex-direction: column;
    }
  }
</style>
