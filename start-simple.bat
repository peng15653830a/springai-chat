@echo off
echo 🚀 启动AI聊天应用...
echo.

echo 📦 编译后端...
mvn clean compile
echo.

echo 🚀 启动后端服务...
start "Backend" cmd /c "mvn spring-boot:run"
echo 后端服务正在启动...

echo 等待10秒...
timeout /t 10 /nobreak

echo 🌐 启动前端...
cd frontend
if not exist node_modules (
    echo 安装依赖...
    npm install
)
start "Frontend" cmd /c "npm run dev"
cd ..

echo.
echo 🎉 应用启动完成！
echo 前端: http://localhost:3000
echo 后端: http://localhost:8080
echo.
pause