package com.example.service;

import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.SseEventResponse;
import reactor.core.publisher.Flux;

/**
 * AI聊天服务接口（响应式架构）
 *
 * @author xupeng
 */
public interface AiChatService {


  /**
   * 响应式流式聊天（使用封装的请求对象）
   *
   * @param request 流式聊天请求对象，包含所有参数
   * @return 响应式SSE事件流
   */
  Flux<SseEventResponse> streamChat(StreamChatRequest request);

  /**
   * 响应式流式聊天（支持模型选择）
   *
   * @param params 聊天执行参数，包含会话ID、用户消息、搜索开关、深度思考模式、用户ID和模型信息
   * @return 响应式SSE事件流
   */
  Flux<SseEventResponse> streamChatWithModel(com.example.dto.request.ChatExecutionParams params);

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