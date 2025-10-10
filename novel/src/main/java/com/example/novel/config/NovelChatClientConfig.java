package com.example.novel.config;

import com.example.strategy.prompt.DefaultPromptBuilder;
import com.example.strategy.prompt.PromptBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Novel模块提示词构建配置。
 */
@Configuration
public class NovelChatClientConfig {

  @Bean
  public PromptBuilder promptBuilder() {
    return new DefaultPromptBuilder();
  }
}
