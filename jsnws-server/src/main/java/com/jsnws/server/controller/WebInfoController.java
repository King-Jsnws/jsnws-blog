package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.AnonymousAvatars;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.IpLocationUtil;
import com.jsnws.server.common.MaintenanceService;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.ServerStatusService;
import com.jsnws.server.common.TokenUtil;
import com.jsnws.server.common.UserAgentUtil;
import com.jsnws.server.common.UserRole;
import com.jsnws.server.entity.Article;
import com.jsnws.server.entity.Comment;
import com.jsnws.server.entity.Friend;
import com.jsnws.server.entity.History;
import com.jsnws.server.entity.Label;
import com.jsnws.server.entity.ResourcePath;
import com.jsnws.server.entity.Sort;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.entity.TreeHole;
import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.job.FriendMonitorJob;
import com.jsnws.server.mapper.ArticleMapper;
import com.jsnws.server.mapper.CommentMapper;
import com.jsnws.server.mapper.FriendMapper;
import com.jsnws.server.mapper.HistoryMapper;
import com.jsnws.server.mapper.LabelMapper;
import com.jsnws.server.mapper.ResourcePathMapper;
import com.jsnws.server.mapper.SortMapper;
import com.jsnws.server.mapper.SysUserMapper;
import com.jsnws.server.mapper.TreeHoleMapper;
import com.jsnws.server.mapper.WebInfoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 站点信息模块：站点配置、分类标签、赞赏、收藏、树洞、照片墙、友链、访问统计等
 */
@RestController
public class WebInfoController extends BaseController {

    private final WebInfoMapper webInfoMapper;
    private final SortMapper sortMapper;
    private final LabelMapper labelMapper;
    private final ArticleMapper articleMapper;
    private final SysUserMapper userMapper;
    private final TreeHoleMapper treeHoleMapper;
    private final ResourcePathMapper resourcePathMapper;
    private final FriendMapper friendMapper;
    private final FriendMonitorJob friendMonitorJob;
    private final HistoryMapper historyMapper;
    private final CommentMapper commentMapper;
    private final IpLocationUtil ipLocationUtil;
    private final TokenUtil tokenUtil;
    private final ObjectMapper objectMapper;
    private final ServerStatusService serverStatusService;
    private final MaintenanceService maintenanceService;

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    public WebInfoController(WebInfoMapper webInfoMapper, SortMapper sortMapper, LabelMapper labelMapper,
                             ArticleMapper articleMapper, SysUserMapper userMapper, TreeHoleMapper treeHoleMapper,
                             ResourcePathMapper resourcePathMapper, FriendMapper friendMapper,
                             FriendMonitorJob friendMonitorJob,
                             HistoryMapper historyMapper, CommentMapper commentMapper,
                             IpLocationUtil ipLocationUtil,
                             TokenUtil tokenUtil, ObjectMapper objectMapper,
                             ServerStatusService serverStatusService,
                             MaintenanceService maintenanceService) {
        this.webInfoMapper = webInfoMapper;
        this.sortMapper = sortMapper;
        this.labelMapper = labelMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.treeHoleMapper = treeHoleMapper;
        this.resourcePathMapper = resourcePathMapper;
        this.friendMapper = friendMapper;
        this.friendMonitorJob = friendMonitorJob;
        this.historyMapper = historyMapper;
        this.commentMapper = commentMapper;
        this.ipLocationUtil = ipLocationUtil;
        this.tokenUtil = tokenUtil;
        this.objectMapper = objectMapper;
        this.serverStatusService = serverStatusService;
        this.maintenanceService = maintenanceService;
    }

    // ==================== 前台 ====================

    /** 站点信息（访问统计由前台路由切换时调用 /webInfo/recordVisit 上报） */
    @GetMapping("/webInfo/getWebInfo")
    public Result<WebInfo> getWebInfo() {
        WebInfo webInfo = webInfoMapper.selectById(1L);
        // 服务端兜底：环境变量强制开启时对外表现为“已开启”，避免维护状态锁死前台
        if (webInfo != null && maintenanceService.isForceOpen() && Boolean.FALSE.equals(webInfo.getStatus())) {
            webInfo.setStatus(true);
        }
        return Result.ok(webInfo);
    }

    /** 访问埋点：前台每次路由切换上报当前路径与来源主机名 */
    @PostMapping("/webInfo/recordVisit")
    public Result<Void> recordVisit(@RequestBody(required = false) Map<String, Object> params,
                                    HttpServletRequest request) {
        String path = params == null ? null : strVal(params.get("path"));
        String referrer = params == null ? null : strVal(params.get("referrer"));
        recordHistory(request, path, referrer);
        return Result.ok();
    }

    /** 服务状态：返回服务器处理器 / 内存 / 存储 / 系统版本，数据来自 5 秒一次的缓存采样 */
    @GetMapping("/webInfo/serverStatus")
    public Result<Map<String, Object>> getServerStatus() {
        return Result.ok(serverStatusService.getSnapshot());
    }

    /** 随机头像：从后台「随机头像」配置中随机取一张，未配置时回落到本地匿名头像 */
    @GetMapping("/webInfo/getRandomAvatar")
    public Result<String> getRandomAvatar() {
        WebInfo webInfo = webInfoMapper.selectById(1L);
        String avatar = randomFrom(webInfo == null ? null : webInfo.getRandomAvatar());
        return Result.ok(avatar == null || avatar.isEmpty() ? AnonymousAvatars.random() : avatar);
    }

    /** 站长信息：web_info 表没有站长姓名字段，头像卡片上的站长名称取站长账号的昵称 */
    @GetMapping("/webInfo/getBossInfo")
    public Result<Map<String, Object>> getBossInfo() {
        SysUser boss = userMapper.selectById(bossUserId);
        if (boss == null) {
            return Result.ok(new LinkedHashMap<>());
        }
        Map<String, Object> map = new LinkedHashMap<>();
        String nickname = boss.getNickname();
        map.put("username", nickname == null || nickname.trim().isEmpty() ? boss.getUsername() : nickname);
        map.put("avatar", boss.getAvatar());
        map.put("introduction", boss.getIntroduction());
        return Result.ok(map);
    }

    /** 分类信息（含分类文章数、标签及标签文章数），按 priority 升序 */
    @GetMapping("/webInfo/getSortInfo")
    public Result<List<Sort>> getSortInfo() {
        List<Sort> sorts = sortMapper.selectList(new QueryWrapper<Sort>().orderByAsc("priority"));
        List<Label> labels = labelMapper.selectList(new QueryWrapper<Label>().orderByAsc("id"));
        Map<Long, List<Label>> labelMap = labels.stream().collect(Collectors.groupingBy(Label::getSortId));

        Map<Long, Long> sortCounts = articleMapper.selectList(new QueryWrapper<Article>().select("sort_id"))
                .stream().filter(a -> a.getSortId() != null)
                .collect(Collectors.groupingBy(Article::getSortId, Collectors.counting()));
        Map<Long, Long> labelCounts = articleMapper.selectList(new QueryWrapper<Article>().select("label_id"))
                .stream().filter(a -> a.getLabelId() != null)
                .collect(Collectors.groupingBy(Article::getLabelId, Collectors.counting()));

        for (Sort sort : sorts) {
            sort.setCountOfSort(sortCounts.getOrDefault(sort.getId(), 0L).intValue());
            List<Label> sortLabels = labelMap.getOrDefault(sort.getId(), new ArrayList<>());
            for (Label label : sortLabels) {
                label.setCountOfLabel(labelCounts.getOrDefault(label.getId(), 0L).intValue());
            }
            sort.setLabels(sortLabels);
        }
        return Result.ok(sorts);
    }

    /** 分类与标签（后台文章编辑用） */
    @GetMapping("/webInfo/listSortAndLabel")
    public Result<Map<String, Object>> listSortAndLabel() {
        Map<String, Object> data = new HashMap<>();
        data.put("sorts", sortMapper.selectList(new QueryWrapper<Sort>().orderByAsc("priority")));
        data.put("labels", labelMapper.selectList(new QueryWrapper<Label>().orderByAsc("id")));
        return Result.ok(data);
    }

    /** 收藏夹：{分类: [条目]} */
    @GetMapping("/webInfo/listCollect")
    public Result<Map<String, List<Map<String, Object>>>> listCollect() {
        List<ResourcePath> list = resourcePathMapper.selectList(new QueryWrapper<ResourcePath>()
                .eq("type", "favorites").eq("status", true).orderByDesc("create_time"));
        Map<String, List<Map<String, Object>>> data = new LinkedHashMap<>();
        for (ResourcePath rp : list) {
            String key = rp.getClassify() == null || rp.getClassify().isEmpty() ? "默认" : rp.getClassify();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", rp.getId());
            item.put("url", rp.getUrl());
            item.put("cover", rp.getCover());
            item.put("title", rp.getTitle());
            item.put("introduction", rp.getIntroduction());
            data.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return Result.ok(data);
    }

    /** 树洞列表：nickname 为展示字段，按 user_id 关联用户实时补齐（昵称优先、账号兜底，无用户记为匿名网友） */
    @GetMapping("/webInfo/listTreeHole")
    public Result<List<TreeHole>> listTreeHole() {
        List<TreeHole> list = treeHoleMapper.selectList(new QueryWrapper<TreeHole>().orderByDesc("create_time"));
        Map<Long, SysUser> userMap = new HashMap<>();
        List<Long> userIds = list.stream().map(TreeHole::getUserId).filter(Objects::nonNull).distinct()
                .collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        for (TreeHole treeHole : list) {
            treeHole.setNickname(displayName(userMap.get(treeHole.getUserId())));
        }
        return Result.ok(list);
    }

    /** 树洞匿名发言频率限制：同一 IP 10 秒内只允许 1 条 */
    private static final long TREE_HOLE_INTERVAL_MS = 10_000L;
    private static final int TREE_HOLE_MAX_LENGTH = 200;
    private static final Map<String, Long> TREE_HOLE_LAST_POST = new java.util.concurrent.ConcurrentHashMap<>();

    /** 保存树洞（游客可用；头像只由服务端决定，忽略客户端传入） */
    @PostMapping("/webInfo/saveTreeHole")
    public Result<TreeHole> saveTreeHole(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String message = params.get("message") == null ? null : String.valueOf(params.get("message")).trim();
        if (message == null || message.isEmpty()) {
            throw new ApiException("树洞内容不能为空！");
        }
        if (message.length() > TREE_HOLE_MAX_LENGTH) {
            throw new ApiException("树洞内容不能超过 " + TREE_HOLE_MAX_LENGTH + " 个字符！");
        }
        String ip = clientIp(request);
        long now = System.currentTimeMillis();
        Long last = TREE_HOLE_LAST_POST.get(ip);
        if (last != null && now - last < TREE_HOLE_INTERVAL_MS) {
            throw new ApiException("发言太频繁，请稍后再试！");
        }
        Long userId = currentUserId();
        SysUser user = userId == null ? null : userMapper.selectById(userId);
        WebInfo webInfo = webInfoMapper.selectById(1L);

        TreeHole treeHole = new TreeHole();
        treeHole.setUserId(userId);
        treeHole.setMessage(message);
        if (user != null) {
            // 登录用户一律使用库中头像，防止冒充他人
            String avatar = user.getAvatar();
            treeHole.setAvatar(avatar == null || avatar.isEmpty()
                    ? randomFrom(webInfo == null ? null : webInfo.getRandomAvatar())
                    : avatar);
        } else {
            String randomAvatar = randomFrom(webInfo == null ? null : webInfo.getRandomAvatar());
            // 未登录（匿名）发言：站点未配置随机头像时，沿用匿名留言的本地随机头像约定，
            // 避免 avatar 为 null 导致弹幕前端 v-if 不渲染头像
            treeHole.setAvatar(randomAvatar == null || randomAvatar.isEmpty()
                    ? AnonymousAvatars.random() : randomAvatar);
        }
        treeHole.setCreateTime(new Date());
        treeHole.setIp(ip);
        treeHoleMapper.insert(treeHole);
        TREE_HOLE_LAST_POST.put(ip, now);
        treeHole.setNickname(displayName(user));
        return Result.ok(treeHole);
    }

    /** 弹幕/树洞展示名：昵称优先、账号兜底，无用户时与匿名留言称呼一致 */
    private String displayName(SysUser user) {
        if (user == null) {
            return "匿名网友";
        }
        String nickname = user.getNickname();
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        String username = user.getUsername();
        return username == null || username.trim().isEmpty() ? "匿名网友" : username;
    }

    /** 相册分组（照片墙封面）：[{classify, count}] */
    @GetMapping("/webInfo/listAdminLovePhoto")
    public Result<List<Map<String, Object>>> listAdminLovePhoto() {
        List<ResourcePath> list = resourcePathMapper.selectList(new QueryWrapper<ResourcePath>()
                .eq("type", "lovePhoto").eq("status", true).orderByDesc("create_time"));
        Map<String, Long> counts = list.stream()
                .collect(Collectors.groupingBy(rp -> rp.getClassify() == null ? "" : rp.getClassify(), Collectors.counting()));
        List<Map<String, Object>> data = new ArrayList<>();
        counts.forEach((classify, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("classify", classify);
            item.put("count", count.intValue());
            data.add(item);
        });
        return Result.ok(data);
    }

    /** 资源路径分页（前台照片墙/音乐，只返回已启用资源） */
    @PostMapping("/webInfo/listResourcePath")
    public Result<Page<ResourcePath>> listResourcePath(@RequestBody Map<String, Object> params) {
        return pageResourcePath(params, true);
    }

    /** 后台资源路径分页（管理员，可按 status 筛选） */
    @PostMapping("/webInfo/listAdminResourcePath")
    public Result<Page<ResourcePath>> listAdminResourcePath(@RequestBody Map<String, Object> params) {
        return pageResourcePath(params, false);
    }

    private Result<Page<ResourcePath>> pageResourcePath(Map<String, Object> params, boolean onlyEnabled) {
        Page<ResourcePath> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<ResourcePath> wrapper = new QueryWrapper<>();
        String resourceType = strVal(params.get("resourceType"));
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq("type", resourceType);
        }
        if (params.get("classify") != null && !String.valueOf(params.get("classify")).isEmpty()) {
            wrapper.eq("classify", strVal(params.get("classify")));
        }
        if (onlyEnabled) {
            // 前台强制只看启用资源，忽略客户端传入的 status
            wrapper.eq("status", true);
        } else if (params.get("status") != null) {
            wrapper.eq("status", Boolean.parseBoolean(strVal(params.get("status"))));
        }
        wrapper.orderByDesc("create_time");
        return Result.ok(resourcePathMapper.selectPage(page, wrapper));
    }

    /** 音乐分类页（type=funny 的资源路径） */
    @PostMapping("/webInfo/listFunny")
    public Result<Page<ResourcePath>> listFunny(@RequestBody Map<String, Object> params) {
        params.put("resourceType", "funny");
        return listResourcePath(params);
    }

    /** 新增资源路径（管理员） */
    @PostMapping("/webInfo/saveResourcePath")
    public Result<Void> saveResourcePath(@RequestBody ResourcePath resourcePath) {
        resourcePath.setId(null);
        if (resourcePath.getStatus() == null) {
            resourcePath.setStatus(true);
        }
        resourcePath.setCreateTime(new Date());
        resourcePathMapper.insert(resourcePath);
        return Result.ok();
    }

    /** 更新资源路径（管理员，也用于状态切换） */
    @PostMapping("/webInfo/updateResourcePath")
    public Result<Void> updateResourcePath(@RequestBody ResourcePath resourcePath) {
        if (resourcePath.getId() == null || resourcePathMapper.selectById(resourcePath.getId()) == null) {
            throw new ApiException("资源不存在！");
        }
        resourcePathMapper.updateById(resourcePath);
        return Result.ok();
    }

    /** 删除资源路径（管理员） */
    @GetMapping("/webInfo/deleteResourcePath")
    public Result<Void> deleteResourcePath(@RequestParam("id") Long id) {
        resourcePathMapper.deleteById(id);
        return Result.ok();
    }

    /** 友链申请（需登录，默认进入待审核）。档次由管理员审核后在后台设置，申请一律按普通档次入库 */
    @PostMapping("/webInfo/saveFriend")
    public Result<Void> saveFriend(@RequestBody Friend friend) {
        friend.setId(null);
        if (friend.getStatus() == null) {
            friend.setStatus(0);
        }
        friend.setTier(0);
        // 推荐、点击数、可用状态由管理员/探测任务维护，申请入库一律取默认值
        friend.setRecommended(0);
        friend.setClickCount(0);
        friend.setHealthStatus(0);
        friend.setCreateTime(new Date());
        friendMapper.insert(friend);
        return Result.ok();
    }

    /** 前台友链列表：仅展示已通过的友链，高档次在前 */
    @GetMapping("/webInfo/listFriend")
    public Result<List<Friend>> listFriend() {
        return Result.ok(friendMapper.selectList(new QueryWrapper<Friend>()
                .eq("status", 1).orderByDesc("tier").orderByAsc("id")));
    }

    /** 后台友链列表：可按审核状态筛选，待审核优先 */
    @GetMapping("/webInfo/listAdminFriend")
    public Result<List<Friend>> listAdminFriend(@RequestParam(value = "status", required = false) Integer status) {
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("status").orderByDesc("create_time");
        return Result.ok(friendMapper.selectList(wrapper));
    }

    /** 友链审核：0 待审核、1 通过、2 拒绝 */
    @GetMapping("/webInfo/auditFriend")
    public Result<Void> auditFriend(@RequestParam("id") Long id,
                                    @RequestParam("status") Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new ApiException("审核状态不合法！");
        }
        Friend friend = friendMapper.selectById(id);
        if (friend == null) {
            throw new ApiException("友链不存在！");
        }
        friend.setStatus(status);
        friendMapper.updateById(friend);
        return Result.ok();
    }

    /** 修改友链（管理员） */
    @PostMapping("/webInfo/updateFriend")
    public Result<Void> updateFriend(@RequestBody Friend friend) {
        if (friend.getId() == null || friendMapper.selectById(friend.getId()) == null) {
            throw new ApiException("友链不存在！");
        }
        if (friend.getTitle() == null || friend.getTitle().trim().isEmpty()) {
            throw new ApiException("友链名称不能为空！");
        }
        if (friend.getUrl() == null || friend.getUrl().trim().isEmpty()) {
            throw new ApiException("友链地址不能为空！");
        }
        if (friend.getTier() != null && (friend.getTier() < 0 || friend.getTier() > 3)) {
            throw new ApiException("友链档次不合法！");
        }
        if (friend.getTags() != null && friend.getTags().trim().length() > 200) {
            throw new ApiException("标签总长度不能超过 200 个字符！");
        }
        if (friend.getRecommendLabel() != null && friend.getRecommendLabel().trim().length() > 40) {
            throw new ApiException("推荐文案不能超过 40 个字符！");
        }
        if (friend.getHealthStatus() != null && (friend.getHealthStatus() < 0 || friend.getHealthStatus() > 2)) {
            throw new ApiException("可用状态不合法！");
        }
        if (friend.getRssUrl() != null && !friend.getRssUrl().trim().isEmpty()
                && !friend.getRssUrl().trim().matches("(?i)^https?://.+")) {
            throw new ApiException("RSS 地址必须以 http:// 或 https:// 开头！");
        }
        if (friend.getRecommended() != null && friend.getRecommended() != 0 && friend.getRecommended() != 1) {
            throw new ApiException("推荐标记不合法！");
        }
        friendMapper.updateById(friend);
        return Result.ok();
    }

    /** 前台点击累计：只对已通过的友链 +1，不覆盖其他字段 */
    @PostMapping("/webInfo/friendClick")
    public Result<Integer> friendClick(@RequestParam("id") Long id) {
        Friend friend = friendMapper.selectById(id);
        if (friend == null || friend.getStatus() == null || friend.getStatus() != 1) {
            return Result.ok(0);
        }
        friendMapper.update(null, new UpdateWrapper<Friend>()
                .eq("id", id).setSql("click_count = click_count + 1"));
        Friend latest = friendMapper.selectById(id);
        return Result.ok(latest == null || latest.getClickCount() == null ? 0 : latest.getClickCount());
    }

    /** 后台手动触发友链探测与 RSS 抓取，返回本轮统计 */
    @GetMapping("/webInfo/runFriendProbe")
    public Result<Map<String, Object>> runFriendProbe() {
        return Result.ok(friendMonitorJob.runNow());
    }

    /** 后台推荐/取消推荐：只改推荐标记与文案，不改变档次分组 */
    @GetMapping("/webInfo/recommendFriend")
    public Result<Void> recommendFriend(@RequestParam("id") Long id,
                                        @RequestParam(value = "recommended", required = false) Integer recommended,
                                        @RequestParam(value = "label", required = false) String label) {
        if (friendMapper.selectById(id) == null) {
            throw new ApiException("友链不存在！");
        }
        int flag = (recommended == null || recommended == 0) ? 0 : 1;
        String labelText = label == null ? null : label.trim();
        if (labelText != null && labelText.length() > 40) {
            throw new ApiException("推荐文案不能超过 40 个字符！");
        }
        UpdateWrapper<Friend> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id).set("recommended", flag);
        if (flag == 0) {
            wrapper.set("recommend_label", null);
        } else if (labelText != null) {
            wrapper.set("recommend_label", labelText.isEmpty() ? null : labelText);
        }
        friendMapper.update(null, wrapper);
        return Result.ok();
    }

    /** 删除友链（管理员） */
    @GetMapping("/webInfo/deleteFriend")
    public Result<Void> deleteFriend(@RequestParam("id") Long id) {
        friendMapper.deleteById(id);
        return Result.ok();
    }

    // ==================== 分类/标签管理 ====================

    @PostMapping("/webInfo/saveSort")
    public Result<Void> saveSort(@RequestBody Sort sort) {
        sort.setId(null);
        sort.setCreateTime(new Date());
        sortMapper.insert(sort);
        return Result.ok();
    }

    @PostMapping("/webInfo/updateSort")
    public Result<Void> updateSort(@RequestBody Sort sort) {
        if (sort.getId() == null || sortMapper.selectById(sort.getId()) == null) {
            throw new ApiException("分类不存在！");
        }
        sortMapper.updateById(sort);
        return Result.ok();
    }

    @GetMapping("/webInfo/deleteSort")
    public Result<Void> deleteSort(@RequestParam("id") Long id) {
        sortMapper.deleteById(id);
        // 同步清理：该分类下的标签删除，文章分类置空
        labelMapper.delete(new QueryWrapper<Label>().eq("sort_id", id));
        List<Article> articles = articleMapper.selectList(new QueryWrapper<Article>().eq("sort_id", id));
        for (Article article : articles) {
            article.setSortId(null);
            articleMapper.updateById(article);
        }
        return Result.ok();
    }

    @PostMapping("/webInfo/saveLabel")
    public Result<Void> saveLabel(@RequestBody Label label) {
        label.setId(null);
        label.setCreateTime(new Date());
        labelMapper.insert(label);
        return Result.ok();
    }

    @PostMapping("/webInfo/updateLabel")
    public Result<Void> updateLabel(@RequestBody Label label) {
        if (label.getId() == null || labelMapper.selectById(label.getId()) == null) {
            throw new ApiException("标签不存在！");
        }
        labelMapper.updateById(label);
        return Result.ok();
    }

    @GetMapping("/webInfo/deleteLabel")
    public Result<Void> deleteLabel(@RequestParam("id") Long id) {
        labelMapper.deleteById(id);
        List<Article> articles = articleMapper.selectList(new QueryWrapper<Article>().eq("label_id", id));
        for (Article article : articles) {
            article.setLabelId(null);
            articleMapper.updateById(article);
        }
        return Result.ok();
    }

    // ==================== 站点信息管理 ====================

    /** 更新站点信息（部分字段更新） */
    @PostMapping("/webInfo/updateWebInfo")
    public Result<Void> updateWebInfo(@RequestBody WebInfo webInfo) {
        if (webInfo.getId() == null) {
            webInfo.setId(1L);
        }
        // AI 站长可以改站点基本信息，但不能关闭/恢复站点（status 仅站长可操作）
        if (currentRole() == UserRole.AI_OWNER && webInfo.getStatus() != null) {
            WebInfo exist = webInfoMapper.selectById(webInfo.getId());
            boolean currentStatus = exist != null && Boolean.TRUE.equals(exist.getStatus());
            if (currentStatus != webInfo.getStatus()) {
                throw new ApiException("AI 站长不能关闭或恢复站点！");
            }
            // 保持原值参与更新：只提交 status 时也不会生成空 SET 的 SQL
            webInfo.setStatus(currentStatus);
        }
        // 标签页标题：独立配置，trim 后限制 50 字符；空串表示不配置（前端回退站点名称），null 表示本次不修改
        if (webInfo.getTabTitle() != null) {
            String tabTitle = webInfo.getTabTitle().trim();
            if (tabTitle.length() > 50) {
                throw new ApiException("标签页标题不能超过 50 个字符！");
            }
            webInfo.setTabTitle(tabTitle);
        }
        webInfoMapper.updateById(webInfo);
        return Result.ok();
    }

    /** 后台获取站点信息 */
    @GetMapping("/admin/webInfo/getAdminWebInfo")
    public Result<WebInfo> getAdminWebInfo() {
        return Result.ok(webInfoMapper.selectById(1L));
    }

    /** 删除树洞（管理员） */
    @GetMapping("/webInfo/deleteTreeHole")
    public Result<Void> deleteTreeHole(@RequestParam("id") Long id) {
        treeHoleMapper.deleteById(id);
        return Result.ok();
    }

    // ==================== 访问统计 ====================

    /** 访问统计明细（管理员及以上；IP 做脱敏处理） */
    @GetMapping("/webInfo/getHistoryInfo")
    public Result<Map<String, Object>> getHistoryInfo() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        String today = fmt.format(new Date());
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DATE, -1);
        String yesterday = fmt.format(calendar.getTime());

        List<Map<String, Object>> topIp = historyMapper.topIp(10);
        for (Map<String, Object> row : topIp) {
            row.put("ip", maskIp(strVal(row.get("ip"))));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ip_history_count", historyMapper.countIpHistory());
        data.put("ip_history_province", historyMapper.topProvince(10));
        data.put("ip_history_ip", topIp);
        data.put("ip_count_today", historyMapper.countIpOfDay(today));
        data.put("province_today", historyMapper.provinceOfDay(today, 10));
        data.put("username_today", historyMapper.usersOfDay(today, 50));
        data.put("ip_count_yest", historyMapper.countIpOfDay(yesterday));
        data.put("username_yest", historyMapper.usersOfDay(yesterday, 50));
        return Result.ok(data);
    }

    /** 前台页脚访问量摘要（公开，只返回聚合计数，不含 IP 与用户明细） */
    @GetMapping("/webInfo/getVisitSummary")
    public Result<Map<String, Object>> getVisitSummary() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Object> data = new LinkedHashMap<>();
        String today = fmt.format(new Date());
        data.put("ip_history_count", historyMapper.countIpHistory());
        data.put("ip_count_today", historyMapper.countIpOfDay(today));
        data.put("page_view_today", historyMapper.countPageViewOfDay(today));
        data.put("page_view_total", historyMapper.countPageViewHistory());
        return Result.ok(data);
    }

    /** 站点统计：关于页「站点统计」的聚合数据（公开只读，仅返回聚合结果） */
    @GetMapping("/webInfo/getAnalytics")
    public Result<Map<String, Object>> getAnalytics(
            @RequestParam(value = "range", required = false, defaultValue = "30") Integer range) {
        int days = (range != null && (range == 7 || range == 30 || range == 90)) ? range : 30;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date tomorrowStart = calendar.getTime();
        calendar.setTime(todayStart);
        calendar.add(Calendar.DATE, -(days - 1));
        Date rangeStart = calendar.getTime();
        String today = fmt.format(todayStart);

        Map<String, Object> data = new LinkedHashMap<>();

        // 概览：累计与今日
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("visitors", nvl(historyMapper.countIpHistory()));
        summary.put("pageviews", nvl(historyMapper.countPageViewHistory()));
        summary.put("todayVisitors", nvl(historyMapper.countIpOfDay(today)));
        summary.put("todayPageviews", nvl(historyMapper.countPageViewOfDay(today)));
        data.put("summary", summary);

        // 访问脉冲：按所选区间补齐没有记录的日期
        List<Map<String, Object>> daily = historyMapper.dailyStats(rangeStart, tomorrowStart);
        Map<String, int[]> dailyMap = new HashMap<>();
        for (Map<String, Object> row : daily) {
            dailyMap.put(strVal(row.get("day")), new int[]{intVal(row.get("pv")), intVal(row.get("uv"))});
        }
        List<Map<String, Object>> traffic = new ArrayList<>();
        Calendar cursor = Calendar.getInstance();
        cursor.setTime(rangeStart);
        for (int i = 0; i < days; i++) {
            String day = fmt.format(cursor.getTime());
            int[] values = dailyMap.getOrDefault(day, new int[]{0, 0});
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day);
            point.put("pageviews", values[0]);
            point.put("visitors", values[1]);
            traffic.add(point);
            cursor.add(Calendar.DATE, 1);
        }
        data.put("traffic", traffic);

        // 本周期数据：固定近 30 天，同时返回前 30 天用于计算升降
        calendar.setTime(todayStart);
        calendar.add(Calendar.DATE, -29);
        Date periodStart = calendar.getTime();
        calendar.add(Calendar.DATE, -30);
        Date previousStart = calendar.getTime();

        int periodPv = nvl(historyMapper.countPageViewBetween(periodStart, tomorrowStart));
        int periodUv = nvl(historyMapper.countVisitorBetween(periodStart, tomorrowStart));
        List<Map<String, Object>> periodDaily = historyMapper.dailyStats(periodStart, tomorrowStart);
        int peak = 0;
        for (Map<String, Object> row : periodDaily) {
            peak = Math.max(peak, intVal(row.get("pv")));
        }
        Map<String, Object> period = new LinkedHashMap<>();
        period.put("available", periodPv > 0);
        period.put("pageviews", periodPv);
        period.put("visitors", periodUv);
        period.put("activeDays", periodDaily.size());
        period.put("peak", peak);
        period.put("days", 30);
        period.put("previousPageviews", nvl(historyMapper.countPageViewBetween(previousStart, periodStart)));
        period.put("previousVisitors", nvl(historyMapper.countVisitorBetween(previousStart, periodStart)));
        data.put("period", period);

        // 页面排行（近 30 天，只有存在 path 的记录才参与）
        data.put("pages", historyMapper.topPages(periodStart, 4));

        // 活跃时间热力图：周一=0 … 周日=6
        List<Map<String, Object>> cellRows = historyMapper.activityCells(periodStart);
        int[][] grid = new int[7][24];
        int activityTotal = 0;
        for (Map<String, Object> row : cellRows) {
            int weekday = intVal(row.get("wd"));
            int hour = intVal(row.get("hr"));
            int num = intVal(row.get("num"));
            if (weekday >= 0 && weekday < 7 && hour >= 0 && hour < 24) {
                grid[weekday][hour] += num;
                activityTotal += num;
            }
        }
        int maxCell = 0;
        int topWeekday = 0;
        int topHour = 0;
        int[] hourTotals = new int[24];
        int[] weekdayTotals = new int[7];
        for (int w = 0; w < 7; w++) {
            for (int h = 0; h < 24; h++) {
                int value = grid[w][h];
                if (value > maxCell) {
                    maxCell = value;
                }
                hourTotals[h] += value;
                weekdayTotals[w] += value;
                if (weekdayTotals[w] > weekdayTotals[topWeekday]) {
                    topWeekday = w;
                }
                if (hourTotals[h] > hourTotals[topHour]) {
                    topHour = h;
                }
            }
        }
        int night = 0;
        for (int h = 0; h < 24; h++) {
            if (h >= 22 || h <= 5) {
                night += hourTotals[h];
            }
        }
        List<List<Integer>> gridList = new ArrayList<>();
        for (int w = 0; w < 7; w++) {
            List<Integer> line = new ArrayList<>();
            for (int h = 0; h < 24; h++) {
                line.add(grid[w][h]);
            }
            gridList.add(line);
        }
        Map<String, Object> activity = new LinkedHashMap<>();
        activity.put("available", activityTotal > 0);
        activity.put("grid", gridList);
        activity.put("max", maxCell);
        activity.put("topHour", topHour);
        activity.put("topWeekday", topWeekday);
        activity.put("nightRatio", activityTotal == 0 ? 0 : Math.round(night * 100f / activityTotal));
        activity.put("todayVisitors", summary.get("todayVisitors"));
        data.put("activity", activity);

        // 设备与浏览器：按 UA 分组后服务端解析，只返回有数据的分类
        List<Map<String, Object>> uaRows = historyMapper.userAgentStats(periodStart);
        Map<String, Integer> deviceCounts = new LinkedHashMap<>();
        Map<String, Integer> browserCounts = new LinkedHashMap<>();
        for (Map<String, Object> row : uaRows) {
            String ua = strVal(row.get("ua"));
            if (ua == null || ua.isEmpty()) {
                continue;
            }
            int num = intVal(row.get("num"));
            String device = UserAgentUtil.device(ua);
            if (device != null) {
                deviceCounts.merge(device, num, Integer::sum);
            }
            String browser = UserAgentUtil.browserName(ua);
            browserCounts.merge(browser == null ? "其他" : browser, num, Integer::sum);
        }
        Map<String, Object> clients = new LinkedHashMap<>();
        clients.put("available", !deviceCounts.isEmpty() || !browserCounts.isEmpty());
        clients.put("devices", ranked(deviceCounts, 10));
        clients.put("browsers", ranked(browserCounts, 4));
        data.put("clients", clients);

        // 流量来源：只统计有 referrer 的记录（直接访问写入「直接」）
        List<Map<String, Object>> sourceRows = historyMapper.sourceStats(periodStart);
        List<Map<String, Object>> sources = new ArrayList<>();
        int sourceTotal = 0;
        for (Map<String, Object> row : sourceRows) {
            String referrer = strVal(row.get("referrer"));
            int num = intVal(row.get("num"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", referrer);
            item.put("value", num);
            sources.add(item);
            sourceTotal += num;
        }
        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("available", sourceTotal > 0);
        sourceData.put("total", sourceTotal);
        sourceData.put("items", sources);
        data.put("sources", sourceData);

        // 地域分布：只统计已解析出省份的记录，未覆盖的省份不补零
        List<Map<String, Object>> regionRows = historyMapper.regionStats(periodStart);
        List<Map<String, Object>> regions = new ArrayList<>();
        int regionTotal = 0;
        for (Map<String, Object> row : regionRows) {
            String province = strVal(row.get("province"));
            int num = intVal(row.get("num"));
            if (province == null || province.isEmpty() || num <= 0) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", province);
            item.put("value", num);
            regions.add(item);
            regionTotal += num;
        }
        Map<String, Object> regionData = new LinkedHashMap<>();
        regionData.put("available", regionTotal > 0);
        regionData.put("total", regionTotal);
        regionData.put("items", regions);
        data.put("region", regionData);

        // 最近评论：文章评论与留言板留言合并，按时间倒序取最新 8 条，昵称优先、账号兜底
        data.put("comments", latestComments(8));

        return Result.ok(data);
    }

    /** 最近评论：补全展示名、头像与所在页面 */
    private List<Map<String, Object>> latestComments(int limit) {
        List<Comment> comments = commentMapper.latestComments(limit);
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = new ArrayList<>();
        List<Long> articleIds = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            Long articleId = articleIdOf(comment);
            if (articleId != null) {
                articleIds.add(articleId);
            }
        }
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(user -> userMap.put(user.getId(), user));
        }
        Map<Long, String> titleMap = new HashMap<>();
        if (!articleIds.isEmpty()) {
            articleMapper.selectBatchIds(articleIds).forEach(article -> titleMap.put(article.getId(), article.getArticleTitle()));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment comment : comments) {
            String fallbackName = comment.getUsername();
            String name = comment.getUserId() == null
                    ? (fallbackName == null || fallbackName.trim().isEmpty() ? "匿名网友" : fallbackName)
                    : displayName(userMap.get(comment.getUserId()));
            String avatar = comment.getIsAnonymous() != null && comment.getIsAnonymous() == 1
                    ? comment.getAnonymousAvatar() : comment.getAvatar();
            Long articleId = articleIdOf(comment);
            String pageTitle;
            String path;
            if (articleId != null) {
                pageTitle = titleMap.get(articleId);
                path = "/article/" + articleId;
            } else {
                pageTitle = "留言板";
                path = "/message";
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", comment.getId());
            item.put("name", name);
            item.put("avatar", avatar);
            item.put("content", comment.getCommentContent());
            item.put("createTime", comment.getCreateTime());
            item.put("commentType", comment.getCommentType());
            // 身份徽章/名字配色所需字段：与文章评论区 Comment.vue 使用同一套判断条件
            SysUser author = comment.getUserId() == null ? null : userMap.get(comment.getUserId());
            item.put("userId", comment.getUserId());
            item.put("isAnonymous", comment.getIsAnonymous());
            item.put("userType", author == null ? null : UserRole.normalize(author.getUserType()));
            item.put("pageTitle", pageTitle);
            item.put("path", path);
            result.add(item);
        }
        return result;
    }

    private Long articleIdOf(Comment comment) {
        if (!"article".equals(comment.getCommentType()) || comment.getSource() == null) {
            return null;
        }
        try {
            return Long.parseLong(comment.getSource().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 计数列表：按数量倒序，可选把前 N 名之外的合并为「其他」 */
    private List<Map<String, Object>> ranked(Map<String, Integer> counts, int limit) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        List<Map<String, Object>> result = new ArrayList<>();
        int other = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (i < limit) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", entries.get(i).getKey());
                item.put("value", entries.get(i).getValue());
                result.add(item);
            } else {
                other += entries.get(i).getValue();
            }
        }
        if (other > 0) {
            for (Map<String, Object> item : result) {
                if ("其他".equals(item.get("name"))) {
                    item.put("value", intVal(item.get("value")) + other);
                    return result;
                }
            }
            Map<String, Object> rest = new LinkedHashMap<>();
            rest.put("name", "其他");
            rest.put("value", other);
            result.add(rest);
        }
        return result;
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private int intVal(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? 0 : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** IP 脱敏：IPv4 隐藏最后一段，IPv6 隐藏最后一段 */
    private String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        if (ip.indexOf('.') >= 0) {
            int idx = ip.lastIndexOf('.');
            return idx > 0 ? ip.substring(0, idx) + ".*" : ip;
        }
        int idx = ip.lastIndexOf(':');
        return idx > 0 ? ip.substring(0, idx) + ":*" : ip;
    }

    /** 埋点：记录访问（页面路径、User-Agent、来源主机名；省份由 ip2region 解析） */
    private void recordHistory(HttpServletRequest request, String path, String referrer) {
        try {
            String ip = clientIp(request);
            History history = new History();
            history.setIp(ip);
            history.setProvince(resolveProvince(ip));
            String safePath = trimToNull(path, 255);
            history.setPath(safePath);
            history.setUserAgent(trimToNull(request.getHeader("User-Agent"), 500));
            String source = trimToNull(referrer, 255);
            // 直接访问没有来源主机名，写入「直接」以便与历史缺失数据区分
            history.setReferrer(source == null ? "直接" : source);
            Long userId = currentUserId();
            if (userId != null) {
                SysUser user = userMapper.selectById(userId);
                if (user != null) {
                    history.setUserId(user.getId());
                    history.setUsername(user.getUsername());
                    history.setAvatar(user.getAvatar());
                }
            }
            history.setCreateTime(new Date());
            historyMapper.insert(history);
        } catch (Exception ignored) {
        }
    }

    /** 省份：取 ip2region 结果第一段并去掉行政后缀；解析失败记为「未知」 */
    private String resolveProvince(String ip) {
        try {
            String location = ipLocationUtil.resolve(ip);
            if (location == null || location.trim().isEmpty() || "未知".equals(location)) {
                return "未知";
            }
            String province = location.split("·")[0].trim();
            province = province.replace("壮族自治区", "").replace("回族自治区", "")
                    .replace("维吾尔自治区", "").replace("特别行政区", "")
                    .replace("自治区", "").replace("省", "").replace("市", "");
            return province.isEmpty() ? "未知" : province;
        } catch (Exception e) {
            return "未知";
        }
    }

    private String trimToNull(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

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

    private String randomFrom(String jsonArray) {
        try {
            if (jsonArray != null && !jsonArray.isEmpty()) {
                List<String> list = objectMapper.readValue(jsonArray, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                if (!list.isEmpty()) {
                    return list.get(new java.util.Random().nextInt(list.size()));
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String strVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private long longVal(Object value, long def) {
        if (value == null) return def;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }
}
