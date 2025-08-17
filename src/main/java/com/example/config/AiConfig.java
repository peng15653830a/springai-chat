package com.example.config;

import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * AI相关配置类 统一管理所有AI模型相关的配置参数
 *
 * @author xupeng
 */
@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai")
public class AiConfig {

  /** API密钥 */
  private String apiKey;

  /** API基础URL */
  private String baseUrl;

  /** 模型名称 */
  private String model;

  /** 温度参数，控制回复的随机性 取值范围：0-2，默认0.7 */
  private double temperature = 0.7;

  /** 最大token数 */
  private int maxTokens = 1000;

  /** 请求超时时间（毫秒） */
  private int timeoutMs = 30000;

  /** 重试次数 */
  private int maxRetries = 3;

  /** 是否启用流式输出 */
  private boolean streamEnabled = false;

  /** HTTP请求头配置 */
  private HttpConfig http = new HttpConfig();

  /** HTTP相关配置 */
  @Data
  public static class HttpConfig {

    /** 连接超时时间（毫秒） */
    private int connectTimeoutMs = 10000;

    /** 读取超时时间（毫秒） */
    private int readTimeoutMs = 30000;

    /** 用户代理 */
    private String userAgent = "SpringAI-ChatBot/1.0";
  }

  /** 验证配置是否有效 */
  public boolean isValid() {
    return apiKey != null
        && !apiKey.trim().isEmpty()
        && baseUrl != null
        && !baseUrl.trim().isEmpty()
        && model != null
        && !model.trim().isEmpty();
  }

  /** 获取完整的聊天API URL */
  public String getChatApiUrl() {
    return baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
  }

  @Bean
  public ChatClient chatClient(ChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultSystem("你是一个有用的AI助手。")
        .build();
  }
}
