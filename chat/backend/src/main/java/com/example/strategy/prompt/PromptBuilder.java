package com.example.strategy.prompt;

import reactor.core.publisher.Mono;

/**
 * 提示词构建服务接口 负责根据对话历史和搜索上下文构建AI聊天提示
 *
 * @author xupeng
 */
public interface PromptBuilder {

  /**
   * 构建聊天提示词
   *
   * @param conversationId 对话ID
   * @param userMessage 用户消息
   * @param searchEnabled 是否启用搜索
   * @return 构建好的提示词
   */
  Mono<String> buildPrompt(Long conversationId, String userMessage, boolean searchEnabled);

}
