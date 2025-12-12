package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI客服聊天消息实体类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_messages")
public class ChatMessage {

    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 用户ID（可选，游客为null）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 消息角色：user-用户, assistant-AI助手, system-系统
     */
    @TableField("role")
    private String role;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 意图分类：greeting-问候, service_inquiry-服务咨询, 
     * booking_help-预约帮助, price_inquiry-价格咨询,
     * order_status-订单状态, complaint-投诉建议, other-其他
     */
    @TableField("intent")
    private String intent;

    /**
     * 置信度分数（0-1）
     */
    @TableField("confidence")
    private Double confidence;

    /**
     * 是否已解决：0-未解决, 1-已解决
     */
    @TableField("resolved")
    private Integer resolved;

    /**
     * 用户满意度评分（1-5）
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
