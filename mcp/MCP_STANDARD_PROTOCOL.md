# 标准MCP协议文档

本系统实现了符合业界标准的Model Context Protocol (MCP)，允许AI模型通过标准化接口与系统进行交互。

## MCP工具列表

### 1. parse_order_instruction - 解析自然语言订单指令

**描述**: 将自然语言指令解析为结构化的产品和数量信息。

**方法名**: `parse_order_instruction`

**参数**:
- `instruction` (string, required): 自然语言订单指令

**返回值**:
```json
{
  "productName": "产品名称",
  "quantity": 1,
  "message": "解析结果信息"
}
```

**示例**:
```json
{
  "name": "parse_order_instruction",
  "arguments": {
    "instruction": "我要买3个苹果手机"
  }
}
```

### 2. get_available_products - 获取所有可用产品

**描述**: 获取系统中所有可用产品的列表。

**方法名**: `get_available_products`

**参数**: 无

**返回值**:
```json
[
  {
    "id": 1,
    "name": "苹果手机",
    "price": 8999.00,
    "stock": 100
  },
  {
    "id": 2,
    "name": "华为手机",
    "price": 7999.00,
    "stock": 50
  }
]
```

**示例**:
```json
{
  "name": "get_available_products"
}
```

### 3. create_order - 创建订单

**描述**: 根据产品名称和数量创建新订单。

**方法名**: `create_order`

**参数**:
- `productName` (string, required): 产品名称
- `quantity` (integer, required): 数量
- `userId` (integer, optional): 用户ID，默认为1

**返回值**:
```json
{
  "orderId": 123,
  "message": "订单创建结果信息"
}
```

**示例**:
```json
{
  "name": "create_order",
  "arguments": {
    "productName": "苹果手机",
    "quantity": 2,
    "userId": 1
  }
}
```

### 4. get_orders - 获取订单列表

**描述**: 获取所有订单或特定用户的订单列表。

**方法名**: `get_orders`

**参数**:
- `userId` (integer, optional): 用户ID，如果不提供则返回所有订单

**返回值**:
```json
[
  {
    "id": 123,
    "productId": 1,
    "quantity": 2,
    "totalPrice": 17998.00,
    "createdAt": "2023-05-01T10:00:00"
  }
]
```

**示例**:
```json
{
  "name": "get_orders",
  "arguments": {
    "userId": 1
  }
}
```

### 5. search_products - 搜索产品

**描述**: 根据关键词搜索产品。

**方法名**: `search_products`

**参数**:
- `keyword` (string, required): 搜索关键词

**返回值**:
```json
[
  {
    "id": 1,
    "name": "苹果手机",
    "price": 8999.00,
    "stock": 100
  }
]
```

**示例**:
```json
{
  "name": "search_products",
  "arguments": {
    "keyword": "苹果"
  }
}
```

## 标准MCP协议端点

系统提供以下标准MCP协议端点：

### 工具发现端点
- **URL**: `/mcp/tools`
- **方法**: GET
- **描述**: 获取可用工具列表

### 工具调用端点
- **URL**: `/mcp/tools/call`
- **方法**: POST
- **描述**: 调用指定工具

## 使用示例

### Python示例

```python
import requests
import json

# 获取工具列表
def get_tools():
    response = requests.get('http://localhost:8080/mcp/tools')
    return response.json()

# 调用工具
def call_tool(tool_name, arguments=None):
    payload = {
        "name": tool_name
    }
    if arguments:
        payload["arguments"] = arguments
    
    response = requests.post(
        'http://localhost:8080/mcp/tools/call',
        headers={'Content-Type': 'application/json'},
        data=json.dumps(payload)
    )
    return response.json()

# 解析自然语言指令
result = call_tool("parse_order_instruction", {
    "instruction": "我要买3个苹果手机"
})
print(result)

# 创建订单
result = call_tool("create_order", {
    "productName": "苹果手机",
    "quantity": 3,
    "userId": 1
})
print(result)
```

### JavaScript示例

```javascript
// 获取工具列表
async function getTools() {
  const response = await fetch('/mcp/tools');
  return await response.json();
}

// 调用工具
async function callTool(toolName, arguments = null) {
  const payload = {
    name: toolName
  };
  if (arguments) {
    payload.arguments = arguments;
  }
  
  const response = await fetch('/mcp/tools/call', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });
  
  return await response.json();
}

// 解析自然语言指令
const parseResult = await callTool("parse_order_instruction", {
  instruction: "我要买3个苹果手机"
});
console.log(parseResult);

// 创建订单
const orderResult = await callTool("create_order", {
  productName: "苹果手机",
  quantity: 3,
  userId: 1
});
console.log(orderResult);
```

## 错误处理

所有错误响应都遵循标准的JSON-RPC 2.0错误格式：

```json
{
  "error": {
    "code": -32602,
    "message": "错误描述信息"
  }
}
```

常见错误代码：
- `-32602`: 参数无效
- `-32002`: 资源未找到
- `-32603`: 内部错误

## 安全考虑

1. 所有工具调用都应进行参数验证
2. 敏感操作应实现访问控制
3. 工具调用应有速率限制
4. 输出数据应进行清理