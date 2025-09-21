package com.example.config;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

/** 测试环境ChatClient配置 提供模拟的ChatClient用于集成测试 */
@Configuration
@Profile("test")
public class TestChatClientConfig {

  @Bean
  @Primary
  public ChatModel testChatModel() {
    return new ChatModel() {
      @Override
      public ChatResponse call(Prompt prompt) {
        // 返回模拟的响应
        Generation generation = new Generation(new AssistantMessage("模拟AI响应"));
        return new ChatResponse(List.of(generation));
      }

      @Override
      public Flux<ChatResponse> stream(Prompt prompt) {
        // 返回模拟的流式响应
        Generation generation1 = new Generation(new AssistantMessage("模拟"));
        Generation generation2 = new Generation(new AssistantMessage("AI"));
        Generation generation3 = new Generation(new AssistantMessage("响应"));
        return Flux.just(
            new ChatResponse(List.of(generation1)),
            new ChatResponse(List.of(generation2)),
            new ChatResponse(List.of(generation3)));
      }
    };
  }

  @Bean
  @Primary
  public ChatClient testChatClient(ChatModel testChatModel) {
    return ChatClient.builder(testChatModel).defaultSystem("你是一个有用的AI助手。").build();
  }
}
