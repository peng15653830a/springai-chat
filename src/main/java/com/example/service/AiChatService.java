package com.example.service;

import com.example.entity.Message;
import com.example.service.dto.ChatMessage;
import java.util.List;

/**
 * AI聊天服务接口
 *
 * @author xupeng
 */
public interface AiChatService {

  /**
   * 与AI聊天
   *
   * @param conversationId 会话ID
   * @param userMessage 用户消息
   * @param searchContext 搜索上下文
   * @return AI响应
   */
  AiResponse chat(Long conversationId, String userMessage, String searchContext);

  /**
   * 与AI聊天
   *
   * @param userMessage 用户消息
   * @param conversationHistory 对话历史
   * @return AI响应
   */
  AiResponse chatWithAi(String userMessage, List<ChatMessage> conversationHistory);

  /**
   * 与AI聊天带搜索上下文
   *
   * @param userMessage 用户消息
   * @param conversationHistory 对话历史
   * @param searchContext 搜索上下文
   * @return AI响应
   */
  AiResponse chatWithAi(
      String userMessage, List<ChatMessage> conversationHistory, String searchContext);

  /**
   * 将响应拆分为流式输出
   *
   * @param response 响应内容
   * @return 拆分后的响应列表
   */
  List<String> splitResponseForStreaming(String response);

  /**
   * 获取对话历史
   *
   * @param conversationId 会话ID
   * @return 消息列表
   */
  List<Message> getConversationHistory(Long conversationId);

  /**
   * 发送消息并处理AI回复
   *
   * @param conversationId 会话ID
   * @param content 消息内容
   * @param searchEnabled 搜索开关
   * @return 消息实体
   */
  Message sendMessage(Long conversationId, String content, boolean searchEnabled);
}
