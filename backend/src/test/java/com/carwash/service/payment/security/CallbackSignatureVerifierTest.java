package com.carwash.service.payment.security;

import com.carwash.config.PaymentConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class CallbackSignatureVerifierTest {

    @Test
    void testWechatSignatureSuccess() throws Exception {
        PaymentConfig cfg = new PaymentConfig();
        cfg.getWechat().setApiKey("TEST_KEY_123");

        CallbackSignatureVerifier verifier = new CallbackSignatureVerifier(cfg);

        Map<String, String> params = new HashMap<>();
        params.put("appid", "wx_test_app");
        params.put("mch_id", "mch_10001");
        params.put("nonce_str", "ABC123");
        params.put("out_trade_no", "ORDER_0001");
        params.put("total_fee", "100");

        String canonical = canonical(params, true, false, false);
        String text = canonical + "&key=" + cfg.getWechat().getApiKey();
        String md5 = md5Upper(text);
        params.put("sign", md5);

        assertTrue(verifier.verify("wechat", params));
    }

    @Test
    void testWechatSignatureFail() throws Exception {
        PaymentConfig cfg = new PaymentConfig();
        cfg.getWechat().setApiKey("TEST_KEY_123");

        CallbackSignatureVerifier verifier = new CallbackSignatureVerifier(cfg);

        Map<String, String> params = new HashMap<>();
        params.put("appid", "wx_test_app");
        params.put("mch_id", "mch_10001");
        params.put("nonce_str", "ABC123");
        params.put("out_trade_no", "ORDER_0001");
        params.put("total_fee", "100");
        params.put("sign", "WRONGSIGN");

        assertFalse(verifier.verify("wechat", params));
    }

    @Test
    void testAlipaySignatureSuccess() throws Exception {
        // 生成临时密钥对用于测试
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        PaymentConfig cfg = new PaymentConfig();
        cfg.getAlipay().setSignType("RSA2");
        cfg.getAlipay().setCharset("UTF-8");
        cfg.getAlipay().setAlipayPublicKey(toPemPublicKey(publicKey));

        CallbackSignatureVerifier verifier = new CallbackSignatureVerifier(cfg);

        Map<String, String> params = new HashMap<>();
        params.put("app_id", "alipay_test_app");
        params.put("method", "alipay.trade.pay");
        params.put("charset", "UTF-8");
        params.put("out_trade_no", "ORDER_0002");
        params.put("total_amount", "25.00");
        params.put("seller_id", "2088102178081234");
        params.put("timestamp", "2025-11-08 10:00:00");
        params.put("sign_type", "RSA2");

        String content = canonical(params, true, true, true); // exclude sign & sign_type, URL编码值
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(content.getBytes(StandardCharsets.UTF_8));
        String sign = Base64.getEncoder().encodeToString(sig.sign());
        params.put("sign", sign);

        assertTrue(verifier.verify("alipay", params));
    }

    @Test
    void testVirtualSignatureAlwaysTrue() {
        PaymentConfig cfg = new PaymentConfig();
        CallbackSignatureVerifier verifier = new CallbackSignatureVerifier(cfg);
        Map<String, String> params = new HashMap<>();
        assertTrue(verifier.verify("virtual", params));
    }

    // 构建字典序拼接字符串（测试侧与实现侧保持一致）
    private static String canonical(Map<String, String> params, boolean excludeSign, boolean excludeSignType, boolean urlEncodeValues) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (v == null || v.isEmpty()) continue;
            if (excludeSign && "sign".equals(k)) continue;
            if (excludeSignType && "sign_type".equals(k)) continue;
            sorted.put(k, v);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(e.getKey()).append('=');
            if (urlEncodeValues) {
                try {
                    sb.append(java.net.URLEncoder.encode(e.getValue(), "UTF-8"));
                } catch (Exception ex) {
                    sb.append(e.getValue());
                }
            } else {
                sb.append(e.getValue());
            }
        }
        return sb.toString();
    }

    private static String md5Upper(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    private static String toPemPublicKey(PublicKey publicKey) {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PUBLIC KEY-----\n");
        sb.append(base64).append("\n");
        sb.append("-----END PUBLIC KEY-----");
        return sb.toString();
    }
}