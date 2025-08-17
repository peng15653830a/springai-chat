package com.example.service.impl;

import com.example.entity.Message;
import com.example.service.*;
import com.example.service.dto.SseEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.service.constants.AiChatConstants.ROLE_ASSISTANT;
import static com.example.service.constants.AiChatConstants.ROLE_USER;

/**
 * AI聊天服务实现类（纯响应式架构）
 *
 * @author xupeng
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

  @Autowired private ChatStreamService chatStreamService;
  @Autowired private SearchIntegrationService searchIntegrationService;
  @Autowired private ConversationManagementService conversationManagementService;
  @Autowired private MessagePersistenceService messagePersistenceService;

  @Override
  public Flux<SseEventResponse> streamChat(Long conversationId, String userMessage, boolean searchEnabled, boolean deepThinking) {
    log.info("开始响应式流式聊天，会话ID: {}, 消息长度: {}, 搜索开启: {}, 深度思考: {}", 
        conversationId, userMessage.length(), searchEnabled, deepThinking);

    return Flux.concat(
        // 1. 保存用户消息并生成标题
        saveUserMessageAndGenerateTitle(conversationId, userMessage),
        
        // 2. 执行搜索（如果启用）
        performSearchStep(userMessage, searchEnabled),
        
        // 3. 构建提示并执行流式聊天
        buildPromptAndStreamChat(conversationId, userMessage, searchEnabled, deepThinking)
    )
    .onErrorResume(error -> {
      log.error("流式聊天过程中发生错误，会话ID: {}", conversationId, error);
      return Flux.just(SseEventResponse.error("聊天服务暂时不可用，请稍后重试"));
    });
  }

  @Override
  public Mono<Message> saveUserMessage(Long conversationId, String content) {
    return messagePersistenceService.saveUserMessage(conversationId, content)
        .doOnNext(message -> {
          // 异步生成标题
          conversationManagementService.generateTitleIfNeeded(conversationId, content)
              .subscribe();
        });
  }

  /**
   * 保存用户消息并生成标题
   */
  private Flux<SseEventResponse> saveUserMessageAndGenerateTitle(Long conversationId, String userMessage) {
    return messagePersistenceService.saveUserMessage(conversationId, userMessage)
        .doOnNext(message -> {
          // 异步生成标题，不阻塞主流程
          conversationManagementService.generateTitleIfNeeded(conversationId, userMessage)
              .subscribe();
        })
        .then(Mono.<SseEventResponse>empty())
        .flux();
  }

  /**
   * 执行搜索步骤
   */
  private Flux<SseEventResponse> performSearchStep(String userMessage, boolean searchEnabled) {
    return searchIntegrationService.performSearchIfEnabled(userMessage, searchEnabled)
        .flatMapMany(SearchIntegrationService.SearchContextResult::getSearchEvents);
  }

  /**
   * 构建提示并执行流式聊天
   */
  private Flux<SseEventResponse> buildPromptAndStreamChat(Long conversationId, String userMessage, 
                                                        boolean searchEnabled, boolean deepThinking) {
    return Mono.zip(
        messagePersistenceService.getConversationHistory(conversationId),
        searchIntegrationService.performSearchIfEnabled(userMessage, searchEnabled)
    )
    .flatMapMany(tuple -> {
      List<Message> history = tuple.getT1();
      String searchContext = tuple.getT2().getSearchContext();
      
      String fullPrompt = buildFullPrompt(userMessage, searchContext, history);
      return chatStreamService.executeStreamingChat(fullPrompt, conversationId, deepThinking);
    });
  }

  /**
   * 构建完整的提示文本
   */
  private String buildFullPrompt(String userMessage, String searchContext, List<Message> history) {
    StringBuilder prompt = new StringBuilder();
    
    // 添加搜索上下文（如果有）
    if (searchContext != null && !searchContext.trim().isEmpty()) {
      prompt.append("基于以下搜索结果回答用户问题：\n").append(searchContext).append("\n\n");
    }
    
    // 添加历史对话（最近10条）
    if (history != null && !history.isEmpty()) {
      int startIndex = Math.max(0, history.size() - 10);
      for (int i = startIndex; i < history.size(); i++) {
        Message msg = history.get(i);
        if (ROLE_USER.equals(msg.getRole())) {
          prompt.append("用户: ").append(msg.getContent()).append("\n");
        } else if (ROLE_ASSISTANT.equals(msg.getRole())) {
          prompt.append("助手: ").append(msg.getContent()).append("\n");
        }
      }
      prompt.append("\n");
    }
    
    // 添加当前用户消息
    prompt.append("用户: ").append(userMessage);
    
    return prompt.toString();
  }
}