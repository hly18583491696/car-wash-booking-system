@echo off
title Alipay MCP Server
color 0A

echo ================================
echo   Alipay MCP Server 启动脚本
echo ================================
echo.

REM 检查是否已设置环境变量，如果没有则从配置文件加载
if "%AP_APP_ID%"=="" (
    echo 未检测到环境变量，正在从 .env.alipay 文件加载配置...
    if exist ".env.alipay" (
        for /f "usebackq tokens=*" %%i in (".env.alipay") do (
            set %%i
        )
        echo 配置文件加载完成。
    ) else (
        echo 警告: 未找到 .env.alipay 配置文件
    )
) else (
    echo 检测到已设置的环境变量。
)

echo.
echo 当前配置:
echo AP_APP_ID=%AP_APP_ID%
echo AP_NOTIFY_URL=%AP_NOTIFY_URL%
echo AP_RETURN_URL=%AP_RETURN_URL%
echo.

REM 检查必要环境变量
if "%AP_APP_ID%"=="" (
    echo 错误: 缺少 AP_APP_ID 环境变量
    echo 请在 .env.alipay 文件中设置正确的支付宝应用ID
    goto error
)

if "%AP_APP_KEY%"=="" (
    echo 错误: 缺少 AP_APP_KEY 环境变量
    echo 请在 .env.alipay 文件中设置正确的支付宝应用私钥
    goto error
)

if "%AP_PUB_KEY%"=="" (
    echo 错误: 缺少 AP_PUB_KEY 环境变量
    echo 请在 .env.alipay 文件中设置正确的支付宝公钥
    goto error
)

echo 所有必需的环境变量已设置，正在启动 Alipay MCP Server...
echo.
echo 如果出现 "context deadline exceeded" 错误，请检查:
echo 1. 网络连接是否正常
echo 2. 防火墙设置是否阻止了网络请求
echo 3. 配置的支付宝密钥是否正确
echo 4. 是否能访问支付宝网关地址
echo.
echo 按任意键开始启动...
pause >nul

npx -y @alipay/mcp-server-alipay
goto end

:error
echo.
echo 启动失败，请检查配置后重试。
echo.

:end
echo.
echo 脚本执行结束。
pause