-- 插入测试订单数据
-- 解决"我的订单"页面无数据显示的问题

-- Insert test booking data for user ID 2 (admin user)
INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD001', 2, 1, 1, CURDATE(), '09:00-09:30', 'A12345', 'Benz C200', '13800138001', 'Careful wash', 50.00, 'completed', 'paid', 'alipay', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 1 HOUR, NULL, NULL, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 1 HOUR);

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD002', 2, 2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00-09:30', 'B67890', 'BMW X5', '13800138002', 'Interior clean', 100.00, 'in_progress', 'paid', 'wechat', NOW() - INTERVAL 1 HOUR, NULL, NULL, NULL, NOW() - INTERVAL 2 HOUR, NOW());

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD003', 2, 3, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00-10:30', 'C11111', 'Audi A6', '13800138003', 'Full service', 200.00, 'pending', 'unpaid', NULL, NULL, NULL, NULL, NULL, NOW() - INTERVAL 30 MINUTE, NOW());

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD004', 2, 4, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00-14:30', 'D22222', 'Toyota Camry', '13800138004', 'Interior only', 80.00, 'confirmed', 'paid', 'alipay', NOW() - INTERVAL 10 MINUTE, NULL, NULL, NULL, NOW() - INTERVAL 1 HOUR, NOW());

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD005', 2, 5, 1, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00-15:30', 'E33333', 'Honda Accord', '13800138005', 'Cancelled', 120.00, 'cancelled', 'refunded', 'wechat', NOW() - INTERVAL 2 HOUR, NULL, NOW() - INTERVAL 30 MINUTE, 'Emergency', NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 30 MINUTE);

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD006', 2, 1, 1, CURDATE(), '09:30-10:00', 'F44444', 'VW Passat', '13800138006', 'Quick wash', 50.00, 'completed', 'paid', 'cash', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 3 HOUR, NULL, NULL, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 3 HOUR);

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD007', 2, 2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30-10:00', 'G55555', 'Nissan Teana', '13800138007', 'Wheel clean', 100.00, 'confirmed', 'paid', 'alipay', NOW() - INTERVAL 1 HOUR, NULL, NULL, NULL, NOW() - INTERVAL 2 HOUR, NOW());

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, paid_at, completed_at, cancelled_at, cancel_reason, created_at, updated_at) VALUES 
('ORD008', 2, 3, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:30-11:00', 'H66666', 'Lexus ES', '13800138008', 'Luxury service', 200.00, 'pending', 'unpaid', NULL, NULL, NULL, NULL, NULL, NOW() - INTERVAL 15 MINUTE, NOW());

-- 更新对应时间段的预约数量
UPDATE `time_slots` SET `current_bookings` = `current_bookings` + 1 WHERE `id` IN (1, 15, 29, 43, 57, 2, 16, 30);

-- 验证插入的数据
SELECT 
    b.id,
    b.order_no,
    b.user_id,
    s.name as service_name,
    b.booking_date,
    b.booking_time,
    b.car_number,
    b.car_model,
    b.total_price,
    b.status,
    b.payment_status,
    b.created_at
FROM bookings b
LEFT JOIN services s ON b.service_id = s.id
WHERE b.user_id = 2
ORDER BY b.created_at DESC;