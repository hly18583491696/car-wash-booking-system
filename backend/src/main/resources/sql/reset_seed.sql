SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE refunds;
TRUNCATE TABLE payments;
TRUNCATE TABLE payment_audit;
TRUNCATE TABLE feedback;
TRUNCATE TABLE bookings;
TRUNCATE TABLE time_slots;
TRUNCATE TABLE services;
TRUNCATE TABLE system_config;
TRUNCATE TABLE users;

ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE services AUTO_INCREMENT = 1;
ALTER TABLE time_slots AUTO_INCREMENT = 1;
ALTER TABLE bookings AUTO_INCREMENT = 1;
ALTER TABLE system_config AUTO_INCREMENT = 1;
ALTER TABLE feedback AUTO_INCREMENT = 1;
ALTER TABLE payments AUTO_INCREMENT = 1;
ALTER TABLE refunds AUTO_INCREMENT = 1;
ALTER TABLE payment_audit AUTO_INCREMENT = 1;

INSERT IGNORE INTO users (username, password, phone, email, real_name, role, status) VALUES
('admin', 'admin123', '13800138000', 'admin@carwash.com', '系统管理员', 'admin', 1),
('user', 'user123', '13800138001', 'user@example.com', '普通用户', 'user', 1);

INSERT INTO services (name, description, price, duration, category, status, sort_order) VALUES
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

INSERT INTO time_slots (date, start_time, end_time, max_bookings, current_bookings, status) VALUES
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

INSERT INTO time_slots (date, start_time, end_time, max_bookings, current_bookings, status) VALUES
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

INSERT INTO time_slots (date, start_time, end_time, max_bookings, current_bookings, status) VALUES
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

INSERT INTO time_slots (date, start_time, end_time, max_bookings, current_bookings, status) VALUES
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

INSERT INTO time_slots (date, start_time, end_time, max_bookings, current_bookings, status) VALUES
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

INSERT INTO time_slots (date, start_time, end_time, max_bookings, current_bookings, status) VALUES
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

INSERT INTO time_slots (date, start_time, end_time, max_bookings, current_bookings, status) VALUES
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

INSERT INTO system_config (config_key, config_value, config_desc, config_type) VALUES
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

SET FOREIGN_KEY_CHECKS = 1;
