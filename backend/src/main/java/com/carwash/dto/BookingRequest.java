package com.carwash.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 预约请求DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class BookingRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 服务ID
     */
    private Long serviceId;

    /**
     * 时间段ID
     */
    private Long timeSlotId;

    /**
     * 预约日期
     */
    private LocalDate bookingDate;

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

    // Getter methods
    public Long getUserId() {
        return userId;
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

    // Setter methods
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
}