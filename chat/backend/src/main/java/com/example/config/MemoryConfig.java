package com.example.config;

import com.example.mapper.MessageMapper;
import com.example.memory.DatabaseChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

/**
 * 基于数据库的对话记忆配置（Spring AI 1.0.0）
 */
@Configuration
public class MemoryConfig {

    @Bean
    public ChatMemory chatMemory(MessageMapper messageMapper) {
        return new DatabaseChatMemory(messageMapper);
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        // 使用自定义的参数键提取 conversationId
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}
