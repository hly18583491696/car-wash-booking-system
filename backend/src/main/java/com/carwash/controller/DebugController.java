package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.BookingRequest;
import com.carwash.entity.Booking;
import com.carwash.entity.Service;
import com.carwash.entity.TimeSlot;
import com.carwash.entity.User;
import com.carwash.mapper.BookingMapper;
import com.carwash.mapper.ServiceMapper;
import com.carwash.mapper.TimeSlotMapper;
import com.carwash.mapper.UserMapper;
import com.carwash.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调试控制器 - 用于测试数据库连接和订单创建
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private BookingMapper bookingMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ServiceMapper serviceMapper;
    
    @Autowired
    private TimeSlotMapper timeSlotMapper;
    
    @Autowired
    private BookingService bookingService;

    /**
     * 测试数据库连接
     */
    @GetMapping("/db-connection")
    public Result<Map<String, Object>> testDatabaseConnection() {
        log.info("测试数据库连接");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试用户表
            List<User> users = userMapper.selectList(null);
            result.put("users_count", users.size());
            
            // 测试服务表
            List<Service> services = serviceMapper.selectList(null);
            result.put("services_count", services.size());
            
            // 测试时间段表
            List<TimeSlot> timeSlots = timeSlotMapper.selectList(null);
            result.put("time_slots_count", timeSlots.size());
            
            // 测试订单表
            List<Booking> bookings = bookingMapper.selectList(null);
            result.put("bookings_count", bookings.size());
            
            result.put("status", "success");
            result.put("message", "数据库连接正常");
            result.put("timestamp", LocalDateTime.now());
            
            log.info("数据库连接测试成功: {}", result);
            
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            result.put("status", "error");
            result.put("message", "数据库连接失败: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return Result.success(result);
    }

    /**
     * 测试创建简单订单
     */
    @PostMapping("/create-simple-booking")
    public Result<Map<String, Object>> createSimpleBooking() {
        log.info("测试创建简单订单");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取第一个用户
            List<User> users = userMapper.selectList(null);
            if (users.isEmpty()) {
                return Result.error("没有找到用户数据");
            }
            User user = users.get(0);
            
            // 获取第一个服务
            List<Service> services = serviceMapper.selectList(null);
            if (services.isEmpty()) {
                return Result.error("没有找到服务数据");
            }
            Service service = services.get(0);
            
            // 创建订单请求
            BookingRequest request = new BookingRequest();
            request.setUserId(user.getId());
            request.setServiceId(service.getId());
            request.setBookingDate(LocalDate.now());
            request.setCarNumber("测试A12345");
            request.setCarModel("测试车型");
            request.setContactPhone("13800138000");
            request.setNotes("测试订单");
            
            log.info("测试订单请求: {}", request);
            
            // 调用服务创建订单
            Long bookingId = bookingService.createBooking(request);
            
            result.put("status", "success");
            result.put("message", "测试订单创建成功");
            result.put("booking_id", bookingId);
            result.put("user_id", user.getId());
            result.put("service_id", service.getId());
            result.put("timestamp", LocalDateTime.now());
            
            // 查询创建的订单
            Booking booking = bookingMapper.selectById(bookingId);
            result.put("booking", booking);
            
            log.info("测试订单创建成功: {}", result);
            
        } catch (Exception e) {
            log.error("创建测试订单失败", e);
            result.put("status", "error");
            result.put("message", "创建测试订单失败: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return Result.success(result);
    }

    /**
     * 测试用户预约创建
     */
    @PostMapping("/test-user-booking")
    public Result<Map<String, Object>> testUserBooking(@RequestBody Map<String, Object> params) {
        log.info("测试用户预约创建，参数: {}", params);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从参数中获取数据
            Long userId = Long.valueOf(params.get("userId").toString());
            Long serviceId = Long.valueOf(params.get("serviceId").toString());
            
            // 验证用户存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在，用户ID: " + userId);
            }
            
            // 验证服务存在
            Service service = serviceMapper.selectById(serviceId);
            if (service == null) {
                return Result.error("服务不存在，服务ID: " + serviceId);
            }
            
            // 创建订单请求
            BookingRequest request = new BookingRequest();
            request.setUserId(userId);
            request.setServiceId(serviceId);
            request.setBookingDate(LocalDate.now());
            request.setCarNumber(params.getOrDefault("carNumber", "默认A12345").toString());
            request.setCarModel(params.getOrDefault("carModel", "默认车型").toString());
            request.setContactPhone(params.getOrDefault("contactPhone", "13800138000").toString());
            request.setNotes(params.getOrDefault("notes", "用户预约").toString());
            
            log.info("用户预约请求: {}", request);
            
            // 调用服务创建订单
            Long bookingId = bookingService.createBooking(request);
            
            result.put("status", "success");
            result.put("message", "用户预约创建成功");
            result.put("booking_id", bookingId);
            result.put("user_name", user.getUsername());
            result.put("service_name", service.getName());
            result.put("timestamp", LocalDateTime.now());
            
            log.info("用户预约创建成功: {}", result);
            
        } catch (Exception e) {
            log.error("用户预约创建失败", e);
            result.put("status", "error");
            result.put("message", "用户预约创建失败: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return Result.success(result);
    }

    /**
     * 获取所有订单（用于调试）
     */
    @GetMapping("/all-bookings")
    public Result<List<Booking>> getAllBookingsForDebug() {
        log.info("获取所有订单用于调试");
        
        try {
            List<Booking> bookings = bookingMapper.selectList(null);
            log.info("找到订单数量: {}", bookings.size());
            return Result.success(bookings);
        } catch (Exception e) {
            log.error("获取订单失败", e);
            return Result.error("获取订单失败: " + e.getMessage());
        }
    }

    /**
     * 清理测试数据
     */
    @DeleteMapping("/cleanup-test-data")
    public Result<String> cleanupTestData() {
        log.info("清理测试数据");
        
        try {
            // 删除包含"测试"的订单
            List<Booking> allBookings = bookingMapper.selectList(null);
            int deletedCount = 0;
            
            for (Booking booking : allBookings) {
                if (booking.getNotes() != null && booking.getNotes().contains("测试")) {
                    bookingMapper.deleteById(booking.getId());
                    deletedCount++;
                }
            }
            
            log.info("清理完成，删除了 {} 条测试数据", deletedCount);
            return Result.success("清理完成，删除了 " + deletedCount + " 条测试数据");
            
        } catch (Exception e) {
            log.error("清理测试数据失败", e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        log.info("执行系统健康检查");
        
        Map<String, Object> healthData = new HashMap<>();
        
        try {
            // 系统基本信息
            healthData.put("status", "healthy");
            healthData.put("timestamp", LocalDateTime.now());
            healthData.put("service", "carwash-backend");
            healthData.put("version", "1.0.0");
            
            // 数据库连接检查
            try {
                List<User> users = userMapper.selectList(null);
                List<Service> services = serviceMapper.selectList(null);
                List<Booking> bookings = bookingMapper.selectList(null);
                
                Map<String, Object> database = new HashMap<>();
                database.put("status", "connected");
                database.put("description", "数据库连接正常");
                database.put("userCount", users.size());
                database.put("serviceCount", services.size());
                database.put("bookingCount", bookings.size());
                healthData.put("database", database);
            } catch (Exception e) {
                Map<String, Object> database = new HashMap<>();
                database.put("status", "error");
                database.put("description", "数据库连接失败: " + e.getMessage());
                healthData.put("database", database);
                log.error("数据库健康检查失败", e);
            }
            
            // JVM信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> jvm = new HashMap<>();
            jvm.put("totalMemory", runtime.totalMemory());
            jvm.put("freeMemory", runtime.freeMemory());
            jvm.put("maxMemory", runtime.maxMemory());
            jvm.put("processors", runtime.availableProcessors());
            healthData.put("jvm", jvm);
            
            return Result.success(healthData);
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("status", "error");
            errorData.put("message", e.getMessage());
            errorData.put("timestamp", LocalDateTime.now());
            return Result.error(500, "健康检查失败", errorData);
        }
    }
}