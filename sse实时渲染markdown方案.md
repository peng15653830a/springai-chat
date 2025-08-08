# SSE 实时渲染 Markdown 方案

## 问题背景

在使用 Spring Boot + Vue 3 开发 AI 聊天应用时，通过 SSE (Server-Sent Events) 实现流式响应。但在实时渲染 Markdown 内容（特别是表格）时遇到了严重的格式问题：

- **现象**：Markdown 表格在流式传输时无法正确渲染，所有内容挤在一行
- **对比**：刷新页面后（从 API 直接获取完整内容）表格正常显示
- **影响**：用户体验极差，实时渲染效果完全失效

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

## 解决方案

### 核心思路：JSON 序列化

使用 JSON 包装 SSE 数据，利用 JSON 自动转义特殊字符的特性：

1. **后端**：将内容包装成 JSON 对象，`\n` 自动转义为 `\\n`
2. **传输**：SSE 传输 JSON 字符串，换行符已被保护
3. **前端**：解析 JSON，`\\n` 自动恢复为 `\n`

### 具体实现

#### 1. 后端改造（SseEmitterManager.java）

```java
@Component
public class SseEmitterManager {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void sendMessage(Long conversationId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(conversationId);
        if (emitter != null) {
            try {
                Object sendData = data;
                
                if ("chunk".equals(eventName)) {
                    // 对于chunk事件，使用JSON包装以保留换行符
                    if (data != null) {
                        Map<String, String> wrapper = new HashMap<>();
                        wrapper.put("content", String.valueOf(data));
                        // JSON序列化会自动转义换行符，避免SSE协议冲突
                        sendData = objectMapper.writeValueAsString(wrapper);
                    }
                }
                
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(sendData));
                    
            } catch (Exception e) {
                emitters.remove(conversationId);
                emitter.completeWithError(e);
            }
        }
    }
}
```

#### 2. 前端改造（Chat.vue）

```javascript
sseClient.on('chunk', (data) => {
    try {
        // 解析JSON格式的chunk数据
        let chunkContent = ''
        try {
            // 尝试解析JSON（后端使用JSON包装的情况）
            const parsed = typeof data === 'string' ? JSON.parse(data) : data
            chunkContent = parsed.content || ''
        } catch (e) {
            // 如果不是JSON，直接使用原始数据（向后兼容）
            chunkContent = String(data || '')
        }
        
        if (!chunkContent) return
        
        // 更新消息内容
        if (lastMessage && lastMessage.role === 'assistant') {
            lastMessage.content = (lastMessage.content || '') + chunkContent
            // 触发响应式更新
            chatStore.messages = [...chatStore.messages]
        }
    } catch (error) {
        console.error('Error processing chunk:', error)
    }
})
```

## 技术要点

### 1. Markdown 渲染组件选择

测试过的组件：
- `vue-markdown-render` - 轻量但对流式更新支持一般
- `@kangc/v-md-editor` - 功能全面，预览组件效果好

**结论**：组件本身都没问题，问题在于数据传输层

### 2. SSE 配置要点

- 使用 `vue-sse` 插件简化前端 SSE 连接管理
- 设置合适的超时时间（5分钟）以支持长时间 AI 响应
- 正确处理连接断开和重连机制

### 3. 性能优化

- 使用防抖机制减少渲染频率
- 通过 Vue 响应式系统批量更新 DOM
- 合理设置 chunk 大小，平衡实时性和性能

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

## 最佳实践

1. **始终使用 JSON 包装**：即使内容简单，也建议用 JSON 包装，统一处理逻辑
2. **保持向后兼容**：前端同时支持 JSON 和纯文本，便于调试和迁移
3. **添加日志监控**：记录原始数据和解析后数据，便于问题排查
4. **单元测试覆盖**：特别测试包含特殊字符的 Markdown 内容

## 经验总结

1. **不要盲目换组件**：先分析问题本质，避免"头痛医头，脚痛医脚"
2. **理解协议规范**：深入了解 SSE、WebSocket 等协议的特性和限制
3. **参考业界实践**：这是 LLM 流式响应的常见问题，已有成熟解决方案
4. **端到端思考**：问题可能出现在任何环节，需要全链路分析

## 参考资料

- [MDN - Using server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events)
- [The line break problem when using Server Sent Events (SSE)](https://medium.com/@thiagosalvatore/the-line-break-problem-when-using-server-sent-events-sse-1159632d09a0)
- [Solving Markdown Newline Issues in LLM Stream Responses](https://yingjiezhao.com/en/articles/Solving-Markdown-Newline-Issues-in-LLM-Stream-Responses/)

## 项目信息

- **技术栈**：Spring Boot + Vue 3 + SSE
- **Markdown 组件**：@kangc/v-md-editor
- **SSE 客户端**：vue-sse
- **解决日期**：2025-08-08