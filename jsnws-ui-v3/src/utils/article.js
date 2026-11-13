/**
 * 公开文章接口：分页拉全量文章
 * 归档、图谱页面都需要完整文章列表，这里统一封装，
 * 逐页请求直到达到 total 或没有更多数据。
 */
export function fetchAllArticles(http, constant, options = {}) {
  const size = options.size || 100
  const maxPage = options.maxPage || 20
  const articles = []

  const loadPage = (current) => {
    return http.post(constant.baseURL + '/article/listArticle', {
      current,
      size,
      total: 0,
      searchKey: '',
      sortId: null,
      labelId: null
    }).then((res) => {
      const data = res && res.data
      if (!data || !Array.isArray(data.records) || data.records.length === 0) {
        return articles
      }
      articles.push(...data.records)
      const total = Number(data.total) || 0
      if (articles.length < total && current < maxPage) {
        return loadPage(current + 1)
      }
      return articles
    })
  }

  return loadPage(1)
}
