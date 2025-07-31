# AI智能聊天应用

基于Spring Boot + Vue3构建的AI聊天应用，支持联网搜索和实时流式对话。

## 🚀 项目特性

- **智能对话**: 支持与AI进行自然语言对话
- **联网搜索**: 集成秘塔搜索API，AI能够搜索最新信息并整合到回复中
- **实时流式响应**: 使用SSE技术实现打字机效果
- **多轮对话**: 支持上下文理解的连续对话
- **对话管理**: 创建、删除、切换多个对话会话
- **响应式界面**: 支持PC和移动端访问

## 🛠 技术栈

### 后端
- **JDK**: 1.8
- **Spring Boot**: 2.7.18
- **MyBatis**: 3.5.13 (原生版本)
- **数据库**: H2 (开发) / MySQL (生产)
- **HTTP客户端**: Apache HttpClient
- **实时通信**: Server-Sent Events (SSE)

### 前端
- **Vue 3**: 3.3.8
- **Element Plus**: 2.4.2 (UI组件库)
- **Vue Router**: 4.2.5 (路由管理)
- **Pinia**: 2.1.7 (状态管理)
- **Axios**: 1.5.0 (HTTP客户端)
- **Vite**: 4.5.0 (构建工具)

## 📁 项目结构

```
springai/
├── src/main/java/com/example/
│   ├── controller/          # 控制器层
│   ├── service/            # 服务层
│   ├── entity/             # 实体类
│   ├── mapper/             # MyBatis Mapper
│   ├── dto/                # 数据传输对象
│   └── config/             # 配置类
├── src/main/resources/
│   ├── mapper/             # MyBatis XML映射文件
│   ├── application.yml     # 应用配置
│   ├── schema.sql          # 数据库表结构
│   └── data.sql           # 测试数据
└── frontend/
    ├── src/
    │   ├── components/     # Vue组件
    │   ├── views/          # 页面视图
    │   ├── stores/         # Pinia状态管理
    │   ├── router/         # 路由配置
    │   └── api/            # API接口
    └── package.json        # 前端依赖
```

## 🔧 环境配置

### 1. 后端配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your_openai_api_key_here}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
      model: ${OPENAI_MODEL:gpt-3.5-turbo}

search:
  metaso:
    api-key: ${METASO_API_KEY:your_metaso_api_key_here}
    enabled: ${SEARCH_ENABLED:true}
```

### 2. 环境变量

```bash
export OPENAI_API_KEY="your-openai-api-key"
export METASO_API_KEY="your-metaso-api-key"
export SEARCH_ENABLED="true"
```

## 🚀 启动应用

### 1. 启动后端服务

```bash
# 编译和启动后端
mvn clean compile
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 2. 启动前端服务

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## 📱 使用说明

1. **用户登录**: 访问 `http://localhost:3000`，输入用户名和昵称登录
2. **创建对话**: 点击"新对话"按钮创建聊天会话
3. **发送消息**: 在输入框中输入问题，按回车或点击发送
4. **智能搜索**: 使用秘塔搜索API，包含"最新"、"今天"等关键词时会自动触发联网搜索
5. **切换对话**: 点击左侧对话列表切换不同会话
6. **删除对话**: 悬停对话项，点击删除按钮

## 🔍 API接口

### 用户管理
- `POST /api/users/login` - 用户登录
- `GET /api/users/profile/{userId}` - 获取用户信息

### 对话管理
- `GET /api/conversations?userId={userId}` - 获取对话列表
- `POST /api/conversations?userId={userId}` - 创建新对话
- `GET /api/conversations/{id}` - 获取对话详情
- `DELETE /api/conversations/{id}` - 删除对话

### 消息管理
- `GET /api/conversations/{id}/messages` - 获取对话消息
- `POST /api/chat/conversations/{id}/messages` - 发送消息
- `GET /api/chat/stream/{conversationId}` - SSE流式接收AI回复

## 🎯 核心功能实现

### SSE流式响应
使用Server-Sent Events实现AI回复的实时流式传输：

```javascript
// 前端连接SSE
const eventSource = new EventSource(`/api/chat/stream/${conversationId}`)
eventSource.addEventListener('message', (event) => {
  const data = JSON.parse(event.data)
  // 处理流式消息
})
```

### 秘塔搜索API集成
当消息包含特定关键词时自动触发搜索：

```java
// 后端搜索判断
public boolean shouldSearch(String message) {
    String[] searchKeywords = {"最新", "今天", "现在", "当前", "实时"};
    return Arrays.stream(searchKeywords)
        .anyMatch(keyword -> message.toLowerCase().contains(keyword));
}

// 调用秘塔搜索API
List<Map<String, String>> results = searchService.searchMetaso(userMessage);
```

## 🐛 故障排除

### 常见问题

1. **编译错误**: 确保使用JDK 1.8
2. **数据库连接失败**: 检查H2数据库配置
3. **前端代理失败**: 确认后端服务在8080端口运行
4. **AI API调用失败**: 检查OpenAI API密钥配置

### 日志查看

```bash
# 查看后端日志
tail -f logs/spring.log

# 查看前端控制台
打开浏览器开发者工具查看Network和Console
```

## 📈 性能优化

- 数据库查询优化和索引
- 前端组件懒加载
- SSE连接复用和自动重连
- API响应缓存

## 🔐 安全考虑

- API密钥安全存储
- 用户输入验证和XSS防护
- SQL注入防护
- CORS跨域配置

## 📝 开发计划

- [ ] 支持文件上传和图片识别
- [ ] 添加对话导出功能
- [ ] 实现多语言支持
- [ ] 集成更多AI模型
- [ ] 添加语音输入功能

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

MIT License

---

**注意**: 本项目仅用于学习和演示目的，生产环境使用请确保API密钥安全和服务器配置优化。