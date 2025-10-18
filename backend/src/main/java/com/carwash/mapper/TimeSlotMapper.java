package com.carwash.mapper;

import com.carwash.entity.TimeSlot;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 时间段数据访问层
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlot> {

    /**
     * 查询指定日期的可用时间段
     */
    @Select("SELECT * FROM time_slots WHERE date = #{date} AND status = 1 AND deleted = 0 ORDER BY start_time ASC")
    List<TimeSlot> selectAvailableByDate(@Param("date") LocalDate date);

    /**
     * 查询指定日期范围的时间段
     */
    @Select("SELECT * FROM time_slots WHERE date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY date ASC, start_time ASC")
    List<TimeSlot> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 更新时间段预约数量
     */
    @Update("UPDATE time_slots SET current_bookings = #{currentBookings} WHERE id = #{timeSlotId}")
    int updateCurrentBookings(@Param("timeSlotId") Long timeSlotId, @Param("currentBookings") Integer currentBookings);

    /**
     * 增加时间段预约数量
     */
    @Update("UPDATE time_slots SET current_bookings = current_bookings + 1 WHERE id = #{timeSlotId}")
    int incrementCurrentBookings(@Param("timeSlotId") Long timeSlotId);

    /**
     * 减少时间段预约数量
     */
    @Update("UPDATE time_slots SET current_bookings = current_bookings - 1 WHERE id = #{timeSlotId} AND current_bookings > 0")
    int decrementCurrentBookings(@Param("timeSlotId") Long timeSlotId);
}