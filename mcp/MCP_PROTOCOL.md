# MCP协议文档

MCP (Model Control Protocol) 是一个用于与AI模型进行标准化交互的协议。本系统实现了MCP协议，允许通过标准化接口与AI模型进行交互。

## 协议方法

### 1. parse_order - 解析自然语言订单指令

**描述**: 将自然语言指令解析为结构化的产品和数量信息。

**请求参数**:
```json
{
  "method": "parse_order",
  "params": {
    "instruction": "我要买3个苹果手机"
  }
}
```

**响应示例**:
```json
{
  "status": "success",
  "result": {
    "product": "苹果手机",
    "quantity": 3
  }
}
```

### 2. get_products - 获取所有产品列表

**描述**: 获取系统中所有可用产品的列表。

**请求参数**:
```json
{
  "method": "get_products"
}
```

**响应示例**:
```json
{
  "status": "success",
  "result": [
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
}
```

### 3. create_order - 创建订单

**描述**: 根据产品名称和数量创建新订单。

**请求参数**:
```json
{
  "method": "create_order",
  "params": {
    "product": "苹果手机",
    "quantity": 2,
    "user_id": 1
  }
}
```

**响应示例**:
```json
{
  "status": "success",
  "result": {
    "order_id": 123,
    "product": "苹果手机",
    "quantity": 2,
    "total_price": 17998.00
  }
}
```

### 4. get_orders - 获取订单列表

**描述**: 获取所有订单或特定用户的订单列表。

**请求参数**:
```json
{
  "method": "get_orders",
  "params": {
    "user_id": 1  // 可选，不提供则返回所有订单
  }
}
```

**响应示例**:
```json
{
  "status": "success",
  "result": [
    {
      "id": 123,
      "product_id": 1,
      "quantity": 2,
      "total_price": 17998.00,
      "created_at": "2023-05-01T10:00:00"
    }
  ]
}
```

### 5. search_products - 搜索产品

**描述**: 根据关键词搜索产品。

**请求参数**:
```json
{
  "method": "search_products",
  "params": {
    "keyword": "苹果"
  }
}
```

**响应示例**:
```json
{
  "status": "success",
  "result": [
    {
      "id": 1,
      "name": "苹果手机",
      "price": 8999.00,
      "stock": 100
    }
  ]
}
```

## 错误响应格式

所有错误响应都遵循以下格式：

```json
{
  "status": "error",
  "error": "错误描述信息"
}
```

## 使用示例

### JavaScript示例

```javascript
// 解析自然语言指令
const parseOrder = async (instruction) => {
  const response = await fetch('/mcp/execute', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      method: 'parse_order',
      params: {
        instruction: instruction
      }
    })
  });
  
  return await response.json();
};

// 创建订单
const createOrder = async (product, quantity) => {
  const response = await fetch('/mcp/execute', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      method: 'create_order',
      params: {
        product: product,
        quantity: quantity,
        user_id: 1
      }
    })
  });
  
  return await response.json();
};
```

## 注意事项

1. 所有请求都需要通过POST方法发送到`/mcp/execute`端点
2. 请求体必须是有效的JSON格式
3. 每个请求必须包含method字段
4. params字段是可选的，根据方法需求提供
5. 响应总是包含status字段，值为"success"或"error"