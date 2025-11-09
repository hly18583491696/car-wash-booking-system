package com.carwash.service.payment;

import com.carwash.config.PaymentConfig;
import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;
import com.carwash.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 支付宝支付网关实现
 */
@Service
public class AlipayGateway implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(AlipayGateway.class);
    
    @Autowired
    private PaymentConfig paymentConfig;
    
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            logger.info("创建支付宝支付订单: {}", request.getOrderNo());
            
            // 构建支付参数
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
            bizContent.put("subject", "洗车服务-" + request.getOrderNo());
            bizContent.put("store_id", "001");
            bizContent.put("timeout_express", "30m");
            
            params.put("biz_content", convertMapToJson(bizContent));
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 模拟调用支付宝API
            // 在实际项目中，这里应该调用支付宝的统一收单线下交易预创建接口
            String qrCode = simulateAlipayAPI(params);
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setPaymentNo(request.getOrderNo());
            response.setPaymentUrl(buildPaymentUrl(params));
            response.setQrCode(qrCode);
            response.setMessage("支付订单创建成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("创建支付宝支付订单失败", e);
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("创建支付订单失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public PaymentResponse queryPayment(String paymentNo) {
        try {
            logger.info("查询支付宝支付状态: {}", paymentNo);
            
            // 构建查询参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", paymentConfig.getAlipay().getAppId());
            params.put("method", "alipay.trade.query");
            params.put("charset", paymentConfig.getAlipay().getCharset());
            params.put("sign_type", paymentConfig.getAlipay().getSignType());
            params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            params.put("version", "1.0");
            
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", paymentNo);
            params.put("biz_content", convertMapToJson(bizContent));
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 模拟查询支付状态
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setPaymentNo(paymentNo);
            response.setStatus("paid"); // 模拟已支付状态
            response.setMessage("查询成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("查询支付宝支付状态失败", e);
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("查询支付状态失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            logger.info("申请支付宝退款: {}", request.getPaymentNo());
            
            // 构建退款参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", paymentConfig.getAlipay().getAppId());
            params.put("method", "alipay.trade.refund");
            params.put("charset", paymentConfig.getAlipay().getCharset());
            params.put("sign_type", paymentConfig.getAlipay().getSignType());
            params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            params.put("version", "1.0");
            
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", request.getPaymentNo());
            bizContent.put("refund_amount", request.getAmount().toString());
            bizContent.put("refund_reason", request.getReason());
            bizContent.put("out_request_no", generateRefundNo());
            
            params.put("biz_content", convertMapToJson(bizContent));
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 模拟调用支付宝退款API
            String refundNo = simulateAlipayRefundAPI(params);
            
            RefundResponse response = new RefundResponse();
            response.setSuccess(true);
            response.setRefundNo(refundNo);
            response.setMessage("退款申请成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("申请支付宝退款失败", e);
            RefundResponse response = new RefundResponse();
            response.setSuccess(false);
            response.setMessage("申请退款失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public RefundResponse queryRefund(String refundNo) {
        try {
            logger.info("查询支付宝退款状态: {}", refundNo);
            
            // 模拟查询退款状态
            RefundResponse response = new RefundResponse();
            response.setSuccess(true);
            response.setRefundNo(refundNo);
            response.setStatus("success");
            response.setMessage("查询成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("查询支付宝退款状态失败", e);
            RefundResponse response = new RefundResponse();
            response.setSuccess(false);
            response.setMessage("查询退款状态失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public boolean handleCallback(String callbackData) {
        try {
            logger.info("处理支付宝回调: {}", callbackData);
            
            // 在实际项目中，这里应该解析支付宝的回调数据
            // 验证签名，更新订单状态等
            
            return true;
            
        } catch (Exception e) {
            logger.error("处理支付宝回调失败", e);
            return false;
        }
    }
    
    @Override
    public String getPaymentMethod() {
        return "alipay";
    }
    
    /**
     * 生成签名
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
            
            // 在实际项目中，这里应该使用RSA私钥进行签名
            // 这里只是模拟签名过程
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
            logger.error("生成签名失败", e);
            return "";
        }
    }
    
    /**
     * 构建支付URL
     */
    private String buildPaymentUrl(Map<String, String> params) {
        try {
            StringBuilder sb = new StringBuilder(paymentConfig.getAlipay().getGatewayUrl());
            sb.append("?");
            
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (sb.charAt(sb.length() - 1) != '?') {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            logger.error("构建支付URL失败", e);
            return "";
        }
    }
    
    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "RF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               String.format("%06d", new Random().nextInt(1000000));
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
    
    /**
     * 模拟支付宝API调用
     */
    private String simulateAlipayAPI(Map<String, String> params) {
        // 模拟返回二维码内容
        return "https://qr.alipay.com/" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 模拟支付宝退款API调用
     */
    private String simulateAlipayRefundAPI(Map<String, String> params) {
        // 模拟返回退款单号
        return generateRefundNo();
    }
}