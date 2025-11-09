package com.carwash.service.payment;

import com.carwash.config.PaymentConfig;
import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 信用卡支付网关实现（模拟）
 * - 解密前端RSA-OAEP加密的securePayload
 * - 校验卡号（Luhn）、有效期、CVV
 * - 不存储任何敏感信息，符合PCI-DSS最小化原则
 */
@Slf4j
@Service
public class CreditCardGateway implements PaymentGateway {

    @Autowired
    private PaymentConfig paymentConfig;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            String payload = request.getSecurePayload();
            if (payload == null || payload.isEmpty()) {
                PaymentResponse resp = new PaymentResponse();
                resp.setStatus("failed");
                resp.setMessage("缺少安全支付载荷");
                return resp;
            }

            // 解密载荷
            String json = decryptSecurePayload(payload, paymentConfig.getSecurity().getRsaPrivateKey());

            Map<String, Object> card = parseJsonToMap(json);
            String cardNumber = String.valueOf(card.getOrDefault("cardNumber", ""));
            String expiry = String.valueOf(card.getOrDefault("expiry", ""));
            String cvv = String.valueOf(card.getOrDefault("cvv", ""));
            String cardHolder = String.valueOf(card.getOrDefault("cardHolder", ""));

            // 基本校验
            if (!isValidCardNumber(cardNumber)) {
                PaymentResponse resp = new PaymentResponse();
                resp.setStatus("failed");
                resp.setMessage("信用卡号校验失败");
                return resp;
            }
            if (!isValidExpiry(expiry)) {
                PaymentResponse resp = new PaymentResponse();
                resp.setStatus("failed");
                resp.setMessage("有效期格式错误或已过期");
                return resp;
            }
            if (!cvv.matches("^[0-9]{3,4}$")) {
                PaymentResponse resp = new PaymentResponse();
                resp.setStatus("failed");
                resp.setMessage("CVV格式错误");
                return resp;
            }
            if (cardHolder == null || cardHolder.trim().isEmpty()) {
                PaymentResponse resp = new PaymentResponse();
                resp.setStatus("failed");
                resp.setMessage("持卡人姓名不能为空");
                return resp;
            }

            String masked = maskPan(cardNumber);
            log.info("信用卡信息通过校验，持卡人: {}, 卡号: {}", cardHolder, masked);

            // 模拟创建支付，返回待支付状态
            PaymentResponse response = new PaymentResponse();
            response.setStatus("pending");
            response.setPaymentUrl("card://charge");
            response.setMessage("卡信息验证通过，等待扣款");
            return response;
        } catch (Exception e) {
            log.error("创建信用卡支付失败", e);
            PaymentResponse resp = new PaymentResponse();
            resp.setStatus("failed");
            resp.setMessage("创建信用卡支付失败: " + e.getMessage());
            return resp;
        }
    }

    @Override
    public PaymentResponse queryPayment(String paymentNo) {
        // 模拟查询：直接返回已支付
        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentNo(paymentNo);
        resp.setStatus("paid");
        resp.setTransactionId("CARD" + System.currentTimeMillis());
        resp.setMessage("支付成功");
        return resp;
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        RefundResponse resp = new RefundResponse();
        resp.setSuccess(true);
        resp.setRefundNo("REF" + System.currentTimeMillis());
        resp.setMessage("信用卡退款受理成功");
        return resp;
    }

    @Override
    public RefundResponse queryRefund(String refundNo) {
        RefundResponse resp = new RefundResponse();
        resp.setSuccess(true);
        resp.setRefundNo(refundNo);
        resp.setMessage("退款成功");
        return resp;
    }

    @Override
    public boolean handleCallback(String callbackData) {
        // 模拟卡支付无回调或回调始终通过
        return true;
    }

    @Override
    public String getPaymentMethod() {
        return "credit_card";
    }

    // ===== helper methods =====
    private String decryptSecurePayload(String base64Cipher, String pemPrivateKey) throws Exception {
        PrivateKey privateKey = loadPrivateKeyFromPem(pemPrivateKey);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(base64Cipher));
        return new String(decrypted);
    }

    private PrivateKey loadPrivateKeyFromPem(String pem) throws Exception {
        String normalized = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\r", "")
                .replaceAll("\n", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) throws Exception {
        // 使用Jackson（Spring Boot默认引入）
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.readValue(json, Map.class);
    }

    private boolean isValidCardNumber(String pan) {
        String digits = pan.replaceAll("[^0-9]", "");
        if (digits.length() < 12 || digits.length() > 19) return false;
        int sum = 0;
        boolean alt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alt) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    private boolean isValidExpiry(String expiry) {
        // 格式 MM/YY
        if (!expiry.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) return false;
        String[] parts = expiry.split("/");
        int mm = Integer.parseInt(parts[0]);
        int yy = Integer.parseInt(parts[1]);
        // 简化校验：不检查当前年月，只校验格式
        return mm >= 1 && mm <= 12;
    }

    private String maskPan(String pan) {
        String digits = pan.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return "****";
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }
}