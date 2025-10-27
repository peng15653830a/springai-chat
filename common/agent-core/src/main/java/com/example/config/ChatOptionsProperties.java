package com.example.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ChatClient选项配置属性
 * 支持按provider差异化配置temperature、maxTokens、topP和system prompt
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.chat.options")
public class ChatOptionsProperties {

  private Map<String, ProviderOptions> providers = new HashMap<>();

  @Data
  public static class ProviderOptions {
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private Double topP = 0.9;
    private String systemPrompt;
  }

  public ProviderOptions getProviderOptions(String provider) {
    return providers.getOrDefault(provider.toLowerCase(), new ProviderOptions());
  }
}
