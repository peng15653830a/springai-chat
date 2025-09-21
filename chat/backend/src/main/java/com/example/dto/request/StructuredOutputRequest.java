package com.example.dto.request;

import lombok.Data;

/**
 * 结构化输出请求
 *
 * @author xupeng
 */
@Data
public class StructuredOutputRequest {
  /** 是否启用结构化输出 */
  private boolean structuredOutputEnabled = false;

  /** JSON Schema定义（用于验证输出格式） */
  private String jsonSchema;

  /** 输出格式类型 */
  private OutputFormat outputFormat = OutputFormat.JSON;

  /** 验证严格程度 */
  private ValidationLevel validationLevel = ValidationLevel.WARN;

  /** 输出格式枚举 */
  public enum OutputFormat {
    /** JSON格式 */
    JSON,
    /** XML格式 */
    XML,
    /** YAML格式 */
    YAML,
    /** 纯文本 */
    TEXT
  }

  /** 验证级别枚举 */
  public enum ValidationLevel {
    /** 严格验证，不符合则报错 */
    STRICT,
    /** 警告级别，记录日志但继续处理 */
    WARN,
    /** 忽略验证错误 */
    IGNORE
  }
}
