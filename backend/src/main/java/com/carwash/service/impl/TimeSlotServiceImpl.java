package com.carwash.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carwash.common.BusinessException;
import com.carwash.common.result.ResultCode;
import com.carwash.entity.Booking;
import com.carwash.entity.TimeSlot;
import com.carwash.mapper.BookingMapper;
import com.carwash.mapper.TimeSlotMapper;
import com.carwash.service.TimeSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 时间段服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotServiceImpl.class);

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTimeSlot(TimeSlot timeSlot) {
        log.info("创建时间段开始");
        
        int result = timeSlotMapper.insert(timeSlot);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建时间段失败");
        }
        
        log.info("时间段创建成功，时间段ID: {}", timeSlot.getId());
        return timeSlot.getId();
    }

    @Override
    public List<TimeSlot> getTimeSlotsByDate(LocalDate date) {
        QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("date", date).eq("deleted", 0);
        return timeSlotMapper.selectList(wrapper);
    }

    @Override
    public List<TimeSlot> getAvailableTimeSlots(LocalDate date) {
        QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("date", date)
               .eq("status", 1)
               .eq("deleted", 0)
               .apply("current_bookings < max_bookings");
        return timeSlotMapper.selectList(wrapper);
    }

    @Override
    public IPage<TimeSlot> getAllTimeSlots(Page<TimeSlot> page) {
        QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).orderByAsc("date", "start_time");
        return timeSlotMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTimeSlotStatus(Long timeSlotId, Integer status) {
        log.info("更新时间段状态，时间段ID: {}, 状态: {}", timeSlotId, status);
        
        TimeSlot timeSlot = timeSlotMapper.selectById(timeSlotId);
        if (timeSlot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间段不存在");
        }
        
        timeSlot.setStatus(status);
        int result = timeSlotMapper.updateById(timeSlot);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新时间段状态失败");
        }
        
        log.info("时间段状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTimeSlot(Long timeSlotId) {
        log.info("删除时间段（软删除），时间段ID: {}", timeSlotId);
        
        TimeSlot timeSlot = timeSlotMapper.selectById(timeSlotId);
        if (timeSlot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间段不存在");
        }
        
        // 软删除
        timeSlot.setDeleted(1);
        int result = timeSlotMapper.updateById(timeSlot);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除时间段失败");
        }
        
        log.info("时间段删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteTimeSlot(Long timeSlotId) {
        log.info("永久删除时间段（硬删除），时间段ID: {}", timeSlotId);

        // 验证时间段ID
        if (timeSlotId == null) {
            log.error("时间段ID为空");
            throw new BusinessException(ResultCode.PARAM_ERROR, "时间段ID不能为空");
        }

        // 检查时间段是否存在
        TimeSlot timeSlot = timeSlotMapper.selectById(timeSlotId);
        if (timeSlot == null) {
            log.error("时间段不存在，时间段ID: {}", timeSlotId);
            throw new BusinessException(ResultCode.NOT_FOUND, "时间段不存在");
        }

        log.info("时间段删除前状态 - 时间段ID: {}, 日期: {}, deleted: {}, 状态: {}", 
            timeSlotId, timeSlot.getDate(), timeSlot.getDeleted(), timeSlot.getStatus());

        // 检查时间段是否被预约引用
        QueryWrapper<Booking> bookingWrapper = new QueryWrapper<>();
        bookingWrapper.eq("time_slot_id", timeSlotId).eq("deleted", 0);
        long bookingCount = bookingMapper.selectCount(bookingWrapper);
        if (bookingCount > 0) {
            log.error("时间段存在预约，无法删除，时间段ID: {}, 预约数量: {}", timeSlotId, bookingCount);
            throw new BusinessException(ResultCode.CANNOT_DELETE, 
                String.format("时间段存在 %d 个预约，无法删除", bookingCount));
        }

        // 执行硬删除，从数据库中完全移除记录
        log.info("执行数据库删除操作");
        int result = timeSlotMapper.deleteById(timeSlotId);
        log.info("数据库删除结果: {}", result);
        
        if (result <= 0) {
            log.error("硬删除时间段失败，数据库操作失败，时间段ID: {}, 影响行数: {}", timeSlotId, result);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "硬删除时间段失败，数据库操作失败");
        }

        // 验证删除结果，确认记录不再存在
        log.info("验证数据库删除结果");
        TimeSlot deletedTimeSlot = timeSlotMapper.selectById(timeSlotId);
        
        if (deletedTimeSlot != null) {
            log.error("验证失败：删除后时间段仍然存在，时间段ID: {}", timeSlotId);
            
            // 尝试重新删除一次
            log.info("尝试重新删除时间段");
            int retryResult = timeSlotMapper.deleteById(timeSlotId);
            
            if (retryResult <= 0) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "重试硬删除时间段失败");
            }
            
            // 再次验证
            TimeSlot finalTimeSlot = timeSlotMapper.selectById(timeSlotId);
            if (finalTimeSlot != null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, 
                    "数据库删除验证失败，记录仍然存在");
            }
        }

        log.info("时间段硬删除成功，时间段ID: {}, 记录已从数据库中完全移除", timeSlotId);
    }
}
