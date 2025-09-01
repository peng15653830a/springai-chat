-- 添加Kimi2提供商和模型配置
-- 创建日期: 2025-08-31
-- 作者: xupeng

-- 1. 插入Kimi2提供者
INSERT INTO ai_providers (name, display_name, base_url, api_key_env, enabled, config_json) VALUES
('kimi2', 'Kimi2', 'https://api-inference.modelscope.cn/v1', 'KIMI2_API_KEY', true, '{"timeout": 30000, "connectTimeout": 10000}')
ON CONFLICT (name) DO NOTHING;

-- 2. 插入Kimi2模型配置
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
-- Kimi2模型
((SELECT id FROM ai_providers WHERE name = 'kimi2'), 'moonshotai/Kimi-K2-Instruct', 'kimi2', 4192, 0.7, false, true, true, 1)
ON CONFLICT (provider_id, name) DO NOTHING;

-- 3. 更新OpenAI提供者，添加kimi2模型
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
-- 在OpenAI提供者下添加Kimi2模型
((SELECT id FROM ai_providers WHERE name = 'openai'), 'moonshotai/Kimi-K2-Instruct', 'kimi2', 4192, 0.7, false, true, true, 2)
ON CONFLICT (provider_id, name) DO NOTHING;

-- 4. 更新DeepSeek提供者配置（如果不存在则插入）
INSERT INTO ai_providers (name, display_name, base_url, api_key_env, enabled, config_json) VALUES
('DeepSeek', 'DeepSeek', 'https://api-inference.modelscope.cn/v1', 'DeepSeek_API_KEY', true, '{"timeout": 30000, "connectTimeout": 10000}')
ON CONFLICT (name) DO NOTHING;

-- 5. 插入DeepSeek模型配置
INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, supports_thinking, supports_streaming, enabled, sort_order) VALUES
-- DeepSeek模型
((SELECT id FROM ai_providers WHERE name = 'DeepSeek'), 'deepseek-ai/DeepSeek-V3.1', 'DeepSeek-V3.1', 4192, 0.7, false, true, true, 1)
ON CONFLICT (provider_id, name) DO NOTHING;

-- 6. 更新现有OpenAI模型配置，替换gpt-oss-120b为当前使用的模型
UPDATE ai_models 
SET name = 'openai-mirror/gpt-oss-120b',
    display_name = 'gpt-oss-120b',
    max_tokens = 4192,
    temperature = 0.7,
    supports_thinking = false,
    supports_streaming = true,
    enabled = true,
    sort_order = 1
WHERE provider_id = (SELECT id FROM ai_providers WHERE name = 'openai') 
  AND name IN ('gpt-4o', 'gpt-4', 'gpt-3.5-turbo');

-- 7. 删除不再使用的OpenAI模型
DELETE FROM ai_models 
WHERE provider_id = (SELECT id FROM ai_providers WHERE name = 'openai') 
  AND name IN ('gpt-4', 'gpt-3.5-turbo');

-- 8. 更新视图（重新创建以包含新的提供者）
DROP VIEW IF EXISTS v_available_models;
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

-- 添加注释
COMMENT ON TABLE ai_providers IS 'AI模型提供者配置表，包含Qwen、OpenAI、DeepSeek、Kimi2等提供者信息';
COMMENT ON VIEW v_available_models IS '可用模型视图，展示所有启用的提供者和模型信息，包括新增的Kimi2提供者';