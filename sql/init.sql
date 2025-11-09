-- 汽车洗车服务预约系统数据库初始化脚本
-- 创建时间: 2025-09-21
-- 更新时间: 2025-10-26
-- 版本: 2.0.0
-- 说明: 保留用户表结构，清空并重新生成其他表数据

-- 设置字符编码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 使用数据库
USE carwash_db;

-- ========================================
-- 第一步：识别并保留用户表结构
-- ========================================
-- 用户表结构保持不变，不进行任何修改

-- 用户表
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `real_name` varchar(200) DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` enum('user','admin') DEFAULT 'user' COMMENT '用户角色',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 服务项目表
CREATE TABLE `services` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `name` varchar(100) NOT NULL COMMENT '服务名称',
  `description` text COMMENT '服务描述',
  `price` decimal(10,2) NOT NULL COMMENT '服务价格',
  `duration` int NOT NULL COMMENT '服务时长（分钟）',
  `image_url` varchar(255) DEFAULT NULL COMMENT '服务图片URL',
  `category` varchar(50) DEFAULT NULL COMMENT '服务分类',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-下架，1-上架',
  `sort_order` int DEFAULT '0' COMMENT '排序权重',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

-- 时间段表
CREATE TABLE `time_slots` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
  `date` date NOT NULL COMMENT '日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `max_bookings` int DEFAULT '1' COMMENT '最大预约数',
  `current_bookings` int DEFAULT '0' COMMENT '当前预约数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-不可用，1-可用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_time` (`date`,`start_time`,`end_time`),
  KEY `idx_date` (`date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间段表';

-- 预约订单表
CREATE TABLE `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `service_id` bigint NOT NULL COMMENT '服务ID',
  `time_slot_id` bigint NOT NULL COMMENT '时间段ID',
  `booking_date` date NOT NULL COMMENT '预约日期',
  `booking_time` varchar(20) NOT NULL COMMENT '预约时间',
  `car_number` varchar(20) DEFAULT NULL COMMENT '车牌号',
  `car_model` varchar(50) DEFAULT NULL COMMENT '车型',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `notes` text COMMENT '备注信息',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价格',
  `status` enum('pending','confirmed','in_progress','completed','cancelled') DEFAULT 'pending' COMMENT '订单状态',
  `payment_status` enum('unpaid','paid','refunded') DEFAULT 'unpaid' COMMENT '支付状态',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `cancelled_at` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_time_slot_id` (`time_slot_id`),
  KEY `idx_booking_date` (`booking_date`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_status` (`payment_status`),
  CONSTRAINT `fk_bookings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_bookings_service` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`),
  CONSTRAINT `fk_bookings_time_slot` FOREIGN KEY (`time_slot_id`) REFERENCES `time_slots` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约订单表';

-- 系统配置表
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_desc` varchar(255) DEFAULT NULL COMMENT '配置描述',
  `config_type` varchar(20) DEFAULT 'string' COMMENT '配置类型',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 用户反馈表
CREATE TABLE `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `booking_id` bigint DEFAULT NULL COMMENT '订单ID',
  `rating` tinyint DEFAULT NULL COMMENT '评分（1-5）',
  `content` text COMMENT '反馈内容',
  `reply` text COMMENT '回复内容',
  `status` enum('pending','replied','closed') DEFAULT 'pending' COMMENT '状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_booking_id` (`booking_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_feedback_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- ========================================
-- 第二步：清除所有表数据的SQL语句
-- ========================================

-- 清空反馈表数据（依赖于bookings表）
TRUNCATE TABLE `feedback`;

-- 清空预约订单表数据（依赖于users、services、time_slots表）
TRUNCATE TABLE `bookings`;

-- 清空时间段表数据
TRUNCATE TABLE `time_slots`;

-- 清空服务项目表数据
TRUNCATE TABLE `services`;

-- 清空系统配置表数据
TRUNCATE TABLE `system_config`;

-- 清空用户表数据
TRUNCATE TABLE `users`;

-- 重置所有表的自增ID
ALTER TABLE `users` AUTO_INCREMENT = 1;
ALTER TABLE `services` AUTO_INCREMENT = 1;
ALTER TABLE `time_slots` AUTO_INCREMENT = 1;
ALTER TABLE `bookings` AUTO_INCREMENT = 1;
ALTER TABLE `system_config` AUTO_INCREMENT = 1;
ALTER TABLE `feedback` AUTO_INCREMENT = 1;

-- ========================================
-- 第三步：重新生成这些表数据的SQL语句
-- ========================================

-- 插入初始用户数据（如果用户表为空）
INSERT IGNORE INTO `users` (`username`, `password`, `phone`, `email`, `real_name`, `role`, `status`) VALUES
('admin', 'admin123', '13800138000', 'admin@carwash.com', '系统管理员', 'admin', 1),
('user', 'user123', '13800138001', 'user@example.com', '普通用户', 'user', 1);

-- 插入全新的服务项目数据
INSERT INTO `services` (`name`, `description`, `price`, `duration`, `category`, `status`, `sort_order`) VALUES
('快速洗车', '快速外观清洗，适合轻度脏污', 20.00, 20, '基础服务', 1, 1),
('基础洗车', '外观清洗、轮胎清洁、玻璃擦拭', 30.00, 30, '基础服务', 1, 2),
('标准洗车', '标准外观清洗 + 轮胎清洁', 35.00, 35, '基础服务', 1, 3),
('精致洗车', '基础洗车 + 内饰清洁 + 仪表台护理', 50.00, 45, '精致服务', 1, 4),
('高级洗车', '全面清洗 + 内饰简单清理', 55.00, 50, '精致服务', 1, 5),
('豪华洗车', '精致洗车 + 打蜡 + 轮胎上光', 80.00, 60, '豪华服务', 1, 6),
('至尊洗车', '豪华洗车 + 打蜡 + 内饰深度清洁', 100.00, 90, '豪华服务', 1, 7),
('内饰深度清洁', '座椅清洁、地毯清洗、空调清洁', 60.00, 40, '专项服务', 1, 8),
('车身打蜡', '专业打蜡服务，保护车漆', 40.00, 30, '专项服务', 1, 9),
('发动机清洗', '发动机舱深度清洗', 80.00, 45, '专项服务', 1, 10),
('轮胎保养', '轮胎清洗 + 轮胎蜡 + 轮毂清洁', 45.00, 25, '专项服务', 1, 11);

-- 插入未来7天的时间段数据（动态生成）
-- 今天
INSERT INTO `time_slots` (`date`, `start_time`, `end_time`, `max_bookings`, `current_bookings`, `status`) VALUES
(CURDATE(), '09:00:00', '09:30:00', 3, 0, 1),
(CURDATE(), '09:30:00', '10:00:00', 3, 0, 1),
(CURDATE(), '10:00:00', '10:30:00', 3, 0, 1),
(CURDATE(), '10:30:00', '11:00:00', 3, 0, 1),
(CURDATE(), '11:00:00', '11:30:00', 3, 0, 1),
(CURDATE(), '11:30:00', '12:00:00', 3, 0, 1),
(CURDATE(), '14:00:00', '14:30:00', 3, 0, 1),
(CURDATE(), '14:30:00', '15:00:00', 3, 0, 1),
(CURDATE(), '15:00:00', '15:30:00', 3, 0, 1),
(CURDATE(), '15:30:00', '16:00:00', 3, 0, 1),
(CURDATE(), '16:00:00', '16:30:00', 3, 0, 1),
(CURDATE(), '16:30:00', '17:00:00', 3, 0, 1),
(CURDATE(), '17:00:00', '17:30:00', 3, 0, 1),
(CURDATE(), '17:30:00', '18:00:00', 3, 0, 1);

-- 明天
INSERT INTO `time_slots` (`date`, `start_time`, `end_time`, `max_bookings`, `current_bookings`, `status`) VALUES
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '09:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', '10:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '10:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00', '11:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00', '11:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:30:00', '12:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '14:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:30:00', '15:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00', '15:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:30:00', '16:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '16:00:00', '16:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '16:30:00', '17:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '17:00:00', '17:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '17:30:00', '18:00:00', 3, 0, 1);

-- 后天
INSERT INTO `time_slots` (`date`, `start_time`, `end_time`, `max_bookings`, `current_bookings`, `status`) VALUES
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', '09:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:30:00', '10:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00', '10:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:30:00', '11:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '11:00:00', '11:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '11:30:00', '12:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '14:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:30:00', '15:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00', '15:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:30:00', '16:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00', '16:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:30:00', '17:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '17:00:00', '17:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '17:30:00', '18:00:00', 3, 0, 1);

-- 第4天
INSERT INTO `time_slots` (`date`, `start_time`, `end_time`, `max_bookings`, `current_bookings`, `status`) VALUES
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', '09:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:30:00', '10:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', '10:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:30:00', '11:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00:00', '11:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:30:00', '12:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00:00', '14:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:30:00', '15:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:00:00', '15:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:30:00', '16:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '16:00:00', '16:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '16:30:00', '17:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '17:00:00', '17:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '17:30:00', '18:00:00', 3, 0, 1);

-- 第5天
INSERT INTO `time_slots` (`date`, `start_time`, `end_time`, `max_bookings`, `current_bookings`, `status`) VALUES
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '09:00:00', '09:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '09:30:00', '10:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:00:00', '10:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:30:00', '11:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '11:00:00', '11:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '11:30:00', '12:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '14:00:00', '14:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '14:30:00', '15:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00:00', '15:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:30:00', '16:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '16:00:00', '16:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '16:30:00', '17:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '17:00:00', '17:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '17:30:00', '18:00:00', 3, 0, 1);

-- 第6天
INSERT INTO `time_slots` (`date`, `start_time`, `end_time`, `max_bookings`, `current_bookings`, `status`) VALUES
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '09:00:00', '09:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '09:30:00', '10:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '10:00:00', '10:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '10:30:00', '11:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '11:00:00', '11:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '11:30:00', '12:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '14:00:00', '14:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '14:30:00', '15:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '15:00:00', '15:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '15:30:00', '16:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00', '16:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:30:00', '17:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '17:00:00', '17:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '17:30:00', '18:00:00', 3, 0, 1);

-- 第7天
INSERT INTO `time_slots` (`date`, `start_time`, `end_time`, `max_bookings`, `current_bookings`, `status`) VALUES
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '09:00:00', '09:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '09:30:00', '10:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '10:00:00', '10:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '10:30:00', '11:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '11:00:00', '11:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '11:30:00', '12:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '14:00:00', '14:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '14:30:00', '15:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '15:00:00', '15:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '15:30:00', '16:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '16:00:00', '16:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '16:30:00', '17:00:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '17:00:00', '17:30:00', 3, 0, 1),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '17:30:00', '18:00:00', 3, 0, 1);

-- 插入全新的系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`, `config_type`) VALUES
('site_name', '汽车洗车服务预约系统', '网站名称', 'string'),
('site_logo', '/images/logo.png', '网站Logo', 'string'),
('contact_phone', '400-888-8888', '客服电话', 'string'),
('business_hours', '09:00-18:00', '营业时间', 'string'),
('max_advance_days', '7', '最大提前预约天数', 'number'),
('ai_service_enabled', 'true', '是否启用AI客服', 'boolean'),
('working_hours_start', '09:00', '营业开始时间', 'time'),
('working_hours_end', '18:00', '营业结束时间', 'time'),
('slot_duration', '30', '每个时间段时长（分钟）', 'number'),
('max_advance_booking_days', '30', '最大提前预约天数', 'number'),
('auto_confirm_booking', 'false', '是否自动确认预约', 'boolean'),
('sms_notification_enabled', 'true', '是否启用短信通知', 'boolean'),
('email_notification_enabled', 'true', '是否启用邮件通知', 'boolean');

-- ========================================
-- 第四步：创建索引优化查询性能
-- ========================================
CREATE INDEX idx_bookings_date_status ON bookings(booking_date, status);
CREATE INDEX idx_time_slots_date_status ON time_slots(date, status);
CREATE INDEX idx_users_role_status ON users(role, status);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

COMMIT;

-- ========================================
-- 操作完成验证
-- ========================================
SELECT '=== 数据库初始化完成 ===' as '状态';

SELECT 
    '用户数据' as '表名', 
    COUNT(*) as '记录数',
    '初始用户数据' as '操作'
FROM users
UNION ALL
SELECT 
    '服务数据', 
    COUNT(*),
    '重新生成数据'
FROM services  
UNION ALL
SELECT 
    '时间段数据', 
    COUNT(*),
    '重新生成数据'
FROM time_slots
UNION ALL
SELECT 
    '预约数据', 
    COUNT(*),
    '已清空'
FROM bookings
UNION ALL
SELECT 
    '反馈数据', 
    COUNT(*),
    '已清空'
FROM feedback
UNION ALL
SELECT 
    '系统配置', 
    COUNT(*),
    '重新生成数据'
FROM system_config;

SELECT '=== 初始用户账号信息 ===' as '提示';
SELECT 
    username as '用户名',
    password as '密码',
    role as '角色',
    real_name as '姓名'
FROM users 
ORDER BY role DESC, id;