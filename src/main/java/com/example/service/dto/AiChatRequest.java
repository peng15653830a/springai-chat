package com.example.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天请求数据传输对象
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

  /** 模型名称 */
  private String model;

  /** 消息列表 */
  private List<ChatMessage> messages;

  /** 温度参数 */
  private double temperature;

  /** 最大token数 */
  @JsonProperty("max_tokens")
  private int maxTokens;

  /** 是否流式输出 */
  private boolean stream;

  /** 创建聊天请求 */
  public static AiChatRequest create(
      String model, List<ChatMessage> messages, double temperature, int maxTokens, boolean stream) {
    return new AiChatRequest(model, messages, temperature, maxTokens, stream);
  }
}
