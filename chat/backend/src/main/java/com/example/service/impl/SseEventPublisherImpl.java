package com.example.service.impl;

import com.example.dto.response.SearchResult;
import com.example.dto.response.SseEventResponse;
import com.example.service.SseEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    private final ConcurrentHashMap<Long, Sinks.Many<SseEventResponse>> conversationSinks = 
        new ConcurrentHashMap<>();
    
    /**
     * 线程本地变量存储当前请求的会话ID
     */
    private final ThreadLocal<Long> currentConversationId = new ThreadLocal<>();
    
    /**
     * 线程本地变量存储当前会话的搜索结果
     */
    private final ThreadLocal<List<SearchResult>> currentSearchResults = new ThreadLocal<>();

    @Override
    public void setCurrentConversationId(Long conversationId) {
        currentConversationId.set(conversationId);
    }

    @Override
    public void publishSearchStart() {
        Long conversationId = currentConversationId.get();
        if (conversationId != null) {
            publishEvent(conversationId, SseEventResponse.start("开始搜索最新信息..."));
            log.debug("发布搜索开始事件，会话ID: {}", conversationId);
        }
    }

    @Override
    public void publishSearchResults(List<SearchResult> results) {
        Long conversationId = currentConversationId.get();
        if (conversationId != null && results != null && !results.isEmpty()) {
            // 存储搜索结果到线程本地变量
            currentSearchResults.set(results);
            
            publishEvent(conversationId, SseEventResponse.searchResults(results));
            log.debug("发布搜索结果事件，会话ID: {}, 结果数量: {}", conversationId, results.size());
        }
    }

    @Override
    public void publishSearchComplete() {
        Long conversationId = currentConversationId.get();
        if (conversationId != null) {
            publishEvent(conversationId, SseEventResponse.search("complete"));
            log.debug("发布搜索完成事件，会话ID: {}", conversationId);
        }
    }

    @Override
    public void publishSearchError(String errorMessage) {
        Long conversationId = currentConversationId.get();
        if (conversationId != null) {
            publishSearchError(conversationId, errorMessage);
        }
    }

    // 添加带会话ID参数的重载方法

    @Override
    public void publishSearchStart(Long conversationId) {
        if (conversationId != null) {
            publishEvent(conversationId, SseEventResponse.start("开始搜索最新信息..."));
            log.debug("发布搜索开始事件，会话ID: {}", conversationId);
        }
    }

    @Override
    public void publishSearchResults(Long conversationId, List<SearchResult> results) {
        if (conversationId != null && results != null && !results.isEmpty()) {
            // 存储搜索结果到线程本地变量
            currentSearchResults.set(results);

            publishEvent(conversationId, SseEventResponse.searchResults(results));
            log.info("🔍 发布搜索结果事件，会话ID: {}, 结果数量: {}", conversationId, results.size());
        }
    }

    @Override
    public void publishSearchComplete(Long conversationId) {
        if (conversationId != null) {
            publishEvent(conversationId, SseEventResponse.search("complete"));
            log.debug("发布搜索完成事件，会话ID: {}", conversationId);
        }
    }

    @Override
    public void publishSearchError(Long conversationId, String errorMessage) {
        if (conversationId != null) {
            publishEvent(conversationId, SseEventResponse.error(errorMessage));
            log.debug("发布搜索错误事件，会话ID: {}, 错误: {}", conversationId, errorMessage);
        }
    }

    /**
     * 注册会话的事件发射器
     * 
     * @param conversationId 会话ID
     * @return 事件发射器
     */
    public Sinks.Many<SseEventResponse> registerConversation(Long conversationId) {
        Sinks.Many<SseEventResponse> sink = Sinks.many().multicast().onBackpressureBuffer();
        conversationSinks.put(conversationId, sink);
        log.debug("注册会话事件发射器，会话ID: {}", conversationId);
        return sink;
    }

    @Override
    public reactor.core.publisher.Flux<SseEventResponse> registerConversationFlux(Long conversationId) {
        return registerConversation(conversationId).asFlux();
    }

    /**
     * 移除会话的事件发射器
     * 
     * @param conversationId 会话ID
     */
    @Override
    public void removeConversation(Long conversationId) {
        Sinks.Many<SseEventResponse> sink = conversationSinks.remove(conversationId);
        if (sink != null) {
            sink.tryEmitComplete();
            log.debug("移除会话事件发射器，会话ID: {}", conversationId);
        }
    }

    /**
     * 发布事件到指定会话
     * 
     * @param conversationId 会话ID
     * @param event 事件
     */
    private void publishEvent(Long conversationId, SseEventResponse event) {
        Sinks.Many<SseEventResponse> sink = conversationSinks.get(conversationId);
        if (sink != null) {
            sink.tryEmitNext(event);
        }
    }

    /**
     * 清理当前线程的会话ID
     */
    @Override
    public void clearCurrentConversationId() {
        currentConversationId.remove();
        currentSearchResults.remove(); // 同时清理搜索结果
    }
    
    @Override
    public List<SearchResult> getCurrentSearchResults() {
        List<SearchResult> results = currentSearchResults.get();
        return results != null ? results : java.util.Collections.emptyList();
    }
    
    @Override
    public void clearCurrentSearchResults() {
        currentSearchResults.remove();
    }
}