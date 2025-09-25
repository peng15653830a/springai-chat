package com.example.exception;

import com.example.dto.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebExchange;

/**
 * 全局异常处理器（WebFlux），统一处理系统中抛出的异常。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleBusinessException(
      BusinessException e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    log.warn("业务异常 - 请求路径: {}, 错误码: {}", path, e.getCode(), e);
    return ApiResponse.error(e.getCode(), e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleValidationException(
      MethodArgumentNotValidException e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    log.warn("参数验证异常 - 请求路径: {}", path, e);

    Map<String, String> errors = new HashMap<>(e.getBindingResult().getFieldErrorCount());
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
              log.debug("验证错误 - 字段: {}, 错误: {}", fieldName, errorMessage);
            });

    return ApiResponse.error("VALIDATION_ERROR", "参数验证失败", errors);
  }

  @ExceptionHandler(BindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleBindException(BindException e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    log.warn("参数绑定异常 - 请求路径: {}", path, e);

    Map<String, String> errors = new HashMap<>(e.getBindingResult().getFieldErrorCount());
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
              log.debug("绑定错误 - 字段: {}, 错误: {}", fieldName, errorMessage);
            });

    return ApiResponse.error("BIND_ERROR", "参数绑定失败", errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleTypeMismatchException(
      MethodArgumentTypeMismatchException e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    log.warn("参数类型不匹配异常 - 请求路径: {}, 参数名: {}", path, e.getName(), e);

    String message =
        String.format(
            "参数 '%s' 类型不匹配，期望类型: %s",
            e.getName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");

    return ApiResponse.error("TYPE_MISMATCH", message);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleIllegalArgumentException(
      IllegalArgumentException e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    log.warn("非法参数异常 - 请求路径: {}", path, e);
    return ApiResponse.error(e.getMessage());
  }

  @ExceptionHandler(NullPointerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Object> handleNullPointerException(
      NullPointerException e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    log.error("空指针异常 - 请求路径: {}", path, e);
    return ApiResponse.error("系统内部错误，请联系管理员");
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Object> handleRuntimeException(
      RuntimeException e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    log.error("运行时异常 - 请求路径: {}", path, e);
    return ApiResponse.error("系统运行异常: " + e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Object> handleGeneralException(
      Exception e, ServerWebExchange exchange) {
    String path = exchange != null ? exchange.getRequest().getURI().getPath() : "Unknown";
    String method =
        exchange != null && exchange.getRequest().getMethod() != null
            ? exchange.getRequest().getMethod().name()
            : "UNKNOWN";
    log.error("未知异常 - 请求路径: {}, Method: {}", path, method, e);
    return ApiResponse.error("系统错误，请联系管理员");
  }
}
