package com.example.service.impl;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.service.MessageService;
import com.example.dto.response.SseEventResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import java.util.List;
import org.springframework.stereotype.Service;

import static com.example.service.constants.AiChatConstants.ROLE_ASSISTANT;
import static com.example.service.constants.AiChatConstants.ROLE_USER;

/**
 * 消息服务实现类（整合了MessagePersistenceService的响应式功能）
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

  private final MessageMapper messageMapper;

  @Override
  public Message saveMessage(Long conversationId, String role, String content) {
    return saveMessage(conversationId, role, content, null);
  }

  @Override
  public Message saveMessage(
      Long conversationId, String role, String content, String searchResults) {
    return saveMessage(conversationId, role, content, null, searchResults);
  }

  @Override
  public Message saveMessage(
      Long conversationId, String role, String content, String thinking, String searchResults) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    if (role == null || role.trim().isEmpty()) {
      throw new IllegalArgumentException("消息角色不能为空");
    }
    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("消息内容不能为空");
    }

    Message message = new Message();
    message.setConversationId(conversationId);
    message.setRole(role);
    message.setContent(content);
    message.setThinking(thinking);
    message.setSearchResults(searchResults);
    messageMapper.insert(message);
    return message;
  }

  @Override
  public Message getMessageById(Long messageId) {
    return messageMapper.selectById(messageId);
  }

  @Override
  public List<Message> getMessagesByConversationId(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    return messageMapper.selectByConversationId(conversationId);
  }

  @Override
  public void deleteMessage(Long messageId) {
    if (messageId == null || messageId <= 0) {
      throw new IllegalArgumentException("消息ID无效");
    }
    messageMapper.deleteById(messageId);
  }

  // ========================= 响应式方法实现 =========================
  
  @Override
  public Mono<Message> saveUserMessageAsync(Long conversationId, String content) {
    return Mono.fromCallable(() -> {
          Message userMessage = saveMessage(conversationId, ROLE_USER, content);
          log.info("用户消息保存成功，消息ID: {}", userMessage.getId());
          return userMessage;
        })
        .onErrorMap(error -> {
          log.error("保存用户消息失败，会话ID: {}", conversationId, error);
          return new RuntimeException("保存用户消息失败: " + error.getMessage(), error);
        });
  }

  @Override
  public Mono<SseEventResponse> saveAiMessageAsync(Long conversationId, String content, String thinking) {
    return Mono.fromCallable(() -> {
          // 保存AI消息，包含thinking内容
          Message aiMessage = saveMessage(conversationId, ROLE_ASSISTANT, content, thinking, null);
          
          log.info("AI消息保存成功，消息ID: {}, thinking内容: {}", 
              aiMessage.getId(), thinking != null ? "有" : "无");
          return SseEventResponse.end(aiMessage.getId());
        })
        .onErrorMap(error -> {
          log.error("保存AI消息失败，会话ID: {}", conversationId, error);
          return new RuntimeException("保存AI消息失败: " + error.getMessage(), error);
        });
  }

  @Override
  public Mono<SseEventResponse> saveAiMessageWithSearchAsync(Long conversationId, String content, 
                                                            String thinking, List<?> searchResults) {
    return Mono.fromCallable(() -> {
          // 将搜索结果序列化为JSON（如果存在）
          String searchResultsJson = null;
          if (searchResults != null && !searchResults.isEmpty()) {
            try {
              searchResultsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                  .writeValueAsString(searchResults);
            } catch (Exception e) {
              log.warn("序列化搜索结果失败", e);
            }
          }
          
          // 保存AI消息，包含thinking和搜索结果
          Message aiMessage = saveMessage(conversationId, ROLE_ASSISTANT, content, thinking, searchResultsJson);
          
          log.info("AI消息保存成功，消息ID: {}, thinking: {}, 搜索结果: {}", 
              aiMessage.getId(), thinking != null ? "有" : "无", searchResults != null ? "有" : "无");
          return SseEventResponse.end(aiMessage.getId());
        })
        .onErrorMap(error -> {
          log.error("保存AI消息失败，会话ID: {}", conversationId, error);
          return new RuntimeException("保存AI消息失败: " + error.getMessage(), error);
        });
  }

  @Override
  public Mono<List<Message>> getConversationHistoryAsync(Long conversationId) {
    return Mono.fromCallable(() -> getMessagesByConversationId(conversationId))
        .doOnNext(messages -> log.debug("加载会话历史，会话ID: {}, 消息数量: {}", 
            conversationId, messages.size()));
  }
}
