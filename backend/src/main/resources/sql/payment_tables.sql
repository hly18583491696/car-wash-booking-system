-- 支付系统相关数据库表

-- 支付记录表
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    payment_no VARCHAR(64) NOT NULL UNIQUE COMMENT '支付流水号',
    transaction_id VARCHAR(64) NULL COMMENT '第三方支付平台交易号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式：wechat-微信支付, alipay-支付宝, unionpay-银联支付',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '支付状态：pending-待支付, paid-已支付, failed-支付失败, cancelled-已取消, refunded-已退款',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    expire_at TIMESTAMP NOT NULL COMMENT '支付过期时间',
    notify_count INT NOT NULL DEFAULT 0 COMMENT '支付回调通知次数',
    description VARCHAR(200) NULL COMMENT '支付描述',
    raw_data TEXT NULL COMMENT '支付平台返回的原始数据',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除, 1-已删除',
    
    INDEX idx_order_no (order_no),
    INDEX idx_payment_no (payment_no),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_payment_method (payment_method),
    INDEX idx_expire_at (expire_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS refunds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    payment_id BIGINT NOT NULL COMMENT '支付记录ID',
    refund_no VARCHAR(64) NOT NULL UNIQUE COMMENT '退款单号',
    refund_id VARCHAR(64) NULL COMMENT '第三方平台退款单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(200) NOT NULL COMMENT '退款原因',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '退款状态：pending-退款中, success-退款成功, failed-退款失败',
    refunded_at TIMESTAMP NULL COMMENT '退款时间',
    operator_id BIGINT NULL COMMENT '退款操作员ID',
    raw_data TEXT NULL COMMENT '退款平台返回的原始数据',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除, 1-已删除',
    
    INDEX idx_payment_id (payment_id),
    INDEX idx_refund_no (refund_no),
    INDEX idx_refund_id (refund_id),
    INDEX idx_status (status),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

-- 为现有的bookings表添加支付相关字段（如果不存在）
ALTER TABLE bookings 
ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) DEFAULT 'unpaid' COMMENT '支付状态：unpaid-未支付, paid-已支付, refunded-已退款' AFTER status,
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NULL COMMENT '支付方式' AFTER payment_status,
ADD COLUMN IF NOT EXISTS paid_at TIMESTAMP NULL COMMENT '支付时间' AFTER payment_method;

-- 添加索引
ALTER TABLE bookings 
ADD INDEX IF NOT EXISTS idx_payment_status (payment_status),
ADD INDEX IF NOT EXISTS idx_payment_method (payment_method);

-- 插入测试数据（可选）
INSERT IGNORE INTO payments (order_no, payment_no, user_id, amount, payment_method, status, expire_at, description) VALUES
('ORD202501011234567890', 'PAY17359876541234ABCD', 2, 50.00, 'wechat', 'pending', DATE_ADD(NOW(), INTERVAL 30 MINUTE), '洗车服务费用'),
('ORD202501011234567891', 'PAY17359876542345BCDE', 2, 80.00, 'alipay', 'paid', DATE_ADD(NOW(), INTERVAL 30 MINUTE), '精洗服务费用');

-- 更新对应的订单支付状态
UPDATE bookings SET payment_status = 'paid', payment_method = 'alipay', paid_at = NOW() 
WHERE order_no = 'ORD202501011234567891' AND payment_status = 'unpaid';