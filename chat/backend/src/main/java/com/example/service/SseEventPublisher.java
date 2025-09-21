package com.example.service;

import com.example.dto.response.SearchResult;
import com.example.dto.stream.ChatEvent;
import java.util.List;

/**
 * SSE事件发布器接口
 *
 * @author xupeng
 */
public interface SseEventPublisher {
  /**
   * 发布搜索开始事件（带会话ID）
   *
   * @param conversationId 会话ID
   */
  void publishSearchStart(Long conversationId);

  /**
   * 发布搜索结果事件（带会话ID）
   *
   * @param conversationId 会话ID
   * @param results 搜索结果
   */
  void publishSearchResults(Long conversationId, List<SearchResult> results);

  /**
   * 发布搜索完成事件（带会话ID）
   *
   * @param conversationId 会话ID
   */
  void publishSearchComplete(Long conversationId);

  /**
   * 发布搜索错误事件（带会话ID）
   *
   * @param conversationId 会话ID
   * @param errorMessage 错误信息
   */
  void publishSearchError(Long conversationId, String errorMessage);

  /**
   * 发布深度思考内容事件
   *
   * @param conversationId 会话ID
   * @param thinking 思考内容
   */
  void publishThinking(Long conversationId, String thinking);

  /**
   * 根据会话ID获取搜索结果（跨线程安全）
   *
   * @param conversationId 会话ID
   * @return 指定会话的搜索结果
   */
  List<SearchResult> getSearchResultsByConversationId(Long conversationId);

  /**
   * 注册会话的事件发射器
   *
   * @param conversationId 会话ID
   * @return 事件发射器的Flux流
   */
  reactor.core.publisher.Flux<ChatEvent> registerConversationFlux(Long conversationId);

  /**
   * 移除会话的事件发射器
   *
   * @param conversationId 会话ID
   */
  void removeConversation(Long conversationId);
}
