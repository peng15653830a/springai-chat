package com.example.dto;

import lombok.Data;

/**
 * 对话请求DTO，用于接收创建对话的请求参数
 *
 * @author xupeng
 */
@Data
public class ConversationRequest {
  /** 对话标题 */
  private String title;
}
