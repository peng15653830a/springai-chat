# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述
Novel 模块是一个面向个人使用的本地长文本创作助手，基于 Ollama 大模型、轻量 RAG 检索与 MCP 工具调用，提供专注于小说/长文本创作的集中工作台。

## 核心开发命令

### 后端命令
- `cd novel/backend && mvn clean compile` - 编译Novel后端代码
- `cd novel/backend && mvn spring-boot:run` - 启动Novel后端服务（端口8083）
- `cd novel/backend && mvn test` - 运行单元测试
- `cd novel/backend && mvn clean package` - 打包应用

### 前端命令
- `cd apps/portal-frontend && npm run dev` - 启动Portal前端（包含Novel功能）

## 核心架构设计

### 后端架构（Java 17 + Spring Boot）
- **独立Spring Boot应用**: Novel模块作为独立服务，与chat、mcp平级
- **响应式编程**: 基于Spring WebFlux实现SSE流式响应
- **Ollama集成**: 直接调用本地Ollama API进行文本生成
- **轻量RAG**: 内存存储的文档分块与检索系统
- **MCP工具代理**: 模拟MCP工具调用（Bash、文件系统等）
- **三层架构**: Controller → Service → Domain

### 前端架构（集成到Portal）
- **路由集成**: `/novel` 路由接入Portal统一认证
- **组件化**: 三栏布局（素材、创作、工具）
- **状态管理**: Pinia管理Novel专用状态
- **流式渲染**: SSE EventSource实现实时文本流

### 数据流程
- 用户输入 → Ollama推理 → 流式返回 → 前端渲染
- 素材导入 → 文件扫描 → 分块处理 → 内存存储
- RAG检索 → 关键词匹配 → 相似度计算 → 结果返回
- MCP调用 → 工具选择 → 参数传递 → 结果展示

## 重要代码位置

### 核心服务类
- `NovelServiceImpl` (service/impl/) - Ollama推理服务
- `RagServiceImpl` (service/rag/) - RAG检索服务
- `McpServiceImpl` (service/mcp/) - MCP工具代理

### 核心控制器
- `NovelController` (controller/) - Novel API统一入口

### 前端核心组件
- `Novel.vue` (apps/portal-frontend/src/features/novel/views/) - 主页面
- `novel.js` (apps/portal-frontend/src/features/novel/stores/) - 状态管理
- 各功能面板组件 (apps/portal-frontend/src/features/novel/components/)

## 环境配置要求

### 必需环境
- **Java 17+**: Spring Boot 3.x要求
- **Maven 3.6+**: 项目构建工具
- **Node.js 16+**: 前端开发环境
- **Ollama服务**: 在localhost:11434运行，已下载目标模型

### 配置文件
```yaml
# application.yml
server:
  port: 8083  # Novel后端端口

novel:
  ollama:
    base-url: http://localhost:11434
  rag:
    chunk-size: 500
    chunk-overlap: 50
```

## API接口说明

### 模型管理
- `GET /api/novel/models` - 获取Ollama可用模型

### 文本生成
- `POST /api/novel/stream` - SSE流式文本生成

### RAG功能
- `POST /api/novel/rag/import` - 导入素材文档
- `POST /api/novel/rag/search` - 检索相关素材

### MCP工具
- `GET /api/novel/mcp/tools` - 获取可用工具列表
- `POST /api/novel/mcp/execute` - 执行指定工具

## 开发注意事项

1. **模块独立性**: Novel作为独立Spring Boot应用，端口8083
2. **与Portal集成**: 前端功能集成在Portal中，通过代理访问Novel后端
3. **数据持久化**: 当前使用内存存储，重启后数据丢失（轻量设计）
4. **Ollama依赖**: 需要确保Ollama服务运行且已下载模型
5. **CORS配置**: 后端已配置跨域支持Portal访问

## 测试策略
- **单元测试**: Service层业务逻辑测试
- **集成测试**: Controller层API测试
- **功能测试**: 完整创作流程验证

## 部署说明
- **开发环境**: 直接运行Spring Boot应用
- **生产环境**: 可打包为jar独立部署
- **健康检查**: 通过Actuator端点监控服务状态

## 故障排除
- **Ollama连接失败**: 检查localhost:11434服务状态
- **模型加载失败**: 确认Ollama已下载目标模型
- **端口冲突**: 修改application.yml中的server.port
- **CORS问题**: 检查NovelController的CrossOrigin配置

## 扩展指南
- **真实MCP集成**: 替换模拟实现为真实MCP客户端
- **向量化RAG**: 集成向量数据库（如Chroma、Pinecone）
- **数据持久化**: 添加数据库支持（如H2、PostgreSQL）
- **多模型支持**: 扩展支持更多AI提供商

## 版本信息
- Novel Backend: 0.0.1-SNAPSHOT
- Spring Boot: 3.3.6
- Java: 17
- 端口: 8083