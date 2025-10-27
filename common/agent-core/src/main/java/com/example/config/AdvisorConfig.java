package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Advisor配置
 * 提供SimpleLoggerAdvisor等通用advisor
 */
@Slf4j
@Configuration
public class AdvisorConfig {

  @Bean
  @ConditionalOnProperty(
      prefix = "spring.ai.chat.advisor.logger",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  public SimpleLoggerAdvisor simpleLoggerAdvisor() {
    log.info("✅ 启用 SimpleLoggerAdvisor 用于记录ChatClient请求/响应");
    return new SimpleLoggerAdvisor();
  }
}
