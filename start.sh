#!/bin/bash

echo "🚀 启动AI聊天应用..."

# 检查Java版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java版本: $java_version"

# 检查Node.js版本
node_version=$(node --version 2>/dev/null || echo "未安装")
echo "Node.js版本: $node_version"

# 启动后端服务
echo "📦 启动后端服务..."
if [ ! -f "target/classes/com/example/springai/SpringaiApplication.class" ]; then
    echo "编译后端代码..."
    mvn clean compile
fi

# 在后台启动Spring Boot
mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo "后端服务PID: $BACKEND_PID"

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 检查后端是否启动成功
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ 后端服务启动成功 (http://localhost:8080)"
else
    echo "❌ 后端服务启动失败，请检查日志"
    exit 1
fi

# 启动前端服务
echo "🌐 启动前端服务..."
cd frontend

if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

# 启动前端开发服务器
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端服务PID: $FRONTEND_PID"

cd ..

echo ""
echo "🎉 应用启动完成！"
echo ""
echo "📍 访问地址:"
echo "   前端: http://localhost:3000"
echo "   后端: http://localhost:8080"
echo "   H2数据库: http://localhost:8080/h2-console"
echo ""
echo "📋 进程信息:"
echo "   后端PID: $BACKEND_PID"
echo "   前端PID: $FRONTEND_PID"
echo ""
echo "🛑 停止服务:"
echo "   kill $BACKEND_PID $FRONTEND_PID"
echo ""
echo "📝 查看日志:"
echo "   后端: tail -f backend.log"
echo "   前端: tail -f frontend.log"
echo ""

# 保存PID到文件
echo "$BACKEND_PID" > .backend.pid
echo "$FRONTEND_PID" > .frontend.pid

echo "按 Ctrl+C 停止服务..."
trap "kill $BACKEND_PID $FRONTEND_PID; exit" INT
wait