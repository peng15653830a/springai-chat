package com.example.entity;

import java.time.LocalDateTime;

/**
 * 基础会话实体接口
 * 定义所有会话类型的通用字段和行为
 *
 * @author 系统自动整合
 */
public interface BaseSession {

  /**
   * 获取会话ID
   */
  Long getId();

  /**
   * 设置会话ID
   */
  void setId(Long id);

  /**
   * 获取会话标题
   */
  String getTitle();

  /**
   * 设置会话标题
   */
  void setTitle(String title);

  /**
   * 获取创建时间
   */
  LocalDateTime getCreatedAt();

  /**
   * 设置创建时间
   */
  void setCreatedAt(LocalDateTime createdAt);

  /**
   * 获取更新时间
   */
  LocalDateTime getUpdatedAt();

  /**
   * 设置更新时间
   */
  void setUpdatedAt(LocalDateTime updatedAt);
}