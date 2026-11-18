<template>
  <!-- 分类 / 标签索引卡：各页面侧栏共用，数据直接取全站共享的 sortInfo -->
  <div v-if="categories.length || tags.length" class="sidebar-index">
    <!-- 分类卡：结构同参考站 widget-layout，折叠高度 7.5rem，底部就地展开/收起 -->
    <section v-if="categories.length"
             class="index-card onload-animation card-categories"
             data-id="categories"
             :style="{ animationDelay: '200ms', '--collapsedHeight': '7.5rem' }">
      <div class="widget-title">
        <span class="widget-name">分类</span>
      </div>
      <div ref="categoriesWrapper"
           class="collapse-wrapper"
           :class="{ 'is-expanded': categoriesExpanded }"
           :style="categoriesExpanded ? { maxHeight: categoriesMaxHeight } : null"
           @transitionend="onCategoriesTransitionEnd">
        <a v-for="sort in categories"
           :key="'side-sort-' + sort.id"
           class="index-link"
           :href="categoryHref(sort)"
           :aria-label="'查看分类 ' + sort.sortName"
           @click.prevent="openSort(sort)">
          <button type="button" class="index-row">
            <span class="index-row-inner">
              <span class="index-name">{{ sort.sortName }}</span>
              <span class="index-count">{{ sort.countOfSort }}</span>
            </span>
          </button>
        </a>
      </div>
      <!-- 只有内容超出折叠高度时才显示「更多 / 收起」，避免出现点了没反应的按钮 -->
      <div v-if="categoriesOverflow || categoriesExpanded" class="expand-btn">
        <button type="button"
                class="widget-more-btn"
                :title="categoriesExpanded ? '收起' : '更多'"
                :aria-label="categoriesExpanded ? '收起' : '更多'"
                :aria-expanded="categoriesExpanded ? 'true' : 'false'"
                :data-expanded="categoriesExpanded"
                @click="toggleCategories">
          <span class="expand-inner">
            <svg v-if="!categoriesExpanded" class="toggle-icon" viewBox="0 0 24 24" fill="currentColor"
                 aria-hidden="true">
              <circle cx="6" cy="12" r="2"/>
              <circle cx="12" cy="12" r="2"/>
              <circle cx="18" cy="12" r="2"/>
            </svg>
            <svg v-else class="toggle-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="m12 10.8-3.9 3.9q-.275.275-.7.275t-.7-.275-.275-.7.275-.7l4.6-4.6q.3-.3.7-.3t.7.3l4.6 4.6q.275.275.275.7t-.275.7-.7.275-.7-.275z"/>
            </svg>
            <span>{{ categoriesExpanded ? '收起' : '更多' }}</span>
          </span>
        </button>
      </div>
    </section>

    <!-- 标签卡：结构同参考站——折叠高度 7.5rem，胶囊跳 /archive?tag=标签名（与全站统一），
         「更多」是跳转 /tags 的链接，没有就地展开/收起 -->
    <section v-if="tags.length"
             class="index-card onload-animation card-tags"
             data-id="tags"
             :style="{ animationDelay: '250ms', '--collapsedHeight': '7.5rem' }">
      <div class="widget-title">
        <span class="widget-name">标签</span>
      </div>
      <div class="collapse-wrapper">
        <div class="tag-chip-row">
          <button v-for="label in tags"
                  :key="'side-label-' + label.sortId + '-' + label.id"
                  type="button"
                  class="tag-chip"
                  @click="openLabel(label)">
            {{ label.labelName }}
          </button>
        </div>
      </div>
      <div class="expand-btn">
        <router-link to="/tags" class="widget-more-btn widget-more-link" title="更多" aria-label="更多">
          <span class="expand-inner">
            <svg class="toggle-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <circle cx="6" cy="12" r="2"/>
              <circle cx="12" cy="12" r="2"/>
              <circle cx="18" cy="12" r="2"/>
            </svg>
            <span>更多</span>
          </span>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script>
  export default {
    name: "SidebarIndexCards",
    data() {
      return {
        categoriesExpanded: false,
        categoriesMaxHeight: "",
        categoriesOverflow: false
      };
    },
    computed: {
      sortInfo() {
        return this.$store.state.sortInfo || [];
      },
      categories() {
        return this.sortInfo;
      },
      tags() {
        const list = [];
        this.sortInfo.forEach((sort) => {
          (sort.labels || []).forEach((label) => {
            list.push({
              id: label.id,
              sortId: sort.id,
              labelName: label.labelName
            });
          });
        });
        return list.sort((a, b) => String(a.labelName).localeCompare(String(b.labelName), "zh-Hans-CN"));
      }
    },
    watch: {
      // 分类数据异步到达后重新判断是否需要「更多 / 收起」
      categories() {
        this.$nextTick(this.measureCategoriesOverflow);
      }
    },
    mounted() {
      this.$nextTick(this.measureCategoriesOverflow);
      window.addEventListener("resize", this.measureCategoriesOverflow);
    },
    beforeUnmount() {
      window.removeEventListener("resize", this.measureCategoriesOverflow);
    },
    methods: {
      // 折叠高度（--collapsedHeight）换算为像素，用于判断内容是否超出
      collapsedHeightPx() {
        const wrapper = this.$refs.categoriesWrapper;
        if (!wrapper) return 120;
        const raw = (getComputedStyle(wrapper).getPropertyValue("--collapsedHeight") || "").trim();
        const value = parseFloat(raw);
        if (Number.isNaN(value)) return 120;
        if (raw.endsWith("rem")) {
          const rootFontSize = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
          return value * rootFontSize;
        }
        return value;
      },
      measureCategoriesOverflow() {
        const wrapper = this.$refs.categoriesWrapper;
        if (!wrapper) {
          this.categoriesOverflow = false;
          return;
        }
        this.categoriesOverflow = wrapper.scrollHeight > this.collapsedHeightPx() + 1;
      },
      // 收起动画结束后再测一次，避免过渡中把 clientHeight 当成内容高度
      onCategoriesTransitionEnd(event) {
        if (event.propertyName !== "max-height" || this.categoriesExpanded) return;
        this.measureCategoriesOverflow();
      },
      // 折叠高度由 CSS 的 --collapsedHeight 控制；展开时按内容实际高度过渡
      toggleCategories() {
        const wrapper = this.$refs.categoriesWrapper;
        if (!wrapper) return;
        if (this.categoriesExpanded) {
          this.categoriesExpanded = false;
          this.categoriesMaxHeight = "";
          return;
        }
        // 直接用内容真实高度，不再抬高到 120px：否则内容不足三行时「展开」与折叠高度相同，看起来点了没反应
        this.categoriesMaxHeight = wrapper.scrollHeight + "px";
        this.categoriesExpanded = true;
      },
      // 分类行与分类页卡片统一：跳归档页并按分类名过滤（参考站同为 ?category=名称）
      categoryHref(sort) {
        return "/archive?category=" + encodeURIComponent(sort.sortName);
      },
      openSort(sort) {
        this.$router.push({path: "/archive", query: {category: sort.sortName}});
      },
      openLabel(label) {
        this.$router.push({path: "/archive", query: {tag: label.labelName}});
      }
    }
  };
</script>

<style scoped>
  .sidebar-index {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
  }

  /* 与参考站 card-base 一致的卡片层：底色 + 16px 圆角 + 1px 描边 + 1px 阴影 */
  .index-card {
    min-width: 0;
    padding: 0 0 16px;
    border: var(--ui-card-border);
    border-radius: var(--ui-card-radius);
    background: var(--ui-card-bg);
    box-shadow: var(--ui-card-shadow);
    --collapsedHeight: 7.5rem;
  }

  /* 参考站 onload-animation：淡入上移，延迟由卡片的 animation-delay 决定 */
  .onload-animation {
    opacity: 0;
    animation: fade-in-up 0.12s ease-out forwards;
  }

  @keyframes fade-in-up {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  /* 标题：18px/700，左缩进 32px、上 16px、下 8px；竖条 4×16、圆角 6px、距卡左 16px */
  .widget-title {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 16px 16px 8px 32px;
    font-size: 18px;
    font-weight: 700;
    line-height: 1.4;
    color: var(--fontColor);
  }

  .widget-title::before {
    content: "";
    position: absolute;
    top: 5.5px;
    left: -16px;
    width: 4px;
    height: 16px;
    border-radius: 6px;
    background: var(--ui-accent);
  }

  /* 列表容器：左右 16px，折叠高度 7.5rem（三行），超出隐藏 */
  .collapse-wrapper {
    overflow: hidden;
    padding: 0 16px;
    max-height: var(--collapsedHeight);
    transition: max-height 0.3s ease;
  }

  .index-link {
    display: block;
    color: inherit;
    text-decoration: none;
  }

  /* 行：40px 高、8px 圆角、左侧 8px，hover 与深色 hover 均变主题色 */
  .index-row {
    display: block;
    width: 100%;
    height: 40px;
    padding: 0 0 0 8px;
    border: 0;
    border-radius: 8px;
    background: none;
    color: var(--fontColor);
    font-family: inherit;
    text-align: left;
    cursor: pointer;
    transition: color 0.15s ease;
  }

  .index-row:hover {
    color: var(--ui-accent);
  }

  .index-row-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    margin-right: 8px;
  }

  .index-name {
    min-width: 0;
    overflow: hidden;
    font-size: 16px;
    text-align: left;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* 数量徽章：最小 32×28、8px 圆角、主题色加粗、无底色无描边 */
  .index-count {
    display: flex;
    flex: none;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 28px;
    margin-left: 16px;
    padding: 0 8px;
    border: 0;
    border-radius: 8px;
    background: none;
    font-size: 14px;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    color: var(--ui-accent);
  }

  /* 标签胶囊：32px 高、12px 内边距、8px 圆角，浅描边 + 卡片底色 + 主题色文字（按截图） */
  .tag-chip-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .tag-chip {
    display: inline-flex;
    align-items: center;
    height: 32px;
    padding: 0 12px;
    border: 1px solid var(--cardBorder);
    border-radius: 8px;
    background: var(--cardBg);
    color: var(--ui-accent);
    font-family: inherit;
    font-size: 14px;
    line-height: 1.4;
    cursor: pointer;
    transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
  }

  .tag-chip:hover {
    border-color: var(--ui-accent);
    background: color-mix(in srgb, var(--ui-accent) 6%, var(--cardBg));
    color: var(--ui-accent);
  }

  /* 底部按钮：左右 16px、底部 -8px；按钮 36px 高、8px 圆角，内容居中并左移 8px */
  .expand-btn {
    padding: 0 16px;
    margin-bottom: -8px;
  }

  .widget-more-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 36px;
    padding: 0;
    border: 1px solid var(--cardBorder);
    border-radius: 8px;
    background: none;
    color: var(--ui-accent);
    font-family: inherit;
    font-size: 14px;
    cursor: pointer;
    transition: border-color 0.15s ease, background-color 0.15s ease;
  }

  .widget-more-btn:hover {
    border-color: var(--ui-accent);
    background: color-mix(in srgb, var(--ui-accent) 8%, transparent);
  }

  /* 标签卡的「更多」是跳转链接（router-link 渲染为 a），去掉下划线 */
  .widget-more-link {
    text-decoration: none;
  }

  .expand-inner {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transform: translateX(-8px);
  }

  .toggle-icon {
    width: 28px;
    height: 28px;
    flex: none;
  }
</style>
