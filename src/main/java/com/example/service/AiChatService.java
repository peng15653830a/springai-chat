package com.example.service;

import com.example.service.dto.SseEventResponse;
import reactor.core.publisher.Flux;

/**
 * AI聊天服务接口（响应式架构）
 *
 * @author xupeng
 */
public interface AiChatService {

  /**
   * 响应式流式聊天
   *
   * @param conversationId 会话ID
   * @param userMessage 用户消息
   * @param searchEnabled 搜索开关
   * @param deepThinking 是否启用深度思考模式
   * @return 响应式SSE事件流
   */
  Flux<SseEventResponse> streamChat(Long conversationId, String userMessage, boolean searchEnabled, boolean deepThinking);

  // ========================= 内部流式处理方法 =========================
  
  /**
   * 执行流式AI聊天
   *
   * @param prompt 完整的聊天提示
   * @param conversationId 会话ID（用于保存AI响应）
   * @param deepThinking 是否启用深度思考模式
   * @return 响应式SSE事件流
   */
  Flux<SseEventResponse> executeStreamingChat(String prompt, Long conversationId, boolean deepThinking);

  /**
   * 处理聊天错误
   *
   * @param error 错误对象
   * @return 错误SSE事件流
   */
  Flux<SseEventResponse> handleChatError(Throwable error);

}