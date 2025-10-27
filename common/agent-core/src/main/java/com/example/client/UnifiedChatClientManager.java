package com.example.client;

import com.example.service.factory.ModelProviderFactory;
import com.example.stream.ClientManager;
import com.example.stream.springai.ChatClientResolver;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 统一的ChatClient管理器
 * 
 * <p>职责：
 * <ul>
 *   <li>懒加载创建并缓存ChatClient实例</li>
 *   <li>从ModelProviderFactory获取ChatModel</li>
 *   <li>从SystemPromptProvider获取system prompt</li>
 *   <li>注入MessageChatMemoryAdvisor和SimpleLoggerAdvisor</li>
 * </ul>
 * 
 * <p>替代：chat模块的ChatClientManager和novel模块的NovelClientManager
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "chatClientManager")
public class UnifiedChatClientManager implements ChatClientResolver, ClientManager {

  private final ModelProviderFactory modelProviderFactory;
  private final SystemPromptProvider systemPromptProvider;
  private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;

  @Autowired(required = false)
  private SimpleLoggerAdvisor simpleLoggerAdvisor;

  private final Map<String, ChatClient> cache = new ConcurrentHashMap<>();

  @Override
  public ChatClient resolve(String provider) {
    return getChatClient(provider);
  }

  @Override
  public ChatClient getChatClient(String provider) {
    if (provider == null || provider.isBlank()) {
      throw new IllegalArgumentException("Provider不能为空");
    }
    return cache.computeIfAbsent(provider, this::createChatClient);
  }

  private ChatClient createChatClient(String provider) {
    ChatModel chatModel = modelProviderFactory.getChatModel(provider);
    String systemPrompt = systemPromptProvider.getSystemPrompt(provider);

    log.info("🔧 创建ChatClient: provider={}", provider);

    ChatClient.Builder builder =
        ChatClient.builder(chatModel).defaultSystem(systemPrompt);

    if (simpleLoggerAdvisor != null) {
      builder.defaultAdvisors(simpleLoggerAdvisor, messageChatMemoryAdvisor);
      log.debug("已注入 SimpleLoggerAdvisor 和 MessageChatMemoryAdvisor");
    } else {
      builder.defaultAdvisors(messageChatMemoryAdvisor);
      log.debug("已注入 MessageChatMemoryAdvisor");
    }

    ChatClient client = builder.build();
    log.info("✅ ChatClient创建完成: provider={}", provider);
    return client;
  }

  @Override
  public boolean isAvailable(String provider) {
    try {
      getChatClient(provider);
      return true;
    } catch (Exception e) {
      log.debug("Provider {} 不可用: {}", provider, e.getMessage());
      return false;
    }
  }

  @Override
  public List<String> getAvailableProviders() {
    return modelProviderFactory.getAvailableProviders();
  }
}
