@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 参数解析：-b 仅后端, -f 仅前端, 无参数全部启动
set "MODE=all"
if "%1"=="-b" set "MODE=backend"
if "%1"=="-f" set "MODE=frontend"
if "%1"=="--backend" set "MODE=backend"
if "%1"=="--frontend" set "MODE=frontend"

echo ========================================
echo   汽车洗车服务预约系统 v2.0
echo ========================================

REM ====== Redis 服务 ======
if "%MODE%"=="all" (
    call :start_redis
) else if "%MODE%"=="backend" (
    call :start_redis
)

REM ====== 后端服务 ======
if "%MODE%"=="all" call :start_backend
if "%MODE%"=="backend" call :start_backend

REM ====== 前端服务 ======
if "%MODE%"=="all" call :start_frontend
if "%MODE%"=="frontend" call :start_frontend

echo.
echo ========================================
echo   启动完成!
echo ----------------------------------------
if "%MODE%" neq "backend" echo   前端: http://localhost:3003
if "%MODE%" neq "frontend" echo   后端: http://localhost:8080/api
echo ----------------------------------------
echo   管理员: admin / admin123
echo   用  户: user001 / user123
echo ========================================
goto :eof

:start_redis
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":6379.*LISTENING" 2^>nul') do set "REDIS_PID=%%a"
if defined REDIS_PID (
    echo [OK] Redis 已运行 ^(PID: %REDIS_PID%^)
) else (
    echo [..] 启动 Redis...
    if exist "%~dp0redis\redis-server.exe" (
        start /min "Redis" "%~dp0redis\redis-server.exe" "%~dp0redis\redis.windows.conf"
        ping -n 2 127.0.0.1 >nul 2>&1
        echo [OK] Redis 已启动
    ) else (
        echo [WARN] Redis 未安装
    )
)
goto :eof

:start_backend
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080.*LISTENING" 2^>nul') do set "BACKEND_PID=%%a"
if defined BACKEND_PID (
    echo [OK] 后端已运行 ^(PID: %BACKEND_PID%, 端口: 8080^)
) else (
    echo [..] 启动后端服务...
    cd /d "%~dp0backend"
    start "后端服务" cmd /c "title 后端-8080 && set SPRING_PROFILES_ACTIVE=payment && mvn spring-boot:run -q"
    echo [OK] 后端启动中 ^(约30秒后就绪^)
)
goto :eof

:start_frontend
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3003.*LISTENING" 2^>nul') do set "FRONTEND_PID=%%a"
if defined FRONTEND_PID (
    echo [OK] 前端已运行 ^(PID: %FRONTEND_PID%, 端口: 3003^)
) else (
    echo [..] 启动前端服务...
    cd /d "%~dp0frontend"
    start "前端服务" cmd /c "title 前端-3003 && npm run dev"
    echo [OK] 前端启动中 ^(约5秒后就绪^)
)
goto :eof