package com.example.handler;

import com.example.dto.stream.ChatEvent;
import reactor.core.publisher.Flux;

/**
 * 聊天错误处理器接口 负责处理聊天过程中的各种错误情况。
 */
public interface ChatErrorHandler {

  Flux<ChatEvent> handleChatError(Throwable error);

  String getErrorMessage(Throwable error);

  boolean isRetryableError(Throwable error);

  enum ErrorType {
    NETWORK_ERROR,
    TIMEOUT_ERROR,
    API_KEY_ERROR,
    QUOTA_EXCEEDED,
    MODEL_UNAVAILABLE,
    INVALID_REQUEST,
    INTERNAL_ERROR,
    UNKNOWN_ERROR
  }

  ErrorType getErrorType(Throwable error);
}

