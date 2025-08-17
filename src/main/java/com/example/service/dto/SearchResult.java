package com.example.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果数据传输对象
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

  /** 标题 */
  private String title;

  /** 内容 */
  private String content;

  /** URL链接 */
  private String url;

  /** 得分 */
  private Double score;

  /** 创建搜索结果 */
  public static SearchResult create(String title, String content) {
    return new SearchResult(title, content, null, null);
  }

  /** 创建完整搜索结果 */
  public static SearchResult create(String title, String content, String url, Double score) {
    return new SearchResult(title, content, url, score);
  }
}
