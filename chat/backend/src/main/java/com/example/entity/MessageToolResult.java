package com.example.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息工具调用结果实体类 用于存储AI消息中的工具调用结果（如搜索结果）
 *
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageToolResult {

  /** 主键ID */
  private Long id;

  /** 关联的消息ID */
  private Long messageId;

  /** 工具名称（如：webSearch） */
  private String toolName;

  /** 工具调用序号（同一消息中的第几次工具调用，从1开始） */
  private Integer callSequence;

  /** 工具输入参数（JSON格式） */
  private String toolInput;

  /** 工具调用结果（JSON格式） */
  private String toolResult;

  /** 工具调用状态：SUCCESS, FAILED, IN_PROGRESS */
  private String status;

  /** 错误信息（如果调用失败） */
  private String errorMessage;

  /** 创建时间 */
  private LocalDateTime createdAt;

  /** 更新时间 */
  private LocalDateTime updatedAt;
}
