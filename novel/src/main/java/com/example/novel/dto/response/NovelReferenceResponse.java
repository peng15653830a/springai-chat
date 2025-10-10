package com.example.novel.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovelReferenceResponse {
  private Long id;
  private Long sessionId;
  private Long messageId;
  private String source;
  private String title;
  private String excerpt;
  private Double similarity;
  private String url;
  private LocalDateTime createdAt;
}

