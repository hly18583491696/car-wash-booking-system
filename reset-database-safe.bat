@echo off
chcp 65001 >nul
echo ========================================
echo 汽车洗车服务预约系统 - 安全数据库重置
echo ========================================
echo.

echo ⚠️  警告：此操作将清空所有数据库数据！
echo.
echo 📋 将要执行的操作：
echo    1. 清空所有用户预约数据
echo    2. 清空所有反馈数据  
echo    3. 重置用户数据（保留初始账号）
echo    4. 重置服务项目数据
echo    5. 重置时间段数据（未来7天）
echo    6. 重置系统配置
echo.

set /p confirm="确定要继续吗？(输入 YES 确认): "

if /i "%confirm%" neq "YES" (
    echo.
    echo ❌ 操作已取消
    echo.
    goto end
)

echo.
echo 🔄 正在备份当前数据...

:: 创建备份目录
if not exist "backup" mkdir backup

:: 生成备份文件名（包含时间戳）
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "datestamp=%YYYY%%MM%%DD%_%HH%%Min%%Sec%"

:: 执行数据备份
mysqldump -u root -p123456 -h localhost -P 3306 carwash_db > backup\carwash_db_backup_%datestamp%.sql

if %errorlevel% equ 0 (
    echo ✅ 数据备份完成: backup\carwash_db_backup_%datestamp%.sql
) else (
    echo ❌ 数据备份失败，操作终止
    goto end
)

echo.
echo 🔄 正在执行数据库重置...

:: 执行数据库重置脚本
mysql -u root -p123456 -h localhost -P 3306 < sql\reset_database.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ 数据库重置成功！
    echo.
    echo 📋 初始账号信息：
    echo    👤 管理员: admin / admin123
    echo    👤 测试用户: user001 / user123  
    echo    👤 测试用户: testuser1 / test123
    echo    👤 测试用户: testuser2 / test123
    echo    👤 店长: manager / manager123
    echo.
    echo 📊 重置完成的数据：
    echo    ✓ 用户数据: 5个初始用户
    echo    ✓ 服务项目: 11个服务项目
    echo    ✓ 时间段: 未来7天 × 14个时间段/天 = 98个时间段
    echo    ✓ 系统配置: 13项基础配置
    echo    ✓ 预约数据: 已清空
    echo    ✓ 反馈数据: 已清空
    echo.
    echo 💾 数据备份位置: backup\carwash_db_backup_%datestamp%.sql
    echo.
) else (
    echo.
    echo ❌ 数据库重置失败！
    echo.
    echo 🔍 可能的原因：
    echo    1. MySQL服务未启动
    echo    2. 数据库连接信息错误
    echo    3. carwash_db数据库不存在
    echo    4. 权限不足
    echo.
    echo 💡 解决建议：
    echo    1. 检查MySQL服务状态
    echo    2. 确认数据库连接参数
    echo    3. 检查用户权限
    echo.
)

:end
echo.
echo 按任意键退出...
pause >nul