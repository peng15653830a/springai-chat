package com.example.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对话实体类，表示用户与AI的一次对话会话
 *
 * @author xupeng
 */
@Data
public class Conversation {
  /** 对话ID */
  private Long id;
  
  /** 用户ID */
  private Long userId;
  
  /** 对话标题 */
  private String title;
  
  /** 创建时间 */
  private LocalDateTime createdAt;
  
  /** 更新时间 */
  private LocalDateTime updatedAt;
}
