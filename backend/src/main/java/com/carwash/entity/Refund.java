package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("refunds")
public class Refund {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付记录ID
     */
    @TableField("payment_id")
    private Long paymentId;

    /**
     * 退款单号
     */
    @TableField("refund_no")
    private String refundNo;

    /**
     * 第三方平台退款单号
     */
    @TableField("refund_id")
    private String refundId;

    /**
     * 退款金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 退款原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 退款状态：pending-退款中, success-退款成功, failed-退款失败
     */
    @TableField("status")
    private String status;

    /**
     * 退款时间
     */
    @TableField("refunded_at")
    private LocalDateTime refundedAt;

    /**
     * 退款操作员ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 退款平台返回的原始数据
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
     * 是否删除：0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // Setter methods
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    // Getter methods
    public String getRefundNo() {
        return refundNo;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public Long getOperatorId() {
        return operatorId;
    }
}