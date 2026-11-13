package com.jsnws.server.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 轻量 HMAC-SHA256 token：base64url(userId.expireAt) + "." + base64url(signature)
 * 无状态，不需要服务端存储
 */
@Component
public class TokenUtil {

    @Value("${jsnws.token-secret}")
    private String secret;

    @Value("${jsnws.token-expire}")
    private long expire;

    public String create(Long userId) {
        long expireAt = System.currentTimeMillis() + expire;
        String payload = userId + "." + expireAt;
        String head = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return head + "." + sign(head);
    }

    /**
     * 校验 token，有效返回 userId，无效返回 null
     */
    public Long verify(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        int index = token.indexOf('.');
        if (index <= 0 || index == token.length() - 1) {
            return null;
        }
        String head = token.substring(0, index);
        String signPart = token.substring(index + 1);
        if (!signPart.equals(sign(head))) {
            return null;
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(head), StandardCharsets.UTF_8);
            String[] parts = payload.split("\\.");
            if (parts.length != 2) {
                return null;
            }
            long expireAt = Long.parseLong(parts[1]);
            if (System.currentTimeMillis() > expireAt) {
                return null;
            }
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String head) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(head.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ApiException("token 签名失败");
        }
    }
}
