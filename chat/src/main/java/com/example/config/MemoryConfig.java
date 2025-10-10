package com.example.config;

import com.example.mapper.MessageMapper;
import com.example.memory.DatabaseChatMemory;
import com.example.service.MessageToolResultService;
import com.example.strategy.prompt.DefaultPromptBuilder;
import com.example.strategy.prompt.PromptBuilder;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于数据库的对话记忆配置（Spring AI 1.0.0）
 *
 * @author xupeng
 */
@Configuration
public class MemoryConfig {

  @Bean
  public ChatMemory chatMemory(
      MessageMapper messageMapper, MessageToolResultService messageToolResultService) {
    return new DatabaseChatMemory(messageMapper, messageToolResultService);
  }

  @Bean
  public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
    // 使用默认配置，先让它能正常启动
    return MessageChatMemoryAdvisor.builder(chatMemory).build();
  }

  @Bean
  public PromptBuilder promptBuilder() {
    return new DefaultPromptBuilder();
  }
}
