package com.example.service.factory;

import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import com.example.service.provider.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认的模型提供者注册实现
 * 使用内存存储，支持并发访问
 *
 * @author xupeng
 */
@Slf4j
@Component
public class DefaultProviderRegistry implements ProviderRegistry {

    /**
     * 提供者注册表
     * Key: 提供者名称, Value: 提供者实例
     */
    private final Map<String, ModelProvider> providers = new ConcurrentHashMap<>();

    /**
     * 模型映射表
     * Key: 完整模型ID, Value: 提供者实例
     */
    private final Map<String, ModelProvider> modelProviders = new ConcurrentHashMap<>();

    @Override
    public void register(ModelProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        String providerName = provider.getProviderName();
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }

        if (providers.containsKey(providerName)) {
            log.warn("Provider {} is already registered, replacing with new instance", providerName);
        }

        providers.put(providerName, provider);

        // 注册该提供者下的所有模型
        try {
            List<ModelInfo> models = provider.getAvailableModels();
            for (ModelInfo model : models) {
                String fullModelId = model.getFullModelId(getProviderId(providerName));
                modelProviders.put(fullModelId, provider);
                log.debug("Registered model: {} -> {}", fullModelId, providerName);
            }
            log.info("Successfully registered provider: {} with {} models", providerName, models.size());
        } catch (Exception e) {
            log.error("Failed to register models for provider: {}", providerName, e);
            providers.remove(providerName); // 回滚注册
            throw new IllegalArgumentException("Failed to register provider: " + providerName, e);
        }
    }

    @Override
    public void unregister(String providerName) {
        ModelProvider provider = providers.remove(providerName);
        if (provider != null) {
            // 移除该提供者的所有模型映射
            modelProviders.entrySet().removeIf(entry -> entry.getValue().equals(provider));
            log.info("Unregistered provider: {}", providerName);
        }
    }

    @Override
    public Optional<ModelProvider> findByName(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            return Optional.empty();
        }
        ModelProvider provider = providers.get(providerName);
        return (provider != null && provider.isAvailable()) ? Optional.of(provider) : Optional.empty();
    }

    @Override
    public Optional<ModelProvider> findByModelId(String fullModelId) {
        if (fullModelId == null || fullModelId.trim().isEmpty()) {
            return Optional.empty();
        }
        ModelProvider provider = modelProviders.get(fullModelId);
        return (provider != null && provider.isAvailable()) ? Optional.of(provider) : Optional.empty();
    }

    @Override
    public List<ModelProvider> findAllAvailable() {
        return providers.values().stream()
                .filter(ModelProvider::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderInfo> getAllProviderInfo() {
        return providers.values().stream()
                .filter(ModelProvider::isAvailable)
                .map(this::convertToProviderInfo)
                .sorted(Comparator.comparing(ProviderInfo::getDisplayName))
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelInfo> getAllModelInfo() {
        List<ModelInfo> allModels = new ArrayList<>();
        for (ModelProvider provider : providers.values()) {
            if (provider.isAvailable()) {
                try {
                    allModels.addAll(provider.getAvailableModels());
                } catch (Exception e) {
                    log.warn("Failed to get models from provider: {}", provider.getProviderName(), e);
                }
            }
        }
        return allModels;
    }

    @Override
    public boolean isAvailable(String providerName) {
        ModelProvider provider = providers.get(providerName);
        return provider != null && provider.isAvailable();
    }

    @Override
    public boolean isModelAvailable(String fullModelId) {
        ModelProvider provider = modelProviders.get(fullModelId);
        return provider != null && provider.isAvailable();
    }

    @Override
    public int size() {
        return providers.size();
    }

    /**
     * 将ModelProvider转换为ProviderInfo
     */
    private ProviderInfo convertToProviderInfo(ModelProvider provider) {
        ProviderInfo info = new ProviderInfo();
        info.setId(getProviderId(provider.getProviderName()));
        info.setName(provider.getProviderName());
        info.setDisplayName(provider.getDisplayName());
        info.setAvailable(provider.isAvailable());
        try {
            info.setModels(provider.getAvailableModels());
        } catch (Exception e) {
            log.warn("Failed to get models for provider info: {}", provider.getProviderName(), e);
            info.setModels(Collections.emptyList());
        }
        return info;
    }

    /**
     * 获取提供者ID（简化处理，实际应从数据库获取）
     */
    private Long getProviderId(String providerName) {
        return (long) providerName.hashCode();
    }
}