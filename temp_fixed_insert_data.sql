-- AI模型默认数据初始化
-- 创建日期: 2024-01-01
-- 作者: xupeng

-- 1. 插入默认AI模型提供者
INSERT INTO ai_providers (name, display_name, base_url, api_key_env, enabled, config_json) VALUES
('qwen', '通义千问', 'https://api-inference.modelscope.cn/v1', 'QWEN_API_KEY', true, '{"timeout": 30000, "connectTimeout": 10000}'),
('openai', 'OpenAI', 'https://api.openai.com/v1', 'OPENAI_API_KEY', true, '{"timeout": 30000, "connectTimeout": 10000}'),
('claude', 'Claude', 'https://api.anthropic.com/v1', 'CLAUDE_API_KEY', false, '{"timeout": 30000, "connectTimeout": 10000}')
ON CONFLICT (name) DO NOTHING;

-- 2. 插入通义千问模型配置
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
-- 通义千问模型
((SELECT id FROM ai_providers WHERE name = 'qwen'), 'Qwen/Qwen3-235B-A22B-Thinking-2507', '通义千问-推理版', 2000, 0.7, true, true, true, 1),
((SELECT id FROM ai_providers WHERE name = 'qwen'), 'Qwen/Qwen2.5-72B-Instruct', '通义千问-标准版', 4000, 0.7, false, true, true, 2),
((SELECT id FROM ai_providers WHERE name = 'qwen'), 'Qwen/Qwen2.5-32B-Instruct', '通义千问-轻量版', 3000, 0.7, false, true, true, 3)
ON CONFLICT (provider_id, name) DO NOTHING;

-- 3. 插入OpenAI模型配置
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
-- OpenAI模型
((SELECT id FROM ai_providers WHERE name = 'openai'), 'gpt-4o', 'GPT-4o', 4000, 0.7, false, true, true, 1),
((SELECT id FROM ai_providers WHERE name = 'openai'), 'gpt-4', 'GPT-4', 4000, 0.7, false, true, true, 2),
((SELECT id FROM ai_providers WHERE name = 'openai'), 'gpt-3.5-turbo', 'GPT-3.5 Turbo', 4000, 0.7, false, true, true, 3)
ON CONFLICT (provider_id, name) DO NOTHING;

-- 4. 插入Claude模型配置
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
-- Claude模型
((SELECT id FROM ai_providers WHERE name = 'claude'), 'claude-3-5-sonnet-20241022', 'Claude 3.5 Sonnet', 4000, 0.7, false, true, true, 1),
((SELECT id FROM ai_providers WHERE name = 'claude'), 'claude-3-opus-20240229', 'Claude 3 Opus', 4000, 0.7, false, true, true, 2),
((SELECT id FROM ai_providers WHERE name = 'claude'), 'claude-3-haiku-20240307', 'Claude 3 Haiku', 4000, 0.7, false, true, true, 3)
ON CONFLICT (provider_id, name) DO NOTHING;

-- 5. 为现有conversations和messages表设置默认模型（如果字段为空）
-- 设置默认使用通义千问推理版
DO $$
DECLARE
    default_provider_id BIGINT;
    default_model_id BIGINT;
BEGIN
    -- 获取默认提供者和模型ID
    SELECT id INTO default_provider_id FROM ai_providers WHERE name = 'qwen';
    SELECT id INTO default_model_id FROM ai_models 
    WHERE provider_id = default_provider_id AND name = 'Qwen/Qwen3-235B-A22B-Thinking-2507';
    
    -- 更新conversations表中provider_id和model_id为空的记录
    IF default_provider_id IS NOT NULL AND default_model_id IS NOT NULL THEN
        UPDATE conversations 
        SET provider_id = default_provider_id, model_id = default_model_id 
        WHERE provider_id IS NULL OR model_id IS NULL;
        
        -- 更新messages表中provider_id和model_id为空的记录（只更新assistant角色的消息）
        UPDATE messages 
        SET provider_id = default_provider_id, model_id = default_model_id 
        WHERE (provider_id IS NULL OR model_id IS NULL) AND role = 'assistant';
    END IF;
END $$;

-- 6. 创建查询视图，方便后续开发使用
CREATE OR REPLACE VIEW v_available_models AS
SELECT 
    p.id as provider_id,
    p.name as provider_name,
    p.display_name as provider_display_name,
    p.enabled as provider_enabled,
    m.id as model_id,
    m.name as model_name,
    m.display_name as model_display_name,
    m.max_tokens,
    m.temperature,
    m.supports_thinking,
    m.supports_streaming,
    m.enabled as model_enabled,
    m.sort_order,
    (p.enabled AND m.enabled) as available,
    CONCAT(p.id, '-', m.name) as full_model_id
FROM ai_providers p
JOIN ai_models m ON p.id = m.provider_id
ORDER BY p.display_name, m.sort_order;

-- 7. 创建用户模型偏好查询视图
CREATE OR REPLACE VIEW v_user_model_preferences AS
SELECT 
    ump.id,
    ump.user_id,
    ump.is_default,
    p.id as provider_id,
    p.name as provider_name,
    p.display_name as provider_display_name,
    m.id as model_id,
    m.name as model_name,
    m.display_name as model_display_name,
    m.supports_thinking,
    m.supports_streaming,
    CONCAT(p.id, '-', m.name) as full_model_id,
    ump.created_at,
    ump.updated_at
FROM user_model_preferences ump
JOIN ai_providers p ON ump.provider_id = p.id
JOIN ai_models m ON ump.model_id = m.id
WHERE p.enabled = true AND m.enabled = true;