package com.example.service.provider;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 极简的模型提供者管理器
 * 直接利用Spring容器管理Provider，无需额外注册机制
 * 
 * @author xupeng
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelProviderManager {
    
    private final List<ModelProvider> providers;  // Spring自动注入所有Provider
    private final MultiModelProperties multiModelProperties;
    
    /**
     * 根据名称获取Provider
     * 
     * @param providerName 提供者名称
     * @return Provider实例
     * @throws IllegalArgumentException 如果Provider不存在或不可用
     */
    public ModelProvider getProvider(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider名称不能为空");
        }
        
        return providers.stream()
            .filter(p -> providerName.equals(p.getProviderName()) && p.isAvailable())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Provider不存在或不可用: " + providerName));
    }
    
    /**
     * 智能选择Provider
     * 
     * @param providerName 指定Provider名称（可选）
     * @param modelName 指定模型名称（可选）
     * @return 选中的Provider实例
     * @throws IllegalArgumentException 如果没有匹配的Provider
     */
    public ModelProvider selectProvider(String providerName, String modelName) {
        // 1. 如果指定了Provider名称，直接返回
        if (providerName != null && !providerName.trim().isEmpty()) {
            return getProvider(providerName);
        }
        
        // 2. 如果指定了模型名称，找到支持该模型的Provider
        if (modelName != null && !modelName.trim().isEmpty()) {
            ModelProvider provider = providers.stream()
                .filter(ModelProvider::isAvailable)
                .filter(p -> supportsModel(p, modelName))
                .findFirst()
                .orElse(null);
                
            if (provider != null) {
                log.debug("根据模型 {} 选择了Provider: {}", modelName, provider.getProviderName());
                return provider;
            }
        }
        
        // 3. 返回默认Provider
        String defaultProvider = multiModelProperties.getDefaultProvider();
        if (defaultProvider != null) {
            try {
                return getProvider(defaultProvider);
            } catch (Exception e) {
                log.warn("默认Provider {} 不可用，选择第一个可用Provider", defaultProvider);
            }
        }
        
        // 4. 选择第一个可用的Provider
        return providers.stream()
            .filter(ModelProvider::isAvailable)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("没有可用的Provider"));
    }
    
    /**
     * 获取默认Provider
     * 
     * @return 默认Provider实例
     */
    public ModelProvider getDefaultProvider() {
        String defaultProviderName = multiModelProperties.getDefaultProvider();
        return selectProvider(defaultProviderName, null);
    }
    
    /**
     * 获取所有可用Provider信息
     * 
     * @return Provider信息列表
     */
    public List<ProviderInfo> getAvailableProviders() {
        return providers.stream()
            .filter(ModelProvider::isAvailable)
            .map(this::convertToProviderInfo)
            .sorted(Comparator.comparing(ProviderInfo::getDisplayName))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有可用模型信息
     * 
     * @return 模型信息列表
     */
    public List<ModelInfo> getAllAvailableModels() {
        return providers.stream()
            .filter(ModelProvider::isAvailable)
            .flatMap(provider -> {
                try {
                    return provider.getAvailableModels().stream();
                } catch (Exception e) {
                    log.warn("获取Provider {} 的模型列表失败", provider.getProviderName(), e);
                    return Collections.<ModelInfo>emptyList().stream();
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 获取指定Provider的模型列表
     * 
     * @param providerName Provider名称
     * @return 模型信息列表
     */
    public List<ModelInfo> getProviderModels(String providerName) {
        try {
            ModelProvider provider = getProvider(providerName);
            return provider.getAvailableModels();
        } catch (Exception e) {
            log.warn("获取Provider {} 的模型列表失败", providerName, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 检查Provider是否可用
     * 
     * @param providerName Provider名称
     * @return 是否可用
     */
    public boolean isProviderAvailable(String providerName) {
        try {
            return providers.stream()
                .anyMatch(p -> providerName.equals(p.getProviderName()) && p.isAvailable());
        } catch (Exception e) {
            log.warn("检查Provider {} 可用性失败", providerName, e);
            return false;
        }
    }
    
    /**
     * 检查模型是否可用
     * 
     * @param providerName Provider名称
     * @param modelName 模型名称
     * @return 是否可用
     */
    public boolean isModelAvailable(String providerName, String modelName) {
        try {
            ModelProvider provider = getProvider(providerName);
            return supportsModel(provider, modelName);
        } catch (Exception e) {
            log.warn("检查模型 {}-{} 可用性失败", providerName, modelName, e);
            return false;
        }
    }
    
    /**
     * 获取模型详细信息
     * 
     * @param providerName Provider名称
     * @param modelName 模型名称
     * @return 模型信息，如果不存在返回null
     */
    public ModelInfo getModelInfo(String providerName, String modelName) {
        try {
            ModelProvider provider = getProvider(providerName);
            return provider.getModelInfo(modelName);
        } catch (Exception e) {
            log.warn("获取模型 {}-{} 信息失败", providerName, modelName, e);
            return null;
        }
    }
    
    /**
     * 获取注册的Provider数量
     * 
     * @return Provider总数
     */
    public int getProviderCount() {
        return providers.size();
    }
    
    /**
     * 获取可用Provider数量
     * 
     * @return 可用Provider数量
     */
    public int getAvailableProviderCount() {
        return (int) providers.stream()
            .filter(ModelProvider::isAvailable)
            .count();
    }
    
    /**
     * 初始化管理器
     */
    @PostConstruct
    public void initialize() {
        log.info("初始化模型提供者管理器，多模型功能启用: {}", multiModelProperties.isEnabled());
        log.info("发现 {} 个Provider实例，其中 {} 个可用", 
            getProviderCount(), getAvailableProviderCount());
        
        if (!multiModelProperties.isEnabled()) {
            log.warn("多模型功能已禁用");
            return;
        }
        
        // 输出所有可用Provider
        providers.stream()
            .filter(ModelProvider::isAvailable)
            .forEach(provider -> {
                int modelCount = provider.getAvailableModels().size();
                log.info("✅ Provider: {} ({}), 模型数量: {}", 
                    provider.getProviderName(), provider.getDisplayName(), modelCount);
            });
        
        log.info("模型提供者管理器初始化完成");
    }
    
    /**
     * 检查Provider是否支持指定模型
     */
    private boolean supportsModel(ModelProvider provider, String modelName) {
        try {
            return provider.getAvailableModels().stream()
                .anyMatch(model -> modelName.equals(model.getName()));
        } catch (Exception e) {
            log.warn("检查Provider {} 模型支持时出错", provider.getProviderName(), e);
            return false;
        }
    }
    
    /**
     * 将ModelProvider转换为ProviderInfo
     */
    private ProviderInfo convertToProviderInfo(ModelProvider provider) {
        ProviderInfo info = new ProviderInfo();
        info.setId((long) provider.getProviderName().hashCode());
        info.setName(provider.getProviderName());
        info.setDisplayName(provider.getDisplayName());
        info.setAvailable(provider.isAvailable());
        
        try {
            info.setModels(provider.getAvailableModels());
        } catch (Exception e) {
            log.warn("获取Provider {} 模型信息失败", provider.getProviderName(), e);
            info.setModels(Collections.emptyList());
        }
        
        return info;
    }
}