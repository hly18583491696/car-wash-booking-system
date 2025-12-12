package com.carwash.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客服聊天响应DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * AI回复消息
     */
    private String message;

    /**
     * 识别的意图
     */
    private String intent;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 相关建议操作
     */
    private List<SuggestedAction> suggestedActions;

    /**
     * 相关服务推荐
     */
    private List<ServiceRecommendation> recommendations;

    /**
     * 是否需要人工客服
     */
    private Boolean needHumanSupport;

    /**
     * 响应时间
     */
    private LocalDateTime timestamp;

    /**
     * 建议操作
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SuggestedAction {
        private String type;      // booking, service, order, contact
        private String label;     // 显示文本
        private String action;    // 操作标识
        private String url;       // 跳转链接（可选）
    }

    /**
     * 服务推荐
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceRecommendation {
        private Long serviceId;
        private String serviceName;
        private String description;
        private Double price;
    }
}
