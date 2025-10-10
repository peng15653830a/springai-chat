package com.example.novel.entity;

import com.example.entity.BaseSession;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovelSession implements BaseSession {
  private Long id;
  private String title;
  private String model;
  private Double temperature;
  private Integer maxTokens;
  private Double topP;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

