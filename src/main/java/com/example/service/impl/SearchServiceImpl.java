package com.example.service.impl;

import static com.example.service.constants.AiChatConstants.HTTP_STATUS_OK;

import com.example.service.SearchService;
import com.example.service.dto.SearchResult;
import com.example.service.dto.TavilyRequest;
import com.example.service.dto.TavilyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 搜索服务实现类
 *
 * @author xupeng
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

  @Value("${search.tavily.api-key:}")
  private String tavilyApiKey;

  @Value("${search.tavily.base-url:https://api.tavily.com/search}")
  private String tavilyBaseUrl;

  @Value("${search.enabled:true}")
  private boolean searchEnabled;

  @Autowired private ObjectMapper objectMapper;

  /** 主搜索方法：使用Tavily搜索API */
  @Override
  public List<SearchResult> searchMetaso(String query) {
    log.info("开始搜索，查询词: {}, 搜索启用: {}", query, searchEnabled);

    if (!searchEnabled) {
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
    if (tavilyApiKey == null || tavilyApiKey.isEmpty()) {
      log.warn("Tavily API密钥未配置");
      return new ArrayList<>();
    }

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(tavilyBaseUrl);

      // 设置请求头
      httpPost.setHeader("Content-Type", "application/json");

      // 构建请求体
      TavilyRequest request = TavilyRequest.createBasic(tavilyApiKey, query);

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
            SearchResult.create("AI 摘要", tavilyResponse.getAnswer(), "AI Generated Summary", null);
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
      formatted.append(String.format("%d. %s\n", i + 1, result.getTitle()));
      formatted.append(String.format("   %s\n", result.getContent()));
      formatted.append(String.format("   链接: %s\n\n", result.getUrl()));
    }

    return formatted.toString();
  }

  /** 判断是否需要搜索 */
  @Override
  public boolean shouldSearch(String message) {
    // 只要消息不为null且不为空（去除空白字符后），就可以搜索
    return message != null && !message.trim().isEmpty();
  }
}
