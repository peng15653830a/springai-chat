package com.example.service.impl;

import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import com.example.dto.common.UserModelPreferenceDto;
import com.example.service.ModelManagementService;
import com.example.service.provider.ModelProviderManager;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.dto.request.UserModelPreferenceRequest;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 模型管理服务实现类
 * 
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelManagementServiceImpl implements ModelManagementService {

    private final ModelProviderManager providerManager;
    
    // 简化的用户偏好缓存（生产环境可考虑使用Redis）
    private final ConcurrentMap<String, String> userDefaultModelCache = new ConcurrentHashMap<>();

    @Override
    public List<ProviderInfo> getAvailableProviders() {
        log.debug("获取所有可用的提供者列表");
        
        List<ProviderInfo> providers = providerManager.getAvailableProviders();
        log.info("获取到 {} 个可用提供者", providers.size());
        return providers;
    }

    @Override
    public List<ModelInfo> getProviderModels(String providerName) {
        log.debug("获取提供者 {} 的可用模型列表", providerName);
        
        return providerManager.getProviderModels(providerName);
    }

    @Override
    public List<ProviderInfo> getAllAvailableModels() {
        log.debug("获取所有可用模型列表");
        
        List<ProviderInfo> providers = getAvailableProviders();
        
        // 为每个提供者加载模型列表
        providers.forEach(provider -> {
            List<ModelInfo> models = getProviderModels(provider.getName());
            provider.setModels(models);
        });
        
        log.info("总共获取到 {} 个提供者的模型信息", providers.size());
        return providers;
    }

    @Override
    public UserModelPreferenceDto getUserDefaultModel(Long userId) {
        log.debug("获取用户 {} 的默认模型偏好", userId);
        
        if (userId == null) {
            return null;
        }
        
        // 从缓存获取默认模型
        String defaultModel = userDefaultModelCache.get(String.valueOf(userId));
        if (defaultModel != null) {
            String[] parts = defaultModel.split(":");
            if (parts.length == 2) {
                UserModelPreferenceDto preference = new UserModelPreferenceDto();
                preference.setUserId(userId);
                preference.setProviderName(parts[0]);
                preference.setModelName(parts[1]);
                preference.setIsDefault(true);
                log.info("用户 {} 的默认模型: {}-{}", userId, parts[0], parts[1]);
                return preference;
            }
        }
        
        return null;
    }

    @Override
    public boolean saveUserModelPreference(UserModelPreferenceRequest request) {
        log.info("保存用户模型偏好，用户ID: {}, 模型: {}-{}, 是否默认: {}", 
                request.getUserId(), request.getProviderName(), request.getModelName(), request.isDefault());
        
        if (request.getUserId() == null || request.getProviderName() == null || request.getModelName() == null) {
            log.warn("参数不完整，无法保存用户模型偏好");
            return false;
        }
        
        try {
            // 验证提供者和模型是否存在
            ModelInfo modelInfo = getModelInfo(request.getProviderName(), request.getModelName());
            if (modelInfo == null) {
                log.warn("模型不存在: {}-{}", request.getProviderName(), request.getModelName());
                return false;
            }
            
            // 如果设置为默认，保存到缓存
            if (request.isDefault()) {
                userDefaultModelCache.put(String.valueOf(request.getUserId()), 
                        request.getProviderName() + ":" + request.getModelName());
                log.info("用户 {} 默认模型设置为: {}-{}", request.getUserId(), 
                        request.getProviderName(), request.getModelName());
            }
            
            return true;
        } catch (Exception e) {
            log.error("保存用户模型偏好时发生错误", e);
            return false;
        }
    }

    @Override
    public List<UserModelPreferenceDto> getUserModelPreferences(Long userId) {
        log.debug("获取用户 {} 的所有模型偏好", userId);
        
        if (userId == null) {
            return List.of();
        }
        
        // 简化实现：只返回默认模型偏好
        UserModelPreferenceDto defaultModel = getUserDefaultModel(userId);
        return defaultModel != null ? List.of(defaultModel) : List.of();
    }

    @Override
    public boolean deleteUserModelPreference(Long userId, String providerName, String modelName) {
        log.info("删除用户模型偏好，用户ID: {}, 模型: {}-{}", userId, providerName, modelName);
        
        if (userId == null || providerName == null || modelName == null) {
            log.warn("参数不完整，无法删除用户模型偏好");
            return false;
        }
        
        try {
            String expectedModel = providerName + ":" + modelName;
            String currentDefault = userDefaultModelCache.get(String.valueOf(userId));
            
            if (expectedModel.equals(currentDefault)) {
                userDefaultModelCache.remove(String.valueOf(userId));
                log.info("用户 {} 的默认模型偏好已删除", userId);
                return true;
            } else {
                log.warn("用户模型偏好删除失败，可能不存在或不是默认模型");
                return false;
            }
        } catch (Exception e) {
            log.error("删除用户模型偏好时发生错误", e);
            return false;
        }
    }

    @Override
    public ModelInfo getModelInfo(String providerName, String modelName) {
        log.debug("获取模型详细信息: {}-{}", providerName, modelName);
        
        return providerManager.getModelInfo(providerName, modelName);
    }

    @Override
    public boolean isModelAvailable(String providerName, String modelName) {
        log.debug("检查模型是否可用: {}-{}", providerName, modelName);
        
        return providerManager.isModelAvailable(providerName, modelName);
    }
}