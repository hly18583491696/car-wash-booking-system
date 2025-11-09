package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付审计日志实体
 */
@Data
@TableName("payment_audit")
public class PaymentAudit {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付流水号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 事件类型：CREATE、CALL_GATEWAY、QUERY_STATUS、STATUS_CHANGE、CALLBACK_VERIFIED、CALLBACK_UPDATE、REFUND_REQUEST、REFUND_SUCCESS、REFUND_FAILED、CANCEL_EXPIRED
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 当前状态
     */
    @TableField("status")
    private String status;

    /**
     * 支付方式/网关
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 金额（可选）
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 操作人ID（可选）
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 信息摘要（可选）
     */
    @TableField("message")
    private String message;

    /**
     * 原始数据（可选）
     */
    @TableField("raw_data")
    private String rawData;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}