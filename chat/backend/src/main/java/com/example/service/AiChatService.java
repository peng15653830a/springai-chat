package com.example.service;

import com.example.dto.request.StreamChatRequest;
import com.example.dto.stream.ChatEvent;
import reactor.core.publisher.Flux;

/**
 * AI聊天服务接口（响应式架构） 简化后的接口，只保留核心的流式聊天功能
 *
 * @author xupeng
 */
public interface AiChatService {

  /**
   * 响应式流式聊天
   *
   * @param request 流式聊天请求对象，包含所有参数
   * @return 响应式SSE事件流
   */
  Flux<ChatEvent> streamChat(StreamChatRequest request);
}
