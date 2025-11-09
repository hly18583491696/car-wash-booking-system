package com.carwash.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 数据统计响应DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class StatisticsResponse {

    /**
     * 总用户数
     */
    private Integer totalUsers;

    /**
     * 今日新增用户数
     */
    private Integer todayNewUsers;

    /**
     * 总服务数
     */
    private Integer totalServices;

    /**
     * 可用服务数
     */
    private Integer availableServices;

    /**
     * 总订单数
     */
    private Integer totalBookings;

    /**
     * 今日订单数
     */
    private Integer todayBookings;

    /**
     * 已完成订单数
     */
    private Integer completedBookings;

    /**
     * 待处理订单数
     */
    private Integer pendingBookings;

    /**
     * 总收入
     */
    private BigDecimal totalRevenue;

    /**
     * 今日收入
     */
    private BigDecimal todayRevenue;

    // Setter methods
    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public void setTodayNewUsers(Integer todayNewUsers) {
        this.todayNewUsers = todayNewUsers;
    }

    public void setTotalServices(Integer totalServices) {
        this.totalServices = totalServices;
    }

    public void setAvailableServices(Integer availableServices) {
        this.availableServices = availableServices;
    }

    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }

    public void setTodayBookings(Integer todayBookings) {
        this.todayBookings = todayBookings;
    }

    public void setCompletedBookings(Integer completedBookings) {
        this.completedBookings = completedBookings;
    }

    public void setPendingBookings(Integer pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    // Getter methods
    public Integer getTotalUsers() {
        return totalUsers;
    }

    public Integer getTodayNewUsers() {
        return todayNewUsers;
    }

    public Integer getTotalServices() {
        return totalServices;
    }

    public Integer getAvailableServices() {
        return availableServices;
    }

    public Integer getTotalBookings() {
        return totalBookings;
    }

    public Integer getTodayBookings() {
        return todayBookings;
    }

    public Integer getCompletedBookings() {
        return completedBookings;
    }

    public Integer getPendingBookings() {
        return pendingBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }
}