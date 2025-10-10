package com.example.novel.dto.response.story;

import lombok.Data;

@Data
public class StoryInitResponse {
  private boolean success;
  private Long sessionId;
  private String message;
}

