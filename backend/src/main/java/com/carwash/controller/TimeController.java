package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.TimeResponse;
import com.carwash.utils.TimeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 时间同步控制器
 * 提供前后端时间同步服务
 */
@RestController
@RequestMapping("/api/time")
public class TimeController {

    /**
     * 获取服务器当前时间
     * @return 时间信息
     */
    @GetMapping("/current")
    public Result<TimeResponse> getCurrentTime() {
        LocalDateTime now = TimeUtils.now();
        
        TimeResponse timeResponse = new TimeResponse(
            now,
            TimeUtils.toTimestamp(now),
            TimeUtils.formatDateTime(now),
            TimeUtils.formatToISO(now),
            TimeUtils.SYSTEM_ZONE.getId()
        );
        
        return Result.success(timeResponse);
    }

    /**
     * 获取服务器时区信息
     * @return 时区信息
     */
    @GetMapping("/timezone")
    public Result<Map<String, Object>> getTimezone() {
        Map<String, Object> timezoneInfo = new HashMap<>();
        timezoneInfo.put("timezone", TimeUtils.SYSTEM_ZONE.getId());
        timezoneInfo.put("offset", TimeUtils.SYSTEM_ZONE.getRules().getOffset(java.time.Instant.now()).getTotalSeconds());
        timezoneInfo.put("displayName", "中国标准时间");
        
        return Result.success(timezoneInfo);
    }

    /**
     * 时间服务健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", TimeUtils.toTimestamp(TimeUtils.now()));
        health.put("timezone", TimeUtils.SYSTEM_ZONE.getId());
        health.put("message", "时间服务运行正常");
        
        return Result.success(health);
    }
}