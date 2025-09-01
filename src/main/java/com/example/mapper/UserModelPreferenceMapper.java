package com.example.mapper;

import com.example.entity.UserModelPreference;
import com.example.dto.common.UserModelPreferenceDto;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户模型偏好数据访问接口
 * 
 * @author xupeng
 */
@Mapper
public interface UserModelPreferenceMapper {

    /**
     * 查询用户的默认模型偏好
     * 
     * @param userId 用户ID
     * @return 用户模型偏好DTO
     */
    @Select("""
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
            m.supports_streaming
        FROM user_model_preferences ump
        JOIN ai_providers p ON ump.provider_id = p.id
        JOIN ai_models m ON ump.model_id = m.id
        WHERE ump.user_id = #{userId} AND ump.is_default = true
        AND p.enabled = true AND m.enabled = true
        LIMIT 1
    """)
    UserModelPreferenceDto findUserDefaultModel(@Param("userId") Long userId);

    /**
     * 查询用户的所有模型偏好
     * 
     * @param userId 用户ID
     * @return 用户模型偏好DTO列表
     */
    @Select("""
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
            m.supports_streaming
        FROM user_model_preferences ump
        JOIN ai_providers p ON ump.provider_id = p.id
        JOIN ai_models m ON ump.model_id = m.id
        WHERE ump.user_id = #{userId}
        AND p.enabled = true AND m.enabled = true
        ORDER BY ump.is_default DESC, p.display_name, m.sort_order
    """)
    List<UserModelPreferenceDto> findUserModelPreferences(@Param("userId") Long userId);

    /**
     * 根据用户ID、提供者ID和模型ID查询偏好
     * 
     * @param userId 用户ID
     * @param providerId 提供者ID
     * @param modelId 模型ID
     * @return 用户模型偏好
     */
    @Select("""
        SELECT * FROM user_model_preferences 
        WHERE user_id = #{userId} AND provider_id = #{providerId} AND model_id = #{modelId}
    """)
    UserModelPreference findByUserAndProviderAndModel(@Param("userId") Long userId, 
                                                     @Param("providerId") Long providerId,
                                                     @Param("modelId") Long modelId);

    /**
     * 保存用户模型偏好（使用ON CONFLICT处理重复）
     * 
     * @param preference 用户模型偏好
     * @return 影响行数
     */
    @Insert("""
        INSERT INTO user_model_preferences (user_id, provider_id, model_id, is_default)
        VALUES (#{userId}, #{providerId}, #{modelId}, #{isDefault})
        ON CONFLICT (user_id, provider_id, model_id) 
        DO UPDATE SET 
            is_default = #{isDefault},
            updated_at = CURRENT_TIMESTAMP
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int saveOrUpdate(UserModelPreference preference);

    /**
     * 设置用户的默认模型（先清除其他默认设置）
     * 
     * @param userId 用户ID
     * @param providerId 提供者ID
     * @param modelId 模型ID
     * @return 影响行数
     */
    @Update("""
        WITH clear_defaults AS (
            UPDATE user_model_preferences 
            SET is_default = false, updated_at = CURRENT_TIMESTAMP
            WHERE user_id = #{userId}
        )
        INSERT INTO user_model_preferences (user_id, provider_id, model_id, is_default)
        VALUES (#{userId}, #{providerId}, #{modelId}, true)
        ON CONFLICT (user_id, provider_id, model_id)
        DO UPDATE SET 
            is_default = true,
            updated_at = CURRENT_TIMESTAMP
    """)
    int setUserDefaultModel(@Param("userId") Long userId, 
                           @Param("providerId") Long providerId,
                           @Param("modelId") Long modelId);

    /**
     * 删除用户模型偏好
     * 
     * @param userId 用户ID
     * @param providerId 提供者ID
     * @param modelId 模型ID
     * @return 影响行数
     */
    @Delete("""
        DELETE FROM user_model_preferences 
        WHERE user_id = #{userId} AND provider_id = #{providerId} AND model_id = #{modelId}
    """)
    int deleteByUserAndProviderAndModel(@Param("userId") Long userId,
                                       @Param("providerId") Long providerId,
                                       @Param("modelId") Long modelId);

    /**
     * 删除用户的所有模型偏好
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_model_preferences WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 清除用户的所有默认设置
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("""
        UPDATE user_model_preferences 
        SET is_default = false, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = #{userId}
    """)
    int clearUserDefaults(@Param("userId") Long userId);

    /**
     * 检查用户是否有指定的模型偏好
     * 
     * @param userId 用户ID
     * @param providerId 提供者ID  
     * @param modelId 模型ID
     * @return 是否存在
     */
    @Select("""
        SELECT COUNT(*) FROM user_model_preferences 
        WHERE user_id = #{userId} AND provider_id = #{providerId} AND model_id = #{modelId}
    """)
    boolean existsByUserAndProviderAndModel(@Param("userId") Long userId,
                                          @Param("providerId") Long providerId,
                                          @Param("modelId") Long modelId);

    /**
     * 统计用户的模型偏好数量
     * 
     * @param userId 用户ID
     * @return 偏好数量
     */
    @Select("SELECT COUNT(*) FROM user_model_preferences WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}