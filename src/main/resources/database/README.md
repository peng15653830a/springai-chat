# 数据库脚本说明

## init.sql
整合的数据库初始化脚本，包含：

1. **表结构创建**
   - 用户表（users）
   - 对话表（conversations）  
   - 消息表（messages）
   - AI提供者表（ai_providers）
   - AI模型表（ai_models）
   - 用户模型偏好表（user_model_preferences）

2. **索引创建**
   - 性能优化相关索引

3. **外键约束**
   - 数据完整性保障

4. **触发器**
   - 自动更新updated_at字段

5. **初始数据**
   - AI提供者配置（通义千问、OpenAI、DeepSeek、Kimi2）
   - 各提供者的模型配置
   - 测试用户数据

## 使用方式

### PostgreSQL
```bash
psql -U username -d database_name -f init.sql
```

### 应用启动时自动执行
在 application.yml 中配置：
```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:database/init.sql
```

## 注意事项

- 脚本使用了 `IF NOT EXISTS` 和 `ON CONFLICT` 确保重复执行安全
- 测试用户数据仅用于开发环境
- 生产环境建议删除测试数据相关部分