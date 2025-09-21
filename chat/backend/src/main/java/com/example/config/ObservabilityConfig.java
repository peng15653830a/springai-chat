package com.example.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 观察性配置 - 指标监控和性能追踪
 *
 * @author xupeng
 */
@Configuration
@RequiredArgsConstructor
public class ObservabilityConfig {

  private final MeterRegistry meterRegistry;

  /** AI聊天请求计数器 */
  @Bean
  public Counter aiChatRequestCounter() {
    return Counter.builder("ai.chat.requests.total")
        .description("Total number of AI chat requests")
        .tag("service", "ai-chat")
        .register(meterRegistry);
  }

  /** AI聊天成功请求计数器 */
  @Bean
  public Counter aiChatSuccessCounter() {
    return Counter.builder("ai.chat.requests.success")
        .description("Number of successful AI chat requests")
        .tag("service", "ai-chat")
        .register(meterRegistry);
  }

  /** AI聊天失败请求计数器 */
  @Bean
  public Counter aiChatErrorCounter() {
    return Counter.builder("ai.chat.requests.error")
        .description("Number of failed AI chat requests")
        .tag("service", "ai-chat")
        .register(meterRegistry);
  }

  /** 搜索请求计数器 */
  @Bean
  public Counter searchRequestCounter() {
    return Counter.builder("search.requests.total")
        .description("Total number of search requests")
        .tag("service", "search")
        .register(meterRegistry);
  }

  /** AI响应时间计时器 */
  @Bean
  public Timer aiResponseTimer() {
    return Timer.builder("ai.response.duration")
        .description("AI response time")
        .tag("service", "ai-chat")
        .register(meterRegistry);
  }

  /** 搜索响应时间计时器 */
  @Bean
  public Timer searchResponseTimer() {
    return Timer.builder("search.response.duration")
        .description("Search response time")
        .tag("service", "search")
        .register(meterRegistry);
  }

  /** Tool调用计数器 */
  @Bean
  public Counter toolCallCounter() {
    return Counter.builder("tool.calls.total")
        .description("Total number of tool calls")
        .tag("service", "tool-calling")
        .register(meterRegistry);
  }

  /** SSE连接计数器 */
  @Bean
  public Counter sseConnectionCounter() {
    return Counter.builder("sse.connections.total")
        .description("Total number of SSE connections")
        .tag("service", "sse")
        .register(meterRegistry);
  }
}
