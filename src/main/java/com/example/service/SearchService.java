package com.example.service;

import com.example.service.dto.SearchResult;
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
}
