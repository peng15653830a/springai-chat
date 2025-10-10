#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}📊 SpringAI Chat 项目状态检查${NC}"
echo "=================================================="

# 检查服务状态
check_service_status() {
    local port=$1
    local service_name=$2
    local health_endpoint=$3

    # 检查端口是否被监听
    if lsof -ti:$port > /dev/null 2>&1; then
        local pid=$(lsof -ti:$port)
        echo -e "${GREEN}✅ ${service_name}${NC} (端口 $port, PID: $pid)"

        # 如果有健康检查端点，检查服务健康状态
        if [ -n "$health_endpoint" ]; then
            if curl -s "$health_endpoint" > /dev/null 2>&1; then
                echo -e "   ${GREEN}🔄 服务健康状态: 正常${NC}"
            else
                echo -e "   ${YELLOW}⚠️  服务健康状态: 异常或启动中${NC}"
            fi
        fi
    else
        echo -e "${RED}❌ ${service_name}${NC} (端口 $port) - 未运行"
        return 1
    fi
    return 0
}

# 检查各个服务
echo -e "\n${YELLOW}🔍 检查服务状态:${NC}"
running_count=0
total_count=5

if check_service_status 5174 "Portal前端" "http://localhost:5174"; then
    ((running_count++))
fi

if check_service_status 8080 "Chat后端" "http://localhost:8080/actuator/health"; then
    ((running_count++))
fi

if check_service_status 8081 "MCP Client" ""; then
    ((running_count++))
fi

if check_service_status 8082 "MCP Server" ""; then
    ((running_count++))
fi

if check_service_status 8083 "Novel后端" "http://localhost:8083/actuator/health"; then
    ((running_count++))
fi

# 检查Ollama服务
echo -e "\n${YELLOW}🔍 检查外部依赖:${NC}"
if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Ollama服务${NC} (端口 11434)"

    # 获取可用模型
    models=$(curl -s http://localhost:11434/api/tags | jq -r '.models[]?.name' 2>/dev/null)
    if [ -n "$models" ]; then
        echo -e "   ${GREEN}📚 可用模型:${NC}"
        echo "$models" | head -5 | sed 's/^/      • /'
        model_count=$(echo "$models" | wc -l)
        if [ "$model_count" -gt 5 ]; then
            echo -e "      ${PURPLE}... 还有 $((model_count - 5)) 个模型${NC}"
        fi
    else
        echo -e "   ${YELLOW}⚠️  未检测到已下载的模型${NC}"
    fi
else
    echo -e "${RED}❌ Ollama服务${NC} (端口 11434) - 未运行"
    echo -e "   ${YELLOW}💡 启动命令: ollama serve${NC}"
fi

# 检查PID文件
echo -e "\n${YELLOW}🔍 检查PID文件:${NC}"
if [ -d ".pids" ]; then
    pid_files=$(ls .pids/*.pid 2>/dev/null | wc -l)
    if [ "$pid_files" -gt 0 ]; then
        echo -e "${GREEN}📁 PID文件目录存在 ($pid_files 个文件)${NC}"
        for pid_file in .pids/*.pid; do
            if [ -f "$pid_file" ]; then
                service_name=$(basename "$pid_file" .pid)
                pid=$(cat "$pid_file")
                if ps -p "$pid" > /dev/null 2>&1; then
                    echo -e "   ${GREEN}✅ $service_name (PID: $pid)${NC}"
                else
                    echo -e "   ${RED}❌ $service_name (PID: $pid) - 进程不存在${NC}"
                fi
            fi
        done
    else
        echo -e "${YELLOW}📁 PID文件目录为空${NC}"
    fi
else
    echo -e "${YELLOW}📁 PID文件目录不存在${NC}"
fi

# 检查日志文件
echo -e "\n${YELLOW}🔍 检查日志文件:${NC}"
if [ -d "logs" ]; then
    log_files=$(ls logs/*.log 2>/dev/null | wc -l)
    if [ "$log_files" -gt 0 ]; then
        echo -e "${GREEN}📁 日志目录存在 ($log_files 个文件)${NC}"
        for log_file in logs/*.log; do
            if [ -f "$log_file" ]; then
                service_name=$(basename "$log_file" .log)
                file_size=$(du -h "$log_file" | cut -f1)
                echo -e "   📄 $service_name ($file_size)"
            fi
        done
    else
        echo -e "${YELLOW}📁 日志目录为空${NC}"
    fi
else
    echo -e "${YELLOW}📁 日志目录不存在${NC}"
fi

# 显示总体状态
echo ""
echo "=================================================="
if [ "$running_count" -eq "$total_count" ]; then
    echo -e "${GREEN}🎉 系统状态: 全部服务运行正常 ($running_count/$total_count)${NC}"
elif [ "$running_count" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  系统状态: 部分服务运行 ($running_count/$total_count)${NC}"
else
    echo -e "${RED}❌ 系统状态: 所有服务已停止 ($running_count/$total_count)${NC}"
fi

# 显示快速访问链接
if [ "$running_count" -gt 0 ]; then
    echo ""
    echo -e "${BLUE}🔗 快速访问:${NC}"
    if lsof -ti:5174 > /dev/null 2>&1; then
        echo -e "   🌐 Portal前端:    ${PURPLE}http://localhost:5174${NC}"
        echo -e "   💬 聊天助手:      ${PURPLE}http://localhost:5174/chat${NC}"
        echo -e "   📖 Novel创作:     ${PURPLE}http://localhost:5174/novel${NC}"
        echo -e "   🔧 MCP工具:       ${PURPLE}http://localhost:5174/mcp${NC}"
    fi
    if lsof -ti:8080 > /dev/null 2>&1; then
        echo -e "   🔧 Chat API:      ${PURPLE}http://localhost:8080/actuator/health${NC}"
    fi
    if lsof -ti:8083 > /dev/null 2>&1; then
        echo -e "   📝 Novel API:     ${PURPLE}http://localhost:8083/actuator/health${NC}"
    fi
fi

echo ""
echo -e "${BLUE}📋 管理命令:${NC}"
echo -e "   🚀 启动所有服务:  ${PURPLE}./start-all.sh${NC}"
echo -e "   🛑 停止所有服务:  ${PURPLE}./stop-all.sh${NC}"
echo -e "   🔄 重启项目:      ${PURPLE}./stop-all.sh && ./start-all.sh${NC}"
echo -e "   📝 查看实时日志:  ${PURPLE}tail -f logs/[service].log${NC}"