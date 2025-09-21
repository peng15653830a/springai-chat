package com.example.service;

import com.example.dto.response.SearchResult;
import com.example.entity.MessageToolResult;
import java.util.List;

/**
 * 消息工具调用结果服务接口
 *
 * @author xupeng
 */
public interface MessageToolResultService {

  /**
   * 开始工具调用记录
   *
   * @param messageId 消息ID
   * @param toolName 工具名称
   * @param toolInput 工具输入参数
   * @return 工具调用记录ID
   */
  Long startToolCall(Long messageId, String toolName, String toolInput);

  /**
   * 完成工具调用记录
   *
   * @param toolResultId 工具调用记录ID
   * @param toolResult 工具调用结果
   */
  void completeToolCall(Long toolResultId, String toolResult);

  /**
   * 记录工具调用失败
   *
   * @param toolResultId 工具调用记录ID
   * @param errorMessage 错误信息
   */
  void failToolCall(Long toolResultId, String errorMessage);

  /**
   * 保存搜索结果到消息工具调用记录
   *
   * @param messageId 消息ID
   * @param query 搜索查询
   * @param searchResults 搜索结果
   * @return 工具调用记录ID
   */
  Long saveSearchResults(Long messageId, String query, List<SearchResult> searchResults);

  /**
   * 获取消息的所有工具调用结果
   *
   * @param messageId 消息ID
   * @return 工具调用结果列表
   */
  List<MessageToolResult> getMessageToolResults(Long messageId);

  /**
   * 获取消息的搜索结果
   *
   * @param messageId 消息ID
   * @return 搜索结果列表
   */
  List<SearchResult> getMessageSearchResults(Long messageId);

  /**
   * 删除消息的所有工具调用结果
   *
   * @param messageId 消息ID
   */
  void deleteMessageToolResults(Long messageId);

  /**
   * 批量删除多条消息的所有工具调用结果
   *
   * @param messageIds 消息ID列表
   */
  void deleteMessageToolResultsByMessageIds(java.util.List<Long> messageIds);
}
