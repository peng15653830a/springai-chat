package com.example.dto.stream;

import com.example.dto.response.SearchResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天流事件（强类型，替代 SseEventResponse）。 通过枚举区分事件类型，payload 承载事件数据。
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatEvent {

  private ChatEventType type;
  private Object payload;

  public enum ChatEventType {
    /** 开始事件 */
    START,
    /** 文本块事件 */
    CHUNK,
    /** 深度思考事件 */
    THINKING,
    /** 搜索事件 */
    SEARCH,
    /** 搜索结果事件 */
    SEARCH_RESULTS,
    /** 结束事件 */
    END,
    /** 错误事件 */
    ERROR
  }

  public static ChatEvent of(ChatEventType type, Object payload) {
    return new ChatEvent(type, payload);
  }

  public static ChatEvent start(String message) {
    return of(ChatEventType.START, new StartPayload(message));
  }

  public static ChatEvent chunk(String content) {
    return of(ChatEventType.CHUNK, new ChunkPayload(content));
  }

  public static ChatEvent thinking(String content) {
    return of(ChatEventType.THINKING, new ChunkPayload(content));
  }

  public static ChatEvent search(String status) {
    return of(ChatEventType.SEARCH, new SearchPayload(status));
  }

  public static ChatEvent searchResults(List<SearchResult> results) {
    return of(ChatEventType.SEARCH_RESULTS, new SearchResultsPayload(results));
  }

  public static ChatEvent end(Long messageId) {
    return of(ChatEventType.END, new EndPayload(messageId));
  }

  public static ChatEvent error(String message) {
    return of(ChatEventType.ERROR, new ErrorPayload(message));
  }

  @Data
  @AllArgsConstructor
  public static class StartPayload {
    private String message;
  }

  @Data
  @AllArgsConstructor
  public static class ChunkPayload {
    private String content;
  }

  @Data
  @AllArgsConstructor
  public static class SearchPayload {
    private String status;
  }

  @Data
  @AllArgsConstructor
  public static class SearchResultsPayload {
    private List<SearchResult> results;
  }

  @Data
  @AllArgsConstructor
  public static class EndPayload {
    private Long messageId;
  }

  @Data
  @AllArgsConstructor
  public static class ErrorPayload {
    private String message;
  }
}
