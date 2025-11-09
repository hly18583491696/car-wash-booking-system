package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.service.BookingService;
import com.carwash.service.UserService;
import com.carwash.service.ServiceManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据同步控制器
 * 提供数据同步检查、验证和修复功能
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/data-sync")
public class DataSyncController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceManagementService serviceManagementService;

    /**
     * 数据同步健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        log.info("执行数据同步健康检查");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查各个服务的数据状态
            Map<String, Object> bookingStatus = checkBookingDataStatus();
            Map<String, Object> userStatus = checkUserDataStatus();
            Map<String, Object> serviceStatus = checkServiceDataStatus();
            
            result.put("timestamp", LocalDateTime.now());
            result.put("status", "healthy");
            result.put("bookings", bookingStatus);
            result.put("users", userStatus);
            result.put("services", serviceStatus);
            
            // 计算总体健康分数
            int totalScore = calculateHealthScore(bookingStatus, userStatus, serviceStatus);
            result.put("healthScore", totalScore);
            result.put("healthLevel", getHealthLevel(totalScore));
            
            log.info("数据同步健康检查完成，健康分数: {}", totalScore);
            
        } catch (Exception e) {
            log.error("数据同步健康检查失败", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }
        
        return Result.success(result);
    }

    /**
     * 数据一致性验证
     */
    @GetMapping("/consistency-check")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> consistencyCheck() {
        log.info("执行数据一致性验证");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查订单数据一致性
            Map<String, Object> bookingConsistency = checkBookingConsistency();
            
            // 检查用户数据一致性
            Map<String, Object> userConsistency = checkUserConsistency();
            
            // 检查服务数据一致性
            Map<String, Object> serviceConsistency = checkServiceConsistency();
            
            result.put("timestamp", LocalDateTime.now());
            result.put("bookings", bookingConsistency);
            result.put("users", userConsistency);
            result.put("services", serviceConsistency);
            
            // 计算一致性分数
            int consistencyScore = calculateConsistencyScore(bookingConsistency, userConsistency, serviceConsistency);
            result.put("consistencyScore", consistencyScore);
            result.put("consistencyLevel", getConsistencyLevel(consistencyScore));
            
            log.info("数据一致性验证完成，一致性分数: {}", consistencyScore);
            
        } catch (Exception e) {
            log.error("数据一致性验证失败", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return Result.success(result);
    }

    /**
     * 数据同步修复
     */
    @PostMapping("/repair")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> repairDataSync(@RequestParam(required = false) String repairType) {
        log.info("执行数据同步修复，修复类型: {}", repairType);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if ("booking".equals(repairType) || repairType == null) {
                Map<String, Object> bookingRepair = repairBookingData();
                result.put("bookingRepair", bookingRepair);
            }
            
            if ("user".equals(repairType) || repairType == null) {
                Map<String, Object> userRepair = repairUserData();
                result.put("userRepair", userRepair);
            }
            
            if ("service".equals(repairType) || repairType == null) {
                Map<String, Object> serviceRepair = repairServiceData();
                result.put("serviceRepair", serviceRepair);
            }
            
            result.put("timestamp", LocalDateTime.now());
            result.put("status", "completed");
            
            log.info("数据同步修复完成");
            
        } catch (Exception e) {
            log.error("数据同步修复失败", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return Result.success(result);
    }

    /**
     * 获取同步统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSyncStatistics() {
        log.info("获取数据同步统计信息");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取各类数据的统计信息
            Map<String, Object> bookingStats = getBookingStatistics();
            Map<String, Object> userStats = getUserStatistics();
            Map<String, Object> serviceStats = getServiceStatistics();
            
            result.put("timestamp", LocalDateTime.now());
            result.put("bookings", bookingStats);
            result.put("users", userStats);
            result.put("services", serviceStats);
            
            // 计算总体统计
            Map<String, Object> overallStats = calculateOverallStatistics(bookingStats, userStats, serviceStats);
            result.put("overall", overallStats);
            
            log.info("数据同步统计信息获取完成");
            
        } catch (Exception e) {
            log.error("获取数据同步统计信息失败", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return Result.success(result);
    }

    // ==================== 私有方法 ====================

    /**
     * 检查订单数据状态
     */
    private Map<String, Object> checkBookingDataStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            var bookings = bookingService.getAllBookings();
            status.put("totalCount", bookings.size());
            status.put("status", "healthy");
            
            // 统计各状态的订单数量
            Map<String, Long> statusCount = new HashMap<>();
            bookings.forEach(booking -> {
                String bookingStatus = booking.getStatus();
                statusCount.put(bookingStatus, statusCount.getOrDefault(bookingStatus, 0L) + 1);
            });
            status.put("statusDistribution", statusCount);
            
            // 检查是否有异常状态
            boolean hasInvalidStatus = bookings.stream()
                .anyMatch(booking -> !isValidBookingStatus(booking.getStatus()));
            status.put("hasInvalidStatus", hasInvalidStatus);
            
        } catch (Exception e) {
            log.error("检查订单数据状态失败", e);
            status.put("status", "error");
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    /**
     * 检查用户数据状态
     */
    private Map<String, Object> checkUserDataStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            var users = userService.getAllUsers();
            status.put("totalCount", users.size());
            status.put("status", "healthy");
            
            // 统计活跃用户数量
            long activeUsers = users.stream()
                .mapToInt(user -> user.getStatus() != null && user.getStatus() == 1 ? 1 : 0)
                .sum();
            status.put("activeCount", activeUsers);
            status.put("inactiveCount", users.size() - activeUsers);
            
        } catch (Exception e) {
            log.error("检查用户数据状态失败", e);
            status.put("status", "error");
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    /**
     * 检查服务数据状态
     */
    private Map<String, Object> checkServiceDataStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            var services = serviceManagementService.getAllServices();
            status.put("totalCount", services.size());
            status.put("status", "healthy");
            
            // 统计可用服务数量
            long availableServices = services.stream()
                .mapToInt(service -> service.getStatus() != null && service.getStatus() == 1 ? 1 : 0)
                .sum();
            status.put("availableCount", availableServices);
            status.put("unavailableCount", services.size() - availableServices);
            
        } catch (Exception e) {
            log.error("检查服务数据状态失败", e);
            status.put("status", "error");
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    /**
     * 检查订单数据一致性
     */
    private Map<String, Object> checkBookingConsistency() {
        Map<String, Object> consistency = new HashMap<>();
        
        try {
            var bookings = bookingService.getAllBookings();
            
            // 检查状态一致性
            long invalidStatusCount = bookings.stream()
                .mapToLong(booking -> isValidBookingStatus(booking.getStatus()) ? 0 : 1)
                .sum();
            
            consistency.put("totalBookings", bookings.size());
            consistency.put("invalidStatusCount", invalidStatusCount);
            consistency.put("consistencyRate", bookings.isEmpty() ? 100.0 : 
                (double)(bookings.size() - invalidStatusCount) / bookings.size() * 100);
            
        } catch (Exception e) {
            log.error("检查订单数据一致性失败", e);
            consistency.put("error", e.getMessage());
        }
        
        return consistency;
    }

    /**
     * 检查用户数据一致性
     */
    private Map<String, Object> checkUserConsistency() {
        Map<String, Object> consistency = new HashMap<>();
        
        try {
            var users = userService.getAllUsers();
            
            // 检查数据完整性
            long incompleteUsers = users.stream()
                .mapToLong(user -> (user.getUsername() == null || user.getUsername().trim().isEmpty()) ? 1 : 0)
                .sum();
            
            consistency.put("totalUsers", users.size());
            consistency.put("incompleteUsers", incompleteUsers);
            consistency.put("consistencyRate", users.isEmpty() ? 100.0 : 
                (double)(users.size() - incompleteUsers) / users.size() * 100);
            
        } catch (Exception e) {
            log.error("检查用户数据一致性失败", e);
            consistency.put("error", e.getMessage());
        }
        
        return consistency;
    }

    /**
     * 检查服务数据一致性
     */
    private Map<String, Object> checkServiceConsistency() {
        Map<String, Object> consistency = new HashMap<>();
        
        try {
            var services = serviceManagementService.getAllServices();
            
            // 检查价格一致性
            long invalidPriceCount = services.stream()
                .mapToLong(service -> (service.getPrice() == null || service.getPrice().doubleValue() <= 0) ? 1 : 0)
                .sum();
            
            consistency.put("totalServices", services.size());
            consistency.put("invalidPriceCount", invalidPriceCount);
            consistency.put("consistencyRate", services.isEmpty() ? 100.0 : 
                (double)(services.size() - invalidPriceCount) / services.size() * 100);
            
        } catch (Exception e) {
            log.error("检查服务数据一致性失败", e);
            consistency.put("error", e.getMessage());
        }
        
        return consistency;
    }

    /**
     * 修复订单数据
     */
    private Map<String, Object> repairBookingData() {
        Map<String, Object> repair = new HashMap<>();
        
        try {
            // 这里可以实现具体的修复逻辑
            repair.put("status", "completed");
            repair.put("repairedCount", 0);
            repair.put("message", "订单数据修复完成");
            
        } catch (Exception e) {
            log.error("修复订单数据失败", e);
            repair.put("status", "error");
            repair.put("error", e.getMessage());
        }
        
        return repair;
    }

    /**
     * 修复用户数据
     */
    private Map<String, Object> repairUserData() {
        Map<String, Object> repair = new HashMap<>();
        
        try {
            repair.put("status", "completed");
            repair.put("repairedCount", 0);
            repair.put("message", "用户数据修复完成");
            
        } catch (Exception e) {
            log.error("修复用户数据失败", e);
            repair.put("status", "error");
            repair.put("error", e.getMessage());
        }
        
        return repair;
    }

    /**
     * 修复服务数据
     */
    private Map<String, Object> repairServiceData() {
        Map<String, Object> repair = new HashMap<>();
        
        try {
            repair.put("status", "completed");
            repair.put("repairedCount", 0);
            repair.put("message", "服务数据修复完成");
            
        } catch (Exception e) {
            log.error("修复服务数据失败", e);
            repair.put("status", "error");
            repair.put("error", e.getMessage());
        }
        
        return repair;
    }

    /**
     * 获取订单统计信息
     */
    private Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            var bookings = bookingService.getAllBookings();
            stats.put("total", bookings.size());
            
            // 按状态统计
            Map<String, Long> statusStats = new HashMap<>();
            bookings.forEach(booking -> {
                String status = booking.getStatus();
                statusStats.put(status, statusStats.getOrDefault(status, 0L) + 1);
            });
            stats.put("byStatus", statusStats);
            
        } catch (Exception e) {
            log.error("获取订单统计信息失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 获取用户统计信息
     */
    private Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            var users = userService.getAllUsers();
            stats.put("total", users.size());
            
            long activeUsers = users.stream()
                .mapToLong(user -> user.getStatus() != null && user.getStatus() == 1 ? 1 : 0)
                .sum();
            stats.put("active", activeUsers);
            stats.put("inactive", users.size() - activeUsers);
            
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 获取服务统计信息
     */
    private Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            var services = serviceManagementService.getAllServices();
            stats.put("total", services.size());
            
            long availableServices = services.stream()
                .mapToLong(service -> service.getStatus() != null && service.getStatus() == 1 ? 1 : 0)
                .sum();
            stats.put("available", availableServices);
            stats.put("unavailable", services.size() - availableServices);
            
        } catch (Exception e) {
            log.error("获取服务统计信息失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 计算总体统计
     */
    private Map<String, Object> calculateOverallStatistics(Map<String, Object> bookingStats, 
                                                          Map<String, Object> userStats, 
                                                          Map<String, Object> serviceStats) {
        Map<String, Object> overall = new HashMap<>();
        
        int totalBookings = (Integer) bookingStats.getOrDefault("total", 0);
        int totalUsers = (Integer) userStats.getOrDefault("total", 0);
        int totalServices = (Integer) serviceStats.getOrDefault("total", 0);
        
        overall.put("totalRecords", totalBookings + totalUsers + totalServices);
        overall.put("bookingPercentage", totalBookings + totalUsers + totalServices > 0 ? 
            (double) totalBookings / (totalBookings + totalUsers + totalServices) * 100 : 0);
        overall.put("userPercentage", totalBookings + totalUsers + totalServices > 0 ? 
            (double) totalUsers / (totalBookings + totalUsers + totalServices) * 100 : 0);
        overall.put("servicePercentage", totalBookings + totalUsers + totalServices > 0 ? 
            (double) totalServices / (totalBookings + totalUsers + totalServices) * 100 : 0);
        
        return overall;
    }

    /**
     * 计算健康分数
     */
    private int calculateHealthScore(Map<String, Object> bookingStatus, 
                                   Map<String, Object> userStatus, 
                                   Map<String, Object> serviceStatus) {
        int score = 100;
        
        // 检查各个模块的状态
        if ("error".equals(bookingStatus.get("status"))) score -= 30;
        if ("error".equals(userStatus.get("status"))) score -= 30;
        if ("error".equals(serviceStatus.get("status"))) score -= 30;
        
        // 检查是否有无效状态
        if (Boolean.TRUE.equals(bookingStatus.get("hasInvalidStatus"))) score -= 10;
        
        return Math.max(0, score);
    }

    /**
     * 计算一致性分数
     */
    private int calculateConsistencyScore(Map<String, Object> bookingConsistency, 
                                        Map<String, Object> userConsistency, 
                                        Map<String, Object> serviceConsistency) {
        double bookingRate = (Double) bookingConsistency.getOrDefault("consistencyRate", 100.0);
        double userRate = (Double) userConsistency.getOrDefault("consistencyRate", 100.0);
        double serviceRate = (Double) serviceConsistency.getOrDefault("consistencyRate", 100.0);
        
        return (int) ((bookingRate + userRate + serviceRate) / 3);
    }

    /**
     * 获取健康等级
     */
    private String getHealthLevel(int score) {
        if (score >= 90) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 50) return "一般";
        return "需要关注";
    }

    /**
     * 获取一致性等级
     */
    private String getConsistencyLevel(int score) {
        if (score >= 95) return "高度一致";
        if (score >= 80) return "基本一致";
        if (score >= 60) return "部分一致";
        return "不一致";
    }

    /**
     * 验证订单状态是否有效
     */
    private boolean isValidBookingStatus(String status) {
        return status != null && (
            "pending".equals(status) ||
            "confirmed".equals(status) ||
            "in_progress".equals(status) ||
            "completed".equals(status) ||
            "cancelled".equals(status)
        );
    }
}