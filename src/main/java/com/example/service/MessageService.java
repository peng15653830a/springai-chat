package com.example.service;

import com.example.entity.Message;
import java.util.List;

/**
 * 消息服务接口
 *
 * @author xupeng
 */
public interface MessageService {

  /**
   * 保存消息
   *
   * @param conversationId 会话ID
   * @param role 消息角色
   * @param content 消息内容
   * @return 消息实体
   */
  Message saveMessage(Long conversationId, String role, String content);

  /**
   * 保存消息
   *
   * @param conversationId 会话ID
   * @param role 消息角色
   * @param content 消息内容
   * @param searchResults 搜索结果
   * @return 消息实体
   */
  Message saveMessage(Long conversationId, String role, String content, String searchResults);

  /**
   * 保存消息
   *
   * @param conversationId 会话ID
   * @param role 消息角色
   * @param content 消息内容
   * @param thinking 思考过程
   * @param searchResults 搜索结果
   * @return 消息实体
   */
  Message saveMessage(
      Long conversationId, String role, String content, String thinking, String searchResults);

  /**
   * 根据ID获取消息
   *
   * @param messageId 消息ID
   * @return 消息实体
   */
  Message getMessageById(Long messageId);

  /**
   * 根据会话ID获取消息列表
   *
   * @param conversationId 会话ID
   * @return 消息列表
   */
  List<Message> getMessagesByConversationId(Long conversationId);

  /**
   * 删除消息
   *
   * @param messageId 消息ID
   */
  void deleteMessage(Long messageId);
}
