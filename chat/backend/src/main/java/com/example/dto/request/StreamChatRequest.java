package com.example.dto.request;

import lombok.Data;

/**
 * 流式聊天请求DTO，封装流式聊天接口的所有参数 利用Spring自动参数绑定机制，根据字段名自动匹配URL路径参数和查询参数
 *
 * @author xupeng
 */
@Data
public class StreamChatRequest {

  /** 会话ID（来自路径参数 {conversationId}） */
  private Long conversationId;

  /** 用户消息内容（来自查询参数 message，可选） */
  private String message;

  /** 是否启用搜索功能（来自查询参数 searchEnabled，默认为false） */
  private boolean searchEnabled = false;

  /** 是否启用深度思考模式（来自查询参数 deepThinking，默认为false） */
  private boolean deepThinking = false;

  /** 用户ID（来自查询参数 userId，可选，用于获取用户模型偏好） */
  private Long userId;

  /** 指定的模型提供者（来自查询参数 provider，可选，如qwen、openai等） */
  private String provider;

  /** 指定的模型名称（来自查询参数 model，可选） */
  private String model;
}
