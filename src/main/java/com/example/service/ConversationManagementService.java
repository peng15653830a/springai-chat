package com.example.service;

import com.example.entity.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 对话管理服务
 *
 * @author xupeng
 */
@Slf4j
@Service
public class ConversationManagementService {

  @Autowired private ConversationService conversationService;

  /**
   * 生成对话标题（如果需要）
   *
   * @param conversationId 会话ID
   * @param userMessage 用户消息
   * @return 完成的响应式流
   */
  public Mono<Void> generateTitleIfNeeded(Long conversationId, String userMessage) {
    return Mono.fromCallable(() -> conversationService.getConversationById(conversationId))
        .filter(this::needsTitle)
        .flatMap(conversation -> generateAndUpdateTitle(conversationId, userMessage))
        .then()
        .doOnSuccess(v -> log.debug("对话标题生成完成，会话ID: {}", conversationId))
        .onErrorResume(error -> {
          log.error("生成对话标题时发生异常，会话ID: {}", conversationId, error);
          return Mono.empty();
        });
  }

  /**
   * 检查是否需要生成标题
   */
  private boolean needsTitle(Conversation conversation) {
    return conversation.getTitle() == null || 
           conversation.getTitle().trim().isEmpty() || 
           "新对话".equals(conversation.getTitle().trim());
  }

  /**
   * 生成并更新标题
   */
  private Mono<Void> generateAndUpdateTitle(Long conversationId, String userMessage) {
    return Mono.fromCallable(() -> {
          String newTitle = generateTitleFromMessage(userMessage.trim());
          conversationService.updateConversationTitle(conversationId, newTitle);
          log.debug("自动生成对话标题成功，会话ID: {}, 标题: {}", conversationId, newTitle);
          return null;
        });
  }

  /**
   * 从用户消息生成简洁标题
   */
  private String generateTitleFromMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      return "新对话";
    }
    
    String cleanMessage = message.trim();
    
    // 如果消息很短（20字以内），直接使用
    if (cleanMessage.length() <= 20) {
      return cleanMessage;
    }
    
    // 尝试找到第一句话
    String[] sentences = cleanMessage.split("[。！？\n]");
    if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
      String firstSentence = sentences[0].trim();
      if (firstSentence.length() <= 25) {
        return firstSentence;
      }
    }
    
    // 智能截取
    if (cleanMessage.length() > 20) {
      String truncated = cleanMessage.substring(0, Math.min(20, cleanMessage.length()));
      // 如果截断位置不是标点，尝试找到合适的截断点
      if (cleanMessage.length() > 20 && !truncated.matches(".*[。！？，、；：]$")) {
        for (int i = Math.min(18, truncated.length() - 1); i >= 10; i--) {
          char c = truncated.charAt(i);
          if (c == '，' || c == '、' || c == '；' || c == '：') {
            return truncated.substring(0, i + 1);
          }
        }
      }
      return truncated + "...";
    }
    
    return cleanMessage;
  }
}