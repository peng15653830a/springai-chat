package com.example.service;

import com.example.dto.response.SearchResult;
import com.example.dto.stream.ChatEvent;
import java.util.List;

/**
 * SSE 事件发布器接口。
 */
public interface SseEventPublisher {

  /**
   * 发布搜索开始事件。
   * @param conversationId 会话ID
   */
  void publishSearchStart(Long conversationId);

  /**
   * 发布搜索结果事件（会话级）。
   * @param conversationId 会话ID
   * @param results 搜索结果
   */
  void publishSearchResults(Long conversationId, List<SearchResult> results);

  /**
   * 发布搜索结果事件（消息级）。
   * @param conversationId 会话ID
   * @param messageId 消息ID
   * @param results 搜索结果
   */
  void publishSearchResults(Long conversationId, Long messageId, List<SearchResult> results);

  /**
   * 发布搜索完成事件。
   * @param conversationId 会话ID
   */
  void publishSearchComplete(Long conversationId);

  /**
   * 发布搜索错误事件。
   * @param conversationId 会话ID
   * @param errorMessage 错误信息
   */
  void publishSearchError(Long conversationId, String errorMessage);

  /**
   * 发布思考内容（会话级）。
   * @param conversationId 会话ID
   * @param thinking 思考内容
   */
  void publishThinking(Long conversationId, String thinking);

  /**
   * 发布思考内容（消息级）。
   * @param conversationId 会话ID
   * @param messageId 消息ID
   * @param thinking 思考内容
   */
  void publishThinking(Long conversationId, Long messageId, String thinking);

  /**
   * 获取会话级搜索结果（跨线程安全）。
   * @param conversationId 会话ID
   * @return 搜索结果
   */
  List<SearchResult> getSearchResultsByConversationId(Long conversationId);

  /**
   * 获取消息级搜索结果（跨线程安全）。
   * @param messageId 消息ID
   * @return 搜索结果
   */
  List<SearchResult> getSearchResultsByMessageId(Long messageId);

  /**
   * 注册会话事件流。
   * @param conversationId 会话ID
   * @return 事件流
   */
  reactor.core.publisher.Flux<ChatEvent> registerConversationFlux(Long conversationId);

  /**
   * 移除会话事件流。
   * @param conversationId 会话ID
   */
  void removeConversation(Long conversationId);
}

