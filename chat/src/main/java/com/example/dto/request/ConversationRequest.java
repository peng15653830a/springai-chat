package com.example.dto.request;

import lombok.Data;

/**
 * 对话请求DTO
 *
 * @author xupeng
 */
@Data
public class ConversationRequest {
  /** 用户ID */
  private Long userId;
  /** 对话标题 */
  private String title;
}
