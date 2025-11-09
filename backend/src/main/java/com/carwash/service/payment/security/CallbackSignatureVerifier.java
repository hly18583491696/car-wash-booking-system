package com.carwash.service.payment.security;

import com.carwash.config.PaymentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * 支付回调验签器
 * - wechat: 采用参数字典序 + &key=API_KEY 的 MD5(大写)校验
 * - alipay: 采用 RSA/RSA2 公钥验签（Base64签名），参数字典序拼接
 * - virtual: 始终通过（开发/测试用）
 */
@Component
public class CallbackSignatureVerifier {

    private final PaymentConfig paymentConfig;

    @Autowired
    public CallbackSignatureVerifier(PaymentConfig paymentConfig) {
        this.paymentConfig = paymentConfig;
    }

    /**
     * 根据支付方式验证签名
     */
    public boolean verify(String paymentMethod, Map<String, String> params) {
        if (paymentMethod == null) return false;
        switch (paymentMethod.toLowerCase()) {
            case "wechat":
                return verifyWechat(params);
            case "alipay":
                return verifyAlipay(params);
            case "virtual":
                return true;
            default:
                return false;
        }
    }

    /**
     * 微信支付验签（MD5 + API_KEY）
     */
    private boolean verifyWechat(Map<String, String> params) {
        String providedSign = safeGet(params, "sign");
        if (providedSign.isEmpty()) return false;
        String canonical = buildCanonicalQuery(params, Set.of("sign"), false);
        String apiKey = Optional.ofNullable(paymentConfig.getWechat())
                .map(PaymentConfig.WechatPay::getApiKey)
                .orElse("");
        String text = canonical + "&key=" + apiKey;
        String calculated = md5Upper(text);
        return providedSign.equalsIgnoreCase(calculated);
    }

    /**
     * 支付宝验签（RSA/RSA2）
     */
    private boolean verifyAlipay(Map<String, String> params) {
        String providedSign = safeGet(params, "sign");
        if (providedSign.isEmpty()) return false;

        String signType = Optional.ofNullable(params.get("sign_type"))
                .orElse(Optional.ofNullable(paymentConfig.getAlipay())
                        .map(PaymentConfig.Alipay::getSignType).orElse("RSA2"));
        String charset = Optional.ofNullable(paymentConfig.getAlipay())
                .map(PaymentConfig.Alipay::getCharset)
                .orElse("UTF-8");

        String content = buildCanonicalQuery(params, Set.of("sign", "sign_type"), true);
        String pemPublic = Optional.ofNullable(paymentConfig.getAlipay())
                .map(PaymentConfig.Alipay::getAlipayPublicKey)
                .orElse("");
        if (pemPublic.isEmpty()) return false;

        try {
            PublicKey publicKey = loadPublicKeyFromPem(pemPublic);
            Signature signature = Signature.getInstance("RSA2".equalsIgnoreCase(signType) ? "SHA256withRSA" : "SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(content.getBytes(charset));
            byte[] signBytes = tryDecodeBase64OrHex(providedSign);
            return signature.verify(signBytes);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 按字典序构建签名内容字符串
     */
    private String buildCanonicalQuery(Map<String, String> params, Set<String> excludeKeys, boolean encodeValues) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (excludeKeys != null && excludeKeys.contains(key)) continue;
            if (value == null || value.isEmpty()) continue;
            sorted.put(key, value);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(e.getKey()).append('=');
            sb.append(encodeValues ? urlEncode(e.getValue(), StandardCharsets.UTF_8.name()) : e.getValue());
        }
        return sb.toString();
    }

    private String safeGet(Map<String, String> params, String key) {
        if (params == null) return "";
        String v = params.get(key);
        return v == null ? "" : v;
    }

    private String urlEncode(String s, String charset) {
        try {
            return URLEncoder.encode(s, charset);
        } catch (Exception e) {
            return s;
        }
    }

    private String md5Upper(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }

    private PublicKey loadPublicKeyFromPem(String pem) throws Exception {
        String normalized = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private byte[] tryDecodeBase64OrHex(String s) {
        try {
            return Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException e) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        }
    }
}