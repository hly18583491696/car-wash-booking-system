package com.carwash.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务响应DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class ServiceResponse {

    /**
     * 服务ID
     */
    private Long id;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 服务价格
     */
    private BigDecimal price;

    /**
     * 服务时长（分钟）
     */
    private Integer duration;

    /**
     * 服务图片URL
     */
    private String imageUrl;

    /**
     * 服务分类
     */
    private String category;

    /**
     * 状态：0-下架, 1-上架
     */
    private Integer status;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}