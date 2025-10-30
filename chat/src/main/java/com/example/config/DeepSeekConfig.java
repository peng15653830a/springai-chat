package com.example.config;

import com.example.util.ModelConfigHelper;
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
 * DeepSeek模型自动配置类 - 使用OpenAI兼容接口
 *
 * @author xupeng
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "ai.models.providers.deepseek.enabled", havingValue = "true")
public class DeepSeekConfig {

  /** 创建DeepSeek ChatModel Bean - 使用OpenAI兼容实现 */
  @Bean
  @ConditionalOnMissingBean(name = "deepseekChatModel")
  public ChatModel deepseekChatModel(MultiModelProperties multiModelProperties) {
    log.info("🏗️ 创建DeepSeek ChatModel Bean（基于OpenAI兼容API）");

    // 获取DeepSeek配置
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get("deepseek");
    String apiKey = multiModelProperties.getApiKey("deepseek");
    MultiModelProperties.ModelConfig modelConfig =
        ModelConfigHelper.getDefaultModelConfig(multiModelProperties, "deepseek");

    // 如果模型未找到，返回null而不是抛出异常
    if (modelConfig == null) {
      log.warn("未找到DeepSeek的模型配置");
      return null;
    }

    try {
      // 使用Builder模式创建OpenAI API客户端，指向DeepSeek端点
      OpenAiApi openAiApi =
          OpenAiApi.builder().baseUrl(providerConfig.getBaseUrl()).apiKey(apiKey).build();

      // 使用Builder模式创建ChatModel
      return OpenAiChatModel.builder()
          .openAiApi(openAiApi)
          .defaultOptions(
              OpenAiChatOptions.builder()
                  .model(modelConfig.getName())
                  .temperature(ModelConfigHelper.getTemperature(modelConfig, multiModelProperties))
                  .maxTokens(ModelConfigHelper.getMaxTokens(modelConfig, multiModelProperties))
                  .build())
          .build();

    } catch (Exception e) {
      log.error("创建DeepSeek ChatModel失败: {}", e.getMessage());
      throw new RuntimeException("Failed to create DeepSeek ChatModel", e);
    }
  }
}
