#!/bin/bash

# 跨平台启动脚本 - 兼容Mac M1和Ubuntu 22
# Author: xupeng
# Version: 2.0

set -e  # 遇到错误立即退出

echo "🚀 启动AI聊天应用..."

# 检测操作系统
OS_TYPE="unknown"
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS_TYPE="macos"
    echo "检测到操作系统: macOS"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS_TYPE="linux"
    echo "检测到操作系统: Linux"
else
    echo "⚠️ 未识别的操作系统: $OSTYPE"
fi

# 检查必要的命令
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "❌ 未找到命令: $1"
        echo "请安装 $1 后重试"
        exit 1
    fi
}

echo "🔍 检查依赖..."
check_command java
check_command mvn
check_command node
check_command npm

# 检查Java版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java版本: $java_version"

# 检查Node.js版本
node_version=$(node --version)
echo "Node.js版本: $node_version"

# 检查端口占用
check_port() {
    local port=$1
    local service=$2
    
    if [[ "$OS_TYPE" == "macos" ]]; then
        if lsof -Pi :$port -sTCP:LISTEN -t &> /dev/null; then
            echo "❌ 端口 $port 被占用，请停止使用该端口的$service服务"
            exit 1
        fi
    else
        if ss -ltn | grep -q ":$port "; then
            echo "❌ 端口 $port 被占用，请停止使用该端口的$service服务"
            exit 1
        fi
    fi
}

echo "🔍 检查端口占用..."
check_port 8080 "后端"
check_port 5174 "前端"

# 启动后端服务
echo "📦 启动后端服务..."
if [ ! -f "backend/target/classes/com/example/springai/SpringaiApplication.class" ]; then
    echo "编译后端代码..."
    cd backend
    mvn clean compile -q
    cd ..
fi

# 在后台启动Spring Boot
echo "启动Spring Boot应用..."
# 确保环境变量被加载
source ~/.bashrc 2>/dev/null || true
cd backend
# 本地开发跳过PMD/Spotless检查与测试，加快并避免因代码规范失败而中断
if [[ "$EMBEDDED_DB" == "true" ]]; then
  echo "🧪 使用内置H2数据库(开发模式)启动后端"
  mvn -Dpmd.skip=true -Dspotless.skip=true -DskipTests \
    spring-boot:run \
    -Dspring-boot.run.arguments="\
      --spring.datasource.url=jdbc:h2:mem:ai_chat;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false \
      --spring.datasource.driver-class-name=org.h2.Driver \
      --spring.datasource.username=sa \
      --spring.datasource.password= \
      --spring.sql.init.mode=always \
      --spring.sql.init.schema-locations=classpath:database/init-h2.sql \
    " \
    > ../backend.log 2>&1 &
else
  echo "🗄️ 使用外部数据库(按 application.yml 或环境变量)启动后端"
  mvn -Dpmd.skip=true -Dspotless.skip=true -DskipTests spring-boot:run > ../backend.log 2>&1 &
fi
BACKEND_PID=$!
echo "后端服务PID: $BACKEND_PID"
cd ..

# 2025年最佳实践健康检查函数 - 使用curl重试机制
wait_for_backend() {
    local max_attempts=60
    local retry_delay=1
    local attempt=0
    
    echo "等待后端服务启动..."
    
    # 先等待5秒，给Spring Boot充分的启动时间
    sleep 5
    
    # 使用业界标准的curl重试机制，而不是自制循环
    echo "检查健康状态..."
    if curl --silent --fail --retry 10 --retry-delay 3 --retry-connrefused --connect-timeout 5 --max-time 10 http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo ""
        echo "✅ 后端服务启动成功 (http://localhost:8080)"
        return 0
    fi
    
    # 如果curl重试失败，进行最终检查
    echo ""
    echo "🔍 进行最终检查..."
    
    local final_attempts=10
    local final_attempt=0
    
    while [ $final_attempt -lt $final_attempts ]; do
        # 检查进程是否还在运行
        if ! kill -0 $BACKEND_PID 2>/dev/null; then
            echo ""
            echo "❌ 后端进程意外退出，请检查日志:"
            echo "   tail -f backend.log"
            exit 1
        fi
        
        # 简单的端口检查
        if command -v nc >/dev/null 2>&1; then
            if nc -z localhost 8080 2>/dev/null; then
                echo ""
                echo "✅ 后端端口8080已打开，服务可能正在初始化"
                # 再等待5秒让服务完全启动
                sleep 5
                return 0
            fi
        else
            # 降级使用ss检查端口
            if ss -ln | grep -q ":8080 "; then
                echo ""
                echo "✅ 后端端口8080已打开，服务可能正在初始化"
                sleep 5
                return 0
            fi
        fi
        
        printf "."
        sleep 2
        ((final_attempt++))
    done
    
    echo ""
    echo "❌ 后端服务启动超时"
    echo "   进程状态: $(kill -0 $BACKEND_PID 2>/dev/null && echo '运行中' || echo '已退出')"
    echo "   检查日志: tail -f backend.log"
    echo "   手动测试: curl http://localhost:8080/actuator/health"
    kill $BACKEND_PID 2>/dev/null || true
    exit 1
}

# 调用健康检查函数
wait_for_backend

# 启动前端服务
echo "🌐 启动前端服务..."
cd ../apps/portal-frontend

if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

# 检查package.json中的dev脚本
if ! npm run | grep -q "dev"; then
    echo "❌ package.json中未找到dev脚本"
    cd ..
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# 启动前端开发服务器
echo "启动前端开发服务器..."
npm run dev > ../../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端服务PID: $FRONTEND_PID"

cd ..

# 2025年最佳实践前端健康检查函数
wait_for_frontend() {
    echo "等待前端服务启动..."
    
    # Vite启动较快，但仍需等待3秒让进程稳定
    sleep 3
    
    # 使用业界标准的curl重试机制检查前端
    echo "检查前端服务状态..."
    if curl --silent --fail --retry 8 --retry-delay 2 --retry-connrefused --connect-timeout 3 --max-time 8 http://localhost:5174 >/dev/null 2>&1; then
        echo ""
        echo "✅ 前端服务启动成功 (http://localhost:3000)"
        return 0
    fi
    
    # 降级检查：至少确保端口打开和进程运行
    echo ""
    echo "🔍 进行降级检查..."
    
    local final_attempts=8
    local final_attempt=0
    
    while [ $final_attempt -lt $final_attempts ]; do
        # 检查前端进程是否还在运行
        if ! kill -0 $FRONTEND_PID 2>/dev/null; then
            echo ""
            echo "❌ 前端进程意外退出，请检查日志:"
            echo "   tail -f frontend.log"
            return 1
        fi
        
        # 检查端口是否开放
        if command -v nc >/dev/null 2>&1; then
            if nc -z localhost 5174 2>/dev/null; then
                echo ""
                echo "✅ 前端端口5174已打开，Vite服务正在运行"
                echo "   访问地址: http://localhost:5174"
                return 0
            fi
        else
            # 降级使用ss检查端口
            if ss -ln | grep -q ":5174 "; then
                echo ""
                echo "✅ 前端端口5174已打开，Vite服务正在运行" 
                echo "   访问地址: http://localhost:5174"
                return 0
            fi
        fi
        
        printf "."
        sleep 2
        ((final_attempt++))
    done
    
    echo ""
    echo "⚠️ 前端服务启动检查超时"
    echo "   进程状态: $(kill -0 $FRONTEND_PID 2>/dev/null && echo '运行中' || echo '已退出')"
    echo "   可能原因: Vite需要更多时间编译或网络较慢"
    echo "   手动检查: http://localhost:3000"
    echo "   检查日志: tail -f frontend.log"
    echo ""
    echo "🚀 继续启动流程，前端可能仍在初始化中..."
    return 0  # 不强制退出，前端启动较慢是正常的
}

# 调用前端健康检查
wait_for_frontend

echo ""
echo "🎉 应用启动完成！"
echo ""
echo "📍 访问地址:"
echo "   前端: http://localhost:3000"
echo "   后端: http://localhost:8080"
echo "   健康检查: http://localhost:8080/actuator/health"
echo ""
echo "📋 进程信息:"
echo "   后端PID: $BACKEND_PID"
echo "   前端PID: $FRONTEND_PID"
echo "   操作系统: $OS_TYPE"
echo ""
echo "🛑 停止服务:"
if [[ "$OS_TYPE" == "macos" ]]; then
    echo "   kill $BACKEND_PID $FRONTEND_PID"
else
    echo "   kill $BACKEND_PID $FRONTEND_PID"
fi
echo "   或运行: ./stop-cross-platform.sh"
echo ""
echo "📝 查看日志:"
echo "   后端: tail -f backend.log"
echo "   前端: tail -f frontend.log"
echo ""

# 保存PID到文件
echo "$BACKEND_PID" > .backend.pid
echo "$FRONTEND_PID" > .frontend.pid
echo "$OS_TYPE" > .os_type

# 创建一个优雅的停止函数
cleanup() {
    echo ""
    echo "🛑 正在停止服务..."
    
    if kill -0 $BACKEND_PID 2>/dev/null; then
        echo "停止后端服务..."
        kill $BACKEND_PID 2>/dev/null
    fi
    
    if kill -0 $FRONTEND_PID 2>/dev/null; then
        echo "停止前端服务..."
        kill $FRONTEND_PID 2>/dev/null
    fi
    
    # 清理PID文件
    rm -f .backend.pid .frontend.pid .os_type
    
    echo "✅ 服务已停止"
    exit 0
}

# 设置信号处理
trap cleanup INT TERM

echo "按 Ctrl+C 停止服务..."
echo ""
echo "🎯 启动完成！服务正在后台运行..."
echo "   要停止服务，运行: ./stop-cross-platform.sh"
echo ""

# 不要使用wait命令，让脚本正常退出
# 服务已经在后台运行，PID已保存到文件
