package com.example.mapper;

import com.example.entity.AiModel;
import com.example.dto.common.ModelInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI模型数据访问接口
 * 
 * @author xupeng
 */
@Mapper
public interface AiModelMapper {

    /**
     * 根据提供者ID查询启用的模型列表
     * 
     * @param providerId 提供者ID
     * @return 模型列表
     */
    @Select("SELECT * FROM ai_models WHERE provider_id = #{providerId} AND enabled = true ORDER BY sort_order, display_name")
    List<AiModel> findEnabledModelsByProvider(@Param("providerId") Long providerId);

    /**
     * 查询所有启用的模型
     * 
     * @return 模型列表
     */
    @Select("SELECT * FROM ai_models WHERE enabled = true ORDER BY provider_id, sort_order")
    List<AiModel> findAllEnabledModels();

    /**
     * 根据提供者ID和模型名称查询模型
     * 
     * @param providerId 提供者ID
     * @param name 模型名称
     * @return 模型信息
     */
    @Select("SELECT * FROM ai_models WHERE provider_id = #{providerId} AND name = #{name}")
    AiModel findByProviderAndName(@Param("providerId") Long providerId, @Param("name") String name);

    /**
     * 根据ID查询模型
     * 
     * @param id 模型ID
     * @return 模型信息
     */
    @Select("SELECT * FROM ai_models WHERE id = #{id}")
    AiModel findById(@Param("id") Long id);

    /**
     * 查询可用模型信息（包含提供者信息）
     * 
     * @return 可用模型列表
     */
    @Select("""
        SELECT 
            m.id,
            m.name,
            m.display_name,
            m.max_tokens,
            m.temperature,
            m.supports_thinking,
            m.supports_streaming,
            m.sort_order,
            (p.enabled AND m.enabled) as available
        FROM ai_models m
        JOIN ai_providers p ON m.provider_id = p.id
        WHERE p.enabled = true AND m.enabled = true
        ORDER BY p.display_name, m.sort_order
    """)
    List<ModelInfo> findAvailableModelInfo();

    /**
     * 根据提供者名称查询可用模型信息
     * 
     * @param providerName 提供者名称
     * @return 模型信息列表
     */
    @Select("""
        SELECT 
            m.id,
            m.name,
            m.display_name,
            m.max_tokens,
            m.temperature,
            m.supports_thinking,
            m.supports_streaming,
            m.sort_order,
            (p.enabled AND m.enabled) as available
        FROM ai_models m
        JOIN ai_providers p ON m.provider_id = p.id
        WHERE p.name = #{providerName} AND p.enabled = true AND m.enabled = true
        ORDER BY m.sort_order
    """)
    List<ModelInfo> findAvailableModelInfoByProvider(@Param("providerName") String providerName);

    /**
     * 插入新模型
     * 
     * @param model 模型信息
     * @return 影响行数
     */
    @Insert("""
        INSERT INTO ai_models (provider_id, name, display_name, max_tokens, temperature, 
                              supports_thinking, supports_streaming, enabled, sort_order, config_json)
        VALUES (#{providerId}, #{name}, #{displayName}, #{maxTokens}, #{temperature},
                #{supportsThinking}, #{supportsStreaming}, #{enabled}, #{sortOrder}, #{configJson})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiModel model);

    /**
     * 更新模型信息
     * 
     * @param model 模型信息
     * @return 影响行数
     */
    @Update("""
        UPDATE ai_models SET
            display_name = #{displayName},
            max_tokens = #{maxTokens},
            temperature = #{temperature},
            supports_thinking = #{supportsThinking},
            supports_streaming = #{supportsStreaming},
            enabled = #{enabled},
            sort_order = #{sortOrder},
            config_json = #{configJson},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
    """)
    int update(AiModel model);

    /**
     * 删除模型
     * 
     * @param id 模型ID
     * @return 影响行数
     */
    @Delete("DELETE FROM ai_models WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 检查模型是否存在
     * 
     * @param providerId 提供者ID
     * @param name 模型名称
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM ai_models WHERE provider_id = #{providerId} AND name = #{name}")
    boolean existsByProviderAndName(@Param("providerId") Long providerId, @Param("name") String name);

    /**
     * 启用或禁用模型
     * 
     * @param id 模型ID
     * @param enabled 是否启用
     * @return 影响行数
     */
    @Update("""
        UPDATE ai_models SET 
            enabled = #{enabled},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
    """)
    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    /**
     * 根据提供者ID删除所有模型
     * 
     * @param providerId 提供者ID
     * @return 影响行数
     */
    @Delete("DELETE FROM ai_models WHERE provider_id = #{providerId}")
    int deleteByProviderId(@Param("providerId") Long providerId);
}