@echo off
chcp 65001 >nul
echo ====================================
echo Redis 服务自动启动脚本
echo ====================================

REM 检查 6379 端口是否被占用
netstat -ano | findstr ":6379" >nul 2>&1

if %errorlevel% equ 0 (
    echo [信息] Redis 服务已在运行中 (端口 6379)
    goto :end
) else (
    echo [信息] Redis 服务未启动，正在启动...
    
    REM 启动 Redis 服务
    start /min "" "%~dp0redis\redis-server.exe" "%~dp0redis\redis.windows.conf"
    
    REM 等待 3 秒让服务完全启动
    timeout /t 3 /nobreak >nul
    
    REM 验证服务是否成功启动
    netstat -ano | findstr ":6379" >nul 2>&1
    
    if %errorlevel% equ 0 (
        echo [成功] Redis 服务已成功启动
    ) else (
        echo [错误] Redis 服务启动失败
        exit /b 1
    )
)

:end
echo ====================================
echo Redis 服务状态检查完成
echo ====================================
