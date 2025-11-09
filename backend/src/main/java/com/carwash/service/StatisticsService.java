package com.carwash.service;

import com.carwash.dto.StatisticsResponse;

/**
 * 数据统计服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface StatisticsService {

    /**
     * 获取系统统计概览
     */
    StatisticsResponse getOverview();

    /**
     * 获取今日统计
     */
    StatisticsResponse getTodayStatistics();

    /**
     * 获取月度统计
     */
    StatisticsResponse getMonthlyStatistics();
}