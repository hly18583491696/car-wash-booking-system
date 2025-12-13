@echo off
chcp 65001 >nul

REM 参数解析：-b 仅后端, -f 仅前端
set "MODE=all"
if "%1"=="-b" set "MODE=backend"
if "%1"=="-f" set "MODE=frontend"
if "%1"=="--backend" set "MODE=backend"
if "%1"=="--frontend" set "MODE=frontend"

echo ========================================
echo   停止服务
echo ========================================

set "STOPPED=0"

if "%MODE%" neq "frontend" (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080.*LISTENING" 2^>nul') do (
        echo [..] 终止后端 PID: %%a
        taskkill /f /pid %%a >nul 2>&1
        set "STOPPED=1"
    )
)

if "%MODE%" neq "backend" (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3003.*LISTENING" 2^>nul') do (
        echo [..] 终止前端 PID: %%a
        taskkill /f /pid %%a >nul 2>&1
        set "STOPPED=1"
    )
)

if "%STOPPED%"=="0" (
    echo [OK] 无运行中的服务
) else (
    echo [OK] 服务已停止
)
echo ========================================
