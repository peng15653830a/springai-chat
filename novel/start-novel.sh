#!/bin/bash

echo "🚀 启动Novel模块 - AI长文本创作助手"
echo "=================================="

# 检查Ollama服务
echo "📋 检查Ollama服务状态..."
if curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "✅ Ollama服务运行正常"
else
    echo "❌ Ollama服务未运行，请先启动Ollama:"
    echo "   ollama serve"
    exit 1
fi

# 检查模型
echo "📋 检查可用模型..."
MODELS=$(curl -s http://localhost:11434/api/tags | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
if [ -z "$MODELS" ]; then
    echo "⚠️  未发现已下载的模型，建议下载:"
    echo "   ollama pull llama3"
    echo "   ollama pull qwen2.5:7b"
else
    echo "✅ 可用模型: $MODELS"
fi

# 启动Novel后端
echo "🔧 启动Novel后端 (端口8083)..."
mvn spring-boot:run &
BACKEND_PID=$!

# 等待后端启动
echo "⏳ 等待后端服务启动..."
for i in {1..30}; do
    if curl -s http://localhost:8083/actuator/health > /dev/null; then
        echo "✅ Novel后端启动成功"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        echo "❌ Novel后端启动超时"
        kill $BACKEND_PID 2>/dev/null
        exit 1
    fi
done

echo ""
echo "🎉 Novel模块启动完成!"
echo "=================================="
echo "📍 Novel后端: http://localhost:8083"
echo "📍 Portal前端: http://localhost:5174 (需要单独启动)"
echo ""
echo "🎯 接下来请启动Portal前端:"
echo "   cd ../apps/portal-frontend"
echo "   npm run dev"
echo ""
echo "💡 访问 http://localhost:5174 并点击 'Novel 创作' 开始使用"
echo ""
echo "🛑 按 Ctrl+C 停止服务"

# 等待用户中断
wait $BACKEND_PID