#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 启动 SpringAI Chat 完整项目${NC}"
echo "=================================================="

# 创建PID文件目录
mkdir -p .pids
mkdir -p logs

# 检查Ollama服务
echo -e "${YELLOW}📋 检查Ollama服务状态...${NC}"
if curl -s http://localhost:11434/api/tags > /dev/null; then
    echo -e "${GREEN}✅ Ollama服务运行正常${NC}"
    MODELS=$(curl -s http://localhost:11434/api/tags | jq -r '.models[]?.name' 2>/dev/null | head -3)
    if [ -n "$MODELS" ]; then
        echo -e "${GREEN}✅ 可用模型:${NC}"
        echo "$MODELS" | sed 's/^/   • /'
    fi
else
    echo -e "${RED}❌ Ollama服务未运行，请先启动:${NC}"
    echo "   ollama serve"
    echo -e "${YELLOW}💡 建议下载模型:${NC}"
    echo "   ollama pull llama3"
    echo "   ollama pull qwen2.5:7b"
    exit 1
fi

# 启动Chat后端（基于聚合工程，自动构建依赖模块）
echo -e "\n${YELLOW}🔧 启动Chat后端 (端口8080)...${NC}"
# 先构建依赖与模块本身，再在子模块POM下运行，避免在父POM上执行 run 目标
mvn -q -f pom.xml -pl chat -am -DskipTests package || { echo -e "${RED}❌ Chat模块构建失败${NC}"; exit 1; }
nohup mvn -q -f chat/pom.xml spring-boot:run > logs/chat-backend.log 2>&1 &
CHAT_PID=$!
echo $CHAT_PID > .pids/chat-backend.pid

# 启动Novel后端（基于聚合工程，自动构建依赖模块）
echo -e "${YELLOW}🔧 启动Novel后端 (端口8083)...${NC}"
mvn -q -f pom.xml -pl novel -am -DskipTests package || { echo -e "${RED}❌ Novel模块构建失败${NC}"; exit 1; }
# 默认启用 Novel 表结构初始化（可通过 NOVEL_DBINIT=false 关闭）
NOVEL_PROFILES=""
if [ "${NOVEL_DBINIT:-true}" = "true" ]; then NOVEL_PROFILES="-Dspring.profiles.active=dbinit"; fi
nohup mvn -q -f novel/pom.xml spring-boot:run ${NOVEL_PROFILES} > logs/novel-backend.log 2>&1 &
NOVEL_PID=$!
echo $NOVEL_PID > .pids/novel-backend.pid

# 启动MCP Server（基于聚合工程）
echo -e "${YELLOW}🔧 启动MCP Server (端口8082)...${NC}"
mvn -q -f pom.xml -pl mcp/mcp-server -am -DskipTests package || { echo -e "${RED}❌ MCP Server构建失败${NC}"; exit 1; }
nohup mvn -q -f mcp/mcp-server/pom.xml spring-boot:run > logs/mcp-server.log 2>&1 &
MCP_SERVER_PID=$!
echo $MCP_SERVER_PID > .pids/mcp-server.pid

# 启动MCP Client（基于聚合工程）
echo -e "${YELLOW}🔧 启动MCP Client (端口8081)...${NC}"
mvn -q -f pom.xml -pl mcp/mcp-client -am -DskipTests package || { echo -e "${RED}❌ MCP Client构建失败${NC}"; exit 1; }
nohup mvn -q -f mcp/mcp-client/pom.xml spring-boot:run > logs/mcp-client.log 2>&1 &
MCP_CLIENT_PID=$!
echo $MCP_CLIENT_PID > .pids/mcp-client.pid

# 等待后端服务启动
echo -e "\n${YELLOW}⏳ 等待后端服务启动...${NC}"

# 检查Chat后端
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Chat后端启动成功${NC}"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Chat后端启动超时${NC}"
        ./stop-all.sh
        exit 1
    fi
done

# 检查Novel后端
for i in {1..30}; do
    if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Novel后端启动成功${NC}"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Novel后端启动超时${NC}"
        ./stop-all.sh
        exit 1
    fi
done

# 检查MCP服务
sleep 5
if netstat -tuln | grep -q ":8081.*LISTEN" 2>/dev/null; then
    echo -e "${GREEN}✅ MCP Client启动成功${NC}"
else
    echo -e "${YELLOW}⚠️  MCP Client可能仍在启动中${NC}"
fi

if netstat -tuln | grep -q ":8082.*LISTEN" 2>/dev/null; then
    echo -e "${GREEN}✅ MCP Server启动成功${NC}"
else
    echo -e "${YELLOW}⚠️  MCP Server可能仍在启动中${NC}"
fi

# 启动前端
echo -e "\n${YELLOW}🎨 启动Portal前端 (端口5174)...${NC}"
cd apps/portal-frontend

# 检查npm依赖
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}📦 安装前端依赖...${NC}"
    npm install
fi

nohup npm run dev > ../../logs/portal-frontend.log 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > ../../.pids/portal-frontend.pid
cd ../..

# 等待前端启动
echo -e "${YELLOW}⏳ 等待前端服务启动...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:5174 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Portal前端启动成功${NC}"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        echo -e "${YELLOW}⚠️  前端启动可能需要更多时间${NC}"
        break
    fi
done

echo ""
echo -e "${GREEN}🎉 项目启动完成!${NC}"
echo "=================================================="
echo -e "${BLUE}📍 服务地址:${NC}"
echo "   🌐 Portal前端:    http://localhost:5174"
echo "   🔧 Chat后端:      http://localhost:8080"
echo "   📝 Novel后端:     http://localhost:8083"
echo "   🛠️  MCP Client:    http://localhost:8081"
echo "   🔌 MCP Server:    http://localhost:8082"
echo ""
echo -e "${BLUE}🎯 功能入口:${NC}"
echo "   💬 聊天助手:      http://localhost:5174/chat"
echo "   📖 Novel创作:     http://localhost:5174/novel"
echo "   🔧 MCP工具:       http://localhost:5174/mcp"
echo ""
echo -e "${BLUE}📋 管理命令:${NC}"
echo "   📊 查看状态:      ./status.sh"
echo "   📝 查看日志:      tail -f logs/*.log"
echo "   🛑 停止服务:      ./stop-all.sh"
echo ""
echo -e "${YELLOW}💡 提示: 首次启动可能需要额外时间下载依赖${NC}"
echo -e "${YELLOW}🔍 如有问题请查看 logs/ 目录下的日志文件${NC}"
