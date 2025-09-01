-- =================================================================
-- Spring AI 项目数据库初始化脚本
-- 作者: xupeng
-- 创建日期: 2024-12-01
-- 说明: 包含建表语句、初始数据和触发器
-- =================================================================

-- =================================================================
-- 1. 基础表结构
-- =================================================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    nickname VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 对话会话表
CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    provider_id BIGINT,
    model_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    thinking TEXT,
    thinking_content TEXT,
    provider_id BIGINT,
    model_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =================================================================
-- 2. AI模型支持表结构
-- =================================================================

-- AI模型提供者表
CREATE TABLE IF NOT EXISTS ai_providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    api_key_env VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT true,
    config_json JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AI模型配置表
CREATE TABLE IF NOT EXISTS ai_models (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    max_tokens INTEGER NOT NULL DEFAULT 2000,
    temperature DECIMAL(3,2) NOT NULL DEFAULT 0.7,
    supports_thinking BOOLEAN NOT NULL DEFAULT false,
    supports_streaming BOOLEAN NOT NULL DEFAULT true,
    enabled BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0,
    config_json JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户模型偏好表
CREATE TABLE IF NOT EXISTS user_model_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    model_id BIGINT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =================================================================
-- 3. 索引
-- =================================================================

-- 用户相关索引
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations (user_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages (conversation_id);

-- AI模型相关索引
CREATE INDEX IF NOT EXISTS idx_ai_providers_name ON ai_providers(name);
CREATE INDEX IF NOT EXISTS idx_ai_providers_enabled ON ai_providers(enabled);
CREATE INDEX IF NOT EXISTS idx_ai_models_provider_id ON ai_models(provider_id);
CREATE INDEX IF NOT EXISTS idx_ai_models_enabled ON ai_models(enabled, sort_order);
CREATE INDEX IF NOT EXISTS idx_user_model_preferences_user_id ON user_model_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_model_preferences_default ON user_model_preferences(user_id, is_default);
CREATE INDEX IF NOT EXISTS idx_conversations_model ON conversations(provider_id, model_id);
CREATE INDEX IF NOT EXISTS idx_messages_model ON messages(provider_id, model_id);
CREATE INDEX IF NOT EXISTS idx_messages_thinking ON messages(id) WHERE thinking_content IS NOT NULL;

-- =================================================================
-- 4. 外键约束
-- =================================================================

-- 基础表外键
ALTER TABLE conversations ADD CONSTRAINT IF NOT EXISTS fk_conversations_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE messages ADD CONSTRAINT IF NOT EXISTS fk_messages_conversation 
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;

-- AI模型相关外键
ALTER TABLE ai_models ADD CONSTRAINT IF NOT EXISTS fk_ai_models_provider 
    FOREIGN KEY (provider_id) REFERENCES ai_providers(id) ON DELETE CASCADE;
ALTER TABLE user_model_preferences ADD CONSTRAINT IF NOT EXISTS fk_user_model_preferences_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_model_preferences ADD CONSTRAINT IF NOT EXISTS fk_user_model_preferences_provider 
    FOREIGN KEY (provider_id) REFERENCES ai_providers(id) ON DELETE CASCADE;
ALTER TABLE user_model_preferences ADD CONSTRAINT IF NOT EXISTS fk_user_model_preferences_model 
    FOREIGN KEY (model_id) REFERENCES ai_models(id) ON DELETE CASCADE;
ALTER TABLE conversations ADD CONSTRAINT IF NOT EXISTS fk_conversations_provider 
    FOREIGN KEY (provider_id) REFERENCES ai_providers(id);
ALTER TABLE conversations ADD CONSTRAINT IF NOT EXISTS fk_conversations_model 
    FOREIGN KEY (model_id) REFERENCES ai_models(id);
ALTER TABLE messages ADD CONSTRAINT IF NOT EXISTS fk_messages_provider 
    FOREIGN KEY (provider_id) REFERENCES ai_providers(id);
ALTER TABLE messages ADD CONSTRAINT IF NOT EXISTS fk_messages_model 
    FOREIGN KEY (model_id) REFERENCES ai_models(id);

-- =================================================================
-- 5. 唯一约束
-- =================================================================

ALTER TABLE ai_models ADD CONSTRAINT IF NOT EXISTS uk_ai_models_provider_name 
    UNIQUE (provider_id, name);
ALTER TABLE user_model_preferences ADD CONSTRAINT IF NOT EXISTS uk_user_model_preferences 
    UNIQUE (user_id, provider_id, model_id);

-- =================================================================
-- 6. 触发器函数
-- =================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 触发器
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_conversations_updated_at ON conversations;
CREATE TRIGGER update_conversations_updated_at 
    BEFORE UPDATE ON conversations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_messages_updated_at ON messages;
CREATE TRIGGER update_messages_updated_at 
    BEFORE UPDATE ON messages 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_ai_providers_updated_at ON ai_providers;
CREATE TRIGGER update_ai_providers_updated_at 
    BEFORE UPDATE ON ai_providers 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_ai_models_updated_at ON ai_models;
CREATE TRIGGER update_ai_models_updated_at 
    BEFORE UPDATE ON ai_models 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_user_model_preferences_updated_at ON user_model_preferences;
CREATE TRIGGER update_user_model_preferences_updated_at 
    BEFORE UPDATE ON user_model_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =================================================================
-- 7. 初始数据 - AI提供者
-- =================================================================

-- 清空现有数据（仅开发环境）
-- DELETE FROM user_model_preferences;
-- DELETE FROM ai_models;
-- DELETE FROM ai_providers;

-- 插入AI提供者
INSERT INTO ai_providers (name, display_name, base_url, api_key_env, enabled, config_json) VALUES
('qwen', '通义千问', 'https://dashscope.aliyuncs.com/api/v1', 'QWEN_API_KEY', true, '{"maxRetries": 3, "timeout": 30000}'),
('openai', 'OpenAI', 'https://api.openai.com/v1', 'OPENAI_API_KEY', true, '{"maxRetries": 3, "timeout": 30000}'),
('deepseek', 'DeepSeek', 'https://api.deepseek.com', 'DEEPSEEK_API_KEY', true, '{"maxRetries": 3, "timeout": 30000}'),
('kimi2', 'Kimi2', 'https://api.moonshot.cn/v1', 'KIMI2_API_KEY', true, '{"maxRetries": 3, "timeout": 30000}')
ON CONFLICT (name) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    base_url = EXCLUDED.base_url,
    api_key_env = EXCLUDED.api_key_env,
    enabled = EXCLUDED.enabled,
    config_json = EXCLUDED.config_json,
    updated_at = CURRENT_TIMESTAMP;

-- =================================================================
-- 8. 初始数据 - AI模型
-- =================================================================

-- 通义千问模型
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
((SELECT id FROM ai_providers WHERE name = 'qwen'), 'qwen-plus', '通义千问Plus', 8192, 0.7, false, true, true, 1),
((SELECT id FROM ai_providers WHERE name = 'qwen'), 'qwen-max', '通义千问Max', 8192, 0.7, false, true, true, 2),
((SELECT id FROM ai_providers WHERE name = 'qwen'), 'qwen-turbo', '通义千问Turbo', 8192, 0.7, false, true, true, 3)
ON CONFLICT (provider_id, name) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    max_tokens = EXCLUDED.max_tokens,
    temperature = EXCLUDED.temperature,
    supports_thinking = EXCLUDED.supports_thinking,
    supports_streaming = EXCLUDED.supports_streaming,
    enabled = EXCLUDED.enabled,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;

-- OpenAI模型
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
((SELECT id FROM ai_providers WHERE name = 'openai'), 'gpt-4', 'GPT-4', 8192, 0.7, false, true, true, 11),
((SELECT id FROM ai_providers WHERE name = 'openai'), 'gpt-4-turbo', 'GPT-4 Turbo', 128000, 0.7, false, true, true, 12),
((SELECT id FROM ai_providers WHERE name = 'openai'), 'gpt-3.5-turbo', 'GPT-3.5 Turbo', 16385, 0.7, false, true, true, 13)
ON CONFLICT (provider_id, name) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    max_tokens = EXCLUDED.max_tokens,
    temperature = EXCLUDED.temperature,
    supports_thinking = EXCLUDED.supports_thinking,
    supports_streaming = EXCLUDED.supports_streaming,
    enabled = EXCLUDED.enabled,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;

-- DeepSeek模型
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
((SELECT id FROM ai_providers WHERE name = 'deepseek'), 'deepseek-chat', 'DeepSeek Chat', 32768, 0.7, false, true, true, 21),
((SELECT id FROM ai_providers WHERE name = 'deepseek'), 'deepseek-reasoner', 'DeepSeek Reasoner', 64000, 0.7, true, true, true, 22)
ON CONFLICT (provider_id, name) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    max_tokens = EXCLUDED.max_tokens,
    temperature = EXCLUDED.temperature,
    supports_thinking = EXCLUDED.supports_thinking,
    supports_streaming = EXCLUDED.supports_streaming,
    enabled = EXCLUDED.enabled,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;

-- Kimi2模型
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
((SELECT id FROM ai_providers WHERE name = 'kimi2'), 'moonshot-v1-8k', 'Kimi2 8K', 8192, 0.7, false, true, true, 31),
((SELECT id FROM ai_providers WHERE name = 'kimi2'), 'moonshot-v1-32k', 'Kimi2 32K', 32768, 0.7, false, true, true, 32),
((SELECT id FROM ai_providers WHERE name = 'kimi2'), 'moonshot-v1-128k', 'Kimi2 128K', 131072, 0.7, false, true, true, 33)
ON CONFLICT (provider_id, name) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    max_tokens = EXCLUDED.max_tokens,
    temperature = EXCLUDED.temperature,
    supports_thinking = EXCLUDED.supports_thinking,
    supports_streaming = EXCLUDED.supports_streaming,
    enabled = EXCLUDED.enabled,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;

-- =================================================================
-- 9. 初始数据 - 测试用户（仅开发环境）
-- =================================================================

-- 插入测试用户
INSERT INTO users (username, nickname) VALUES
('admin', '管理员'),
('test', '测试用户'),
('demo', '演示用户')
ON CONFLICT (username) DO UPDATE SET
    nickname = EXCLUDED.nickname,
    updated_at = CURRENT_TIMESTAMP;

-- =================================================================
-- 10. 初始数据 - 用户模型偏好
-- =================================================================

-- 为测试用户设置默认模型偏好
INSERT INTO user_model_preferences (user_id, provider_id, model_id, is_default)
SELECT 
    u.id,
    p.id,
    m.id,
    true
FROM users u
CROSS JOIN ai_providers p
INNER JOIN ai_models m ON m.provider_id = p.id AND m.sort_order = 1
WHERE u.username = 'admin' AND p.name = 'qwen'
ON CONFLICT (user_id, provider_id, model_id) DO NOTHING;

-- =================================================================
-- 结束
-- =================================================================