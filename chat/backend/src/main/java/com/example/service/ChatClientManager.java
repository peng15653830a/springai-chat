package com.example.service;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChatClient统一管理器
 * 利用Spring容器自动管理所有ChatClient实例，极简实现
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class ChatClientManager {

    @Autowired
    private Map<String, ChatClient> chatClients;
    
    @Autowired
    private MultiModelProperties properties;
    
    @PostConstruct
    public void initialize() {
        log.info("🚀 ChatClientManager初始化完成，发现ChatClient: {}", chatClients.keySet());
    }

    /**
     * 根据提供者名称获取ChatClient
     */
    public ChatClient getChatClient(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider名称不能为空");
        }

        String beanName = provider.toLowerCase() + "ChatClient";
        ChatClient client = chatClients.get(beanName);
        
        if (client == null) {
            // 尝试精确匹配
            client = chatClients.values().stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
                
            if (client == null) {
                throw new IllegalArgumentException("未找到支持的ChatClient: " + provider + 
                    "，可用的ChatClient: " + chatClients.keySet());
            }
            
            log.warn("⚠️ 未找到精确匹配的ChatClient: {}，使用默认客户端", beanName);
        }

        return client;
    }

    /**
     * 检查提供者是否可用
     */
    public boolean isAvailable(String provider) {
        try {
            ChatClient client = getChatClient(provider);
            return client != null;
        } catch (Exception e) {
            log.debug("检查提供者 {} 可用性失败: {}", provider, e.getMessage());
            return false;
        }
    }

    /**
     * 获取指定提供者的所有模型信息
     */
    public List<ModelInfo> getModels(String provider) {
        MultiModelProperties.ProviderConfig config = properties.getProviders().get(provider);
        
        if (config == null || !config.isEnabled()) {
            return Collections.emptyList();
        }

        return config.getModels().stream()
                .filter(MultiModelProperties.ModelConfig::isEnabled)
                .map(this::convertToModelInfo)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用的提供者名称
     */
    public List<String> getAvailableProviders() {
        return properties.getProviders().entrySet().stream()
                .filter(entry -> entry.getValue().isEnabled())
                .filter(entry -> isAvailable(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取模型信息
     */
    public ModelInfo getModelInfo(String provider, String modelName) {
        MultiModelProperties.ProviderConfig config = properties.getProviders().get(provider);
        
        if (config == null) {
            return null;
        }

        return config.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .map(this::convertToModelInfo)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查模型是否支持思考模式
     */
    public boolean supportsThinking(String provider, String modelName) {
        return getModelConfig(provider, modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsThinking)
                .orElse(false);
    }

    /**
     * 检查模型是否支持流式输出
     */
    public boolean supportsStreaming(String provider, String modelName) {
        return getModelConfig(provider, modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsStreaming)
                .orElse(true);
    }

    /**
     * 将模型配置转换为ModelInfo
     */
    private ModelInfo convertToModelInfo(MultiModelProperties.ModelConfig config) {
        ModelInfo info = new ModelInfo();
        info.setId((long) config.getName().hashCode());
        info.setName(config.getName());
        info.setDisplayName(config.getDisplayName());
        info.setMaxTokens(config.getMaxTokens());
        info.setTemperature(config.getTemperature());
        info.setSupportsThinking(config.isSupportsThinking());
        info.setSupportsStreaming(config.isSupportsStreaming());
        info.setAvailable(config.isEnabled());
        info.setSortOrder(config.getSortOrder());
        return info;
    }

    /**
     * 获取模型配置
     */
    private Optional<MultiModelProperties.ModelConfig> getModelConfig(String provider, String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = properties.getProviders().get(provider);
        
        if (providerConfig == null) {
            return Optional.empty();
        }

        return providerConfig.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .findFirst();
    }
}