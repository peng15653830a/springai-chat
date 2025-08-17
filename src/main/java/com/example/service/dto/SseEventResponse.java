package com.example.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE事件响应DTO
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SseEventResponse {

  /** 事件类型 */
  private String type;

  /** 事件数据 */
  private Object data;

  /** 静态工厂方法 */
  public static SseEventResponse of(String type, Object data) {
    return new SseEventResponse(type, data);
  }

  public static SseEventResponse start(String message) {
    return of("start", message);
  }

  public static SseEventResponse chunk(String content) {
    return of("chunk", new ChunkData(content));
  }

  public static SseEventResponse thinking(String content) {
    return of("thinking", new ChunkData(content));
  }

  public static SseEventResponse search(String status) {
    return of("search", new SearchData(status));
  }

  public static SseEventResponse searchResults(Object results) {
    return of("search_results", new SearchResultsData(results));
  }

  public static SseEventResponse end(Long messageId) {
    return of("end", new EndData(messageId));
  }

  public static SseEventResponse error(String message) {
    return of("error", message);
  }

  @Data
  @AllArgsConstructor
  public static class ChunkData {
    private String content;
  }

  @Data
  @AllArgsConstructor
  public static class SearchData {
    private String type;
  }

  @Data
  @AllArgsConstructor
  public static class SearchResultsData {
    private Object results;
  }

  @Data
  @AllArgsConstructor
  public static class EndData {
    private Long messageId;
  }
}