# 项目管理脚本使用指南

## 📋 脚本说明

### 🚀 `start-all.sh` - 启动所有服务
一键启动完整的SpringAI Chat项目，包括：
- Chat后端 (端口8080)
- Novel后端 (端口8082)
- MCP Client (端口8081)
- MCP Server (端口8082)
- Portal前端 (端口5174)

**特性：**
- 自动检查Ollama服务状态
- 等待服务健康检查完成
- 自动创建PID文件用于管理
- 日志输出到logs目录
- 彩色状态提示

### 🛑 `stop-all.sh` - 停止所有服务
优雅停止所有运行的服务：
- 从PID文件获取进程ID
- 先发送TERM信号优雅停止
- 超时后强制KILL
- 清理残留进程和PID文件
- 端口验证确保完全停止

### 📊 `status.sh` - 查看服务状态
实时监控所有服务状态：
- 检查端口占用情况
- 健康状态验证
- Ollama服务和模型检查
- PID文件状态
- 日志文件信息
- 快速访问链接

### 🔄 `restart-all.sh` - 重启所有服务
安全重启项目：
- 先停止所有服务
- 等待5秒确保清理完成
- 重新启动所有服务

## 🎯 使用方法

### 日常开发
```bash
# 启动开发环境
./start-all.sh

# 查看状态
./status.sh

# 停止服务
./stop-all.sh
```

### 故障排除
```bash
# 查看服务状态
./status.sh

# 查看日志
tail -f logs/chat-backend.log
tail -f logs/novel-backend.log
tail -f logs/portal-frontend.log

# 强制重启
./restart-all.sh
```

### 生产部署
```bash
# 启动服务
./start-all.sh

# 监控状态
watch -n 5 ./status.sh

# 优雅停止
./stop-all.sh
```

## 📁 文件结构

```
项目根目录/
├── start-all.sh          # 启动脚本
├── stop-all.sh           # 停止脚本
├── status.sh             # 状态检查脚本
├── restart-all.sh        # 重启脚本
├── logs/                 # 日志目录
│   ├── chat-backend.log
│   ├── novel-backend.log
│   ├── mcp-server.log
│   ├── mcp-client.log
│   └── portal-frontend.log
└── .pids/                # PID文件目录（运行时创建）
    ├── chat-backend.pid
    ├── novel-backend.pid
    ├── mcp-server.pid
    ├── mcp-client.pid
    └── portal-frontend.pid
```

## 🔧 服务端口分配

| 服务 | 端口 | 描述 |
|------|------|------|
| Portal前端 | 5174 | Vue3前端应用 |
| Chat后端 | 8080 | 聊天服务API |
| MCP Client | 8081 | MCP客户端服务 |
| MCP Server | 8082 | MCP服务端 |
| Novel后端 | 8083 | Novel创作服务 |
| Ollama | 11434 | 本地AI模型服务 |

## 📋 前置条件

### 必需环境
- **Java 17+**: Spring Boot 3.x要求
- **Maven 3.6+**: 后端构建
- **Node.js 16+**: 前端开发
- **Ollama**: 本地AI模型服务

### Ollama设置
```bash
# 安装Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# 启动服务
ollama serve

# 下载模型
ollama pull llama3
ollama pull qwen2.5:7b
```

## ⚠️ 注意事项

### 启动顺序
脚本按以下顺序启动服务：
1. 检查Ollama服务
2. 启动后端服务（Chat、Novel、MCP）
3. 等待后端健康检查
4. 启动前端服务

### 停止顺序
脚本按以下顺序停止服务：
1. 停止前端服务
2. 停止后端服务
3. 清理残留进程
4. 验证端口释放

### 端口冲突
如果遇到端口冲突：
1. 运行`./status.sh`检查占用情况
2. 手动停止冲突进程
3. 或修改配置文件中的端口设置

### 权限问题
确保脚本有执行权限：
```bash
chmod +x *.sh
```

## 🐛 故障排除

### 常见问题

1. **Ollama连接失败**
   - 检查：`curl http://localhost:11434/api/tags`
   - 解决：`ollama serve`

2. **端口被占用**
   - 检查：`netstat -tuln | grep [端口]`
   - 解决：`kill [PID]` 或 `./stop-all.sh`

3. **Maven编译失败**
   - 检查：Java版本是否为17+
   - 解决：更新JAVA_HOME环境变量

4. **前端启动失败**
   - 检查：Node.js版本是否16+
   - 解决：`npm install` 重新安装依赖

5. **服务健康检查失败**
   - 查看日志：`tail -f logs/[service].log`
   - 检查配置文件是否正确

### 日志分析
每个服务的日志都保存在`logs/`目录：
```bash
# 实时查看所有日志
tail -f logs/*.log

# 查看特定服务日志
tail -f logs/chat-backend.log

# 搜索错误
grep -i error logs/*.log
```

### 性能监控
```bash
# 持续监控状态
watch -n 5 ./status.sh

# 检查资源使用
top -p $(cat .pids/*.pid | tr '\n' ',' | sed 's/,$//')
```

## 🚀 生产环境建议

1. **使用systemd管理服务**（推荐）
2. **配置日志轮转**防止日志文件过大
3. **设置监控告警**及时发现服务异常
4. **定期备份配置文件**
5. **使用nginx反向代理**统一入口

---

💡 **提示**: 这些脚本设计为开发和轻量级生产环境使用。对于大规模生产部署，建议使用容器化（Docker）或服务编排（Kubernetes）方案。