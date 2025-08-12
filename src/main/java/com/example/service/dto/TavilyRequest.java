package com.example.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tavily API请求数据传输对象
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TavilyRequest {

  /** API密钥 */
  @JsonProperty("api_key")
  private String apiKey;

  /** 查询字符串 */
  private String query;

  /** 搜索深度 */
  @JsonProperty("search_depth")
  private String searchDepth;

  /** 包含答案 */
  @JsonProperty("include_answer")
  private Boolean includeAnswer;

  /** 包含原始内容 */
  @JsonProperty("include_raw_content")
  private Boolean includeRawContent;

  /** 最大结果数 */
  @JsonProperty("max_results")
  private Integer maxResults;

  /** 包含域名 */
  @JsonProperty("include_domains")
  private List<String> includeDomains;

  /** 排除域名 */
  @JsonProperty("exclude_domains")
  private List<String> excludeDomains;

  /** 创建基本搜索请求 */
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
