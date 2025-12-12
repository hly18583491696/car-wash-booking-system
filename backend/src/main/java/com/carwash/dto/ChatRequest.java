package com.carwash.dto;

import lombok.Data;

/**
 * AI客服聊天请求DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class ChatRequest {

    /**
     * 会话ID（可选，新会话时为空）
     */
    private String sessionId;

    /**
     * 用户消息内容
     */
    private String message;

    /**
     * 用户ID（可选，未登录时为空）
     */
    private Long userId;

    /**
     * 上下文信息（可选，用于提供额外上下文）
     */
    private String context;
}
