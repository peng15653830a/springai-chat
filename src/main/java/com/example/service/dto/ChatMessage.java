package com.example.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息数据传输对象
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

  /** 消息角色 */
  private String role;

  /** 消息内容 */
  private String content;

  /** 创建系统消息 */
  public static ChatMessage createSystemMessage(String content) {
    return new ChatMessage("system", content);
  }

  /** 创建用户消息 */
  public static ChatMessage createUserMessage(String content) {
    return new ChatMessage("user", content);
  }

  /** 创建助手消息 */
  public static ChatMessage createAssistantMessage(String content) {
    return new ChatMessage("assistant", content);
  }
}
