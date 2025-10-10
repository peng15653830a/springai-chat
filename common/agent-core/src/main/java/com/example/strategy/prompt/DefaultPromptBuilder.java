package com.example.strategy.prompt;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 默认的提示词构建服务实现
 *
 * 这是一个通用的实现，适用于大多数场景：
 * - 直接返回用户消息
 * - 历史消息由MessageChatMemoryAdvisor自动注入
 * - 系统提示由ChatClientManager的defaultSystem注入
 *
 * @author 系统自动整合
 */
@Slf4j
public class DefaultPromptBuilder implements PromptBuilder {

  @Override
  public Mono<String> buildPrompt(Long conversationId, String userMessage, boolean searchEnabled) {
    log.debug("构建用户消息，对话ID: {}，搜索开启: {}（由Tool Calling自动处理）", conversationId, searchEnabled);

    // 由于历史消息已由MessageChatMemoryAdvisor自动注入，这里只返回用户消息
    // 系统提示由ChatClientManager的defaultSystem注入
    return Mono.justOrEmpty(userMessage);
  }
}