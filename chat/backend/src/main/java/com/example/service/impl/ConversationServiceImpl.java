package com.example.service.impl;

import com.example.dto.response.SearchResult;
import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import com.example.service.ConversationService;
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

  // 标题生成常量
  /** 短消息长度阈值 */
  private static final int SHORT_MESSAGE_LENGTH = 20;

  private static final int FIRST_SENTENCE_MAX_LENGTH = 25;
  private static final int TRUNCATED_LENGTH = 20;
  private static final int MIN_TRUNCATE_POSITION = 10;
  private static final int MAX_TRUNCATE_POSITION = 18;
  /** 句子分割正则表达式 */
  private static final String SENTENCE_SPLIT_REGEX = "[。！？\n]";
  /** 默认对话标题 */
  private static final String DEFAULT_CONVERSATION_TITLE = "新对话";
  /** 标点符号正则表达式 */
  private static final String PUNCTUATION_REGEX = ".*[。！？，、；：]$";

  private final ConversationMapper conversationMapper;
  private final MessageMapper messageMapper;
  private final MessageToolResultService messageToolResultService;
  private final ObjectMapper objectMapper;

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

  // ========================= 标题管理方法实现 =========================

  @Override
  public Mono<Void> generateTitleIfNeededAsync(Long conversationId, String userMessage) {
    return Mono.fromCallable(() -> getConversationById(conversationId))
        .filter(this::needsTitle)
        .flatMap(conversation -> generateAndUpdateTitle(conversationId, userMessage))
        .then()
        .doOnSuccess(v -> log.debug("对话标题生成完成，会话ID: {}", conversationId))
        .onErrorResume(
            error -> {
              log.error("生成对话标题时发生异常，会话ID: {}", conversationId, error);
              return Mono.empty();
            });
  }

  @Override
  public String generateTitleFromMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      return DEFAULT_CONVERSATION_TITLE;
    }

    String cleanMessage = message.trim();

    // 如果消息很短（20字以内），直接使用
    if (cleanMessage.length() <= SHORT_MESSAGE_LENGTH) {
      return cleanMessage;
    }

    // 尝试找到第一句话
    String[] sentences = cleanMessage.split(SENTENCE_SPLIT_REGEX);
    if (sentences.length > 0 && !sentences[0].trim().isEmpty()) {
      String firstSentence = sentences[0].trim();
      if (firstSentence.length() <= FIRST_SENTENCE_MAX_LENGTH) {
        return firstSentence;
      }
    }

    // 智能截取
    String truncated = cleanMessage.substring(0, TRUNCATED_LENGTH);
    // 如果截断位置不是标点，尝试找到合适的截断点
    if (!truncated.matches(PUNCTUATION_REGEX)) {
      for (int i = MAX_TRUNCATE_POSITION; i >= MIN_TRUNCATE_POSITION; i--) {
        char c = truncated.charAt(i);
        if (c == '，' || c == '、' || c == '；' || c == '：') {
          return truncated.substring(0, i + 1);
        }
      }
    }
    return truncated + "...";
  }

  /** 检查是否需要生成标题 */
  private boolean needsTitle(Conversation conversation) {
    return conversation.getTitle() == null
        || conversation.getTitle().trim().isEmpty()
        || DEFAULT_CONVERSATION_TITLE.equals(conversation.getTitle().trim());
  }

  /** 生成并更新标题 */
  private Mono<Void> generateAndUpdateTitle(Long conversationId, String userMessage) {
    return Mono.fromCallable(
        () -> {
          String newTitle = generateTitleFromMessage(userMessage.trim());
          updateConversationTitle(conversationId, newTitle);
          log.debug("自动生成对话标题成功，会话ID: {}, 标题: {}", conversationId, newTitle);
          return null;
        });
  }
}
