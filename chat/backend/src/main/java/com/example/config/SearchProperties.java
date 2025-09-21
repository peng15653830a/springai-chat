package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 搜索相关配置属性
 *
 * @author xupeng
 */
@Data
@Component
@ConfigurationProperties(prefix = "search")
public class SearchProperties {

  /** 是否启用搜索功能 */
  private boolean enabled = true;

  /** Tavily搜索API配置 */
  private Tavily tavily = new Tavily();

  @Data
  public static class Tavily {
    /** Tavily API密钥 */
    private String apiKey;

    /** Tavily API基础URL */
    private String baseUrl = "https://api.tavily.com/search";
  }
}
