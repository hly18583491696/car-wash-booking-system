package com.carwash.common.enums;

/**
 * 预约状态枚举
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public enum AppointmentStatus {
    
    PENDING("PENDING", "待确认"),
    CONFIRMED("CONFIRMED", "已确认"),
    IN_PROGRESS("IN_PROGRESS", "服务中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    AppointmentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static AppointmentStatus getByCode(String code) {
        for (AppointmentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}