package com.example.controller;

import com.example.dto.request.StreamChatRequest;
import com.example.dto.stream.ChatEvent;
import com.example.sse.SseEventMapper;
import com.example.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 聊天控制器，处理聊天相关的HTTP请求
 *
 * @author xupeng
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

  @Autowired private AiChatService aiChatService;

  /**
   * SSE聊天端点 - 按需建立连接处理消息（支持模型选择） 利用Spring自动参数绑定，将URL路径参数和查询参数自动绑定到StreamChatRequest对象
   *
   * @param request 流式聊天请求对象，包含所有参数
   * @return 响应式SSE事件流
   */
  @GetMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<Object>> streamChat(
      @PathVariable Long conversationId, StreamChatRequest request) {
    // 设置路径参数到请求对象中
    request.setConversationId(conversationId);

    if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
      // 无消息时返回空流，连接会自然结束
      log.debug("无消息内容，返回空流");
      return Flux.empty();
    }

    return aiChatService
        .streamChat(request)
        .map(SseEventMapper::toSseEvent)
        .doOnNext(
            event -> {
              if (log.isDebugEnabled()) {
                Object payload = event.data();
                if (payload instanceof ChatEvent.ChunkPayload data) {
                  String content = data.getContent();
                  String escaped = content != null ? content.replace("\n", "\\n") : "";
                  log.debug(
                      "发送SSE事件: {} - chunk(len={}, preview={})",
                      event.event(),
                      content != null ? content.length() : 0,
                      escaped.length() > 200 ? escaped.substring(0, 200) + "..." : escaped);
                } else {
                  log.debug("发送SSE事件: {} - {}", event.event(), payload);
                }
              }
            })
        .doOnError(error -> log.error("流式聊天发生错误，会话ID: {}", request.getConversationId(), error))
        .doOnComplete(() -> log.info("流式聊天完成，会话ID: {}", request.getConversationId()));
  }

  // 统一由 SseEventMapper 处理事件名与payload
}
