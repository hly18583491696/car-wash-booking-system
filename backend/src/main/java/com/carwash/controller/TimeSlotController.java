package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.entity.TimeSlot;
import com.carwash.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@Tag(name = "时间段", description = "预约时间段相关接口")
public class TimeSlotController {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotController.class);

    @Autowired
    private TimeSlotService timeSlotService;

    @GetMapping("/available")
    @Operation(summary = "获取可用时间段", description = "根据日期获取可用的预约时间段列表")
    public Result<List<TimeSlot>> getAvailable(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        log.info("查询可用时间段，日期: {}", localDate);
        List<TimeSlot> slots = timeSlotService.getAvailableTimeSlots(localDate);
        return Result.success(slots);
    }
}

