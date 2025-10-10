package com.example.novel.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class RagCrawlRequest {
  private String url;
  private Integer maxPages = 200;
  private Boolean sameDomainOnly = true;
  private List<String> includePatterns; // 正则，匹配链接
  private List<String> excludePatterns; // 正则，排除链接
  private String contentSelector; // CSS 选择器，正文容器
  private String titleSelector; // CSS 选择器，标题
  private Integer rateLimitMs = 500;
  private Boolean analyzeStyle = false; // 是否生成文风画像
  private String sitePreset = "xbookcn"; // 站点预设
}
