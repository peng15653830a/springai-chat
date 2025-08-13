package com.example.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE事件数据传输对象
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {

  /** 事件类型 */
  private String type;

  /** 事件数据 */
  private Object data;

  /** 创建开始事件 */
  public static SseEvent start(String message) {
    return new SseEvent("start", message);
  }

  /** 创建结束事件 */
  public static SseEvent end(Long messageId) {
    EndEventData data = new EndEventData(messageId);
    return new SseEvent("end", data);
  }

  /** 创建错误事件 */
  public static SseEvent error(String message) {
    return new SseEvent("error", message);
  }

  /** 创建搜索事件 */
  public static SseEvent search(String status) {
    SearchEventData data = new SearchEventData(status);
    return new SseEvent("search", data);
  }

  /** 创建内容块事件 */
  public static SseEvent chunk(String content) {
    ChunkEventData data = new ChunkEventData(content);
    return new SseEvent("chunk", data);
  }

  /** 创建思考事件 */
  public static SseEvent thinking(String content) {
    ThinkingEventData data = new ThinkingEventData(content);
    return new SseEvent("thinking", data);
  }

  /** 创建搜索结果事件 */
  public static SseEvent searchResults(List<SearchResult> results) {
    SearchResultsEventData data = new SearchResultsEventData(results);
    return new SseEvent("search_results", data);
  }

  /** 结束事件数据 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EndEventData {
    private Long messageId;
  }

  /** 搜索事件数据 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SearchEventData {
    private String type;
  }

  /** 内容块事件数据 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChunkEventData {
    private String content;
  }

  /** 思考事件数据 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThinkingEventData {
    private String content;
  }

  /** 搜索结果事件数据 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SearchResultsEventData {
    private List<SearchResult> results;
  }
}
