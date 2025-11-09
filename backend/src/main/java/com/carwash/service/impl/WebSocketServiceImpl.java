package com.carwash.service.impl;

import com.carwash.service.WebSocketService;
import com.carwash.utils.TimeUtils;
import com.carwash.websocket.OrderStatusWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * WebSocket服务实现类
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    @Autowired
    private OrderStatusWebSocketHandler orderStatusWebSocketHandler;

    @Override
    public void pushOrderStatusUpdate(Long userId, Long orderId, String orderNo, String oldStatus, String newStatus, String updateReason) {
        try {
            log.info("推送订单状态更新: userId={}, orderId={}, {} -> {}", userId, orderId, oldStatus, newStatus);
            
            OrderStatusWebSocketHandler.OrderStatusUpdateMessage message = 
                new OrderStatusWebSocketHandler.OrderStatusUpdateMessage(
                    orderId,
                    orderNo,
                    oldStatus,
                    newStatus,
                    TimeUtils.formatDateTime(TimeUtils.now()),
                    updateReason
                );
            
            orderStatusWebSocketHandler.pushOrderStatusUpdate(userId, message);
            
        } catch (Exception e) {
            log.error("推送订单状态更新失败", e);
        }
    }

    @Override
    public int getActiveConnectionCount() {
        return orderStatusWebSocketHandler.getActiveConnectionCount();
    }
}