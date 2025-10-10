package com.example.entity;

import java.time.LocalDateTime;

/**
 * 基础消息实体接口
 * 定义所有消息类型的通用字段和行为
 *
 * @author 系统自动整合
 */
public interface BaseMessage {

  /**
   * 获取消息ID
   */
  Long getId();

  /**
   * 设置消息ID
   */
  void setId(Long id);

  /**
   * 获取会话ID（可能是conversationId或sessionId）
   */
  Long getSessionId();

  /**
   * 设置会话ID
   */
  void setSessionId(Long sessionId);

  /**
   * 获取消息角色（user/assistant/system）
   */
  String getRole();

  /**
   * 设置消息角色
   */
  void setRole(String role);

  /**
   * 获取消息内容
   */
  String getContent();

  /**
   * 设置消息内容
   */
  void setContent(String content);

  /**
   * 获取创建时间
   */
  LocalDateTime getCreatedAt();

  /**
   * 设置创建时间
   */
  void setCreatedAt(LocalDateTime createdAt);
}