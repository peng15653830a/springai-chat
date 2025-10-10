package com.example.sse;

import com.example.dto.stream.ChatEvent;
import org.springframework.http.codec.ServerSentEvent;

/**
 * 将 ChatEvent 映射为标准的 ServerSentEvent（统一事件名）。
 */
public final class SseEventMapper {

  private SseEventMapper() {}

  public static ServerSentEvent<Object> toSseEvent(ChatEvent event) {
    String name = switch (event.getType()) {
      case START -> "start";
      case CHUNK -> "chunk";
      case THINKING -> "thinking";
      case SEARCH -> "search";
      case SEARCH_RESULTS -> "search_results";
      case END -> "end";
      case ERROR -> "error";
    };
    return ServerSentEvent.builder(event.getPayload()).event(name).build();
  }
}

