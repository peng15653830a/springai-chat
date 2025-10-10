package com.example.stream;

import lombok.Builder;
import lombok.Data;

/**
 * 通用的文本流请求参数。
 */
@Data
@Builder
public class TextStreamRequest {
  private String provider;
  private String model;
  private String prompt;

  private Double temperature;
  private Integer maxTokens;
  private Double topP;

  private Long userId;
  private Long conversationId;
  private boolean deepThinking;
  private boolean searchEnabled;

  /** 可选：用于绑定消息ID（如需工具调用精确归属） */
  private Long assistantMessageId;
}

