package com.example.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tavily API响应数据传输对象
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TavilyResponse {

  /** 查询字符串 */
  private String query;

  /** 回答 */
  private String answer;

  /** 跟进问题 */
  @JsonProperty("follow_up_questions")
  private List<String> followUpQuestions;

  /** 图片 */
  private List<String> images;

  /** 搜索结果 */
  private List<TavilySearchResult> results;

  /** Tavily搜索结果数据传输对象 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TavilySearchResult {

    /** 标题 */
    private String title;

    /** URL */
    private String url;

    /** 内容 */
    private String content;

    /** 原始内容 */
    @JsonProperty("raw_content")
    private String rawContent;

    /** 发布日期 */
    @JsonProperty("published_date")
    private String publishedDate;

    /** 得分 */
    private Double score;

    /** 转换为SearchResult */
    public SearchResult toSearchResult() {
      return SearchResult.create(this.title, this.content, this.url, this.score);
    }
  }
}
