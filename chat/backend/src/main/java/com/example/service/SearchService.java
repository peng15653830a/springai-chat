package com.example.service;

import com.example.dto.response.SearchResult;
import com.example.dto.response.SseEventResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 搜索服务接口
 *
 * @author xupeng
 */
public interface SearchService {

  /**
   * 主搜索方法：使用Tavily搜索API
   *
   * @param query 搜索查询
   * @return 搜索结果列表
   */
  List<SearchResult> searchMetaso(String query);

  /**
   * 格式化搜索结果为文本
   *
   * @param searchResults 搜索结果列表
   * @return 格式化后的文本
   */
  String formatSearchResults(List<SearchResult> searchResults);

  /**
   * 判断是否需要搜索
   *
   * @param message 消息内容
   * @return 是否需要搜索
   */
  boolean shouldSearch(String message);

  /**
   * 执行搜索并返回包含事件流的结果
   *
   * @param userMessage 用户消息，不能为null
   * @param searchEnabled 是否启用搜索
   * @return 包含搜索上下文和事件流的响应式结果，不会返回null
   */
  Mono<SearchContextResult> performSearchWithEvents(String userMessage, boolean searchEnabled);

  /**
   * 创建搜索相关的SSE事件流
   *
   * @param searchResults 搜索结果列表，可以为null或空
   * @return SSE事件流，不会返回null
   */
  Flux<SseEventResponse> createSearchEvents(List<SearchResult> searchResults);

  /**
   * 搜索上下文结果数据传输对象
   *
   * @author xupeng
   * @since 1.0
   */
  class SearchContextResult {
    
    /** 搜索上下文文本 */
    private final String searchContext;
    
    /** 搜索结果列表 */
    private final List<SearchResult> searchResults;
    
    /** 搜索事件流 */
    private final Flux<SseEventResponse> searchEvents;

    /**
     * 构造函数
     *
     * @param searchContext 搜索上下文，不能为null
     * @param searchResults 搜索结果列表，可以为null
     * @param searchEvents 搜索事件流，不能为null
     */
    public SearchContextResult(String searchContext, List<SearchResult> searchResults,
                              Flux<SseEventResponse> searchEvents) {
      // 【强制】使用Objects.requireNonNull进行参数校验
      this.searchContext = java.util.Objects.requireNonNull(searchContext, "搜索上下文不能为null");
      this.searchResults = searchResults;
      this.searchEvents = java.util.Objects.requireNonNull(searchEvents, "搜索事件流不能为null");
    }

    /**
     * 获取搜索上下文
     *
     * @return 搜索上下文，不会返回null
     */
    public String getSearchContext() {
      return searchContext;
    }

    /**
     * 获取搜索结果列表
     *
     * @return 搜索结果列表，可能为null
     */
    public List<SearchResult> getSearchResults() {
      return searchResults;
    }

    /**
     * 获取搜索事件流
     *
     * @return 搜索事件流，不会返回null
     */
    public Flux<SseEventResponse> getSearchEvents() {
      return searchEvents;
    }
  }
}
