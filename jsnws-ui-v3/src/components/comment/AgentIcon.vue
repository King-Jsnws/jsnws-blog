<template>
  <!-- 留言 / 评论里的浏览器、系统标签图标：内联 SVG、自托管、按品牌配色 -->
  <svg class="art-agent-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <template v-if="kind === 'browser'">
      <template v-if="brand === 'chrome'">
        <path fill="#EA4335" d="M4.21 7.5A9 9 0 0 1 19.79 7.5L12 12z"/>
        <path fill="#34A853" d="M12 21A9 9 0 0 1 4.21 7.5L12 12z"/>
        <path fill="#FBBC05" d="M19.79 7.5A9 9 0 0 1 12 21l-2.21-9z"/>
        <circle cx="12" cy="12" r="4" fill="#fff"/>
        <circle cx="12" cy="12" r="3.05" fill="#4285F4"/>
      </template>
      <template v-else-if="brand === 'edge'">
        <circle cx="12" cy="12" r="9" fill="#0F6CBD"/>
        <path fill="#2FB36D" d="M3 13.6a9 9 0 0 0 16.4 4.5 7.7 7.7 0 0 1-13.3-2.5A9.4 9.4 0 0 1 3 13.6z"/>
        <path d="M15.6 13.5A4.3 4.3 0 1 1 15.6 10.5" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/>
        <path d="M11.8 12h3.9" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/>
      </template>
      <template v-else-if="brand === 'firefox'">
        <circle cx="12" cy="12" r="8.6" fill="#5B2BE0"/>
        <path d="M20.4 10.1c-.6-2.2-2.2-4-4.4-5.1 1.9 1.6 3 3.7 3 5.9 0 3.9-3.4 7-7.6 7-3.4 0-6.3-2.1-7.3-5-1 1.5-1.4 3.2-1.1 4.9.6 3 3.4 5.3 6.8 5.3 4.7 0 8.5-3.6 8.5-8.1 0-1.7-.6-3.3-1.8-4.6z" fill="#FF7139"/>
        <path d="M8.6 5.6c1.1-.5 2.2-.7 3.4-.7 2.5 0 4.7 1.1 6.1 3-1.8-.7-3.7-.6-5.3.3-1.8 1-2.9 2.7-3 4.7-.1 1.7.6 3.3 1.9 4.4-2-.6-3.7-2.1-4.5-4-.6-1.3-.7-2.6-.3-3.9.3-1.4.9-2.7 1.7-3.8z" fill="#FFC24B"/>
      </template>
      <template v-else-if="brand === 'safari'">
        <circle cx="12" cy="12" r="9" fill="#2CA5E0"/>
        <circle cx="12" cy="12" r="7.1" fill="none" stroke="#fff" stroke-width="1"/>
        <path d="M16.4 7.6 13.5 12.4 11.6 10.5z" fill="#F5333F"/>
        <path d="M7.6 16.4 10.5 11.6 12.4 13.5z" fill="#fff"/>
      </template>
      <template v-else-if="brand === 'opera'">
        <circle cx="12" cy="12" r="9" fill="#FF1B2D"/>
        <circle cx="12" cy="12" r="4.2" fill="#fff"/>
      </template>
      <template v-else-if="brand === 'wechat'">
        <path d="M9.8 4.4c-3.6 0-6.5 2.4-6.5 5.3 0 1.7.9 3.2 2.4 4.2l-.6 2.1 2.4-1.2c.7.2 1.5.3 2.3.3.3 0 .7 0 1-.1-.2-.6-.3-1.2-.3-1.9 0-2.9 2.8-5.2 6.2-5.2h.6c-.9-2.1-3.7-3.5-7.5-3.5z" fill="#07C160"/>
        <path d="M21.2 13.1c0-2.3-2.3-4.2-5.1-4.2s-5.1 1.9-5.1 4.2 2.3 4.2 5.1 4.2c.6 0 1.2-.1 1.8-.3l1.9 1-.5-1.7c1.2-.8 1.9-2 1.9-3.2z" fill="#7ADB9A"/>
        <circle cx="7.9" cy="9.2" r=".8" fill="#fff"/>
        <circle cx="11.7" cy="9.2" r=".8" fill="#fff"/>
        <circle cx="14.6" cy="12.3" r=".7" fill="#fff"/>
        <circle cx="17.7" cy="12.3" r=".7" fill="#fff"/>
      </template>
      <template v-else>
        <circle cx="12" cy="12" r="9" fill="#8C97A8"/>
        <ellipse cx="12" cy="12" rx="3.9" ry="8.2" fill="none" stroke="#fff" stroke-width="1.2"/>
        <path d="M3.4 9.3h17.2M3.4 14.7h17.2" fill="none" stroke="#fff" stroke-width="1.2"/>
      </template>
    </template>

    <template v-else>
      <template v-if="brand === 'windows'">
        <rect x="3.5" y="3.5" width="7.5" height="7.5" fill="#F25022"/>
        <rect x="13" y="3.5" width="7.5" height="7.5" fill="#7FBA00"/>
        <rect x="3.5" y="13" width="7.5" height="7.5" fill="#00A4EF"/>
        <rect x="13" y="13" width="7.5" height="7.5" fill="#FFB900"/>
      </template>
      <template v-else-if="brand === 'apple'">
        <!-- iOS / iPadOS / macOS 统一黑色苹果标；深色模式也保持纯黑，不反白 -->
        <path fill="#000" d="M12.152 6.896c-.948 0-2.415-1.078-3.96-1.04-2.04.027-3.91 1.183-4.961 3.014-2.117 3.675-.546 9.103 1.519 12.09 1.013 1.454 2.208 3.09 3.792 3.039 1.52-.065 2.09-.987 3.935-.987 1.831 0 2.35.987 3.96.948 1.637-.026 2.676-1.48 3.676-2.948 1.156-1.688 1.636-3.325 1.662-3.415-.039-.013-3.182-1.221-3.22-4.857-.026-3.04 2.48-4.494 2.597-4.559-1.429-2.09-3.623-2.324-4.39-2.376-2-.156-3.675 1.09-4.61 1.09zM15.53 3.83c.843-1.012 1.4-2.427 1.245-3.83-1.207.052-2.662.805-3.532 1.818-.78.896-1.454 2.338-1.273 3.714 1.338.104 2.715-.688 3.559-1.701"/>
      </template>
      <template v-else-if="brand === 'linux'">
        <ellipse cx="12" cy="13.4" rx="6.2" ry="7.4" fill="#3A4350" stroke="#8A97AB" stroke-width="0.7"/>
        <ellipse cx="12" cy="14.8" rx="4" ry="5.2" fill="#fff"/>
        <circle cx="9.7" cy="8.5" r="2.1" fill="#3A4350" stroke="#8A97AB" stroke-width="0.7"/>
        <circle cx="14.3" cy="8.5" r="2.1" fill="#3A4350" stroke="#8A97AB" stroke-width="0.7"/>
        <circle cx="9.9" cy="8.4" r=".8" fill="#fff"/>
        <circle cx="14.1" cy="8.4" r=".8" fill="#fff"/>
        <path d="M10.7 10.6h2.6l-1.3 2z" fill="#F5A623"/>
        <path d="M8.2 20.4c.4.9 1.1 1.4 1.8 1.4h1.1l-.7-2.6zM15.8 20.4c-.4.9-1.1 1.4-1.8 1.4h-1.1l.7-2.6z" fill="#F5A623"/>
      </template>
      <template v-else-if="brand === 'android'">
        <path d="M6.9 5.2 8.6 7.2M17.1 5.2 15.4 7.2" fill="none" stroke="#3DDC84" stroke-width="1.3" stroke-linecap="round"/>
        <rect x="4.6" y="8.2" width="14.8" height="9.4" rx="4.7" fill="#3DDC84"/>
        <circle cx="9.5" cy="12.2" r="1.1" fill="#fff"/>
        <circle cx="14.5" cy="12.2" r="1.1" fill="#fff"/>
      </template>
      <template v-else>
        <rect x="3.2" y="4.4" width="17.6" height="11.6" rx="2" fill="#8C97A8"/>
        <rect x="5.4" y="6.6" width="13.2" height="7.2" rx="1" fill="#E8EEF6"/>
        <path d="M9.4 20.2h5.2M12 16v4.2" fill="none" stroke="#8C97A8" stroke-width="1.6" stroke-linecap="round"/>
      </template>
    </template>
  </svg>
</template>

<script>
  export default {
    name: "AgentIcon",
    props: {
      kind: {
        type: String,
        default: "browser"
      },
      // 后端下发的原始文案，如 "Chrome142.0"、"Windows 10"
      name: {
        type: String,
        default: ""
      }
    },
    computed: {
      brand() {
        const label = this.name.toLowerCase();
        if (this.kind === "os") {
          if (label.startsWith("windows")) return "windows";
          // iOS / iPadOS / macOS 共用黑色苹果标
          if (label.startsWith("macos") || label.startsWith("ios") || label.startsWith("ipados")) return "apple";
          if (label.startsWith("android")) return "android";
          if (label.startsWith("linux")) return "linux";
          // Chrome OS 用 Chrome 的品牌色，仍是 Google 体系
          if (label.startsWith("chrome os")) return "chrome";
          return "generic";
        }
        if (label.startsWith("edge")) return "edge";
        if (label.startsWith("opera")) return "opera";
        if (label.startsWith("wechat")) return "wechat";
        if (label.startsWith("firefox")) return "firefox";
        if (label.startsWith("safari")) return "safari";
        if (label.startsWith("chrome")) return "chrome";
        return "generic";
      }
    }
  };
</script>

<style scoped>
  .art-agent-icon {
    width: 12px;
    height: 12px;
    /* 跟 10px 标签文字居中对齐，同时不撑高标签 */
    vertical-align: -2px;
    margin-right: 3px;
    flex: none;
  }
</style>
