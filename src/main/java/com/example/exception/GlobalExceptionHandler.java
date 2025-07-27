package com.example.exception;

import com.example.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - 请求路径: {}, 错误码: {}", request.getRequestURI(), e.getCode(), e);
        
        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证异常 - 请求路径: {}", request.getRequestURI(), e);
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.debug("验证错误 - 字段: {}, 错误: {}", fieldName, errorMessage);
        });

        ApiResponse<Object> response = ApiResponse.error("VALIDATION_ERROR", "参数验证失败", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定异常 - 请求路径: {}", request.getRequestURI(), e);
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.debug("绑定错误 - 字段: {}, 错误: {}", fieldName, errorMessage);
        });

        ApiResponse<Object> response = ApiResponse.error("BIND_ERROR", "参数绑定失败", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配异常 - 请求路径: {}, 参数名: {}", request.getRequestURI(), e.getName(), e);
        
        String message = String.format("参数 '%s' 类型不匹配，期望类型: %s", 
                                     e.getName(), 
                                     e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");
        
        ApiResponse<Object> response = ApiResponse.error("TYPE_MISMATCH", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常 - 请求路径: {}", request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error("ILLEGAL_ARGUMENT", "参数错误: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 - 请求路径: {}", request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error("NULL_POINTER", "系统内部错误，请联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 - 请求路径: {}", request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error("RUNTIME_ERROR", "系统运行异常: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("未知异常 - 请求路径: {}, Method: {}", request.getRequestURI(), request.getMethod(), e);
        
        ApiResponse<Object> response = ApiResponse.error("SYSTEM_ERROR", "系统错误，请联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}