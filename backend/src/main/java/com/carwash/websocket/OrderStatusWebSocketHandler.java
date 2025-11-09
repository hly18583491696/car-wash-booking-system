package com.carwash.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单状态WebSocket处理器
 * 用于实时推送订单状态变更
 */
@Component
public class OrderStatusWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储用户ID与WebSocket会话的映射
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // 存储会话ID与用户ID的映射
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立: {}", session.getId());
        
        // 从查询参数中获取用户ID
        String userId = getQueryParam(session, "userId");
        if (userId != null) {
            try {
                Long userIdLong = Long.parseLong(userId);
                userSessions.put(userIdLong, session);
                sessionUsers.put(session.getId(), userIdLong);
                log.info("用户 {} 的WebSocket连接已建立", userIdLong);
                
                // 发送连接成功消息
                sendMessage(session, new WebSocketMessage("connection", "connected", "WebSocket连接成功"));
            } catch (NumberFormatException e) {
                log.error("无效的用户ID: {}", userId);
                session.close();
            }
        } else {
            log.error("缺少用户ID参数，关闭连接");
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到WebSocket消息: {}", message.getPayload());
        
        // 处理心跳消息
        if ("ping".equals(message.getPayload())) {
            sendMessage(session, new WebSocketMessage("pong", "pong", "心跳响应"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", session.getId(), exception);
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket连接关闭: {}, 状态: {}", session.getId(), closeStatus);
        cleanupSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 向指定用户推送订单状态变更消息
     */
    public void pushOrderStatusUpdate(Long userId, OrderStatusUpdateMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                WebSocketMessage wsMessage = new WebSocketMessage("order_status_update", message, "订单状态更新");
                sendMessage(session, wsMessage);
                log.info("向用户 {} 推送订单状态更新: {}", userId, message);
            } catch (Exception e) {
                log.error("推送订单状态更新失败: userId={}", userId, e);
                // 如果发送失败，移除无效连接
                cleanupSession(session);
            }
        } else {
            log.debug("用户 {} 没有活跃的WebSocket连接", userId);
        }
    }

    /**
     * 向所有连接的用户广播消息
     */
    public void broadcastMessage(WebSocketMessage message) {
        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("广播消息失败: sessionId={}", session.getId(), e);
                    cleanupSession(session);
                }
            }
        });
    }

    /**
     * 获取当前连接数
     */
    public int getActiveConnectionCount() {
        return userSessions.size();
    }

    /**
     * 发送消息到WebSocket会话
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(jsonMessage));
    }

    /**
     * 清理会话
     */
    private void cleanupSession(WebSocketSession session) {
        Long userId = sessionUsers.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
            log.info("清理用户 {} 的WebSocket会话", userId);
        }
    }

    /**
     * 从WebSocket会话中获取查询参数
     */
    private String getQueryParam(WebSocketSession session, String paramName) {
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    /**
     * WebSocket消息包装类
     */
    public static class WebSocketMessage {
        private String type;
        private Object data;
        private String message;
        private long timestamp;

        public WebSocketMessage(String type, Object data, String message) {
            this.type = type;
            this.data = data;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 订单状态更新消息
     */
    public static class OrderStatusUpdateMessage {
        private Long orderId;
        private String orderNo;
        private String oldStatus;
        private String newStatus;
        private String updateTime;
        private String updateReason;

        public OrderStatusUpdateMessage(Long orderId, String orderNo, String oldStatus, String newStatus, String updateTime, String updateReason) {
            this.orderId = orderId;
            this.orderNo = orderNo;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.updateTime = updateTime;
            this.updateReason = updateReason;
        }

        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getOldStatus() { return oldStatus; }
        public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public String getUpdateTime() { return updateTime; }
        public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
        public String getUpdateReason() { return updateReason; }
        public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
    }
}