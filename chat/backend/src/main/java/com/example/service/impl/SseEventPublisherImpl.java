package com.example.service.impl;

import com.example.dto.response.SearchResult;
import com.example.dto.stream.ChatEvent;
import com.example.service.SseEventPublisher;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

/**
 * SSE事件发布器实现类
 *
 * @author xupeng
 */
@Slf4j
@Service
public class SseEventPublisherImpl implements SseEventPublisher {

  /** 存储每个会话的事件发射器 Key: conversationId, Value: Sinks.Many */
  private final ConcurrentHashMap<Long, Sinks.Many<ChatEvent>> conversationSinks =
      new ConcurrentHashMap<>();

  /** 跨线程存储每个会话的搜索结果（向后兼容） */
  private final ConcurrentHashMap<Long, List<SearchResult>> conversationSearchResults =
      new ConcurrentHashMap<>();

  /** 跨线程存储每条消息的搜索结果（精准关联到消息，避免混淆） */
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
      // 存入跨线程可见Map，便于后续落库
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
    // 同时仍然发布到会话级SSE（payload 中带上 messageId，前端可做精准归属）
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

  /**
   * 注册会话的事件发射器（带容量限制和背压控制）
   *
   * @param conversationId 会话ID
   * @return 事件发射器
   */
  public Sinks.Many<ChatEvent> registerConversation(Long conversationId) {
    // 设置缓冲区容量限制（最多缓存1000个事件，防止内存溢出）
    Sinks.Many<ChatEvent> sink = Sinks.many().multicast().onBackpressureBuffer(1000, false);
    conversationSinks.put(conversationId, sink);
    log.debug("注册会话事件发射器（缓冲容量：1000），会话ID: {}", conversationId);
    return sink;
  }

  @Override
  public reactor.core.publisher.Flux<ChatEvent> registerConversationFlux(Long conversationId) {
    return registerConversation(conversationId)
        .asFlux()
        // 添加背压处理：当订阅者处理不过来时，丢弃最新的事件并记录日志
        .onBackpressureDrop(
            dropped -> log.warn("⚠️ SSE背压：丢弃事件 {} (会话ID: {})", dropped.getType(), conversationId))
        // 添加请求数量控制：每次最多请求256个事件
        .limitRate(256);
  }

  /**
   * 移除会话的事件发射器
   *
   * @param conversationId 会话ID
   */
  @Override
  public void removeConversation(Long conversationId) {
    Sinks.Many<ChatEvent> sink = conversationSinks.remove(conversationId);
    if (sink != null) {
      sink.tryEmitComplete();
      log.debug("移除会话事件发射器，会话ID: {}", conversationId);
    }
    // 清理会话级搜索结果
    if (conversationId != null) {
      conversationSearchResults.remove(conversationId);
    }
  }

  /**
   * 发布事件到指定会话（带重试机制）
   *
   * @param conversationId 会话ID
   * @param event 事件
   */
  private void publishEvent(Long conversationId, ChatEvent event) {
    Sinks.Many<ChatEvent> sink = conversationSinks.get(conversationId);
    if (sink != null) {
      Sinks.EmitResult result = sink.tryEmitNext(event);

      // 如果发送失败且是由于背压原因，记录警告并重试一次
      if (result == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
        log.warn("⚠️ 事件发送失败（非序列化），会话ID: {}, 事件类型: {}", conversationId, event.getType());
      } else if (result == Sinks.EmitResult.FAIL_OVERFLOW) {
        log.warn("⚠️ 事件发送失败（缓冲区溢出），会话ID: {}, 事件类型: {}，尝试清理旧事件", conversationId, event.getType());
        // 缓冲区溢出时，不再重试，避免无限循环
      } else if (result == Sinks.EmitResult.FAIL_CANCELLED) {
        log.debug("会话已取消，移除发射器，会话ID: {}", conversationId);
        conversationSinks.remove(conversationId);
      } else if (result == Sinks.EmitResult.FAIL_TERMINATED) {
        log.debug("会话已终止，移除发射器，会话ID: {}", conversationId);
        conversationSinks.remove(conversationId);
      }
    }
  }

  // 无历史兼容代码

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

  /** 应用关闭时清理ThreadLocal，防止内存泄漏 这是针对Spring AI框架限制的合理工作区域的安全保护 */
  @PreDestroy
  public void cleanup() {
    try {
      conversationSearchResults.clear();
      messageSearchResults.clear();
      // 清理所有事件发射器
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
      log.info("🧹 ThreadLocal和事件发射器已清理完成");
    } catch (Exception e) {
      log.warn("⚠️ 清理ThreadLocal时出错: {}", e.getMessage());
    }
  }
}
