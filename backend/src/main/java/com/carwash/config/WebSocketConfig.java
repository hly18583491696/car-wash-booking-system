package com.carwash.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.carwash.websocket.OrderStatusWebSocketHandler;

/**
 * WebSocket配置类
 * 用于配置WebSocket端点和处理器
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderStatusWebSocketHandler orderStatusWebSocketHandler;

    public WebSocketConfig(OrderStatusWebSocketHandler orderStatusWebSocketHandler) {
        this.orderStatusWebSocketHandler = orderStatusWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册订单状态WebSocket处理器
        registry.addHandler(orderStatusWebSocketHandler, "/ws/order-status")
                .setAllowedOrigins("*"); // 允许跨域，生产环境应该限制具体域名
    }
}