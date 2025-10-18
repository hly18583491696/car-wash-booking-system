package com.carwash.mapper;

import com.carwash.entity.Booking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约订单数据访问层
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    /**
     * 分页查询用户订单
     */
    @Select("SELECT * FROM bookings WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    IPage<Booking> selectUserBookings(Page<Booking> page, @Param("userId") Long userId);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM bookings WHERE order_no = #{orderNo} AND deleted = 0")
    Booking selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询指定日期的订单
     */
    @Select("SELECT * FROM bookings WHERE booking_date = #{date} AND deleted = 0 ORDER BY booking_time ASC")
    List<Booking> selectByDate(@Param("date") LocalDate date);

    /**
     * 查询指定时间段的订单数量
     */
    @Select("SELECT COUNT(*) FROM bookings WHERE time_slot_id = #{timeSlotId} AND status NOT IN ('cancelled') AND deleted = 0")
    int countByTimeSlot(@Param("timeSlotId") Long timeSlotId);

    /**
     * 更新订单状态
     */
    @Update("UPDATE bookings SET status = #{status} WHERE id = #{bookingId}")
    int updateStatus(@Param("bookingId") Long bookingId, @Param("status") String status);

    /**
     * 更新支付状态
     */
    @Update("UPDATE bookings SET payment_status = #{paymentStatus} WHERE id = #{bookingId}")
    int updatePaymentStatus(@Param("bookingId") Long bookingId, @Param("paymentStatus") String paymentStatus);
}