package com.example.dto.response;

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

  /** 事件类型常量 */
  public static final String START_TYPE = "start";
  public static final String CHUNK_TYPE = "chunk";
  public static final String THINKING_TYPE = "thinking";
  public static final String SEARCH_TYPE = "search";
  public static final String SEARCH_RESULTS_TYPE = "search_results";
  public static final String END_TYPE = "end";
  public static final String ERROR_TYPE = "error";

  /** 静态工厂方法 */
  public static SseEventResponse of(String type, Object data) {
    return new SseEventResponse(type, data);
  }

  public static SseEventResponse start(String message) {
    return of(START_TYPE, message);
  }

  public static SseEventResponse chunk(String content) {
    return of(CHUNK_TYPE, new ChunkData(content));
  }

  public static SseEventResponse thinking(String content) {
    return of(THINKING_TYPE, new ChunkData(content));
  }

  public static SseEventResponse search(String status) {
    return of(SEARCH_TYPE, new SearchData(status));
  }

  public static SseEventResponse searchResults(Object results) {
    return of(SEARCH_RESULTS_TYPE, new SearchResultsData(results));
  }

  public static SseEventResponse end(Long messageId) {
    return of(END_TYPE, new EndData(messageId));
  }

  public static SseEventResponse error(String message) {
    return of(ERROR_TYPE, message);
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