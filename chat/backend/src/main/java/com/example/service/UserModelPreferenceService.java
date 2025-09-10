package com.example.service;

import com.example.dto.common.UserModelPreferenceDto;
import com.example.dto.request.UserModelPreferenceRequest;

import java.util.List;

/**
 * 用户模型偏好服务接口
 * 专注于用户模型偏好的CRUD操作，不涉及模型查询逻辑
 * 
 * @author xupeng
 */
public interface UserModelPreferenceService {

    /**
     * 获取用户的默认模型偏好
     * 
     * @param userId 用户ID
     * @return 用户模型偏好，如果没有则返回null
     */
    UserModelPreferenceDto getUserDefaultModel(Long userId);

    /**
     * 保存用户模型偏好
     * 
     * @param request 用户模型偏好请求对象
     * @return 是否保存成功
     */
    boolean saveUserModelPreference(UserModelPreferenceRequest request);

    /**
     * 获取用户的所有模型偏好
     * 
     * @param userId 用户ID
     * @return 用户模型偏好列表
     */
    List<UserModelPreferenceDto> getUserModelPreferences(Long userId);

    /**
     * 删除用户模型偏好
     * 
     * @param request 删除请求，包含用户ID、提供者名称和模型名称
     * @return 是否删除成功
     */
    boolean deleteUserModelPreference(com.example.dto.request.DeleteUserModelPreferenceRequest request);

    /**
     * 删除用户模型偏好（向后兼容）
     * 
     * @param userId 用户ID
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 是否删除成功
     */
    default boolean deleteUserModelPreference(Long userId, String providerName, String modelName) {
        return deleteUserModelPreference(com.example.dto.request.DeleteUserModelPreferenceRequest.builder()
                .userId(userId)
                .providerName(providerName)
                .modelName(modelName)
                .build());
    }
}