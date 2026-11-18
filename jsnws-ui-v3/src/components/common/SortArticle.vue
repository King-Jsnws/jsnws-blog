<template>
  <div v-if="!$common.isEmpty(articleList)" class="recent-post-container">
    <div class="recent-post-item"
         v-for="(article, index) in articleList"
         :key="index"
         @click="$router.push({path: `/article/${article.id}`})">
      <!-- 封面 -->
      <div class="recent-post-item-image">
        <el-image class="my-el-image"
                  v-once
                  lazy
                  :src="article.articleCover"
                  fit="cover">
          <template #error>
            <div class="image-slot myCenter" style="background-color: var(--lightGreen)">
              <div class="error-text">
                <div>遇事不决，可问春风</div>
              </div>
            </div>
          </template>
        </el-image>
        <div class="transformCenter hasVideo" v-if="article.hasVideo">
          <svg viewBox="0 0 1024 1024" width="54" height="54">
            <path
              d="M514 114.3c-219.9 0-398.9 178.9-398.9 398.9 0.1 219.9 179 398.8 398.9 398.8 219.9 0 398.8-178.9 398.8-398.8S733.9 114.3 514 114.3z m173 421.9L437.1 680.5c-17.7 10.2-39.8-2.6-39.8-23V368.9c0-20.4 22.1-33.2 39.8-23L687 490.2c17.7 10.2 17.7 35.8 0 46z"
              fill="#0C0C0C"></path>
          </svg>
        </div>
      </div>
      <!-- 内容 -->
      <div class="recent-post-item-post">
        <!-- 标题 -->
        <div class="post-title" :title="article.articleTitle">{{ article.articleTitle }}</div>
        <!-- 元信息：分类 + 标签 -->
        <div class="post-meta">
          <!-- 置顶徽标 -->
          <div class="pinned-btn" v-if="article.isTop">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
              <path d="m6.5 22l-1-1v-4H2v-2l1.5-2.65V10H2V8h9v2H9.5v2.35L11 15v2H7.5v4zm5.5-2v-2h8V6H2q0-.825.588-1.412T4 4h16q.825 0 1.413.588T22 6v12q0 .825-.587 1.413T20 20z"></path>
            </svg>
            <span>置顶</span>
          </div>
          <div class="meta-group" v-if="article.sort">
            <span class="meta-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                <path d="M6 15.325q.35-.175.725-.25T7.5 15H8V4h-.5q-.625 0-1.062.438T6 5.5zM10 15h8V4h-8zm-4 .325V4zM7.5 22q-1.45 0-2.475-1.025T4 18.5v-13q0-1.45 1.025-2.475T7.5 2H18q.825 0 1.413.587T20 4v12.525q0 .2-.162.363t-.588.362q-.35.175-.55.5t-.2.75t.2.763t.55.487t.55.413t.2.562v.25q0 .425-.288.725T19 22zm0-2h9.325q-.15-.35-.237-.712T16.5 18.5q0-.4.075-.775t.25-.725H7.5q-.65 0-1.075.438T6 18.5q0 .65.425 1.075T7.5 20"></path>
              </svg>
            </span>
            <span class="meta-link"
                  @click.stop="$router.push({path: '/archive', query: {category: article.sort && article.sort.sortName}})">{{ article.sort.sortName }}</span>
          </div>
          <div class="meta-group meta-tags" v-if="tagsOf(article).length > 0">
            <span class="meta-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                <path d="m9 16l-.825 3.275q-.075.325-.325.525t-.6.2q-.475 0-.775-.375T6.3 18.8L7 16H4.275q-.5 0-.8-.387T3.3 14.75q.075-.35.35-.55t.625-.2H7.5l1-4H5.775q-.5 0-.8-.387T4.8 8.75q.075-.35.35-.55t.625-.2H9l.825-3.275Q9.9 4.4 10.15 4.2t.6-.2q.475 0 .775.375t.175.825L11 8h4l.825-3.275q.075-.325.325-.525t.6-.2q.475 0 .775.375t.175.825L17 8h2.725q.5 0 .8.387t.175.863q-.075.35-.35.55t-.625.2H16.5l-1 4h2.725q.5 0 .8.388t.175.862q-.075.35-.35.55t-.625.2H15l-.825 3.275q-.075.325-.325.525t-.6.2q-.475 0-.775-.375T12.3 18.8L13 16zm.5-2h4l1-4h-4z"></path>
              </svg>
            </span>
            <span class="meta-tag-list">
              <span class="meta-tag-item" v-for="(lb, i) in tagsOf(article)" :key="i">
                <span class="meta-divider">/</span>
                <span class="meta-link"
                      @click.stop="$router.push({path: '/archive', query: {tag: lb.labelName}})">{{ lb.labelName }}</span>
              </span>
            </span>
          </div>
        </div>
        <!-- 摘要 -->
        <div class="recent-post-desc">{{ article.articleContent }}</div>
        <!-- 底部统计：日期 / 字数 / 阅读时长 -->
        <div class="post-stats">
          <div class="stat-item">
            <span class="stat-icon">
              <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor" aria-hidden="true">
                <path d="M5 22a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h1V3a1 1 0 0 1 2 0v1h8V3a1 1 0 0 1 2 0v1h1a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5zm0-2h14V10H5v10zm0-12h14V6H5v2z"></path>
              </svg>
            </span>
            <span class="stat-text">{{ article.createTime }}</span>
          </div>
          <span class="stat-divider">|</span>
          <div class="stat-item">
            <span class="stat-icon">
              <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor" aria-hidden="true">
                <path d="M4 18a1 1 0 1 1 0-2h10a1 1 0 1 1 0 2H4zm0-5a1 1 0 1 1 0-2h16a1 1 0 1 1 0 2H4zm0-5a1 1 0 1 1 0-2h16a1 1 0 1 1 0 2H4z"></path>
              </svg>
            </span>
            <span class="stat-text">{{ wordCount(article) }} 字</span>
          </div>
          <span class="stat-divider">|</span>
          <div class="stat-item">
            <span class="stat-icon">
              <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor" aria-hidden="true">
                <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 10.6V7a1 1 0 0 0-2 0v6a1 1 0 0 0 .3.7l3.3 3.3a1 1 0 0 0 1.4-1.4L13 12.6z"></path>
              </svg>
            </span>
            <span class="stat-text">{{ readMinutes(article) }} 分钟</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      articleList: {
        type: Array
      }
    },
    methods: {
      /** 文章标签列表：后端当前一篇文章一个标签，兼容将来多标签返回 */
      tagsOf(article) {
        if (Array.isArray(article.labels) && article.labels.length > 0) {
          return article.labels;
        }
        return article.label ? [article.label] : [];
      },
      /** 粗略统计正文字数（去掉 markdown 符号与空白） */
      wordCount(article) {
        const text = (article.articleContent || '')
          .replace(/```[\s\S]*?```/g, '')
          .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
          .replace(/\[[^\]]*\]\([^)]*\)/g, '')
          .replace(/[#>*`\-_~|]/g, '')
          .replace(/\s+/g, '');
        return text.length;
      },
      /** 按每分钟 400 字估算阅读时长，最少 1 分钟 */
      readMinutes(article) {
        return Math.max(1, Math.ceil(this.wordCount(article) / 400));
      }
    }
  }
</script>

<style scoped>

  .recent-post-container {
    display: flex;
    flex-wrap: wrap;
    margin: 0 -10px;
  }

  .recent-post-item {
    display: flex;
    flex-direction: column;
    width: calc(100% / 3 - 20px);
    margin: 10px;
    border-radius: 12px;
    overflow: hidden;
    background: var(--background);
    border: 1px solid var(--maxLightGray);
    cursor: pointer;
    user-select: none;
    transition: transform 0.3s, box-shadow 0.3s;
  }

  .recent-post-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 26px rgba(0, 0, 0, 0.1);
  }

  .recent-post-item-image {
    width: 100%;
    aspect-ratio: 3 / 2;
    position: relative;
    overflow: hidden;
  }

  .recent-post-item-image :deep(.el-image) {
    width: 100%;
    height: 100%;
    display: block;
  }

  .recent-post-item-image :deep(.el-image__inner) {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 1s;
  }

  .recent-post-item-image :deep(.el-image__inner):hover {
    transform: scale(1.08);
  }

  .recent-post-item-post {
    display: flex;
    flex-direction: column;
    flex: 1;
    padding: 14px 16px 16px;
  }

  /* 标题：左侧主题色竖条 */
  .post-title {
    position: relative;
    padding-left: 12px;
    font-size: 17px;
    font-weight: 700;
    line-height: 1.5;
    color: var(--fontColor);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .post-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 4px;
    width: 4px;
    height: 17px;
    border-radius: 4px;
    background: var(--themeBackground);
  }

  /* 元信息：分类 + 标签（参考站样式：图标 + 灰色链接，标签间用 / 分隔） */
  .post-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 6px 16px;
    margin: 10px 0;
    font-size: 14px;
    color: var(--greyFont);
  }

  /* 置顶徽标：与分类/标签图标方块同高 */
  .pinned-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 700;
    line-height: 20px;
    background: var(--maxLightGray);
    color: var(--themeBackgroundDeep);
  }

  .meta-group {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
  }

  .meta-tag-list {
    display: inline-flex;
    align-items: center;
    flex-wrap: wrap;
    min-width: 0;
  }

  .meta-tag-item {
    display: inline-flex;
    align-items: center;
    min-width: 0;
  }

  .meta-icon {
    width: 28px;
    height: 28px;
    flex: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 7px;
    background: var(--maxLightGray);
    color: var(--themeBackgroundDeep);
  }

  .meta-link {
    font-weight: 500;
    white-space: nowrap;
    cursor: pointer;
    transition: color 0.3s;
  }

  .meta-link:hover {
    color: var(--themeBackground);
  }

  .meta-divider {
    color: var(--lightGray);
    margin: 0 5px;
  }

  .recent-post-desc {
    font-size: 13px;
    line-height: 1.6;
    color: var(--greyFontColor);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    flex: 1;
  }

  /* 底部统计：图标方块 + 文字，竖线分隔 */
  .post-stats {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    margin-top: 14px;
    font-size: 12px;
    color: var(--greyFontColor);
  }

  .stat-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .stat-icon {
    width: 20px;
    height: 20px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    background: var(--maxLightGray);
    color: var(--greyFont);
  }

  .stat-divider {
    color: var(--lightGray);
  }

  .error-text {
    font-size: 20px;
    line-height: 1.8;
    letter-spacing: 4px;
    color: var(--white);
  }

  .hasVideo {
    padding: 0 20px;
    background: var(--maxMaxWhiteMask);
    border-radius: 15px;
  }

  @media screen and (max-width: 1400px) {
    .recent-post-item {
      width: calc(100% / 2 - 20px);
    }
  }

  @media screen and (max-width: 600px) {
    .recent-post-item {
      width: calc(100% - 20px);
    }
  }

</style>
