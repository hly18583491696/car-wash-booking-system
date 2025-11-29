package com.carwash.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.carwash.entity.TimeSlot;
import com.carwash.mapper.TimeSlotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeSlotMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotMaintenanceService.class);

    private static final int EXPECTED_SLOTS_PER_DAY = 14;

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    @Autowired
    private TimeSlotService timeSlotService;

    @Scheduled(cron = "0 0 2 * * *")
    public void verifyAndSeedTimeSlotsDaily() {
        try {
            log.info("开始每日时间段校验与补全任务");
            for (int i = 0; i < 7; i++) {
                LocalDate date = LocalDate.now().plusDays(i);
                int count = countSlotsForDate(date);
                if (count < EXPECTED_SLOTS_PER_DAY) {
                    log.info("日期 {} 时间段不足，当前 {} / {}，开始补全", date, count, EXPECTED_SLOTS_PER_DAY);
                    seedSlotsForDate(date);
                } else {
                    log.info("日期 {} 时间段数量正常：{}", date, count);
                }
            }
            log.info("每日时间段校验与补全任务完成");
        } catch (Exception e) {
            log.error("每日时间段校验任务失败", e);
        }
    }

    private int countSlotsForDate(LocalDate date) {
        QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("date", date).eq("deleted", 0);
        return timeSlotMapper.selectCount(wrapper).intValue();
    }

    private void seedSlotsForDate(LocalDate date) {
        List<TimeSlot> toCreate = generateStandardSlots(date);
        for (TimeSlot slot : toCreate) {
            // 防重复：查询是否存在相同时间段
            QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
            wrapper.eq("date", slot.getDate())
                   .eq("start_time", slot.getStartTime())
                   .eq("end_time", slot.getEndTime())
                   .eq("deleted", 0);
            TimeSlot existing = timeSlotMapper.selectOne(wrapper);
            if (existing == null) {
                try {
                    timeSlotService.createTimeSlot(slot);
                    log.info("已创建时间段: {} {}-{}", slot.getDate(), slot.getStartTime(), slot.getEndTime());
                } catch (Exception e) {
                    log.error("创建时间段失败: {} {}-{}", slot.getDate(), slot.getStartTime(), slot.getEndTime(), e);
                }
            }
        }
    }

    private List<TimeSlot> generateStandardSlots(LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        // 上午
        add(slots, date, LocalTime.of(9, 0), LocalTime.of(9, 30));
        add(slots, date, LocalTime.of(9, 30), LocalTime.of(10, 0));
        add(slots, date, LocalTime.of(10, 0), LocalTime.of(10, 30));
        add(slots, date, LocalTime.of(10, 30), LocalTime.of(11, 0));
        add(slots, date, LocalTime.of(11, 0), LocalTime.of(11, 30));
        add(slots, date, LocalTime.of(11, 30), LocalTime.of(12, 0));
        // 下午
        add(slots, date, LocalTime.of(14, 0), LocalTime.of(14, 30));
        add(slots, date, LocalTime.of(14, 30), LocalTime.of(15, 0));
        add(slots, date, LocalTime.of(15, 0), LocalTime.of(15, 30));
        add(slots, date, LocalTime.of(15, 30), LocalTime.of(16, 0));
        add(slots, date, LocalTime.of(16, 0), LocalTime.of(16, 30));
        add(slots, date, LocalTime.of(16, 30), LocalTime.of(17, 0));
        add(slots, date, LocalTime.of(17, 0), LocalTime.of(17, 30));
        add(slots, date, LocalTime.of(17, 30), LocalTime.of(18, 0));
        return slots;
    }

    private void add(List<TimeSlot> list, LocalDate date, LocalTime start, LocalTime end) {
        TimeSlot slot = new TimeSlot();
        slot.setDate(date);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setMaxBookings(3);
        slot.setCurrentBookings(0);
        slot.setStatus(1);
        slot.setDeleted(0);
        list.add(slot);
    }
}

