@echo off
chcp 65001 >nul
echo ====================================
echo MCP Server Redis 自动启动脚本
echo ====================================
echo.

REM 先启动 Redis 服务
echo [步骤1/2] 检查并启动 Redis 服务...
call "%~dp0start-redis-server.bat"

if %errorlevel% neq 0 (
    echo [错误] Redis 服务启动失败，无法继续
    pause
    exit /b 1
)

echo.
echo [步骤2/2] 启动 MCP Server Redis...
echo.

REM 启动 MCP Server Redis
npx -y @modelcontextprotocol/server-redis redis://localhost:6379

pause
