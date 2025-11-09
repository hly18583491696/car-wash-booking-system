package com.carwash.service.impl;

import com.carwash.dto.StatisticsResponse;
import com.carwash.mapper.BookingMapper;
import com.carwash.mapper.ServiceMapper;
import com.carwash.mapper.UserMapper;
import com.carwash.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 数据统计服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ServiceMapper serviceMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Override
    public StatisticsResponse getOverview() {
        log.info("获取系统统计概览");

        StatisticsResponse response = new StatisticsResponse();
        
        // 用户统计
        response.setTotalUsers(userMapper.countTotalUsers());
        response.setTodayNewUsers(userMapper.countTodayNewUsers());
        
        // 服务统计
        response.setTotalServices(serviceMapper.countTotalServices());
        response.setAvailableServices(serviceMapper.countAvailableServices());
        
        // 订单统计
        response.setTotalBookings(bookingMapper.countTotalBookings());
        response.setTodayBookings(bookingMapper.countTodayBookings());
        response.setCompletedBookings(bookingMapper.countCompletedBookings());
        response.setPendingBookings(bookingMapper.countPendingBookings());
        
        // 收入统计
        BigDecimal totalRevenue = bookingMapper.getTotalRevenue();
        BigDecimal todayRevenue = bookingMapper.getTodayRevenue();
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        response.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        return response;
    }

    @Override
    public StatisticsResponse getTodayStatistics() {
        log.info("获取今日统计");

        StatisticsResponse response = new StatisticsResponse();
        
        response.setTodayNewUsers(userMapper.countTodayNewUsers());
        response.setTodayBookings(bookingMapper.countTodayBookings());
        
        BigDecimal todayRevenue = bookingMapper.getTodayRevenue();
        response.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        return response;
    }

    @Override
    public StatisticsResponse getMonthlyStatistics() {
        log.info("获取月度统计");

        StatisticsResponse response = new StatisticsResponse();
        
        response.setTotalUsers(userMapper.countMonthlyNewUsers());
        response.setTotalBookings(bookingMapper.countMonthlyBookings());
        
        BigDecimal monthlyRevenue = bookingMapper.getMonthlyRevenue();
        response.setTotalRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);

        return response;
    }
}