package com.example.service.factory;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import com.example.service.provider.ModelProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * 优化后的模型提供者工厂类
 * 使用策略模式和注册机制，支持动态扩展
 * 
 * @author xupeng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelProviderFactory {

    private final MultiModelProperties multiModelProperties;
    private final ProviderRegistry providerRegistry;
    private final ProviderSelectionStrategy selectionStrategy;

    /**
     * 注册模型提供者
     * 
     * @param provider 模型提供者实例
     */
    public void registerProvider(ModelProvider provider) {
        log.info("注册模型提供者: {}", provider.getProviderName());
        providerRegistry.register(provider);
    }

    /**
     * 注销模型提供者
     * 
     * @param providerName 提供者名称
     */
    public void unregisterProvider(String providerName) {
        log.info("注销模型提供者: {}", providerName);
        providerRegistry.unregister(providerName);
    }

    /**
     * 获取指定提供者
     * 
     * @param providerName 提供者名称
     * @return 模型提供者实例
     * @throws IllegalArgumentException 如果提供者不存在或不可用
     */
    public ModelProvider getProvider(String providerName) {
        return providerRegistry.findByName(providerName)
                .orElseThrow(() -> new IllegalArgumentException("模型提供者不存在或不可用: " + providerName));
    }

    /**
     * 根据完整模型ID获取提供者
     * 
     * @param fullModelId 完整模型ID (providerId-modelName)
     * @return 模型提供者实例
     * @throws IllegalArgumentException 如果模型不存在或不可用
     */
    public ModelProvider getProviderByModelId(String fullModelId) {
        return providerRegistry.findByModelId(fullModelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在或不可用: " + fullModelId));
    }

    /**
     * 获取默认提供者
     * 使用选择策略来选择最佳提供者
     * 
     * @return 默认模型提供者实例
     * @throws IllegalStateException 如果没有可用的提供者
     */
    public ModelProvider getDefaultProvider() {
        String defaultProviderName = multiModelProperties.getDefaultProvider();
        List<ModelProvider> availableProviders = providerRegistry.findAllAvailable();
        
        Optional<ModelProvider> selectedProvider = selectionStrategy.selectProvider(
                availableProviders, 
                defaultProviderName, 
                null
        );
        
        return selectedProvider.orElseThrow(() -> 
                new IllegalStateException("没有可用的模型提供者"));
    }

    /**
     * 智能选择提供者
     * 根据指定的提供者名称和模型名称选择最合适的提供者
     * 
     * @param providerName 指定的提供者名称（可选）
     * @param modelName 指定的模型名称（可选）
     * @return 选中的提供者
     * @throws IllegalStateException 如果没有匹配的提供者
     */
    public ModelProvider selectProvider(String providerName, String modelName) {
        List<ModelProvider> availableProviders = providerRegistry.findAllAvailable();
        
        Optional<ModelProvider> selectedProvider = selectionStrategy.selectProvider(
                availableProviders, 
                providerName, 
                modelName
        );
        
        return selectedProvider.orElseThrow(() -> 
                new IllegalStateException("没有找到匹配的模型提供者"));
    }

    /**
     * 获取所有可用的提供者信息
     * 
     * @return 提供者信息列表
     */
    public List<ProviderInfo> getAvailableProviders() {
        return providerRegistry.getAllProviderInfo();
    }

    /**
     * 获取所有可用的模型信息
     * 
     * @return 模型信息列表
     */
    public List<ModelInfo> getAllAvailableModels() {
        return providerRegistry.getAllModelInfo();
    }

    /**
     * 检查指定模型是否可用
     * 
     * @param fullModelId 完整模型ID
     * @return 是否可用
     */
    public boolean isModelAvailable(String fullModelId) {
        return providerRegistry.isModelAvailable(fullModelId);
    }

    /**
     * 检查指定提供者是否可用
     * 
     * @param providerName 提供者名称
     * @return 是否可用
     */
    public boolean isProviderAvailable(String providerName) {
        return providerRegistry.isAvailable(providerName);
    }

    /**
     * 获取模型信息
     * 
     * @param fullModelId 完整模型ID
     * @return 模型信息
     * @throws IllegalArgumentException 如果模型不存在
     */
    public ModelInfo getModelInfo(String fullModelId) {
        ModelProvider provider = getProviderByModelId(fullModelId);
        String modelName = extractModelName(fullModelId);
        return provider.getModelInfo(modelName);
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
        return providerRegistry.findAllAvailable().size();
    }

    /**
     * 初始化工厂
     */
    @PostConstruct
    public void initialize() {
        log.info("初始化模型提供者工厂，多模型功能启用: {}", multiModelProperties.isEnabled());
        log.info("使用选择策略: {}", selectionStrategy.getStrategyName());
        
        if (!multiModelProperties.isEnabled()) {
            log.warn("多模型功能已禁用");
            return;
        }

        // Spring会自动注入ModelProvider实例并通过registerProvider方法注册
        log.info("模型提供者工厂初始化完成");
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
}