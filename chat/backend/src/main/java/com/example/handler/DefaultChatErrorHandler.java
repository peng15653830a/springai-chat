package com.example.handler;

import com.example.dto.stream.ChatEvent;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 默认的聊天错误处理器实现
 *
 * @author xupeng
 */
@Slf4j
@Service
public class DefaultChatErrorHandler implements ChatErrorHandler {

  /** API密钥错误消息 */
  private static final String API_KEY_ERROR_MSG = "api key";
  /** 配额错误消息 */
  private static final String QUOTA_ERROR_MSG = "quota";
  /** 模型错误消息 */
  private static final String MODEL_ERROR_MSG = "model";
  /** 无效请求错误消息 */
  private static final String INVALID_ERROR_MSG = "invalid";
  /** 非法状态错误 */
  private static final String ILLEGAL_STATE_ERROR = "IllegalState";

  @Override
  public Flux<ChatEvent> handleChatError(Throwable error) {
    log.error("聊天过程中发生错误", error);

    String errorMessage = getErrorMessage(error);
    ErrorType errorType = getErrorType(error);

    log.debug("错误类型: {}, 错误消息: {}", errorType, errorMessage);

    return Flux.just(ChatEvent.error(errorMessage));
  }

  @Override
  public String getErrorMessage(Throwable error) {
    if (error == null) {
      return "未知错误";
    }

    ErrorType errorType = getErrorType(error);

    return switch (errorType) {
      case NETWORK_ERROR -> "网络连接异常，请检查网络设置后重试";
      case TIMEOUT_ERROR -> "请求超时，请稍后重试";
      case API_KEY_ERROR -> "API密钥配置错误，请联系管理员";
      case QUOTA_EXCEEDED -> "API调用配额已用完，请稍后重试";
      case MODEL_UNAVAILABLE -> "所选模型暂时不可用，请尝试其他模型";
      case INVALID_REQUEST -> "请求参数错误: " + error.getMessage();
      case INTERNAL_ERROR -> "系统内部错误，请稍后重试";
      case UNKNOWN_ERROR -> "发生未知错误: " + error.getMessage();
    };
  }

  @Override
  public boolean isRetryableError(Throwable error) {
    ErrorType errorType = getErrorType(error);

    // 未知错误默认不重试
    return switch (errorType) {
      case NETWORK_ERROR, TIMEOUT_ERROR, INTERNAL_ERROR -> true;
      case API_KEY_ERROR, QUOTA_EXCEEDED, MODEL_UNAVAILABLE, INVALID_REQUEST -> false;
      case UNKNOWN_ERROR -> false;
    };
  }

  @Override
  public ErrorType getErrorType(Throwable error) {
    if (error == null) {
      return ErrorType.UNKNOWN_ERROR;
    }

    String errorMessage = error.getMessage();
    String errorClass = error.getClass().getSimpleName();

    // 处理errorMessage为null的情况
    if (errorMessage == null) {
      errorMessage = "";
    }

    // 网络相关错误
    if (error instanceof ConnectException
        || error instanceof java.net.UnknownHostException
        || errorMessage.contains("Connection refused")
        || errorMessage.contains("No route to host")) {
      return ErrorType.NETWORK_ERROR;
    }

    // 超时错误
    if (error instanceof TimeoutException
        || error instanceof SocketTimeoutException
        || errorClass.contains("Timeout")
        || errorMessage.contains("timeout")
        || errorMessage.contains("timed out")) {
      return ErrorType.TIMEOUT_ERROR;
    }

    // API密钥错误
    if (errorMessage.contains(API_KEY_ERROR_MSG)
        || errorMessage.contains("authentication")
        || errorMessage.contains("unauthorized")
        || errorMessage.contains("invalid key")) {
      return ErrorType.API_KEY_ERROR;
    }

    // 配额不足
    if (isQuotaExceededError(errorMessage)) {
      return ErrorType.QUOTA_EXCEEDED;
    }

    // 模型不可用
    if (isModelUnavailableError(errorMessage)) {
      return ErrorType.MODEL_UNAVAILABLE;
    }

    // 请求参数错误
    if (errorMessage.contains(INVALID_ERROR_MSG)
        || errorMessage.contains("bad request")
        || errorMessage.contains("parameter")
        || errorClass.contains("IllegalArgument")) {
      return ErrorType.INVALID_REQUEST;
    }

    // 系统内部错误
    if (isInternalError(errorClass, errorMessage)) {
      return ErrorType.INTERNAL_ERROR;
    }

    // 默认为未知错误
    return ErrorType.UNKNOWN_ERROR;
  }

  /** 检查是否为系统内部错误 */
  private boolean isInternalError(String errorClass, String errorMessage) {
    return errorClass.contains(ILLEGAL_STATE_ERROR)
        || errorClass.contains("NullPointer")
        || errorClass.contains("Runtime")
        || errorMessage.contains("internal error");
  }

  /** 检查是否为配额超出错误 */
  private boolean isQuotaExceededError(String errorMessage) {
    return errorMessage.contains(QUOTA_ERROR_MSG)
        || errorMessage.contains("rate limit")
        || errorMessage.contains("exceeded")
        || errorMessage.contains("too many requests");
  }

  /** 检查是否为模型不可用错误 */
  private boolean isModelUnavailableError(String errorMessage) {
    return errorMessage.contains(MODEL_ERROR_MSG)
        && (errorMessage.contains("not found")
            || errorMessage.contains("unavailable")
            || errorMessage.contains("not supported"));
  }
}
