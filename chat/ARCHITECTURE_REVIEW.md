# 后端架构与设计评审报告（Spring AI / WebFlux / MyBatis）

本文面向本仓库 chat 模块（Spring Boot 3 + Java 17），聚焦架构、软件设计、Spring AI 使用方式与可运维性，识别关键问题并提出循序渐进的改进建议。评审以"降低复杂性"为核心原则，尽量在不引入不必要抽象的前提下提升一致性、稳定性与可观测性。

- 评审对象：`chat/`（Spring Boot 3、Spring AI 1.0、WebFlux、MyBatis）
- 代码入口与关键文件：
  - 配置：`src/main/resources/application.yml`
  - Spring AI 管理：`com.example.manager.ChatClientManager`
  - 模型装配：`com.example.config.OpenAiCompatibleConfig`、`DeepSeekConfig`、`GreatWallConfig`
  - 聊天主流程：`com.example.service.impl.AiChatServiceImpl`
  - 工具（Tool Calling）：`com.example.tool.WebSearchTool`
  - 记忆（Memory）：`com.example.memory.DatabaseChatMemory` + `MessageChatMemoryAdvisor`
  - SSE 事件：`com.example.service.impl.SseEventPublisherImpl`
  - 错误处理：`com.example.handler.DefaultChatErrorHandler`

---

## 总体结论

- 基础能力较完备：已正确使用 Spring AI ChatClient、Memory、@Tool 注解与 SSE 流；自研了非标准 API（长城大模型）的 ChatModel 实现，抽象清晰。
- 当前主要短板：
  - 职责边界与一致性欠佳（系统提示/Prompt/工具注册/模型解析混杂于 `ChatClientManager`）。
  - Reactive 与阻塞调用混用（搜索在 Tool 中阻塞）。
  - 命名与配置不统一（Provider 大小写、Bean 名兼容逻辑复杂）。
  - Spring AI 能力未充分利用（结构化输出、消息建模、统一观测与容错）。
- 改进方向：
  - 收敛职责与命名，按请求注入工具，标准化 Prompt/消息建模。
  - 消除阻塞点，增强重试/超时与可观测性。
  - 以“结构化输出/RAG 组件/统一工厂”逐步升级能力但不过度设计。

---

## 现状扫描

- 分层：Controller / Service / Config / Strategy（模型选择、提示构建）/ Tool / Memory / ErrorHandler 清晰。
- Spring AI 实用点：
  - `ChatClient.builder(chatModel).defaultSystem().defaultTools().defaultAdvisors()`（统一注入系统提示、工具与记忆 Advisor）。
  - `@Tool` + `ToolContext` 传入会话/消息 ID，落库工具调用（`MessageToolResult`）。
  - 自定义 `ChatMemory`（数据库持久化）与 `MessageChatMemoryAdvisor`。
- SSE：将搜索事件与聊天主流合并（`Flux.merge`），结构清晰。

---

## 架构与分层评估

### 模块边界
- 优点：SSE 与搜索事件分离；错误处理集中；策略接口（`PromptBuilder`、`ModelSelector`）具备扩展性。
- 问题：`ChatClientManager` 同时负责系统提示、工具注册、记忆 Advisor 注入与 ChatModel 解析，职责偏重；与 Bean 命名强耦合，复杂度与隐式行为上升。

### Reactive 链路
- 优点：端到端使用 Reactor，SSE 合并流清晰。
- 问题：`WebSearchTool` 内部调用 `SearchServiceImpl` 为阻塞式（`block()`），在 Tool 调用期间可能占用事件线程；`Sinks.many().multicast().onBackpressureBuffer()` 若无上限在极端情况有 OOM 风险。

### 记忆（Memory）
- 优点：`DatabaseChatMemory` 只读历史，由应用层负责持久化，避免重复写。
- 问题：`ChatMemory.add` 跳过用户与助手消息持久化，强依赖“应用层先存”的隐式契约；如果未来出现新入口，容易出现历史缺失。建议将“持久化的唯一通道”收敛到 `MessageService`，并在内核处标注清晰契约。

### 控制器与服务
- 控制器较薄，服务内主流程（准备→执行→完成）分段清晰。
- `extractThinkingParts` 存在但未贯穿到最终保存（`updateMessageContent` 传入 `thinking=null`），导致“深度思考内容”没有真正落库。

---

## Spring AI 使用评估

### 已使用得当
- ChatClient Builder 与 Advisor、Memory、Tool 注解与 ToolContext 使用符合 1.0 版本理念。
- 非标准 API（长城）以 `ChatModel` 实现对齐 Spring AI 抽象，较为规范。

### 不足与改进空间
- 提示与消息建模：
  - 现用 `PromptBuilder` 拼文本（形如 `User:`/`Assistant:`），而 ChatClient 原生支持 System/User/Assistant 角色消息。建议将 `PromptBuilder` 收敛为“构造消息集合”，避免“双重提示格式”。
- 工具注册：
  - 通过 `defaultTools(webSearchTool)` 全局启用，即使 `request.isSearchEnabled()==false` 仍可能触发工具，影响稳定性。建议改为“按请求注入工具”。
- 结构化输出：
  - 未使用 Spring AI 的结构化输出（JsonSchema/Bean 提取）。对于需要强类型消费（标题、引用列表等），建议启用以减少二次解析。
- 观测与容错：
  - 目前主要靠手写 `timeout/retry`，未统一到 `Observation/Micrometer` 与 Spring Retry；缺少指标（延迟、失败率、工具调用次数等）。
- RAG 组件：
  - 目前为 Web 搜索 + 文本拼接，缺少向量检索与检索器管线（VectorStore/Retriever），效果稳定性受限。

---

## 配置与运维性

- Provider 命名不统一：`DeepSeek`（配置） vs `deepseek`（代码常量），`resolveChatModel` 通过历史命名/子串匹配解析 Bean，隐式且不可预测。

---

## 具体问题清单

- 高优先级
  1. 提示词构建与系统提示职责混淆：`PromptBuilder` 文本拼接叠加 `defaultSystem`。
  2. 工具全局注册：未开启搜索也可能触发 Tool，增加不确定性与性能开销。
  3. ChatModel 解析复杂且与 Bean 命名强耦合：大小写/历史命名/子串匹配提高了复杂度与误判概率。
  4. Tool 内部阻塞调用：搜索使用阻塞式 HTTP，破坏 Reactor 线程模型。
  5. 深度思考未落库：`extractThinkingParts` 未在保存阶段应用。
  6. SSE Backpressure：无限缓冲的 Sinks 在压力场景下存在内存风险。

- 中优先级
  1. 各 Provider ChatModel 装配重复：OpenAI 兼容/DeepSeek/GreatWall 存在重复样板代码。
  2. 重试策略粗糙：对流式接口的粗暴重试可能放大副作用，缺少按错误类型的精细控制。
  3. 观测缺失：无统一指标与链路观测（QPS、P95、错误分类、工具调用次数等）。

---

## 改进建议（按复杂度分批）

### 第一批（低风险，高收益）
- 按请求注入工具
  - 移除 `ChatClientManager.defaultTools(webSearchTool)`；在 `AiChatServiceImpl.streamFromAi` 中根据 `request.isSearchEnabled()` 决定 `.tools(webSearchTool)` 是否启用。
- 标准化提示与消息
  - `PromptBuilder` 改为产出 `List<Message>`（System/User 等角色），`ChatClient.prompt().messages(...)` 直接使用；系统提示改为配置化或由 `PromptBuilder.buildSystemPrompt()` 提供，避免在 `ChatClientManager.defaultSystem` 里硬编码。
- 统一 Provider 命名
  - 配置与代码统一使用小写 provider key，并将 ChatModel Bean 命名为 provider 同名（如 `@Bean(name="openai")`），`ChatClientManager` 直接映射获取，删除启发式/历史兼容分支。
- 深度思考内容落库
  - 在最终聚合内容时应用 `extractThinkingParts`，并在 `messageService.updateMessageContent(id, content, thinking)` 落库；可同时在 SSE 增加 `thinking` 事件（已有类型）。

### 第二批（可靠性与可观测性）
- 搜索服务非阻塞化
  - 将 `SearchService` 改为 `Mono<List<SearchResult>>`；`WebSearchTool` 在独立调度器（`Schedulers.boundedElastic`）调用并限时，避免阻塞 Reactor 线程。
- 统一重试与超时
  - 使用 Spring Retry（基于错误类型）与 WebClient 超时，统一在配置中管理；对 streaming 接口避免无脑重试。
- Sinks 背压与心跳
  - 为 `SseEventPublisher` 的 Sinks 设定容量/丢弃策略；实现 `heartbeat-interval` 心跳事件，提升长连接稳定性。
- 观测指标
  - 接入 Micrometer/Observation，暴露：聊天时延、chunk 数量、工具调用次数/时延、错误分类等指标。

### 第三批（能力增强）
- 结构化输出
  - 对强类型需求（标题/引用/检索条目）使用 Spring AI 结构化输出（JsonSchema/Bean 提取），减少文本解析与歧义。
- Provider 工厂化
  - 抽象 `ProviderModelFactory`，输入 `ProviderConfig/ModelConfig` 输出 `ChatModel`，消除 `OpenAiCompatibleConfig/DeepSeekConfig/GreatWallConfig` 重复样板，降低新接入成本。
- RAG 管线
  - 如有需要，引入 VectorStore + Retriever，将 Web 搜索替换/补充为可观察的知识检索链；继续沿用 `MessageToolResult` 统一审计。

---

## 与 MCP 的结合（方向建议）

- 搜索外包：将后端 SearchService 切换为调用 MCP 网关（如 tavily/exa MCP），后端只用 HTTP 访问统一网关，可在 MCP 侧快速替换/组合搜索源。
- 工具目录：通过 MCP 工具 registry 按会话动态选择可用工具，再按请求注入 ChatClient，降低后端耦合与复杂度。
- 审计复用：沿用 `MessageToolResult` 记录 MCP 工具名/输入/输出/耗时，方便效果评估与 A/B 实验。

---

## 风险与权衡（复杂性优先）

- 默认全局注入（工具/系统提示）虽易上手，但长期会积累隐式耦合与不可预期行为；按请求注入与配置驱动更利于控制复杂性。
- 结构化输出/RAG/观测能力的引入应循序渐进，优先替换最痛点的文本解析与不可观测环节，避免一次性重构带来风险。

---

## 落地计划（建议）

- 里程碑 M1（1–2 天）
  - 工具按需注入；统一 provider 命名/Bean 名；提取与落库 thinking；配置安全项修正；PromptBuilder 输出标准消息。
  - 验收：核心用例通过；未开启搜索时不再触发工具；thinking 在 DB 可见。
- 里程碑 M2（2–3 天）
  - 搜索非阻塞化；Sinks 背压；统一超时/重试；接入基础指标。
  - 验收：压测不阻塞事件线程；指标可在 Actuator/Micrometer 拉取。
- 里程碑 M3（按需）
  - 结构化输出；Provider 工厂化；引入 RAG 管线。
  - 验收：强类型字段以 DTO 输出；新增 Provider 仅改配置与工厂映射。

---

## 附录：关键代码清单

- Chat 管理：`com.example.manager.ChatClientManager`
- 提示构建：`com.example.strategy.prompt.*`
- 聊天主流程：`com.example.service.impl.AiChatServiceImpl`
- 工具调用：`com.example.tool.WebSearchTool`
- 记忆持久化：`com.example.memory.DatabaseChatMemory`，`com.example.config.MemoryConfig`
- 错误处理：`com.example.handler.*`
- 长城模型：`com.example.integration.ai.greatwall.*`
- 搜索服务：`com.example.service.impl.SearchServiceImpl`，`com.example.config.SearchProperties`
- 测试：`src/test/java`（覆盖控制器、服务与错误处理等）

---

（本报告仅涉及后端架构与设计，未覆盖前端与部署脚本细节。）

