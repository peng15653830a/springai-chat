package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI兼容模型自动配置类 处理OpenAI、通义千问、Kimi2等兼容模型
 *
 * @author xupeng
 */
@Slf4j
@Configuration
public class OpenAiCompatibleConfig {

  /** 创建自定义OpenAI ChatModel Bean（避免与Spring AI自动配置冲突） */
  @Bean
  @ConditionalOnProperty(name = "ai.models.providers.openai.enabled", havingValue = "true")
  @ConditionalOnMissingBean(name = "customOpenAiChatModel")
  public ChatModel customOpenAiChatModel(MultiModelProperties multiModelProperties) {
    log.info("🏗️ 创建自定义OpenAI ChatModel Bean");
    return createOpenAiCompatibleModel("openai", multiModelProperties);
  }

  /** 创建通义千问 ChatModel Bean */
  @Bean
  @ConditionalOnProperty(name = "ai.models.providers.qwen.enabled", havingValue = "true")
  @ConditionalOnMissingBean(name = "qwenChatModel")
  public ChatModel qwenChatModel(MultiModelProperties multiModelProperties) {
    log.info("🏗️ 创建通义千问 ChatModel Bean");
    return createOpenAiCompatibleModel("qwen", multiModelProperties);
  }

  /** 创建Kimi2 ChatModel Bean */
  @Bean
  @ConditionalOnProperty(name = "ai.models.providers.kimi2.enabled", havingValue = "true")
  @ConditionalOnMissingBean(name = "kimi2ChatModel")
  public ChatModel kimi2ChatModel(MultiModelProperties multiModelProperties) {
    log.info("🏗️ 创建Kimi2 ChatModel Bean");
    return createOpenAiCompatibleModel("kimi2", multiModelProperties);
  }

  /** 创建OpenAI兼容的ChatModel */
  private ChatModel createOpenAiCompatibleModel(
      String providerName, MultiModelProperties multiModelProperties) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName);
    String apiKey = multiModelProperties.getApiKey(providerName);
    MultiModelProperties.ModelConfig modelConfig =
        getDefaultModelConfig(providerName, multiModelProperties);

    // 如果模型未找到，返回null而不是抛出异常
    if (modelConfig == null) {
      log.warn("未找到提供者 {} 的模型配置", providerName);
      return null;
    }

    try {
      // 使用Builder模式创建OpenAI API客户端
      OpenAiApi openAiApi =
          OpenAiApi.builder().baseUrl(providerConfig.getBaseUrl()).apiKey(apiKey).build();

      // 使用Builder模式创建ChatModel
      return OpenAiChatModel.builder()
          .openAiApi(openAiApi)
          .defaultOptions(
              OpenAiChatOptions.builder()
                  .model(modelConfig.getName())
                  .temperature(getTemperature(modelConfig, multiModelProperties))
                  .maxTokens(getMaxTokens(modelConfig, multiModelProperties))
                  .build())
          .build();

    } catch (Exception e) {
      log.error("创建OpenAI兼容ChatModel失败: {}", e.getMessage());
      throw new RuntimeException("Failed to create ChatModel for " + providerName, e);
    }
  }

  /** 获取默认模型配置 */
  private MultiModelProperties.ModelConfig getDefaultModelConfig(
      String providerName, MultiModelProperties multiModelProperties) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName);
    if (providerConfig == null || providerConfig.getModels() == null) {
      return null;
    }

    return providerConfig.getModels().stream().findFirst().orElse(null);
  }

  /** 获取温度参数 */
  private Double getTemperature(
      MultiModelProperties.ModelConfig modelConfig, MultiModelProperties multiModelProperties) {
    if (modelConfig != null && modelConfig.getTemperature() != null) {
      return modelConfig.getTemperature().doubleValue();
    }
    return multiModelProperties.getDefaults().getTemperature().doubleValue();
  }

  /** 获取最大token数 */
  private Integer getMaxTokens(
      MultiModelProperties.ModelConfig modelConfig, MultiModelProperties multiModelProperties) {
    if (modelConfig != null && modelConfig.getMaxTokens() != null) {
      return modelConfig.getMaxTokens();
    }
    return multiModelProperties.getDefaults().getMaxTokens();
  }
}
