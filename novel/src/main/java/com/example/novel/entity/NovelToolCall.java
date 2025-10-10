package com.example.novel.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovelToolCall {
  private Long id;
  private Long sessionId;
  private Long messageId;
  private String toolName;
  private String inputJson;
  private String resultJson;
  private String status; // IN_PROGRESS / SUCCESS / FAILED
  private String errorMessage;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

