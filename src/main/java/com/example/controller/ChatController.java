package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.MessageRequest;
import com.example.entity.Message;
import com.example.service.AiChatService;
import com.example.service.dto.SseEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

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
   * SSE聊天端点 - 按需建立连接处理消息
   *
   * @param conversationId 会话ID
   * @param message 用户消息（可选）
   * @param searchEnabled 是否启用搜索
   * @param deepThinking 是否启用深度思考模式
   * @return 响应式SSE事件流
   */
  @GetMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<SseEventResponse> streamChat(
      @PathVariable Long conversationId,
      @RequestParam(required = false) String message,
      @RequestParam(defaultValue = "false") boolean searchEnabled,
      @RequestParam(defaultValue = "false") boolean deepThinking) {
    
    log.info("SSE连接请求，会话ID: {}, 是否有消息: {}", conversationId, message != null);
    
    if (message == null || message.trim().isEmpty()) {
      // 无消息时返回空流，连接会自然结束
      log.debug("无消息内容，返回空流");
      return Flux.empty();
    }
    
    // 有消息时，执行完整的AI聊天流
    log.info("开始处理聊天消息，会话ID: {}, 消息长度: {}, 搜索开启: {}, 深度思考: {}", 
        conversationId, message.length(), searchEnabled, deepThinking);
    
    return aiChatService.streamChat(conversationId, message, searchEnabled, deepThinking)
        .doOnNext(event -> log.debug("发送SSE事件: {} - {}", event.getType(), 
            event.getData() instanceof SseEventResponse.ChunkData ? "chunk" : event.getData()))
        .doOnError(error -> log.error("流式聊天发生错误，会话ID: {}", conversationId, error))
        .doOnComplete(() -> log.info("流式聊天完成，会话ID: {}", conversationId));
  }

}
