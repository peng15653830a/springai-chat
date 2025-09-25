package com.example.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 聊天流式配置属性
 *
 * @author xupeng
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.chat")
public class ChatStreamingProperties {

  /** 最大历史消息数量 */
  private int maxHistorySize = 20;

  /** 响应超时时间 */
  private Duration responseTimeout = Duration.ofSeconds(300);

  /** SSE超时时间 */
  private Duration sseTimeout = Duration.ofSeconds(300);

  /** 流式配置 */
  private Streaming streaming = new Streaming();

  /** 错误处理配置 */
  private Error error = new Error();

  /** 搜索相关配置 */
  private Search search = new Search();

  @Data
  public static class Streaming {
    /** 流式传输块大小 */
    private int chunkSize = 50;

    /** 缓冲超时时间 */
    private Duration bufferTimeout = Duration.ofMillis(100);

    /** 心跳间隔 */
    private Duration heartbeatInterval = Duration.ofSeconds(30);
  }

  @Data
  public static class Error {
    /** 重试次数 */
    private int retryAttempts = 3;

    /** 重试延迟 */
    private Duration retryDelay = Duration.ofMillis(1000);
  }

  @Data
  public static class Search {
    /** 每条消息允许触发搜索工具的最大次数 */
    private int maxToolCalls = 3;
  }
}
