package com.carwash.controller;

import com.carwash.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import com.carwash.dto.BookingRequest;
import com.carwash.dto.BookingResponse;
import com.carwash.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * 预约订单控制器
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    /**
     * 创建预约订单
     */
    @PostMapping
    public Result<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        log.info("创建预约订单请求: {}", request);
        
        try {
            // 验证请求参数
            if (request.getUserId() == null) {
                log.warn("用户ID为空");
                return Result.error("用户ID不能为空");
            }
            if (request.getServiceId() == null) {
                log.warn("服务ID为空");
                return Result.error("服务ID不能为空");
            }
            
            Long bookingId = bookingService.createBooking(request);
            log.info("预约订单创建成功，订单ID: {}", bookingId);
            
            // 返回完整的订单信息
            BookingResponse booking = bookingService.getBookingById(bookingId);
            return Result.success(booking);
            
        } catch (Exception e) {
            log.error("创建预约订单失败", e);
            return Result.error("创建预约订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        log.info("获取用户订单列表，用户ID: {}", userId);
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        return Result.success(bookings);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{bookingId}")
    public Result<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        log.info("获取订单详情，订单ID: {}", bookingId);
        BookingResponse booking = bookingService.getBookingById(bookingId);
        return Result.success(booking);
    }

    /**
     * 根据订单号获取订单详情
     */
    @GetMapping("/order/{orderNo}")
    public Result<BookingResponse> getBookingByOrderNo(@PathVariable String orderNo) {
        log.info("根据订单号获取订单详情，订单号: {}", orderNo);
        BookingResponse booking = bookingService.getBookingByOrderNo(orderNo);
        return Result.success(booking);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{bookingId}/cancel")
    public Result<Void> cancelBooking(@PathVariable Long bookingId, @RequestParam String reason) {
        log.info("取消订单，订单ID: {}, 原因: {}", bookingId, reason);
        bookingService.cancelBooking(bookingId, reason);
        return Result.success();
    }

    /**
     * 删除订单（管理员）
     * 使用硬删除，完全从数据库中移除订单数据
     */
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBooking(@PathVariable Long bookingId) {
        log.info("删除订单请求，订单ID: {}", bookingId);
        
        try {
            bookingService.deleteBooking(bookingId);
            log.info("订单删除成功，订单ID: {}", bookingId);
            return Result.success("订单删除成功");
        } catch (Exception e) {
            log.error("删除订单失败，订单ID: {}", bookingId, e);
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }

    /**
     * 永久删除订单（管理员）
     * 完全从数据库中移除订单数据，此操作不可恢复
     */
    @DeleteMapping("/{bookingId}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> permanentlyDeleteBooking(@PathVariable Long bookingId) {
        log.info("永久删除订单请求，订单ID: {}", bookingId);
        
        try {
            bookingService.permanentlyDeleteBooking(bookingId);
            log.info("订单永久删除成功，订单ID: {}", bookingId);
            return Result.success("订单已永久删除");
        } catch (Exception e) {
            log.error("永久删除订单失败，订单ID: {}", bookingId, e);
            return Result.error("永久删除订单失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态（管理员）
     */
    @PutMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BookingResponse> updateBookingStatus(@PathVariable Long bookingId, @RequestParam String status) {
        log.info("更新订单状态，订单ID: {}, 状态: {}", bookingId, status);
        
        // 验证状态值
        if (!isValidStatus(status)) {
            log.warn("无效的订单状态: {}", status);
            return Result.error("无效的订单状态: " + status);
        }
        
        bookingService.updateBookingStatus(bookingId, status);
        
        // 返回更新后的订单信息
        BookingResponse updatedBooking = bookingService.getBookingById(bookingId);
        log.info("订单状态更新成功，订单ID: {}, 新状态: {}", bookingId, updatedBooking.getStatus());
        
        return Result.success(updatedBooking);
    }
    
    /**
     * 验证订单状态是否有效
     */
    private boolean isValidStatus(String status) {
        return status != null && (
            "pending".equals(status) ||
            "confirmed".equals(status) ||
            "in_progress".equals(status) ||
            "completed".equals(status) ||
            "cancelled".equals(status)
        );
    }

    /**
     * 获取所有订单（管理员）
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BookingResponse>> getAllBookings() {
        log.info("获取所有订单列表");
        List<BookingResponse> bookings = bookingService.getAllBookings();
        log.info("返回订单数量: {}", bookings.size());
        return Result.success(bookings);
    }
    
    /**
     * 数据同步验证接口（管理员）
     */
    @GetMapping("/admin/sync-check")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> syncCheck() {
        log.info("执行数据同步检查");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<BookingResponse> bookings = bookingService.getAllBookings();
            
            // 统计各状态的订单数量
            Map<String, Long> statusCount = bookings.stream()
                .collect(Collectors.groupingBy(
                    BookingResponse::getStatus,
                    Collectors.counting()
                ));
            
            result.put("totalBookings", bookings.size());
            result.put("statusDistribution", statusCount);
            result.put("lastSyncTime", LocalDateTime.now());
            result.put("syncStatus", "success");
            
            log.info("数据同步检查完成，总订单数: {}, 状态分布: {}", bookings.size(), statusCount);
            
        } catch (Exception e) {
            log.error("数据同步检查失败", e);
            result.put("syncStatus", "error");
            result.put("errorMessage", e.getMessage());
        }
        
        return Result.success(result);
    }
}