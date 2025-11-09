package com.carwash.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约响应DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class BookingResponse {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 服务ID
     */
    private Long serviceId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 时间段ID
     */
    private Long timeSlotId;

    /**
     * 预约日期
     */
    private LocalDate bookingDate;

    /**
     * 预约时间
     */
    private String bookingTime;

    /**
     * 车牌号
     */
    private String carNumber;

    /**
     * 车型
     */
    private String carModel;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 支付状态
     */
    private String paymentStatus;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 取消时间
     */
    private LocalDateTime cancelledAt;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    public String getStatus() {
        return status;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}