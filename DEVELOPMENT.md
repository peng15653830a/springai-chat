# 开发指南

## 🛠 开发环境要求

### 基础环境
- **JDK**: 1.8+
- **Maven**: 3.6+
- **Node.js**: 16+
- **npm**: 8+

### 推荐IDE
- **后端**: IntelliJ IDEA / Eclipse
- **前端**: VS Code / WebStorm

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd springai
```

### 2. 配置环境变量
```bash
cp .env.example .env
# 编辑 .env 文件，填入真实的API密钥
```

### 3. 一键启动
```bash
./start.sh
```

### 4. 访问应用
- 前端: http://localhost:3000
- 后端: http://localhost:8080
- H2数据库: http://localhost:8080/h2-console

## 📝 开发规范

### 后端开发规范

#### 目录结构
```
src/main/java/com/example/
├── controller/     # 控制器层 - 处理HTTP请求
├── service/        # 服务层 - 业务逻辑
├── mapper/         # 数据访问层 - MyBatis映射器
├── entity/         # 实体类 - 数据模型
├── dto/           # 数据传输对象
├── config/        # 配置类
└── springai/      # 主应用类
```

#### 编码规范
- 使用Lombok减少样板代码
- 统一使用`@RestController`和`@RequestMapping`
- 异常处理使用`ApiResponse<T>`统一返回格式
- 数据库操作使用事务注解`@Transactional`

#### API设计规范
```java
@RestController
@RequestMapping("/api/example")
@CrossOrigin(origins = "*")
public class ExampleController {
    
    @PostMapping
    public ApiResponse<Entity> create(@RequestBody CreateRequest request) {
        try {
            Entity entity = service.create(request);
            return ApiResponse.success("创建成功", entity);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }
}
```

### 前端开发规范

#### 目录结构
```
src/
├── components/     # 可复用组件
├── views/         # 页面组件
├── stores/        # Pinia状态管理
├── router/        # 路由配置
├── api/          # API接口
└── assets/       # 静态资源
```

#### 组件规范
- 使用Composition API
- 组件名使用PascalCase
- 文件名使用PascalCase
- 使用Element Plus组件库

#### API调用规范
```javascript
// api/index.js
export const exampleApi = {
  create: (data) => api.post('/example', data),
  getList: (params) => api.get('/example', { params }),
  update: (id, data) => api.put(`/example/${id}`, data),
  delete: (id) => api.delete(`/example/${id}`)
}
```

## 🔧 开发工具配置

### IntelliJ IDEA配置
1. 安装Lombok插件
2. 启用注解处理: `Preferences > Build > Compiler > Annotation Processors`
3. 配置代码格式: `Preferences > Editor > Code Style > Java`

### VS Code配置
推荐插件:
- Vetur / Volar (Vue支持)
- ESLint (代码检查)
- Prettier (代码格式化)
- Auto Rename Tag (标签重命名)

## 🐛 调试指南

### 后端调试
1. 在IDE中设置断点
2. 以Debug模式启动Spring Boot应用
3. 查看控制台日志和H2数据库

### 前端调试
1. 使用浏览器开发者工具
2. 查看Network面板检查API调用
3. 使用Vue DevTools扩展

### 日志查看
```bash
# 后端日志
tail -f backend.log

# 前端日志
tail -f frontend.log

# 实时查看应用日志
./start.sh  # 会显示实时日志
```

## 🧪 测试指南

### 后端测试
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn integration-test

# 生成覆盖率报告
mvn jacoco:report
```

### 前端测试
```bash
cd frontend

# 运行单元测试
npm run test

# 运行E2E测试
npm run test:e2e
```

### API测试
使用Postman或curl测试API接口:

```bash
# 用户登录
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","nickname":"测试用户"}'

# 创建对话
curl -X POST "http://localhost:8080/api/conversations?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"title":"测试对话"}'
```

## 📦 构建和部署

### 本地构建
```bash
# 构建后端
mvn clean package

# 构建前端
cd frontend
npm run build
```

### Docker部署
```bash
# 构建Docker镜像
docker build -t ai-chat .

# 运行容器
docker run -p 8080:8080 -p 3000:3000 ai-chat
```

### 生产环境配置
1. 修改`application-prod.yml`
2. 配置真实的数据库连接
3. 设置环境变量
4. 启用HTTPS

## 🔍 性能优化

### 后端优化
- 数据库连接池配置
- SQL查询优化
- 缓存策略
- 异步处理

### 前端优化
- 代码分割和懒加载
- 图片压缩和CDN
- 打包优化
- 缓存策略

## 🛡 安全考虑

### 后端安全
- API密钥管理
- 输入验证
- SQL注入防护
- CORS配置

### 前端安全
- XSS防护
- CSRF防护
- 敏感信息保护
- HTTPS使用

## 📈 监控和日志

### 应用监控
- Spring Boot Actuator
- 应用性能监控
- 错误跟踪

### 日志管理
- 日志级别配置
- 日志轮转
- 集中日志收集

## 🤝 贡献流程

1. Fork项目到个人账号
2. 创建特性分支: `git checkout -b feature/amazing-feature`
3. 提交更改: `git commit -m 'Add amazing feature'`
4. 推送分支: `git push origin feature/amazing-feature`
5. 创建Pull Request

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建工具或辅助工具的变动
```

## 📚 相关资源

### 技术文档
- [Spring Boot文档](https://spring.io/projects/spring-boot)
- [MyBatis文档](https://mybatis.org/mybatis-3/)
- [Vue 3文档](https://vuejs.org/)
- [Element Plus文档](https://element-plus.org/)

### 开发工具
- [Postman](https://www.postman.com/) - API测试
- [DBeaver](https://dbeaver.io/) - 数据库管理
- [Vue DevTools](https://devtools.vuejs.org/) - Vue调试

## ❓ 常见问题

### Q: 后端启动失败，提示端口被占用
A: 使用`lsof -i :8080`查看端口占用，或修改`application.yml`中的端口配置

### Q: 前端代理请求失败
A: 确认后端服务已启动，检查`vite.config.js`中的代理配置

### Q: AI API调用失败
A: 检查`.env`文件中的API密钥配置，确认网络连接正常

### Q: 数据库连接失败
A: 检查H2数据库配置，或参考文档配置MySQL连接

---

如有其他问题，请查看项目Issue或联系开发团队。