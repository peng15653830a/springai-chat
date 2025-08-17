# API配置指南

## 月之暗面 Kimi API 配置

本项目使用月之暗面的 Kimi API 作为主要的 AI 对话服务。

### 1. 获取 API 密钥

1. 访问 [月之暗面开放平台](https://platform.moonshot.cn/)
2. 注册账号并完成实名认证
3. 在控制台中创建新的 API 密钥
4. 记录下 API Key，格式类似：`sk-xxx...`

### 2. 配置 API 密钥

#### 方式一：环境变量（推荐）

```bash
# Linux/macOS
export AI_API_KEY="sk-your-kimi-api-key"
export AI_API_URL="https://api.moonshot.cn/v1/chat/completions"

# Windows
set AI_API_KEY=sk-your-kimi-api-key
set AI_API_URL=https://api.moonshot.cn/v1/chat/completions
```

#### 方式二：配置文件

在 `application.yml` 中配置：

```yaml
ai:
  chat:
    api-key: sk-your-kimi-api-key
    api-url: https://api.moonshot.cn/v1/chat/completions
    model: kimi-k2-0711-preview
```

### 3. 支持的模型

| 模型名称 | 描述 | 上下文长度 |
|---------|------|-----------|
| kimi-k2-0711-preview | 最新预览版本 | 200K tokens |
| moonshot-v1-8k | 标准版本 | 8K tokens |
| moonshot-v1-32k | 长文本版本 | 32K tokens |
| moonshot-v1-128k | 超长文本版本 | 128K tokens |

### 4. API 限制

- **请求频率限制**：根据账户等级不同
- **并发连接数**：建议不超过 10 个
- **单次请求长度**：最大 200K tokens
- **流式响应**：支持 SSE 格式

## 秘塔搜索 API 配置

本项目集成秘塔搜索API来提供联网搜索功能，特别针对中文搜索进行了优化。

### 1. 获取秘塔 API 密钥

1. 访问 [秘塔搜索API官网](https://metaso.cn/)
2. 注册开发者账号
3. 在开发者控制台申请 API 密钥
4. 记录下 API Key

### 2. 配置搜索API

#### 环境变量配置

```bash
# Linux/macOS
export METASO_API_KEY="your-metaso-api-key"

# Windows  
set METASO_API_KEY=your-metaso-api-key
```

#### 配置文件

```yaml
search:
  metaso:
    api-key: ${METASO_API_KEY}
    api-url: https://metaso-api.com/search
    timeout: 10000
    max-results: 6
```

### 3. 搜索功能特性

- **中文优化**：针对中文搜索结果进行优化
- **实时搜索**：支持最新信息检索
- **结果过滤**：自动过滤低质量内容
- **摘要生成**：AI自动生成搜索结果摘要

### 4. API 使用限制

- **每日调用次数**：根据套餐不同
- **并发请求数**：建议控制在 5 个以内
- **响应时间**：通常在 2-5 秒内

## MySQL MCP 配置

本项目支持通过 Model Context Protocol (MCP) 连接MySQL数据库，用于高级数据查询和分析。

### 1. 安装 MCP 服务器

```bash
# 使用 Python 包管理器安装
pip install mysql-mcp-server

# 或使用 uv（推荐）
uv add mysql-mcp-server
```

### 2. 配置 MCP 服务器

在 `.claude/settings.local.json` 中配置：

```json
{
  "mcpServers": {
    "mysql": {
      "command": "python",
      "args": ["-m", "mysql_mcp_server"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "ai_chat_user",
        "MYSQL_PASSWORD": "your_password",
        "MYSQL_DATABASE": "ai_chat"
      }
    }
  }
}
```

### 3. 启动 MCP 服务器

```bash
# 直接启动
uv run mysql_mcp_server

# 或通过 Python
python -m mysql_mcp_server
```

### 4. 验证连接

MCP 服务器启动后，可以通过 Claude Code 使用以下功能：
- 查询数据库表结构
- 执行数据查询
- 分析用户行为数据
- 生成数据报告

## 配置验证

### 1. 检查 AI API 连接

```bash
# 测试 API 连接
curl -X POST "https://api.moonshot.cn/v1/chat/completions" \
  -H "Authorization: Bearer sk-your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "kimi-k2-0711-preview",
    "messages": [{"role": "user", "content": "Hello"}],
    "stream": false
  }'
```

### 2. 检查搜索 API 连接

```bash
# 测试搜索 API（具体格式依据秘塔API文档）
curl -X POST "https://metaso-api.com/search" \
  -H "Authorization: Bearer your-metaso-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "春节"
  }'
```

### 3. 应用内验证

启动应用后，检查以下功能：

1. **AI 对话功能**
   - 发送测试消息
   - 检查是否有AI回复
   - 验证流式响应是否正常

2. **搜索功能**  
   - 开启搜索开关
   - 发送需要搜索的问题
   - 检查是否返回搜索结果

3. **数据库连接**
   - 检查应用启动日志
   - 验证数据表是否创建成功
   - 测试消息存储功能

## 安全最佳实践

### 1. API 密钥管理

- ✅ **使用环境变量**：绝不在代码中硬编码 API 密钥
- ✅ **定期轮换**：定期更换 API 密钥
- ✅ **权限控制**：使用最小权限原则
- ❌ **避免提交**：确保 API 密钥不被提交到版本控制

### 2. 网络安全

```yaml
# 配置 HTTPS
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### 3. 请求频率控制

```java
// 配置请求限制
@Component
public class RateLimitingFilter {
    // 实现请求频率限制逻辑
}
```

## 故障排除

### 常见问题

1. **API 密钥无效**
   ```
   HTTP 401: Unauthorized
   ```
   - 检查 API 密钥是否正确
   - 验证密钥是否已过期
   - 确认环境变量是否正确设置

2. **网络连接超时**
   ```
   java.net.SocketTimeoutException: Read timed out
   ```
   - 检查网络连接
   - 调整超时时间配置
   - 验证 API 服务是否可用

3. **搜索功能不可用**
   ```
   SearchServiceException: Search API not responding
   ```
   - 检查秘塔 API 密钥
   - 验证搜索 API 配额
   - 检查网络防火墙设置

4. **MCP 连接失败**
   ```
   MCP server connection failed
   ```
   - 检查 MySQL 服务是否启动
   - 验证数据库连接参数
   - 确认 MCP 服务器安装正确

### 调试方法

1. **启用详细日志**
   ```yaml
   logging:
     level:
       com.example.service: DEBUG
       org.springframework.web: DEBUG
   ```

2. **健康检查端点**
   ```
   GET /actuator/health
   ```

3. **API 测试工具**
   - 使用 Postman 或 curl 测试 API 连接
   - 检查请求和响应格式
   - 验证认证头是否正确