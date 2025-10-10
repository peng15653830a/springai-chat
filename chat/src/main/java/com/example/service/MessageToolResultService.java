package com.example.service;

import com.example.dto.response.SearchResult;
import com.example.entity.MessageToolResult;
import java.util.List;

/**
 * 消息工具调用结果服务接口
 */
public interface MessageToolResultService {
  Long startToolCall(Long messageId, String toolName, String toolInput);
  void completeToolCall(Long toolResultId, String toolResult);
  void failToolCall(Long toolResultId, String errorMessage);
  Long saveSearchResults(Long messageId, String query, List<SearchResult> searchResults);
  List<MessageToolResult> getMessageToolResults(Long messageId);
  List<SearchResult> getMessageSearchResults(Long messageId);
  void deleteMessageToolResults(Long messageId);
  void deleteMessageToolResultsByMessageIds(java.util.List<Long> messageIds);
}

