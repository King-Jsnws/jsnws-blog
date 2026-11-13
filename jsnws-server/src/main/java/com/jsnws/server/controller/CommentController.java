package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.Article;
import com.jsnws.server.entity.Comment;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.mapper.ArticleMapper;
import com.jsnws.server.mapper.CommentMapper;
import com.jsnws.server.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论模块：前台楼层+子评论树、评论数、发表评论 + 后台管理（/admin/comment/**）
 */
@RestController
public class CommentController extends BaseController {

    private final CommentMapper commentMapper;
    private final SysUserMapper userMapper;
    private final ArticleMapper articleMapper;

    public CommentController(CommentMapper commentMapper, SysUserMapper userMapper, ArticleMapper articleMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
    }

    /** 评论总数 */
    @GetMapping("/comment/getCommentCount")
    public Result<Long> getCommentCount(@RequestParam("source") String source,
                                        @RequestParam("type") String type) {
        return Result.ok(commentMapper.selectCount(new QueryWrapper<Comment>()
                .eq("source", source).eq("comment_type", type)));
    }

    /**
     * 评论列表：
     * floorCommentId 为空 → 分页返回楼层主评论，每层带第一页子评论
     * floorCommentId 不为空 → 分页返回该楼层的子评论
     */
    @PostMapping("/comment/listComment")
    public Result<Page<Comment>> listComment(@RequestBody Map<String, Object> params) {
        long current = longVal(params.get("current"), 1L);
        long size = longVal(params.get("size"), 10L);
        String source = strVal(params.get("source"));
        String commentType = strVal(params.get("commentType"));
        Long floorCommentId = params.get("floorCommentId") == null ? null : longVal(params.get("floorCommentId"), null);

        Page<Comment> page = new Page<>(current, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("source", source).eq("comment_type", commentType);
        if (floorCommentId == null) {
            wrapper.isNull("floor_comment_id");
            wrapper.orderByDesc("create_time");
        } else {
            wrapper.eq("floor_comment_id", floorCommentId);
            wrapper.orderByAsc("create_time");
        }
        Page<Comment> result = commentMapper.selectPage(page, wrapper);
        fillParentUsername(result.getRecords());

        if (floorCommentId == null) {
            for (Comment floor : result.getRecords()) {
                Page<Comment> childPage = new Page<>(1, 10);
                QueryWrapper<Comment> childWrapper = new QueryWrapper<Comment>()
                        .eq("floor_comment_id", floor.getId()).orderByAsc("create_time");
                Page<Comment> children = commentMapper.selectPage(childPage, childWrapper);
                fillParentUsername(children.getRecords());
                floor.setChildComments(children.getRecords());
                floor.setChildTotal((int) children.getTotal());
                floor.setChildCurrent(1);
            }
        }
        return Result.ok(result);
    }

    /** 发表评论（楼层或回复） */
    @PostMapping("/comment/saveComment")
    public Result<Comment> saveComment(@RequestBody Map<String, Object> params) {
        SysUser user = currentUser(userMapper);
        String content = strVal(params.get("commentContent"));
        String source = strVal(params.get("source"));
        String commentType = strVal(params.get("commentType"));
        if (content == null || content.isEmpty() || source == null || commentType == null) {
            throw new ApiException("评论内容不完整！");
        }

        Comment comment = new Comment();
        comment.setUserId(user.getId());
        comment.setUsername(user.getUsername());
        comment.setAvatar(user.getAvatar());
        comment.setCommentContent(content);
        comment.setSource(source);
        comment.setCommentType(commentType);
        comment.setFloorCommentId(params.get("floorCommentId") == null ? null : longVal(params.get("floorCommentId"), null));
        comment.setParentCommentId(params.get("parentCommentId") == null ? null : longVal(params.get("parentCommentId"), null));
        comment.setParentUserId(params.get("parentUserId") == null ? null : longVal(params.get("parentUserId"), null));
        comment.setCreateTime(new Date());
        commentMapper.insert(comment);

        // 文章评论数维护
        if ("article".equals(commentType)) {
            Article article = articleMapper.selectById(parseId(source));
            if (article != null) {
                article.setCommentCount((article.getCommentCount() == null ? 0 : article.getCommentCount()) + 1);
                articleMapper.updateById(article);
            }
        }
        return Result.ok(comment);
    }

    // ==================== 后台管理 ====================

    @PostMapping("/admin/comment/boss/list")
    public Result<Page<Comment>> bossList(@RequestBody Map<String, Object> params) {
        return adminPage(params);
    }

    @PostMapping("/admin/comment/user/list")
    public Result<Page<Comment>> userList(@RequestBody Map<String, Object> params) {
        return adminPage(params);
    }

    @GetMapping("/admin/comment/boss/deleteComment")
    public Result<Void> bossDelete(@RequestParam("id") Long id) {
        return doDelete(id);
    }

    @GetMapping("/admin/comment/user/deleteComment")
    public Result<Void> userDelete(@RequestParam("id") Long id) {
        return doDelete(id);
    }

    private Result<Page<Comment>> adminPage(Map<String, Object> params) {
        Page<Comment> page = new Page<>(longVal(params.get("current"), 1L), longVal(params.get("size"), 10L));
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        if (params.get("source") != null && !String.valueOf(params.get("source")).isEmpty()) {
            wrapper.eq("source", strVal(params.get("source")));
        }
        if (params.get("commentType") != null && !String.valueOf(params.get("commentType")).isEmpty()) {
            wrapper.eq("comment_type", strVal(params.get("commentType")));
        }
        wrapper.orderByDesc("create_time");
        Page<Comment> result = commentMapper.selectPage(page, wrapper);
        return Result.ok(result);
    }

    /** 删除评论：楼层评论连同其全部子评论一并删除，并同步文章评论数 */
    private Result<Void> doDelete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ApiException("评论不存在！");
        }
        long deleted = 1;
        commentMapper.deleteById(id);
        if (comment.getFloorCommentId() == null) {
            List<Comment> children = commentMapper.selectList(
                    new QueryWrapper<Comment>().eq("floor_comment_id", id));
            if (!children.isEmpty()) {
                deleted += children.size();
                commentMapper.delete(new QueryWrapper<Comment>().eq("floor_comment_id", id));
            }
        }
        if ("article".equals(comment.getCommentType())) {
            Article article = articleMapper.selectById(parseId(comment.getSource()));
            if (article != null) {
                article.setCommentCount(Math.max(0,
                        (article.getCommentCount() == null ? 0 : article.getCommentCount()) - (int) deleted));
                articleMapper.updateById(article);
            }
        }
        return Result.ok();
    }

    /** 填充子评论展示用的 parentUsername（@回复人） */
    private void fillParentUsername(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        List<Long> parentUserIds = comments.stream()
                .filter(c -> c.getParentUserId() != null && !c.getParentUserId().equals(c.getUserId()))
                .map(Comment::getParentUserId).distinct().collect(Collectors.toList());
        if (parentUserIds.isEmpty()) {
            return;
        }
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(parentUserIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        for (Comment c : comments) {
            if (c.getParentUserId() != null && !c.getParentUserId().equals(c.getUserId())) {
                SysUser parent = userMap.get(c.getParentUserId());
                c.setParentUsername(parent == null ? "" : parent.getUsername());
            }
        }
    }

    private Long parseId(String source) {
        try {
            return Long.parseLong(source);
        } catch (Exception e) {
            return null;
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
}
