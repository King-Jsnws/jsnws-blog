<template>
  <div class="float-field" :class="{ 'is-filled': isFilled, 'has-fill': fill }" :style="notchStyle">
    <span class="float-border" aria-hidden="true"></span>
    <label ref="label" class="float-label" :for="inputId">{{ label }}</label>
    <!-- 默认用 el-input；留言页这类原生 input（自带按钮/独立背景）通过默认插槽替换 -->
    <el-input
      v-if="!$slots.default"
      :id="inputId"
      class="float-input"
      :model-value="modelValue"
      v-bind="$attrs"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <slot v-else></slot>
  </div>
</template>

<script>
  /**
   * 浮动标签输入框：登录页、注册页、找回密码弹窗共用
   * 标签静止时作为占位文字停在输入框内；聚焦或有内容时上浮缩小并压在上边框上。
   * 边框不用背景色遮挡，而是用 clip-path 在上边框「标签宽度」区间真正挖空，标签背后保持透明。
   * 聚焦态由 CSS :focus-within 驱动，键盘 Tab 聚焦同样生效。
   */
  export default {
    name: "FloatInput",
    inheritAttrs: false,
    props: {
      modelValue: {
        type: [String, Number],
        default: ""
      },
      label: {
        type: String,
        required: true
      },
      // 需与 label 的 for 一致，Element Plus 会把 id 透传到原生 input 上
      inputId: {
        type: String,
        required: true
      },
      // 输入框自身有实心底色（白底胶囊 / 玻璃底）时为 true：
      // 骑线标签会被底色上沿横切，需要在同区间把底色一并挖空，见 .has-fill 样式
      fill: {
        type: Boolean,
        default: false
      }
    },
    emits: ["update:modelValue"],
    data() {
      return {
        // 上浮后标签的渲染宽度（px），决定上边框缺口宽度
        notchWidth: 0
      };
    },
    computed: {
      isFilled() {
        return this.modelValue !== null
          && this.modelValue !== undefined
          && String(this.modelValue).length > 0;
      },
      notchStyle() {
        return {
          "--float-notch-width": this.notchWidth + "px"
        };
      }
    },
    watch: {
      label() {
        this.$nextTick(this.measureLabel);
      }
    },
    mounted() {
      this.measureLabel();
      window.addEventListener("resize", this.measureLabel);
      // 字体加载完成后文字宽度会变，重新量一次
      if (document.fonts && document.fonts.ready) {
        document.fonts.ready.then(this.measureLabel).catch(() => {});
      }
    },
    beforeUnmount() {
      window.removeEventListener("resize", this.measureLabel);
    },
    methods: {
      /**
       * 缺口宽度 = 标签静止时的布局宽度 × 上浮缩放比例（0.8），再左右各放 2px 余量。
       * 天然跟随「账号」「账号 / 邮箱 / 手机号」「确认密码」等长短不同的文案，不写死。
       * 余量单独加：缺口必须比标签盒先张开，字形贴着盒边时也不会被端点切到；
       * 同时让出缩放后 padding 的收缩误差，缺口不会被文字墨迹顶到。
       */
      measureLabel() {
        const label = this.$refs.label;
        if (!label) {
          return;
        }
        this.notchWidth = Math.round((label.offsetWidth * 0.8 + 4) * 100) / 100;
      }
    }
  }
</script>

<style scoped>
  .float-field {
    position: relative;
    margin-bottom: var(--float-margin-bottom, 16px);
    /* 圆角、缺口起点、标签配色可由使用方按自己的输入框规格覆盖 */
    --float-radius: 4px;
    --float-notch-left: 12px;
    --float-border-color: rgba(148, 163, 184, 0.35);
    --float-label-color: var(--maxGreyFont);
    --float-label-active-color: var(--fontColor);
    --float-accent-color: var(--floatAccent);
    --float-label-size: 0.9rem;
    /* 缺口高度只需盖住 1px 上边框，再留 2px 余量 */
    --float-notch-height: 3px;
    /* 底色挖空深度：标签盒下沿在 field 顶边下约 7.4px（0.9rem/1.2 行高缩放 0.8 后），
       取 8px 刚好盖过字形最低点，又不至于在白底胶囊里露出一条多余的切口 */
    --float-notch-depth: 8px;
    /* 缺口比标签盒左右各多 2px，先于字形张开 */
    --float-notch-start: calc(var(--float-notch-left) - 2px);
  }

  /* 自绘边框：替换 el-input 自带的 inset 阴影，才能在上边框真正开缺口 */
  .float-border {
    position: absolute;
    inset: 0;
    border: 1px solid var(--float-border-color);
    border-radius: var(--float-radius);
    pointer-events: none;
    /* el-input 自带背景绘制在后续兄弟节点上，边框层必须抬高一层才可见 */
    z-index: 1;
    /* 静止态缺口宽度为 0：多边形点重合、等于没挖，
       和上浮态保持同样的点数，切换时缺口随标签一起张开/收合，不会有突跳 */
    clip-path: polygon(
      0 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) 0,
      100% 0,
      100% 100%,
      0 100%
    );
    transition: border-color 0.18s ease, clip-path 0.18s ease;
  }

  .float-field :deep(.el-input__wrapper),
  .float-field :deep(.el-input__wrapper.is-focus),
  .float-field :deep(.el-input__wrapper:hover) {
    box-shadow: none !important;
  }

  /* 上浮后：在上边框的标签区间挖空，标签背后完全透明（不是铺一块底色）。
     用 clip-path 而不是 mask-composite：mask 的多层合成在部分浏览器/内核里不生效，
     会退化成「整条边框不透明」，表现为标签飞上去但缺口没开。 */
  .float-field:focus-within .float-border,
  .float-field.is-filled .float-border {
    clip-path: polygon(
      0 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) var(--float-notch-height),
      calc(var(--float-notch-start) + var(--float-notch-width)) var(--float-notch-height),
      calc(var(--float-notch-start) + var(--float-notch-width)) 0,
      100% 0,
      100% 100%,
      0 100%
    );
  }

  /* 实心底输入框（白底胶囊 / 玻璃底）骑线时，底色的上沿会从标签字形中间横穿，
     表现为半截字踩在卡片色上、半截踩在输入框底色上的一道接缝。
     在与边框缺口完全相同的 x 区间里把底色也向下挖开，深度盖过标签盒底部与下伸部，
     标签整块落在卡片底色上，接缝消失。未开缺口时多边形宽度为 0，不改变静止观感。 */
  /* 直接子级 input：留言页这类用插槽塞进来的原生输入框（底色画在 input 自己身上）；
     el-input 的 .el-input__inner 不带底色，只裁外层 wrapper，避免多裁一层文字 */
  .float-field.has-fill > :deep(input),
  .float-field.has-fill :deep(.el-input__wrapper) {
    clip-path: polygon(
      0 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) 0,
      100% 0,
      100% 100%,
      0 100%
    );
    transition: clip-path 0.18s ease;
  }

  .float-field.has-fill:focus-within > :deep(input),
  .float-field.has-fill.is-filled > :deep(input),
  .float-field.has-fill:focus-within :deep(.el-input__wrapper),
  .float-field.has-fill.is-filled :deep(.el-input__wrapper) {
    clip-path: polygon(
      0 0,
      var(--float-notch-start) 0,
      var(--float-notch-start) var(--float-notch-depth),
      calc(var(--float-notch-start) + var(--float-notch-width)) var(--float-notch-depth),
      calc(var(--float-notch-start) + var(--float-notch-width)) 0,
      100% 0,
      100% 100%,
      0 100%
    );
  }

  .float-field:focus-within .float-border {
    border-color: var(--float-accent-color);
  }

  .float-label {
    position: absolute;
    left: var(--float-notch-left);
    top: 50%;
    transform: translateY(-50%);
    max-width: calc(100% - 56px);
    padding: 0 6px;
    font-size: var(--float-label-size);
    line-height: 1.2;
    color: var(--float-label-color);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    pointer-events: none;
    /* 输入框背景绘制在标签之上，需抬高标签层级 */
    z-index: 2;
    transform-origin: left center;
    background: transparent;
    transition: top 0.18s ease, transform 0.18s ease, color 0.18s ease;
  }

  /* 聚焦或有内容：标签骑在上边框线上，字号缩小，颜色随主题。
     line-height 为 1.2（盒高 1.2em，缩放后 0.96em），-50% 让缩放后的盒中心落在
     field 顶边，再 +0.5px 与 1px 边框线的中心重合，形成跨线居中的观感。 */
  .float-field:focus-within .float-label,
  .float-field.is-filled .float-label {
    top: 0;
    transform: translateY(calc(-50% + 0.5px)) scale(0.8);
    color: var(--float-label-active-color);
  }

  .float-field:focus-within .float-label {
    color: var(--float-accent-color);
  }

  @media (prefers-reduced-motion: reduce) {
    .float-label,
    .float-border {
      transition: none;
    }
  }
</style>
