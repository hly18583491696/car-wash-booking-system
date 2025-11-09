package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("payments")
public class Payment {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 支付流水号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 第三方支付平台交易号
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付方式：wechat-微信支付, alipay-支付宝, unionpay-银联支付
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付状态：pending-待支付, paid-已支付, failed-支付失败, cancelled-已取消, refunded-已退款
     */
    @TableField("status")
    private String status;

    /**
     * 支付时间
     */
    @TableField("paid_at")
    private LocalDateTime paidAt;

    /**
     * 支付过期时间
     */
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /**
     * 支付回调通知次数
     */
    @TableField("notify_count")
    private Integer notifyCount;

    /**
     * 支付描述
     */
    @TableField("description")
    private String description;

    /**
     * 支付平台返回的原始数据
     */
    @TableField("raw_data")
    private String rawData;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // Getter methods
    public String getStatus() {
        return status;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public void setNotifyCount(int notifyCount) {
        this.notifyCount = notifyCount;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
}