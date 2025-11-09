package com.carwash.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付配置类
 * 管理微信支付和支付宝的配置信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {
    
    private WechatPay wechat = new WechatPay();
    private Alipay alipay = new Alipay();
    private Security security = new Security();
    
    public WechatPay getWechat() {
        return wechat;
    }
    
    public void setWechat(WechatPay wechat) {
        this.wechat = wechat;
    }
    
    public Alipay getAlipay() {
        return alipay;
    }
    
    public void setAlipay(Alipay alipay) {
        this.alipay = alipay;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }
    
    /**
     * 微信支付配置
     */
    public static class WechatPay {
        private String appId;
        private String mchId;
        private String apiKey;
        private String certPath;
        private String notifyUrl;
        private String returnUrl;
        
        public String getAppId() {
            return appId;
        }
        
        public void setAppId(String appId) {
            this.appId = appId;
        }
        
        public String getMchId() {
            return mchId;
        }
        
        public void setMchId(String mchId) {
            this.mchId = mchId;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getCertPath() {
            return certPath;
        }
        
        public void setCertPath(String certPath) {
            this.certPath = certPath;
        }
        
        public String getNotifyUrl() {
            return notifyUrl;
        }
        
        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }
        
        public String getReturnUrl() {
            return returnUrl;
        }
        
        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }
    }
    
    /**
     * 支付宝配置
     */
    public static class Alipay {
        private String appId;
        private String privateKey;
        private String publicKey;
        private String alipayPublicKey;
        private String notifyUrl;
        private String returnUrl;
        private String signType = "RSA2";
        private String charset = "UTF-8";
        private String format = "json";
        private String gatewayUrl = "https://openapi.alipay.com/gateway.do";
        
        public String getAppId() {
            return appId;
        }
        
        public void setAppId(String appId) {
            this.appId = appId;
        }
        
        public String getPrivateKey() {
            return privateKey;
        }
        
        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
        
        public String getPublicKey() {
            return publicKey;
        }
        
        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }
        
        public String getAlipayPublicKey() {
            return alipayPublicKey;
        }
        
        public void setAlipayPublicKey(String alipayPublicKey) {
            this.alipayPublicKey = alipayPublicKey;
        }
        
        public String getNotifyUrl() {
            return notifyUrl;
        }
        
        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }
        
        public String getReturnUrl() {
            return returnUrl;
        }
        
        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }
        
        public String getSignType() {
            return signType;
        }
        
        public void setSignType(String signType) {
            this.signType = signType;
        }
        
        public String getCharset() {
            return charset;
        }
        
        public void setCharset(String charset) {
            this.charset = charset;
        }
        
        public String getFormat() {
            return format;
        }
        
        public void setFormat(String format) {
            this.format = format;
        }
        
        public String getGatewayUrl() {
            return gatewayUrl;
        }
        
        public void setGatewayUrl(String gatewayUrl) {
            this.gatewayUrl = gatewayUrl;
        }
    }

    /**
     * 安全配置（RSA密钥等）
     */
    public static class Security {
        /** RSA公钥（用于前端加密） */
        private String rsaPublicKey;
        /** RSA私钥（用于后端解密） */
        private String rsaPrivateKey;

        public String getRsaPublicKey() {
            return rsaPublicKey;
        }

        public void setRsaPublicKey(String rsaPublicKey) {
            this.rsaPublicKey = rsaPublicKey;
        }

        public String getRsaPrivateKey() {
            return rsaPrivateKey;
        }

        public void setRsaPrivateKey(String rsaPrivateKey) {
            this.rsaPrivateKey = rsaPrivateKey;
        }
    }
}