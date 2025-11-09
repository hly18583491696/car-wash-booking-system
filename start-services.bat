@echo off
echo 启动汽车洗车服务预约系统
echo ================================

echo 1. 检查MySQL服务状态...
sc query mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo MySQL服务未运行，尝试启动...
    net start mysql
) else (
    echo MySQL服务已运行
)

echo.
echo 2. 启动Redis服务...
netstat -ano | findstr :6379 >nul 2>&1
if %errorlevel% neq 0 (
    if exist "%~dp0redis\redis-server.exe" (
        echo Redis服务未运行，正在启动...
        start "Redis Server" "%~dp0redis\redis-server.exe" "%~dp0redis\redis.windows.conf"
        timeout /t 3 /nobreak >nul
        echo Redis服务已启动
    ) else (
        echo 警告：Redis未安装，部分功能可能不可用
    )
) else (
    echo Redis服务已运行
)

echo.
echo 3. 启动后端服务...
cd /d "%~dp0backend"
start "后端服务" cmd /k "echo 启动Spring Boot后端服务... && mvn spring-boot:run"

echo.
echo 4. 等待后端服务启动...
timeout /t 10 /nobreak >nul

echo.
echo 5. 启动前端服务...
cd /d "%~dp0frontend"
start "前端服务" cmd /k "echo 启动Vue.js前端服务... && npm run dev"

echo.
echo 6. 等待前端服务启动...
timeout /t 5 /nobreak >nul

echo.
echo ================================
echo 系统启动完成！
echo.
echo 前端地址: http://localhost:3000
echo 后端地址: http://localhost:8080
echo 真实数据测试: http://localhost:3000/real-data-test
echo.
echo 测试账号:
echo 管理员: admin / admin123
echo 用户: user001 / user123
echo ================================

pause