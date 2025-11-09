package com.carwash.service;

import java.util.Map;

/**
 * 通知服务接口
 * 支持短信、邮件、站内消息等多种通知方式
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface NotificationService {

    /**
     * 发送订单状态变更短信通知
     * 
     * @param phone 手机号
     * @param orderNo 订单号
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param customerName 客户姓名
     * @return 是否发送成功
     */
    boolean sendOrderStatusSms(String phone, String orderNo, String oldStatus, String newStatus, String customerName);

    /**
     * 发送通用短信通知
     * 
     * @param phone 手机号
     * @param template 短信模板
     * @param params 模板参数
     * @return 是否发送成功
     */
    boolean sendSms(String phone, String template, Map<String, Object> params);

    /**
     * 发送邮件通知
     * 
     * @param email 邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 是否发送成功
     */
    boolean sendEmail(String email, String subject, String content);

    /**
     * 发送站内消息
     * 
     * @param userId 用户ID
     * @param title 消息标题
     * @param content 消息内容
     * @return 是否发送成功
     */
    boolean sendInAppMessage(Long userId, String title, String content);

    /**
     * 发送微信消息（预留接口）
     * 
     * @param openId 微信OpenID
     * @param templateId 模板ID
     * @param data 模板数据
     * @return 是否发送成功
     */
    boolean sendWechatMessage(String openId, String templateId, Map<String, Object> data);
}