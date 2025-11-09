@echo off
title MCP Request Server
color 0A

echo ================================
echo   MCP Request Server 启动脚本
echo ================================
echo.

echo 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% == 0 (
    echo Node.js已安装，版本: 
    node --version
    echo.
) else (
    echo 错误：未检测到Node.js安装
    echo 请先安装Node.js环境
    echo.
    echo 按任意键退出...
    pause >nul
    exit /b 1
)

echo 启动MCP Request服务...
echo ================================
npx -y mcp-request@latest

echo.
echo 脚本执行结束
pause