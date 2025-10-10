# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述
这是一个基于Spring Boot + Vue 3构建的现代化AI聊天系统，支持多AI提供商（OpenAI、Kimi、DeepSeek、通义千问、长城大模型等），集成搜索增强功能，采用响应式编程模型，提供流式聊天体验。使用Spring AI 1.0.0正式版和ModelScope推理API。

## 核心开发命令

### 后端命令
- `mvn clean compile` - 编译后端代码
- `mvn spring-boot:run` - 启动Spring Boot应用（端口8080）
- `mvn test` - 运行单元测试
- `mvn test -Dtest=类名` - 运行单个测试类（如：`mvn test -Dtest=MessageRequestTest`）
- `mvn test -Dtest=类名#方法名` - 运行单个测试方法
- `mvn pmd:check` - 阿里巴巴P3C代码规范检查
- `mvn spotless:check` - 代码格式检查
- `mvn spotless:apply` - 自动格式化代码
- `mvn test jacoco:report` - 生成测试覆盖率报告
- `./scripts/code-review.sh` - 执行完整代码质量检查（P3C + 编译 + 格式）
- `./scripts/code-review.sh p3c` - 仅执行P3C代码规范检查
- `./scripts/code-review.sh compile` - 仅执行编译检查
- `./scripts/code-review.sh format` - 仅执行代码格式检查

### 前端命令
- `cd frontend && npm install` - 安装前端依赖
- `cd frontend && npm run dev` - 启动前端开发服务器（端口3000）
- `cd frontend && npm run build` - 构建生产版本
- `cd frontend && npm run preview` - 预览生产构建

### 项目启动脚本
- `./start-cross-platform.sh` - 跨平台启动脚本（自动启动后端+前端）
- `./stop-cross-platform.sh` - 停止所有服务

## 核心架构设计

### 后端架构（Java 17 + Spring Boot）
- **响应式编程**: 基于Spring WebFlux + Project Reactor实现SSE流式响应
- **Spring AI集成**: 使用Spring AI 1.0.0正式版统一AI模型访问
- **多模型支持**: 工厂模式 + 策略模式支持多AI提供商动态切换
- **ModelScope集成**: 统一使用ModelScope推理API访问各大模型
- **三层架构**: Controller → Service → Mapper（MyBatis）
- **SSE流式传输**: 使用JSON包装解决Markdown格式传输问题
- **异步处理**: AI响应、搜索、标题生成全异步
- **记忆管理**: 基于MessageChatMemoryAdvisor实现对话历史持久化，支持上下文连续对话
- **企业模型支持**: 支持长城大模型等企业级AI服务

### 前端架构（Vue 3 + Element Plus）
- **组合式API**: 全部使用Composition API + `<script setup>`语法
- **状态管理**: Pinia管理全局状态
- **实时渲染**: SSE客户端 + 打字机效果Markdown渲染
- **响应式设计**: 支持桌面端和移动端

### 数据库设计
- **用户表**: `users`（用户信息）
- **对话表**: `conversations`（对话会话）
- **消息表**: `messages`（聊天消息）
- **模型配置**: `ai_providers`、`ai_models`、`user_model_preferences`

### 关键技术特性
- **流式响应**: SSE + JSON包装解决换行符传输问题
- **智能搜索**: 基于关键词自动触发Tavily搜索API
- **模型管理**: 支持用户自定义模型偏好
- **错误处理**: 统一异常处理 + 用户友好错误信息

## 重要代码位置

### 核心服务类
- `AiChatServiceImpl` (src/main/java/com/example/service/impl/) - 主要聊天逻辑
- `SearchServiceImpl` (src/main/java/com/example/service/impl/) - 搜索增强功能
- `ModelProviderFactory` (src/main/java/com/example/service/factory/) - 模型提供者工厂

### 核心工具和管理类
- `WebSearchTool` (src/main/java/com/example/tool/) - 搜索增强工具
- `ChatClientManager` (src/main/java/com/example/manager/) - 聊天客户端管理器
- `DatabaseChatMemory` (src/main/java/com/example/memory/) - 数据库聊天记忆实现

### 关键配置类
- `MultiModelProperties` (src/main/java/com/example/config/) - 多模型配置
- `ChatStreamingProperties` (src/main/java/com/example/config/) - 流式响应配置
- `AiConfig` (src/main/java/com/example/config/) - AI相关配置
- `MemoryConfig` (src/main/java/com/example/config/) - 聊天记忆配置

### 前端核心组件
- `Chat.vue` (frontend/src/views/) - 主聊天界面
- `TypewriterMarkdown.vue` (frontend/src/components/) - 打字机效果Markdown渲染
- SSE客户端逻辑分布在Chat.vue中

## 环境配置要求

### 必需环境变量
```bash
# 数据库连接
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# AI API密钥（统一使用ModelScope推理API）
QWEN_API_KEY=your_modelscope_key
OPENAI_API_KEY=your_modelscope_key
KIMI2_API_KEY=your_modelscope_key
DEEPSEEK_API_KEY=your_modelscope_key
GREATWALL_API_KEY=your_greatwall_key

# 搜索API
TAVILY_API_KEY=your_tavily_key

# 长城大模型SSL配置（开发环境）
GREATWALL_SSL_SKIP_VERIFICATION=true
```

### 开发环境要求
- Java 17+
- Maven 3.6+
- Node.js 16+
- PostgreSQL 8.0+（生产）/ H2（开发测试）

## 编码规范和最佳实践

### Java代码规范
- 遵循阿里巴巴Java开发手册（P3C规范）
- 使用Lombok减少样板代码
- Service接口采用响应式编程模型（Flux/Mono）
- 所有外部API调用需要异常处理和重试机制

### 前端代码规范
- 统一使用Composition API + `<script setup>`
- 组件命名采用PascalCase
- 使用TypeScript类型声明（虽然当前是JS）
- 异步操作统一使用async/await

### 数据库操作
- 使用MyBatis原生XML映射，避免复杂的ORM映射
- 所有SQL查询使用预编译语句防SQL注入
- 合理使用数据库索引优化查询性能

## 测试策略
- **单元测试覆盖率**: 目标85%（通过JaCoCo强制检查）
- **集成测试**: 重点测试Controller层和Service层
- **模拟测试**: 使用Mockito模拟外部API调用
- 测试数据使用H2内存数据库

## 部署和监控
- **健康检查端点**: `http://localhost:8080/actuator/health`
- **日志文件**: `backend.log`（后端）、`frontend.log`（前端）
- **生产部署**: 参考README中的部署指南
- **监控指标**: Spring Boot Actuator提供应用监控

## 故障排除
- 后端启动失败：检查API密钥配置和数据库连接
- Spring AI连接问题：确认ModelScope API密钥和base-url配置正确
- 长城大模型SSL错误：开发环境设置`GREATWALL_SSL_SKIP_VERIFICATION=true`
- 前端代理错误：检查vite.config.js中的代理配置
- SSE连接问题：检查浏览器Network面板EventStream连接状态
- 详细调试信息：设置`LOGGING_LEVEL_COM_EXAMPLE=DEBUG`

## 重要技术更新
- **Spring AI 1.0.0**: 项目已升级到正式版，API更加稳定
- **ModelScope统一**: 所有AI提供商通过ModelScope推理API统一访问
- **企业AI支持**: 新增长城大模型等企业级AI服务集成
- **SSL配置**: 支持企业环境的SSL证书配置和跳过验证选项