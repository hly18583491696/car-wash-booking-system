USE carwash_db;

SELECT '=== 数据库初始化验证 ===' as status;

-- 验证所有表的数据量
SELECT 
    '用户数据' as '表名', 
    COUNT(*) as '记录数'
FROM users
UNION ALL
SELECT 
    '服务数据', 
    COUNT(*)
FROM services  
UNION ALL
SELECT 
    '时间段数据', 
    COUNT(*)
FROM time_slots
UNION ALL
SELECT 
    '预约数据', 
    COUNT(*)
FROM bookings
UNION ALL
SELECT 
    '反馈数据', 
    COUNT(*)
FROM feedback
UNION ALL
SELECT 
    '系统配置', 
    COUNT(*)
FROM system_config;

SELECT '=== 用户账号信息 ===' as info;
SELECT 
    username as '用户名',
    password as '密码',
    role as '角色',
    real_name as '姓名',
    phone as '手机号'
FROM users 
ORDER BY role DESC, id;

SELECT '=== 服务项目信息 ===' as service_info;
SELECT 
    name as '服务名称',
    price as '价格',
    duration as '时长(分钟)',
    category as '分类'
FROM services 
ORDER BY sort_order;

SELECT '=== 时间段统计 ===' as time_info;
SELECT 
    DATE(date) as '日期',
    COUNT(*) as '时间段数量'
FROM time_slots 
GROUP BY DATE(date)
ORDER BY date
LIMIT 7;