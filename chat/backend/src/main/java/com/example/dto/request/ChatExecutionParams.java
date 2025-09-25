package com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天执行参数类 用于Service层内部方法参数传递，统一替代以下类： - BuildPromptAndStreamChatParams - StreamingChatParams
 *
 * @author xupeng
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatExecutionParams {

  // ========================= 基础信息 =========================

  /** 会话ID */
  private Long conversationId;

  /** 用户ID（可选，用于获取用户模型偏好） */
  private Long userId;

  /** 是否启用深度思考 */
  private boolean deepThinking;

  /** 模型提供者名称 */
  private String providerName;

  /** 模型名称 */
  private String modelName;

  // ========================= 消息相关 =========================

  /** 用户原始消息内容 */
  private String userMessage;

  /** 构建后的完整提示内容（包含历史对话和搜索上下文） */
  private String prompt;

  /** 是否启用搜索功能 */
  private boolean searchEnabled;

  // ========================= 便利方法 =========================

  /** 从StreamChatRequest创建ChatExecutionParams的便利方法 */
  public static ChatExecutionParams from(StreamChatRequest request) {
    return ChatExecutionParams.builder()
        .conversationId(request.getConversationId())
        .userMessage(request.getMessage())
        .searchEnabled(request.isSearchEnabled())
        .deepThinking(request.isDeepThinking())
        .userId(request.getUserId())
        .providerName(request.getProvider())
        .modelName(request.getModel())
        .build();
  }

  /** 创建用于执行流式聊天的参数（已有prompt） */
  public static ChatExecutionParams forExecution(
      String prompt,
      Long conversationId,
      boolean deepThinking,
      String providerName,
      String modelName) {
    return ChatExecutionParams.builder()
        .prompt(prompt)
        .conversationId(conversationId)
        .deepThinking(deepThinking)
        .providerName(providerName)
        .modelName(modelName)
        .build();
  }
}
