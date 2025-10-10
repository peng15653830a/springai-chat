package com.example.strategy.prompt;

import reactor.core.publisher.Mono;

/**
 * 提示词构建服务接口。
 */
public interface PromptBuilder {
  Mono<String> buildPrompt(Long conversationId, String userMessage, boolean searchEnabled);
}

