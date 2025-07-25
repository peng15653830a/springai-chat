#!/bin/bash

echo "🛑 停止AI聊天应用..."

# 读取PID文件
if [ -f ".backend.pid" ]; then
    BACKEND_PID=$(cat .backend.pid)
    echo "停止后端服务 (PID: $BACKEND_PID)..."
    kill $BACKEND_PID 2>/dev/null
    rm .backend.pid
    echo "✅ 后端服务已停止"
else
    echo "❌ 未找到后端PID文件"
fi

if [ -f ".frontend.pid" ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    echo "停止前端服务 (PID: $FRONTEND_PID)..."
    kill $FRONTEND_PID 2>/dev/null
    rm .frontend.pid
    echo "✅ 前端服务已停止"
else
    echo "❌ 未找到前端PID文件"
fi

# 清理可能残留的进程
echo "清理残留进程..."
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "vite" 2>/dev/null

echo "🎉 所有服务已停止"