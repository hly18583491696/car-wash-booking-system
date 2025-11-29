package com.carwash.service.impl;

import com.carwash.entity.BookingStatusLog;
import com.carwash.mapper.BookingStatusLogMapper;
import com.carwash.service.BookingStatusLogService;
import com.carwash.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单状态变更日志服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class BookingStatusLogServiceImpl implements BookingStatusLogService {

    private static final Logger log = LoggerFactory.getLogger(BookingStatusLogServiceImpl.class);

    @Autowired
    private BookingStatusLogMapper bookingStatusLogMapper;

    @Override
    public void logStatusChange(Long bookingId, String oldStatus, String newStatus,
                               String updateReason, Long operatorId, String operatorType) {
        logStatusChangeWithDetails(bookingId, oldStatus, newStatus, updateReason,
                                  operatorId, operatorType, null, null, null);
    }

    @Override
    public void logStatusChangeWithDetails(Long bookingId, String oldStatus, String newStatus,
                                          String updateReason, Long operatorId, String operatorType,
                                          String clientIp, String userAgent, String extraData) {
        try {
            BookingStatusLog statusLog = BookingStatusLog.builder()
                    .bookingId(bookingId)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .updateReason(updateReason)
                    .operatorId(operatorId)
                    .operatorType(operatorType)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .extraData(extraData)
                    .updateTime(TimeUtils.now())
                    .build();

            bookingStatusLogMapper.insert(statusLog);
            log.info("订单状态变更日志记录成功: bookingId={}, {} -> {}", bookingId, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("记录订单状态变更日志失败: bookingId={}", bookingId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public List<BookingStatusLog> getStatusHistory(Long bookingId) {
        try {
            return bookingStatusLogMapper.selectByBookingId(bookingId);
        } catch (Exception e) {
            log.error("查询订单状态历史失败: bookingId={}", bookingId, e);
            return List.of();
        }
    }

    @Override
    public List<BookingStatusLog> getRecentStatusHistory(Long bookingId, Integer limit) {
        try {
            return bookingStatusLogMapper.selectRecentByBookingId(bookingId, limit);
        } catch (Exception e) {
            log.error("查询订单最近状态历史失败: bookingId={}", bookingId, e);
            return List.of();
        }
    }

    @Override
    public String getPreviousStatus(Long bookingId) {
        try {
            List<BookingStatusLog> recentLogs = getRecentStatusHistory(bookingId, 2);
            // 如果有至少2条记录，返回第二条的new_status（即上一次的状态）
            if (recentLogs.size() >= 2) {
                return recentLogs.get(1).getNewStatus();
            }
            // 如果只有1条记录，返回old_status（即初始状态）
            if (recentLogs.size() == 1) {
                return recentLogs.get(0).getOldStatus();
            }
            return null;
        } catch (Exception e) {
            log.error("获取上一次状态失败: bookingId={}", bookingId, e);
            return null;
        }
    }
}
