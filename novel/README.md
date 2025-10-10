# Novel 模块 - AI长文本创作助手

## 概述
Novel模块是一个基于Ollama大模型的本地长文本创作助手，集成RAG检索和MCP工具调用功能，专为个人创作者设计。

## 架构特点
- ✅ **独立Spring Boot应用** - 与chat、mcp模块平级，端口8083
- ✅ **前端集成Portal** - 通过Vite代理访问，统一认证和界面
- ✅ **响应式架构** - WebFlux + SSE实现流式文本生成
- ✅ **轻量级设计** - 内存存储，无数据库依赖
- ✅ **本地优先** - 基于Ollama，数据不离开本地环境

## 功能特性

### 🎯 核心功能
- **Ollama推理服务** - 支持多模型选择和参数调节
- **流式文本生成** - 实时展示AI创作过程
- **轻量RAG系统** - 本地文档导入、分块、检索
- **MCP工具集成** - 支持Bash、文件系统等工具调用

### 🎨 用户界面
- **三栏布局** - 素材管理 | 创作对话 | 参数工具
- **实时响应** - SSE流式渲染，打字机效果
- **素材引用** - 可视化展示检索结果和相似度
- **工具调用** - 直观的MCP工具执行界面

## 快速开始

### 1. 环境准备
```bash
# 安装Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# 启动Ollama服务
ollama serve

# 下载模型（示例）
ollama pull llama3
ollama pull qwen2.5:7b
```

### 2. 启动Novel后端
```bash
cd novel/backend
mvn spring-boot:run
```
服务将在 http://localhost:8083 启动

### 3. 启动Portal前端
```bash
cd apps/portal-frontend
npm install
npm run dev
```
前端将在 http://localhost:5174 启动

### 4. 访问应用
1. 打开浏览器访问 http://localhost:5174
2. 登录Portal（如需要）
3. 点击"Novel 创作"卡片
4. 开始你的创作之旅！

## API接口

### 模型管理
```http
GET /api/novel/models
# 返回Ollama可用模型列表
```

### 文本生成
```http
POST /api/novel/stream
Content-Type: application/json

{
  "model": "llama3",
  "prompt": "写一个科幻故事的开头",
  "temperature": 0.7,
  "maxTokens": 2000
}
# 返回SSE流式响应
```

### RAG功能
```http
POST /api/novel/rag/import
{
  "path": "/path/to/materials",
  "recursive": true,
  "filePattern": "*.txt,*.md,*.docx"
}

POST /api/novel/rag/search
{
  "query": "科幻设定",
  "topK": 5,
  "minSimilarity": 0.3
}
```

### MCP工具
```http
GET /api/novel/mcp/tools
# 获取可用工具列表

POST /api/novel/mcp/execute
{
  "toolName": "bash",
  "parameters": {
    "command": "ls -la"
  }
}
```

## 配置说明

### application.yml
```yaml
server:
  port: 8083

novel:
  ollama:
    base-url: http://localhost:11434
  rag:
    chunk-size: 500
    chunk-overlap: 50
  mcp:
    config-path: ~/.codex/config.toml
```

### Vite代理配置
```javascript
// apps/portal-frontend/vite.config.js
proxy: {
  '/api/novel': {
    target: 'http://localhost:8083',
    changeOrigin: true
  }
}
```

## 使用指南

### 创作流程
1. **素材准备** - 在左侧面板导入相关素材文档
2. **参数配置** - 在右侧选择模型和调整生成参数
3. **开始创作** - 在中间面板输入创作提示
4. **引用素材** - 使用RAG搜索功能查找相关内容
5. **工具辅助** - 调用MCP工具进行文件操作或命令执行

### 高级功能
- **多轮对话** - 支持上下文连续的创作对话
- **素材检索** - 基于关键词的语义搜索
- **参数调节** - 温度、Top-P、最大Token等精细控制
- **结果复制** - 一键复制生成内容和引用素材

## 开发指南

### 后端开发
```bash
cd novel/backend

# 编译
mvn clean compile

# 测试
mvn test

# 打包
mvn clean package

# 运行
java -jar target/novel-backend-0.0.1-SNAPSHOT.jar
```

### 前端开发
Novel前端组件位于 `apps/portal-frontend/src/features/novel/`：
- `views/` - 页面组件
- `components/` - 功能组件
- `stores/` - 状态管理

## 故障排除

### 常见问题
1. **Ollama连接失败**
   - 检查Ollama服务：`curl http://localhost:11434/api/tags`
   - 确认模型已下载：`ollama list`

2. **后端启动失败**
   - 检查端口8083是否被占用
   - 确认Java 17+环境

3. **前端无法访问**
   - 检查Vite代理配置
   - 确认后端服务运行正常

4. **素材导入失败**
   - 检查文件路径权限
   - 确认文件格式支持（.txt, .md, .docx）

### 调试模式
```bash
# 后端调试日志
export LOGGING_LEVEL_COM_EXAMPLE_NOVEL=DEBUG

# 前端开发工具
npm run dev -- --debug
```

## 扩展计划

### 短期目标
- [ ] 数据持久化（SQLite/H2）
- [ ] 真实MCP客户端集成
- [ ] 向量化RAG（Chroma/Pinecone）
- [ ] 导出功能（PDF/Word）

### 长期目标
- [ ] 多用户支持
- [ ] 云端同步
- [ ] 高级RAG算法
- [ ] 插件系统

## 技术栈
- **后端**: Spring Boot 3.3.6 + WebFlux + Java 17
- **前端**: Vue 3 + Pinia + Element Plus + Vite
- **AI**: Ollama + 本地大模型
- **工具**: MCP协议 + Spring AI

## 许可证
与主项目保持一致

---
**Happy Writing! 🚀**