package com.jsnws.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.AesUtil;
import com.jsnws.server.common.GlobalExceptionHandler;
import com.jsnws.server.common.TokenUtil;
import com.jsnws.server.entity.SysUser;
import com.jsnws.server.mapper.SysUserMapper;
import com.jsnws.server.mapper.WebInfoMapper;
import com.jsnws.server.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /user/login 登录接口单元测试（不依赖数据库与 Spring 容器，Mapper 全部 mock）
 */
@ExtendWith(MockitoExtension.class)
class UserControllerLoginTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 前端 encrypt('123456') 的结果：AES/ECB + Base64 后将 / 换 _、+ 换 - */
    private static final String ADMIN_PASSWORD_CIPHER = "S1TbWZ3KoFpR2qkkySTSHg==";

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private WebInfoMapper webInfoMapper;

    private TokenUtil tokenUtil;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tokenUtil = new TokenUtil();
        ReflectionTestUtils.setField(tokenUtil, "secret", "unit-test-token-secret");
        ReflectionTestUtils.setField(tokenUtil, "expire", 3600_000L);

        AesUtil aesUtil = new AesUtil();
        ReflectionTestUtils.setField(aesUtil, "key", "sarasarasarasara");

        UserController controller = new UserController(userMapper, webInfoMapper, tokenUtil, aesUtil,
                OBJECT_MAPPER, new MailService(null, null, OBJECT_MAPPER, false));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("管理员账号密码正确：返回 200、token 与用户信息，且不回传密码")
    void loginSuccess() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(buildUser("admin", true, 1));

        JsonNode body = login("admin", ADMIN_PASSWORD_CIPHER, false);

        assertEquals(200, body.get("code").asInt());
        assertEquals("admin", body.get("data").get("username").asText());
        assertEquals("admin@jsnws.com", body.get("data").get("email").asText());
        assertEquals("操作成功", body.get("message").asText());
        assertFalse(body.get("data").has("password"), "响应中不应包含密码字段");

        String token = body.get("data").get("accessToken").asText();
        assertFalse(token.isEmpty(), "accessToken 不应为空");
        assertEquals(1L, tokenUtil.verify(token), "token 应能解析出用户 id");
        assertNull(tokenUtil.verify(token + "x"), "篡改后的 token 应校验失败");
    }

    @Test
    @DisplayName("用邮箱登录：同样可以登录成功")
    void loginByEmailSuccess() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(buildUser("admin", true, 1));

        JsonNode body = login("admin@jsnws.com", ADMIN_PASSWORD_CIPHER, false);

        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("data").hasNonNull("accessToken"));
    }

    @Test
    @DisplayName("密码错误：返回 500 与提示语")
    void loginWithWrongPassword() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(buildUser("admin", true, 1));

        JsonNode body = login("admin", "wrong-cipher==", false);

        assertEquals(500, body.get("code").asInt());
        assertEquals("账号或密码错误！", body.get("message").asText());
        assertTrue(body.get("data").isNull());
    }

    @Test
    @DisplayName("账号不存在：返回 500 与提示语")
    void loginWithUnknownAccount() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(null);

        JsonNode body = login("nobody", ADMIN_PASSWORD_CIPHER, false);

        assertEquals(500, body.get("code").asInt());
        assertEquals("账号或密码错误！", body.get("message").asText());
    }

    @Test
    @DisplayName("账号被禁用：返回 500 与提示语")
    void loginWithDisabledUser() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(buildUser("admin", false, 1));

        JsonNode body = login("admin", ADMIN_PASSWORD_CIPHER, false);

        assertEquals(500, body.get("code").asInt());
        assertEquals("账号已被禁用！", body.get("message").asText());
    }

    @Test
    @DisplayName("普通用户走管理员登录：返回 500 与提示语")
    void loginAsAdminWithNormalUser() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(buildUser("reader", true, 0));

        JsonNode body = login("reader", ADMIN_PASSWORD_CIPHER, true);

        assertEquals(500, body.get("code").asInt());
        assertEquals("该账号没有管理员权限！", body.get("message").asText());
    }

    @Test
    @DisplayName("缺少 password 参数：被全局异常处理兜底，返回 500")
    void loginWithoutPasswordParam() throws Exception {
        JsonNode body = login("admin", null, false);

        assertEquals(500, body.get("code").asInt());
        assertTrue(body.get("message").asText().contains("服务异常"), "应由 GlobalExceptionHandler 兜底");
    }

    @Test
    @DisplayName("种子密码密文与前端 encrypt('123456') 一致")
    void seedPasswordCipherMatchesFrontend() {
        AesUtil aesUtil = new AesUtil();
        ReflectionTestUtils.setField(aesUtil, "key", "sarasarasarasara");

        assertTrue(aesUtil.match(ADMIN_PASSWORD_CIPHER, "123456"));
        assertFalse(aesUtil.match(ADMIN_PASSWORD_CIPHER, "1234567"));
    }

    // ==================== 辅助方法 ====================

    private SysUser buildUser(String username, boolean enabled, int userType) {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername(username);
        user.setAccount("admin@jsnws.com");
        user.setEmail("admin@jsnws.com");
        user.setPassword(ADMIN_PASSWORD_CIPHER);
        user.setUserStatus(enabled);
        user.setUserType(userType);
        return user;
    }

    /** 以表单方式提交登录请求（与前端 $http.post(..., false, false) 一致），返回解析后的响应体 */
    private JsonNode login(String account, String password, Boolean isAdmin) throws Exception {
        MockHttpServletRequestBuilder builder = post("/user/login");
        if (account != null) {
            builder.param("account", account);
        }
        if (password != null) {
            builder.param("password", password);
        }
        if (isAdmin != null) {
            builder.param("isAdmin", String.valueOf(isAdmin));
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        String json = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        return OBJECT_MAPPER.readTree(json);
    }
}
