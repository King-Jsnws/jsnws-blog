<template>
  <div class="emoji-panel">
    <!-- 分组切换：微博 / B站 / bmoji，每组各自滚动 -->
    <div class="emoji-panel-tabs">
      <button v-for="group in groups"
              :key="group.key"
              type="button"
              class="emoji-panel-tab"
              :class="{ 'is-active': group.key === activeKey }"
              @click="activeKey = group.key">{{ group.name }}
      </button>
    </div>
    <div class="emoji-panel-list">
      <button v-for="name in activeNames"
              :key="name"
              type="button"
              class="emoji-panel-cell"
              :title="name"
              @click="choose(name)">
        <img :src="emojiUrl(activeKey, name)" :alt="name" loading="lazy" width="24" height="24"/>
      </button>
    </div>
  </div>
</template>

<script>
  import { EMOJI_GROUPS, emojiNames, emojiUrl, emojiToken } from "../../utils/emoji";

  /**
   * 评论 / 留言共用的表情面板：Waline 三套图片表情。
   * 只负责展示与分组切换，点选后把 :包名/文件名: token 抛给使用方插入输入框。
   */
  export default {
    name: "EmojiPanel",
    emits: ["select"],
    data() {
      return {
        groups: EMOJI_GROUPS,
        activeKey: "weibo"
      };
    },
    computed: {
      activeNames() {
        const group = this.groups.find(item => item.key === this.activeKey) || this.groups[0];
        return emojiNames(group);
      }
    },
    methods: {
      emojiUrl,
      choose(name) {
        this.$emit("select", emojiToken(this.activeKey, name));
      }
    }
  }
</script>

<style scoped>
  .emoji-panel {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  .emoji-panel-tabs {
    display: flex;
    gap: 6px;
    margin-bottom: 8px;
  }

  .emoji-panel-tab {
    flex: 1;
    min-width: 0;
    height: 30px;
    padding: 0 4px;
    border: 1px solid rgba(120, 120, 120, 0.18);
    border-radius: 9px;
    background: transparent;
    color: #4b6b66;
    font-size: 12px;
    line-height: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    cursor: pointer;
    transition: color 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
  }

  .emoji-panel-tab:hover {
    border-color: rgba(120, 120, 120, 0.42);
  }

  .emoji-panel-tab.is-active {
    border-color: var(--themeBackground);
    background: var(--themeBackground);
    color: #ffffff;
  }

  /* 每组固定露出六行（6×28 + 5×4 = 188），超出的在面板内滚动，
     不把上百个图标一次铺满屏幕，也不会露出半行破口 */
  .emoji-panel-list {
    display: grid;
    grid-template-columns: repeat(10, minmax(0, 1fr));
    gap: 4px;
    max-height: 188px;
    padding-right: 2px;
    overflow-y: auto;
    overscroll-behavior: contain;
    scrollbar-width: thin;
  }

  /* 表情格子：透明底，悬停时淡色底 + 图片轻微放大，按压微缩 */
  .emoji-panel-cell {
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 28px;
    padding: 0;
    border: 0;
    border-radius: 8px;
    background: transparent;
    cursor: pointer;
    transition: background-color 0.22s ease, transform 0.18s ease;
  }

  .emoji-panel-cell img {
    display: block;
    width: 24px;
    height: 24px;
    transition: transform 0.18s ease;
  }

  .emoji-panel-cell:hover {
    background: rgba(45, 212, 191, 0.14);
  }

  .emoji-panel-cell:hover img {
    transform: scale(1.12);
  }

  .emoji-panel-cell:active {
    transform: scale(0.94);
  }

  html.dark .emoji-panel-tab {
    border-color: rgba(255, 255, 255, 0.16);
    color: #b7cfca;
  }

  html.dark .emoji-panel-tab:hover {
    border-color: rgba(255, 255, 255, 0.4);
  }

  /* 深色：主题蓝 #63b3ff 偏亮，白字压在上面仅 2.23:1，实心块改用深色字（7.72:1） */
  html.dark .emoji-panel-tab.is-active {
    border-color: var(--themeBackground);
    background: var(--themeBackground);
    color: #0b1b2b;
  }

  html.dark .emoji-panel-cell:hover {
    background: rgba(255, 255, 255, 0.10);
  }

  @media screen and (max-width: 768px) {
    .emoji-panel-list {
      grid-template-columns: repeat(8, minmax(0, 1fr));
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .emoji-panel-tab,
    .emoji-panel-cell,
    .emoji-panel-cell img {
      transition: none;
    }
  }
</style>
