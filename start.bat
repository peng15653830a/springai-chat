@echo off
setlocal enabledelayedexpansion
chcp 65001 > nul

echo 🚀 启动AI聊天应用...
echo.

REM 检查Java版本
echo 📋 检查环境...
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    set JAVA_VERSION=!JAVA_VERSION:"=!
)
echo Java版本: !JAVA_VERSION!

REM 检查Node.js版本
node --version > nul 2>&1
if %errorlevel% equ 0 (
    for /f %%i in ('node --version') do set NODE_VERSION=%%i
    echo Node.js版本: !NODE_VERSION!
) else (
    echo Node.js版本: 未安装
    echo ❌ 请先安装Node.js
    pause
    exit /b 1
)

REM 跳过Maven检查，直接开始编译
echo ✅ 环境检查完成，开始编译...

echo.

REM 编译后端代码（如果需要）
echo 📦 编译后端代码...
set MAVEN_OPTS=-Dfile.encoding=UTF-8
call mvn clean compile
if %errorlevel% neq 0 (
    echo ❌ 编译失败
    pause
    exit /b 1
) else (
    echo ✅ 编译成功
)

REM 启动后端服务
echo 📦 启动后端服务...
start /b "" cmd /c "set MAVEN_OPTS=-Dfile.encoding=UTF-8 && mvn spring-boot:run > backend.log 2>&1"

REM 等待后端启动
echo 等待后端服务启动...
timeout /t 15 /nobreak > nul

REM 检查后端是否启动成功
echo 检查后端服务状态...
echo 尝试访问 http://localhost:8080/actuator/health
powershell -command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 10; Write-Host '后端健康检查通过'; exit 0 } catch { Write-Host '后端健康检查失败:' $_.Exception.Message; exit 1 }"
if %errorlevel% equ 0 (
    echo ✅ 后端服务启动成功 ^(http://localhost:8080^)
) else (
    echo ⚠️ 后端服务可能还在启动中，继续启动前端...
)

REM 启动前端服务
echo 🌐 启动前端服务...
cd frontend

if not exist "node_modules" (
    echo 📦 安装前端依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo ❌ 安装前端依赖失败
        cd ..
        pause
        exit /b 1
    ) else (
        echo ✅ 前端依赖安装成功
    )
)

REM 启动前端开发服务器
echo 🚀 启动前端开发服务器...
start /b "" cmd /c "npm run dev > ../frontend.log 2>&1"

cd ..
echo ✅ 前端服务已启动

echo.
echo 🎉 应用启动完成！
echo.
echo 📍 访问地址:
echo    前端: http://localhost:3000
echo    后端: http://localhost:8080
echo    H2数据库: http://localhost:8080/h2-console
echo.
echo 📝 查看日志:
echo    后端: type backend.log
echo    前端: type frontend.log
echo.
echo 🛑 停止服务请运行: stop.bat
echo.

REM 等待用户输入退出
echo 按任意键退出...
pause > nul