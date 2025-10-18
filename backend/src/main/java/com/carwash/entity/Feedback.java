package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("feedback")
public class Feedback {

    /**
     * 反馈ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单ID
     */
    @TableField("booking_id")
    private Long bookingId;

    /**
     * 评分（1-5）
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 反馈内容
     */
    @TableField("content")
    private String content;

    /**
     * 回复内容
     */
    @TableField("reply")
    private String reply;

    /**
     * 状态：pending-待处理, replied-已回复, closed-已关闭
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 是否删除：0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}