@echo off
echo ========================================
echo 汽车洗车预约系统 - 依赖安装脚本
echo ========================================

echo.
echo 正在检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请先安装JDK 17
    pause
    exit /b 1
)

echo.
echo 正在检查Maven环境...
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo 警告: 未找到Maven命令，尝试使用Maven Wrapper...
    if exist "mvnw.cmd" (
        echo 使用Maven Wrapper安装依赖...
        call mvnw.cmd clean install -DskipTests
    ) else (
        echo 错误: 未找到Maven或Maven Wrapper
        echo 请安装Maven或使用IDE的Maven功能
        pause
        exit /b 1
    )
) else (
    echo 使用系统Maven安装依赖...
    mvn clean install -DskipTests
)

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 依赖安装成功！
    echo ========================================
    echo.
    echo 接下来可以：
    echo 1. 在IDE中启动 CarwashApplication.java
    echo 2. 或运行: mvn spring-boot:run
    echo 3. 访问: http://localhost:8080/api/test/health
    echo 4. API文档: http://localhost:8080/doc.html
    echo.
) else (
    echo.
    echo ========================================
    echo 依赖安装失败！
    echo ========================================
    echo 请检查网络连接和Maven配置
)

pause