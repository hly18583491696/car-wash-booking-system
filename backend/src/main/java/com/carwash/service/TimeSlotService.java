package com.carwash.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carwash.entity.TimeSlot;

import java.time.LocalDate;
import java.util.List;

/**
 * 时间段服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface TimeSlotService {

    /**
     * 创建时间段
     */
    Long createTimeSlot(TimeSlot timeSlot);

    /**
     * 获取指定日期的时间段列表
     */
    List<TimeSlot> getTimeSlotsByDate(LocalDate date);

    /**
     * 获取可用的时间段列表
     */
    List<TimeSlot> getAvailableTimeSlots(LocalDate date);

    /**
     * 获取所有时间段（分页）
     */
    IPage<TimeSlot> getAllTimeSlots(Page<TimeSlot> page);

    /**
     * 更新时间段状态
     */
    void updateTimeSlotStatus(Long timeSlotId, Integer status);

    /**
     * 删除时间段（软删除）
     */
    void deleteTimeSlot(Long timeSlotId);

    /**
     * 永久删除时间段（硬删除）
     * 注意：如果时间段存在预约，将禁止删除
     */
    void permanentlyDeleteTimeSlot(Long timeSlotId);
}
