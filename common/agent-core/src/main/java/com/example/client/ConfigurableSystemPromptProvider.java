package com.example.client;

import com.example.config.ChatOptionsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于配置的SystemPromptProvider实现
 * 从ChatOptionsProperties读取system prompt配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigurableSystemPromptProvider implements SystemPromptProvider {

  private final ChatOptionsProperties chatOptionsProperties;

  @Override
  public String getSystemPrompt(String provider) {
    ChatOptionsProperties.ProviderOptions options =
        chatOptionsProperties.getProviderOptions(provider);

    String prompt = options.getSystemPrompt();
    if (prompt == null || prompt.isBlank()) {
      log.debug("Provider {} 没有配置专用system prompt，使用默认值", provider);
      return getDefaultSystemPrompt();
    }

    return prompt.trim();
  }
}
