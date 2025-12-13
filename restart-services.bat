@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 参数解析：-b 仅后端, -f 仅前端
set "MODE=all"
if "%1"=="-b" set "MODE=backend"
if "%1"=="-f" set "MODE=frontend"
if "%1"=="--backend" set "MODE=backend"
if "%1"=="--frontend" set "MODE=frontend"

echo ========================================
echo   服务重启 v2.0
echo ========================================

REM ====== 停止服务 ======
echo [1/2] 停止服务...

if "%MODE%" neq "frontend" (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080.*LISTENING" 2^>nul') do (
        echo       终止后端 PID: %%a
        taskkill /f /pid %%a >nul 2>&1
    )
)

if "%MODE%" neq "backend" (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3003.*LISTENING" 2^>nul') do (
        echo       终止前端 PID: %%a
        taskkill /f /pid %%a >nul 2>&1
    )
)

ping -n 2 127.0.0.1 >nul 2>&1
echo [OK] 服务已停止

REM ====== 启动服务 ======
echo [2/2] 启动服务...

REM Redis
if "%MODE%" neq "frontend" (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":6379.*LISTENING" 2^>nul') do set "REDIS_OK=1"
    if not defined REDIS_OK (
        if exist "%~dp0redis\redis-server.exe" (
            start /min "Redis" "%~dp0redis\redis-server.exe" "%~dp0redis\redis.windows.conf"
            ping -n 2 127.0.0.1 >nul 2>&1
        )
    )
    echo [OK] Redis 就绪
)

REM 后端
if "%MODE%" neq "frontend" (
    cd /d "%~dp0backend"
    start "后端" cmd /c "title 后端-8080 && set SPRING_PROFILES_ACTIVE=payment && mvn spring-boot:run -q"
    echo [OK] 后端启动中
)

REM 前端
if "%MODE%" neq "backend" (
    cd /d "%~dp0frontend"
    start "前端" cmd /c "title 前端-3003 && npm run dev"
    echo [OK] 前端启动中
)

echo.
echo ========================================
echo   重启完成!
echo ----------------------------------------
if "%MODE%" neq "backend" echo   前端: http://localhost:3003
if "%MODE%" neq "frontend" echo   后端: http://localhost:8080/api
echo ========================================