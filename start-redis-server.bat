@echo off
title Redis Server
color 0A

echo ================================
echo   Redis Server 启动脚本
echo ================================
echo.

REM 检查项目目录中的Redis是否可用
echo 检查本地Redis安装...
if exist "redis\redis-server.exe" (
    echo 启动本地Redis服务...
    cd redis
    start "Redis Server" redis-server.exe redis.windows.conf
    cd ..
    timeout /t 5 >nul
    goto start_mcp
) else (
    echo 错误：未检测到Redis安装
    echo 请先安装Redis服务，参考 Redis安装和配置指南.md
    goto error
)

:start_mcp
echo.
echo 验证Redis连接...
redis-cli ping >nul 2>&1
if %errorlevel% == 0 (
    echo Redis连接正常，启动MCP Server Redis...
    echo.
    npx -y @modelcontextprotocol/server-redis redis://localhost:6379
) else (
    echo Redis连接失败，请检查Redis服务状态
    goto error
)

:error
echo.
echo Redis服务启动失败，请参考 Redis安装和配置指南.md 进行配置
echo.

:end
echo.
echo 脚本执行结束
pause