-- 更新用户密码为BCrypt加密格式
-- admin密码: admin123
-- user001密码: user123

UPDATE users SET password = '$2a$10$N.zmdr9k7uOCQb07YxWS4OUdmJvGKt/SZvziXB6.P5fJyW8/CbZAy' WHERE username = 'admin';
UPDATE users SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.' WHERE username = 'user001';

-- 验证更新结果
SELECT username, SUBSTRING(password, 1, 10) as password_prefix FROM users;