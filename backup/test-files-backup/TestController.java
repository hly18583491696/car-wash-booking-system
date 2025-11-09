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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器 - 用于调试数据库连接和订单创建问题
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

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
    @GetMapping("/db")
    public Result<Map<String, Object>> testDatabase() {
        log.info("测试数据库连接");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试用户表
            List<User> users = userMapper.selectList(null);
            result.put("users_count", users.size());
            result.put("users", users);
            
            // 测试服务表
            List<Service> services = serviceMapper.selectList(null);
            result.put("services_count", services.size());
            result.put("services", services);
            
            // 测试时间段表
            List<TimeSlot> timeSlots = timeSlotMapper.selectList(null);
            result.put("time_slots_count", timeSlots.size());
            result.put("time_slots", timeSlots);
            
            // 测试订单表
            List<Booking> bookings = bookingMapper.selectList(null);
            result.put("bookings_count", bookings.size());
            result.put("bookings", bookings);
            
            result.put("status", "success");
            result.put("message", "数据库连接正常");
            
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            result.put("status", "error");
            result.put("message", "数据库连接失败: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return Result.success(result);
    }

    /**
     * 测试创建订单
     */
    @PostMapping("/booking")
    public Result<Map<String, Object>> testCreateBooking() {
        log.info("测试创建订单");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建测试订单请求
            BookingRequest request = new BookingRequest();
            request.setUserId(2L); // 使用user001用户
            request.setServiceId(1L); // 使用基础洗车服务
            request.setTimeSlotId(1L); // 使用第一个时间段
            request.setBookingDate(LocalDate.of(2025, 9, 22));
            request.setCarNumber("京A12345");
            request.setCarModel("丰田卡罗拉");
            request.setContactPhone("13800138001");
            request.setNotes("测试订单");
            
            log.info("测试订单请求: {}", request);
            
            // 调用服务创建订单
            Long bookingId = bookingService.createBooking(request);
            
            result.put("status", "success");
            result.put("message", "订单创建成功");
            result.put("booking_id", bookingId);
            
            // 查询创建的订单
            Booking booking = bookingMapper.selectById(bookingId);
            result.put("booking", booking);
            
        } catch (Exception e) {
            log.error("创建订单测试失败", e);
            result.put("status", "error");
            result.put("message", "创建订单失败: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return Result.success(result);
    }

    /**
     * 测试直接插入订单
     */
    @PostMapping("/direct-insert")
    public Result<Map<String, Object>> testDirectInsert() {
        log.info("测试直接插入订单");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建订单对象
            Booking booking = new Booking();
            booking.setOrderNo("TEST" + System.currentTimeMillis());
            booking.setUserId(2L);
            booking.setServiceId(1L);
            booking.setTimeSlotId(1L);
            booking.setBookingDate(LocalDate.of(2025, 9, 22));
            booking.setCarNumber("京B67890");
            booking.setCarModel("本田雅阁");
            booking.setContactPhone("13800138002");
            booking.setNotes("直接插入测试");
            booking.setTotalPrice(new BigDecimal("30.00"));
            booking.setStatus("pending");
            booking.setPaymentStatus("unpaid");
            booking.setDeleted(0);
            
            log.info("准备插入的订单: {}", booking);
            
            // 直接插入
            int insertResult = bookingMapper.insert(booking);
            
            result.put("status", "success");
            result.put("message", "直接插入成功");
            result.put("insert_result", insertResult);
            result.put("booking_id", booking.getId());
            result.put("booking", booking);
            
        } catch (Exception e) {
            log.error("直接插入订单失败", e);
            result.put("status", "error");
            result.put("message", "直接插入失败: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return Result.success(result);
    }
    
    /**
     * 清理测试数据
     */
    @DeleteMapping("/cleanup")
    public Result<String> cleanup() {
        log.info("清理测试数据");
        
        try {
            // 删除测试订单
            bookingMapper.delete(null);
            return Result.success("测试数据清理成功");
        } catch (Exception e) {
            log.error("清理测试数据失败", e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }
}