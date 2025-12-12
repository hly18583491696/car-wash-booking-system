package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI客服知识库实体类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("knowledge_base")
public class KnowledgeBase {

    /**
     * 知识ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 问题分类：service-服务相关, booking-预约相关, 
     * payment-支付相关, account-账户相关, general-通用问题
     */
    @TableField("category")
    private String category;

    /**
     * 问题关键词（逗号分隔）
     */
    @TableField("keywords")
    private String keywords;

    /**
     * 问题内容
     */
    @TableField("question")
    private String question;

    /**
     * 标准答案
     */
    @TableField("answer")
    private String answer;

    /**
     * 匹配优先级（越高越优先）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 匹配次数统计
     */
    @TableField("hit_count")
    private Integer hitCount;

    /**
     * 状态：0-禁用, 1-启用
     */
    @TableField("status")
    private Integer status;

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
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
