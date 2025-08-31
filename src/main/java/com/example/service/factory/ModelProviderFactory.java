package com.example.service.factory;

import com.example.config.MultiModelProperties;
import com.example.dto.ModelInfo;
import com.example.dto.ProviderInfo;
import com.example.service.provider.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模型提供者工厂类
 * 管理所有AI模型提供者，提供统一的模型访问接口
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class ModelProviderFactory {

    /**
     * 模型提供者注册表
     * Key: 提供者名称, Value: 提供者实例
     */
    private final Map<String, ModelProvider> providerRegistry = new ConcurrentHashMap<>();

    /**
     * 模型映射表
     * Key: 完整模型ID (providerId-modelName), Value: 提供者实例
     */
    private final Map<String, ModelProvider> modelProviderMap = new ConcurrentHashMap<>();

    @Autowired
    private MultiModelProperties multiModelProperties;

    /**
     * 注册模型提供者
     * 
     * @param provider 模型提供者实例
     */
    public void registerProvider(ModelProvider provider) {
        String providerName = provider.getProviderName();
        log.info("注册模型提供者: {}", providerName);
        
        providerRegistry.put(providerName, provider);
        
        // 注册该提供者下的所有模型
        List<ModelInfo> models = provider.getAvailableModels();
        for (ModelInfo model : models) {
            String fullModelId = model.getFullModelId(getProviderId(providerName));
            modelProviderMap.put(fullModelId, provider);
            log.debug("注册模型: {} -> {}", fullModelId, providerName);
        }
    }

    /**
     * 获取指定提供者
     * 
     * @param providerName 提供者名称
     * @return 模型提供者实例
     * @throws IllegalArgumentException 如果提供者不存在或不可用
     */
    public ModelProvider getProvider(String providerName) {
        ModelProvider provider = providerRegistry.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("未找到模型提供者: " + providerName);
        }
        if (!provider.isAvailable()) {
            throw new IllegalArgumentException("模型提供者不可用: " + providerName);
        }
        return provider;
    }

    /**
     * 根据完整模型ID获取提供者
     * 
     * @param fullModelId 完整模型ID (providerId-modelName)
     * @return 模型提供者实例
     * @throws IllegalArgumentException 如果模型不存在或不可用
     */
    public ModelProvider getProviderByModelId(String fullModelId) {
        ModelProvider provider = modelProviderMap.get(fullModelId);
        if (provider == null) {
            throw new IllegalArgumentException("未找到模型: " + fullModelId);
        }
        if (!provider.isAvailable()) {
            throw new IllegalArgumentException("模型提供者不可用: " + provider.getProviderName());
        }
        return provider;
    }

    /**
     * 获取默认提供者
     * 
     * @return 默认模型提供者实例
     */
    public ModelProvider getDefaultProvider() {
        String defaultProviderName = multiModelProperties.getDefaultProvider();
        return getProvider(defaultProviderName);
    }

    /**
     * 获取所有可用的提供者信息
     * 
     * @return 提供者信息列表
     */
    public List<ProviderInfo> getAvailableProviders() {
        return providerRegistry.values().stream()
                .filter(ModelProvider::isAvailable)
                .map(this::convertToProviderInfo)
                .sorted(Comparator.comparing(ProviderInfo::getDisplayName))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用的模型信息
     * 
     * @return 模型信息列表
     */
    public List<ModelInfo> getAllAvailableModels() {
        List<ModelInfo> allModels = new ArrayList<>();
        for (ModelProvider provider : providerRegistry.values()) {
            if (provider.isAvailable()) {
                allModels.addAll(provider.getAvailableModels());
            }
        }
        return allModels;
    }

    /**
     * 检查指定模型是否可用
     * 
     * @param fullModelId 完整模型ID
     * @return 是否可用
     */
    public boolean isModelAvailable(String fullModelId) {
        ModelProvider provider = modelProviderMap.get(fullModelId);
        return provider != null && provider.isAvailable();
    }

    /**
     * 获取模型信息
     * 
     * @param fullModelId 完整模型ID
     * @return 模型信息
     */
    public ModelInfo getModelInfo(String fullModelId) {
        ModelProvider provider = getProviderByModelId(fullModelId);
        String modelName = extractModelName(fullModelId);
        return provider.getModelInfo(modelName);
    }

    /**
     * 初始化工厂
     */
    @PostConstruct
    public void initialize() {
        log.info("初始化模型提供者工厂，多模型功能启用: {}", multiModelProperties.isEnabled());
        
        if (!multiModelProperties.isEnabled()) {
            log.warn("多模型功能已禁用");
            return;
        }

        // 这里会被Spring自动注入的ModelProvider实例通过registerProvider方法注册
        log.info("模型提供者工厂初始化完成");
    }

    /**
     * 将ModelProvider转换为ProviderInfo
     * 
     * @param provider 模型提供者
     * @return 提供者信息
     */
    private ProviderInfo convertToProviderInfo(ModelProvider provider) {
        ProviderInfo info = new ProviderInfo();
        info.setId(getProviderId(provider.getProviderName()));
        info.setName(provider.getProviderName());
        info.setDisplayName(provider.getDisplayName());
        info.setAvailable(provider.isAvailable());
        info.setModels(provider.getAvailableModels());
        return info;
    }

    /**
     * 获取提供者ID（这里简化处理，实际应从数据库获取）
     * 
     * @param providerName 提供者名称
     * @return 提供者ID
     */
    private Long getProviderId(String providerName) {
        // TODO: 从数据库获取真实的提供者ID
        // 这里暂时使用hashCode作为临时ID
        return (long) providerName.hashCode();
    }

    /**
     * 从完整模型ID中提取模型名称
     * 
     * @param fullModelId 完整模型ID (providerId-modelName)
     * @return 模型名称
     */
    private String extractModelName(String fullModelId) {
        int dashIndex = fullModelId.indexOf('-');
        if (dashIndex == -1) {
            throw new IllegalArgumentException("无效的模型ID格式: " + fullModelId);
        }
        return fullModelId.substring(dashIndex + 1);
    }

    /**
     * 获取已注册的提供者数量
     * 
     * @return 提供者数量
     */
    public int getProviderCount() {
        return providerRegistry.size();
    }

    /**
     * 获取可用的提供者数量
     * 
     * @return 可用提供者数量
     */
    public int getAvailableProviderCount() {
        return (int) providerRegistry.values().stream()
                .filter(ModelProvider::isAvailable)
                .count();
    }
}