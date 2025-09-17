package com.example.service;

import com.example.dto.stream.ChatEvent;
import com.example.entity.Message;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author xupeng
 */
public interface MessageService {

  /**
   * 保存消息（统一方法）
   *
   * @param request 消息保存请求，包含会话ID、角色、内容等信息
   * @return 消息实体
   */
  Message saveMessage(com.example.dto.request.MessageSaveRequest request);


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

  /**
   * 更新已存在的AI消息内容/思考
   *
   * @param messageId 消息ID
   * @param content 内容
   * @param thinking 思考内容
   */
  void updateMessageContent(Long messageId, String content, String thinking);

  // ========================= 响应式方法 =========================
  
  /**
   * 异步保存用户消息
   *
   * @param conversationId 会话ID
   * @param content 消息内容
   * @return 保存的用户消息
   */
  Mono<Message> saveUserMessageAsync(Long conversationId, String content);

  /**
   * 异步保存AI响应消息
   *
   * @param conversationId 会话ID
   * @param content AI响应内容
   * @param thinking 推理过程内容（可选）
   * @return 保存的AI消息和结束事件
   */
  Mono<ChatEvent> saveAiMessageAsync(Long conversationId, String content, String thinking);


  /**
   * 异步获取会话历史消息
   *
   * @param conversationId 会话ID
   * @return 历史消息列表
   */
  Mono<List<Message>> getConversationHistoryAsync(Long conversationId);

  /**
   * 预创建助手消息用于工具调用关联
   *
   * @param conversationId 会话ID
   * @return 预创建的助手消息ID
   */
  Mono<Long> preCreateAssistantMessage(Long conversationId);
}
