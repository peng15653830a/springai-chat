package com.example.novel.dto.response.story;

import lombok.Data;

@Data
public class SegmentDTO {
  private int index;         // 段序号（从1开始）
  private String title;      // 小节标题
  private String prompt;     // 剧情要点
  private String starter;    // 开头句
  private String status;     // draft/approved/needs_revision
  private Integer version;   // 最新版本号
  private String latestText; // 最新文本（可选）
}

