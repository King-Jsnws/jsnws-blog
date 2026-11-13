module.exports = {
  root: true,
  env: {
    node: true,
    browser: true
  },
  extends: [
    'plugin:vue/vue3-essential'
  ],
  parserOptions: {
    ecmaVersion: 2020,
    sourceType: 'module'
  },
  globals: {
    // 由 public/index.html 中的 CDN 脚本提供，沿用旧项目的全局依赖
    $: 'readonly',
    jQuery: 'readonly',
    anime: 'readonly',
    hljs: 'readonly',
    WOW: 'readonly',
    ClipboardJS: 'readonly',
    qiniu: 'readonly',
    tocbot: 'readonly'
  },
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off'
  }
}
