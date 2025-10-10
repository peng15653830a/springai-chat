package com.example.novel.entity;

import com.example.entity.BaseMessage;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovelMessage implements BaseMessage {
  private Long id;
  private Long sessionId;
  private String role; // user/assistant/system
  private String content;
  private LocalDateTime createdAt;
}

