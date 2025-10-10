package com.example.service;

import com.example.dto.response.SearchResult;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * 搜索服务接口 - 统一搜索服务，支持Tool Calling
 *
 * @author xupeng
 */
public interface SearchService {

  /**
   * 执行搜索
   *
   * @param query 搜索查询
   * @return 搜索结果列表的Mono
   */
  Mono<List<SearchResult>> search(String query);

  /**
   * 检查搜索服务是否可用
   *
   * @return 是否可用
   */
  boolean isAvailable();
}
