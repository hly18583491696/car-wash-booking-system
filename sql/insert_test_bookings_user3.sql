-- 为用户ID=3 (asum) 添加测试订单数据
USE carwash_db;

-- 插入测试订单数据
INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, created_at, updated_at, deleted) VALUES
('ORD301', 3, 1, 1, '2024-11-02', '09:00:00', 'ABC301', 'Toyota Camry', '18289099999', 'Regular wash service', 50.00, 'completed', 'paid', 'alipay', NOW(), NOW(), 0);

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, created_at, updated_at, deleted) VALUES
('ORD302', 3, 2, 2, '2024-11-02', '10:00:00', 'ABC302', 'Honda Civic', '18289099999', 'Premium wash with wax', 80.00, 'in_progress', 'paid', 'wechat', NOW(), NOW(), 0);

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, created_at, updated_at, deleted) VALUES
('ORD303', 3, 3, 3, '2024-11-03', '14:00:00', 'ABC303', 'BMW X5', '18289099999', 'Interior cleaning service', 120.00, 'pending', 'unpaid', 'cash', NOW(), NOW(), 0);

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, created_at, updated_at, deleted) VALUES
('ORD304', 3, 4, 4, '2024-11-04', '15:30:00', 'ABC304', 'Mercedes C200', '18289099999', 'Full service package', 200.00, 'confirmed', 'paid', 'alipay', NOW(), NOW(), 0);

INSERT INTO bookings (order_no, user_id, service_id, time_slot_id, booking_date, booking_time, car_number, car_model, contact_phone, notes, total_price, status, payment_status, payment_method, created_at, updated_at, deleted) VALUES
('ORD305', 3, 1, 5, '2024-11-01', '11:00:00', 'ABC305', 'Audi A4', '18289099999', 'Quick wash', 45.00, 'cancelled', 'refunded', 'wechat', NOW(), NOW(), 0);

-- 验证插入的数据
SELECT COUNT(*) as total_orders FROM bookings WHERE user_id = 3 AND deleted = 0;
SELECT * FROM bookings WHERE user_id = 3 AND deleted = 0 ORDER BY created_at DESC LIMIT 5;