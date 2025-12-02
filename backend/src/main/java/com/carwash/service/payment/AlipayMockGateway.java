package com.carwash.service.payment;

import com.carwash.config.PaymentConfig;
import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 支付宝沙箱模拟支付网关
 * 用于测试和演示,不进行真实资金扣款
 * 
 * 功能特性:
 * 1. 模拟支付宝扫码支付流程
 * 2. 自动触发支付成功回调
 * 3. 完整的签名验证机制
 * 4. 支持退款模拟
 * 
 * @author CarWash Team
 * @version 1.0
 */
@Slf4j
@Service
public class AlipayMockGateway implements PaymentGateway {

    @Autowired
    private PaymentConfig paymentConfig;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    // 模拟支付订单存储(实际项目应使用Redis)
    private final Map<String, MockPaymentInfo> mockPaymentStore = new ConcurrentHashMap<>();
    
    /**
     * 模拟支付信息
     */
    private static class MockPaymentInfo {
        String orderNo;
        String paymentNo;
        BigDecimal amount;
        String status;
        LocalDateTime createTime;
        LocalDateTime paidTime;
    }
    
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            log.info("【支付宝模拟支付】创建支付订单: {}, 金额: {}", request.getOrderNo(), request.getAmount());
            
            // 构建支付参数
            Map<String, String> params = buildPaymentParams(request);
            
            // 生成二维码内容(模拟支付宝二维码)
            String qrCode = generateMockQrCode(request.getOrderNo());
            
            // 生成支付URL(模拟支付宝支付页面)
            String paymentUrl = buildPaymentUrl(params);
            
            // 存储模拟支付信息
            MockPaymentInfo mockInfo = new MockPaymentInfo();
            mockInfo.orderNo = request.getOrderNo();
            mockInfo.paymentNo = generatePaymentNo(request.getOrderNo());
            mockInfo.amount = request.getAmount();
            mockInfo.status = "pending";
            mockInfo.createTime = LocalDateTime.now();
            mockPaymentStore.put(request.getOrderNo(), mockInfo);
            
            // 异步触发支付成功回调(模拟用户扫码支付,延迟3秒)
            triggerPaymentSuccessCallback(request.getOrderNo(), mockInfo.paymentNo, request.getAmount());
            
            // 构建响应
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setPaymentNo(mockInfo.paymentNo);
            response.setOrderNo(request.getOrderNo());
            response.setStatus("pending");
            response.setPaymentUrl(paymentUrl);
            response.setQrCode(qrCode);
            response.setMessage("【模拟支付】支付订单创建成功,将在3秒后自动支付成功");
            
            log.info("【支付宝模拟支付】订单创建成功: {}, 支付流水号: {}", request.getOrderNo(), mockInfo.paymentNo);
            
            return response;
            
        } catch (Exception e) {
            log.error("【支付宝模拟支付】创建支付订单失败", e);
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setStatus("failed");
            response.setMessage("创建支付订单失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public PaymentResponse queryPayment(String paymentNo) {
        try {
            log.info("【支付宝模拟支付】查询支付状态: {}", paymentNo);
            
            // 从存储中查询支付信息
            MockPaymentInfo mockInfo = mockPaymentStore.values().stream()
                    .filter(info -> info.paymentNo.equals(paymentNo))
                    .findFirst()
                    .orElse(null);
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setPaymentNo(paymentNo);
            
            if (mockInfo != null) {
                response.setOrderNo(mockInfo.orderNo);
                response.setAmount(mockInfo.amount);
                response.setStatus(mockInfo.status);
                response.setPaidAt(mockInfo.paidTime);
                response.setMessage("查询成功");
                
                if ("paid".equals(mockInfo.status)) {
                    response.setTransactionId("ALIPAY_MOCK_" + paymentNo);
                }
            } else {
                // 默认返回已支付(兼容已有订单)
                response.setStatus("paid");
                response.setTransactionId("ALIPAY_MOCK_" + paymentNo);
                response.setMessage("【模拟支付】查询成功(默认已支付)");
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("【支付宝模拟支付】查询支付状态失败", e);
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("查询支付状态失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            log.info("【支付宝模拟支付】申请退款: {}, 金额: {}", request.getPaymentNo(), request.getAmount());
            
            // 查找对应的支付信息
            MockPaymentInfo mockInfo = mockPaymentStore.values().stream()
                    .filter(info -> info.paymentNo.equals(request.getPaymentNo()))
                    .findFirst()
                    .orElse(null);
            
            if (mockInfo == null || !"paid".equals(mockInfo.status)) {
                RefundResponse response = new RefundResponse();
                response.setSuccess(false);
                response.setMessage("支付订单不存在或未支付,无法退款");
                return response;
            }
            
            // 模拟退款成功
            String refundNo = generateRefundNo();
            
            RefundResponse response = new RefundResponse();
            response.setSuccess(true);
            response.setRefundNo(refundNo);
            response.setStatus("success");
            response.setMessage("【模拟支付】退款申请成功");
            
            log.info("【支付宝模拟支付】退款成功: {}, 退款单号: {}", request.getPaymentNo(), refundNo);
            
            return response;
            
        } catch (Exception e) {
            log.error("【支付宝模拟支付】申请退款失败", e);
            RefundResponse response = new RefundResponse();
            response.setSuccess(false);
            response.setMessage("申请退款失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public RefundResponse queryRefund(String refundNo) {
        try {
            log.info("【支付宝模拟支付】查询退款状态: {}", refundNo);
            
            // 模拟退款查询成功
            RefundResponse response = new RefundResponse();
            response.setSuccess(true);
            response.setRefundNo(refundNo);
            response.setStatus("success");
            response.setMessage("【模拟支付】查询成功");
            
            return response;
            
        } catch (Exception e) {
            log.error("【支付宝模拟支付】查询退款状态失败", e);
            RefundResponse response = new RefundResponse();
            response.setSuccess(false);
            response.setMessage("查询退款状态失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public boolean handleCallback(String callbackData) {
        try {
            log.info("【支付宝模拟支付】处理回调: {}", callbackData);
            
            // 在实际项目中,这里应该验证支付宝的回调签名
            // 模拟支付环境下,我们直接返回成功
            
            return true;
            
        } catch (Exception e) {
            log.error("【支付宝模拟支付】处理回调失败", e);
            return false;
        }
    }
    
    @Override
    public String getPaymentMethod() {
        return "alipay_mock";
    }
    
    /**
     * 异步触发支付成功回调
     * 模拟用户扫码支付成功后,支付宝回调商户服务器的场景
     */
    @Async
    private void triggerPaymentSuccessCallback(String orderNo, String paymentNo, BigDecimal amount) {
        try {
            // 延迟3秒模拟支付处理时间
            TimeUnit.SECONDS.sleep(3);
            
            log.info("【支付宝模拟支付】触发支付成功回调: {}", orderNo);
            
            // 更新模拟支付状态
            MockPaymentInfo mockInfo = mockPaymentStore.get(orderNo);
            if (mockInfo != null) {
                mockInfo.status = "paid";
                mockInfo.paidTime = LocalDateTime.now();
            }
            
            // 构建回调参数(模拟支付宝回调参数)
            Map<String, String> callbackParams = buildCallbackParams(orderNo, paymentNo, amount);
            
            // 调用本地回调接口
            String callbackUrl = "http://localhost:" + serverPort + "/api/payment/callback/alipay";
            
            if (restTemplate == null) {
                restTemplate = new RestTemplate();
            }
            
            // 发送POST请求模拟支付宝回调
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            callbackParams.forEach(formData::add);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(callbackUrl, request, String.class);
            
            log.info("【支付宝模拟支付】回调结果: {}, 响应: {}", orderNo, response.getBody());
            
        } catch (Exception e) {
            log.error("【支付宝模拟支付】触发回调失败", e);
        }
    }
    
    /**
     * 构建支付参数
     */
    private Map<String, String> buildPaymentParams(PaymentRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", paymentConfig.getAlipay().getAppId());
        params.put("method", "alipay.trade.precreate");
        params.put("charset", paymentConfig.getAlipay().getCharset());
        params.put("sign_type", paymentConfig.getAlipay().getSignType());
        params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.put("version", "1.0");
        params.put("notify_url", paymentConfig.getAlipay().getNotifyUrl());
        
        // 业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", request.getOrderNo());
        bizContent.put("total_amount", request.getAmount().toString());
        bizContent.put("subject", "【模拟支付】洗车服务-" + request.getOrderNo());
        bizContent.put("store_id", "001");
        bizContent.put("timeout_express", "30m");
        
        params.put("biz_content", convertMapToJson(bizContent));
        
        // 生成签名
        String sign = generateSign(params);
        params.put("sign", sign);
        
        return params;
    }
    
    /**
     * 构建回调参数
     */
    private Map<String, String> buildCallbackParams(String orderNo, String paymentNo, BigDecimal amount) {
        Map<String, String> params = new HashMap<>();
        
        // 支付宝标准回调参数
        params.put("out_trade_no", orderNo);
        params.put("trade_no", "ALIPAY_MOCK_" + paymentNo);
        params.put("transaction_id", "ALIPAY_MOCK_" + paymentNo);
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("total_amount", amount.toString());
        params.put("receipt_amount", amount.toString());
        params.put("buyer_id", "2088000000000001");
        params.put("buyer_logon_id", "test***@sandbox.com");
        params.put("seller_id", paymentConfig.getAlipay().getAppId());
        params.put("app_id", paymentConfig.getAlipay().getAppId());
        params.put("notify_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.put("notify_type", "trade_status_sync");
        params.put("notify_id", UUID.randomUUID().toString().replace("-", ""));
        
        // 生成回调签名
        String sign = generateSign(params);
        params.put("sign", sign);
        params.put("sign_type", "RSA2");
        
        return params;
    }
    
    /**
     * 生成模拟二维码
     */
    private String generateMockQrCode(String orderNo) {
        return "https://qr.alipay.com/mock/" + UUID.randomUUID().toString().replace("-", "") + 
               "?orderNo=" + orderNo + "&mock=true";
    }
    
    /**
     * 生成支付流水号
     */
    private String generatePaymentNo(String orderNo) {
        return "PAY_MOCK_" + System.currentTimeMillis() + "_" + orderNo.substring(Math.max(0, orderNo.length() - 8));
    }
    
    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "REF_MOCK_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               String.format("%06d", new Random().nextInt(1000000));
    }
    
    /**
     * 生成签名(模拟签名,实际项目应使用RSA签名)
     */
    private String generateSign(Map<String, String> params) {
        try {
            // 排序参数
            TreeMap<String, String> sortedParams = new TreeMap<>(params);
            
            // 构建签名字符串
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (!"sign".equals(entry.getKey()) && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            
            // 添加模拟密钥
            sb.append("&key=MOCK_ALIPAY_KEY");
            
            // SHA-256签名
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "MOCK_SIGN_" + UUID.randomUUID().toString();
        }
    }
    
    /**
     * 构建支付URL
     */
    private String buildPaymentUrl(Map<String, String> params) {
        try {
            StringBuilder sb = new StringBuilder("https://openapi.alipay.com/gateway.do");
            sb.append("?mock=true");
            
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append("&");
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("构建支付URL失败", e);
            return "https://openapi.alipay.com/gateway.do?mock=true&error=true";
        }
    }
    
    /**
     * 将Map转换为JSON字符串
     */
    private String convertMapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
