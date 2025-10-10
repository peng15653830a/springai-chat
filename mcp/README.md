# 自然语言下单系统

这是一个基于Spring Boot、Spring AI、Vue3和Element UI的自然语言下单系统。用户可以通过自然语言指令下单，系统会自动解析指令并创建订单。

## 系统架构

- 后端：Spring Boot + Spring AI + JPA
- 前端：Vue3 + Element UI
- 数据库：H2内存数据库（开发环境）

## 功能特性

1. 自然语言下单：用户可以通过自然语言指令下单
2. 商品管理：查看商品列表
3. 订单管理：查看订单列表
4. MCP协议支持：与AI模型进行交互

## 环境要求

- Java 17+
- Node.js 16+
- Maven 3.8+

## 配置说明

本项目已配置使用阿里云Maven仓库，可显著提高依赖下载速度。同时使用了本地已验证的Spring Boot 3.4.2和Spring AI 1.0.0-M6版本。

## 后端运行步骤

1. 进入后端目录：
   ```
   cd backend
   ```

2. 编译项目：
   ```
   mvn clean compile
   ```

3. 运行应用：
   ```
   mvn spring-boot:run
   ```

   或者设置DeepSeek API密钥后运行：
   ```
   DeepSeek_API_KEY=your_api_key mvn spring-boot:run
   ```

## 前端运行步骤

1. 进入前端目录：
   ```
   cd frontend
   ```

2. 安装依赖：
   ```
   npm install
   ```

3. 运行开发服务器：
   ```
   npm run serve
   ```

## 使用说明

1. 启动后端和前端服务
2. 在浏览器中访问 http://localhost:8081
3. 在"自然语言下单"标签页中输入指令，例如："我要买3个苹果手机"
4. 系统会自动解析指令并创建订单

## API接口

- GET /api/products - 获取商品列表
- GET /api/products/{id} - 获取商品详情
- GET /api/orders - 获取订单列表
- GET /api/orders/{id} - 获取订单详情
- POST /api/nl-orders - 自然语言下单
- POST /mcp/execute - MCP协议接口

## 数据库

系统使用H2内存数据库，数据在应用重启后会丢失。可以通过以下URL访问H2控制台：
http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb
用户名: sa
密码: (留空)