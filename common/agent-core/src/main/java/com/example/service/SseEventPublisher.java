package com.example.service;

import com.example.dto.response.SearchResult;
import com.example.dto.stream.ChatEvent;
import java.util.List;
import reactor.core.publisher.Flux;

/**
 * SSE 事件发布器接口，统一管理跨模块的实时事件流。
 */
public interface SseEventPublisher {

  void publishSearchStart(Long conversationId);

  void publishSearchResults(Long conversationId, List<SearchResult> results);

  void publishSearchResults(Long conversationId, Long messageId, List<SearchResult> results);

  void publishSearchComplete(Long conversationId);

  void publishSearchError(Long conversationId, String errorMessage);

  void publishThinking(Long conversationId, String thinking);

  void publishThinking(Long conversationId, Long messageId, String thinking);

  List<SearchResult> getSearchResultsByConversationId(Long conversationId);

  List<SearchResult> getSearchResultsByMessageId(Long messageId);

  Flux<ChatEvent> registerConversationFlux(Long conversationId);

  void removeConversation(Long conversationId);
}
