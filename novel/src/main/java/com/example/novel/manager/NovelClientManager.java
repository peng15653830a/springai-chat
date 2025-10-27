package com.example.novel.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Novel 模块的 ChatClient 管理器
 * 委托给UnifiedChatClientManager统一管理，避免重复实现
 * 
 * @deprecated 建议直接使用 {@link com.example.client.UnifiedChatClientManager}
 */
@Slf4j
@Component
@Deprecated
public class NovelClientManager {

  @Autowired(required = false)
  private com.example.client.UnifiedChatClientManager unifiedChatClientManager;

  public ChatClient getChatClient(String provider) {
    if (unifiedChatClientManager != null) {
      log.debug("使用 UnifiedChatClientManager 获取 ChatClient: provider={}", provider);
      return unifiedChatClientManager.getChatClient(provider);
    }

    throw new UnsupportedOperationException(
        "UnifiedChatClientManager未注入，无法创建ChatClient。请检查配置。");
  }
}

