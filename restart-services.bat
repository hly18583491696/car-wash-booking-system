@echo off
chcp 65001 >nul
echo ========================================
echo 汽车洗车服务预约系统 - 服务重启
echo ========================================
echo.

echo [1/3] 停止现有服务...
echo 🛑 正在停止前端服务（端口3003）...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3003') do (
    echo 发现进程 %%a 占用端口 3003，正在终止...
    taskkill /f /pid %%a >nul 2>&1
)

echo 🛑 正在停止后端服务（端口8080）...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    echo 发现进程 %%a 占用端口 8080，正在终止...
    taskkill /f /pid %%a >nul 2>&1
)

echo 🛑 正在停止相关Java进程...
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im node.exe >nul 2>&1

echo ✅ 服务停止完成
echo.

echo [2/3] 等待端口释放...
timeout /t 3 >nul
echo ✅ 端口释放完成
echo.

echo [3/3] 重新启动服务...
echo 🚀 启动后端服务...
cd /d "%~dp0backend"
start "后端服务" cmd /k "echo 重启后端服务... && mvn spring-boot:run"

echo 🚀 启动前端服务...
cd /d "%~dp0frontend"
start "前端服务" cmd /k "echo 重启前端服务... && npm run dev"

echo.
echo ========================================
echo 🎉 服务重启完成！
echo ========================================
echo.
echo 📋 访问地址：
echo   • 前端主页: http://localhost:3003
echo   • 数据同步测试: http://localhost:3003/data-sync-test
echo   • 后台管理: http://localhost:3003/admin
echo   • 后端API: http://localhost:8080/api
echo.
echo ⏰ 请等待服务完全启动（约30-60秒）
echo.

timeout /t 5 >nul
echo 🌐 正在打开测试页面...
start http://localhost:3003/data-sync-test

echo.
echo 按任意键退出...
pause >nul