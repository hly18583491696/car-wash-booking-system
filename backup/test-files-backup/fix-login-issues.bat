@echo off
chcp 65001 >nul
echo ========================================
echo 汽车洗车服务预约系统 - 登录问题修复工具
echo ========================================
echo.

echo [1/5] 检查服务状态...
echo 🔍 检查前端服务（端口3003）...
netstat -an | findstr :3003 >nul
if %errorlevel% equ 0 (
    echo ✅ 前端服务正在运行
) else (
    echo ❌ 前端服务未运行，正在启动...
    cd /d "%~dp0frontend"
    start "前端服务" cmd /k "npm run dev"
    echo ⏰ 等待前端服务启动...
    timeout /t 10 >nul
)

echo 🔍 检查后端服务（端口8080）...
netstat -an | findstr :8080 >nul
if %errorlevel% equ 0 (
    echo ✅ 后端服务正在运行
) else (
    echo ❌ 后端服务未运行，正在启动...
    cd /d "%~dp0backend"
    start "后端服务" cmd /k "mvn spring-boot:run"
    echo ⏰ 等待后端服务启动...
    timeout /t 30 >nul
)

echo.
echo [2/5] 检查数据库连接...
echo 🗄️ 检查MySQL服务...
sc query mysql80 | findstr "RUNNING" >nul
if %errorlevel% equ 0 (
    echo ✅ MySQL服务正在运行
) else (
    echo ❌ MySQL服务未运行，正在启动...
    net start mysql80 >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ MySQL服务启动成功
    ) else (
        echo ⚠️ MySQL服务启动失败，请手动检查
    )
)

echo.
echo [3/5] 测试API连接...
echo 🌐 测试后端API连接...
curl -s http://localhost:8080/api/test/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 后端API连接正常
) else (
    echo ⚠️ 后端API连接失败，请检查服务状态
)

echo.
echo [4/5] 清理缓存和临时文件...
echo 🧹 清理浏览器缓存相关...
echo   - 建议手动清理浏览器缓存和本地存储
echo   - 或使用无痕模式/隐私模式访问

echo 🧹 清理前端缓存...
cd /d "%~dp0frontend"
if exist node_modules\.cache (
    rmdir /s /q node_modules\.cache >nul 2>&1
    echo ✅ 前端缓存已清理
)

echo.
echo [5/5] 打开诊断工具...
echo 🔧 正在打开登录诊断工具...
timeout /t 3 >nul
start http://localhost:3003/login-diagnostic

echo.
echo ========================================
echo 🎉 登录问题修复工具启动完成！
echo ========================================
echo.
echo 📋 接下来的步骤：
echo   1. 在打开的诊断页面中点击"开始诊断"
echo   2. 查看诊断结果，了解具体问题
echo   3. 点击"自动修复"尝试解决问题
echo   4. 使用"测试登录"功能验证修复效果
echo.
echo 🔗 相关链接：
echo   • 登录诊断工具: http://localhost:3003/login-diagnostic
echo   • 登录页面: http://localhost:3003/login
echo   • 后端API文档: http://localhost:8080/swagger-ui.html
echo.
echo 💡 常见问题解决方案：
echo   • 如果端口被占用，请关闭占用进程或修改配置
echo   • 如果数据库连接失败，请检查MySQL服务和配置
echo   • 如果API调用失败，请检查防火墙和网络设置
echo.

echo 按任意键退出...
pause >nul