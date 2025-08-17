package com.example.exception;

import com.example.dto.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器，统一处理系统中抛出的异常
 *
 * @author xupeng
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 处理业务异常
   *
   * @param e 业务异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleBusinessException(
      BusinessException e, HttpServletRequest request) {
    log.warn("业务异常 - 请求路径: {}, 错误码: {}", request.getRequestURI(), e.getCode(), e);
    return ApiResponse.error(e.getCode(), e.getMessage());
  }

  /**
   * 处理参数验证异常
   *
   * @param e 参数验证异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleValidationException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    log.warn("参数验证异常 - 请求路径: {}", request.getRequestURI(), e);

    // 初始化指定容量的HashMap
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

  /**
   * 处理绑定异常
   *
   * @param e 绑定异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(BindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleBindException(BindException e, HttpServletRequest request) {
    log.warn("参数绑定异常 - 请求路径: {}", request.getRequestURI(), e);

    // 初始化指定容量的HashMap
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

  /**
   * 处理参数类型不匹配异常
   *
   * @param e 参数类型不匹配异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleTypeMismatchException(
      MethodArgumentTypeMismatchException e, HttpServletRequest request) {
    log.warn("参数类型不匹配异常 - 请求路径: {}, 参数名: {}", request.getRequestURI(), e.getName(), e);

    String message =
        String.format(
            "参数 '%s' 类型不匹配，期望类型: %s",
            e.getName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");

    return ApiResponse.error("TYPE_MISMATCH", message);
  }

  /**
   * 处理IllegalArgumentException
   *
   * @param e 非法参数异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Object> handleIllegalArgumentException(
      IllegalArgumentException e, HttpServletRequest request) {
    log.warn("非法参数异常 - 请求路径: {}", request.getRequestURI(), e);
    return ApiResponse.error(e.getMessage());
  }

  /**
   * 处理NullPointerException
   *
   * @param e 空指针异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(NullPointerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Object> handleNullPointerException(
      NullPointerException e, HttpServletRequest request) {
    log.error("空指针异常 - 请求路径: {}", request.getRequestURI(), e);
    return ApiResponse.error("系统内部错误，请联系管理员");
  }

  /**
   * 处理运行时异常
   *
   * @param e 运行时异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Object> handleRuntimeException(
      RuntimeException e, HttpServletRequest request) {
    log.error("运行时异常 - 请求路径: {}", request.getRequestURI(), e);
    return ApiResponse.error("系统运行异常: " + e.getMessage());
  }

  /**
   * 处理所有其他异常
   *
   * @param e 异常
   * @param request HTTP请求
   * @return 包含错误信息的ApiResponse
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Object> handleGeneralException(Exception e, HttpServletRequest request) {
    log.error("未知异常 - 请求路径: {}, Method: {}", request.getRequestURI(), request.getMethod(), e);
    return ApiResponse.error("系统错误，请联系管理员");
  }
}
