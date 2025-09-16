package com.example.controller;

import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
   * SSE聊天端点 - 按需建立连接处理消息（支持模型选择）
   * 利用Spring自动参数绑定，将URL路径参数和查询参数自动绑定到StreamChatRequest对象
   *
   * @param request 流式聊天请求对象，包含所有参数
   * @return 响应式SSE事件流
   */
  @GetMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<SseEventResponse> streamChat(@PathVariable Long conversationId, StreamChatRequest request) {
    // 设置路径参数到请求对象中
    request.setConversationId(conversationId);
    
    log.info("SSE连接请求，会话ID: {}, 是否有消息: {}, 用户ID: {}, 指定模型: {}-{}", 
        request.getConversationId(), request.getMessage() != null, request.getUserId(), 
        request.getProvider(), request.getModel());
    
    if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
      // 无消息时返回空流，连接会自然结束
      log.debug("无消息内容，返回空流");
      return Flux.empty();
    }
    
    // 有消息时，执行完整的AI聊天流
    log.info("开始处理聊天消息，会话ID: {}, 消息长度: {}, 搜索开启: {}, 深度思考: {}, 用户ID: {}, 指定模型: {}-{}", 
        request.getConversationId(), request.getMessage().length(), request.isSearchEnabled(), 
        request.isDeepThinking(), request.getUserId(), request.getProvider(), request.getModel());
    
    return aiChatService.streamChat(request)
        .doOnNext(event -> {
          if (log.isDebugEnabled()) {
            if (event.getData() instanceof SseEventResponse.ChunkData data) {
              String content = data.getContent();
              String escaped = content != null ? content.replace("\n", "\\n") : "";
              log.debug("发送SSE事件: {} - chunk(len={}, preview={})", event.getType(),
                  content != null ? content.length() : 0,
                  escaped.length() > 200 ? escaped.substring(0, 200) + "..." : escaped);
            } else {
              log.debug("发送SSE事件: {} - {}", event.getType(), event.getData());
            }
          }
        })
        .doOnError(error -> log.error("流式聊天发生错误，会话ID: {}", request.getConversationId(), error))
        .doOnComplete(() -> log.info("流式聊天完成，会话ID: {}", request.getConversationId()));
  }

}
