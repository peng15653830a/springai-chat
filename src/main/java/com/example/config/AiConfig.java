package com.example.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AI相关配置类 - 重构为支持多模型ChatClient配置
 *
 * @author xupeng
 */
@Slf4j
@Data
@Component
@Configuration
public class AiConfig {

    private final MultiModelProperties multiModelProperties;

    public AiConfig(MultiModelProperties multiModelProperties) {
        this.multiModelProperties = multiModelProperties;
    }

    /**
     * 创建ChatClient工厂Bean
     * 负责为不同的模型提供者创建ChatClient实例
     */
    @Bean
    public ChatClientFactory chatClientFactory() {
        return new ChatClientFactory(multiModelProperties);
    }

    /**
     * ChatClient工厂类 - 管理多个模型的ChatClient实例
     */
    public static class ChatClientFactory {
        
        private final MultiModelProperties multiModelProperties;
        private final Map<String, ChatClient> chatClientCache = new HashMap<>();

        public ChatClientFactory(MultiModelProperties multiModelProperties) {
            this.multiModelProperties = multiModelProperties;
        }

        /**
         * 获取指定提供者和模型的ChatClient
         */
        public ChatClient getChatClient(String providerName, String modelName) {
            String key = providerName + ":" + modelName;
            return chatClientCache.computeIfAbsent(key, k -> createChatClient(providerName, modelName));
        }

        /**
         * 创建ChatClient实例
         */
        private ChatClient createChatClient(String providerName, String modelName) {
            log.info("🏗️ 创建ChatClient实例: {} - {}", providerName, modelName);
            
            MultiModelProperties.ProviderConfig providerConfig = 
                multiModelProperties.getProviders().get(providerName);
            
            if (providerConfig == null || !providerConfig.isEnabled()) {
                throw new IllegalArgumentException("Provider not available: " + providerName);
            }

            String apiKey = multiModelProperties.getApiKey(providerName);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API key not found for provider: " + providerName);
            }

            // 获取模型配置
            MultiModelProperties.ModelConfig modelConfig = getModelConfig(providerName, modelName);
            
            try {
                // 创建OpenAI兼容的API客户端（大部分模型都兼容OpenAI格式）
                OpenAiApi openAiApi = new OpenAiApi(providerConfig.getBaseUrl(), apiKey);

                // 创建ChatModel
                OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, 
                    OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(getTemperature(modelConfig))
                        .maxTokens(getMaxTokens(modelConfig))
                        .build());

                // 创建ChatClient
                return ChatClient.builder(chatModel)
                    .defaultSystem("你是一个有用的AI助手。")
                    .build();
                    
            } catch (Exception e) {
                log.error("创建ChatClient失败: {}", e.getMessage());
                throw new RuntimeException("Failed to create ChatClient for " + providerName + ":" + modelName, e);
            }
        }

        /**
         * 获取模型配置
         */
        private MultiModelProperties.ModelConfig getModelConfig(String providerName, String modelName) {
            return multiModelProperties.getProviders().get(providerName)
                .getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .findFirst()
                .orElse(null);
        }

        /**
         * 获取温度参数
         */
        private Double getTemperature(MultiModelProperties.ModelConfig modelConfig) {
            if (modelConfig != null && modelConfig.getTemperature() != null) {
                return modelConfig.getTemperature().doubleValue();
            }
            return multiModelProperties.getDefaults().getTemperature().doubleValue();
        }

        /**
         * 获取最大token数
         */
        private Integer getMaxTokens(MultiModelProperties.ModelConfig modelConfig) {
            if (modelConfig != null && modelConfig.getMaxTokens() != null) {
                return modelConfig.getMaxTokens();
            }
            return multiModelProperties.getDefaults().getMaxTokens();
        }
    }
}