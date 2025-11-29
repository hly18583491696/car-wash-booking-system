package com.carwash.service;

import com.carwash.entity.BookingStatusLog;

import java.util.List;

/**
 * 订单状态变更日志服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface BookingStatusLogService {

    /**
     * 记录订单状态变更
     * 
     * @param bookingId 订单ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param updateReason 变更原因
     * @param operatorId 操作人ID
     * @param operatorType 操作人类型（user/admin/system）
     */
    void logStatusChange(Long bookingId, String oldStatus, String newStatus, 
                        String updateReason, Long operatorId, String operatorType);

    /**
     * 记录订单状态变更（带额外信息）
     * 
     * @param bookingId 订单ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param updateReason 变更原因
     * @param operatorId 操作人ID
     * @param operatorType 操作人类型
     * @param clientIp 客户端IP
     * @param userAgent 客户端信息
     * @param extraData 额外数据
     */
    void logStatusChangeWithDetails(Long bookingId, String oldStatus, String newStatus,
                                    String updateReason, Long operatorId, String operatorType,
                                    String clientIp, String userAgent, String extraData);

    /**
     * 查询订单的状态变更历史
     * 
     * @param bookingId 订单ID
     * @return 状态变更日志列表
     */
    List<BookingStatusLog> getStatusHistory(Long bookingId);

    /**
     * 查询订单的最近状态变更记录
     * 
     * @param bookingId 订单ID
     * @param limit 限制数量
     * @return 状态变更日志列表
     */
    List<BookingStatusLog> getRecentStatusHistory(Long bookingId, Integer limit);

    /**
     * 获取上一次的状态（用于回滚）
     * 
     * @param bookingId 订单ID
     * @return 上一次的状态，如果没有则返回null
     */
    String getPreviousStatus(Long bookingId);
}
