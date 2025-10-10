package com.example.dto.common;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 模型信息DTO。
 */
@Data
public class ModelInfo {

  private Long id;
  private String name;
  private String displayName;
  private Integer maxTokens;
  private BigDecimal temperature;
  private Boolean supportsThinking;
  private Boolean supportsStreaming;
  private Boolean available;
  private Integer sortOrder;

  public String getFullModelId(Long providerId) {
    return providerId + "-" + name;
  }
}
