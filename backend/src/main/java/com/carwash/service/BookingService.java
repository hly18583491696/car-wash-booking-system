package com.carwash.service;

import com.carwash.dto.BookingRequest;
import com.carwash.dto.BookingResponse;

import java.util.List;

/**
 * 预约订单服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface BookingService {

    /**
     * 创建预约订单
     */
    Long createBooking(BookingRequest request);

    /**
     * 获取用户订单列表
     */
    List<BookingResponse> getUserBookings(Long userId);

    /**
     * 获取订单详情
     */
    BookingResponse getBookingById(Long bookingId);

    /**
     * 根据订单号获取订单详情
     */
    BookingResponse getBookingByOrderNo(String orderNo);

    /**
     * 取消订单
     */
    void cancelBooking(Long bookingId, String reason);

    /**
     * 更新订单状态
     */
    void updateBookingStatus(Long bookingId, String status);

    /**
     * 删除订单（硬删除）
     */
    void deleteBooking(Long bookingId);

    /**
     * 获取所有订单（管理员）
     */
    List<BookingResponse> getAllBookings();
}