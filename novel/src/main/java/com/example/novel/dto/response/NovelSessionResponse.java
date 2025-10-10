package com.example.novel.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovelSessionResponse {
  private Long id;
  private String title;
  private String model;
  private Double temperature;
  private Integer maxTokens;
  private Double topP;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

