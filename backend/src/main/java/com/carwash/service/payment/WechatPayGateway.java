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
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 微信支付网关实现
 */@Slf4j
@Service
public class WechatPayGateway implements PaymentGateway {

    @Autowired
    private PaymentConfig paymentConfig;
    
    private static final Logger logger = LoggerFactory.getLogger(WechatPayGateway.class);
    
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            logger.info("创建微信支付订单: {}", request.getOrderNo());
            
            // 构建支付参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", paymentConfig.getWechat().getAppId());
        params.put("mch_id", paymentConfig.getWechat().getMchId());
            params.put("nonce_str", generateNonceStr());
            params.put("body", "洗车服务-" + request.getOrderNo());
            params.put("out_trade_no", request.getOrderNo());
            params.put("total_fee", String.valueOf(request.getAmount().multiply(new BigDecimal("100")).intValue()));
            params.put("spbill_create_ip", "127.0.0.1");
            params.put("notify_url", paymentConfig.getWechat().getNotifyUrl());
            params.put("trade_type", "NATIVE"); // 扫码支付
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 模拟调用微信支付API
            // 在实际项目中，这里应该调用微信支付的统一下单接口
            String codeUrl = simulateWechatPayAPI(params);
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setPaymentNo(request.getOrderNo());
            response.setPaymentUrl(codeUrl);
            response.setQrCode(codeUrl);
            response.setMessage("支付订单创建成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("创建微信支付订单失败", e);
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("创建支付订单失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public PaymentResponse queryPayment(String paymentNo) {
        try {
            logger.info("查询微信支付状态: {}", paymentNo);
            
            // 模拟查询支付状态
            // 在实际项目中，这里应该调用微信支付的查询订单接口
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setPaymentNo(paymentNo);
            response.setStatus("paid"); // 模拟已支付状态
            response.setMessage("查询成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("查询微信支付状态失败", e);
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("查询支付状态失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            logger.info("申请微信支付退款: {}", request.getPaymentNo());
            
            // 构建退款参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", paymentConfig.getWechat().getAppId());
        params.put("mch_id", paymentConfig.getWechat().getMchId());
            params.put("nonce_str", generateNonceStr());
            params.put("out_trade_no", request.getPaymentNo());
            params.put("out_refund_no", generateRefundNo());
            params.put("total_fee", String.valueOf(request.getAmount().multiply(new BigDecimal("100")).intValue()));
            params.put("refund_fee", String.valueOf(request.getAmount().multiply(new BigDecimal("100")).intValue()));
            params.put("refund_desc", request.getReason());
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 模拟调用微信支付退款API
            // 在实际项目中，这里应该调用微信支付的申请退款接口
            String refundNo = simulateWechatRefundAPI(params);
            
            RefundResponse response = new RefundResponse();
            response.setSuccess(true);
            response.setRefundNo(refundNo);
            response.setMessage("退款申请成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("申请微信支付退款失败", e);
            RefundResponse response = new RefundResponse();
            response.setSuccess(false);
            response.setMessage("申请退款失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public RefundResponse queryRefund(String refundNo) {
        try {
            logger.info("查询微信支付退款状态: {}", refundNo);
            
            // 模拟查询退款状态
            RefundResponse response = new RefundResponse();
            response.setSuccess(true);
            response.setRefundNo(refundNo);
            response.setStatus("success");
            response.setMessage("查询成功");
            
            return response;
            
        } catch (Exception e) {
            logger.error("查询微信支付退款状态失败", e);
            RefundResponse response = new RefundResponse();
            response.setSuccess(false);
            response.setMessage("查询退款状态失败: " + e.getMessage());
            return response;
        }
    }
    
    @Override
    public boolean handleCallback(String callbackData) {
        try {
            logger.info("处理微信支付回调: {}", callbackData);
            
            // 在实际项目中，这里应该解析微信支付的回调数据
            // 验证签名，更新订单状态等
            
            return true;
            
        } catch (Exception e) {
            logger.error("处理微信支付回调失败", e);
            return false;
        }
    }
    
    @Override
    public String getPaymentMethod() {
        return "wechat";
    }
    
    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
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
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            sb.append("key=").append(paymentConfig.getWechat().getApiKey());
            
            // MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
            
        } catch (Exception e) {
            logger.error("生成签名失败", e);
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
     * 模拟微信支付API调用
     */
    private String simulateWechatPayAPI(Map<String, String> params) {
        // 模拟返回二维码链接
        return "weixin://wxpay/bizpayurl?pr=" + generateNonceStr();
    }
    
    /**
     * 模拟微信退款API调用
     */
    private String simulateWechatRefundAPI(Map<String, String> params) {
        // 模拟返回退款单号
        return params.get("out_refund_no");
    }
}