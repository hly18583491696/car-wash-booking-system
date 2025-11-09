package com.carwash.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知配置类
 * 用于管理各种通知功能的开关和配置
 */
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationConfig {

    /**
     * 短信通知配置
     */
    private SmsConfig sms = new SmsConfig();

    /**
     * 邮件通知配置
     */
    private EmailConfig email = new EmailConfig();

    /**
     * 微信通知配置
     */
    private WechatConfig wechat = new WechatConfig();

    /**
     * 应用内通知配置
     */
    private InAppConfig inApp = new InAppConfig();

    public SmsConfig getSms() {
        return sms;
    }

    public void setSms(SmsConfig sms) {
        this.sms = sms;
    }

    public EmailConfig getEmail() {
        return email;
    }

    public void setEmail(EmailConfig email) {
        this.email = email;
    }

    public WechatConfig getWechat() {
        return wechat;
    }

    public void setWechat(WechatConfig wechat) {
        this.wechat = wechat;
    }

    public InAppConfig getInApp() {
        return inApp;
    }

    public void setInApp(InAppConfig inApp) {
        this.inApp = inApp;
    }

    /**
     * 短信通知配置
     */
    public static class SmsConfig {
        /**
         * 是否启用短信通知
         */
        private boolean enabled = true;

        /**
         * 是否启用订单状态变更短信通知
         */
        private boolean orderStatusEnabled = true;

        /**
         * 是否启用验证码短信
         */
        private boolean verificationEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isOrderStatusEnabled() {
            return orderStatusEnabled;
        }

        public void setOrderStatusEnabled(boolean orderStatusEnabled) {
            this.orderStatusEnabled = orderStatusEnabled;
        }

        public boolean isVerificationEnabled() {
            return verificationEnabled;
        }

        public void setVerificationEnabled(boolean verificationEnabled) {
            this.verificationEnabled = verificationEnabled;
        }
    }

    /**
     * 邮件通知配置
     */
    public static class EmailConfig {
        /**
         * 是否启用邮件通知
         */
        private boolean enabled = false;

        /**
         * 是否启用订单状态变更邮件通知
         */
        private boolean orderStatusEnabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isOrderStatusEnabled() {
            return orderStatusEnabled;
        }

        public void setOrderStatusEnabled(boolean orderStatusEnabled) {
            this.orderStatusEnabled = orderStatusEnabled;
        }
    }

    /**
     * 微信通知配置
     */
    public static class WechatConfig {
        /**
         * 是否启用微信通知
         */
        private boolean enabled = false;

        /**
         * 是否启用订单状态变更微信通知
         */
        private boolean orderStatusEnabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isOrderStatusEnabled() {
            return orderStatusEnabled;
        }

        public void setOrderStatusEnabled(boolean orderStatusEnabled) {
            this.orderStatusEnabled = orderStatusEnabled;
        }
    }

    /**
     * 应用内通知配置
     */
    public static class InAppConfig {
        /**
         * 是否启用应用内通知
         */
        private boolean enabled = true;

        /**
         * 是否启用订单状态变更应用内通知
         */
        private boolean orderStatusEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isOrderStatusEnabled() {
            return orderStatusEnabled;
        }

        public void setOrderStatusEnabled(boolean orderStatusEnabled) {
            this.orderStatusEnabled = orderStatusEnabled;
        }
    }
}