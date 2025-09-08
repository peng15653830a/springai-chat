# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于Spring Boot + Spring AI + Vue3的自然语言下单系统。系统支持通过自然语言指令创建订单，具有智能商品匹配和MCP协议支持。

## 常用命令

### 后端开发
```bash
# 切换到后端目录
cd backend

# 清理并编译
mvn clean compile

# 运行应用（需要设置DeepSeek API密钥）
DeepSeek_API_KEY=your_api_key mvn spring-boot:run

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=NaturalLanguageOrderServiceTest

# 打包应用
mvn clean package
```

### 前端开发
```bash
# 切换到前端目录
cd frontend

# 安装依赖
npm install

# 运行开发服务器（端口8081）
npm run serve

# 构建生产版本
npm run build

# 运行代码检查
npm run lint
```

### E2E测试
```bash
# 切换到e2e测试目录
cd e2e-test

# 安装依赖
npm install

# 运行测试（需要先启动后端和前端服务）
npm test
```

## 系统架构

### 核心服务层
- **NaturalLanguageOrderService**: 自然语言订单处理核心服务，负责解析用户输入并创建订单
- **UnifiedAIService**: 统一的AI服务接口，支持多种AI提供商（Spring AI、DeepSeek、MCP）
- **ProductMatchingService**: 智能商品匹配服务，通过模糊匹配和拼音匹配找到对应商品
- **SpringAIOrderService**: Spring AI集成服务，处理AI模型交互
- **OrderService/ProductService**: 基础业务服务层

### 控制器层
- **NaturalLanguageOrderController**: 处理自然语言下单请求
- **MCPController**: MCP协议接口控制器
- **OrderController/ProductController**: 标准REST API控制器

### MCP协议支持
- **MCPProtocolHandler**: 处理MCP协议请求和响应
- 支持与AI模型进行标准化交互

### 数据模型
- **Order**: 订单实体（用户、商品、数量、总价等）
- **Product**: 商品实体（名称、价格、库存等）
- **User**: 用户实体

## 关键配置

### 数据库
- 使用H2内存数据库（开发环境）
- 访问H2控制台：http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- 用户名: sa，密码: (留空)

### API端点
- 后端：http://localhost:8080
- 前端：http://localhost:8081
- 主要API：
  - POST /api/nl-orders - 自然语言下单
  - GET /api/products - 商品列表
  - GET /api/orders - 订单列表
  - POST /mcp/execute - MCP协议接口

## 开发注意事项

1. **AI服务配置**：需要设置DeepSeek_API_KEY环境变量才能使用AI功能
2. **依赖管理**：项目使用阿里云Maven仓库加速依赖下载
3. **版本要求**：Java 17+, Node.js 16+, Maven 3.8+
4. **测试数据**：DataInitializer类会在启动时初始化测试数据
5. **跨域配置**：后端已配置CORS支持前端开发服务器访问