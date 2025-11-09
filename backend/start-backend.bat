@echo off
echo ========================================
echo 汽车洗车预约系统 - 后端服务启动脚本
echo ========================================

echo 正在启动后端服务...
cd /d "%~dp0"

REM 设置Java环境变量
set JAVA_HOME=C:\Program Files\Java\jdk-21

REM 启动Spring Boot应用（使用可执行 JAR）
java -Dspring.profiles.active=dev ^
     -Dserver.port=8080 ^
     -Dspring.datasource.url=jdbc:mysql://localhost:3306/carwash_db?useUnicode=true^&characterEncoding=utf8^&useSSL=false^&serverTimezone=Asia/Shanghai ^
     -Dspring.datasource.username=root ^
     -Dspring.datasource.password=123456 ^
     -jar "target/carwash-reservation-system-1.0.0.jar"

pause