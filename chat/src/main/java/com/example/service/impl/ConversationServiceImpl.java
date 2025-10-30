package com.example.service.impl;

import com.example.dto.response.SearchResult;
import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import com.example.service.ConversationService;
import com.example.service.ConversationTitleService;
import com.example.service.MessageToolResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * 会话服务实现类（整合了ConversationManagementService的标题生成功能）
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

  private static final String DEFAULT_CONVERSATION_TITLE = "新对话";

  private final ConversationMapper conversationMapper;
  private final MessageMapper messageMapper;
  private final MessageToolResultService messageToolResultService;
  private final ObjectMapper objectMapper;
  private final ConversationTitleService conversationTitleService;

  @Override
  public Conversation createConversation(Long userId, String title) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("用户ID无效");
    }

    Conversation conversation = new Conversation();
    conversation.setUserId(userId);

    // 处理标题：如果为null或空则使用默认标题
    if (title == null || title.trim().isEmpty()) {
      conversation.setTitle(DEFAULT_CONVERSATION_TITLE);
    } else {
      conversation.setTitle(title.trim());
    }

    conversationMapper.insert(conversation);
    return conversation;
  }

  @Override
  public Conversation getConversationById(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    Conversation conversation = conversationMapper.selectById(conversationId);
    if (conversation == null) {
      throw new IllegalArgumentException("对话不存在");
    }
    return conversation;
  }

  @Override
  public List<Conversation> getUserConversations(Long userId) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("用户ID无效");
    }
    return conversationMapper.selectByUserId(userId);
  }

  @Override
  public List<Conversation> getRecentConversations(Long userId, int limit) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("用户ID无效");
    }
    return conversationMapper.selectRecentByUserId(userId, limit);
  }

  @Override
  public void updateConversationTitle(Long conversationId, String title) {
    Conversation conversation = new Conversation();
    conversation.setId(conversationId);
    conversation.setTitle(title);
    conversationMapper.updateById(conversation);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteConversation(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    // 先清理该会话下所有消息的工具调用结果，再删除消息
    try {
      List<Message> messages = messageMapper.selectByConversationId(conversationId);
      if (messages != null && !messages.isEmpty()) {
        java.util.List<Long> ids = messages.stream().map(Message::getId).toList();
        messageToolResultService.deleteMessageToolResultsByMessageIds(ids);
      }
    } catch (Exception e) {
      log.warn("删除会话前清理工具调用结果失败，会话ID: {}，错误: {}", conversationId, e.getMessage());
    }
    messageMapper.deleteByConversationId(conversationId);
    conversationMapper.deleteById(conversationId);
  }

  @Override
  public List<Message> getConversationMessages(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    List<Message> messages = messageMapper.selectByConversationId(conversationId);

    // 为每条AI消息填充其搜索结果，便于前端历史展示（不做任何兼容回溯）
    if (messages != null && !messages.isEmpty()) {
      for (Message msg : messages) {
        try {
          if (msg != null && "assistant".equalsIgnoreCase(msg.getRole())) {
            List<SearchResult> results =
                messageToolResultService.getMessageSearchResults(msg.getId());
            if (results != null && !results.isEmpty()) {
              // 历史接口返回完整结果集合（包括摘要等非链接项）；前端在展示层再做过滤
              msg.setSearchResults(results);
            }
          }
        } catch (Exception e) {
          log.warn(
              "填充搜索结果时发生异常，messageId: {}，错误: {}", msg != null ? msg.getId() : null, e.getMessage());
        }
      }
    }

    return messages;
  }

  @Override
  public List<Message> getRecentMessages(Long conversationId, int limit) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    return messageMapper.selectRecentMessages(conversationId, limit);
  }

  @Override
  public Mono<Void> generateTitleIfNeededAsync(Long conversationId, String userMessage) {
    return conversationTitleService.updateTitleIfNeeded(conversationId, userMessage);
  }

  @Override
  public String generateTitleFromMessage(String message) {
    return conversationTitleService.generateTitle(message);
  }
}
