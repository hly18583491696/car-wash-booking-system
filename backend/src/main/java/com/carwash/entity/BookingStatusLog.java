package com.carwash.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单状态变更日志实体类
 * 用于记录订单状态的所有变更历史
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("booking_status_log")
public class BookingStatusLog {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("booking_id")
    private Long bookingId;

    /**
     * 旧状态
     */
    @TableField("old_status")
    private String oldStatus;

    /**
     * 新状态
     */
    @TableField("new_status")
    private String newStatus;

    /**
     * 状态变更原因
     */
    @TableField("update_reason")
    private String updateReason;

    /**
     * 操作人ID（用户或管理员）
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人类型（user/admin/system）
     */
    @TableField("operator_type")
    private String operatorType;

    /**
     * 状态变更时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

    /**
     * 客户端IP
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 客户端信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 额外数据（JSON格式）
     */
    @TableField("extra_data")
    private String extraData;
}
