package com.carwash.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 服务请求DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class ServiceRequest {

    /**
     * 服务名称
     */
    @NotBlank(message = "服务名称不能为空")
    private String name;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 服务价格
     */
    @NotNull(message = "服务价格不能为空")
    @DecimalMin(value = "0.01", message = "服务价格必须大于0")
    private BigDecimal price;

    /**
     * 服务时长（分钟）
     */
    @NotNull(message = "服务时长不能为空")
    @Min(value = 1, message = "服务时长必须大于0")
    private Integer duration;

    /**
     * 服务图片URL
     */
    private String imageUrl;

    /**
     * 服务分类
     */
    @NotBlank(message = "服务分类不能为空")
    private String category;

    /**
     * 排序权重
     */
    private Integer sortOrder = 0;

    // Getter methods
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    // Setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}