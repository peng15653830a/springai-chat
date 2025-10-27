# Spring AI 架构改进实施指南

本文档是 `SPRING_AI_ARCHITECTURE_ANALYSIS.md` 的实施指南，记录已完成的改进和待完成的任务。

---

## 实施概览

| 改进项 | 优先级 | 状态 | 预计收益 |
|--------|--------|------|----------|
| 统一ChatClient管理 | ⭐⭐⭐⭐⭐ | ✅ 已完成 | 减少200行重复代码 |
| 统一ChatMemory | ⭐⭐⭐⭐⭐ | ✅ 已完成 | 减少150行重复代码 |
| SystemPrompt配置化 | ⭐⭐⭐⭐ | ✅ 已完成 | 易于维护和调优 |
| SimpleLoggerAdvisor | ⭐⭐⭐⭐ | ✅ 已完成 | 提升可观测性 |
| ChatOptions统一管理 | ⭐⭐⭐ | ✅ 已完成 | 配置集中管理 |
| 工具动态注入 | ⭐⭐⭐⭐ | 🔄 进行中 | 降低不必要开销 |
| 结构化输出 | ⭐⭐⭐ | 📋 待实施 | 类型安全 |
| Observation集成 | ⭐⭐⭐ | 📋 待实施 | 监控能力 |
| RAG Advisor | ⭐⭐⭐ | 📋 待实施 | 标准化RAG |

---

## Phase 1: 已完成的改进 ✅

### 1.1 创建统一的ChatClient管理器

**新增文件**：
- `common/agent-core/src/main/java/com/example/client/UnifiedChatClientManager.java`
- `common/agent-core/src/main/java/com/example/client/SystemPromptProvider.java`
- `common/agent-core/src/main/java/com/example/client/ConfigurableSystemPromptProvider.java`

**功能**：
- 实现 `ChatClientResolver` 和 `ClientManager` 接口
- 懒加载创建并缓存 `ChatClient` 实例
- 从 `ModelProviderFactory` 获取 `ChatModel`
- 从 `SystemPromptProvider` 获取 system prompt
- 自动注入 `MessageChatMemoryAdvisor` 和 `SimpleLoggerAdvisor`

**使用方式**：
```java
@Autowired
private UnifiedChatClientManager chatClientManager;

ChatClient client = chatClientManager.getChatClient("deepseek");
```

---

### 1.2 创建AbstractDatabaseChatMemory基类

**新增文件**：
- `common/agent-core/src/main/java/com/example/memory/AbstractDatabaseChatMemory.java`

**功能**：
- 提供 `ChatMemory` 接口的通用实现逻辑
- 处理 conversationId 解析和验证
- Spring AI Message 与数据库实体的转换
- 角色映射（User/Assistant/System）

**子类实现**：
```java
// chat模块
public class DatabaseChatMemory extends AbstractDatabaseChatMemory {
    @Override
    protected void saveMessage(Long cid, String role, String content) {
        // 使用 MessageMapper 保存
    }
    
    @Override
    protected List<MessageEntity> loadMessages(Long cid) {
        // 从数据库加载
    }
    
    @Override
    protected void deleteMessages(Long cid) {
        // 删除消息
    }
}
```

**重构成果**：
- `chat/DatabaseChatMemory`: 从109行减少到74行（减少32%）
- `novel/NovelDatabaseChatMemory`: 从113行减少到56行（减少50%）
- 总计减少约102行重复代码

---

### 1.3 SystemPrompt配置化

**新增文件**：
- `chat/src/main/java/com/example/config/ChatSystemPromptProvider.java`
- `novel/src/main/java/com/example/novel/config/NovelSystemPromptProvider.java`

**功能**：
- 不同模块可以提供自己的 `SystemPromptProvider` 实现
- 支持针对不同 provider 定制不同的 system prompt
- chat 模块: DeepSeek vs 其他模型使用不同的prompt
- novel 模块: 专注长文本创作的prompt

**配置示例**：
```yaml
# application.yml
chat:
  system-prompt:
    custom-enabled: true
    max-tool-calls: 3

novel:
  system-prompt:
    custom-enabled: true
```

**优势**：
- 移除了硬编码的system prompt
- 易于调优和A/B测试
- 支持国际化（多语言prompt）

---

### 1.4 SimpleLoggerAdvisor集成

**新增文件**：
- `common/agent-core/src/main/java/com/example/config/AdvisorConfig.java`

**功能**：
- 自动记录所有 ChatClient 的请求和响应
- 支持通过配置开启/关闭

**配置**：
```yaml
spring:
  ai:
    chat:
      advisor:
        logger:
          enabled: true  # 默认开启
```

**效果**：
- 统一的日志格式
- 便于调试和问题排查
- 为后续接入 Observation 打基础

---

### 1.5 ChatOptions统一管理

**新增文件**：
- `common/agent-core/src/main/java/com/example/config/ChatOptionsProperties.java`
- `common/agent-core/src/main/java/com/example/stream/springai/AbstractChatOptionsFactory.java`

**重构文件**：
- `chat/src/main/java/com/example/streaming/ChatModuleOptionsFactory.java`

**功能**：
- 提供 `AbstractChatOptionsFactory` 基类
- 统一 temperature、maxTokens、topP 的解析逻辑
- 支持按provider差异化配置

**配置示例**：
```yaml
spring:
  ai:
    chat:
      options:
        openai:
          temperature: 0.7
          max-tokens: 2000
          top-p: 0.9
          system-prompt: "自定义OpenAI prompt"
        deepseek:
          temperature: 0.8
          max-tokens: 4000
          system-prompt: "自定义DeepSeek prompt"
```

**重构成果**：
- ChatModuleOptionsFactory 从89行减少到85行
- 移除了重复的参数解析逻辑
- 更易于扩展新的 provider

---

### 1.6 模块委托统一管理器

**重构文件**：
- `novel/src/main/java/com/example/novel/manager/NovelClientManager.java`

**变更**：
```java
// 重构前：自己实现完整的ChatClient创建逻辑
private ChatClient createClient(String provider) {
    ChatModel chatModel = modelProviderFactory.getChatModel(provider);
    String systemPrompt = buildSystemPrompt();
    return ChatClient.builder(chatModel)
        .defaultSystem(systemPrompt)
        .defaultAdvisors(messageChatMemoryAdvisor)
        .build();
}

// 重构后：委托给UnifiedChatClientManager
public ChatClient getChatClient(String provider) {
    return unifiedChatClientManager.getChatClient(provider);
}
```

**成果**：
- NovelClientManager 从61行减少到31行（减少49%）
- 标记为 `@Deprecated`，提示开发者直接使用 UnifiedChatClientManager
- 保持向后兼容，现有代码无需修改

---

## Phase 2: 进行中的改进 🔄

### 2.1 工具动态注入机制

**现状问题**：
- WebSearchTool 通过 ToolsProvider 在运行时注入
- 即使 searchEnabled=false，工具仍然被注册到prompt中
- 浪费 prompt tokens

**计划改进**：
```java
// 创建 ToolManager 接口
public interface ToolManager {
    List<Object> resolveTools(ToolContext context);
}

// 实现按需注入
@Component
public class DefaultToolManager implements ToolManager {
    @Override
    public List<Object> resolveTools(ToolContext context) {
        List<Object> tools = new ArrayList<>();
        
        if (context.isSearchEnabled()) {
            tools.add(webSearchTool);
        }
        
        if (context.isMcpEnabled()) {
            tools.add(mcpTool);
        }
        
        return tools;
    }
}

// 在 SpringAiTextStreamClient 中使用
var tools = toolManager.resolveTools(request);
if (!tools.isEmpty()) {
    promptSpec = promptSpec.tools(tools.toArray());
}
```

**预期收益**：
- 减少不必要的 prompt token 消耗
- 更清晰的工具启用逻辑
- 易于扩展新工具（MCP、数据库查询等）

---

## Phase 3: 待实施的改进 📋

### 3.1 结构化输出

**目标场景**：
1. 对话标题生成
2. Thinking 内容提取
3. 搜索结果解析

**实施示例**：
```java
// 定义输出结构
public record ConversationTitle(
    @JsonProperty("title") String title,
    @JsonProperty("summary") String summary,
    @JsonProperty("keywords") List<String> keywords
) {}

// 使用结构化输出
ConversationTitle result = chatClient
    .prompt()
    .user("根据以下内容生成标题：" + message)
    .call()
    .entity(ConversationTitle.class);
```

**预期收益**：
- 类型安全，减少运行时错误
- 减少字符串解析代码
- AI 输出更稳定（JSON schema 约束）

---

### 3.2 Observation 集成

**计划步骤**：

1. **配置 ObservationRegistry**：
```java
@Configuration
public class AiObservabilityConfig {
    @Bean
    public ObservationRegistry aiObservationRegistry(MeterRegistry meterRegistry) {
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig()
            .observationHandler(new DefaultMeterObservationHandler(meterRegistry));
        return registry;
    }
}
```

2. **ChatClient 启用观测**：
```java
ChatClient.builder(model)
    .observationRegistry(observationRegistry)
    .build();
```

3. **自定义指标**：
```java
@Component
public class AiMetricsRecorder {
    public void recordToolCall(String toolName, long durationMs, boolean success) {
        Timer.builder("ai.tool.call")
            .tag("tool", toolName)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
```

4. **暴露 Prometheus 端点**：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**预期指标**：
- `spring.ai.chat.client.call.duration` - 调用延迟
- `spring.ai.chat.client.call.tokens` - Token使用量
- `ai.tool.call` - 工具调用次数和耗时
- `ai.errors` - 错误分类统计

---

### 3.3 QuestionAnswerAdvisor for RAG

**当前问题**：
- novel 模块自己实现了一套 RAG 逻辑
- 没有使用 Spring AI 的标准 RAG advisor

**改进方案**：
```java
@Configuration
public class NovelRagConfig {
    @Bean
    public QuestionAnswerAdvisor novelRagAdvisor(
            VectorStore vectorStore,
            SearchRequestProvider searchRequestProvider) {
        return QuestionAnswerAdvisor.builder()
            .vectorStore(vectorStore)
            .searchRequestProvider(searchRequestProvider)
            .build();
    }
    
    @Bean
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return new PgVectorStore(jdbcTemplate, embeddingModel);
    }
}
```

**使用**：
```java
ChatClient.builder(model)
    .defaultSystem(systemPrompt)
    .defaultAdvisors(
        simpleLoggerAdvisor,
        messageChatMemoryAdvisor,
        novelRagAdvisor  // 标准 RAG
    )
    .build();
```

---

## 使用指南

### 如何启用改进

#### 1. 启用 UnifiedChatClientManager

在模块的 application.yml 中配置：
```yaml
spring:
  ai:
    chat:
      advisor:
        logger:
          enabled: true
      options:
        deepseek:
          temperature: 0.8
          max-tokens: 4000
```

然后注入使用：
```java
@Autowired
private UnifiedChatClientManager chatClientManager;

ChatClient client = chatClientManager.getChatClient("deepseek");
```

#### 2. 自定义 SystemPrompt

创建自己的 SystemPromptProvider：
```java
@Component
public class MySystemPromptProvider implements SystemPromptProvider {
    @Override
    public String getSystemPrompt(String provider) {
        return "自定义的system prompt for " + provider;
    }
}
```

#### 3. 迁移现有代码

**chat模块**：
```java
// 旧代码（仍然可用）
@Autowired
private ChatClientManager chatClientManager;
ChatClient client = chatClientManager.getChatClient("deepseek");

// 推荐使用（更简洁）
@Autowired
private UnifiedChatClientManager unifiedManager;
ChatClient client = unifiedManager.getChatClient("deepseek");
```

**novel模块**：
```java
// 旧代码（已标记为deprecated）
@Autowired
private NovelClientManager novelClientManager;
ChatClient client = novelClientManager.getChatClient("deepseek");

// 推荐直接使用
@Autowired
private UnifiedChatClientManager chatClientManager;
ChatClient client = chatClientManager.getChatClient("deepseek");
```

---

## 向后兼容性

所有改进都保持向后兼容：

1. **现有代码无需修改**：
   - ChatClientManager 和 NovelClientManager 仍然可用
   - 它们内部委托给 UnifiedChatClientManager

2. **渐进式迁移**：
   - 可以逐步将代码迁移到新的API
   - 不会造成breaking changes

3. **配置兼容**：
   - 新增的配置项都有默认值
   - 不配置也能正常运行

---

## 性能影响

| 改进项 | 性能影响 | 说明 |
|--------|----------|------|
| UnifiedChatClientManager | 无影响 | 仍使用缓存，懒加载 |
| AbstractDatabaseChatMemory | 轻微提升 | 减少了重复的类型转换 |
| SimpleLoggerAdvisor | 轻微开销 | 可通过配置关闭 |
| ChatOptions统一管理 | 无影响 | 只是重构，逻辑不变 |

---

## 测试建议

### 单元测试

```java
@SpringBootTest
class UnifiedChatClientManagerTest {
    @Autowired
    private UnifiedChatClientManager manager;
    
    @Test
    void testGetChatClient() {
        ChatClient client = manager.getChatClient("deepseek");
        assertNotNull(client);
    }
    
    @Test
    void testCaching() {
        ChatClient client1 = manager.getChatClient("deepseek");
        ChatClient client2 = manager.getChatClient("deepseek");
        assertSame(client1, client2); // 验证缓存
    }
}
```

### 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ChatIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testChatWithUnifiedManager() throws Exception {
        mockMvc.perform(post("/api/chat/stream")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "message": "Hello",
                    "provider": "deepseek",
                    "searchEnabled": false
                }
                """))
            .andExpect(status().isOk());
    }
}
```

---

## 常见问题

### Q1: UnifiedChatClientManager 会影响现有功能吗？

A: 不会。它通过 `@ConditionalOnMissingBean` 注解，只在没有冲突的bean时才创建。现有的 ChatClientManager 仍会正常工作。

### Q2: 如何禁用 SimpleLoggerAdvisor？

A: 在 application.yml 中配置：
```yaml
spring:
  ai:
    chat:
      advisor:
        logger:
          enabled: false
```

### Q3: SystemPromptProvider 优先级如何确定？

A: Spring会自动选择最具体的实现。如果你的模块提供了自定义的 SystemPromptProvider，它会覆盖默认的 ConfigurableSystemPromptProvider。

### Q4: 旧代码什么时候需要迁移？

A: 不着急。可以等新功能稳定后再逐步迁移。我们会在下个大版本中才移除 deprecated 的类。

---

## 下一步计划

### 短期（1-2周）
- [ ] 完成工具动态注入机制
- [ ] 为关键场景添加结构化输出
- [ ] 编写迁移指南和最佳实践文档

### 中期（1个月）
- [ ] 集成 Observation 和 Micrometer
- [ ] 为 novel 模块接入 QuestionAnswerAdvisor
- [ ] 性能优化和压测

### 长期（2-3个月）
- [ ] 完全移除重复的 ChatClientManager
- [ ] 引入更多 Spring AI 能力（Embedding、Moderation等）
- [ ] 建立 AI 系统的完整监控体系

---

## 贡献指南

如果你要继续实施改进：

1. **遵循现有模式**：参考已完成的改进方式
2. **保持向后兼容**：不要破坏现有API
3. **添加测试**：确保新功能有测试覆盖
4. **更新文档**：及时更新本文档和代码注释

---

## 参考资料

- [SPRING_AI_ARCHITECTURE_ANALYSIS.md](./SPRING_AI_ARCHITECTURE_ANALYSIS.md) - 详细的架构分析报告
- [Spring AI官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)
- [Spring AI Advisor指南](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_advisors)

---

**最后更新**: 2024-01
**维护者**: AI架构团队
