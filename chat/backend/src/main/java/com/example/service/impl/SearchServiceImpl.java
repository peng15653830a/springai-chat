package com.example.service.impl;

import static com.example.service.constants.AiChatConstants.HTTP_STATUS_OK;

import com.example.config.SearchProperties;
import com.example.service.SearchService;
import com.example.dto.response.SearchResult;
import com.example.dto.response.SseEventResponse;
import com.example.dto.request.TavilyRequest;
import com.example.dto.response.TavilyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 搜索服务实现类
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

  private final SearchProperties searchProperties;
  private final ObjectMapper objectMapper;

  /**
   * 日志消息最大长度限制
   */
  private static final int MAX_LOG_MESSAGE_LENGTH = 50;

  /** 主搜索方法：使用Tavily搜索API */
  @Override
  public List<SearchResult> searchMetaso(String query) {
    log.info("开始搜索，查询词: {}, 搜索启用: {}", query, searchProperties.isEnabled());

    if (!searchProperties.isEnabled()) {
      log.info("搜索功能已禁用，返回空结果");
      return new ArrayList<>();
    }

    // 使用Tavily搜索API
    List<SearchResult> searchResults = callTavilyApi(query);

    log.info("搜索完成，返回 {} 条结果", searchResults.size());
    return searchResults;
  }

  /** 调用Tavily搜索API Tavily提供每月1000次免费调用 */
  private List<SearchResult> callTavilyApi(String query) {
    if (searchProperties.getTavily().getApiKey() == null || searchProperties.getTavily().getApiKey().isEmpty()) {
      log.warn("Tavily API密钥未配置");
      return new ArrayList<>();
    }

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(searchProperties.getTavily().getBaseUrl());

      // 设置请求头
      httpPost.setHeader("Content-Type", "application/json");

      // 构建请求体
      TavilyRequest request = TavilyRequest.createBasic(searchProperties.getTavily().getApiKey(), query);

      String jsonRequest = objectMapper.writeValueAsString(request);
      httpPost.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));

      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        if (statusCode == HTTP_STATUS_OK) {
          return parseTavilyResponse(responseString);
        } else {
          log.error("Tavily搜索API调用失败，状态码: {}, 响应: {}", statusCode, responseString);
          return new ArrayList<>();
        }
      }
    } catch (Exception e) {
      log.error("Tavily搜索API调用异常: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /** 解析Tavily API响应 */
  private List<SearchResult> parseTavilyResponse(String responseString) {
    try {
      TavilyResponse tavilyResponse = objectMapper.readValue(responseString, TavilyResponse.class);

      List<SearchResult> results = new ArrayList<>();

      // 首先添加AI生成的答案（如果有）
      if (tavilyResponse.getAnswer() != null && !tavilyResponse.getAnswer().isEmpty()) {
        SearchResult answerResult =
            SearchResult.create("AI 摘要", "AI Generated Summary", tavilyResponse.getAnswer(), null);
        results.add(answerResult);
      }

      // 解析搜索结果
      if (tavilyResponse.getResults() != null && !tavilyResponse.getResults().isEmpty()) {
        for (TavilyResponse.TavilySearchResult item : tavilyResponse.getResults()) {
          SearchResult result = item.toSearchResult();
          results.add(result);
        }
      }

      return results;
    } catch (Exception e) {
      log.error("解析Tavily API响应失败: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /** 格式化搜索结果为文本 */
  @Override
  public String formatSearchResults(List<SearchResult> searchResults) {
    if (searchResults == null || searchResults.isEmpty()) {
      return "";
    }

    StringBuilder formatted = new StringBuilder();
    formatted.append("搜索结果：\n");

    for (int i = 0; i < searchResults.size(); i++) {
      SearchResult result = searchResults.get(i);
      // 处理null值情况
      String title = result.getTitle() != null ? result.getTitle() : "";
      String content = result.getContent() != null ? result.getContent() : "";
      String url = result.getUrl() != null ? result.getUrl() : "";
      
      formatted.append(String.format("%d. %s\n", i + 1, title));
      
      // 如果是AI摘要，只显示内容，不显示链接
      if ("AI 摘要".equals(title)) {
        formatted.append(String.format("   %s\n\n", content));
      } else {
        formatted.append(String.format("   %s\n", content));
        if (!url.isEmpty()) {
          formatted.append(String.format("   链接: %s\n\n", url));
        } else {
          formatted.append("\n");
        }
      }
    }

    return formatted.toString();
  }

  /** 判断是否需要搜索 */
  @Override
  public boolean shouldSearch(String message) {
    // 只要消息不为null且不为空（去除空白字符后），就可以搜索
    return message != null && !message.trim().isEmpty();
  }

  /**
   * 执行搜索并返回包含事件流的结果
   *
   * @param userMessage 用户消息，不能为null
   * @param searchEnabled 是否启用搜索
   * @return 包含搜索上下文和事件流的响应式结果，不会返回null
   */
  @Override
  public Mono<SearchContextResult> performSearchWithEvents(String userMessage, boolean searchEnabled) {
    // 【强制】参数校验
    if (userMessage == null) {
      throw new IllegalArgumentException("用户消息不能为null");
    }
    
    if (!searchProperties.isEnabled()) {
      return Mono.just(new SearchContextResult("", null, Flux.empty()));
    }

    // 【推荐】日志中避免字符串拼接，使用占位符
    String logMessage = userMessage.length() > MAX_LOG_MESSAGE_LENGTH 
        ? userMessage.substring(0, MAX_LOG_MESSAGE_LENGTH) 
        : userMessage;
    log.info("开始执行搜索，关键词: [{}]", logMessage);

    return Mono.fromCallable(() -> {
            List<SearchResult> searchResults = searchMetaso(userMessage);
            String searchContext = formatSearchResults(searchResults);
            Flux<SseEventResponse> searchEvents = createSearchEvents(searchResults);
            return new SearchContextResult(searchContext, searchResults, searchEvents);
        })
        .doOnNext(result -> {
            int resultCount = result.getSearchResults() != null ? result.getSearchResults().size() : 0;
            int contextLength = result.getSearchContext().length();
            log.debug("搜索完成，结果数量: [{}], 上下文长度: [{}]", resultCount, contextLength);
        })
        .onErrorReturn(new SearchContextResult("", null,
            Flux.just(SseEventResponse.error("搜索服务暂时不可用"))));
  }

  /**
   * 创建搜索相关的SSE事件流
   *
   * @param searchResults 搜索结果列表，可以为null或空
   * @return SSE事件流，不会返回null
   */
  @Override
  public Flux<SseEventResponse> createSearchEvents(List<SearchResult> searchResults) {
    return Flux.concat(
        Mono.just(SseEventResponse.search("start")),
        Mono.justOrEmpty(searchResults)
            .filter(results -> !results.isEmpty())
            .map(SseEventResponse::searchResults),
        Mono.just(SseEventResponse.search("complete"))
    );
  }
}
