package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时间段实体类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("time_slots")
public class TimeSlot {

    /**
     * 时间段ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    @TableField("date")
    private LocalDate date;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalTime endTime;

    /**
     * 最大预约数
     */
    @TableField("max_bookings")
    private Integer maxBookings;

    /**
     * 当前预约数
     */
    @TableField("current_bookings")
    private Integer currentBookings;

    /**
     * 状态：0-不可用, 1-可用
     */
    @TableField("status")
    private Integer status;

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

    // Getter methods
    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Integer getMaxBookings() {
        return maxBookings;
    }

    public Integer getCurrentBookings() {
        return currentBookings;
    }

    public Integer getStatus() {
        return status;
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

    // Setter methods
    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setMaxBookings(Integer maxBookings) {
        this.maxBookings = maxBookings;
    }

    public void setCurrentBookings(Integer currentBookings) {
        this.currentBookings = currentBookings;
    }

    public void setStatus(Integer status) {
        this.status = status;
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