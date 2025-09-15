package com.example.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类，表示一次对话中的消息
 *
 * @author xupeng
 */
@Data
public class Message {
  /** 消息ID */
  private Long id;
  
  /** 会话ID */
  private Long conversationId;
  
  /** 消息角色（用户或AI） */
  private String role;
  
  /** 消息内容 */
  private String content;
  
  /** AI推理过程 */
  private String thinking;
  
  // 搜索结果已迁移到message_tool_results表，支持一对多关系
  
  /** 创建时间 */
  private LocalDateTime createdAt;
}
