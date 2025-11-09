package com.carwash.service;

/**
 * WebSocket服务接口
 * 用于处理实时消息推送
 */
public interface WebSocketService {
    
    /**
     * 推送订单状态更新
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param updateReason 更新原因
     */
    void pushOrderStatusUpdate(Long userId, Long orderId, String orderNo, String oldStatus, String newStatus, String updateReason);
    
    /**
     * 获取当前WebSocket连接数
     * @return 连接数
     */
    int getActiveConnectionCount();
}