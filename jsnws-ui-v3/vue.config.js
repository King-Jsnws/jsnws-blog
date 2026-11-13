const CompressionPlugin = require('compression-webpack-plugin')

// Vue CLI 4.5 构建配置
module.exports = {
  // Element Plus 与 Vue Router 4.5 的发布包内含可选链等较新语法，
  // 需要让 Babel 一并转译，否则 webpack 4 的解析器会报错
  transpileDependencies: [
    /element-plus/,
    'vue-router',
    'vue3-seamless-scroll',
    '@videojs-player/vue',
    'md-editor-v3',
    // md-editor-v3 依赖链里的包也用了 ?? 等新语法
    '@lucide/vue',
    /@vavt/
  ],
  lintOnSave: false,
  productionSourceMap: false,
  chainWebpack: config => {
    // md-editor-v3 的 style.css 用 `@import '@vavt/markdown-theme/...'` 引用主题，
    // 而 webpack 4 配套的 css-loader 会把不以 ~ 开头的 @import 当作相对路径，
    // 这里用 alias 把相对请求重定向到真实的 node_modules 包
    config.resolve.alias.set(
      './@vavt/markdown-theme',
      require('path').resolve(__dirname, 'node_modules/@vavt/markdown-theme')
    )
    // md-editor-v3 内置的 markdown-it 走 CJS，而 linkify-it 5 的 module 字段是 ESM，
    // webpack 4 优先取 module 字段后 require 互操作会拿到非构造函数，这里强制用 CJS 产物
    config.resolve.alias.set(
      'linkify-it',
      require.resolve('linkify-it', {
        paths: [require('path').resolve(__dirname, 'node_modules/md-editor-v3')]
      })
    )

    // CodeMirror 6 / Lezer 的包同时带 ESM(module) 与 CJS(main) 两份产物，
    // webpack 4 会在不同引用方式下各打包一份，导致 instanceof 校验失败、
    // 编辑器无法初始化。这里统一强制指向 CJS 产物，保证只有一个实例。
    const fs = require('fs')
    const path = require('path')
    const nodeModules = path.resolve(__dirname, 'node_modules')
    ;['@codemirror', '@lezer'].forEach(scope => {
      const scopeDir = path.join(nodeModules, scope)
      if (!fs.existsSync(scopeDir)) {
        return
      }
      fs.readdirSync(scopeDir).forEach(name => {
        const pkgFile = path.join(scopeDir, name, 'package.json')
        if (!fs.existsSync(pkgFile)) {
          return
        }
        const pkg = JSON.parse(fs.readFileSync(pkgFile, 'utf8'))
        const mainFile = pkg.main && path.resolve(scopeDir, name, pkg.main)
        if (mainFile && fs.existsSync(mainFile)) {
          config.resolve.alias.set(`${scope}/${name}`, mainFile)
        }
      })
    })
  },
  devServer: {
    // 与旧项目（80 端口）错开，便于同时启动对照
    port: 8080,
    https: false,
    open: false
  },
  configureWebpack: {
    plugins: [
      // 打包时额外生成 gzip 文件，沿用旧项目的产物策略
      new CompressionPlugin({
        algorithm: 'gzip',
        test: /\.js$|\.html$|\.css$/,
        filename: '[path].gz[query]',
        minRatio: 1,
        threshold: 10240,
        deleteOriginalAssets: false
      })
    ]
  }
}
