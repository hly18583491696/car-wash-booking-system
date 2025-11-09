@echo off
chcp 65001 >nul
echo ========================================
echo 汽车洗车服务预约系统 - 数据库重置工具
echo ========================================
echo.

echo 正在连接数据库并执行重置脚本...
echo.

:: 执行数据库重置脚本
mysql -u root -p123456 -h localhost -P 3306 < sql\reset_database.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ 数据库重置成功！
    echo.
    echo 📋 初始账号信息：
    echo    管理员账号: admin / admin123
    echo    测试用户: user001 / user123
    echo    店长账号: manager / manager123
    echo.
    echo 📊 已重置的数据：
    echo    ✓ 用户数据 (5个初始用户)
    echo    ✓ 服务项目 (11个服务项目)
    echo    ✓ 时间段数据 (未来7天，每天14个时间段)
    echo    ✓ 系统配置 (13项基础配置)
    echo    ✓ 清空所有预约和反馈数据
    echo.
) else (
    echo.
    echo ❌ 数据库重置失败！
    echo 请检查：
    echo 1. MySQL服务是否启动
    echo 2. 数据库连接信息是否正确
    echo 3. carwash_db数据库是否存在
    echo.
)

echo 按任意键退出...
pause >nul