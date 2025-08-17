package com.example.service;

import com.example.entity.Message;
import com.example.service.dto.SseEventResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

  /**
   * 保存用户消息
   *
   * @param conversationId 会话ID
   * @param content 消息内容
   * @return 用户消息实体
   */
  Mono<Message> saveUserMessage(Long conversationId, String content);
}