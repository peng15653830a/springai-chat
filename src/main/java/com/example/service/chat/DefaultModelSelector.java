package com.example.service.chat;

import com.example.dto.common.ModelInfo;
import com.example.dto.common.UserModelPreferenceDto;
import com.example.service.ModelManagementService;
import com.example.service.provider.ModelProvider;
import com.example.service.provider.ModelProviderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认的模型选择器实现
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultModelSelector implements ModelSelector {

    private final ModelProviderManager providerManager;
    private final ModelManagementService modelManagementService;

    @Override
    public ModelProvider getModelProvider(String providerName) {
        if (providerName != null && !providerName.trim().isEmpty()) {
            // 使用指定的提供者
            return providerManager.getProvider(providerName);
        } else {
            // 使用默认提供者
            return providerManager.getDefaultProvider();
        }
    }

    @Override
    public String getActualModelName(ModelProvider provider, String modelName) {
        if (modelName != null && !modelName.trim().isEmpty()) {
            // 验证指定的模型是否可用
            List<ModelInfo> availableModels = provider.getAvailableModels();
            boolean modelExists = availableModels.stream()
                    .anyMatch(model -> modelName.equals(model.getName()));
            
            if (modelExists) {
                log.debug("使用指定模型: {}", modelName);
                return modelName;
            } else {
                log.warn("指定的模型 {} 不可用，使用默认模型", modelName);
            }
        }
        
        // 使用该提供者的第一个可用模型
        List<ModelInfo> availableModels = provider.getAvailableModels();
        if (!availableModels.isEmpty()) {
            String defaultModel = availableModels.get(0).getName();
            log.debug("使用默认模型: {}", defaultModel);
            return defaultModel;
        }
        
        throw new IllegalStateException("提供者 " + provider.getProviderName() + " 没有可用的模型");
    }

    @Override
    public ModelSelection selectModelForUser(Long userId, String providerName, String modelName) {
        log.debug("为用户 {} 选择模型，指定提供者: {}, 指定模型: {}", userId, providerName, modelName);

        // 如果明确指定了提供者和模型，直接使用
        if (providerName != null && modelName != null) {
            ModelProvider provider = getModelProvider(providerName);
            String actualModelName = getActualModelName(provider, modelName);
            return new ModelSelection(provider, actualModelName);
        }

        // 尝试获取用户偏好
        if (userId != null) {
            try {
                UserModelPreferenceDto userPreference = modelManagementService.getUserDefaultModel(userId);
                if (userPreference != null) {
                    String preferredProvider = userPreference.getProviderName();
                    String preferredModel = userPreference.getModelName();
                    
                    log.debug("使用用户偏好模型: {} - {}", preferredProvider, preferredModel);
                    
                    ModelProvider provider = getModelProvider(preferredProvider);
                    String actualModelName = getActualModelName(provider, preferredModel);
                    return new ModelSelection(provider, actualModelName);
                }
            } catch (Exception e) {
                log.warn("获取用户模型偏好失败，使用默认模型", e);
            }
        }

        // 使用系统默认选择
        ModelProvider provider = getModelProvider(providerName);
        String actualModelName = getActualModelName(provider, modelName);
        return new ModelSelection(provider, actualModelName);
    }
}