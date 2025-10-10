package com.example.novel.dto.response;

import lombok.Data;

@Data
public class RagCrawlResponse {
  private Boolean success;
  private String message;
  private Integer pagesFetched;
  private Integer totalChunks;
  private java.util.List<String> errors;
  private String styleProfilePath; // 可选：文风画像保存路径
}
