package com.jsnws.server.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 与前端 utils/common.js 的 encrypt() 对应：
 * AES/ECB/PKCS7(PKCS5 等价)，key 为 UTF-8 字节（16 位），输出 Base64 后把 / 替换为 _、+ 替换为 -
 */
@Component
public class AesUtil {

    @Value("${jsnws.crypto-key}")
    private String key;

    public String decrypt(String encrypted) {
        try {
            String normalized = encrypted.replace("_", "/").replace("-", "+");
            byte[] raw = Base64.getDecoder().decode(normalized);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"));
            return new String(cipher.doFinal(raw), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ApiException("解密失败");
        }
    }

    public boolean match(String encrypted, String plainText) {
        try {
            return plainText != null && plainText.equals(decrypt(encrypted));
        } catch (Exception e) {
            return false;
        }
    }
}
