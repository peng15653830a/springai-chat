package com.example.strategy.prompt;

import com.example.entity.Message;
import com.example.service.MessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 默认的提示词构建服务实现
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultPromptBuilder implements PromptBuilder {

  private final MessageService messageService;

  @Override
  public Mono<String> buildPrompt(Long conversationId, String userMessage, boolean searchEnabled) {
    log.debug("构建用户消息，对话ID: {}，搜索开启: {}（由Tool Calling自动处理）", conversationId, searchEnabled);

    // 由于历史消息已由MessageChatMemoryAdvisor自动注入，这里只返回用户消息
    // 系统提示由ChatClientManager的defaultSystem注入
    return Mono.just(userMessage);
  }

}
