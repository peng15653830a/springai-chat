package com.example.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类，表示系统的用户信息
 *
 * @author xupeng
 */
@Data
public class User {
  /** 用户ID */
  private Long id;
  
  /** 用户名 */
  private String username;
  
  /** 用户昵称 */
  private String nickname;
  
  /** 创建时间 */
  private LocalDateTime createdAt;
  
  /** 更新时间 */
  private LocalDateTime updatedAt;
}
