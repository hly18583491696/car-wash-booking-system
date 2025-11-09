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
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // Getter methods
    public Long getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    // Additional getter methods
    public Long getId() {
        return id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    // Additional setter methods
    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}