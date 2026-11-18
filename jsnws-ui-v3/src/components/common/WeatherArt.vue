<template>
  <!-- 天气插画：按天气文案映射到对应场景，纯内联 SVG，配色随主题变量切换 -->
  <div class="weather-art" aria-hidden="true">
    <svg viewBox="0 0 64 64" role="presentation">
      <!-- 晴：太阳光芒缓慢旋转 + 核心轻微呼吸 -->
      <g v-if="scene === 'sunny'">
        <g class="wa-spin">
          <g stroke="var(--wa-sun-line)" stroke-width="2.6" stroke-linecap="round">
            <path d="M32 6v5M32 53v5M6 32h5M53 32h5M13.6 13.6l3.5 3.5M46.9 46.9l3.5 3.5M50.4 13.6l-3.5 3.5M17.1 46.9l-3.5 3.5"/>
          </g>
        </g>
        <circle class="wa-breathe" cx="32" cy="32" r="10.5" fill="var(--wa-sun)"/>
      </g>

      <!-- 多云：太阳 + 云层缓慢横向漂移 -->
      <g v-else-if="scene === 'partly'">
        <g class="wa-spin">
          <g stroke="var(--wa-sun-line)" stroke-width="2.2" stroke-linecap="round">
            <path d="M24 5v4M24 39v4M6 24h4M38 24h4M11 11l2.6 2.6M34.4 34.4L37 37M37 11l-2.6 2.6M13.6 34.4L11 37"/>
          </g>
        </g>
        <circle cx="24" cy="24" r="7.5" fill="var(--wa-sun)"/>
        <g class="wa-drift">
          <path d="M24 50a7.5 7.5 0 0 1 .7-14.96 10.4 10.4 0 0 1 20-1.8A7 7 0 0 1 47 50z" fill="var(--wa-cloud)"/>
        </g>
      </g>

      <!-- 阴 / 雾：两层云错速漂移 -->
      <g v-else-if="scene === 'overcast'">
        <g class="wa-drift wa-drift-slow">
          <path d="M26 35a6.5 6.5 0 0 1 .6-12.96 9 9 0 0 1 17.3-1.6A6 6 0 0 1 45 35z"
                fill="var(--wa-cloud-deep)" opacity=".8"/>
        </g>
        <g class="wa-drift">
          <path d="M20 51a8 8 0 0 1 .8-15.96 10.5 10.5 0 0 1 20.1-1.9A7.5 7.5 0 0 1 43 51z" fill="var(--wa-cloud)"/>
        </g>
      </g>

      <!-- 雨：云 + 雨滴持续下落 -->
      <g v-else-if="scene === 'rain'">
        <g class="wa-drift">
          <path d="M18 40a8 8 0 0 1 .8-15.96 10.6 10.6 0 0 1 20.3-2A7.6 7.6 0 0 1 44 40z" fill="var(--wa-cloud)"/>
        </g>
        <g stroke="var(--wa-drop)" stroke-width="2.4" stroke-linecap="round">
          <path class="wa-drop wa-drop-1" d="M23 46l-1.5 5"/>
          <path class="wa-drop wa-drop-2" d="M32 46l-1.5 5"/>
          <path class="wa-drop wa-drop-3" d="M41 46l-1.5 5"/>
        </g>
      </g>

      <!-- 暴雨 / 雷雨：深色云 + 闪电 + 更密更快的雨滴 -->
      <g v-else-if="scene === 'storm'">
        <g class="wa-drift">
          <path d="M18 40a8 8 0 0 1 .8-15.96 10.6 10.6 0 0 1 20.3-2A7.6 7.6 0 0 1 44 40z" fill="var(--wa-cloud-deep)"/>
        </g>
        <path class="wa-bolt" d="M33 37h6.5l-4.8 7.4h4.2L30 57l2.3-8.2h-4.5z" fill="var(--wa-bolt)"/>
        <g stroke="var(--wa-drop)" stroke-width="2.2" stroke-linecap="round">
          <path class="wa-drop wa-drop-fast wa-drop-1" d="M17 44l-2 6"/>
          <path class="wa-drop wa-drop-fast wa-drop-2" d="M24 44l-2 6"/>
          <path class="wa-drop wa-drop-fast wa-drop-3" d="M46 44l-2 6"/>
          <path class="wa-drop wa-drop-fast wa-drop-2" d="M52 44l-2 6"/>
        </g>
      </g>

      <!-- 雪：云 + 雪花下落并轻微左右摆动 -->
      <g v-else-if="scene === 'snow'">
        <g class="wa-drift">
          <path d="M18 40a8 8 0 0 1 .8-15.96 10.6 10.6 0 0 1 20.3-2A7.6 7.6 0 0 1 44 40z" fill="var(--wa-cloud)"/>
        </g>
        <g fill="var(--wa-snow)">
          <circle class="wa-flake wa-flake-1" cx="23" cy="47" r="2"/>
          <circle class="wa-flake wa-flake-2" cx="32" cy="50" r="2"/>
          <circle class="wa-flake wa-flake-3" cx="41" cy="47" r="2"/>
        </g>
      </g>

      <!-- 兜底：无法识别的天气文案，中性云，不做动效 -->
      <g v-else>
        <path d="M18 43a8 8 0 0 1 .8-15.96 10.6 10.6 0 0 1 20.3-2A7.6 7.6 0 0 1 44 43z"
              fill="var(--wa-cloud)" opacity=".75"/>
      </g>
    </svg>
  </div>
</template>

<script>
  export default {
    name: "WeatherArt",
    props: {
      // 接口返回的天气文案，如「晴」「多云」「暴雨」「小雪」
      desc: {
        type: String,
        default: ""
      }
    },
    computed: {
      // 文案容错映射：先匹配强语义（雷/暴雨/雪），再匹配普通雨、阴、多云、晴，均不命中则中性兜底
      scene() {
        const desc = this.desc || "";
        if (/雷|暴雨|大雨|强阵雨/.test(desc)) {
          return "storm";
        }
        if (/雪|霰|冰粒/.test(desc)) {
          return "snow";
        }
        if (/雨/.test(desc)) {
          return "rain";
        }
        if (/阴|雾|霾|沙|尘/.test(desc)) {
          return "overcast";
        }
        if (/多云|晴间/.test(desc)) {
          return "partly";
        }
        if (/晴/.test(desc)) {
          return "sunny";
        }
        return "neutral";
      }
    }
  }
</script>

<style scoped>

  .weather-art {
    /* 亮色：偏亮的云与雨滴；深色：降低明度避免在深底上发白发刺眼 */
    --wa-sun: #f7c948;
    --wa-sun-line: #f0b429;
    --wa-cloud: #cfe2f2;
    --wa-cloud-deep: #a9c6de;
    --wa-drop: #79b0e0;
    --wa-snow: #e6f1fa;
    --wa-bolt: #f3bc3f;
    width: 64px;
    height: 64px;
    pointer-events: none;
  }

  html.dark .weather-art {
    --wa-sun: #f0c35a;
    --wa-sun-line: #e0ae46;
    --wa-cloud: #7e93a6;
    --wa-cloud-deep: #64798c;
    --wa-drop: #79aedd;
    --wa-snow: #cfe2f2;
    --wa-bolt: #f2c159;
  }

  .weather-art svg {
    display: block;
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  /* 太阳：光芒缓慢旋转 */
  .wa-spin {
    transform-origin: 32px 32px;
    animation: wa-spin 30s linear infinite;
  }

  @keyframes wa-spin {
    to {
      transform: rotate(360deg);
    }
  }

  /* 太阳核心：轻微呼吸 */
  .wa-breathe {
    transform-origin: 32px 32px;
    animation: wa-breathe 5s ease-in-out infinite;
  }

  @keyframes wa-breathe {
    0%, 100% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.07);
    }
  }

  /* 云层：缓慢横向漂移 */
  .wa-drift {
    animation: wa-drift 7s ease-in-out infinite alternate;
  }

  .wa-drift-slow {
    animation-duration: 11s;
  }

  @keyframes wa-drift {
    from {
      transform: translateX(-1.6px);
    }
    to {
      transform: translateX(1.6px);
    }
  }

  /* 雨滴：下落 + 淡入淡出；暴雨用更短周期与更短延迟 */
  .wa-drop {
    animation: wa-fall 1.5s linear infinite;
  }

  .wa-drop-fast {
    animation-duration: 0.85s;
  }

  .wa-drop-2 {
    animation-delay: 0.45s;
  }

  .wa-drop-3 {
    animation-delay: 0.9s;
  }

  @keyframes wa-fall {
    0% {
      transform: translateY(-2px);
      opacity: 0;
    }
    25% {
      opacity: 1;
    }
    100% {
      transform: translateY(9px);
      opacity: 0;
    }
  }

  /* 雪花：下落同时左右摆动 */
  .wa-flake {
    transform-box: fill-box;
    transform-origin: center;
    animation: wa-snow 3.6s linear infinite;
  }

  .wa-flake-2 {
    animation-delay: 1.2s;
  }

  .wa-flake-3 {
    animation-delay: 2.4s;
  }

  @keyframes wa-snow {
    0% {
      transform: translate(0, -2px);
      opacity: 0;
    }
    20% {
      opacity: 0.95;
    }
    50% {
      transform: translate(2.2px, 4px);
    }
    80% {
      opacity: 0.95;
    }
    100% {
      transform: translate(-1.4px, 10px);
      opacity: 0;
    }
  }

  /* 系统关闭动效时静态显示，所有图形保持完整可见 */
  @media (prefers-reduced-motion: reduce) {
    .weather-art .wa-spin,
    .weather-art .wa-breathe,
    .weather-art .wa-drift,
    .weather-art .wa-drop,
    .weather-art .wa-flake {
      animation: none !important;
      opacity: 1;
    }
  }

</style>
