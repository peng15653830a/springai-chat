package com.example.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天响应数据传输对象
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {

  /** 响应ID */
  private String id;

  /** 对象类型 */
  private String object;

  /** 创建时间 */
  private Long created;

  /** 模型名称 */
  private String model;

  /** 选择列表 */
  private List<Choice> choices;

  /** 使用统计 */
  private Usage usage;

  /** 选择项数据传输对象 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Choice {

    /** 索引 */
    private Integer index;

    /** 消息 */
    private ResponseMessage message;

    /** 流式响应增量数据 */
    private ResponseMessage delta;

    /** 完成原因 */
    @JsonProperty("finish_reason")
    private String finishReason;
  }

  /** 响应消息数据传输对象 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ResponseMessage {

    /** 角色 */
    private String role;

    /** 内容 */
    private String content;

    /** 思考过程 */
    private String thinking;

    /** 推理内容（Qwen模型专用） */
    @JsonProperty("reasoning_content")
    private String reasoningContent;
  }

  /** 使用统计数据传输对象 */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Usage {

    /** 提示token数 */
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    /** 完成token数 */
    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    /** 总token数 */
    @JsonProperty("total_tokens")
    private Integer totalTokens;
  }
}
