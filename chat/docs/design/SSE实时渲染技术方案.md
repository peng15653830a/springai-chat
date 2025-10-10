# SSE 实时渲染 Markdown 技术方案

## 问题背景

在使用 Spring Boot + Vue 3 开发 AI 聊天应用时，通过 SSE (Server-Sent Events) 实现流式响应。在技术演进过程中遇到了两个核心问题：

### 第一代问题：Markdown格式传输
- **现象**：Markdown 表格在流式传输时无法正确渲染，所有内容挤在一行
- **对比**：刷新页面后（从 API 直接获取完整内容）表格正常显示
- **影响**：用户体验极差，实时渲染效果完全失效

### 第二代问题：技术选型和架构
- **Vue兼容性**：@kangc/v-md-editor与Vue 3兼容性问题
- **SSE连接稳定性**：自制SSE逻辑复杂，连接中断导致内容不完整
- **代码维护性**：过多自制组件，维护成本高，容易出错

## 问题分析

### 1. 根本原因

SSE 协议与 Markdown 格式的冲突：

- **SSE 协议规范**：使用 `\n\n`（双换行）作为消息分隔符
- **Markdown 格式**：依赖换行符 `\n` 来分隔表格行、段落等
- **冲突结果**：Markdown 内容中的换行符被 SSE 协议误处理，导致格式丢失

### 2. 数据流分析

```
原始内容：
"# 标题\n| 列1 | 列2 |\n|---|---|\n| 数据1 | 数据2 |"

SSE 传输后：
"# 标题| 列1 | 列2 ||---|---|| 数据1 | 数据2 |"  // 换行符全部丢失
```

### 3. 为什么刷新后正常？

刷新页面后，Vue 从后端 API 直接获取历史消息（通过普通 HTTP 请求），不经过 SSE 传输，因此换行符保持完整。

## 解决方案演进

### 第一代解决方案：JSON 序列化（已过时）

使用 JSON 包装 SSE 数据，利用 JSON 自动转义特殊字符的特性：

1. **后端**：将内容包装成 JSON 对象，`\n` 自动转义为 `\\n`
2. **传输**：SSE 传输 JSON 字符串，换行符已被保护
3. **前端**：解析 JSON，`\\n` 自动恢复为 `\n`

**局限性**：只解决了格式传输问题，未解决技术架构根本问题

### 第二代解决方案：专业组件重构（当前）

**核心理念**：使用业界标准组件，避免重复造轮子

#### 1. Markdown渲染组件升级
```javascript
// 旧方案：@kangc/v-md-editor (Vue 2兼容性问题)
import VMdPreview from '@kangc/v-md-editor/lib/preview'

// 新方案：vue-markdown-render (Vue 3原生支持)
import VueMarkdownRender from 'vue-markdown-render'
```

#### 2. SSE连接管理专业化
```javascript
// 旧方案：自制150+行SSE逻辑
const eventSource = ref(null)
const setupEventSource = (conversationId) => {
  // 150+行复杂的连接管理、错误处理、重连逻辑
}

// 新方案：VueUse专业组件
import { useEventSource } from '@vueuse/core'
const { data, status, error } = useEventSource(sseUrl, [], {
  autoReconnect: { retries: 3, delay: 1000 }
})
```

### 第二代具体实现

#### 1. Markdown渲染组件重构

```vue
<!-- 旧实现：复杂的自制组件 -->
<StreamingMarkdown 
  :content="message.content"
  :enable-typewriter="true"
  class="message-body"
/>

<!-- 新实现：标准组件调用 -->
<VueMarkdownRender 
  :source="String(message.content || '')"
  class="message-body markdown-content"
/>
```

**优势对比**：
- ✅ 代码量：从150行减少到1行
- ✅ 稳定性：使用经过验证的开源组件
- ✅ 兼容性：Vue 3原生支持
- ✅ 维护性：无需维护自制逻辑

#### 2. SSE连接管理重构

```javascript
// 旧实现：复杂的自制SSE逻辑
const eventSource = ref(null)
const disconnectSSE = () => { /* 复杂逻辑 */ }
const setupEventSource = (conversationId) => {
  // 150+行代码处理：
  // - 连接建立
  // - 错误处理  
  // - 重连机制
  // - 状态管理
  // - 数据解析
}

// 新实现：VueUse专业组件
import { useEventSource } from '@vueuse/core'

const sseUrl = computed(() => 
  chatStore.currentConversation?.id 
    ? `/api/chat/stream/${chatStore.currentConversation.id}`
    : undefined
)

const { data: sseData, status: sseStatus, error: sseError } = useEventSource(
  sseUrl,
  [],
  {
    immediate: false,
    autoReconnect: {
      retries: 3,
      delay: 1000,
      onFailed() {
        ElMessage.error('连接失败，请检查网络')
      }
    }
  }
)

// 监听数据变化
watch(sseData, (newData) => {
  if (newData) {
    const sseEvent = JSON.parse(newData)
    handleSSEEvent(sseEvent)
  }
})
```

**优势对比**：
- ✅ 代码量：从150行减少到20行
- ✅ 自动重连：内置专业重连机制
- ✅ 错误恢复：久经考验的错误处理
- ✅ 状态管理：响应式状态自动管理

## 技术要点

### 1. Markdown 渲染组件选择（2025年更新）

**第二代组件选型原则**：
- 优先选择Vue 3原生支持的组件
- 避免Vue 2兼容性问题
- 选择轻量级、稳定的标准组件

**推荐方案**：
```javascript
// 当前推荐：vue-markdown-render
import VueMarkdownRender from 'vue-markdown-render'
// - Vue 3原生支持
// - 轻量级（基于markdown-it）
// - 支持代码高亮、表格等完整语法
```

**不推荐方案**：
```javascript
// 已弃用：@kangc/v-md-editor
// - Vue 2设计，Vue 3兼容性问题
// - 功能过重，不适合简单场景
```

### 2. SSE 连接管理最佳实践

**使用专业组件**：
```javascript
// 推荐：VueUse useEventSource
import { useEventSource } from '@vueuse/core'
// - 自动重连机制
// - 响应式状态管理
// - 久经考验的错误处理
```

**避免自制实现**：
- ❌ 自制EventSource封装：复杂且容易出错
- ❌ 手动重连逻辑：边界情况处理困难
- ❌ 自制状态管理：不如专业组件稳定

### 3. 性能优化策略

**第二代优化重点**：
- ✅ **组件层面**：使用高效的标准组件
- ✅ **连接层面**：专业组件自动优化
- ✅ **渲染层面**：Vue 3响应式系统自动批量更新
- ✅ **内存管理**：专业组件自动清理资源

## 其他可选方案

### 1. 占位符替换

```javascript
// 后端
content = content.replace(/\n/g, "<|newline|>")

// 前端
content = content.replace(/<\|newline\|>/g, '\n')
```

**缺点**：需要确保占位符不会与实际内容冲突

### 2. Base64 编码

```javascript
// 后端
sendData = Base64.encode(content)

// 前端
content = Base64.decode(data)
```

**缺点**：增加数据传输量，性能开销较大

### 3. 自定义 SSE 解析器

完全绕过标准 EventSource API，自己实现 SSE 解析逻辑。

**缺点**：实现复杂，需要处理各种边界情况

## 最佳实践（2025年更新）

### 技术选型原则
1. **优先使用业界标准组件**：避免重复造轮子，选择经过验证的开源方案
2. **Vue 3原生兼容**：确保所有组件都支持Vue 3，避免兼容性问题
3. **专业组件胜过自制**：复杂功能（SSE、状态管理）交给专业组件处理
4. **简单即是美**：21行代码胜过300行复杂实现

### 开发实践
1. **渐进式升级**：先解决核心问题，再优化细节
2. **代码审视**：定期检查是否有过度设计的自制组件
3. **性能监控**：关注bundle size，避免引入过重的依赖
4. **测试覆盖**：重点测试组件集成和边界情况

### 维护策略
1. **依赖管理**：定期更新依赖，关注安全漏洞
2. **文档同步**：技术变更后及时更新文档
3. **团队协作**：建立组件选型和技术决策流程

## 经验总结

### 技术决策教训
1. **不要盲目自制组件**：先调研社区方案，评估维护成本
2. **听取用户反馈**：用户往往能指出技术选型的根本问题
3. **关注生态兼容性**：选择与主框架版本匹配的组件
4. **代码简洁性优于功能完整性**：稳定简单的方案胜过复杂全能的方案

### 架构演进启示
1. **技术栈要与时俱进**：Vue 2到Vue 3的升级不可避免
2. **专业组件生态很重要**：VueUse等专业库大大降低开发复杂度
3. **性能和维护性并重**：不仅要考虑当前性能，还要考虑长期维护
4. **社区实践值得参考**：成熟的开源项目已经解决了大部分常见问题

## 参考资料

- [MDN - Using server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events)
- [The line break problem when using Server Sent Events (SSE)](https://medium.com/@thiagosalvatore/the-line-break-problem-when-using-server-sent-events-sse-1159632d09a0)
- [Solving Markdown Newline Issues in LLM Stream Responses](https://yingjiezhao.com/en/articles/Solving-Markdown-Newline-Issues-in-LLM-Stream-Responses/)

## 项目信息

### 当前技术栈（第二代）
- **后端框架**：Spring Boot 2.7.18
- **前端框架**：Vue 3.3.8 + Vite 4.5.0
- **状态管理**：Pinia 2.1.7
- **UI组件库**：Element Plus 2.4.2
- **Markdown渲染**：vue-markdown-render 2.2.1
- **SSE客户端**：@vueuse/core useEventSource
- **代码高亮**：highlight.js 11.11.1
- **工具库**：@vueuse/core 13.6.0

### 技术演进历史
- **第一代**（2025-08-08）：JSON序列化方案，解决格式传输问题
- **第二代**（2025-08-15）：专业组件重构，解决架构和维护性问题

### 核心改进成果
- **代码量减少**：从300+行减少到21行核心逻辑
- **稳定性提升**：使用久经考验的专业组件
- **维护成本降低**：标准化技术栈，易于维护
- **Vue 3兼容性**：完全解决兼容性问题