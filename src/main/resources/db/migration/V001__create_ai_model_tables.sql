-- AI多模型支持数据库结构
-- 创建日期: 2024-01-01
-- 作者: xupeng

-- 1. 创建AI模型提供者表
CREATE TABLE IF NOT EXISTS ai_providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '提供者名称（唯一标识）',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    base_url VARCHAR(500) NOT NULL COMMENT 'API基础URL',
    api_key_env VARCHAR(100) COMMENT 'API密钥环境变量名',
    enabled BOOLEAN NOT NULL DEFAULT true COMMENT '是否启用',
    config_json JSONB COMMENT '提供者配置JSON',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 为ai_providers表添加索引
CREATE INDEX IF NOT EXISTS idx_ai_providers_name ON ai_providers(name);
CREATE INDEX IF NOT EXISTS idx_ai_providers_enabled ON ai_providers(enabled);

-- 2. 创建AI模型配置表
CREATE TABLE IF NOT EXISTS ai_models (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL COMMENT '提供者ID',
    name VARCHAR(200) NOT NULL COMMENT '模型名称（API调用时使用）',
    display_name VARCHAR(200) NOT NULL COMMENT '显示名称',
    max_tokens INTEGER NOT NULL DEFAULT 2000 COMMENT '最大token数',
    temperature DECIMAL(3,2) NOT NULL DEFAULT 0.7 COMMENT '温度参数',
    supports_thinking BOOLEAN NOT NULL DEFAULT false COMMENT '是否支持推理模式',
    supports_streaming BOOLEAN NOT NULL DEFAULT true COMMENT '是否支持流式输出',
    enabled BOOLEAN NOT NULL DEFAULT true COMMENT '是否启用',
    sort_order INTEGER NOT NULL DEFAULT 0 COMMENT '排序顺序',
    config_json JSONB COMMENT '模型特有配置JSON',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_ai_models_provider FOREIGN KEY (provider_id) REFERENCES ai_providers(id) ON DELETE CASCADE,
    CONSTRAINT uk_ai_models_provider_name UNIQUE (provider_id, name)
);

-- 为ai_models表添加索引
CREATE INDEX IF NOT EXISTS idx_ai_models_provider_id ON ai_models(provider_id);
CREATE INDEX IF NOT EXISTS idx_ai_models_enabled ON ai_models(enabled, sort_order);

-- 3. 创建用户模型偏好表
CREATE TABLE IF NOT EXISTS user_model_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    provider_id BIGINT NOT NULL COMMENT '提供者ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    is_default BOOLEAN NOT NULL DEFAULT false COMMENT '是否为默认选择',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_user_model_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_model_preferences_provider FOREIGN KEY (provider_id) REFERENCES ai_providers(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_model_preferences_model FOREIGN KEY (model_id) REFERENCES ai_models(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_model_preferences UNIQUE (user_id, provider_id, model_id)
);

-- 为user_model_preferences表添加索引
CREATE INDEX IF NOT EXISTS idx_user_model_preferences_user_id ON user_model_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_model_preferences_default ON user_model_preferences(user_id, is_default);

-- 4. 扩展现有conversations表，添加模型信息字段
DO $$
BEGIN
    -- 检查列是否存在，如果不存在则添加
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'conversations' AND column_name = 'provider_id') THEN
        ALTER TABLE conversations ADD COLUMN provider_id BIGINT COMMENT '使用的提供者ID';
        ALTER TABLE conversations ADD CONSTRAINT fk_conversations_provider 
            FOREIGN KEY (provider_id) REFERENCES ai_providers(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'conversations' AND column_name = 'model_id') THEN
        ALTER TABLE conversations ADD COLUMN model_id BIGINT COMMENT '使用的模型ID';
        ALTER TABLE conversations ADD CONSTRAINT fk_conversations_model 
            FOREIGN KEY (model_id) REFERENCES ai_models(id);
    END IF;
END $$;

-- 为conversations表新增字段添加索引
CREATE INDEX IF NOT EXISTS idx_conversations_model ON conversations(provider_id, model_id);

-- 5. 扩展现有messages表，添加模型信息字段
DO $$
BEGIN
    -- 检查列是否存在，如果不存在则添加
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'messages' AND column_name = 'provider_id') THEN
        ALTER TABLE messages ADD COLUMN provider_id BIGINT COMMENT '使用的提供者ID';
        ALTER TABLE messages ADD CONSTRAINT fk_messages_provider 
            FOREIGN KEY (provider_id) REFERENCES ai_providers(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'messages' AND column_name = 'model_id') THEN
        ALTER TABLE messages ADD COLUMN model_id BIGINT COMMENT '使用的模型ID';
        ALTER TABLE messages ADD CONSTRAINT fk_messages_model 
            FOREIGN KEY (model_id) REFERENCES ai_models(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'messages' AND column_name = 'thinking_content') THEN
        ALTER TABLE messages ADD COLUMN thinking_content TEXT COMMENT '推理过程内容';
    END IF;
END $$;

-- 为messages表新增字段添加索引
CREATE INDEX IF NOT EXISTS idx_messages_model ON messages(provider_id, model_id);
CREATE INDEX IF NOT EXISTS idx_messages_thinking ON messages(id) WHERE thinking_content IS NOT NULL;

-- 6. 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为相关表创建更新时间触发器
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