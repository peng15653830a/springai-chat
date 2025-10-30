package com.example.service;

import com.example.dto.stream.ChatEvent;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.SpringAiTextStreamClient;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 提供统一的流式聊天处理模板，封装共享的流复用与最终响应聚合逻辑。
 */
public abstract class BaseChatService {

  private final SpringAiTextStreamClient textStreamClient;

  protected BaseChatService(SpringAiTextStreamClient textStreamClient) {
    this.textStreamClient = Objects.requireNonNull(textStreamClient, "textStreamClient");
  }

  protected Flux<ChatEvent> streamText(
      TextStreamRequest request,
      BiFunction<TextStreamRequest, String, Mono<ChatEvent>> completionHandler) {

    Flux<String> shared = textStreamClient.stream(request).publish().autoConnect(2);

    Flux<ChatEvent> chunkFlux = shared.map(this::mapChunk);

    Flux<ChatEvent> terminalFlux =
        shared
            .collectList()
            .flatMapMany(
                segments ->
                    completionHandler
                        .apply(request, joinChunks(segments))
                        .filter(Objects::nonNull)
                        .flux());

    return Flux.merge(chunkFlux, terminalFlux);
  }

  protected ChatEvent mapChunk(String chunk) {
    return ChatEvent.chunk(chunk);
  }

  private String joinChunks(List<String> segments) {
    StringBuilder builder = new StringBuilder();
    for (String segment : segments) {
      builder.append(segment);
    }
    return builder.toString();
  }

  protected SpringAiTextStreamClient getTextStreamClient() {
    return textStreamClient;
  }
}
