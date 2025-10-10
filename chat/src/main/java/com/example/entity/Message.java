package com.example.entity;

import com.example.dto.response.SearchResult;
import com.example.entity.BaseMessage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 消息实体类，表示一次对话中的消息
 */
@Data
public class Message implements BaseMessage {
  private Long id;
  private Long conversationId;
  private String role;
  private String content;
  private String thinking;
  private List<SearchResult> searchResults;
  private LocalDateTime createdAt;

  // 实现BaseMessage接口的getSessionId方法
  @Override
  public Long getSessionId() {
    return conversationId;
  }

  // 实现BaseMessage接口的setSessionId方法
  @Override
  public void setSessionId(Long sessionId) {
    this.conversationId = sessionId;
  }
}

