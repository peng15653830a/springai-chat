#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 重启 SpringAI Chat 完整项目${NC}"
echo "=================================================="

# 先停止所有服务
echo -e "${YELLOW}第一步: 停止现有服务${NC}"
./stop-all.sh

echo ""
echo -e "${YELLOW}等待 5 秒后重新启动...${NC}"
sleep 5

# 重新启动所有服务
echo -e "${YELLOW}第二步: 启动所有服务${NC}"
./start-all.sh