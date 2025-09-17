package com.example.service.impl;

import com.example.dto.response.SearchResult;
import com.example.dto.stream.ChatEvent;
import com.example.service.SseEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PreDestroy;

/**
 * SSE事件发布器实现类
 * 
 * @author xupeng
 */
@Slf4j
@Service
public class SseEventPublisherImpl implements SseEventPublisher {

    /**
     * 存储每个会话的事件发射器
     * Key: conversationId, Value: Sinks.Many
     */
    private final ConcurrentHashMap<Long, Sinks.Many<ChatEvent>> conversationSinks = 
        new ConcurrentHashMap<>();
    
    /** 跨线程存储每个会话的搜索结果 */
    private final ConcurrentHashMap<Long, List<SearchResult>> conversationSearchResults = new ConcurrentHashMap<>();

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

    /**
     * 注册会话的事件发射器
     * 
     * @param conversationId 会话ID
     * @return 事件发射器
     */
    public Sinks.Many<ChatEvent> registerConversation(Long conversationId) {
        Sinks.Many<ChatEvent> sink = Sinks.many().multicast().onBackpressureBuffer();
        conversationSinks.put(conversationId, sink);
        log.debug("注册会话事件发射器，会话ID: {}", conversationId);
        return sink;
    }

    @Override
    public reactor.core.publisher.Flux<ChatEvent> registerConversationFlux(Long conversationId) {
        return registerConversation(conversationId).asFlux();
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
     * 发布事件到指定会话
     * 
     * @param conversationId 会话ID
     * @param event 事件
     */
    private void publishEvent(Long conversationId, ChatEvent event) {
        Sinks.Many<ChatEvent> sink = conversationSinks.get(conversationId);
        if (sink != null) {
            sink.tryEmitNext(event);
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

    /**
     * 应用关闭时清理ThreadLocal，防止内存泄漏
     * 这是针对Spring AI框架限制的合理工作区域的安全保护
     */
    @PreDestroy
    public void cleanup() {
        try {
            conversationSearchResults.clear();
            // 清理所有事件发射器
            conversationSinks.values().forEach(sink -> {
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
