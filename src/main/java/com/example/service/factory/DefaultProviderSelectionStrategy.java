package com.example.service.factory;

import com.example.service.provider.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 默认的提供者选择策略
 * 优先级：指定提供者 > 指定模型的提供者 > 第一个可用提供者
 *
 * @author xupeng
 */
@Slf4j
@Component
public class DefaultProviderSelectionStrategy implements ProviderSelectionStrategy {

    @Override
    public Optional<ModelProvider> selectProvider(List<ModelProvider> availableProviders, 
                                                 String providerName, 
                                                 String modelName) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            log.debug("No available providers");
            return Optional.empty();
        }

        // 1. 如果指定了提供者名称，优先选择
        if (providerName != null && !providerName.trim().isEmpty()) {
            Optional<ModelProvider> namedProvider = availableProviders.stream()
                    .filter(provider -> providerName.equals(provider.getProviderName()))
                    .filter(ModelProvider::isAvailable)
                    .findFirst();
            
            if (namedProvider.isPresent()) {
                log.debug("Selected provider by name: {}", providerName);
                return namedProvider;
            } else {
                log.warn("Specified provider not found or not available: {}", providerName);
            }
        }

        // 2. 如果指定了模型名称，查找支持该模型的提供者
        if (modelName != null && !modelName.trim().isEmpty()) {
            Optional<ModelProvider> modelProvider = availableProviders.stream()
                    .filter(ModelProvider::isAvailable)
                    .filter(provider -> supportsModel(provider, modelName))
                    .findFirst();
            
            if (modelProvider.isPresent()) {
                log.debug("Selected provider by model: {}", modelName);
                return modelProvider;
            } else {
                log.warn("No provider found for specified model: {}", modelName);
            }
        }

        // 3. 选择第一个可用的提供者
        Optional<ModelProvider> defaultProvider = availableProviders.stream()
                .filter(ModelProvider::isAvailable)
                .findFirst();
        
        if (defaultProvider.isPresent()) {
            log.debug("Selected default provider: {}", defaultProvider.get().getProviderName());
        } else {
            log.warn("No available providers found");
        }
        
        return defaultProvider;
    }

    @Override
    public String getStrategyName() {
        return "default";
    }

    @Override
    public String getDescription() {
        return "Default selection strategy: prefer specified provider > model match > first available";
    }

    /**
     * 检查提供者是否支持指定的模型
     */
    private boolean supportsModel(ModelProvider provider, String modelName) {
        try {
            return provider.getAvailableModels().stream()
                    .anyMatch(model -> modelName.equals(model.getName()));
        } catch (Exception e) {
            log.warn("Error checking model support for provider: {}", provider.getProviderName(), e);
            return false;
        }
    }
}