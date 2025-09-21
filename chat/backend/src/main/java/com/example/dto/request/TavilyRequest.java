package com.example.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * Tavily搜索请求DTO
 *
 * @author xupeng
 */
@Data
public class TavilyRequest {
  @JsonProperty("api_key")
  private String apiKey;

  private String query;

  @JsonProperty("search_depth")
  private String searchDepth;

  @JsonProperty("include_answer")
  private Boolean includeAnswer;

  @JsonProperty("include_raw_content")
  private Boolean includeRawContent;

  @JsonProperty("max_results")
  private int maxResults = 5;

  @JsonProperty("include_domains")
  private List<String> includeDomains;

  @JsonProperty("exclude_domains")
  private List<String> excludeDomains;

  /** 创建基础搜索请求 */
  public static TavilyRequest createBasic(String apiKey, String query) {
    TavilyRequest request = new TavilyRequest();
    request.setApiKey(apiKey);
    request.setQuery(query);
    request.setSearchDepth("basic");
    request.setIncludeAnswer(true);
    request.setIncludeRawContent(false);
    request.setMaxResults(5);
    return request;
  }
}
