package com.carwash.service.impl;

import com.carwash.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    
    // Redis key前缀
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";

    @Override
    public boolean sendRegisterCode(String phone) {
        return sendCode(phone, "register");
    }

    @Override
    public boolean sendLoginCode(String phone) {
        return sendCode(phone, "login");
    }

    @Override
    public boolean sendResetPasswordCode(String phone) {
        return sendCode(phone, "reset_password");
    }

    @Override
    public boolean verifyCode(String phone, String code, String type) {
        String key = SMS_CODE_PREFIX + type + ":" + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            log.warn("验证码不存在或已过期，手机号: {}, 类型: {}", phone, type);
            return false;
        }
        
        if (!storedCode.equals(code)) {
            log.warn("验证码错误，手机号: {}, 类型: {}, 输入: {}, 正确: {}", phone, type, code, storedCode);
            return false;
        }
        
        log.info("验证码验证成功，手机号: {}, 类型: {}", phone, type);
        return true;
    }

    @Override
    public void clearCode(String phone, String type) {
        String key = SMS_CODE_PREFIX + type + ":" + phone;
        redisTemplate.delete(key);
        log.info("验证码已清除，手机号: {}, 类型: {}", phone, type);
    }

    /**
     * 发送验证码
     */
    private boolean sendCode(String phone, String type) {
        // 检查发送频率限制
        if (!checkSendLimit(phone)) {
            log.warn("发送验证码过于频繁，手机号: {}", phone);
            return false;
        }

        // 生成验证码
        String code = generateCode();
        
        // 存储验证码到Redis
        String key = SMS_CODE_PREFIX + type + ":" + phone;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 设置发送频率限制
        setSendLimit(phone);
        
        // 模拟发送短信（实际项目中应该调用短信服务商API）
        boolean success = simulateSendSms(phone, code, type);
        
        if (success) {
            log.info("验证码发送成功，手机号: {}, 类型: {}, 验证码: {}", phone, type, code);
        } else {
            log.error("验证码发送失败，手机号: {}, 类型: {}", phone, type);
            // 发送失败时清除Redis中的验证码
            redisTemplate.delete(key);
        }
        
        return success;
    }

    /**
     * 生成验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 检查发送频率限制
     */
    private boolean checkSendLimit(String phone) {
        String limitKey = SMS_LIMIT_PREFIX + phone;
        String limit = redisTemplate.opsForValue().get(limitKey);
        return limit == null;
    }

    /**
     * 设置发送频率限制（1分钟内只能发送一次）
     */
    private void setSendLimit(String phone) {
        String limitKey = SMS_LIMIT_PREFIX + phone;
        redisTemplate.opsForValue().set(limitKey, "1", 1, TimeUnit.MINUTES);
    }

    /**
     * 模拟发送短信
     * 实际项目中应该调用阿里云、腾讯云等短信服务商的API
     */
    private boolean simulateSendSms(String phone, String code, String type) {
        try {
            // 模拟网络延迟
            Thread.sleep(500);
            
            String message = getMessageTemplate(type, code);
            log.info("【模拟短信】发送到 {}: {}", phone, message);
            
            // 模拟99%的成功率
            return Math.random() < 0.99;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean sendOrderStatusNotification(String phone, String orderNo, String oldStatus, String newStatus, String customerName) {
        try {
            String message = buildOrderStatusMessage(orderNo, oldStatus, newStatus, customerName);
            return sendNotificationSms(phone, message);
        } catch (Exception e) {
            log.error("发送订单状态变更短信失败: phone={}, orderNo={}, error={}", phone, orderNo, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendNotificationSms(String phone, String message) {
        try {
            log.info("【模拟短信】发送通知到 {}: {}", phone, message);
            
            // 模拟99%的成功率
            return Math.random() < 0.99;
        } catch (Exception e) {
            log.error("发送通知短信失败: phone={}, message={}, error={}", phone, message, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建订单状态变更消息
     */
    private String buildOrderStatusMessage(String orderNo, String oldStatus, String newStatus, String customerName) {
        // 状态中文映射
        String oldStatusText = getStatusText(oldStatus);
        String newStatusText = getStatusText(newStatus);
        
        StringBuilder message = new StringBuilder();
        message.append("【洗车服务】");
        
        if (customerName != null && !customerName.trim().isEmpty()) {
            message.append(customerName).append("，");
        }
        
        message.append("您的订单").append(orderNo);
        message.append("状态已从").append(oldStatusText);
        message.append("变更为").append(newStatusText);
        
        // 根据新状态添加相应的提示信息
        switch (newStatus) {
            case "confirmed":
                message.append("，我们将按时为您提供服务，请保持手机畅通。");
                break;
            case "in_progress":
                message.append("，技师正在为您的爱车提供专业服务。");
                break;
            case "completed":
                message.append("，感谢您的信任，欢迎再次使用我们的服务！");
                break;
            case "cancelled":
                message.append("，如有疑问请联系客服。");
                break;
            case "refunded":
                message.append("，退款将在3-5个工作日内到账。");
                break;
            default:
                message.append("。");
                break;
        }
        
        return message.toString();
    }

    /**
     * 获取状态中文文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "待确认";
            case "confirmed":
                return "已确认";
            case "in_progress":
                return "服务中";
            case "completed":
                return "已完成";
            case "cancelled":
                return "已取消";
            case "refunded":
                return "已退款";
            default:
                return status;
        }
    }

    /**
     * 获取短信模板
     */
    private String getMessageTemplate(String type, String code) {
        switch (type) {
            case "register":
                return String.format("【洗车服务】您的注册验证码是：%s，有效期5分钟，请勿泄露给他人。", code);
            case "login":
                return String.format("【洗车服务】您的登录验证码是：%s，有效期5分钟，请勿泄露给他人。", code);
            case "reset_password":
                return String.format("【洗车服务】您的密码重置验证码是：%s，有效期5分钟，请勿泄露给他人。", code);
            default:
                return String.format("【洗车服务】您的验证码是：%s，有效期5分钟，请勿泄露给他人。", code);
        }
    }
}