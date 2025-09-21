package com.example.service;

import com.example.dto.request.StructuredOutputRequest;
import reactor.core.publisher.Mono;

/**
 * 结构化输出验证服务接口
 *
 * @author xupeng
 */
public interface StructuredOutputValidator {

  /**
   * 验证输出内容是否符合指定的JSON Schema
   *
   * @param content 输出内容
   * @param request 结构化输出请求配置
   * @return 验证结果的Mono包装
   */
  Mono<ValidationResult> validateOutput(String content, StructuredOutputRequest request);

  /** 验证结果类 */
  record ValidationResult(
      boolean isValid, String errorMessage, String validatedContent, String originalContent) {

    public static ValidationResult success(String content) {
      return new ValidationResult(true, null, content, content);
    }

    public static ValidationResult success(String validatedContent, String originalContent) {
      return new ValidationResult(true, null, validatedContent, originalContent);
    }

    public static ValidationResult failure(String errorMessage, String originalContent) {
      return new ValidationResult(false, errorMessage, null, originalContent);
    }
  }
}
