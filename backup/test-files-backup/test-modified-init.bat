@echo off
chcp 65001 >nul
echo ========================================
echo 测试修改后的 init.sql 文件
echo ========================================
echo.

echo 🔄 正在执行修改后的 init.sql 脚本...
echo.

:: 执行修改后的初始化脚本
mysql -u root -p123456 -h localhost -P 3306 < sql\init.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ init.sql 执行成功！
    echo.
    echo 📊 执行结果：
    echo    ✓ 用户表结构：保持不变
    echo    ✓ 其他表数据：已清空并重新生成
    echo    ✓ 服务项目：11个新服务
    echo    ✓ 时间段：未来7天 × 14个时段/天 = 98个时段
    echo    ✓ 系统配置：13项配置
    echo    ✓ 预约数据：已清空
    echo    ✓ 反馈数据：已清空
    echo.
) else (
    echo.
    echo ❌ init.sql 执行失败！
    echo 请检查：
    echo 1. MySQL服务是否启动
    echo 2. 数据库连接信息是否正确
    echo 3. carwash_db数据库是否存在
    echo.
)

echo 按任意键退出...
pause >nul