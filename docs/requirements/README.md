# Spring AI 多模块智能助手平台 - 需求规格说明书

> **版本**: v3.0  
> **更新时间**: 2024-01-27  
> **状态**: ✅ 生产就绪

---

## 📋 目录

- [1. 项目概述](#1-项目概述)
- [2. 系统架构](#2-系统架构)
- [3. 功能需求](#3-功能需求)
- [4. 非功能需求](#4-非功能需求)
- [5. 技术选型](#5-技术选型)
- [6. 交付标准](#6-交付标准)

---

## 1. 项目概述

### 1.1 项目定位

**Spring AI 多模块智能助手平台** 是一个基于 Spring Boot 3.4 和 Spring AI 的企业级智能助手系统，采用多模块架构，提供统一的 AI 能力基础设施。

### 1.2 核心价值

- **统一基础设施**: 通过 agent-core 提供可复用的 AI 能力
- **多模块扩展**: chat（对话）、novel（创作）、mcp（工具）独立部署
- **多模型支持**: OpenAI、DeepSeek、GreatWall、Ollama 等
- **企业级特性**: SSE 流式输出、会话管理、工具调用、RAG 检索

### 1.3 目标用户

| 用户类型 | 使用场景 | 核心需求 |
|---------|---------|---------|
| **企业用户** | 内部 AI 助手 | 多模型选择、数据隔离、可扩展 |
| **开发者** | AI 应用开发 | 标准化 API、工具集成、文档完善 |
| **内容创作者** | 长文本创作 | RAG 检索、素材管理、MCP 工具 |
| **研究人员** | 实验和评估 | 多模型对比、Prompt 优化、可观测性 |

### 1.4 项目边界

#### 包含范围
- ✅ 多模型 ChatClient 管理
- ✅ SSE 流式对话
- ✅ 会话记忆管理
- ✅ 工具动态调用（搜索、MCP）
- ✅ RAG 检索增强（novel 模块）
- ✅ 用户偏好管理
- ✅ 统一的前端门户

#### 不包含范围
- ❌ AI 模型训练
- ❌ 大规模向量数据库运维
- ❌ 多租户完整隔离
- ❌ 企业级 SSO 集成（需额外开发）

---

## 2. 系统架构

### 2.1 模块划分

```
SpringAI-MultiModule-Platform
├── common/agent-core           # 统一 AI 基础设施
│   ├── UnifiedChatClientManager    # ChatClient 管理
│   ├── AbstractDatabaseChatMemory  # 会话记忆基类
│   ├── ToolManager                 # 工具动态注入
│   └── SystemPromptProvider        # Prompt 配置化
│
├── chat/                       # 对话模块
│   ├── 实时对话                 # SSE 流式输出
│   ├── 搜索增强                 # WebSearch Tool
│   ├── 会话管理                 # 历史记录、标题生成
│   └── 用户偏好                 # 模型选择偏好
│
├── novel/                      # 创作模块
│   ├── 长文本生成               # 故事、剧本、散文
│   ├── RAG 检索                # PGVector、文档导入
│   ├── 素材管理                 # 爬取、导入、搜索
│   └── MCP 工具                # 外部工具集成
│
├── mcp/                        # MCP 模块
│   ├── mcp-server              # MCP 协议服务器
│   └── mcp-client              # MCP 客户端
│
└── apps/portal-frontend        # 统一前端门户
    ├── 登录                    # 简单用户认证
    ├── 功能导航                 # chat/novel/mcp 入口
    └── 统一风格                 # Element Plus UI
```

### 2.2 技术栈

| 层次 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **后端框架** | Spring Boot | 3.4.x | WebFlux 响应式 |
| **AI 框架** | Spring AI | 1.0.0-M5 | ChatClient、Advisor |
| **数据库** | PostgreSQL / H2 | 15+ / 2.x | 主库 / 开发库 |
| **持久层** | MyBatis | 3.0.3 | SQL 映射 |
| **向量存储** | PGVector | - | RAG 检索 |
| **前端框架** | Vue 3 | 3.x | Composition API |
| **UI 组件** | Element Plus | 2.x | 企业级 UI |
| **构建工具** | Maven | 3.9+ | 多模块构建 |

---

## 3. 功能需求

### 3.1 Chat 模块功能

#### 3.1.1 实时对话（P0 - 核心）

**需求描述**:  
用户输入消息后，系统通过 SSE 流式返回 AI 回复，实现类似 ChatGPT 的打字机效果。

**功能点**:
- ✅ SSE 流式输出
- ✅ Markdown 渲染（代码高亮、表格、数学公式）
- ✅ 消息历史记录
- ✅ 中断流式输出

**API 接口**:
```http
POST /api/chat/stream
Content-Type: application/json

{
  "conversationId": 123,
  "message": "用户消息",
  "provider": "deepseek",
  "model": "deepseek-chat",
  "searchEnabled": true
}

Response: text/event-stream
data: {"type":"content","content":"流式文本"}
data: {"type":"done"}
```

**验收标准**:
- [ ] 消息延迟 < 200ms
- [ ] 支持 10+ 并发用户
- [ ] Markdown 渲染正确率 > 99%
- [ ] 无内存泄漏

#### 3.1.2 搜索增强（P1 - 重要）

**需求描述**:  
当用户询问需要最新信息的问题时，系统自动调用 Tavily 搜索 API，将结果注入 AI 上下文。

**功能点**:
- ✅ Tavily Search API 集成
- ✅ 搜索结果结构化展示
- ✅ 用户控制搜索开关
- ✅ 最多调用次数限制（默认 3 次）

**搜索事件**:
```json
{
  "type": "search",
  "query": "搜索关键词",
  "results": [
    {"title": "...", "url": "...", "snippet": "..."}
  ]
}
```

**验收标准**:
- [ ] 搜索响应 < 3s
- [ ] 支持中英文搜索
- [ ] 结果与回答相关性 > 80%

#### 3.1.3 会话管理（P1 - 重要）

**需求描述**:  
用户可以创建、查看、删除会话，每个会话独立保存消息历史。

**功能点**:
- ✅ 创建会话
- ✅ 会话列表
- ✅ 自动生成标题
- ✅ 删除会话
- ✅ 清空历史

**API 接口**:
```http
GET    /api/conversations
POST   /api/conversations
DELETE /api/conversations/{id}
POST   /api/conversations/{id}/clear
POST   /api/conversations/{id}/regenerate-title
```

**验收标准**:
- [ ] 支持 1000+ 会话存储
- [ ] 标题生成准确率 > 90%
- [ ] 列表加载 < 500ms

#### 3.1.4 多模型支持（P0 - 核心）

**需求描述**:  
用户可以选择不同的 AI 提供商和模型，系统动态适配。

**支持的模型**:
- ✅ OpenAI (gpt-4, gpt-3.5-turbo)
- ✅ DeepSeek (deepseek-chat)
- ✅ GreatWall (greatwall-deepseek-v3)
- ✅ Ollama (本地模型)

**功能点**:
- ✅ 模型列表查询
- ✅ 用户偏好保存
- ✅ 动态切换模型
- ✅ 模型能力查询（工具调用、Thinking）

**验收标准**:
- [ ] 模型切换无感知
- [ ] 配置外部化
- [ ] 新增模型只需改配置

### 3.2 Novel 模块功能

#### 3.2.1 长文本创作（P0 - 核心）

**需求描述**:  
用户输入创作指令（大纲、续写、润色），系统生成长篇内容。

**功能点**:
- ✅ 大纲生成
- ✅ 续写内容
- ✅ 文本润色
- ✅ 风格模仿

**验收标准**:
- [ ] 单次生成最多 4000 tokens
- [ ] 内容连贯性 > 85%
- [ ] 风格一致性 > 80%

#### 3.2.2 RAG 检索增强（P1 - 重要）

**需求描述**:  
用户导入素材（PDF、TXT、网页），系统通过 PGVector 检索相关内容，增强创作质量。

**功能点**:
- ✅ 文档导入（PDF、TXT、Markdown）
- ✅ 网页爬取
- ✅ 向量化存储
- ✅ 相似度检索
- ✅ 素材管理（列表、删除）

**技术实现**:
- Embedding Model: text-embedding-ada-002
- Vector DB: PGVector
- 检索算法: Cosine Similarity
- Top-K: 5

**验收标准**:
- [ ] 检索准确率 > 75%
- [ ] 检索延迟 < 1s
- [ ] 支持 10000+ 文档

#### 3.2.3 MCP 工具集成（P2 - 可选）

**需求描述**:  
通过 Model Context Protocol 调用外部工具（如文件操作、API 调用）。

**功能点**:
- ✅ MCP Server 健康检查
- ✅ 工具列表查询
- ✅ 工具动态调用

**验收标准**:
- [ ] 工具调用成功率 > 95%
- [ ] 支持 10+ 工具注册

### 3.3 Portal 前端功能

#### 3.3.1 统一登录（P1 - 重要）

**需求描述**:  
简单的用户认证，支持用户名+昵称登录。

**功能点**:
- ✅ 登录验证
- ✅ 用户信息存储（LocalStorage）
- ✅ 会话保持

**验收标准**:
- [ ] 登录响应 < 500ms
- [ ] Token 有效期管理

#### 3.3.2 功能导航（P0 - 核心）

**需求描述**:  
用户登录后看到 chat、novel、mcp 三个功能入口。

**验收标准**:
- [ ] 导航响应 < 100ms
- [ ] UI 统一风格

---

## 4. 非功能需求

### 4.1 性能要求

| 指标 | 目标值 | 说明 |
|------|-------|------|
| **并发用户** | 100+ | 同时在线用户 |
| **响应时间** | < 200ms | SSE 首字节 |
| **吞吐量** | 1000 req/min | 消息处理能力 |
| **内存占用** | < 2GB | 单模块运行时 |

### 4.2 可靠性要求

- **可用性**: 99% uptime
- **容错性**: 模型切换失败自动降级
- **数据一致性**: 会话消息不丢失
- **错误处理**: 统一异常处理和日志记录

### 4.3 可维护性要求

- **代码质量**: 遵循 Spring Boot 最佳实践
- **文档完整性**: 需求、设计、部署文档齐全
- **测试覆盖**: 核心功能单元测试覆盖 > 60%
- **日志规范**: 统一日志格式（Logback）

### 4.4 安全性要求

- **API Key 保护**: 环境变量存储，不提交代码
- **SQL 注入防护**: MyBatis 参数化查询
- **XSS 防护**: 前端输入过滤
- **CORS 配置**: 限制跨域访问

### 4.5 可扩展性要求

- **水平扩展**: 支持多实例部署
- **模块独立**: 各模块可独立扩展
- **插件机制**: 新增模型/工具无需改核心代码

---

## 5. 技术选型

### 5.1 后端技术选型理由

#### Spring Boot 3.4 + WebFlux
- **优势**: 响应式编程，天然支持 SSE 流式输出
- **适用场景**: 实时聊天、长连接

#### Spring AI
- **优势**: 统一的 ChatClient API，支持多模型
- **适用场景**: 多模型管理、Advisor 机制

#### MyBatis
- **优势**: 灵活的 SQL 控制，适合复杂查询
- **适用场景**: 会话历史、用户偏好查询

#### PGVector
- **优势**: 原生向量存储，PostgreSQL 生态
- **适用场景**: RAG 检索、相似度搜索

### 5.2 前端技术选型理由

#### Vue 3 + Composition API
- **优势**: 响应式、组件化、TypeScript 支持
- **适用场景**: SPA 应用、复杂交互

#### Element Plus
- **优势**: 企业级 UI 组件库，开箱即用
- **适用场景**: 后台管理风格应用

---

## 6. 交付标准

### 6.1 代码交付标准

- ✅ 编译通过（BUILD SUCCESS）
- ✅ 核心功能可用
- ✅ 无明显 Bug
- ✅ 代码符合规范（统一格式化）

### 6.2 文档交付标准

- ✅ 需求文档（本文档）
- ✅ 设计文档（架构、API）
- ✅ 部署文档（环境配置、启动脚本）
- ✅ README 文档（快速开始）

### 6.3 部署交付标准

- ✅ 提供启动脚本（start-all.sh）
- ✅ 环境配置模板（application.yml）
- ✅ Docker 部署支持（可选）

### 6.4 测试交付标准

- ✅ 核心功能手动测试通过
- ✅ 关键 API 接口测试通过
- ✅ 前端页面交互测试通过

---

## 7. 项目里程碑

### Phase 1: 基础设施（已完成 ✅）
- [x] Spring AI 集成
- [x] 多模型配置
- [x] SSE 流式输出
- [x] 会话记忆

### Phase 2: 核心功能（已完成 ✅）
- [x] chat 模块
- [x] 搜索增强
- [x] 会话管理
- [x] 前端门户

### Phase 3: 高级功能（已完成 ✅）
- [x] novel 模块
- [x] RAG 检索
- [x] MCP 工具
- [x] 素材管理

### Phase 4: 架构优化（已完成 ✅）
- [x] 统一 ChatClient 管理
- [x] 消除重复代码
- [x] 工具动态注入
- [x] SystemPrompt 配置化

### Phase 5: 未来规划（可选）
- [ ] Observation 监控
- [ ] 结构化输出
- [ ] 多租户隔离
- [ ] SSO 集成

---

## 8. 附录

### 8.1 术语表

| 术语 | 解释 |
|------|------|
| **SSE** | Server-Sent Events，服务器推送事件 |
| **RAG** | Retrieval-Augmented Generation，检索增强生成 |
| **MCP** | Model Context Protocol，模型上下文协议 |
| **Advisor** | Spring AI 的拦截器机制 |
| **PGVector** | PostgreSQL 向量扩展 |
| **Embedding** | 文本向量化 |

### 8.2 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring Boot 3.4 文档](https://docs.spring.io/spring-boot/docs/3.4.x/reference/)
- [Element Plus 文档](https://element-plus.org/)
- [PGVector 文档](https://github.com/pgvector/pgvector)

---

**文档维护者**: 架构团队  
**最后更新**: 2024-01-27  
**状态**: ✅ 当前版本
