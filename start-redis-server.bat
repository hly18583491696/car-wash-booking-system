@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 快速模式：传入 -q 或 --quiet 参数
set "QUIET=0"
if "%1"=="-q" set "QUIET=1"
if "%1"=="--quiet" set "QUIET=1"

if "%QUIET%"=="0" (
    echo ====================================
    echo   Redis 服务快速启动脚本 v2.0
    echo ====================================
)

REM 检查 6379 端口是否被占用
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":6379.*LISTENING" 2^>nul') do set "REDIS_PID=%%a"

if defined REDIS_PID (
    if "%QUIET%"=="0" echo [OK] Redis 已运行 ^(PID: %REDIS_PID%, 端口: 6379^)
    goto :success
)

if "%QUIET%"=="0" echo [..] 正在启动 Redis 服务...

REM 检查 Redis 可执行文件是否存在
if not exist "%~dp0redis\redis-server.exe" (
    echo [ERROR] 找不到 Redis: %~dp0redis\redis-server.exe
    exit /b 1
)

REM 启动 Redis 服务（最小化窗口）
start /min "Redis-Server" "%~dp0redis\redis-server.exe" "%~dp0redis\redis.windows.conf"

REM 快速轮询检测（最多等待5秒，每0.5秒检查一次）
set "RETRY=0"
:wait_loop
if %RETRY% geq 10 goto :timeout
ping -n 1 -w 500 127.0.0.1 >nul 2>&1
netstat -ano | findstr ":6379.*LISTENING" >nul 2>&1
if %errorlevel% equ 0 goto :started
set /a RETRY+=1
goto :wait_loop

:started
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":6379.*LISTENING" 2^>nul') do set "REDIS_PID=%%a"
if "%QUIET%"=="0" echo [OK] Redis 启动成功 ^(PID: %REDIS_PID%, 耗时: ~%RETRY%00ms^)
goto :success

:timeout
echo [ERROR] Redis 启动超时，请检查配置
exit /b 1

:success
if "%QUIET%"=="0" (
    echo ------------------------------------
    echo   redis://localhost:6379
    echo ====================================
)
exit /b 0
