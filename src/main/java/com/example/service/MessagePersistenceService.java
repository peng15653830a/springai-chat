package com.example.service;

import com.example.entity.Message;
import com.example.service.dto.SseEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.service.constants.AiChatConstants.ROLE_ASSISTANT;
import static com.example.service.constants.AiChatConstants.ROLE_USER;

/**
 * 消息持久化服务
 *
 * @author xupeng
 */
@Slf4j
@Service
public class MessagePersistenceService {

  @Autowired private MessageService messageService;

  /**
   * 保存用户消息
   *
   * @param conversationId 会话ID
   * @param content 消息内容
   * @return 保存的用户消息
   */
  public Mono<Message> saveUserMessage(Long conversationId, String content) {
    return Mono.fromCallable(() -> {
          Message userMessage = messageService.saveMessage(conversationId, ROLE_USER, content);
          log.info("用户消息保存成功，消息ID: {}", userMessage.getId());
          return userMessage;
        })
        .onErrorMap(error -> {
          log.error("保存用户消息失败，会话ID: {}", conversationId, error);
          return new RuntimeException("保存用户消息失败: " + error.getMessage(), error);
        });
  }

  /**
   * 保存AI响应消息
   *
   * @param conversationId 会话ID
   * @param content AI响应内容
   * @param thinking 推理过程内容（可选）
   * @return 保存的AI消息和结束事件
   */
  public Mono<SseEventResponse> saveAiMessage(Long conversationId, String content, 
                                            String thinking) {
    return Mono.fromCallable(() -> {
          // 保存AI消息，包含thinking内容
          Message aiMessage = messageService.saveMessage(
              conversationId, ROLE_ASSISTANT, content, thinking, null);
          
          log.info("AI消息保存成功，消息ID: {}, thinking内容: {}", 
              aiMessage.getId(), thinking != null ? "有" : "无");
          return SseEventResponse.end(aiMessage.getId());
        })
        .onErrorMap(error -> {
          log.error("保存AI消息失败，会话ID: {}", conversationId, error);
          return new RuntimeException("保存AI消息失败: " + error.getMessage(), error);
        });
  }

  /**
   * 保存AI响应消息（带搜索结果）
   *
   * @param conversationId 会话ID
   * @param content AI响应内容
   * @param thinking 推理过程内容（可选）
   * @param searchResults 搜索结果（可选）
   * @return 保存的AI消息和结束事件
   */
  public Mono<SseEventResponse> saveAiMessageWithSearch(Long conversationId, String content, 
                                                      String thinking, List<?> searchResults) {
    return Mono.fromCallable(() -> {
          // 将搜索结果序列化为JSON（如果存在）
          String searchResultsJson = null;
          if (searchResults != null && !searchResults.isEmpty()) {
            try {
              searchResultsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                  .writeValueAsString(searchResults);
            } catch (Exception e) {
              log.warn("序列化搜索结果失败", e);
            }
          }
          
          // 保存AI消息，包含thinking和搜索结果
          Message aiMessage = messageService.saveMessage(
              conversationId, ROLE_ASSISTANT, content, thinking, searchResultsJson);
          
          log.info("AI消息保存成功，消息ID: {}, thinking: {}, 搜索结果: {}", 
              aiMessage.getId(), thinking != null ? "有" : "无", searchResults != null ? "有" : "无");
          return SseEventResponse.end(aiMessage.getId());
        })
        .onErrorMap(error -> {
          log.error("保存AI消息失败，会话ID: {}", conversationId, error);
          return new RuntimeException("保存AI消息失败: " + error.getMessage(), error);
        });
  }

  /**
   * 获取会话历史消息
   *
   * @param conversationId 会话ID
   * @return 历史消息列表
   */
  public Mono<List<Message>> getConversationHistory(Long conversationId) {
    return Mono.fromCallable(() -> messageService.getMessagesByConversationId(conversationId))
        .doOnNext(messages -> log.debug("加载会话历史，会话ID: {}, 消息数量: {}", 
            conversationId, messages.size()));
  }
}