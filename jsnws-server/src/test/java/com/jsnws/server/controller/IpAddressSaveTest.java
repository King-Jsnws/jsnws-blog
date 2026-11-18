package com.jsnws.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.GlobalExceptionHandler;
import com.jsnws.server.common.IpLocationUtil;
import com.jsnws.server.common.ServerStatusService;
import com.jsnws.server.common.TokenUtil;
import com.jsnws.server.entity.Article;
import com.jsnws.server.entity.Comment;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.entity.TreeHole;
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
import com.jsnws.server.mapper.WeiYanMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 弹幕/留言/评论 IP 记录与后台展示单元测试（不启动 Spring 容器，Mapper 全部 mock）
 */
@ExtendWith(MockitoExtension.class)
class IpAddressSaveTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private TreeHoleMapper treeHoleMapper;
    @Mock
    private WebInfoMapper webInfoMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private WeiYanMapper weiYanMapper;
    @Mock
    private IpLocationUtil ipLocationUtil;

    /** 弹幕提交（WebInfoController） */
    private MockMvc webInfoMockMvc;
    /** 评论/留言（CommentController） */
    private MockMvc commentMockMvc;
    /** 后台弹幕列表（WeiYanController） */
    private MockMvc weiYanMockMvc;

    @BeforeEach
    void setUp() {
        WebInfoController webInfoController = new WebInfoController(webInfoMapper,
                org.mockito.Mockito.mock(SortMapper.class), org.mockito.Mockito.mock(LabelMapper.class),
                articleMapper, userMapper, treeHoleMapper,
                org.mockito.Mockito.mock(ResourcePathMapper.class), org.mockito.Mockito.mock(FriendMapper.class),
                org.mockito.Mockito.mock(FriendMonitorJob.class), org.mockito.Mockito.mock(HistoryMapper.class),
                commentMapper, ipLocationUtil, new TokenUtil(), OBJECT_MAPPER,
                new ServerStatusService(),
                new com.jsnws.server.common.MaintenanceService(webInfoMapper));
        webInfoMockMvc = MockMvcBuilders.standaloneSetup(webInfoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        CommentController commentController = new CommentController(commentMapper, userMapper, articleMapper,
                webInfoMapper, ipLocationUtil, OBJECT_MAPPER);
        commentMockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        WeiYanController weiYanController = new WeiYanController(weiYanMapper, treeHoleMapper);
        weiYanMockMvc = MockMvcBuilders.standaloneSetup(weiYanController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("弹幕保存：按 X-Forwarded-For 首段记录客户端 IP，响应不返回 IP")
    void saveTreeHoleRecordsClientIp() throws Exception {
        when(webInfoMapper.selectById(1L)).thenReturn(null);
        when(treeHoleMapper.insert(any(TreeHole.class))).thenReturn(1);

        JsonNode body = perform(webInfoMockMvc, post("/webInfo/saveTreeHole")
                .header("X-Forwarded-For", "203.0.113.9, 10.0.0.1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"单元测试弹幕\"}"));

        assertEquals(200, body.get("code").asInt());
        assertFalse(body.get("data").has("ip"), "前台接口响应不应返回 ip 字段");
        assertNull(body.get("data").get("ipAddress").textValue(), "前台接口的 ipAddress 应保持为空");

        ArgumentCaptor<TreeHole> captor = ArgumentCaptor.forClass(TreeHole.class);
        verify(treeHoleMapper).insert(captor.capture());
        assertEquals("203.0.113.9", captor.getValue().getIp(), "应取 X-Forwarded-For 的第一段作为真实 IP");
    }

    @Test
    @DisplayName("后台弹幕列表：返回 ipAddress 供管理员查看，且不暴露 ip 字段")
    void adminBarrageListExposesIp() throws Exception {
        TreeHole hole = new TreeHole();
        hole.setId(7L);
        hole.setMessage("测试弹幕");
        hole.setIp("198.51.100.7");
        Page<TreeHole> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(hole));
        page.setTotal(1);
        when(treeHoleMapper.selectPage(ArgumentMatchers.<Page<TreeHole>>any(), any())).thenReturn(page);

        JsonNode body = perform(weiYanMockMvc, post("/admin/treeHole/boss/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"current\":1,\"size\":10}"));

        JsonNode record = body.get("data").get("records").get(0);
        assertEquals("198.51.100.7", record.get("ipAddress").asText());
        assertFalse(record.has("ip"), "后台列表只输出 ipAddress，原始 ip 字段仍应隐藏");
    }

    @Test
    @DisplayName("后台评论/留言列表：返回 ipAddress，原始 ip 字段不输出")
    void adminCommentListExposesIp() throws Exception {
        Comment comment = new Comment();
        comment.setId(9L);
        comment.setCommentType("message");
        comment.setCommentContent("单元测试留言");
        comment.setIp("198.51.100.9");
        comment.setLocation("测试省·测试市");
        Page<Comment> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(comment));
        page.setTotal(1);
        when(commentMapper.selectPage(ArgumentMatchers.<Page<Comment>>any(), any())).thenReturn(page);

        JsonNode body = perform(commentMockMvc, post("/admin/comment/boss/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"current\":1,\"size\":10,\"commentType\":\"message\"}"));

        JsonNode record = body.get("data").get("records").get(0);
        assertEquals("198.51.100.9", record.get("ipAddress").asText());
        assertEquals("测试省·测试市", record.get("location").asText());
        assertFalse(record.has("ip"), "后台列表只输出 ipAddress，原始 ip 字段仍应隐藏");
    }

    @Test
    @DisplayName("文章评论保存：记录客户端 IP、归属地与浏览器信息（原有逻辑回归）")
    void saveArticleCommentRecordsClientIp() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("站长");
        when(userMapper.selectById(1L)).thenReturn(user);

        Article article = new Article();
        article.setId(5L);
        article.setViewStatus(true);
        article.setCommentStatus(true);
        article.setCommentCount(0);
        when(articleMapper.selectById(5L)).thenReturn(article);

        when(ipLocationUtil.resolve("198.51.100.21")).thenReturn("测试省·测试市");
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);

        JsonNode body = perform(commentMockMvc, post("/comment/saveComment")
                .requestAttr("userId", 1L)
                .header("X-Forwarded-For", "198.51.100.21")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/151.0.0.0 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commentContent\":\"单元测试评论\",\"source\":\"5\",\"commentType\":\"article\"}"));

        assertEquals(200, body.get("code").asInt());
        assertFalse(body.get("data").has("ip"), "前台接口响应不应返回 ip 字段");

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insert(captor.capture());
        Comment saved = captor.getValue();
        assertEquals("198.51.100.21", saved.getIp());
        assertEquals("测试省·测试市", saved.getLocation());
        assertEquals("Chrome151.0", saved.getBrowser());
    }

    @Test
    @DisplayName("实体序列化：ip 字段不参与 JSON 输出，ipAddress 可正常展示")
    void ipFieldIsNotSerialized() {
        Comment comment = new Comment();
        comment.setIp("1.1.1.1");
        comment.setIpAddress("1.1.1.1");
        JsonNode commentNode = OBJECT_MAPPER.valueToTree(comment);
        assertFalse(commentNode.has("ip"));
        assertEquals("1.1.1.1", commentNode.get("ipAddress").asText());

        TreeHole treeHole = new TreeHole();
        treeHole.setIp("2.2.2.2");
        treeHole.setIpAddress("2.2.2.2");
        JsonNode treeHoleNode = OBJECT_MAPPER.valueToTree(treeHole);
        assertFalse(treeHoleNode.has("ip"));
        assertEquals("2.2.2.2", treeHoleNode.get("ipAddress").asText());
    }

    // ==================== 辅助方法 ====================

    /** 执行请求并解析 UTF-8 响应体 */
    private JsonNode perform(MockMvc mockMvc, MockHttpServletRequestBuilder builder) throws Exception {
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        String json = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        return OBJECT_MAPPER.readTree(json);
    }
}
