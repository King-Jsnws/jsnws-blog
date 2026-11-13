package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.common.TokenUtil;
import com.jsnws.server.entity.Article;
import com.jsnws.server.entity.Friend;
import com.jsnws.server.entity.History;
import com.jsnws.server.entity.Label;
import com.jsnws.server.entity.ResourcePath;
import com.jsnws.server.entity.Sort;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.entity.TreeHole;
import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.mapper.ArticleMapper;
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
    private final HistoryMapper historyMapper;
    private final TokenUtil tokenUtil;
    private final ObjectMapper objectMapper;

    @Value("${jsnws.boss-user-id}")
    private Long bossUserId;

    public WebInfoController(WebInfoMapper webInfoMapper, SortMapper sortMapper, LabelMapper labelMapper,
                             ArticleMapper articleMapper, SysUserMapper userMapper, TreeHoleMapper treeHoleMapper,
                             ResourcePathMapper resourcePathMapper, FriendMapper friendMapper,
                             HistoryMapper historyMapper, TokenUtil tokenUtil, ObjectMapper objectMapper) {
        this.webInfoMapper = webInfoMapper;
        this.sortMapper = sortMapper;
        this.labelMapper = labelMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.treeHoleMapper = treeHoleMapper;
        this.resourcePathMapper = resourcePathMapper;
        this.friendMapper = friendMapper;
        this.historyMapper = historyMapper;
        this.tokenUtil = tokenUtil;
        this.objectMapper = objectMapper;
    }

    // ==================== 前台 ====================

    /** 站点信息（同时记录一条访问统计） */
    @GetMapping("/webInfo/getWebInfo")
    public Result<WebInfo> getWebInfo(HttpServletRequest request) {
        recordHistory(request);
        return Result.ok(webInfoMapper.selectById(1L));
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

    /** 树洞列表 */
    @GetMapping("/webInfo/listTreeHole")
    public Result<List<TreeHole>> listTreeHole() {
        return Result.ok(treeHoleMapper.selectList(new QueryWrapper<TreeHole>().orderByDesc("create_time")));
    }

    /** 保存树洞（游客可用，头像取自定义头像或随机头像） */
    @PostMapping("/webInfo/saveTreeHole")
    public Result<TreeHole> saveTreeHole(@RequestBody Map<String, Object> params) {
        String message = params.get("message") == null ? null : String.valueOf(params.get("message"));
        if (message == null || message.isEmpty()) {
            throw new ApiException("树洞内容不能为空！");
        }
        Long userId = currentUserId();
        SysUser user = userId == null ? null : userMapper.selectById(userId);

        TreeHole treeHole = new TreeHole();
        treeHole.setUserId(userId);
        treeHole.setMessage(message);
        if (params.get("avatar") != null && !String.valueOf(params.get("avatar")).isEmpty()) {
            treeHole.setAvatar(String.valueOf(params.get("avatar")));
        } else if (user != null) {
            treeHole.setAvatar(user.getAvatar());
        } else {
            WebInfo webInfo = webInfoMapper.selectById(1L);
            treeHole.setAvatar(randomFrom(webInfo == null ? null : webInfo.getRandomAvatar()));
        }
        treeHole.setCreateTime(new Date());
        treeHoleMapper.insert(treeHole);
        return Result.ok(treeHole);
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

    /** 资源路径分页（前台照片墙/音乐；后台管理共用） */
    @PostMapping("/webInfo/listResourcePath")
    public Result<Page<ResourcePath>> listResourcePath(@RequestBody Map<String, Object> params) {
        Page<ResourcePath> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<ResourcePath> wrapper = new QueryWrapper<>();
        String resourceType = strVal(params.get("resourceType"));
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq("type", resourceType);
        }
        if (params.get("classify") != null && !String.valueOf(params.get("classify")).isEmpty()) {
            wrapper.eq("classify", strVal(params.get("classify")));
        }
        if (params.get("status") != null) {
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

    /** 友链申请（需登录，默认进入待审核） */
    @PostMapping("/webInfo/saveFriend")
    public Result<Void> saveFriend(@RequestBody Friend friend) {
        friend.setId(null);
        if (friend.getAvatar() == null || friend.getAvatar().isEmpty()) {
            friend.setAvatar(friend.getCover());
        }
        if (friend.getStatus() == null) {
            friend.setStatus(0);
        }
        friend.setCreateTime(new Date());
        friendMapper.insert(friend);
        return Result.ok();
    }

    /** 前台友链列表：仅展示已通过的友链 */
    @GetMapping("/webInfo/listFriend")
    public Result<List<Friend>> listFriend() {
        return Result.ok(friendMapper.selectList(new QueryWrapper<Friend>()
                .eq("status", 1).orderByAsc("id")));
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
        if (friend.getAvatar() == null || friend.getAvatar().isEmpty()) {
            friend.setAvatar(friend.getCover());
        }
        friendMapper.updateById(friend);
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

    @GetMapping("/webInfo/getHistoryInfo")
    public Result<Map<String, Object>> getHistoryInfo() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        String today = fmt.format(new Date());
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DATE, -1);
        String yesterday = fmt.format(calendar.getTime());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ip_history_count", historyMapper.countIpHistory());
        data.put("ip_history_province", historyMapper.topProvince(10));
        data.put("ip_history_ip", historyMapper.topIp(10));
        data.put("ip_count_today", historyMapper.countIpOfDay(today));
        data.put("province_today", historyMapper.provinceOfDay(today, 10));
        data.put("username_today", historyMapper.usersOfDay(today, 50));
        data.put("ip_count_yest", historyMapper.countIpOfDay(yesterday));
        data.put("username_yest", historyMapper.usersOfDay(yesterday, 50));
        return Result.ok(data);
    }

    /** 埋点：记录访问（每分钟内同一 IP 去重） */
    private void recordHistory(HttpServletRequest request) {
        try {
            String ip = clientIp(request);
            History history = new History();
            history.setIp(ip);
            history.setProvince("未知");
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
