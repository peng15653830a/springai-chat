package com.example.novel.dto.request.story;

import java.util.List;
import lombok.Data;

@Data
public class OutlineRequest {
  private List<Item> items; // 顺序即段落顺序

  @Data
  public static class Item {
    private String title;    // 段落标题/小节名（可选）
    private String prompt;   // 段落要点/剧情概述
    private String starter;  // 开头句（可选）
  }
}

