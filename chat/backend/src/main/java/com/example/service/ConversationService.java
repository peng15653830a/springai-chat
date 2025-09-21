package com.example.service;

import com.example.entity.Conversation;
import com.example.entity.Message;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * 对话服务接口
 *
 * @author xupeng
 */
public interface ConversationService {

  /**
   * 创建对话
   *
   * @param userId 用户ID
   * @param title 对话标题
   * @return 对话实体
   */
  Conversation createConversation(Long userId, String title);

  /**
   * 根据ID获取对话
   *
   * @param conversationId 对话ID
   * @return 对话实体
   */
  Conversation getConversationById(Long conversationId);

  /**
   * 获取用户对话列表
   *
   * @param userId 用户ID
   * @return 对话列表
   */
  List<Conversation> getUserConversations(Long userId);

  /**
   * 获取用户最近的对话列表
   *
   * @param userId 用户ID
   * @param limit 限制数量
   * @return 对话列表
   */
  List<Conversation> getRecentConversations(Long userId, int limit);

  /**
   * 更新对话标题
   *
   * @param conversationId 对话ID
   * @param title 对话标题
   */
  void updateConversationTitle(Long conversationId, String title);

  /**
   * 删除对话
   *
   * @param conversationId 对话ID
   */
  void deleteConversation(Long conversationId);

  /**
   * 获取对话消息列表
   *
   * @param conversationId 对话ID
   * @return 消息列表
   */
  List<Message> getConversationMessages(Long conversationId);

  /**
   * 获取对话最近的消息列表
   *
   * @param conversationId 对话ID
   * @param limit 限制数量
   * @return 消息列表
   */
  List<Message> getRecentMessages(Long conversationId, int limit);

  // ========================= 标题管理方法 =========================

  /**
   * 异步生成对话标题（如果需要）
   *
   * @param conversationId 会话ID
   * @param userMessage 用户消息
   * @return 完成的响应式流
   */
  Mono<Void> generateTitleIfNeededAsync(Long conversationId, String userMessage);

  /**
   * 从用户消息生成简洁标题
   *
   * @param message 用户消息
   * @return 生成的标题
   */
  String generateTitleFromMessage(String message);
}
