-- 支付审计日志表（MySQL）
CREATE TABLE IF NOT EXISTS `payment_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_no` VARCHAR(64) NOT NULL,
  `order_no` VARCHAR(64) NOT NULL,
  `event_type` VARCHAR(32) NOT NULL,
  `status` VARCHAR(32) NULL,
  `payment_method` VARCHAR(32) NULL,
  `amount` DECIMAL(10,2) NULL,
  `operator_id` BIGINT NULL,
  `message` VARCHAR(255) NULL,
  `raw_data` TEXT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;