package com.example.service.provider;

import com.example.config.EnhancedAiConfig;
import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型注册表抽象基类
 * 负责管理和提供模型信息，不负责具体的聊天功能
 * 聊天功能由对应的ChatModel实现处理
 * 
 * @author xupeng
 */
@Slf4j
public abstract class AbstractModelRegistry implements ModelProvider {

    protected final MultiModelProperties multiModelProperties;

    public AbstractModelRegistry(MultiModelProperties multiModelProperties) {
        this.multiModelProperties = multiModelProperties;
    }

    @Override
    public List<ModelInfo> getAvailableModels() {
        Map<String, MultiModelProperties.ProviderConfig> providers = multiModelProperties.getProviders();
        if (providers == null) {
            return Collections.emptyList();
        }
        
        MultiModelProperties.ProviderConfig providerConfig = providers.get(getProviderName());
        
        if (providerConfig == null || !providerConfig.isEnabled()) {
            return Collections.emptyList();
        }

        return providerConfig.getModels().stream()
                .filter(MultiModelProperties.ModelConfig::isEnabled)
                .map(this::convertToModelInfo)
                .collect(Collectors.toList());
    }

    // 移除streamChat方法，该功能由ChatModel实现

    @Override
    public boolean isAvailable() {
        return multiModelProperties.isProviderAvailable(getProviderName());
    }

    @Override
    public boolean supportsThinking(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsThinking)
                .orElse(false);
    }

    @Override
    public boolean supportsStreaming(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsStreaming)
                .orElse(true);
    }

    @Override
    public ModelInfo getModelInfo(String modelName) {
        return getModelConfig(modelName)
                .map(this::convertToModelInfo)
                .orElse(null);
    }

    // 移除callModelWithChatClient方法，该功能由ChatModel实现

    // 移除saveMessageAndGenerateEndEvent方法，消息保存由服务层处理

    /**
     * 将配置转换为ModelInfo
     */
    protected ModelInfo convertToModelInfo(MultiModelProperties.ModelConfig config) {
        ModelInfo info = new ModelInfo();
        info.setId((long) config.getName().hashCode());
        info.setName(config.getName());
        info.setDisplayName(config.getDisplayName());
        info.setMaxTokens(config.getMaxTokens());
        info.setTemperature(config.getTemperature());
        info.setSupportsThinking(config.isSupportsThinking());
        info.setSupportsStreaming(config.isSupportsStreaming());
        info.setAvailable(config.isEnabled() && isAvailable());
        info.setSortOrder(config.getSortOrder());
        return info;
    }

    /**
     * 获取模型配置
     */
    protected Optional<MultiModelProperties.ModelConfig> getModelConfig(String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(getProviderName());
        
        if (providerConfig == null) {
            return Optional.empty();
        }

        return providerConfig.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .findFirst();
    }

    /**
     * 获取默认温度参数
     */
    protected double getDefaultTemperature(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::getTemperature)
                .map(BigDecimal::doubleValue)
                .orElse(multiModelProperties.getDefaults().getTemperature().doubleValue());
    }

    /**
     * 获取默认最大token数
     */
    protected int getDefaultMaxTokens(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::getMaxTokens)
                .orElse(multiModelProperties.getDefaults().getMaxTokens());
    }
}