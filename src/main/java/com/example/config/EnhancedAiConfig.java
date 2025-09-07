package com.example.config;

import com.example.ai.chat.DeepSeekChatModel;
import com.example.ai.chat.DeepSeekChatOptions;
import com.example.ai.chat.GreatWallChatModel;
import com.example.ai.chat.GreatWallChatOptions;
import com.example.service.api.impl.DeepSeekApiClient;
import com.example.service.api.impl.GreatWallApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * 增强版AI配置类
 * 支持自定义ChatModel实现（长城大模型、DeepSeek等）
 *
 * @author xupeng
 */
@Slf4j
@Configuration
public class EnhancedAiConfig {

    private final MultiModelProperties multiModelProperties;
    private final GreatWallApiClient greatWallApiClient;
    private final DeepSeekApiClient deepSeekApiClient;

    public EnhancedAiConfig(MultiModelProperties multiModelProperties,
                           GreatWallApiClient greatWallApiClient,
                           DeepSeekApiClient deepSeekApiClient) {
        this.multiModelProperties = multiModelProperties;
        this.greatWallApiClient = greatWallApiClient;
        this.deepSeekApiClient = deepSeekApiClient;
    }

    /**
     * 创建增强版ChatClient工厂Bean
     */
    @Bean
    @Primary
    public EnhancedChatClientFactory enhancedChatClientFactory() {
        return new EnhancedChatClientFactory(
            multiModelProperties,
            greatWallApiClient,
            deepSeekApiClient
        );
    }

    /**
     * 增强版ChatClient工厂类
     * 支持自定义ChatModel和标准OpenAI ChatModel
     */
    public static class EnhancedChatClientFactory {
        
        private final MultiModelProperties multiModelProperties;
        private final GreatWallApiClient greatWallApiClient;
        private final DeepSeekApiClient deepSeekApiClient;
        private final Map<String, ChatClient> chatClientCache = new HashMap<>();
        private final Map<String, ChatModel> chatModelCache = new HashMap<>();

        public EnhancedChatClientFactory(MultiModelProperties multiModelProperties,
                                       GreatWallApiClient greatWallApiClient,
                                       DeepSeekApiClient deepSeekApiClient) {
            this.multiModelProperties = multiModelProperties;
            this.greatWallApiClient = greatWallApiClient;
            this.deepSeekApiClient = deepSeekApiClient;
        }

        /**
         * 获取指定提供者和模型的ChatClient
         */
        public ChatClient getChatClient(String providerName, String modelName) {
            String key = providerName + ":" + modelName;
            return chatClientCache.computeIfAbsent(key, k -> createChatClient(providerName, modelName));
        }

        /**
         * 获取指定提供者和模型的ChatModel
         */
        public ChatModel getChatModel(String providerName, String modelName) {
            String key = providerName + ":" + modelName;
            return chatModelCache.computeIfAbsent(key, k -> createChatModel(providerName, modelName));
        }

        /**
         * 创建ChatClient实例
         */
        private ChatClient createChatClient(String providerName, String modelName) {
            log.info("🏗️ 创建增强版ChatClient实例: {} - {}", providerName, modelName);
            
            ChatModel chatModel = createChatModel(providerName, modelName);
            
            return ChatClient.builder(chatModel)
                    .defaultSystem("你是一个有用的AI助手。")
                    .build();
        }

        /**
         * 创建ChatModel实例
         */
        private ChatModel createChatModel(String providerName, String modelName) {
            log.info("🏗️ 创建ChatModel实例: {} - {}", providerName, modelName);
            
            MultiModelProperties.ProviderConfig providerConfig = 
                multiModelProperties.getProviders().get(providerName);
            
            if (providerConfig == null || !providerConfig.isEnabled()) {
                throw new IllegalArgumentException("Provider not available: " + providerName);
            }

            String apiKey = multiModelProperties.getApiKey(providerName);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API key not found for provider: " + providerName);
            }

            // 根据提供者类型创建对应的ChatModel
            switch (providerName.toLowerCase()) {
                case "greatwall":
                    return createGreatWallChatModel(modelName);
                    
                case "deepseek":
                    return createDeepSeekChatModel(modelName);
                    
                case "openai":
                case "qwen":
                case "kimi2":
                default:
                    // 标准OpenAI兼容模型
                    return createOpenAiCompatibleChatModel(providerName, modelName);
            }
        }

        /**
         * 创建长城大模型ChatModel
         */
        private ChatModel createGreatWallChatModel(String modelName) {
            MultiModelProperties.ModelConfig modelConfig = getModelConfig("greatwall", modelName);
            
            GreatWallChatOptions defaultOptions = GreatWallChatOptions.builder()
                .model(modelName)
                .temperature(getTemperature(modelConfig))
                .maxTokens(getMaxTokens(modelConfig))
                .enableThinking(false) // 长城大模型默认不启用推理
                .build();

            return new GreatWallChatModel(greatWallApiClient, defaultOptions);
        }

        /**
         * 创建DeepSeek推理模型ChatModel
         */
        private ChatModel createDeepSeekChatModel(String modelName) {
            MultiModelProperties.ModelConfig modelConfig = getModelConfig("DeepSeek", modelName);
            
            DeepSeekChatOptions defaultOptions = DeepSeekChatOptions.builder()
                .model(modelName)
                .temperature(getTemperature(modelConfig))
                .maxTokens(getMaxTokens(modelConfig))
                .enableThinking(false) // 默认不启用推理，由调用方决定
                .thinkingBudget(modelConfig.getThinkingBudget())
                .build();

            return new DeepSeekChatModel(deepSeekApiClient, defaultOptions);
        }

        /**
         * 创建OpenAI兼容模型ChatModel
         */
        private ChatModel createOpenAiCompatibleChatModel(String providerName, String modelName) {
            MultiModelProperties.ProviderConfig providerConfig = 
                multiModelProperties.getProviders().get(providerName);
            String apiKey = multiModelProperties.getApiKey(providerName);
            MultiModelProperties.ModelConfig modelConfig = getModelConfig(providerName, modelName);
            
            try {
                // 使用Builder模式创建OpenAI API客户端
                OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(providerConfig.getBaseUrl())
                    .apiKey(apiKey)
                    .build();

                // 使用Builder模式创建ChatModel
                return OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(getTemperature(modelConfig))
                        .maxTokens(getMaxTokens(modelConfig))
                        .build())
                    .build();
                    
            } catch (Exception e) {
                log.error("创建OpenAI兼容ChatModel失败: {}", e.getMessage());
                throw new RuntimeException("Failed to create ChatModel for " + providerName + ":" + modelName, e);
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