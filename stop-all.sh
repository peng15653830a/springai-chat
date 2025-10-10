#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${RED}🛑 停止 SpringAI Chat 完整项目${NC}"
echo "=================================================="

# 从PID文件停止服务
stop_service() {
    local service_name=$1
    local pid_file=".pids/${service_name}.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${YELLOW}🔄 停止 ${service_name} (PID: $pid)...${NC}"
            kill "$pid"

            # 等待进程结束
            for i in {1..10}; do
                if ! ps -p "$pid" > /dev/null 2>&1; then
                    echo -e "${GREEN}✅ ${service_name} 已停止${NC}"
                    break
                fi
                sleep 1
                if [ $i -eq 10 ]; then
                    echo -e "${RED}⚠️  强制终止 ${service_name}${NC}"
                    kill -9 "$pid" 2>/dev/null
                fi
            done
        else
            echo -e "${YELLOW}⚠️  ${service_name} 进程不存在 (PID: $pid)${NC}"
        fi
        rm -f "$pid_file"
    else
        echo -e "${YELLOW}⚠️  ${service_name} PID文件不存在${NC}"
    fi
}

# 按照依赖关系倒序停止服务
echo -e "${YELLOW}🔄 停止前端服务...${NC}"
stop_service "portal-frontend"

echo -e "\n${YELLOW}🔄 停止后端服务...${NC}"
stop_service "chat-backend"
stop_service "novel-backend"
stop_service "mcp-client"
stop_service "mcp-server"

# 额外清理：通过端口号强制停止
echo -e "\n${YELLOW}🧹 清理残留进程...${NC}"

# 停止可能的残留Maven进程
MAVEN_PIDS=$(ps aux | grep "[m]vn spring-boot:run" | awk '{print $2}')
if [ -n "$MAVEN_PIDS" ]; then
    echo -e "${YELLOW}🔄 停止Maven进程...${NC}"
    echo "$MAVEN_PIDS" | xargs kill 2>/dev/null
    sleep 2
    echo "$MAVEN_PIDS" | xargs kill -9 2>/dev/null
fi

# 停止可能的残留Node进程
NODE_PIDS=$(ps aux | grep "[n]ode.*vite\|[n]pm run dev" | awk '{print $2}')
if [ -n "$NODE_PIDS" ]; then
    echo -e "${YELLOW}🔄 停止Node进程...${NC}"
    echo "$NODE_PIDS" | xargs kill 2>/dev/null
    sleep 2
    echo "$NODE_PIDS" | xargs kill -9 2>/dev/null
fi

# 通过端口强制停止
check_and_kill_port() {
    local port=$1
    local service_name=$2

    local pid=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}🔄 停止端口 $port 上的 $service_name (PID: $pid)...${NC}"
        kill "$pid" 2>/dev/null
        sleep 2
        if lsof -ti:$port > /dev/null 2>&1; then
            kill -9 "$pid" 2>/dev/null
        fi
    fi
}

check_and_kill_port 5174 "Portal前端"
check_and_kill_port 8080 "Chat后端"
check_and_kill_port 8081 "MCP Client"
check_and_kill_port 8082 "MCP Server"
check_and_kill_port 8083 "Novel后端"

# 清理PID文件目录
echo -e "\n${YELLOW}🧹 清理PID文件...${NC}"
rm -rf .pids/

# 验证停止结果
echo -e "\n${BLUE}📊 验证停止状态:${NC}"
ports=(5174 8080 8081 8082 8083)
services=("Portal前端" "Chat后端" "MCP Client" "MCP Server" "Novel后端")

all_stopped=true
for i in "${!ports[@]}"; do
    port=${ports[$i]}
    service=${services[$i]}

    if lsof -ti:$port > /dev/null 2>&1; then
        echo -e "${RED}❌ ${service} (端口 $port) 仍在运行${NC}"
        all_stopped=false
    else
        echo -e "${GREEN}✅ ${service} (端口 $port) 已停止${NC}"
    fi
done

echo ""
if [ "$all_stopped" = true ]; then
    echo -e "${GREEN}🎉 所有服务已成功停止!${NC}"
else
    echo -e "${YELLOW}⚠️  部分服务可能仍在运行，请手动检查${NC}"
    echo -e "${YELLOW}💡 可以使用以下命令检查:${NC}"
    echo "   netstat -tuln | grep -E ':(5174|8080|8081|8082|8083)'"
    echo "   ps aux | grep -E 'mvn|node|vite'"
fi

echo ""
echo -e "${BLUE}📋 其他管理命令:${NC}"
echo "   🚀 重新启动:      ./start-all.sh"
echo "   📊 查看状态:      ./status.sh"
echo "   📝 查看日志:      ls -la logs/"