package com.example.handler;

import com.example.dto.stream.ChatEvent;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 默认错误处理：将异常映射为统一的 ChatEvent.error。
 */
@Slf4j
@Service
public class DefaultChatErrorHandler implements ChatErrorHandler {

  private static final String API_KEY_ERROR_MSG = "api key";
  private static final String QUOTA_ERROR_MSG = "quota";
  private static final String MODEL_ERROR_MSG = "model";
  private static final String INVALID_ERROR_MSG = "invalid";
  private static final String ILLEGAL_STATE_ERROR = "IllegalState";

  @Override
  public Flux<ChatEvent> handleChatError(Throwable error) {
    log.error("聊天过程中发生错误", error);
    return Flux.just(ChatEvent.error(getErrorMessage(error)));
  }

  @Override
  public String getErrorMessage(Throwable error) {
    if (error == null) return "未知错误";
    ErrorType errorType = getErrorType(error);
    return switch (errorType) {
      case NETWORK_ERROR -> "网络连接异常，请检查网络设置后重试";
      case TIMEOUT_ERROR -> "请求超时，请稍后重试";
      case API_KEY_ERROR -> "API密钥配置错误，请联系管理员";
      case QUOTA_EXCEEDED -> "API调用配额已用完，请稍后重试";
      case MODEL_UNAVAILABLE -> "所选模型暂时不可用，请尝试其他模型";
      case INVALID_REQUEST -> "请求参数错误: " + error.getMessage();
      case INTERNAL_ERROR -> "系统内部错误，请稍后重试";
      case UNKNOWN_ERROR -> "发生未知错误: " + (error.getMessage() == null ? "" : error.getMessage());
    };
  }

  @Override
  public boolean isRetryableError(Throwable error) {
    ErrorType errorType = getErrorType(error);
    return switch (errorType) {
      case NETWORK_ERROR, TIMEOUT_ERROR, INTERNAL_ERROR -> true;
      case API_KEY_ERROR, QUOTA_EXCEEDED, MODEL_UNAVAILABLE, INVALID_REQUEST, UNKNOWN_ERROR -> false;
    };
  }

  @Override
  public ErrorType getErrorType(Throwable error) {
    if (error == null) return ErrorType.UNKNOWN_ERROR;
    String msg = error.getMessage() == null ? "" : error.getMessage();
    String cls = error.getClass().getSimpleName();

    if (error instanceof ConnectException
        || error instanceof java.net.UnknownHostException
        || msg.contains("Connection refused")
        || msg.contains("No route to host")) {
      return ErrorType.NETWORK_ERROR;
    }
    if (error instanceof TimeoutException
        || error instanceof SocketTimeoutException
        || cls.contains("Timeout")
        || msg.contains("timeout")
        || msg.contains("timed out")) {
      return ErrorType.TIMEOUT_ERROR;
    }
    if (msg.contains(API_KEY_ERROR_MSG)
        || msg.contains("authentication")
        || msg.contains("unauthorized")
        || msg.contains("invalid key")) {
      return ErrorType.API_KEY_ERROR;
    }
    if (msg.contains(QUOTA_ERROR_MSG)
        || msg.contains("rate limit")
        || msg.contains("exceeded")
        || msg.contains("too many requests")) {
      return ErrorType.QUOTA_EXCEEDED;
    }
    if (msg.contains(MODEL_ERROR_MSG)
        && (msg.contains("not found") || msg.contains("unavailable") || msg.contains("not supported"))) {
      return ErrorType.MODEL_UNAVAILABLE;
    }
    if (msg.contains(INVALID_ERROR_MSG)
        || msg.contains("bad request")
        || msg.contains("parameter")
        || cls.contains("IllegalArgument")) {
      return ErrorType.INVALID_REQUEST;
    }
    if (cls.contains(ILLEGAL_STATE_ERROR)
        || cls.contains("NullPointer")
        || cls.contains("Runtime")
        || msg.contains("internal error")) {
      return ErrorType.INTERNAL_ERROR;
    }
    return ErrorType.UNKNOWN_ERROR;
  }
}

