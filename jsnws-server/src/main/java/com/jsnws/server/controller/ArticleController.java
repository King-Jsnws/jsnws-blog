package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.UserRole;
import com.jsnws.server.entity.Article;
import com.jsnws.server.entity.Comment;
import com.jsnws.server.entity.Label;
import com.jsnws.server.entity.Sort;
import com.jsnws.server.mapper.ArticleMapper;
import com.jsnws.server.mapper.CommentMapper;
import com.jsnws.server.mapper.LabelMapper;
import com.jsnws.server.mapper.SortMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章模块：前台列表/详情 + 后台管理（/admin/article/**）
 */
@RestController
public class ArticleController extends BaseController {

    private final ArticleMapper articleMapper;
    private final SortMapper sortMapper;
    private final LabelMapper labelMapper;
    private final CommentMapper commentMapper;

    public ArticleController(ArticleMapper articleMapper, SortMapper sortMapper,
                             LabelMapper labelMapper, CommentMapper commentMapper) {
        this.articleMapper = articleMapper;
        this.sortMapper = sortMapper;
        this.labelMapper = labelMapper;
        this.commentMapper = commentMapper;
    }

    /** 前台文章分页列表（仅可见文章） */
    @PostMapping("/article/listArticle")
    public Result<Page<Article>> listArticle(@RequestBody Map<String, Object> params) {
        Page<Article> page = new Page<>(longVal(params.get("current"), 1L), longVal(params.get("size"), 10L));
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("view_status", true);
        String searchKey = strVal(params.get("searchKey"));
        if (searchKey != null && !searchKey.isEmpty()) {
            wrapper.like("article_title", searchKey);
        }
        if (params.get("sortId") != null) {
            wrapper.eq("sort_id", longVal(params.get("sortId"), null));
        }
        if (params.get("labelId") != null) {
            wrapper.eq("label_id", longVal(params.get("labelId"), null));
        }
        if (params.get("recommendStatus") != null) {
            wrapper.eq("recommend_status", boolVal(params.get("recommendStatus")));
        }
        if (params.get("userId") != null) {
            wrapper.eq("user_id", longVal(params.get("userId"), null));
        }
        // 置顶文章排在最前，其余按发布时间倒序
        wrapper.orderByDesc("is_top").orderByDesc("create_time");
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        fillSortAndLabel(result.getRecords());
        stripPasswords(result.getRecords());
        return Result.ok(result);
    }

    /** 首页分类文章：{sortId: [文章...]}，每个分类取最近 6 篇 */
    @GetMapping("/article/listSortArticle")
    public Result<Map<String, List<Article>>> listSortArticle() {
        Map<String, List<Article>> data = new HashMap<>();
        List<Sort> sorts = sortMapper.selectList(new QueryWrapper<Sort>().orderByAsc("priority"));
        List<Long> sortIds = sorts.stream().map(Sort::getId).collect(Collectors.toList());
        Map<Long, List<Article>> bySort = new HashMap<>();
        if (!sortIds.isEmpty()) {
            List<Article> articles = articleMapper.selectList(new QueryWrapper<Article>()
                    .eq("view_status", true)
                    .in("sort_id", sortIds)
                    .orderByDesc("is_top")
                    .orderByDesc("create_time"));
            bySort = articles.stream().collect(Collectors.groupingBy(Article::getSortId));
        }
        for (Sort sort : sorts) {
            List<Article> list = bySort.getOrDefault(sort.getId(),
                    new java.util.ArrayList<>()).stream().limit(6).collect(Collectors.toList());
            fillSortAndLabel(list);
            stripPasswords(list);
            data.put(String.valueOf(sort.getId()), list);
        }
        return Result.ok(data);
    }

    /** 文章详情（不可见文章必须校验密码），成功后浏览量 +1 */
    @GetMapping("/article/getArticleById")
    public Result<Article> getArticleById(@RequestParam("id") Long id,
                                          @RequestParam(value = "password", required = false) String password) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ApiException("文章不存在！");
        }
        if (Boolean.FALSE.equals(article.getViewStatus())) {
            // 不可见文章必须提供正确密码，未授权一律不返回
            if (article.getPassword() == null || article.getPassword().isEmpty()
                    || password == null || !article.getPassword().equals(password)) {
                throw new ApiException("文章不可见，请输入访问密码！");
            }
        } else if (article.getPassword() != null && !article.getPassword().isEmpty()) {
            if (password == null || !article.getPassword().equals(password)) {
                throw new ApiException("密码错误" + (article.getTips() == null ? "" : article.getTips()));
            }
        }
        article.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
        articleMapper.updateById(article);
        fillSortAndLabel(java.util.Collections.singletonList(article));
        article.setPassword(null);
        return Result.ok(article);
    }

    /** 新增文章（主理人 / 管理员 / AI 站长 / 作者） */
    @PostMapping("/article/saveArticle")
    public Result<Void> saveArticle(@RequestBody Article article) {
        int role = currentRole();
        if (role > UserRole.ADMIN && role != UserRole.AI_OWNER) {
            // 作者及以下不能自设推荐/可见/评论状态
            article.setRecommendStatus(false);
            article.setViewStatus(true);
            article.setCommentStatus(true);
            article.setIsTop(false);
            article.setPassword(null);
            article.setTips(null);
        }
        checkPasswordRule(article);
        article.setId(null);
        Long userId = currentUserId();
        article.setUserId(userId);
        if (article.getViewCount() == null) article.setViewCount(0);
        if (article.getLikeCount() == null) article.setLikeCount(0);
        if (article.getCommentCount() == null) article.setCommentCount(0);
        if (article.getIsTop() == null) article.setIsTop(false);
        if (article.getHasVideo() == null) article.setHasVideo(article.getVideoUrl() != null && !article.getVideoUrl().isEmpty());
        article.setCreateTime(new Date());
        articleMapper.insert(article);
        return Result.ok();
    }

    /** 更新文章（主理人可改任意文章，作者只能改自己的） */
    @PostMapping("/article/updateArticle")
    public Result<Void> updateArticle(@RequestBody Article article) {
        Article exist = article.getId() == null ? null : articleMapper.selectById(article.getId());
        if (exist == null) {
            throw new ApiException("文章不存在！");
        }
        Long userId = currentUserId();
        if (userId == null) {
            throw new ApiException("登录已失效");
        }
        boolean isBoss = currentRole() == UserRole.BOSS;
        // AI 站长是站点级内容编辑，可修改任意文章
        boolean canEditAll = isBoss || currentRole() == UserRole.AI_OWNER;
        if (!canEditAll && !userId.equals(exist.getUserId())) {
            throw new ApiException("只能修改自己发布的文章！");
        }
        if (!isBoss) {
            // 作者及以下只能改内容字段，状态字段保持数据库原值，避免越权上推荐/改可见性
            article.setRecommendStatus(exist.getRecommendStatus());
            article.setViewStatus(exist.getViewStatus());
            article.setCommentStatus(exist.getCommentStatus());
            article.setIsTop(exist.getIsTop());
        } else {
            checkPasswordRule(article);
        }
        // 作者不能通过请求体把文章改挂到别人名下
        article.setUserId(exist.getUserId());
        articleMapper.updateById(article);
        return Result.ok();
    }

    /** 删除文章（管理员，同时清理评论） */
    @GetMapping("/article/deleteArticle")
    public Result<Void> deleteArticle(@RequestParam("id") Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ApiException("文章不存在！");
        }
        articleMapper.deleteById(id);
        List<Comment> comments = commentMapper.selectList(
                new QueryWrapper<Comment>().eq("source", String.valueOf(id)).eq("comment_type", "article"));
        if (!comments.isEmpty()) {
            commentMapper.delete(new QueryWrapper<Comment>()
                    .eq("source", String.valueOf(id)).eq("comment_type", "article"));
        }
        return Result.ok();
    }

    // ==================== 后台管理 ====================

    /** 全部文章分页（管理员-站长） */
    @PostMapping("/admin/article/boss/list")
    public Result<Page<Article>> bossList(@RequestBody Map<String, Object> params) {
        return adminPage(params, null);
    }

    /** 当前管理员自己的文章分页（管理员-普通） */
    @PostMapping("/admin/article/user/list")
    public Result<Page<Article>> userList(@RequestBody Map<String, Object> params) {
        return adminPage(params, currentUserId());
    }

    /** 后台获取文章（主理人可看任意，作者只能看自己的） */
    @GetMapping("/admin/article/getArticleById")
    public Result<Article> adminGetArticle(@RequestParam("id") Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ApiException("文章不存在！");
        }
        Long userId = currentUserId();
        int role = currentRole();
        if (userId != null && role != UserRole.BOSS && role != UserRole.AI_OWNER
                && !userId.equals(article.getUserId())) {
            throw new ApiException("只能查看自己发布的文章！");
        }
        fillSortAndLabel(java.util.Collections.singletonList(article));
        return Result.ok(article);
    }

    /** 切换文章状态（可见/可评论/推荐/置顶） */
    @GetMapping("/admin/article/changeArticleStatus")
    public Result<Void> changeArticleStatus(@RequestParam("articleId") Long articleId,
                                            @RequestParam(value = "viewStatus", required = false) Boolean viewStatus,
                                            @RequestParam(value = "commentStatus", required = false) Boolean commentStatus,
                                            @RequestParam(value = "recommendStatus", required = false) Boolean recommendStatus,
                                            @RequestParam(value = "isTop", required = false) Boolean isTop) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ApiException("文章不存在！");
        }
        if (Boolean.FALSE.equals(viewStatus)
                && (article.getPassword() == null || article.getPassword().isEmpty())) {
            throw new ApiException("设为不可见前请先在编辑页设置访问密码！");
        }
        if (viewStatus != null) article.setViewStatus(viewStatus);
        if (commentStatus != null) article.setCommentStatus(commentStatus);
        if (recommendStatus != null) article.setRecommendStatus(recommendStatus);
        if (isTop != null) article.setIsTop(isTop);
        articleMapper.updateById(article);
        return Result.ok();
    }

    private Result<Page<Article>> adminPage(Map<String, Object> params, Long userId) {
        Page<Article> page = new Page<>(longVal(params.get("current"), 1L), longVal(params.get("size"), 10L));
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        String searchKey = strVal(params.get("searchKey"));
        if (searchKey != null && !searchKey.isEmpty()) {
            wrapper.like("article_title", searchKey);
        }
        if (params.get("sortId") != null) {
            wrapper.eq("sort_id", longVal(params.get("sortId"), null));
        }
        if (params.get("labelId") != null) {
            wrapper.eq("label_id", longVal(params.get("labelId"), null));
        }
        if (params.get("recommendStatus") != null) {
            wrapper.eq("recommend_status", boolVal(params.get("recommendStatus")));
        }
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        wrapper.orderByDesc("create_time");
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        fillSortAndLabel(result.getRecords());
        return Result.ok(result);
    }

    private void checkPasswordRule(Article article) {
        if (Boolean.FALSE.equals(article.getViewStatus())
                && (article.getPassword() == null || article.getPassword().isEmpty())) {
            throw new ApiException("文章不可见时必须输入密码！");
        }
    }

    /** 公开响应中剔除文章访问密码 */
    private void stripPasswords(List<Article> articles) {
        if (articles == null) {
            return;
        }
        for (Article article : articles) {
            article.setPassword(null);
        }
    }

    /** 为文章列表填充关联的 sort/label 对象 */
    private void fillSortAndLabel(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        List<Long> sortIds = articles.stream().map(Article::getSortId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> labelIds = articles.stream().map(Article::getLabelId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Sort> sortMap = sortIds.isEmpty() ? new HashMap<>()
                : sortMapper.selectBatchIds(sortIds).stream().collect(Collectors.toMap(Sort::getId, Function.identity()));
        Map<Long, Label> labelMap = labelIds.isEmpty() ? new HashMap<>()
                : labelMapper.selectBatchIds(labelIds).stream().collect(Collectors.toMap(Label::getId, Function.identity()));
        for (Article article : articles) {
            article.setSort(sortMap.get(article.getSortId()));
            article.setLabel(labelMap.get(article.getLabelId()));
        }
    }

    private String strVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long longVal(Object value, Long def) {
        if (value == null) return def;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }

    private Boolean boolVal(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
