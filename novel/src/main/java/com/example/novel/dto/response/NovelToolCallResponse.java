package com.example.novel.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovelToolCallResponse {
  private Long id;
  private Long sessionId;
  private Long messageId;
  private String toolName;
  private String inputJson;
  private String resultJson;
  private String status;
  private String errorMessage;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

