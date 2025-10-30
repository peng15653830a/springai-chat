package com.example.config;

import com.example.integration.ai.greatwall.GreatWallChatApi;
import com.example.integration.ai.greatwall.GreatWallChatModel;
import com.example.integration.ai.greatwall.GreatWallChatOptions;
import com.example.util.ModelConfigHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
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

  /** 创建GreatWall ChatModel Bean */
  @Bean
  @ConditionalOnMissingBean(name = "greatwallChatModel")
  public ChatModel greatwallChatModel(
      GreatWallChatApi greatWallChatApi, MultiModelProperties multiModelProperties) {
    log.info("🏗️ 创建长城大模型 ChatModel Bean");

    // 获取默认长城模型配置
    MultiModelProperties.ModelConfig modelConfig =
        ModelConfigHelper.getDefaultModelConfig(multiModelProperties, "greatwall");

    // 长城大模型默认不启用推理
    GreatWallChatOptions defaultOptions =
        GreatWallChatOptions.builder()
            .model(modelConfig != null ? modelConfig.getName() : "greatwall-chat")
            .temperature(ModelConfigHelper.getTemperature(modelConfig, multiModelProperties))
            .maxTokens(ModelConfigHelper.getMaxTokens(modelConfig, multiModelProperties))
            .enableThinking(false)
            .build();

    return new GreatWallChatModel(greatWallChatApi, defaultOptions);
  }
}
