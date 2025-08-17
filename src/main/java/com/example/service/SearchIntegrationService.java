package com.example.service;

import com.example.service.dto.SearchResult;
import com.example.service.dto.SseEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 搜索集成服务
 *
 * @author xupeng
 */
@Slf4j
@Service
public class SearchIntegrationService {

  @Autowired private SearchService searchService;

  /**
   * 执行搜索并返回搜索上下文和事件流
   *
   * @param userMessage 用户消息
   * @return 包含搜索事件和上下文的响应式流
   */
  public Mono<SearchContextResult> performSearchIfEnabled(String userMessage, boolean searchEnabled) {
    if (!searchEnabled) {
      return Mono.just(new SearchContextResult("", null, Flux.empty()));
    }

    log.info("开始执行搜索，关键词: {}", userMessage.substring(0, Math.min(50, userMessage.length())));

    return Mono.fromCallable(() -> {
          // 执行搜索
          List<SearchResult> searchResults = searchService.searchMetaso(userMessage);
          String searchContext = searchService.formatSearchResults(searchResults);
          
          // 创建搜索事件流
          Flux<SseEventResponse> searchEvents = createSearchEvents(searchResults);
          
          return new SearchContextResult(searchContext, searchResults, searchEvents);
        })
        .doOnNext(result -> log.debug("搜索完成，结果数量: {}, 上下文长度: {}", 
            result.getSearchResults() != null ? result.getSearchResults().size() : 0,
            result.getSearchContext().length()))
        .onErrorReturn(new SearchContextResult("", null, 
            Flux.just(SseEventResponse.error("搜索服务暂时不可用"))));
  }

  /**
   * 创建搜索相关的SSE事件流
   */
  private Flux<SseEventResponse> createSearchEvents(List<SearchResult> searchResults) {
    return Flux.concat(
        Mono.just(SseEventResponse.search("start")),
        Mono.justOrEmpty(searchResults)
            .filter(results -> !results.isEmpty())
            .map(SseEventResponse::searchResults),
        Mono.just(SseEventResponse.search("complete"))
    );
  }

  /**
   * 搜索上下文结果
   */
  public static class SearchContextResult {
    private final String searchContext;
    private final List<SearchResult> searchResults;
    private final Flux<SseEventResponse> searchEvents;

    public SearchContextResult(String searchContext, List<SearchResult> searchResults, 
                             Flux<SseEventResponse> searchEvents) {
      this.searchContext = searchContext;
      this.searchResults = searchResults;
      this.searchEvents = searchEvents;
    }

    public String getSearchContext() {
      return searchContext;
    }

    public List<SearchResult> getSearchResults() {
      return searchResults;
    }

    public Flux<SseEventResponse> getSearchEvents() {
      return searchEvents;
    }
  }
}