package com.example.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 重试和超时策略配置
 *
 * @author xupeng
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.retry-timeout")
public class RetryTimeoutProperties {

  /** AI聊天请求配置 */
  private AiChatConfig aiChat = new AiChatConfig();

  /** 搜索服务配置 */
  private SearchConfig search = new SearchConfig();

  /** 数据库操作配置 */
  private DatabaseConfig database = new DatabaseConfig();

  @Data
  public static class AiChatConfig {
    /** 连接超时时间（秒） */
    private int connectTimeoutSeconds = 30;
    /** 响应超时时间（秒） */
    private int responseTimeoutSeconds = 120;
    /** 重试次数 */
    private int maxRetries = 2;
    /** 重试间隔基数（毫秒） */
    private long retryBackoffMs = 1000;
  }

  @Data
  public static class SearchConfig {
    /** 连接超时时间（秒） */
    private int connectTimeoutSeconds = 10;
    /** 响应超时时间（秒） */
    private int responseTimeoutSeconds = 20;
    /** 重试次数 */
    private int maxRetries = 2;
    /** 重试间隔基数（毫秒） */
    private long retryBackoffMs = 500;
  }

  @Data
  public static class DatabaseConfig {
    /** 查询超时时间（秒） */
    private int queryTimeoutSeconds = 15;
    /** 重试次数 */
    private int maxRetries = 1;
    /** 重试间隔基数（毫秒） */
    private long retryBackoffMs = 200;
  }

  public Duration getAiChatConnectTimeout() {
    return Duration.ofSeconds(aiChat.connectTimeoutSeconds);
  }

  public Duration getAiChatResponseTimeout() {
    return Duration.ofSeconds(aiChat.responseTimeoutSeconds);
  }

  public Duration getSearchConnectTimeout() {
    return Duration.ofSeconds(search.connectTimeoutSeconds);
  }

  public Duration getSearchResponseTimeout() {
    return Duration.ofSeconds(search.responseTimeoutSeconds);
  }

  public Duration getDatabaseQueryTimeout() {
    return Duration.ofSeconds(database.queryTimeoutSeconds);
  }
}
