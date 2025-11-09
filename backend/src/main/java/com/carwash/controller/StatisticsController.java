package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.StatisticsResponse;
import com.carwash.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计控制器
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取系统统计概览
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsResponse> getOverview() {
        log.info("获取系统统计概览");
        StatisticsResponse statistics = statisticsService.getOverview();
        return Result.success(statistics);
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsResponse> getTodayStatistics() {
        log.info("获取今日统计");
        StatisticsResponse statistics = statisticsService.getTodayStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取月度统计
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsResponse> getMonthlyStatistics() {
        log.info("获取月度统计");
        StatisticsResponse statistics = statisticsService.getMonthlyStatistics();
        return Result.success(statistics);
    }
}