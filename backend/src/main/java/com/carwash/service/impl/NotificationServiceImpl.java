package com.carwash.service.impl;

import com.carwash.config.NotificationConfig;
import com.carwash.service.NotificationService;
import com.carwash.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private NotificationConfig notificationConfig;

    /**
     * 订单状态中文映射
     */
    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    
    static {
        STATUS_MAP.put("pending", "待确认");
        STATUS_MAP.put("confirmed", "已确认");
        STATUS_MAP.put("in_progress", "服务中");
        STATUS_MAP.put("completed", "已完成");
        STATUS_MAP.put("cancelled", "已取消");
        STATUS_MAP.put("refunded", "已退款");
    }

    @Override
    public boolean sendOrderStatusSms(String phone, String orderNo, String oldStatus, String newStatus, String customerName) {
        // 检查短信通知总开关
        if (!notificationConfig.getSms().isEnabled()) {
            log.info("短信通知功能已关闭，跳过发送，手机号: {}, 订单号: {}", phone, orderNo);
            return false;
        }
        
        // 检查订单状态变更短信通知开关
        if (!notificationConfig.getSms().isOrderStatusEnabled()) {
            log.info("订单状态变更短信通知已关闭，跳过发送，手机号: {}, 订单号: {}", phone, orderNo);
            return false;
        }
        
        log.info("发送订单状态变更短信通知，手机号: {}, 订单号: {}, 状态变更: {} -> {}", 
            phone, orderNo, oldStatus, newStatus);
        
        try {
            // 构建订单状态变更消息
            String message = buildOrderStatusMessage(orderNo, oldStatus, newStatus, customerName);
            
            // 发送短信
            return smsService.sendOrderStatusNotification(phone, orderNo, oldStatus, newStatus, customerName);
        } catch (Exception e) {
            log.error("发送订单状态变更短信失败，手机号: {}, 订单号: {}, 错误: {}", 
                phone, orderNo, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendSms(String phone, String template, Map<String, Object> params) {
        if (!notificationConfig.getSms().isEnabled()) {
            log.info("短信通知功能已关闭，跳过发送，手机号: {}", phone);
            return false;
        }

        try {
            String message = processTemplate(template, params);
            log.info("发送通用短信通知，手机号: {}, 消息: {}", phone, message);
            return smsService.sendNotificationSms(phone, message);
        } catch (Exception e) {
            log.error("发送短信失败，手机号: {}, 模板: {}, 错误: {}", phone, template, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendEmail(String email, String subject, String content) {
        if (!notificationConfig.getEmail().isEnabled()) {
            log.info("邮件通知功能已关闭，跳过发送，邮箱: {}", email);
            return false;
        }

        // TODO: 实现邮件发送功能
        log.info("发送邮件通知: email={}, subject={}", email, subject);
        return true;
    }

    @Override
    public boolean sendInAppMessage(Long userId, String title, String content) {
        if (!notificationConfig.getInApp().isEnabled()) {
            log.info("站内消息功能已关闭，跳过发送，用户ID: {}", userId);
            return false;
        }

        // TODO: 实现站内消息功能
        log.info("发送站内消息: userId={}, title={}", userId, title);
        return true;
    }

    @Override
    public boolean sendWechatMessage(String openId, String templateId, Map<String, Object> data) {
        if (!notificationConfig.getWechat().isEnabled()) {
            log.info("微信消息功能已关闭，跳过发送，OpenID: {}", openId);
            return false;
        }

        // TODO: 实现微信消息发送功能
        log.info("发送微信消息: openId={}, templateId={}", openId, templateId);
        return true;
    }

    /**
     * 构建订单状态变更消息
     */
    private String buildOrderStatusMessage(String orderNo, String oldStatus, String newStatus, String customerName) {
        StringBuilder message = new StringBuilder();
        message.append("【洗车服务】");
        
        if (customerName != null && !customerName.trim().isEmpty()) {
            message.append(customerName).append("，");
        }
        
        message.append("您的订单").append(orderNo);
        message.append("状态已从").append(oldStatus);
        message.append("变更为").append(newStatus);
        
        // 根据新状态添加相应的提示信息
        switch (newStatus) {
            case "已确认":
                message.append("，我们将按时为您提供服务，请保持手机畅通。");
                break;
            case "服务中":
                message.append("，技师正在为您的爱车提供专业服务。");
                break;
            case "已完成":
                message.append("，感谢您的信任，欢迎再次使用我们的服务！");
                break;
            case "已取消":
                message.append("，如有疑问请联系客服。");
                break;
            case "已退款":
                message.append("，退款将在3-5个工作日内到账。");
                break;
            default:
                message.append("。");
                break;
        }
        
        return message.toString();
    }

    /**
     * 处理模板消息
     */
    private String processTemplate(String template, Map<String, Object> params) {
        String message = template;
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                message = message.replace(placeholder, value);
            }
        }
        return message;
    }


}