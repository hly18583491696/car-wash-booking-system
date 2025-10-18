package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约订单实体类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bookings")
public class Booking {

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 服务ID
     */
    @TableField("service_id")
    private Long serviceId;

    /**
     * 时间段ID
     */
    @TableField("time_slot_id")
    private Long timeSlotId;

    /**
     * 预约日期
     */
    @TableField("booking_date")
    private LocalDate bookingDate;

    /**
     * 预约时间
     */
    @TableField("booking_time")
    private String bookingTime;

    /**
     * 车牌号
     */
    @TableField("car_number")
    private String carNumber;

    /**
     * 车型
     */
    @TableField("car_model")
    private String carModel;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 备注信息
     */
    @TableField("notes")
    private String notes;

    /**
     * 总价格
     */
    @TableField("total_price")
    private BigDecimal totalPrice;

    /**
     * 订单状态：pending-待确认, confirmed-已确认, in_progress-进行中, completed-已完成, cancelled-已取消
     */
    @TableField("status")
    private String status;

    /**
     * 支付状态：unpaid-未支付, paid-已支付, refunded-已退款
     */
    @TableField("payment_status")
    private String paymentStatus;

    /**
     * 支付方式
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付时间
     */
    @TableField("paid_at")
    private LocalDateTime paidAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 取消时间
     */
    @TableField("cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

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
}