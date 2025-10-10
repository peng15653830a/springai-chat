package com.example.novel.dto.request.story;

import lombok.Data;

@Data
public class StoryInitRequest {
  private String background; // 故事背景/世界观/人物设定
  private String style;      // 目标风格/口吻/约束
  private String title;      // 作品标题（可选）
}

