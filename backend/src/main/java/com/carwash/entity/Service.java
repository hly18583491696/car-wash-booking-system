package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目实体类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("services")
public class Service {

    /**
     * 服务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务名称
     */
    @TableField("name")
    private String name;

    /**
     * 服务描述
     */
    @TableField("description")
    private String description;

    /**
     * 服务价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 服务时长（分钟）
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 服务图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 服务分类
     */
    @TableField("category")
    private String category;

    /**
     * 状态：0-下架, 1-上架
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

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