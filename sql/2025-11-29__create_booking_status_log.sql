-- 订单状态变更日志表
-- 用于记录订单状态的所有变更历史，支持审计和回滚
-- 创建时间: 2025-11-29

CREATE TABLE IF NOT EXISTS booking_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    booking_id BIGINT NOT NULL COMMENT '订单ID',
    old_status VARCHAR(20) COMMENT '旧状态',
    new_status VARCHAR(20) NOT NULL COMMENT '新状态',
    update_reason VARCHAR(200) COMMENT '状态变更原因',
    operator_id BIGINT COMMENT '操作人ID（用户或管理员）',
    operator_type VARCHAR(20) COMMENT '操作人类型（user/admin/system）',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '状态变更时间',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT '客户端信息',
    extra_data JSON COMMENT '额外数据（JSON格式）',
    INDEX idx_booking_id (booking_id),
    INDEX idx_update_time (update_time),
    INDEX idx_operator (operator_id, operator_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志表';
