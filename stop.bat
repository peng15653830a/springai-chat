@echo off
chcp 65001 > nul

echo 🛑 停止AI聊天应用...
echo.

REM 停止占用8080端口的进程（后端）
echo 停止后端服务...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    set pid=%%a
    if !pid! gtr 0 (
        taskkill /F /PID !pid! > nul 2>&1
        if !errorlevel! equ 0 (
            echo ✅ 已停止后端服务 ^(PID: !pid!^)
        )
    )
)

REM 停止占用3000端口的进程（前端）
echo 停止前端服务...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3000') do (
    set pid=%%a
    if !pid! gtr 0 (
        taskkill /F /PID !pid! > nul 2>&1
        if !errorlevel! equ 0 (
            echo ✅ 已停止前端服务 ^(PID: !pid!^)
        )
    )
)

REM 停止所有Java进程中包含spring-boot的
echo 停止Spring Boot进程...
for /f "tokens=2" %%a in ('tasklist /fi "imagename eq java.exe" /fo csv ^| findstr spring-boot') do (
    taskkill /F /PID %%a > nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 已停止Spring Boot进程 ^(PID: %%a^)
    )
)

REM 停止Node.js进程
echo 停止Node.js进程...
taskkill /F /IM node.exe > nul 2>&1
if !errorlevel! equ 0 (
    echo ✅ 已停止Node.js进程
)

REM 清理PID文件
if exist ".backend.pid" del ".backend.pid"
if exist ".frontend.pid" del ".frontend.pid"

echo.
echo 🎉 所有服务已停止
pause