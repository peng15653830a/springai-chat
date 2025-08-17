#!/bin/bash

# 跨平台停止脚本 - 兼容Mac M1和Ubuntu 22
# Author: xupeng
# Version: 2.0

echo "🛑 停止AI聊天应用..."

# 检测操作系统
OS_TYPE="unknown"
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS_TYPE="macos"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS_TYPE="linux"
elif [ -f ".os_type" ]; then
    OS_TYPE=$(cat .os_type)
fi

echo "操作系统: $OS_TYPE"

# 优雅停止进程
graceful_kill() {
    local pid=$1
    local name=$2
    local timeout=10
    
    if ! kill -0 $pid 2>/dev/null; then
        echo "进程 $name (PID: $pid) 已经停止"
        return 0
    fi
    
    echo "正在停止 $name (PID: $pid)..."
    
    # 先发送TERM信号
    kill -TERM $pid 2>/dev/null
    
    # 等待进程优雅退出
    local count=0
    while [ $count -lt $timeout ] && kill -0 $pid 2>/dev/null; do
        sleep 1
        ((count++))
        echo -n "."
    done
    
    # 如果还没退出，强制杀死
    if kill -0 $pid 2>/dev/null; then
        echo ""
        echo "强制停止 $name..."
        kill -KILL $pid 2>/dev/null
        sleep 1
    fi
    
    if kill -0 $pid 2>/dev/null; then
        echo "❌ 无法停止进程 $name (PID: $pid)"
        return 1
    else
        echo ""
        echo "✅ $name 已停止"
        return 0
    fi
}

# 读取并停止后端服务
if [ -f ".backend.pid" ]; then
    BACKEND_PID=$(cat .backend.pid)
    graceful_kill $BACKEND_PID "后端服务"
    rm -f .backend.pid
else
    echo "❌ 未找到后端PID文件"
fi

# 读取并停止前端服务
if [ -f ".frontend.pid" ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    graceful_kill $FRONTEND_PID "前端服务"
    rm -f .frontend.pid
else
    echo "❌ 未找到前端PID文件"
fi

# 清理可能残留的进程 - 跨平台兼容
echo "🧹 清理残留进程..."

case "$OS_TYPE" in
    "macos")
        # macOS使用pgrep/pkill
        if pgrep -f "spring-boot:run" > /dev/null 2>&1; then
            echo "发现残留的Spring Boot进程，正在清理..."
            pkill -f "spring-boot:run" 2>/dev/null
        fi
        
        if pgrep -f "vite.*--port.*3000" > /dev/null 2>&1; then
            echo "发现残留的Vite进程，正在清理..."
            pkill -f "vite.*--port.*3000" 2>/dev/null
        fi
        
        # 清理可能的npm/node进程
        if pgrep -f "node.*vite" > /dev/null 2>&1; then
            echo "发现残留的Node.js进程，正在清理..."
            pkill -f "node.*vite" 2>/dev/null
        fi
        ;;
    "linux")
        # Linux使用pgrep/pkill
        if pgrep -f "spring-boot:run" > /dev/null 2>&1; then
            echo "发现残留的Spring Boot进程，正在清理..."
            pkill -f "spring-boot:run" 2>/dev/null
        fi
        
        if pgrep -f "vite" > /dev/null 2>&1; then
            echo "发现残留的Vite进程，正在清理..."
            pkill -f "vite" 2>/dev/null
        fi
        
        # 检查端口占用
        if ss -ltn | grep -q ":8080 "; then
            echo "端口8080仍被占用，尝试清理..."
            fuser -k 8080/tcp 2>/dev/null || true
        fi
        
        if ss -ltn | grep -q ":3000 "; then
            echo "端口3000仍被占用，尝试清理..."
            fuser -k 3000/tcp 2>/dev/null || true
        fi
        ;;
    *)
        # 通用清理方法
        echo "使用通用清理方法..."
        pkill -f "spring-boot:run" 2>/dev/null || true
        pkill -f "vite" 2>/dev/null || true
        pkill -f "mvn spring-boot:run" 2>/dev/null || true
        ;;
esac

# 清理临时文件
echo "🧹 清理临时文件..."
rm -f .backend.pid .frontend.pid .os_type

# 最终验证
echo ""
echo "🔍 验证服务是否完全停止..."

# 检查端口8080和3000是否还被占用
port_check() {
    local port=$1
    local service=$2
    
    case "$OS_TYPE" in
        "macos")
            if lsof -Pi :$port -sTCP:LISTEN -t &> /dev/null; then
                echo "⚠️ 端口 $port ($service) 仍被占用"
                return 1
            fi
            ;;
        "linux")
            if ss -ltn | grep -q ":$port "; then
                echo "⚠️ 端口 $port ($service) 仍被占用"
                return 1
            fi
            ;;
        *)
            # 使用netstat作为后备方案
            if netstat -ln 2>/dev/null | grep -q ":$port "; then
                echo "⚠️ 端口 $port ($service) 仍被占用"
                return 1
            fi
            ;;
    esac
    
    echo "✅ 端口 $port ($service) 已释放"
    return 0
}

port_check 8080 "后端"
port_check 3000 "前端"

echo ""
echo "🎉 所有服务已完全停止"
echo ""
echo "💡 提示:"
echo "   - 如果需要重新启动，运行: ./start.sh"
echo "   - 或使用跨平台版本: ./start-cross-platform.sh"
echo ""