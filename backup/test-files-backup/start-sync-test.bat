@echo off
chcp 65001 >nul
echo ========================================
echo 汽车洗车服务预约系统 - 数据同步测试启动
echo ========================================
echo.

echo [1/4] 检查环境...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js 未安装，请先安装 Node.js
    pause
    exit /b 1
)

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java 未安装，请先安装 Java JDK 11+
    pause
    exit /b 1
)

where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  MySQL 命令行工具未找到，请确保 MySQL 已安装并添加到 PATH
)

echo ✅ 环境检查完成

echo.
echo [2/4] 启动 MySQL 服务...
net start mysql80 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL 服务启动成功
) else (
    echo ⚠️  MySQL 服务启动失败或已在运行
)

echo.
echo [3/4] 启动后端服务...
cd /d "%~dp0backend"
start "后端服务" cmd /k "echo 启动后端服务... && mvn spring-boot:run"
echo ✅ 后端服务启动中...

echo.
echo [4/4] 启动前端服务...
cd /d "%~dp0frontend"
start "前端服务" cmd /k "echo 启动前端服务... && npm run dev"
echo ✅ 前端服务启动中...

echo.
echo ========================================
echo 🚀 服务启动完成！
echo ========================================
echo.
echo 📋 访问地址：
echo   • 前端主页: http://localhost:3003
echo   • 数据同步测试: http://localhost:3003/data-sync-test
echo   • 后台管理: http://localhost:3003/admin
echo   • 后端API: http://localhost:8080/api
echo.
echo 🔧 测试步骤：
echo   1. 等待服务完全启动（约30-60秒）
echo   2. 访问数据同步测试页面
echo   3. 点击"快速诊断"检测问题
echo   4. 点击"快速修复"解决问题
echo   5. 进行各项功能测试
echo.
echo 📝 注意事项：
echo   • 确保端口 3003 和 8080 未被占用
echo   • 首次启动可能需要下载依赖，请耐心等待
echo   • 如遇问题，请查看各服务窗口的错误信息
echo.

timeout /t 5 >nul
echo 🌐 正在打开测试页面...
start http://localhost:3003/data-sync-test

echo.
echo 按任意键退出...
pause >nul