package com.jsnws.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.entity.SysConfig;
import com.jsnws.server.entity.WebInfo;
import com.jsnws.server.mapper.SysConfigMapper;
import com.jsnws.server.mapper.WebInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送：SMTP 参数全部来自后台 sys_config，每次发送都重新读库，
 * 后台改完配置立即生效，不做任何缓存。
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    public static final String KEY_HOST = "mailHost";
    public static final String KEY_PORT = "mailPort";
    public static final String KEY_USERNAME = "mailUsername";
    public static final String KEY_PASSWORD = "mailPassword";
    public static final String KEY_FROM = "mailFrom";
    public static final String KEY_ENCRYPT = "mailEncrypt";
    public static final String KEY_ENABLED = "mailEnabled";

    private static final List<String> ALL_KEYS = Arrays.asList(
            KEY_HOST, KEY_PORT, KEY_USERNAME, KEY_PASSWORD, KEY_FROM, KEY_ENCRYPT, KEY_ENABLED);

    private static final int DEFAULT_PORT = 465;
    private static final int TIMEOUT_MS = 10000;

    private final SysConfigMapper sysConfigMapper;
    private final WebInfoMapper webInfoMapper;
    private final ObjectMapper objectMapper;

    /** application.yml / 环境变量 JSNWS_MAIL_ENABLED 提供的兜底开关 */
    private final boolean defaultEnabled;

    public MailService(SysConfigMapper sysConfigMapper, WebInfoMapper webInfoMapper,
                       ObjectMapper objectMapper,
                       @Value("${jsnws.mail-enabled:true}") boolean defaultEnabled) {
        this.sysConfigMapper = sysConfigMapper;
        this.webInfoMapper = webInfoMapper;
        this.objectMapper = objectMapper;
        this.defaultEnabled = defaultEnabled;
    }

    /**
     * 站点名称：与前台顶栏/页脚一致，取「网站设置 → 网站名称」webName，
     * 为空时退回网站标题 webTitle，再为空用「博客」兜底。
     * 每次发送都重新读库，后台改名后无需重启立即生效。
     */
    public String siteName() {
        WebInfo info = webInfoMapper.selectById(1L);
        if (info != null) {
            if (!isBlank(info.getWebName())) {
                return info.getWebName().trim();
            }
            if (!isBlank(info.getWebTitle())) {
                String title = info.getWebTitle().trim();
                // 网站标题在前台可能是字符串数组，拼接后与顶栏展示保持一致
                if (title.startsWith("[")) {
                    try {
                        List<?> parts = objectMapper.readValue(title, List.class);
                        StringBuilder joined = new StringBuilder();
                        for (Object part : parts) {
                            if (part != null) {
                                joined.append(part);
                            }
                        }
                        if (!isBlank(joined.toString())) {
                            return joined.toString().trim();
                        }
                    } catch (Exception ignored) {
                        // 解析失败时按原文使用
                    }
                }
                return title;
            }
        }
        return "博客";
    }

    /** 是否启用邮件服务：后台 mailEnabled 优先，其次 application.yml / 环境变量 */
    public boolean isEnabled() {
        MailConfig config = loadConfig();
        return config.getEnabled() == null ? defaultEnabled : config.getEnabled();
    }

    /** 每次调用都重新读取 sys_config，保证后台修改后立即生效 */
    public MailConfig loadConfig() {
        MailConfig config = new MailConfig();
        Map<String, String> values = new HashMap<>();
        List<SysConfig> configs = sysConfigMapper.selectList(new QueryWrapper<SysConfig>()
                .in("config_key", ALL_KEYS));
        for (SysConfig item : configs) {
            String value = item.getConfigValue();
            if (value != null && !value.trim().isEmpty()) {
                values.putIfAbsent(item.getConfigKey(), value.trim());
            }
        }
        config.setHost(values.get(KEY_HOST));
        config.setUsername(values.get(KEY_USERNAME));
        config.setPassword(values.get(KEY_PASSWORD));
        config.setFromName(values.get(KEY_FROM));
        config.setEncrypt(normalizeEncrypt(values.get(KEY_ENCRYPT)));

        String port = values.get(KEY_PORT);
        if (port == null) {
            config.setPort(DEFAULT_PORT);
        } else {
            try {
                config.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                config.setPort(-1);
            }
        }

        String enabled = values.get(KEY_ENABLED);
        if (enabled != null) {
            config.setEnabled(parseEnabled(enabled));
        }
        return config;
    }

    /** 发送验证码等业务邮件；配置不完整或认证失败时抛出可直接展示给用户的中文提示 */
    public void send(String to, String subject, String content) {
        MailConfig config = assertReady();
        JavaMailSenderImpl sender = buildSender(config);
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            // 发件人名称：后台填写值优先，留空时跟站点名称走
            String displayName = isBlank(config.getFromName()) ? siteName() : config.getFromName().trim();
            helper.setFrom(new InternetAddress(config.getUsername(), displayName, "UTF-8"));
            helper.setSubject(subject);
            helper.setText(content, false);
            sender.send(message);
        } catch (MailAuthenticationException e) {
            throw new ApiException("SMTP 认证失败（535）：请确认「发件邮箱」填写的是完整邮箱地址，"
                    + "「邮箱授权码」使用客户端专用密码（授权码），并已在网页版邮箱开启 SMTP 服务。");
        } catch (MailException e) {
            throw new ApiException(translate(e));
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new ApiException("邮件内容组装失败：" + rootMessage(e));
        }
    }

    /** 后台「发送测试邮件」：配置缺失时直接抛出缺项提示，不抛 500 堆栈 */
    public void sendTest(String to) {
        assertReady();
        String siteName = siteName();
        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        send(to, "【" + siteName + "】邮件发送测试",
                "这是一封来自「" + siteName + "」的测试邮件，用于验证站点邮件发送配置。\n\n"
                        + "发送时间：" + now + "\n"
                        + "收到本邮件说明 SMTP 服务器、端口、加密方式与账号授权码均配置正确。");
    }

    /** 校验必填项，返回本次生效配置；缺项时给出逐项中文提示 */
    public MailConfig assertReady() {
        MailConfig config = loadConfig();
        List<String> missing = new ArrayList<>();
        if (isBlank(config.getHost())) {
            missing.add("邮件服务器");
        }
        if (config.getPort() <= 0) {
            missing.add("邮件端口（必须是数字）");
        }
        if (isBlank(config.getUsername())) {
            missing.add("发件邮箱");
        }
        if (isBlank(config.getPassword())) {
            missing.add("邮箱授权码");
        }
        if (!missing.isEmpty()) {
            throw new ApiException("邮箱配置未完成，缺少：" + String.join("、", missing)
                    + "。请在后台「系统 → 邮箱配置」补齐并保存后重试。");
        }
        return config;
    }

    private JavaMailSenderImpl buildSender(MailConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost());
        sender.setPort(config.getPort());
        sender.setUsername(config.getUsername());
        sender.setPassword(config.getPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", String.valueOf(TIMEOUT_MS));
        props.put("mail.smtp.timeout", String.valueOf(TIMEOUT_MS));
        props.put("mail.smtp.writetimeout", String.valueOf(TIMEOUT_MS));
        if ("ssl".equals(config.getEncrypt())) {
            // 465 隐式 SSL：连接建立即加密
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else if ("tls".equals(config.getEncrypt())) {
            // 587 STARTTLS：先明文连接再升级
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        return sender;
    }

    /** 把 JavaMail 英文异常翻译成可操作的排查提示，绝不输出账号密码 */
    private String translate(MailException e) {
        String message = rootMessage(e);
        String lower = message.toLowerCase();
        if (lower.contains("535") || lower.contains("authentication")
                || lower.contains("username and password not accepted")) {
            return "SMTP 认证失败（535）：请确认发件邮箱为完整地址、密码使用客户端专用密码（授权码），"
                    + "并已在网页版邮箱开启 SMTP 服务。";
        }
        if (lower.contains("550")) {
            if (lower.contains("frequency") || lower.contains("denied") || lower.contains("limit")) {
                return "发送被拒（550）：发信频率或服务器 IP 触发腾讯限制，请降低频率后稍后重试。";
            }
            return "发送被拒（550）：请确认收件地址真实存在，且企业邮箱已开启 SMTP 服务。";
        }
        if (lower.contains("501")) {
            return "发送被拒（501）：邮箱可能被管理员强制开启微信动态码登录，"
                    + "请生成客户端专用密码后再试。";
        }
        if (lower.contains("unknown host") || lower.contains("connect") || lower.contains("timeout")
                || lower.contains("timed out") || lower.contains("refused")) {
            return "无法连接 SMTP 服务器：请检查邮件服务器地址、端口、加密方式，"
                    + "以及服务器出站 465/587 端口是否放通。";
        }
        if (lower.contains("handshake") || lower.contains("ssl") || lower.contains("certificate")
                || lower.contains("starttls")) {
            return "SSL/TLS 握手失败：请确认加密方式与端口匹配（SSL 用 465，TLS 用 587）。";
        }
        if (lower.contains("sender") || lower.contains("from")) {
            return "发件地址被拒绝：请确认「发件邮箱」与企业邮箱账号一致，且未被管理员限制发信。";
        }
        // 只记录失败类型，不打印账号、授权码或 SMTP 原始报文
        log.warn("邮件发送失败：未匹配到已知 SMTP 错误码，请检查后台邮箱配置");
        return "邮件发送失败：" + message;
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        String message = error.getMessage();
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
            if (current.getMessage() != null && !current.getMessage().trim().isEmpty()) {
                message = current.getMessage();
            }
        }
        return message == null ? "未知错误" : message.trim();
    }

    private Boolean parseEnabled(String value) {
        String text = value.trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "on".equalsIgnoreCase(text);
    }

    private String normalizeEncrypt(String value) {
        if (isBlank(value)) {
            return "ssl";
        }
        String text = value.trim().toLowerCase();
        if ("tls".equals(text) || "starttls".equals(text)) {
            return "tls";
        }
        if ("none".equals(text) || "false".equals(text)) {
            return "none";
        }
        return "ssl";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** 本次发送使用的配置快照（不缓存，仅在一次调用内使用） */
    public static class MailConfig {
        private String host;
        private int port;
        private String username;
        private String password;
        private String fromName;
        private String encrypt;
        private Boolean enabled;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFromName() { return fromName; }
        public void setFromName(String fromName) { this.fromName = fromName; }
        public String getEncrypt() { return encrypt; }
        public void setEncrypt(String encrypt) { this.encrypt = encrypt; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }
}
