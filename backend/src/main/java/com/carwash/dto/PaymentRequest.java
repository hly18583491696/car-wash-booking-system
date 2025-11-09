package com.carwash.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * 支付请求DTO
 */
@Data
public class PaymentRequest {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    /**
     * 支付方式：wechat-微信支付, alipay-支付宝, credit_card-信用卡
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 支付渠道：qr-扫码支付, app-APP支付, h5-H5支付
     * 默认值为qr，便于兼容现有实现
     */
    @Pattern(regexp = "^(qr|app|h5)$", message = "支付渠道仅支持 qr/app/h5")
    private String channel = "qr";

    /**
     * 加密的敏感支付载荷（信用卡等），使用RSA-OAEP加密后的Base64，仅在credit_card方式下使用
     */
    private String securePayload;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 用户IP地址
     */
    private String clientIp;

    /**
     * 支付成功回调地址
     */
    private String returnUrl;

    /**
     * 支付异步通知地址
     */
    private String notifyUrl;

    // Getter methods
    public String getOrderNo() {
        return orderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getChannel() {
        return channel;
    }

    public String getSecurePayload() {
        return securePayload;
    }

    public String getDescription() {
        return description;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    // Setter methods
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setSecurePayload(String securePayload) {
        this.securePayload = securePayload;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}