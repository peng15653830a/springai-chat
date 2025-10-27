# Spring AI 多模块智能助手平台 - 系统设计文档

> **版本**: v3.0  
> **更新时间**: 2024-01-27  
> **状态**: ✅ 生产就绪

---

## 📋 目录

- [1. 系统架构设计](#1-系统架构设计)
- [2. 核心组件设计](#2-核心组件设计)
- [3. 数据库设计](#3-数据库设计)
- [4. API 设计](#4-api-设计)
- [5. 前端设计](#5-前端设计)
- [6. 技术决策](#6-技术决策)

---

## 1. 系统架构设计

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     Portal Frontend (Vue 3)                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Login   │  │   Chat   │  │  Novel   │  │   MCP    │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└───────────────────┬─────────────────┬───────────────────────┘
                    │                 │
         ┌──────────▼─────────┐ ┌────▼──────────┐
         │   Chat Module      │ │ Novel Module  │
         │   (Spring Boot)    │ │ (Spring Boot) │
         └──────────┬─────────┘ └────┬──────────┘
                    │                 │
         ┌──────────▼─────────────────▼──────────┐
         │        Agent-Core (Shared)             │
         │  ┌───────────────────────────────┐    │
         │  │ UnifiedChatClientManager      │    │
         │  │ AbstractDatabaseChatMemory    │    │
         │  │ ToolManager                   │    │
         │  │ SystemPromptProvider          │    │
         │  └───────────────────────────────┘    │
         └──────────┬─────────────────────────────┘
                    │
         ┌──────────▼─────────────────────────────┐
         │        Spring AI Framework             │
         │  ┌─────────┐  ┌─────────┐  ┌────────┐ │
         │  │ChatModel│  │ Advisor │  │  Tool  │ │
         │  └─────────┘  └─────────┘  └────────┘ │
         └────────────────────────────────────────┘
                    │
         ┌──────────▼─────────────────────────────┐
         │  AI Providers                          │
         │  OpenAI | DeepSeek | GreatWall | ...  │
         └────────────────────────────────────────┘
```

### 1.2 模块职责

#### Agent-Core (共享基础设施)
**职责**：
- 统一 ChatClient 创建和管理
- 提供 ChatMemory 抽象基类
- 工具动态注入机制
- SystemPrompt 配置化
- ChatOptions 统一管理
- SSE 流式输出基础设施

**核心类**：
```java
// ChatClient 管理
UnifiedChatClientManager implements ChatClientResolver, ClientManager

// ChatMemory 基类
AbstractDatabaseChatMemory implements ChatMemory

// 工具管理
ToolManager / DefaultToolManager

// Prompt 提供者
SystemPromptProvider / ConfigurableSystemPromptProvider

// Options 工厂
AbstractChatOptionsFactory
```

#### Chat Module (对话模块)
**职责**：
- 实时对话功能
- 搜索增强
- 会话管理
- 用户偏好
- 模型目录服务

**主要包结构**：
```
chat/
├── controller/       # REST API 控制器
│   ├── ChatController
│   ├── ConversationController
│   └── ModelController
├── service/          # 业务逻辑
│   ├── ChatService
│   ├── ConversationService
│   └── UserModelPreferenceService
├── manager/          # 管理器
│   └── ChatClientManager (ModelCatalogService)
├── streaming/        # 流式处理
│   └── ChatModuleOptionsFactory
├── tool/             # 工具
│   └── WebSearchTool
└── memory/           # 会话记忆
    └── DatabaseChatMemory
```

#### Novel Module (创作模块)
**职责**：
- 长文本创作
- RAG 检索增强
- 素材管理
- MCP 工具调用

**主要包结构**：
```
novel/
├── controller/       # REST API 控制器
│   ├── NovelGenerationController
│   ├── MaterialController
│   └── McpController
├── service/          # 业务逻辑
│   ├── NovelGenerationService
│   ├── MaterialService
│   └── RagService
├── streaming/        # 流式处理
│   └── NovelOptionsFactory
├── tool/             # 工具
│   └── NovelMcpTool
└── memory/           # 会话记忆
    └── NovelDatabaseChatMemory
```

---

## 2. 核心组件设计

### 2.1 UnifiedChatClientManager

**设计目标**：
- 所有模块共享的 ChatClient 管理器
- 懒加载创建并缓存 ChatClient
- 自动注入 Advisor（Memory、Logger）
- 从配置中获取 SystemPrompt

**类图**：
```
┌─────────────────────────────────────┐
│   UnifiedChatClientManager          │
├─────────────────────────────────────┤
│ - modelProviderFactory              │
│ - systemPromptProvider              │
│ - messageChatMemoryAdvisor          │
│ - simpleLoggerAdvisor               │
│ - cache: Map<String, ChatClient>   │
├─────────────────────────────────────┤
│ + getChatClient(provider): ChatClient│
│ + isAvailable(provider): boolean    │
│ + getAvailableProviders(): List     │
│ - createChatClient(provider)        │
└─────────────────────────────────────┘
```

**实现逻辑**：
```java
@Component
public class UnifiedChatClientManager implements ChatClientResolver, ClientManager {
    
    @Override
    public ChatClient getChatClient(String provider) {
        return cache.computeIfAbsent(provider, this::createChatClient);
    }
    
    private ChatClient createChatClient(String provider) {
        // 1. 获取 ChatModel
        ChatModel chatModel = modelProviderFactory.getChatModel(provider);
        
        // 2. 获取 SystemPrompt
        String systemPrompt = systemPromptProvider.getSystemPrompt(provider);
        
        // 3. 构建 ChatClient
        ChatClient.Builder builder = ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt);
        
        // 4. 注入 Advisor
        if (simpleLoggerAdvisor != null) {
            builder.defaultAdvisors(simpleLoggerAdvisor, messageChatMemoryAdvisor);
        } else {
            builder.defaultAdvisors(messageChatMemoryAdvisor);
        }
        
        return builder.build();
    }
}
```

### 2.2 AbstractDatabaseChatMemory

**设计目标**：
- 提供 ChatMemory 接口的通用实现
- 子类只需实现存储相关的 3 个方法
- 统一处理 conversationId 解析、Message 转换等

**类图**：
```
┌──────────────────────────────────────┐
│   AbstractDatabaseChatMemory         │
│   (abstract)                         │
├──────────────────────────────────────┤
│ + add(conversationId, messages)      │
│ + get(conversationId): List<Message> │
│ + clear(conversationId)              │
├──────────────────────────────────────┤
│ # saveMessage(cid, role, content)*   │
│ # loadMessages(cid): List*           │
│ # deleteMessages(cid)*               │
│ # afterClear(cid)                    │
│ - parseConversationId()              │
│ - mapRoleFromMessage()               │
│ - toSpringAiMessage()                │
└──────────────────────────────────────┘
          ▲                 ▲
          │                 │
┌─────────┴──────┐  ┌───────┴─────────────┐
│DatabaseChat    │  │NovelDatabaseChat    │
│Memory          │  │Memory               │
└────────────────┘  └─────────────────────┘
```

**子类实现示例**：
```java
@Component
public class DatabaseChatMemory extends AbstractDatabaseChatMemory {
    
    private final MessageMapper messageMapper;
    
    @Override
    protected void saveMessage(Long cid, String role, String content) {
        // 只保存 system 消息，user/assistant 由调用方保存
        if ("system".equals(role)) {
            Message entity = new Message();
            entity.setConversationId(cid);
            entity.setRole(role);
            entity.setContent(content);
            messageMapper.insert(entity);
        }
    }
    
    @Override
    protected List<MessageEntity> loadMessages(Long cid) {
        return messageMapper.selectByConversationId(cid);
    }
    
    @Override
    protected void deleteMessages(Long cid) {
        messageMapper.deleteByConversationId(cid);
    }
}
```

### 2.3 ToolManager

**设计目标**：
- 自动发现所有 @Tool 注解的 bean
- 根据请求上下文动态注入工具
- 避免不必要的工具注册（节省 prompt tokens）

**类图**：
```
┌────────────────────────────────┐
│   ToolManager (interface)      │
├────────────────────────────────┤
│ + resolveTools(request): List  │
└────────────────────────────────┘
                ▲
                │
┌───────────────┴──────────────────┐
│   DefaultToolManager             │
├──────────────────────────────────┤
│ - availableTools: Map<String, > │
├──────────────────────────────────┤
│ + resolveTools(request): List    │
│ - hasToolAnnotation(bean)        │
└──────────────────────────────────┘
```

**实现逻辑**：
```java
@Component
public class DefaultToolManager implements ToolManager {
    
    private final Map<String, Object> availableTools = new HashMap<>();
    
    @Autowired
    public DefaultToolManager(List<Object> allBeans) {
        // 自动发现所有带 @Tool 注解的 bean
        for (Object bean : allBeans) {
            if (hasToolAnnotation(bean)) {
                availableTools.put(bean.getClass().getSimpleName(), bean);
            }
        }
    }
    
    @Override
    public List<Object> resolveTools(TextStreamRequest request) {
        List<Object> tools = new ArrayList<>();
        
        // 根据请求标志动态注入
        if (request.isSearchEnabled()) {
            Object webSearchTool = availableTools.get("WebSearchTool");
            if (webSearchTool != null) {
                tools.add(webSearchTool);
            }
        }
        
        return tools;
    }
}
```

### 2.4 SystemPromptProvider

**设计目标**：
- 可插拔的 SystemPrompt 提供机制
- 不同模块可定制不同的 prompt
- 支持针对不同 provider 的差异化配置

**类图**：
```
┌──────────────────────────────────────┐
│   SystemPromptProvider (interface)   │
├──────────────────────────────────────┤
│ + getSystemPrompt(provider): String  │
│ + getDefaultSystemPrompt(): String   │
└──────────────────────────────────────┘
          ▲                 ▲
          │                 │
┌─────────┴──────┐  ┌───────┴─────────────┐
│ChatSystem      │  │NovelSystem          │
│PromptProvider  │  │PromptProvider       │
└────────────────┘  └─────────────────────┘
```

**chat 模块实现**：
```java
@Component
public class ChatSystemPromptProvider implements SystemPromptProvider {
    
    @Value("${chat.system-prompt.max-tool-calls:3}")
    private int maxToolCalls;
    
    @Override
    public String getSystemPrompt(String provider) {
        if ("deepseek".equalsIgnoreCase(provider)) {
            return buildDeepSeekPrompt();  // 针对 DeepSeek 优化
        }
        return buildDefaultChatPrompt();
    }
    
    private String buildDefaultChatPrompt() {
        return """
            你是一个智能AI助手。请以清晰、可读的 Markdown 作答。
            
            能力：
            - 需要最新信息时调用搜索工具（最多调用 %d 次）
            - 准确理解问题并给出有用答案
            
            风格：准确、有用、友好
            """.formatted(maxToolCalls).trim();
    }
}
```

---

## 3. 数据库设计

### 3.1 Chat 模块数据表

#### conversations (会话表)
```sql
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_conversations_created_at ON conversations(created_at DESC);
```

#### messages (消息表)
```sql
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,  -- 'user', 'assistant', 'system'
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
```

#### message_tool_results (工具调用结果表)
```sql
CREATE TABLE message_tool_results (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    tool_name VARCHAR(100) NOT NULL,
    input_params TEXT,
    output_result TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tool_results_message_id ON message_tool_results(message_id);
```

#### users (用户表)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    nickname VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### user_model_preferences (用户模型偏好表)
```sql
CREATE TABLE user_model_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_name VARCHAR(50) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, provider_name, model_name)
);

CREATE INDEX idx_user_model_prefs_user_id ON user_model_preferences(user_id);
```

### 3.2 Novel 模块数据表

#### novel_sessions (创作会话表)
```sql
CREATE TABLE novel_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### novel_messages (创作消息表)
```sql
CREATE TABLE novel_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES novel_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_novel_messages_session_id ON novel_messages(session_id);
```

#### novel_materials (素材表)
```sql
CREATE TABLE novel_materials (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    source_url TEXT,
    content TEXT NOT NULL,
    content_type VARCHAR(50),  -- 'pdf', 'txt', 'web'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### novel_material_vectors (素材向量表 - PGVector)
```sql
CREATE TABLE novel_material_vectors (
    id BIGSERIAL PRIMARY KEY,
    material_id BIGINT NOT NULL REFERENCES novel_materials(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding vector(1536),  -- OpenAI embedding dimension
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_material_vectors_embedding ON novel_material_vectors 
USING ivfflat (embedding vector_cosine_ops);
```

---

## 4. API 设计

### 4.1 Chat 模块 API

#### 流式对话
```http
POST /api/chat/stream
Content-Type: application/json
Accept: text/event-stream

Request:
{
  "conversationId": 123,
  "message": "用户消息",
  "provider": "deepseek",
  "model": "deepseek-chat",
  "searchEnabled": true,
  "temperature": 0.7,
  "maxTokens": 2000
}

Response (SSE):
data: {"type":"content","content":"流式"}
data: {"type":"search","query":"...","results":[...]}
data: {"type":"done"}
data: {"type":"error","message":"..."}
```

#### 会话管理
```http
# 获取会话列表
GET /api/conversations?userId=1
Response: {
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "会话标题",
      "createdAt": "2024-01-27T10:00:00",
      "messageCount": 10
    }
  ]
}

# 创建会话
POST /api/conversations
Request: {"userId": 1, "title": "新会话"}
Response: {"success": true, "data": {"id": 2, ...}}

# 删除会话
DELETE /api/conversations/{id}
Response: {"success": true}

# 重新生成标题
POST /api/conversations/{id}/regenerate-title
Response: {"success": true, "data": {"title": "新标题"}}
```

#### 模型管理
```http
# 获取可用模型列表
GET /api/models/available
Response: {
  "success": true,
  "data": [
    {
      "name": "deepseek",
      "displayName": "DeepSeek",
      "models": [
        {
          "name": "deepseek-chat",
          "displayName": "DeepSeek Chat",
          "supportsTools": true,
          "supportsStreaming": true
        }
      ]
    }
  ]
}

# 设置用户偏好
POST /api/models/users/{userId}/preferences
Request: {
  "providerName": "deepseek",
  "modelName": "deepseek-chat",
  "isDefault": true
}
```

### 4.2 Novel 模块 API

#### 文本生成
```http
POST /api/novel/generate/stream
Content-Type: application/json
Accept: text/event-stream

Request:
{
  "sessionId": 1,
  "prompt": "创作指令",
  "provider": "deepseek",
  "useRag": true,
  "ragQuery": "检索关键词"
}

Response (SSE):
data: {"type":"content","content":"生成的文本"}
data: {"type":"rag_context","documents":[...]}
data: {"type":"done"}
```

#### 素材管理
```http
# 导入素材
POST /api/novel/materials
Request: {
  "title": "素材标题",
  "content": "素材内容",
  "sourceUrl": "https://..."
}

# 素材列表
GET /api/novel/materials
Response: {
  "success": true,
  "data": [...]
}

# 搜索素材
GET /api/novel/materials/search?query=关键词&topK=5
Response: {
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "素材标题",
      "snippet": "相关片段",
      "similarity": 0.85
    }
  ]
}
```

---

## 5. 前端设计

### 5.1 路由设计

```javascript
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: () => import('./views/Login.vue')
  },
  {
    path: '/home',
    component: () => import('./views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    component: () => import('./views/Chat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/novel',
    component: () => import('./views/NovelWorkspace.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/mcp',
    component: () => import('./views/Mcp.vue'),
    meta: { requiresAuth: true }
  }
];
```

### 5.2 状态管理 (Pinia)

```javascript
// stores/user.js
export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isAuthenticated: false
  }),
  
  actions: {
    setUser(userData) {
      this.user = userData;
      this.isAuthenticated = true;
      localStorage.setItem('user', JSON.stringify(userData));
    },
    
    logout() {
      this.user = null;
      this.isAuthenticated = false;
      localStorage.removeItem('user');
    }
  }
});
```

### 5.3 SSE 流式处理

```javascript
// utils/sse.js
export function createEventSource(url, data, callbacks) {
  const eventSource = new EventSource(`${url}?${new URLSearchParams(data)}`);
  
  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data);
    
    switch (data.type) {
      case 'content':
        callbacks.onContent?.(data.content);
        break;
      case 'search':
        callbacks.onSearch?.(data);
        break;
      case 'done':
        callbacks.onDone?.();
        eventSource.close();
        break;
      case 'error':
        callbacks.onError?.(data.message);
        eventSource.close();
        break;
    }
  };
  
  eventSource.onerror = () => {
    callbacks.onError?.('连接错误');
    eventSource.close();
  };
  
  return eventSource;
}
```

---

## 6. 技术决策

### 6.1 为什么使用 Spring AI？

**优势**：
- 统一的 ChatClient API，支持多模型
- Advisor 机制实现横切关注点（日志、记忆、安全）
- 工具调用（Tool Calling）标准化
- 活跃的社区和持续更新

**替代方案对比**：
| 方案 | 优势 | 劣势 | 结论 |
|------|------|------|------|
| LangChain4j | Java 原生、功能丰富 | API 复杂、文档少 | ❌ |
| 直接调用 SDK | 灵活性高 | 重复代码多、难维护 | ❌ |
| Spring AI | 统一抽象、Spring 生态 | 相对新、API 变化 | ✅ |

### 6.2 为什么使用 SSE 而不是 WebSocket？

**SSE 优势**：
- 单向推送，满足流式输出需求
- 自动重连机制
- 基于 HTTP，防火墙友好
- 实现简单

**WebSocket 劣势**：
- 双向通信，对于流式输出过于复杂
- 需要额外的心跳保活
- 部分代理不支持

### 6.3 为什么选择 MyBatis 而不是 JPA？

**MyBatis 优势**：
- SQL 控制灵活，适合复杂查询
- 性能更高（无 ORM 转换开销）
- 学习曲线平缓

**JPA 劣势**：
- 复杂查询需要写 JPQL 或 Criteria API
- N+1 问题需要额外处理
- 对于本项目的简单 CRUD，过于重量级

### 6.4 为什么使用 PGVector 而不是专门的向量数据库？

**PGVector 优势**：
- 无需额外部署，PostgreSQL 扩展即可
- 关系数据和向量数据在同一数据库
- 运维简单

**专门向量数据库（Milvus、Weaviate）劣势**：
- 需要额外部署和维护
- 数据同步复杂
- 对于本项目的规模（< 10k 文档）过于重量级

---

## 7. 性能优化策略

### 7.1 ChatClient 缓存

**问题**：每次请求创建 ChatClient 开销大

**解决方案**：
```java
private final Map<String, ChatClient> cache = new ConcurrentHashMap<>();

public ChatClient getChatClient(String provider) {
    return cache.computeIfAbsent(provider, this::createChatClient);
}
```

**效果**：
- 首次请求：200ms
- 缓存命中：< 1ms

### 7.2 连接池配置

**HikariCP 配置**：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 7.3 向量检索优化

**索引策略**：
```sql
-- IVFFlat 索引：适合中等规模数据
CREATE INDEX idx_material_vectors_embedding 
ON novel_material_vectors 
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- 查询优化
SET ivfflat.probes = 10;  -- 增加探测列表数量
```

---

## 8. 安全设计

### 8.1 API Key 保护

```yaml
# 环境变量
export OPENAI_API_KEY=sk-xxx
export DEEPSEEK_API_KEY=sk-xxx

# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

### 8.2 SQL 注入防护

```xml
<!-- MyBatis 参数化查询 -->
<select id="selectByConversationId" resultMap="BaseResultMap">
    SELECT * FROM messages 
    WHERE conversation_id = #{conversationId}
    ORDER BY created_at ASC
</select>
```

### 8.3 XSS 防护

```javascript
// 前端输入过滤
import DOMPurify from 'dompurify';

const sanitizedContent = DOMPurify.sanitize(userInput);
```

---

**文档维护者**: 架构团队  
**最后更新**: 2024-01-27  
**状态**: ✅ 当前版本
