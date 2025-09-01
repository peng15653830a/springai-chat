package com.example.service;

import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import com.example.dto.common.UserModelPreferenceDto;

import java.util.List;

/**
 * 模型管理服务接口
 * 
 * @author xupeng
 */
public interface ModelManagementService {

    /**
     * 获取所有可用的提供者列表
     * 
     * @return 提供者信息列表
     */
    List<ProviderInfo> getAvailableProviders();

    /**
     * 获取指定提供者下的可用模型列表
     * 
     * @param providerName 提供者名称
     * @return 模型信息列表
     */
    List<ModelInfo> getProviderModels(String providerName);

    /**
     * 获取所有可用的模型列表
     * 
     * @return 模型信息列表（按提供者分组）
     */
    List<ProviderInfo> getAllAvailableModels();

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
     * @param userId 用户ID
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @param isDefault 是否设为默认
     * @return 是否保存成功
     */
    boolean saveUserModelPreference(Long userId, String providerName, String modelName, boolean isDefault);

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
     * @param userId 用户ID
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 是否删除成功
     */
    boolean deleteUserModelPreference(Long userId, String providerName, String modelName);

    /**
     * 获取模型详细信息
     * 
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 模型信息
     */
    ModelInfo getModelInfo(String providerName, String modelName);

    /**
     * 检查指定模型是否可用
     * 
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 是否可用
     */
    boolean isModelAvailable(String providerName, String modelName);
}