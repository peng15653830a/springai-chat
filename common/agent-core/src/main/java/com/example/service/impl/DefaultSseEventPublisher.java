package com.example.service.impl;

import com.example.dto.response.SearchResult;
import com.example.dto.stream.ChatEvent;
import com.example.service.SseEventPublisher;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 通用的 SSE 事件发布器实现。
 */
@Slf4j
@Service
public class DefaultSseEventPublisher implements SseEventPublisher {

  private final ConcurrentHashMap<Long, Sinks.Many<ChatEvent>> conversationSinks =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Long, List<SearchResult>> conversationSearchResults =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Long, List<SearchResult>> messageSearchResults =
      new ConcurrentHashMap<>();

  @Override
  public void publishSearchStart(Long conversationId) {
    if (conversationId != null) {
      publishEvent(conversationId, ChatEvent.start("开始搜索最新信息..."));
      log.debug("发布搜索开始事件，会话ID: {}", conversationId);
    }
  }

  @Override
  public void publishSearchResults(Long conversationId, List<SearchResult> results) {
    if (conversationId != null && results != null && !results.isEmpty()) {
      conversationSearchResults.put(conversationId, results);
      publishEvent(conversationId, ChatEvent.searchResults(results));
      log.info("🔍 发布搜索结果事件，会话ID: {}, 结果数量: {}", conversationId, results.size());
    }
  }

  @Override
  public void publishSearchResults(Long conversationId, Long messageId, List<SearchResult> results) {
    if (messageId != null && results != null && !results.isEmpty()) {
      messageSearchResults.put(messageId, results);
    }
    if (conversationId != null && results != null && !results.isEmpty()) {
      publishEvent(conversationId, ChatEvent.searchResults(messageId, results));
      log.info(
          "🔍 发布搜索结果事件，会话ID: {}, 消息ID: {}, 结果数量: {}",
          conversationId,
          messageId,
          results.size());
    }
  }

  @Override
  public void publishSearchComplete(Long conversationId) {
    if (conversationId != null) {
      publishEvent(conversationId, ChatEvent.search("complete"));
      log.debug("发布搜索完成事件，会话ID: {}", conversationId);
    }
  }

  @Override
  public void publishSearchError(Long conversationId, String errorMessage) {
    if (conversationId != null) {
      publishEvent(conversationId, ChatEvent.error(errorMessage));
      log.debug("发布搜索错误事件，会话ID: {}, 错误: {}", conversationId, errorMessage);
    }
  }

  @Override
  public void publishThinking(Long conversationId, String thinking) {
    if (conversationId != null && thinking != null && !thinking.trim().isEmpty()) {
      publishEvent(conversationId, ChatEvent.thinking(thinking));
      log.debug("发布深度思考事件，会话ID: {}, thinking长度: {}", conversationId, thinking.length());
    }
  }

  @Override
  public void publishThinking(Long conversationId, Long messageId, String thinking) {
    if (conversationId != null && thinking != null && !thinking.trim().isEmpty()) {
      publishEvent(conversationId, ChatEvent.thinking(messageId, thinking));
      log.debug(
          "发布深度思考事件，会话ID: {}, 消息ID: {}, thinking长度: {}",
          conversationId,
          messageId,
          thinking.length());
    }
  }

  @Override
  public Flux<ChatEvent> registerConversationFlux(Long conversationId) {
    return registerConversation(conversationId)
        .asFlux()
        .onBackpressureDrop(
            dropped ->
                log.warn("⚠️ SSE背压：丢弃事件 {} (会话ID: {})", dropped.getType(), conversationId))
        .limitRate(256);
  }

  @Override
  public void removeConversation(Long conversationId) {
    Sinks.Many<ChatEvent> sink = conversationSinks.remove(conversationId);
    if (sink != null) {
      sink.tryEmitComplete();
      log.debug("移除会话事件发射器，会话ID: {}", conversationId);
    }
    if (conversationId != null) {
      conversationSearchResults.remove(conversationId);
    }
  }

  @Override
  public List<SearchResult> getSearchResultsByConversationId(Long conversationId) {
    if (conversationId == null) {
      return null;
    }
    return conversationSearchResults.get(conversationId);
  }

  @Override
  public List<SearchResult> getSearchResultsByMessageId(Long messageId) {
    if (messageId == null) {
      return null;
    }
    return messageSearchResults.get(messageId);
  }

  private Sinks.Many<ChatEvent> registerConversation(Long conversationId) {
    Sinks.Many<ChatEvent> sink = Sinks.many().multicast().onBackpressureBuffer(1000, false);
    conversationSinks.put(conversationId, sink);
    log.debug("注册会话事件发射器（缓冲容量：1000），会话ID: {}", conversationId);
    return sink;
  }

  private void publishEvent(Long conversationId, ChatEvent event) {
    Sinks.Many<ChatEvent> sink = conversationSinks.get(conversationId);
    if (sink != null) {
      Sinks.EmitResult result = sink.tryEmitNext(event);
      if (result == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
        log.warn("⚠️ 事件发送失败（非序列化），会话ID: {}, 事件类型: {}", conversationId, event.getType());
      } else if (result == Sinks.EmitResult.FAIL_OVERFLOW) {
        log.warn(
            "⚠️ 事件发送失败（缓冲区溢出），会话ID: {}, 事件类型: {}，尝试清理旧事件",
            conversationId,
            event.getType());
      } else if (result == Sinks.EmitResult.FAIL_CANCELLED
          || result == Sinks.EmitResult.FAIL_TERMINATED) {
        conversationSinks.remove(conversationId);
      }
    }
  }

  @PreDestroy
  public void cleanup() {
    try {
      conversationSearchResults.clear();
      messageSearchResults.clear();
      conversationSinks
          .values()
          .forEach(
              sink -> {
                try {
                  sink.tryEmitComplete();
                } catch (Exception e) {
                  log.warn("清理事件发射器时出错: {}", e.getMessage());
                }
              });
      conversationSinks.clear();
      log.info("🧹 SSE事件资源已清理");
    } catch (Exception e) {
      log.warn("⚠️ 清理SSE资源时出错: {}", e.getMessage());
    }
  }
}
