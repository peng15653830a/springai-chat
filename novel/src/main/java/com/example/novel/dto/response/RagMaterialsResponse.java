package com.example.novel.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class RagMaterialsResponse {
  private Boolean success;
  private String message;
  private Integer totalFiles;
  private Integer totalChunks;
  private List<Item> items;

  @Data
  public static class Item {
    private String source;
    private String title;
    private Integer chunks;
  }
}

