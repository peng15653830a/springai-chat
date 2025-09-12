package com.example.service.impl;

import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import com.example.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

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
  private static final int SHORT_MESSAGE_LENGTH = 20;
  private static final int FIRST_SENTENCE_MAX_LENGTH = 25;
  private static final int TRUNCATED_LENGTH = 20;
  private static final int MIN_TRUNCATE_POSITION = 10;
  private static final int MAX_TRUNCATE_POSITION = 18;
  private static final String SENTENCE_SPLIT_REGEX = "[。！？
]";
  private static final String DEFAULT_CONVERSATION_TITLE = "新对话";

  private final ConversationMapper conversationMapper;
  private final MessageMapper messageMapper;

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
    messageMapper.deleteByConversationId(conversationId);
    conversationMapper.deleteById(conversationId);
  }

  @Override
  public List<Message> getConversationMessages(Long conversationId) {
    if (conversationId == null || conversationId <= 0) {
      throw new IllegalArgumentException("对话ID无效");
    }
    return messageMapper.selectByConversationId(conversationId);
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
        .onErrorResume(error -> {
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
    if (cleanMessage.length() > TRUNCATED_LENGTH) {
      String truncated = cleanMessage.substring(0, Math.min(TRUNCATED_LENGTH, cleanMessage.length()));
      // 如果截断位置不是标点，尝试找到合适的截断点
      if (cleanMessage.length() > TRUNCATED_LENGTH && !truncated.matches(".*[。！？，、；：]$")) {
        for (int i = Math.min(MAX_TRUNCATE_POSITION, truncated.length() - 1); i >= MIN_TRUNCATE_POSITION; i--) {
          char c = truncated.charAt(i);
          if (c == '，' || c == '、' || c == '；' || c == '：') {
            return truncated.substring(0, i + 1);
          }
        }
      }
      return truncated + "...";
    }
    
    return cleanMessage;
  }

  /**
   * 检查是否需要生成标题
   */
  private boolean needsTitle(Conversation conversation) {
    return conversation.getTitle() == null || 
           conversation.getTitle().trim().isEmpty() || 
           DEFAULT_CONVERSATION_TITLE.equals(conversation.getTitle().trim());
  }

  /**
   * 生成并更新标题
   */
  private Mono<Void> generateAndUpdateTitle(Long conversationId, String userMessage) {
    return Mono.fromCallable(() -> {
          String newTitle = generateTitleFromMessage(userMessage.trim());
          updateConversationTitle(conversationId, newTitle);
          log.debug("自动生成对话标题成功，会话ID: {}, 标题: {}", conversationId, newTitle);
          return null;
        });
  }
}
