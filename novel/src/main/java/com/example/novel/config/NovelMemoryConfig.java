package com.example.novel.config;

import com.example.novel.memory.NovelDatabaseChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Novel模块的会话记忆配置
 */
@Configuration
@RequiredArgsConstructor
public class NovelMemoryConfig {

  private final NovelDatabaseChatMemory novelDatabaseChatMemory;

  @Bean
  public ChatMemory novelChatMemory() {
    return novelDatabaseChatMemory;
  }

  @Bean
  public MessageChatMemoryAdvisor novelMessageChatMemoryAdvisor() {
    return MessageChatMemoryAdvisor.builder(novelDatabaseChatMemory).build();
  }
}
