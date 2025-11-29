package com.carwash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carwash.entity.BookingStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单状态变更日志 Mapper 接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface BookingStatusLogMapper extends BaseMapper<BookingStatusLog> {

    /**
     * 根据订单ID查询状态变更日志
     * 
     * @param bookingId 订单ID
     * @return 状态变更日志列表
     */
    @Select("SELECT * FROM booking_status_log WHERE booking_id = #{bookingId} ORDER BY update_time DESC")
    List<BookingStatusLog> selectByBookingId(Long bookingId);

    /**
     * 查询最近的状态变更日志
     * 
     * @param bookingId 订单ID
     * @param limit 限制数量
     * @return 状态变更日志列表
     */
    @Select("SELECT * FROM booking_status_log WHERE booking_id = #{bookingId} ORDER BY update_time DESC LIMIT #{limit}")
    List<BookingStatusLog> selectRecentByBookingId(Long bookingId, Integer limit);
}
