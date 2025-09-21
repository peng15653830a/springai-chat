package com.example.service.impl;

import com.example.dto.request.StructuredOutputRequest;
import com.example.service.StructuredOutputValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 结构化输出验证服务实现类
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StructuredOutputValidatorImpl implements StructuredOutputValidator {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<ValidationResult> validateOutput(String content, StructuredOutputRequest request) {
    return Mono.fromCallable(() -> performValidation(content, request))
        .doOnNext(
            result -> {
              if (!result.isValid()
                  && request.getValidationLevel() == StructuredOutputRequest.ValidationLevel.WARN) {
                log.warn("⚠️ 结构化输出验证失败: {}", result.errorMessage());
              }
            })
        .onErrorReturn(
            throwable -> {
              log.error("结构化输出验证异常: {}", throwable.getMessage(), throwable);
              return true;
            },
            ValidationResult.failure("验证过程发生异常", content));
  }

  /** 执行实际的验证逻辑 */
  private ValidationResult performValidation(String content, StructuredOutputRequest request) {
    if (!request.isStructuredOutputEnabled()) {
      return ValidationResult.success(content);
    }

    // 根据输出格式进行不同的验证
    switch (request.getOutputFormat()) {
      case JSON:
        return validateJsonFormat(content, request);
      case XML:
        return validateXmlFormat(content, request);
      case YAML:
        return validateYamlFormat(content, request);
      case TEXT:
        return validateTextFormat(content, request);
      default:
        return ValidationResult.failure("不支持的输出格式: " + request.getOutputFormat(), content);
    }
  }

  /** 验证JSON格式 */
  private ValidationResult validateJsonFormat(String content, StructuredOutputRequest request) {
    try {
      // 尝试解析为JSON
      JsonNode jsonNode = objectMapper.readTree(content);

      // 如果提供了JSON Schema，进行Schema验证
      if (request.getJsonSchema() != null && !request.getJsonSchema().trim().isEmpty()) {
        return performJsonSchemaValidation(jsonNode, request.getJsonSchema(), content);
      }

      // 如果没有Schema，只验证是否为有效JSON
      return ValidationResult.success(objectMapper.writeValueAsString(jsonNode), content);

    } catch (Exception e) {
      return ValidationResult.failure("JSON格式验证失败: " + e.getMessage(), content);
    }
  }

  /** 执行JSON Schema验证（简化实现） */
  private ValidationResult performJsonSchemaValidation(
      JsonNode content, String schema, String originalContent) {
    try {
      // 简化的Schema验证实现
      // 在实际项目中，可以使用 networknt/json-schema-validator 等库
      JsonNode schemaNode = objectMapper.readTree(schema);

      // 基础验证：检查必需字段
      final String requiredKey = "required";
      if (schemaNode.has(requiredKey) && schemaNode.get(requiredKey).isArray()) {
        for (JsonNode requiredField : schemaNode.get(requiredKey)) {
          String fieldName = requiredField.asText();
          if (!content.has(fieldName)) {
            return ValidationResult.failure("缺少必需字段: " + fieldName, originalContent);
          }
        }
      }

      return ValidationResult.success(content.toString(), originalContent);

    } catch (Exception e) {
      return ValidationResult.failure("JSON Schema验证失败: " + e.getMessage(), originalContent);
    }
  }

  /** 验证XML格式 */
  private ValidationResult validateXmlFormat(String content, StructuredOutputRequest request) {
    // 简化实现：检查基本XML格式
    final String xmlStart = "<";
    final String xmlEnd = ">";
    if (content.trim().startsWith(xmlStart) && content.trim().endsWith(xmlEnd)) {
      return ValidationResult.success(content);
    }
    return ValidationResult.failure("XML格式验证失败", content);
  }

  /** 验证YAML格式 */
  private ValidationResult validateYamlFormat(String content, StructuredOutputRequest request) {
    // 简化实现：基本的YAML格式检查
    final String yamlColon = ":";
    final String yamlDash = "-";
    if (content.contains(yamlColon) || content.contains(yamlDash)) {
      return ValidationResult.success(content);
    }
    return ValidationResult.failure("YAML格式验证失败", content);
  }

  /** 验证文本格式 */
  private ValidationResult validateTextFormat(String content, StructuredOutputRequest request) {
    // 文本格式总是有效的
    return ValidationResult.success(content);
  }
}
