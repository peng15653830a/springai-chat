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

  @Override
  public String buildPromptFromMessages(
      List<Message> messages, String currentMessage, String searchContext) {
    // 兼容性方法：由于新架构下历史消息由Memory Advisor自动注入，搜索由Tool Calling处理
    // 这里只返回用户消息内容
    log.debug("兼容性方法调用，返回用户消息内容，长度: {} 字符", currentMessage != null ? currentMessage.length() : 0);
    return currentMessage;
  }

  @Override
  public String buildSystemPrompt() {
    // 系统提示改由 ChatClientManager.defaultSystem 注入
    return "你是一个有用的AI助理。";
  }

  @Override
  public String formatSearchContext(String searchResults) {
    // 已废弃：搜索现在由Spring AI Tool Calling自动处理
    log.debug("formatSearchContext方法已废弃，搜索由Tool Calling处理");
    return "";
  }
}
