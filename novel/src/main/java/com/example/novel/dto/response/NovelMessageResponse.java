package com.example.novel.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovelMessageResponse {
  private Long id;
  private Long sessionId;
  private String role;
  private String content;
  private LocalDateTime createdAt;
}

