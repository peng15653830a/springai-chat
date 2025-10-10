package com.example.service.impl;

import com.example.config.RetryTimeoutProperties;
import com.example.config.SearchProperties;
import com.example.dto.request.TavilyRequest;
import com.example.dto.response.SearchResult;
import com.example.dto.response.TavilyResponse;
import com.example.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 搜索服务实现类 - 统一搜索服务，支持Tool Calling
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

  private final SearchProperties searchProperties;
  private final RetryTimeoutProperties retryTimeoutProperties;
  private final ObjectMapper objectMapper;
  private final WebClient.Builder webClientBuilder;

  // 不保留历史构造方式，统一通过注入 WebClient.Builder

  @Override
  public Mono<List<SearchResult>> search(String query) {
    log.info("开始搜索，查询: {}", query);

    if (!isAvailable()) {
      log.warn("搜索服务不可用");
      return Mono.just(new ArrayList<>());
    }

    try {
      TavilyRequest request =
          TavilyRequest.createBasic(searchProperties.getTavily().getApiKey(), query);

      String jsonRequest = objectMapper.writeValueAsString(request);

      return webClientBuilder
          .build()
          .post()
          .uri(searchProperties.getTavily().getBaseUrl())
          .header("Content-Type", "application/json")
          .bodyValue(jsonRequest)
          .retrieve()
          .bodyToMono(String.class)
          .timeout(retryTimeoutProperties.getSearchResponseTimeout())
          .retryWhen(
              Retry.backoff(
                      retryTimeoutProperties.getSearch().getMaxRetries(),
                      Duration.ofMillis(retryTimeoutProperties.getSearch().getRetryBackoffMs()))
                  .filter(
                      throwable -> {
                        // 只对网络错误、超时等可重试异常进行重试
                        return !(throwable instanceof IllegalArgumentException);
                      })
                  .doBeforeRetry(
                      signal ->
                          log.warn(
                              "🔄 搜索重试 {}/{}，异常: {}",
                              signal.totalRetries() + 1,
                              retryTimeoutProperties.getSearch().getMaxRetries(),
                              signal.failure().getMessage())))
          .map(this::parseTavilyResponse)
          .doOnSuccess(results -> log.info("搜索完成，返回 {} 条结果", results.size()))
          .onErrorReturn(
              throwable -> {
                log.error("Tavily搜索API调用异常: {}", throwable.getMessage(), throwable);
                return true;
              },
              new ArrayList<>());
    } catch (Exception e) {
      log.error("构建搜索请求异常: {}", e.getMessage(), e);
      return Mono.just(new ArrayList<>());
    }
  }

  @Override
  public boolean isAvailable() {
    return searchProperties.isEnabled()
        && searchProperties.getTavily() != null
        && searchProperties.getTavily().getApiKey() != null
        && !searchProperties.getTavily().getApiKey().isEmpty();
  }

  /** 解析Tavily API响应 */
  private List<SearchResult> parseTavilyResponse(String responseString) {
    try {
      TavilyResponse tavilyResponse = objectMapper.readValue(responseString, TavilyResponse.class);

      List<SearchResult> results = new ArrayList<>();

      // 解析搜索结果
      if (tavilyResponse.getResults() != null && !tavilyResponse.getResults().isEmpty()) {
        for (TavilyResponse.TavilySearchResult item : tavilyResponse.getResults()) {
          SearchResult result = item.toSearchResult();
          results.add(result);
        }
      }

      return results;
    } catch (Exception e) {
      log.error("解析Tavily API响应失败: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }
}
