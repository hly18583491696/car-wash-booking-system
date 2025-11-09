package com.carwash.service;

/**
 * 短信服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface SmsService {

    /**
     * 发送注册验证码
     * 
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendRegisterCode(String phone);

    /**
     * 发送登录验证码
     * 
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendLoginCode(String phone);

    /**
     * 发送重置密码验证码
     * 
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendResetPasswordCode(String phone);

    /**
     * 验证验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @param type 验证码类型 (register, login, reset_password)
     * @return 是否验证成功
     */
    boolean verifyCode(String phone, String code, String type);

    /**
     * 清除验证码
     * 
     * @param phone 手机号
     * @param type 验证码类型
     */
    void clearCode(String phone, String type);

    /**
     * 发送订单状态变更通知短信
     * 
     * @param phone 手机号
     * @param orderNo 订单号
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param customerName 客户姓名
     * @return 是否发送成功
     */
    boolean sendOrderStatusNotification(String phone, String orderNo, String oldStatus, String newStatus, String customerName);

    /**
     * 发送通用通知短信
     * 
     * @param phone 手机号
     * @param message 短信内容
     * @return 是否发送成功
     */
    boolean sendNotificationSms(String phone, String message);
}