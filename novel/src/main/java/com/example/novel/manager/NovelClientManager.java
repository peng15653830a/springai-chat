package com.example.novel.manager;

import com.example.service.factory.ModelProviderFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * Novel 模块的 ChatClient 管理器：按 provider 懒加载并缓存 ChatClient。
 * 使用 ModelProviderFactory 解析具体的 ChatModel。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NovelClientManager {

  private final ModelProviderFactory modelProviderFactory;
  private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;

  private final Map<String, ChatClient> cache = new ConcurrentHashMap<>();

  public ChatClient getChatClient(String provider) {
    if (provider == null || provider.isBlank()) {
      throw new IllegalArgumentException("Provider不能为空");
    }
    return cache.computeIfAbsent(provider, this::createClient);
  }

  private ChatClient createClient(String provider) {
    ChatModel chatModel = modelProviderFactory.getChatModel(provider);
    String systemPrompt = buildSystemPrompt();
    log.info("Novel: 创建 ChatClient for provider={}", provider);
    return ChatClient
        .builder(chatModel)
        .defaultSystem(systemPrompt)
        .defaultAdvisors(messageChatMemoryAdvisor)
        .build();
  }

  private String buildSystemPrompt() {
    return ("""
你是一个专业的长文本创作助手，擅长小说、剧本、散文等各类文学创作。

核心能力：
- 故事构思、文本创作、素材整合、风格模仿

创作原则：
- 自然流畅、适度分段、细节丰富、逻辑连贯

交互方式：
- 大纲→结构化规划；续写→延续风格与情节；润色→保持原意优化表达
""").trim();
  }
}

