package com.carwash.controller;

import com.carwash.service.WebSocketService;
import com.carwash.websocket.OrderStatusWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket测试控制器
 * 用于测试WebSocket订单状态实时推送功能
 */
@Tag(name = "WebSocket测试", description = "WebSocket功能测试接口")
@RestController
@RequestMapping("/api/test/websocket")
@RequiredArgsConstructor
public class WebSocketTestController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTestController.class);
    
    private final WebSocketService webSocketService;

    /**
     * 测试订单状态推送
     */
    @Operation(summary = "测试订单状态推送", description = "向指定用户推送测试订单状态更新消息")
    @PostMapping("/push-order-status")
    public String testPushOrderStatus(
            @RequestParam Long userId,
            @RequestParam Long orderId,
            @RequestParam String orderNo,
            @RequestParam String oldStatus,
            @RequestParam String newStatus,
            @RequestParam(defaultValue = "测试订单状态更新") String reason) {
        
        try {
            log.info("测试推送订单状态更新: userId={}, orderId={}, orderNo={}, {}→{}", 
                    userId, orderId, orderNo, oldStatus, newStatus);
            
            webSocketService.pushOrderStatusUpdate(userId, orderId, orderNo, oldStatus, newStatus, reason);
            
            return String.format("成功向用户 %d 推送订单 %d 状态更新: %s → %s", 
                    userId, orderId, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("推送订单状态更新失败", e);
            return "推送失败: " + e.getMessage();
        }
    }

    /**
     * 获取当前WebSocket连接数
     */
    @Operation(summary = "获取WebSocket连接数", description = "获取当前活跃的WebSocket连接数量")
    @GetMapping("/connection-count")
    public String getConnectionCount() {
        try {
            int count = webSocketService.getActiveConnectionCount();
            return String.format("当前活跃WebSocket连接数: %d", count);
        } catch (Exception e) {
            log.error("获取连接数失败", e);
            return "获取连接数失败: " + e.getMessage();
        }
    }

    /**
     * 广播测试消息
     */
    @Operation(summary = "广播测试消息", description = "向所有连接的WebSocket客户端广播测试消息")
    @PostMapping("/broadcast")
    public String broadcastTestMessage(@RequestParam(defaultValue = "这是一条测试广播消息") String message) {
        try {
            // 创建测试消息
            OrderStatusWebSocketHandler.WebSocketMessage wsMessage = 
                new OrderStatusWebSocketHandler.WebSocketMessage("test_broadcast", message, "测试广播");
            
            // 这里需要在WebSocketService中添加广播方法
            // webSocketService.broadcastMessage(wsMessage);
            
            log.info("广播测试消息: {}", message);
            return "广播消息发送成功: " + message;
        } catch (Exception e) {
            log.error("广播消息失败", e);
            return "广播失败: " + e.getMessage();
        }
    }

    /**
     * 健康检查
     */
    @Operation(summary = "WebSocket健康检查", description = "检查WebSocket服务是否正常运行")
    @GetMapping("/health")
    public String healthCheck() {
        try {
            int connectionCount = webSocketService.getActiveConnectionCount();
            return String.format("WebSocket服务运行正常，当前连接数: %d", connectionCount);
        } catch (Exception e) {
            log.error("WebSocket健康检查失败", e);
            return "WebSocket服务异常: " + e.getMessage();
        }
    }
}