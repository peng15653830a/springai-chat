package com.example.config;

import com.example.integration.ai.greatwall.GreatWallChatApi;
import com.example.integration.ai.greatwall.GreatWallChatModel;
import com.example.integration.ai.greatwall.GreatWallChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 长城大模型自动配置类
 * 
 * @author xupeng
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "ai.models.providers.greatwall.enabled", havingValue = "true")
public class GreatWallConfig {

    /**
     * 创建GreatWall ChatModel Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "greatWallChatModel")
    public ChatModel greatWallChatModel(GreatWallChatApi greatWallChatApi,
                                        MultiModelProperties multiModelProperties) {
        log.info("🏗️ 创建长城大模型 ChatModel Bean");
        
        // 获取默认长城模型配置
        MultiModelProperties.ModelConfig modelConfig = getDefaultModelConfig(multiModelProperties, "greatwall");
        
        GreatWallChatOptions defaultOptions = GreatWallChatOptions.builder()
            .model(modelConfig != null ? modelConfig.getName() : "greatwall-chat")
            .temperature(getTemperature(modelConfig, multiModelProperties))
            .maxTokens(getMaxTokens(modelConfig, multiModelProperties))
            .enableThinking(false) // 长城大模型默认不启用推理
            .build();
            
        return new GreatWallChatModel(greatWallChatApi, defaultOptions);
    }
    
    /**
     * 创建长城大模型 ChatClient Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "greatWallChatClient")
    public ChatClient greatWallChatClient(@Qualifier("greatWallChatModel") ChatModel greatWallChatModel) {
        log.info("🏗️ 创建长城大模型 ChatClient Bean");
        
        return ChatClient.builder(greatWallChatModel)
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