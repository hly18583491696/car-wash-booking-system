package com.carwash.mapper;

import com.carwash.entity.Booking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
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

    /**
     * 统计总订单数
     */
    @Select("SELECT COUNT(*) FROM bookings WHERE deleted = 0")
    int countTotalBookings();

    /**
     * 统计今日订单数
     */
    @Select("SELECT COUNT(*) FROM bookings WHERE DATE(created_at) = CURDATE() AND deleted = 0")
    int countTodayBookings();

    /**
     * 统计月度订单数
     */
    @Select("SELECT COUNT(*) FROM bookings WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE()) AND deleted = 0")
    int countMonthlyBookings();

    /**
     * 统计已完成订单数
     */
    @Select("SELECT COUNT(*) FROM bookings WHERE status = 'completed' AND deleted = 0")
    int countCompletedBookings();

    /**
     * 统计待处理订单数
     */
    @Select("SELECT COUNT(*) FROM bookings WHERE status = 'pending' AND deleted = 0")
    int countPendingBookings();

    /**
     * 获取总收入
     */
    @Select("SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE status = 'completed' AND deleted = 0")
    BigDecimal getTotalRevenue();

    /**
     * 获取今日收入
     */
    @Select("SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE DATE(created_at) = CURDATE() AND status = 'completed' AND deleted = 0")
    BigDecimal getTodayRevenue();

    /**
     * 获取月度收入
     */
    @Select("SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE()) AND status = 'completed' AND deleted = 0")
    BigDecimal getMonthlyRevenue();
}