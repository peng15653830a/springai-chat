package com.example.mapper;

import com.example.entity.AiProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI提供者数据访问接口
 * 
 * @author xupeng
 */
@Mapper
public interface AiProviderMapper {

    /**
     * 查询所有启用的提供者
     * 
     * @return 提供者列表
     */
    @Select("SELECT * FROM ai_providers WHERE enabled = true ORDER BY display_name")
    List<AiProvider> findEnabledProviders();

    /**
     * 根据名称查询提供者
     * 
     * @param name 提供者名称
     * @return 提供者信息
     */
    @Select("SELECT * FROM ai_providers WHERE name = #{name}")
    AiProvider findByName(@Param("name") String name);

    /**
     * 根据ID查询提供者
     * 
     * @param id 提供者ID
     * @return 提供者信息
     */
    @Select("SELECT * FROM ai_providers WHERE id = #{id}")
    AiProvider findById(@Param("id") Long id);

    /**
     * 查询所有提供者
     * 
     * @return 提供者列表
     */
    @Select("SELECT * FROM ai_providers ORDER BY display_name")
    List<AiProvider> findAll();

    /**
     * 插入新提供者
     * 
     * @param provider 提供者信息
     * @return 影响行数
     */
    @Insert("""
        INSERT INTO ai_providers (name, display_name, base_url, api_key_env, enabled, config_json)
        VALUES (#{name}, #{displayName}, #{baseUrl}, #{apiKeyEnv}, #{enabled}, #{configJson})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiProvider provider);

    /**
     * 更新提供者信息
     * 
     * @param provider 提供者信息
     * @return 影响行数
     */
    @Update("""
        UPDATE ai_providers SET 
            display_name = #{displayName},
            base_url = #{baseUrl},
            api_key_env = #{apiKeyEnv},
            enabled = #{enabled},
            config_json = #{configJson},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
    """)
    int update(AiProvider provider);

    /**
     * 删除提供者
     * 
     * @param id 提供者ID
     * @return 影响行数
     */
    @Delete("DELETE FROM ai_providers WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 检查提供者名称是否存在
     * 
     * @param name 提供者名称
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM ai_providers WHERE name = #{name}")
    boolean existsByName(@Param("name") String name);

    /**
     * 启用或禁用提供者
     * 
     * @param id 提供者ID
     * @param enabled 是否启用
     * @return 影响行数
     */
    @Update("""
        UPDATE ai_providers SET 
            enabled = #{enabled},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
    """)
    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);
}