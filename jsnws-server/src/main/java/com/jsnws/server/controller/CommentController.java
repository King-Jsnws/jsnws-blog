package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.AnonymousAvatars;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.IpLocationUtil;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.UserAgentUtil;
import com.jsnws.server.common.UserRole;
import com.jsnws.server.entity.Article;
import com.jsnws.server.entity.Comment;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.mapper.ArticleMapper;
import com.jsnws.server.mapper.CommentMapper;
import com.jsnws.server.mapper.SysUserMapper;
import com.jsnws.server.mapper.WebInfoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
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
    private final WebInfoMapper webInfoMapper;
    private final IpLocationUtil ipLocationUtil;
    private final ObjectMapper objectMapper;

    /** 未登录匿名留言的轻量频率限制：同一 IP 10 秒内只允许 1 条 */
    private static final long ANONYMOUS_INTERVAL_MS = 10_000L;
    private static final Map<String, Long> ANONYMOUS_LAST_POST = new java.util.concurrent.ConcurrentHashMap<>();

    public CommentController(CommentMapper commentMapper, SysUserMapper userMapper, ArticleMapper articleMapper,
                             WebInfoMapper webInfoMapper, IpLocationUtil ipLocationUtil, ObjectMapper objectMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.webInfoMapper = webInfoMapper;
        this.ipLocationUtil = ipLocationUtil;
        this.objectMapper = objectMapper;
    }

    /** 匿名头像优先取后台「随机头像」配置，未配置时回落到本地匿名头像素材 */
    private String randomAnonymousAvatar() {
        String json = null;
        WebInfo webInfo = webInfoMapper.selectById(1L);
        if (webInfo != null) {
            json = webInfo.getRandomAvatar();
        }
        try {
            if (json != null && !json.isEmpty()) {
                List<String> list = objectMapper.readValue(json,
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                if (!list.isEmpty()) {
                    return list.get(new java.util.Random().nextInt(list.size()));
                }
            }
        } catch (Exception ignored) {
        }
        return AnonymousAvatars.random();
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
        String sortType = strVal(params.get("sortType"));
        Long floorCommentId = params.get("floorCommentId") == null ? null : longVal(params.get("floorCommentId"), null);

        Page<Comment> page = new Page<>(current, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("source", source).eq("comment_type", commentType);
        if (floorCommentId == null) {
            wrapper.isNull("floor_comment_id");
            wrapper.orderByDesc("is_top");
            // 排序方式：time_asc=正序(旧→新) / time_desc=倒序(新→旧，默认) / hot=热度(点赞数多→少)
            if ("time_asc".equals(sortType)) {
                wrapper.orderByAsc("create_time");
            } else if ("hot".equals(sortType)) {
                wrapper.orderByDesc("like_count");
                wrapper.orderByDesc("create_time");
            } else {
                wrapper.orderByDesc("create_time");
            }
        } else {
            wrapper.eq("floor_comment_id", floorCommentId);
            wrapper.orderByAsc("create_time");
        }
        Page<Comment> result = commentMapper.selectPage(page, wrapper);
        fillParentUsername(result.getRecords());
        fillNickname(result.getRecords());

        if (floorCommentId == null) {
            for (Comment floor : result.getRecords()) {
                Page<Comment> childPage = new Page<>(1, 10);
                QueryWrapper<Comment> childWrapper = new QueryWrapper<Comment>()
                        .eq("floor_comment_id", floor.getId()).orderByAsc("create_time");
                Page<Comment> children = commentMapper.selectPage(childPage, childWrapper);
                fillParentUsername(children.getRecords());
                fillNickname(children.getRecords());
                floor.setChildComments(children.getRecords());
                floor.setChildTotal((int) children.getTotal());
                floor.setChildCurrent(1);
            }
        }
        return Result.ok(result);
    }

    /** 点赞评论：like_count +1；未登录也可点赞 */
    @PostMapping("/comment/likeComment")
    public Result<Integer> likeComment(@RequestBody Map<String, Object> params) {
        Long commentId = params.get("commentId") == null ? null : longVal(params.get("commentId"), null);
        if (commentId == null) {
            throw new ApiException("评论不存在！");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new ApiException("评论不存在！");
        }
        int likeCount = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
        likeCount++;
        comment.setLikeCount(likeCount);
        commentMapper.updateById(comment);
        return Result.ok(likeCount);
    }

    /** 发表评论（楼层或回复） */
    @PostMapping("/comment/saveComment")
    public Result<Comment> saveComment(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = currentUserId();
        SysUser user = userId == null ? null : userMapper.selectById(userId);
        String content = strVal(params.get("commentContent"));
        String source = strVal(params.get("source"));
        String commentType = strVal(params.get("commentType"));
        if (content == null || content.isEmpty() || source == null || commentType == null) {
            throw new ApiException("评论内容不完整！");
        }

        // 留言页主留言：允许无 token / token 失效直接提交，服务端按匿名规则落库；
        // 文章评论、回复（floorCommentId 非空）仍保持原有登录校验与 300 语义
        boolean messageType = "message".equals(commentType);
        boolean anonymous = messageType && params.get("floorCommentId") == null && user == null;
        if (user == null && !anonymous) {
            return Result.tokenInvalid("登录已失效");
        }
        if (messageType && content.length() > 300) {
            throw new ApiException("留言内容不能超过300字！");
        }

        String ip = clientIp(request);
        if (anonymous) {
            checkAnonymousRate(ip);
        }

        // 文章评论：校验目标文章存在且允许评论，避免给不可见/关闭评论的文章刷评论
        Article targetArticle = null;
        if ("article".equals(commentType)) {
            targetArticle = articleMapper.selectById(parseId(source));
            if (targetArticle == null) {
                throw new ApiException("文章不存在！");
            }
            if (Boolean.FALSE.equals(targetArticle.getViewStatus())) {
                throw new ApiException("文章不可见，无法评论！");
            }
            if (Boolean.FALSE.equals(targetArticle.getCommentStatus())) {
                throw new ApiException("该文章已关闭评论！");
            }
        }

        Comment comment = new Comment();
        comment.setUserId(user == null ? 0L : user.getId());
        comment.setUsername(user == null ? "匿名网友" : user.getUsername());
        comment.setAvatar(user == null ? null : user.getAvatar());
        comment.setIsAnonymous(0);
        if (anonymous) {
            comment.setUsername("匿名网友");
            comment.setAvatar(null);
            comment.setIsAnonymous(1);
            comment.setAnonymousAvatar(randomAnonymousAvatar());
        }
        comment.setCommentContent(content);
        comment.setSource(source);
        comment.setCommentType(commentType);
        comment.setFloorCommentId(params.get("floorCommentId") == null ? null : longVal(params.get("floorCommentId"), null));
        Long parentCommentId = params.get("parentCommentId") == null ? null : longVal(params.get("parentCommentId"), null);
        comment.setParentCommentId(parentCommentId);
        if (parentCommentId != null) {
            // 被回复人由服务端根据父评论推导，忽略客户端传入的 parentUserId
            Comment parent = commentMapper.selectById(parentCommentId);
            if (parent == null) {
                throw new ApiException("回复的评论不存在！");
            }
            comment.setParentUserId(parent.getUserId());
        } else {
            comment.setParentUserId(params.get("parentUserId") == null ? null : longVal(params.get("parentUserId"), null));
        }
        comment.setIp(ip);
        comment.setLocation(ipLocationUtil.resolve(ip));
        String userAgent = request.getHeader("User-Agent");
        comment.setBrowser(resolveBrowser(userAgent));
        comment.setOs(resolveOs(userAgent));
        comment.setCreateTime(new Date());
        comment.setNickname(user == null ? null : user.getNickname());
        commentMapper.insert(comment);

        // 文章评论数维护
        if ("article".equals(commentType) && targetArticle != null) {
            Article article = targetArticle;
            article.setCommentCount((article.getCommentCount() == null ? 0 : article.getCommentCount()) + 1);
            articleMapper.updateById(article);
        }
        return Result.ok(comment);
    }

    /**
     * 未登录匿名留言的轻量防刷：同一 IP 10 秒内只允许提交 1 条。
     * 仅用内存 Map 记录最近提交时间，无新增组件；超过 2000 条时顺带清理 10 分钟前的记录。
     */
    private void checkAnonymousRate(String ip) {
        long now = System.currentTimeMillis();
        Long last = ANONYMOUS_LAST_POST.get(ip);
        if (last != null && now - last < ANONYMOUS_INTERVAL_MS) {
            throw new ApiException("操作太频繁，请稍后再试！");
        }
        ANONYMOUS_LAST_POST.put(ip, now);
        if (ANONYMOUS_LAST_POST.size() > 2000) {
            ANONYMOUS_LAST_POST.entrySet().removeIf(entry -> now - entry.getValue() > 600000L);
        }
    }

    /** 置顶 / 取消置顶留言：仅站长可操作 */
    @PostMapping("/comment/topComment")
    public Result<Void> topComment(@RequestBody Map<String, Object> params) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new ApiException("登录已失效");
        }
        if (currentRole() != UserRole.BOSS) {
            throw new ApiException("权限不足，仅站长可置顶留言");
        }
        Long id = longVal(params.get("id"), null);
        Object topValue = params.get("top");
        if (id == null || topValue == null) {
            throw new ApiException("参数不完整！");
        }
        boolean top = Boolean.parseBoolean(String.valueOf(topValue));
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ApiException("留言不存在！");
        }
        if (comment.getFloorCommentId() != null) {
            throw new ApiException("仅支持置顶主留言！");
        }
        UpdateWrapper<Comment> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        if (top) {
            wrapper.set("is_top", 1).set("top_time", new Date());
        } else {
            wrapper.set("is_top", 0).set("top_time", null);
        }
        commentMapper.update(null, wrapper);
        return Result.ok();
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
        // 后台列表额外提供 IP 字段（实体中 ip 带 @JsonIgnore，避免前台接口泄露）
        result.getRecords().forEach(item -> item.setIpAddress(item.getIp()));
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
                c.setParentUsername(parent == null ? ""
                        : (parent.getNickname() != null && !parent.getNickname().isEmpty()
                                ? parent.getNickname() : parent.getUsername()));
            }
        }
    }

    /** 填充评论者昵称：从 sys_user 取 nickname，缺失时不填（前端回落 username） */
    private void fillNickname(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        List<Long> userIds = comments.stream()
                .filter(c -> c.getUserId() != null && c.getUserId() > 0)
                .map(Comment::getUserId).distinct().collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        for (Comment c : comments) {
            SysUser u = userMap.get(c.getUserId());
            if (u != null) {
                c.setNickname(u.getNickname());
                c.setUserType(UserRole.normalize(u.getUserType()));
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

    /** 从 User-Agent 解析浏览器名称+版本，格式与参考站一致（如 Chrome151.0） */
    private String resolveBrowser(String ua) {
        return UserAgentUtil.resolveBrowser(ua);
    }

    /** 从 User-Agent 解析操作系统名称+版本（如 Windows 10 / macOS 10.15.7 / Android 13 / iOS 17.1） */
    private String resolveOs(String ua) {
        return UserAgentUtil.resolveOs(ua);
    }

    /** 取客户端真实 IP：X-Forwarded-For 首段 > X-Real-IP > remoteAddr */
    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            return index > 0 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
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
