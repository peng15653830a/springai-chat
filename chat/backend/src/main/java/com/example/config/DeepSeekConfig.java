package com.example.config;

import com.example.integration.ai.deepseek.DeepSeekChatApi;
import com.example.integration.ai.deepseek.DeepSeekChatModel;
import com.example.integration.ai.deepseek.DeepSeekChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek模型自动配置类
 * 
 * @author xupeng
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "ai.models.providers.DeepSeek.enabled", havingValue = "true")
public class DeepSeekConfig {

    /**
     * 创建DeepSeek ChatModel Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "deepSeekChatModel")
    public ChatModel deepSeekChatModel(DeepSeekChatApi deepSeekChatApi,
                                       MultiModelProperties multiModelProperties) {
        log.info("🏗️ 创建DeepSeek ChatModel Bean");
        
        // 获取默认DeepSeek模型配置
        MultiModelProperties.ModelConfig modelConfig = getDefaultModelConfig(multiModelProperties, "DeepSeek");
        
        DeepSeekChatOptions defaultOptions = DeepSeekChatOptions.builder()
            .model(modelConfig != null ? modelConfig.getName() : "deepseek-chat")
            .temperature(getTemperature(modelConfig, multiModelProperties))
            .maxTokens(getMaxTokens(modelConfig, multiModelProperties))
            .enableThinking(false) // 默认不启用推理，由调用方决定
            .thinkingBudget(modelConfig != null ? modelConfig.getThinkingBudget() : null)
            .build();
            
        return new DeepSeekChatModel(deepSeekChatApi, defaultOptions);
    }
    
    /**
     * 创建DeepSeek ChatClient Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "deepSeekChatClient")
    public ChatClient deepSeekChatClient(@Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel) {
        log.info("🏗️ 创建DeepSeek ChatClient Bean");
        
        return ChatClient.builder(deepSeekChatModel)
                .defaultSystem("你是一个有用的AI助手。")
                .build();
    }
    
    /**
     * 获取默认模型配置
     */
    private MultiModelProperties.ModelConfig getDefaultModelConfig(MultiModelProperties multiModelProperties, String providerName) {
        MultiModelProperties.ProviderConfig providerConfig = multiModelProperties.getProviders().get(providerName);
        if (providerConfig == null || providerConfig.getModels() == null) {
            return null;
        }
        
        // 返回第一个模型作为默认模型
        return providerConfig.getModels().stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取温度参数
     */
    private Double getTemperature(MultiModelProperties.ModelConfig modelConfig, MultiModelProperties multiModelProperties) {
        if (modelConfig != null && modelConfig.getTemperature() != null) {
            return modelConfig.getTemperature().doubleValue();
        }
        return multiModelProperties.getDefaults().getTemperature().doubleValue();
    }
    
    /**
     * 获取最大token数
     */
    private Integer getMaxTokens(MultiModelProperties.ModelConfig modelConfig, MultiModelProperties multiModelProperties) {
        if (modelConfig != null && modelConfig.getMaxTokens() != null) {
            return modelConfig.getMaxTokens();
        }
        return multiModelProperties.getDefaults().getMaxTokens();
    }
}