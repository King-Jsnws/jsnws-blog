<template>
  <div>
    <div class="blog-log-page my-animation-slide-bottom">
      <!-- 左栏：与项目更新日志页一致，复用全站侧栏 -->
      <aside class="blog-log-aside">
        <MyAside hide-calendar
                 @selectSort="selectSort"
                 @selectArticle="selectArticle"></MyAside>
      </aside>

      <main class="blog-log-main">
          <div class="blog-log-inner">
        <!-- 页头：参考站为标题 + 一句说明 -->
        <header class="blog-log-intro">
          <h1>
            <svg viewBox="0 0 24 24" width="1.18em" height="1.18em" fill="none" stroke="currentColor"
                 stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M3.6 12a8.4 8.4 0 1 0 2.5-6"/>
              <path d="M3.6 4.6v4h4"/>
              <path d="M12 8v4.2l2.8 1.7"/>
            </svg>
            更新日志
          </h1>
          <p>记录博客项目的功能迭代、问题修复与重要变更。</p>
        </header>

        <section class="blog-log-module" aria-labelledby="blog-log-title">
          <!-- 区块标题：条数 + 五色图例 + 视图切换 -->
          <div class="blog-log-heading">
            <h2 id="blog-log-title">全部更新</h2>
            <div class="blog-log-section-summary">
              <span>{{ total }} 条记录</span>
            </div>
            <ul class="blog-log-legend" aria-label="更新颜色说明">
              <li v-for="tone in toneList" :key="tone.key" :class="'tone-' + tone.key" :aria-label="tone.label">
                <span class="blog-log-dot" aria-hidden="true"></span>
                <span class="blog-log-legend-full">{{ tone.label }}</span>
                <span class="blog-log-legend-short" aria-hidden="true">{{ tone.short }}</span>
              </li>
            </ul>
            <div class="blog-log-view-switch" role="group" aria-label="更新日志视图">
              <button type="button" :aria-pressed="view === 'timeline' ? 'true' : 'false'"
                      @click="switchView('timeline')">时间线</button>
              <button type="button" :aria-pressed="view === 'graph' ? 'true' : 'false'"
                      @click="switchView('graph')">关系图</button>
            </div>
          </div>

          <div class="blog-log-board" :class="{ 'is-hovering': hoverId !== null }" :data-view="view"
               @mouseleave="clearHover">
            <div class="blog-log-state" v-if="loading">
              <p>正在加载更新记录…</p>
            </div>
            <div class="blog-log-state" v-else-if="!entryList.length">
              <p>暂无更新记录</p>
            </div>

            <template v-else>
              <!-- 时间线：单列，日期 / 节点连线 / 卡片 -->
              <ol id="blog-log-timeline" class="blog-log-timeline" data-changelog-timeline>
                <li v-for="(entry, index) in entryList" :key="entry.id"
                    class="blog-log-timeline-item" :class="'tone-' + entry.tone">
                  <time class="blog-log-timeline-date" :datetime="entry.logDate">{{ dotDate(entry.logDate) }}</time>
                  <span class="blog-log-timeline-marker" aria-hidden="true">
                    <span class="blog-log-dot" :class="'tone-' + entry.tone"></span>
                  </span>
                  <button class="blog-log-timeline-card" type="button" aria-haspopup="dialog"
                          :aria-label="'查看第 ' + numberOf(index) + ' 条更新：' + displayTitle(entry)"
                          @click="openEntry(index)">
                    <span class="blog-log-timeline-heading">
                      <strong>{{ displayTitle(entry) }}</strong>
                      <span class="blog-log-timeline-kind" :class="'tone-' + entry.tone">{{ entry.kind }}</span>
                    </span>
                    <span class="blog-log-timeline-summary">{{ entry.summary }}</span>
                  </button>
                </li>
              </ol>

              <!-- 关系图：五列卡片 + 连线 -->
              <div id="blog-log-graph" class="blog-log-graph" v-show="view === 'graph'" data-changelog-graph>
                <svg class="blog-log-connectors" ref="connectors" aria-hidden="true">
                  <path v-for="wire in wires" :key="wire.key" class="blog-log-wire"
                        :class="{ 'is-hover': hoverId !== null && (wire.from === hoverId || wire.to === hoverId) }"
                        :d="wire.d" :stroke="toneColor(wire.tone)" />
                </svg>
                <ol class="blog-log-grid" ref="grid">
                  <li v-for="(entry, index) in entryList" :key="entry.id"
                      class="blog-log-item"
                      :class="{ 'is-active': hoverId === entry.id, 'is-linked': linkedIds.has(entry.id) }"
                      :data-entry-id="entry.id">
                    <button class="blog-log-card" type="button" :class="'tone-' + entry.tone"
                            aria-haspopup="dialog" @click="openEntry(index)"
                            @mouseenter="hoverId = entry.id" @focus="hoverId = entry.id" @blur="clearHover">
                      <span class="blog-log-number" aria-hidden="true">{{ numberOf(index) }}</span>
                      <span class="blog-log-meta">
                        <span class="blog-log-kind">
                          <span class="blog-log-dot" aria-hidden="true"></span>{{ entry.kind }}
                        </span>
                        <time :datetime="entry.logDate">{{ dotDate(entry.logDate) }}</time>
                      </span>
                      <span class="blog-log-card-version">{{ versionLabel(entry) }}</span>
                      <span class="blog-log-card-title">{{ displayTitle(entry) }}</span>
                      <span class="blog-log-summary">{{ entry.summary }}</span>
                    </button>
                  </li>
                </ol>
              </div>
            </template>
          </div>
        </section>
          </div>
      </main>

      <!-- 右栏：与项目更新日志页一致，复用首页同款侧栏卡片 -->
      <aside class="blog-log-side">
        <ArchiveSidebar></ArchiveSidebar>
      </aside>
    </div>

    <MyFooter></MyFooter>

    <!-- 详情弹窗 -->
    <teleport to="body">
      <transition name="blog-log-dialog">
        <div v-if="activeEntry" class="blog-log-dialog-backdrop" @click.self="closeEntry">
          <div class="blog-log-dialog" role="dialog" aria-modal="true" aria-labelledby="blog-log-entry-title">
            <div class="blog-log-dialog-frame">
              <div class="blog-log-dialog-bar">
                <span>更新详情</span>
                <button class="blog-log-close" type="button" aria-label="关闭更新详情" @click="closeEntry">
                  <svg viewBox="0 0 24 24" width="1.25rem" height="1.25rem" aria-hidden="true">
                    <path fill="currentColor"
                          d="m12 13.4-4.9 4.9q-.275.275-.7.275t-.7-.275-.275-.7.275-.7l4.9-4.9-4.9-4.9q-.275-.275-.275-.7t.275-.7.7-.275.7.275l4.9 4.9 4.9-4.9q.275-.275.7-.275t.7.275.275.7-.275.7L13.4 12l4.9 4.9q.275.275.275.7t-.275.7-.7.275-.7-.275z"/>
                  </svg>
                </button>
              </div>
              <div class="blog-log-dialog-body">
                <article class="blog-log-entry" :class="'tone-' + activeEntry.tone">
                  <div class="blog-log-entry-meta">
                    <span class="blog-log-entry-kind">
                      <span class="blog-log-dot" aria-hidden="true"></span>{{ activeEntry.kind }}
                    </span>
                    <time :datetime="activeEntry.logDate">{{ dotDate(activeEntry.logDate) }}</time>
                  </div>
                  <h2 id="blog-log-entry-title" class="blog-log-entry-title" tabindex="-1">
                    {{ displayTitle(activeEntry) }}
                  </h2>
                  <p class="blog-log-entry-summary">{{ activeEntry.summary }}</p>

                  <div class="blog-log-version">
                    <span>版本</span>
                    <strong>{{ versionLabel(activeEntry) }}</strong>
                  </div>

                  <section class="blog-log-detail-section" v-if="activeEntry.topics.length">
                    <h3>涉及内容</h3>
                    <ul class="blog-log-tags">
                      <li v-for="topic in activeEntry.topics" :key="topic">{{ topic }}</li>
                    </ul>
                  </section>

                  <section class="blog-log-detail-section" v-if="activeEntry.details.length">
                    <h3>更新详情</h3>
                    <ol class="blog-log-details">
                      <li v-for="(detail, index) in activeEntry.details" :key="index">
                        <span>{{ String(index + 1).padStart(2, '0') }}</span>
                        <p>{{ detail }}</p>
                      </li>
                    </ol>
                  </section>

                  <section class="blog-log-detail-section" v-if="relatedEntries.length">
                    <h3>相关记录</h3>
                    <ul class="blog-log-related">
                      <li v-for="item in relatedEntries" :key="item.id">
                        <button type="button" @click="openEntryById(item.id)">
                          <span>{{ item.kind }} · {{ dotDate(item.logDate) }}</span>
                          <strong>{{ displayTitle(item) }}</strong>
                        </button>
                      </li>
                    </ul>
                  </section>
                </article>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script>
  import MyFooter from "../components/common/MyFooter.vue";
  import MyAside from "../components/common/MyAside.vue";
  import ArchiveSidebar from "../components/common/ArchiveSidebar.vue";
  import fallbackChangelog from "../assets/data/changelog.json";

  // 五色分类与参考站一致：重大 / 功能 / 优化 / 修复 / 内容
  const TONES = [
    {key: "major", label: "重大更新", short: "重大"},
    {key: "feature", label: "功能迭代", short: "功能"},
    {key: "minor", label: "优化维护", short: "优化"},
    {key: "fix", label: "问题修复", short: "修复"},
    {key: "content", label: "内容更新", short: "内容"}
  ];

  const TONE_COLORS = {
    major: "#8b5cf6",
    feature: "#53a3f2",
    minor: "#10a37f",
    fix: "#e5484d",
    content: "#c98a2b"
  };

  export default {
    name: "BlogLogView",
    components: {
      MyFooter,
      MyAside,
      ArchiveSidebar
    },
    data() {
      return {
        entryList: [],
        loading: true,
        view: "timeline",
        hoverId: null,
        activeIndex: -1,
        wires: [],
        toneList: TONES,
        resizeHandler: null,
        keyHandler: null
      };
    },
    computed: {
      total() {
        return this.entryList.length;
      },
      activeEntry() {
        return this.activeIndex >= 0 ? this.entryList[this.activeIndex] : null;
      },
      // 关系规则：同一分类里，较新的记录连到上一条更早的记录
      relations() {
        const lastByTone = {};
        const pairs = [];
        this.entryList.forEach((entry, index) => {
          if (lastByTone[entry.tone] !== undefined) {
            pairs.push([lastByTone[entry.tone], index]);
          }
          lastByTone[entry.tone] = index;
        });
        return pairs;
      },
      linkedIds() {
        const set = new Set();
        if (this.hoverId === null) {
          return set;
        }
        this.relations.forEach(([a, b]) => {
          const idA = this.entryList[a] && this.entryList[a].id;
          const idB = this.entryList[b] && this.entryList[b].id;
          if (idA === this.hoverId) {
            set.add(idB);
          } else if (idB === this.hoverId) {
            set.add(idA);
          }
        });
        return set;
      },
      relatedEntries() {
        if (!this.activeEntry) {
          return [];
        }
        const ids = [];
        this.relations.forEach(([a, b]) => {
          const idA = this.entryList[a] && this.entryList[a].id;
          const idB = this.entryList[b] && this.entryList[b].id;
          if (idA === this.activeEntry.id) {
            ids.push(idB);
          } else if (idB === this.activeEntry.id) {
            ids.push(idA);
          }
        });
        return ids.map((id) => this.entryList.find((item) => item.id === id)).filter(Boolean);
      }
    },
    created() {
      this.getChangelog();
    },
    mounted() {
      this.resizeHandler = () => this.drawWires();
      window.addEventListener("resize", this.resizeHandler);
      this.keyHandler = (event) => {
        if (event.key === "Escape" && this.activeEntry) {
          this.closeEntry();
        }
      };
      window.addEventListener("keydown", this.keyHandler);
    },
    beforeUnmount() {
      window.removeEventListener("resize", this.resizeHandler);
      window.removeEventListener("keydown", this.keyHandler);
      document.body.style.overflow = "";
    },
    methods: {
      getChangelog() {
        this.$http.get(this.$constant.baseURL + "/webInfo/listChangelog")
          .then((res) => {
            const list = Array.isArray(res.data) ? res.data : [];
            this.entryList = this.normalizeList(list);
          })
          .catch(() => {
            // 静态部署时接口不可用，退回随包的同一份更新数据
            this.entryList = this.normalizeList(fallbackChangelog);
          })
          .finally(() => {
            this.loading = false;
            this.$nextTick(() => this.drawWires());
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
        const text = String(value || "");
        try {
          const parsed = JSON.parse(text || "[]");
          return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
          // 兼容历史记录里字符串内引号未转义的情况：按数组分隔符拆分，保留原文
          const trimmed = text.trim();
          if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return [];
          }
          return trimmed.slice(1, -1)
            .split(/","/)
            .map((item) => item.replace(/^"|"$/g, "").trim())
            .filter(Boolean);
        }
      },
      numberOf(index) {
        return this.entryList.length - index;
      },
      versionLabel(entry) {
        const version = String(entry.version || "");
        return /^v/i.test(version) ? version.toUpperCase() : "V" + version;
      },
      // 数据里的标题带版本前缀，展示时去掉，版本在弹窗与关系图卡片里单独给出
      displayTitle(entry) {
        return String(entry.title || "").replace(/^V[\d.]+\s*[-—]\s*\d{4}[.\-]\d{1,2}[.\-]\d{1,2}\s*/i, "");
      },
      dotDate(date) {
        return String(date || "").replace(/-/g, ".");
      },
      toneColor(tone) {
        return TONE_COLORS[tone] || TONE_COLORS.feature;
      },
      switchView(view) {
        this.view = view;
        this.clearHover();
        this.$nextTick(() => this.drawWires());
      },
      // 侧栏分类/搜索的跳转口径与项目更新日志页一致
      selectSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      selectArticle(articleSearch) {
        if (!this.$common.isEmpty(articleSearch)) {
          this.$router.push({path: "/"});
        }
      },
      clearHover() {
        this.hoverId = null;
      },
      openEntry(index) {
        this.activeIndex = index;
        document.body.style.overflow = "hidden";
      },
      openEntryById(id) {
        const index = this.entryList.findIndex((item) => item.id === id);
        if (index >= 0) {
          this.activeIndex = index;
        }
      },
      closeEntry() {
        this.activeIndex = -1;
        document.body.style.overflow = "";
      },
      // 连线按卡片实际位置计算，窗口变化或切换视图后重算
      drawWires() {
        if (this.view !== "graph") {
          this.wires = [];
          return;
        }
        const grid = this.$refs.grid;
        if (!grid) {
          this.wires = [];
          return;
        }
        const gridRect = grid.getBoundingClientRect();
        const cards = {};
        grid.querySelectorAll("[data-entry-id]").forEach((el) => {
          cards[el.getAttribute("data-entry-id")] = el.getBoundingClientRect();
        });
        this.wires = this.relations.map(([a, b]) => {
          const from = this.entryList[a];
          const to = this.entryList[b];
          const ra = cards[from.id];
          const rb = cards[to.id];
          if (!ra || !rb) {
            return null;
          }
          const x1 = ra.left + ra.width / 2 - gridRect.left;
          const y1 = ra.top + ra.height / 2 - gridRect.top;
          const x2 = rb.left + rb.width / 2 - gridRect.left;
          const y2 = rb.top + rb.height / 2 - gridRect.top;
          const mx = (x1 + x2) / 2;
          return {
            key: from.id + "-" + to.id,
            from: from.id,
            to: to.id,
            tone: from.tone,
            d: `M ${x1} ${y1} C ${mx} ${y1}, ${mx} ${y2}, ${x2} ${y2}`
          };
        }).filter(Boolean);
      }
    }
  }
</script>

<style scoped>
  /* ---------- 页面外壳：与项目更新日志页同一套栅格（单列 / 两栏 / 三栏） ---------- */
  .blog-log-page {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    align-items: start;
    width: 92vw;
    max-width: 112rem;
    margin: 0 auto;
    padding: 76px 16px clamp(48px, 5vw, 72px);
    background: var(--ui-page-bg);
  }

  .blog-log-aside,
  .blog-log-side {
    display: none;
    min-width: 0;
  }

  .blog-log-main {
    min-width: 0;
  }

  /* 中栏铺满 1fr 栅格，不再额外限宽居中，否则宽屏会撑出 40px 以上的空档 */
  .blog-log-inner {
    width: 100%;
    min-width: 0;
  }

  @media (min-width: 768px) {
    .blog-log-page {
      grid-template-columns: 280px minmax(0, 1fr);
    }

    .blog-log-aside {
      display: block;
    }
  }

  @media (min-width: 1280px) {
    .blog-log-page {
      grid-template-columns: 280px minmax(0, 1fr) 280px;
    }

    .blog-log-side {
      display: block;
    }
  }

  /* 五色：与参考站一致，深色下提亮 */
  .blog-log-module {
    --blog-accent: var(--themeBackground);
    --blog-soft: rgba(83, 163, 242, 0.1);
    --tone-major: #8b5cf6;
    --tone-feature: var(--themeBackground);
    --tone-minor: #10a37f;
    --tone-fix: #e5484d;
    --tone-content: #c98a2b;
    width: 100%;
    color: var(--fontColor);
  }

  html.dark .blog-log-module {
    --tone-major: #c4a3f7;
    --tone-minor: #4fd1a5;
    --tone-fix: #f2777a;
    --tone-content: #e3b464;
  }

  .tone-major {
    --entry-accent: var(--tone-major);
  }

  .tone-feature {
    --entry-accent: var(--tone-feature);
  }

  .tone-minor {
    --entry-accent: var(--tone-minor);
  }

  .tone-fix {
    --entry-accent: var(--tone-fix);
  }

  .tone-content {
    --entry-accent: var(--tone-content);
  }

  /* ---------- 页头 ---------- */
  .blog-log-intro {
    margin: 0 0 2.1rem;
  }

  .blog-log-intro h1 {
    display: flex;
    align-items: center;
    gap: 0.65rem;
    margin: 0;
    font-size: clamp(1.55rem, 2.5vw, 1.95rem);
    font-weight: 720;
    letter-spacing: -0.025em;
    line-height: 1.25;
    color: var(--fontColor);
  }

  .blog-log-intro h1 svg {
    color: var(--themeBackground);
  }

  .blog-log-intro p {
    margin: 0.55rem 0 0 2.05rem;
    color: var(--greyFont);
    font-size: 0.9rem;
    line-height: 1.65;
  }

  /* ---------- 区块标题 / 图例 / 视图切换 ---------- */
  .blog-log-heading {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.4rem 1rem;
    margin: 0 0 1.1rem;
    padding-left: 0.85rem;
    border-left: 3px solid var(--themeBackground);
  }

  .blog-log-heading h2 {
    margin: 0;
    font-size: 1.12rem;
    font-weight: 680;
    letter-spacing: -0.015em;
    color: var(--fontColor);
  }

  .blog-log-section-summary {
    display: flex;
    flex: 1 1 auto;
    align-items: baseline;
    gap: 0.65rem;
    min-width: 0;
  }

  .blog-log-section-summary > span {
    color: var(--greyFont);
    font-size: 0.78rem;
  }

  .blog-log-legend {
    display: flex;
    flex: 1 1 100%;
    order: 3;
    flex-wrap: wrap;
    align-items: center;
    justify-content: flex-start;
    gap: 0.3rem 0.75rem;
    min-width: 0;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .blog-log-legend li {
    display: inline-flex;
    align-items: center;
    gap: 0.3rem;
    color: var(--entry-accent);
    font-size: 0.73rem;
    font-weight: 650;
    line-height: 1.5;
    white-space: nowrap;
  }

  .blog-log-legend .blog-log-dot {
    width: 0.48rem;
    height: 0.48rem;
  }

  .blog-log-legend-short {
    display: none;
  }

  .blog-log-view-switch {
    display: flex;
    flex: 0 0 auto;
    gap: 0.3rem;
    margin-left: auto;
  }

  .blog-log-view-switch button {
    padding: 0.34rem 0.6rem;
    border: 1px solid transparent;
    border-radius: 0.45rem;
    background: transparent;
    color: var(--greyFont);
    font: inherit;
    font-size: 0.73rem;
    cursor: pointer;
    transition: border-color 0.15s ease, color 0.15s ease;
  }

  .blog-log-view-switch button[aria-pressed="true"] {
    border-color: var(--themeBackground);
    background: var(--blog-soft);
    color: var(--themeBackground);
    font-weight: 650;
  }

  .blog-log-view-switch button:focus-visible {
    outline: 2px solid var(--themeBackground);
    outline-offset: 2px;
  }

  /* ---------- 时间线 ---------- */
  .blog-log-board {
    position: relative;
    isolation: isolate;
    padding-inline: 0;
  }

  .blog-log-board[data-view="timeline"] [data-changelog-graph],
  .blog-log-board[data-view="graph"] [data-changelog-timeline] {
    display: none;
  }

  .blog-log-state {
    padding: 3rem 1rem;
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    color: var(--greyFont);
    text-align: center;
    font-size: 0.88rem;
  }

  .blog-log-timeline {
    display: grid;
    gap: 1rem;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .blog-log-timeline-item {
    display: grid;
    grid-template-columns: minmax(5.4rem, 6.4rem) 1.2rem minmax(0, 1fr);
    gap: 0.55rem;
    align-items: stretch;
    min-width: 0;
  }

  .blog-log-timeline-date {
    grid-column: 1;
    grid-row: 1;
    justify-self: end;
    padding-top: 0.93rem;
    color: var(--themeBackground);
    font-size: 0.73rem;
    font-variant-numeric: tabular-nums;
    font-weight: 600;
    line-height: 1.5;
    white-space: nowrap;
  }

  .blog-log-timeline-marker {
    position: relative;
    grid-column: 2;
    grid-row: 1;
    display: flex;
    justify-content: center;
  }

  .blog-log-timeline-marker::before {
    position: absolute;
    top: -1rem;
    bottom: -1rem;
    left: 50%;
    width: 1px;
    background: rgba(83, 163, 242, 0.38);
    content: "";
  }

  html.dark .blog-log-timeline-marker::before {
    background: rgba(99, 179, 255, 0.28);
  }

  .blog-log-timeline-item:first-child .blog-log-timeline-marker::before {
    top: 1.5rem;
  }

  .blog-log-timeline-item:last-child .blog-log-timeline-marker::before {
    bottom: calc(100% - 1.5rem);
  }

  .blog-log-timeline-marker > .blog-log-dot {
    position: relative;
    z-index: 1;
    width: 0.72rem;
    height: 0.72rem;
    margin-top: 1.12rem;
    border: 2px solid var(--ui-card-bg);
    border-radius: 50%;
    background: var(--entry-accent);
    box-shadow: 0 0 0 2px rgba(83, 163, 242, 0.09);
  }

  .blog-log-timeline-card {
    grid-column: 3;
    grid-row: 1;
    display: grid;
    gap: 0.34rem;
    min-width: 0;
    padding: 0.7rem 0.9rem;
    border: 1px solid var(--ui-card-border-color);
    border-radius: 0.78rem;
    background: var(--ui-card-bg);
    color: var(--fontColor);
    text-align: left;
    cursor: pointer;
    transition: border-color 0.16s ease, transform 0.16s ease;
  }

  .blog-log-timeline-card:hover {
    border-color: var(--themeBackground);
    transform: translate(2px);
  }

  .blog-log-timeline-card:focus-visible {
    outline: 2px solid var(--themeBackground);
    outline-offset: 2px;
  }

  .blog-log-timeline-heading {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.42rem 0.55rem;
  }

  .blog-log-timeline-heading > strong {
    font-size: 0.91rem;
    font-weight: 680;
    line-height: 1.45;
    color: var(--fontColor);
  }

  .blog-log-timeline-kind {
    padding: 0.11rem 0.44rem;
    border-radius: 999px;
    background: color-mix(in srgb, var(--entry-accent) 12%, transparent);
    color: var(--entry-accent);
    font-size: 0.64rem;
    font-weight: 650;
    line-height: 1.4;
  }

  .blog-log-timeline-summary {
    color: var(--greyFont);
    font-size: 0.76rem;
    line-height: 1.6;
  }

  /* ---------- 关系图 ---------- */
  .blog-log-graph {
    position: relative;
    padding-inline: 1.25rem;
  }

  .blog-log-connectors {
    position: absolute;
    inset: 0;
    z-index: 2;
    width: 100%;
    height: 100%;
    overflow: visible;
    pointer-events: none;
  }

  .blog-log-wire {
    fill: none;
    stroke-width: 1.5;
    opacity: 0.22;
    transition: opacity 0.15s ease;
  }

  .blog-log-wire.is-hover {
    opacity: 1;
    stroke: var(--themeBackground);
    stroke-width: 1.75;
    stroke-dasharray: 6 5;
    animation: blog-log-dash 0.7s linear infinite;
  }

  @keyframes blog-log-dash {
    to {
      stroke-dashoffset: -11;
    }
  }

  .blog-log-grid {
    position: relative;
    z-index: 1;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 0.875rem;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .blog-log-item {
    position: relative;
    min-width: 0;
    aspect-ratio: 1.12 / 1;
    transition: opacity 0.25s ease;
  }

  .blog-log-board.is-hovering .blog-log-item {
    opacity: 0.3;
  }

  .blog-log-board.is-hovering .blog-log-item.is-active,
  .blog-log-board.is-hovering .blog-log-item.is-linked {
    opacity: 1;
  }

  .blog-log-item.is-active .blog-log-card {
    box-shadow: 0 0.5rem 1.5rem rgba(0, 0, 0, 0.12);
  }

  .blog-log-item.is-linked .blog-log-card {
    box-shadow: inset 0 0 0 1.5px var(--themeBackground);
  }

  .blog-log-card {
    position: relative;
    display: flex;
    width: 100%;
    height: 100%;
    min-height: 0;
    flex-direction: column;
    align-items: flex-start;
    gap: 0.375rem;
    overflow: hidden;
    padding: 0.875rem 1rem;
    border: 1px solid var(--ui-card-border-color);
    border-radius: 0.48rem;
    background: var(--ui-card-bg);
    color: var(--fontColor);
    text-align: left;
    cursor: pointer;
    transition: color 0.16s ease, transform 0.16s ease, box-shadow 0.16s ease;
  }

  .blog-log-card:hover {
    color: var(--themeBackground);
    transform: translateY(-2px);
  }

  .blog-log-number {
    position: absolute;
    z-index: 0;
    right: 0.45rem;
    bottom: 0.25rem;
    color: var(--greyFont);
    font-size: 2.75rem;
    font-variant-numeric: tabular-nums;
    font-weight: 800;
    letter-spacing: -0.04em;
    line-height: 1;
    opacity: 0.22;
    pointer-events: none;
    user-select: none;
  }

  .blog-log-meta,
  .blog-log-kind {
    display: flex;
    align-items: center;
  }

  .blog-log-meta {
    z-index: 1;
    width: 100%;
    justify-content: space-between;
    gap: 0.5rem;
    color: var(--greyFont);
    font-size: 0.75rem;
    line-height: 1.35;
  }

  .blog-log-kind {
    min-width: 0;
    flex: 1 1 auto;
    gap: 0.38rem;
    color: var(--entry-accent);
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .blog-log-meta time {
    flex: 0 0 auto;
    color: var(--themeBackground);
    font-variant-numeric: tabular-nums;
    letter-spacing: 0.015em;
  }

  .blog-log-card-version {
    z-index: 1;
    margin-top: auto;
    order: 5;
    color: var(--themeBackground);
    font-size: 0.69rem;
    font-weight: 600;
    font-variant-numeric: tabular-nums;
    letter-spacing: 0.02em;
    line-height: 1.2;
  }

  .blog-log-dot {
    display: inline-block;
    width: 0.42rem;
    height: 0.42rem;
    flex: 0 0 auto;
    border-radius: 50%;
    background: var(--entry-accent, var(--themeBackground));
  }

  .blog-log-card-title {
    z-index: 1;
    display: -webkit-box;
    width: 100%;
    overflow: hidden;
    color: var(--fontColor);
    font-size: 0.9375rem;
    font-weight: 700;
    line-height: 1.4;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  .blog-log-summary {
    z-index: 1;
    display: -webkit-box;
    width: 100%;
    overflow: hidden;
    color: var(--greyFont);
    font-size: 0.8125rem;
    line-height: 1.55;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  /* ---------- 弹窗 ---------- */
  .blog-log-dialog-backdrop {
    position: fixed;
    inset: 0;
    z-index: 3000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 1rem;
    background: rgba(16, 19, 25, 0.48);
    backdrop-filter: blur(6px);
  }

  .blog-log-dialog {
    width: min(42rem, calc(100vw - 2rem));
    max-height: min(52rem, calc(100dvh - 2rem));
    overflow: hidden;
    border: 1px solid var(--ui-card-border-color);
    border-radius: 0.85rem;
    background: var(--ui-card-bg);
    color: var(--fontColor);
    box-shadow: 0 1.5rem 5rem rgba(0, 0, 0, 0.26);
  }

  .blog-log-dialog-frame {
    display: grid;
    max-height: inherit;
    grid-template-rows: auto minmax(0, 1fr);
  }

  .blog-log-dialog-bar {
    display: flex;
    min-height: 3.6rem;
    align-items: center;
    justify-content: space-between;
    padding: 0.6rem 1.4rem;
    border-bottom: 1px solid var(--ui-card-border-color);
    color: var(--greyFont);
    font-size: 0.82rem;
    font-weight: 630;
  }

  .blog-log-close {
    display: grid;
    width: 2.35rem;
    height: 2.35rem;
    place-items: center;
    padding: 0;
    border: 0;
    border-radius: 50%;
    background: transparent;
    color: var(--greyFont);
    cursor: pointer;
    transition: color 0.15s ease, background-color 0.15s ease;
  }

  .blog-log-close:hover {
    background: var(--blog-soft);
    color: var(--fontColor);
  }

  .blog-log-dialog-body {
    min-height: 0;
    overflow: auto;
    overscroll-behavior: contain;
    padding: 1.4rem clamp(1.25rem, 4vw, 2rem) 2rem;
    scrollbar-width: none;
  }

  .blog-log-dialog-body::-webkit-scrollbar {
    display: none;
  }

  .blog-log-entry-meta,
  .blog-log-entry-kind {
    display: flex;
    align-items: center;
  }

  .blog-log-entry-meta {
    justify-content: space-between;
    gap: 0.8rem;
    color: var(--greyFont);
    font-size: 0.78rem;
    line-height: 1.5;
  }

  .blog-log-entry-kind {
    gap: 0.45rem;
    color: var(--entry-accent);
    font-weight: 650;
  }

  .blog-log-entry-meta time {
    color: var(--themeBackground);
    font-variant-numeric: tabular-nums;
  }

  .blog-log-entry-title {
    margin: 0.85rem 0 0;
    font-size: clamp(1.45rem, 3.8vw, 1.85rem);
    font-weight: 720;
    letter-spacing: -0.025em;
    line-height: 1.35;
    color: var(--fontColor);
  }

  .blog-log-entry-summary {
    margin: 0.8rem 0 1.2rem;
    color: var(--greyFont);
    font-size: 0.94rem;
    line-height: 1.8;
  }

  .blog-log-version {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    margin: 1.2rem 0 1.5rem;
    padding: 0.85rem 1rem;
    border: 1px solid var(--ui-card-border-color);
    border-radius: 0.6rem;
    background: var(--blog-soft);
  }

  .blog-log-version span {
    color: var(--greyFont);
    font-size: 0.8rem;
  }

  .blog-log-version strong {
    color: var(--themeBackground);
    font-size: 0.94rem;
    font-variant-numeric: tabular-nums;
  }

  .blog-log-detail-section {
    margin-top: 1.6rem;
  }

  .blog-log-detail-section h3 {
    margin: 0 0 0.75rem;
    font-size: 1rem;
    font-weight: 680;
    color: var(--fontColor);
  }

  .blog-log-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .blog-log-tags li {
    padding: 0.38rem 0.65rem;
    border-radius: 999px;
    background: var(--blog-soft);
    color: var(--themeBackground);
    font-size: 0.76rem;
    font-weight: 580;
  }

  .blog-log-details {
    display: grid;
    gap: 0.65rem;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .blog-log-details li {
    display: grid;
    grid-template-columns: 1.8rem minmax(0, 1fr);
    gap: 0.7rem;
    align-items: start;
    padding: 0.8rem 0;
    border-bottom: 1px solid var(--ui-card-border-color);
  }

  .blog-log-details li > span {
    padding-top: 0.12rem;
    color: var(--themeBackground);
    font-size: 0.74rem;
    font-variant-numeric: tabular-nums;
    font-weight: 700;
  }

  .blog-log-details p {
    margin: 0;
    color: var(--fontColor);
    font-size: 0.9rem;
    line-height: 1.75;
  }

  .blog-log-related {
    display: grid;
    gap: 0.55rem;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .blog-log-related button {
    display: grid;
    width: 100%;
    gap: 0.25rem;
    padding: 0.8rem 0.9rem;
    border: 1px solid var(--ui-card-border-color);
    border-radius: 0.55rem;
    background: var(--ui-card-bg);
    color: var(--fontColor);
    text-align: left;
    cursor: pointer;
    transition: border-color 0.15s ease, background-color 0.15s ease;
  }

  .blog-log-related button:hover {
    border-color: var(--themeBackground);
    background: var(--blog-soft);
  }

  .blog-log-related button span {
    color: var(--greyFont);
    font-size: 0.73rem;
  }

  .blog-log-related button strong {
    font-size: 0.84rem;
    font-weight: 630;
    line-height: 1.5;
  }

  /* 弹窗过渡 */
  .blog-log-dialog-enter-active,
  .blog-log-dialog-leave-active {
    transition: opacity 0.22s ease;
  }

  .blog-log-dialog-enter-active .blog-log-dialog,
  .blog-log-dialog-leave-active .blog-log-dialog {
    transition: transform 0.22s cubic-bezier(0.22, 1, 0.36, 1);
  }

  .blog-log-dialog-enter-from,
  .blog-log-dialog-leave-to {
    opacity: 0;
  }

  .blog-log-dialog-enter-from .blog-log-dialog,
  .blog-log-dialog-leave-to .blog-log-dialog {
    transform: translateY(1rem) scale(0.985);
  }

  /* ---------- 断点：与参考站一致 5 → 4 → 3 → 2 列 ---------- */
  @media screen and (max-width: 1200px) {
    .blog-log-grid {
      grid-template-columns: repeat(4, minmax(0, 1fr));
    }
  }

  @media screen and (max-width: 900px) {
    .blog-log-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }

    .blog-log-heading {
      align-items: flex-start;
      flex-wrap: wrap;
    }

    .blog-log-legend {
      order: 3;
      flex-basis: 100%;
      justify-content: flex-start;
    }
  }

  @media screen and (max-width: 640px) {
    .blog-log-heading {
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      align-items: center;
      gap: 0.45rem 0.5rem;
    }

    .blog-log-section-summary {
      grid-column: 1;
      grid-row: 1;
      flex-direction: row;
      flex-wrap: wrap;
      align-items: baseline;
      gap: 0.15rem 0.5rem;
    }

    .blog-log-heading h2 {
      font-size: 1rem;
    }

    .blog-log-legend {
      grid-column: 1 / -1;
      grid-row: 2;
      display: flex;
      flex-wrap: nowrap;
      justify-content: center;
      gap: 0.8rem;
    }

    .blog-log-legend li {
      gap: 0.2rem;
      font-size: 0.76rem;
    }

    .blog-log-legend-full {
      display: none;
    }

    .blog-log-legend-short {
      display: inline;
    }

    .blog-log-view-switch {
      grid-column: 2;
      grid-row: 1;
      gap: 0.15rem;
    }

    .blog-log-view-switch button {
      min-height: 2.125rem;
      padding: 0.35rem 0.55rem;
      font-size: 0.72rem;
    }

    .blog-log-timeline-item {
      grid-template-columns: minmax(4.1rem, 4.9rem) 1rem minmax(0, 1fr);
      gap: 0.35rem;
    }

    .blog-log-timeline-date {
      font-size: 0.63rem;
    }

    .blog-log-timeline-card {
      padding: 0.62rem 0.72rem;
    }

    .blog-log-timeline-heading > strong {
      font-size: 0.82rem;
    }

    .blog-log-timeline-summary {
      font-size: 0.7rem;
    }

    .blog-log-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0.625rem;
    }

    .blog-log-item {
      aspect-ratio: 1;
    }

    .blog-log-card {
      min-height: 0;
      padding: 0.75rem 0.875rem;
    }

    .blog-log-number {
      font-size: 2.25rem;
    }

    .blog-log-dialog-backdrop {
      padding: 0.5rem;
    }

    .blog-log-dialog {
      width: calc(100vw - 1rem);
      max-height: calc(100dvh - 1rem);
    }

    .blog-log-dialog-bar {
      padding: 0.4rem 0.9rem;
    }

    .blog-log-dialog-body {
      padding: 1.15rem 1rem 1.5rem;
    }

    .blog-log-entry-meta {
      align-items: flex-start;
      flex-direction: column;
      gap: 0.25rem;
    }
  }

  @media screen and (max-width: 420px) {
    .blog-log-intro {
      margin-bottom: 1.7rem;
    }

    .blog-log-intro p {
      margin-left: 0;
      font-size: 0.85rem;
    }

    .blog-log-heading {
      margin-bottom: 0.85rem;
    }

    .blog-log-view-switch button {
      padding-inline: 0.48rem;
      font-size: 0.68rem;
    }

    .blog-log-timeline-item {
      grid-template-columns: 3.8rem 0.8rem minmax(0, 1fr);
      gap: 0.25rem;
    }

    .blog-log-timeline-date {
      font-size: 0.58rem;
    }

    .blog-log-timeline-card {
      padding: 0.58rem 0.62rem;
    }

    .blog-log-grid {
      gap: 0.7rem;
    }

    .blog-log-card {
      gap: 0.48rem;
      padding: 0.72rem;
    }

    .blog-log-meta {
      font-size: 0.63rem;
    }

    .blog-log-card-title {
      font-size: 0.81rem;
    }

    .blog-log-card-version {
      font-size: 0.6rem;
    }

    .blog-log-summary {
      font-size: 0.7rem;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .blog-log-wire.is-hover {
      animation: none;
    }

    .blog-log-timeline-card,
    .blog-log-card,
    .blog-log-close,
    .blog-log-related button {
      transition: none;
    }
  }
</style>
