package com.example.memory;

import static com.example.constant.MessageRoles.ASSISTANT;
import static com.example.constant.MessageRoles.SYSTEM;
import static com.example.constant.MessageRoles.USER;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * 数据库ChatMemory的抽象基类
 * 
 * <p>提供通用的ChatMemory实现逻辑，子类只需实现具体的存储操作
 * 
 * <p>职责：
 * <ul>
 *   <li>conversationId解析和验证</li>
 *   <li>Spring AI Message与数据库实体的转换</li>
 *   <li>角色映射（User/Assistant/System）</li>
 * </ul>
 * 
 * <p>子类需实现：
 * <ul>
 *   <li>{@link #saveMessage(Long, String, String)} - 保存单条消息</li>
 *   <li>{@link #loadMessages(Long)} - 加载会话历史</li>
 *   <li>{@link #deleteMessages(Long)} - 删除会话消息</li>
 *   <li>{@link #afterClear(Long)} - 清空后的额外操作（可选）</li>
 * </ul>
 */
@Slf4j
public abstract class AbstractDatabaseChatMemory implements ChatMemory {

  @Override
  public void add(String conversationId, List<Message> messages) {
    Long cid = parseConversationId(conversationId);
    if (cid == null || messages == null || messages.isEmpty()) {
      return;
    }

    for (Message msg : messages) {
      try {
        String role = mapRoleFromMessage(msg);
        if (role != null) {
          String content = msg.getText();
          if (content != null && !content.isBlank()) {
            saveMessage(cid, role, content);
            log.debug("保存消息: conversationId={}, role={}, length={}", cid, role, content.length());
          }
        }
      } catch (Exception e) {
        log.error("保存消息失败: conversationId={}, messageType={}, error={}",
            cid, msg.getMessageType(), e.getMessage(), e);
      }
    }
  }

  @Override
  public List<Message> get(String conversationId) {
    Long cid = parseConversationId(conversationId);
    if (cid == null) {
      return List.of();
    }

    try {
      List<MessageEntity> entities = loadMessages(cid);
      List<Message> messages = new ArrayList<>(entities.size());

      for (MessageEntity entity : entities) {
        Message msg = toSpringAiMessage(entity);
        if (msg != null) {
          messages.add(msg);
        }
      }

      log.debug("加载历史消息: conversationId={}, count={}", cid, messages.size());
      return messages;

    } catch (Exception e) {
      log.error("加载历史消息失败: conversationId={}, error={}", cid, e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public void clear(String conversationId) {
    Long cid = parseConversationId(conversationId);
    if (cid == null) {
      return;
    }

    try {
      afterClear(cid);
      deleteMessages(cid);
      log.info("清空会话历史: conversationId={}", cid);
    } catch (Exception e) {
      log.warn("清空会话历史失败: conversationId={}, error={}", cid, e.getMessage());
    }
  }

  protected abstract void saveMessage(Long conversationId, String role, String content);

  protected abstract List<MessageEntity> loadMessages(Long conversationId);

  protected abstract void deleteMessages(Long conversationId);

  protected void afterClear(Long conversationId) {
  }

  protected Long parseConversationId(String conversationId) {
    if (conversationId == null || conversationId.isBlank()) {
      return null;
    }
    try {
      return Long.valueOf(conversationId);
    } catch (Exception e) {
      log.warn("无效的conversationId: {}", conversationId);
      return null;
    }
  }

  protected String mapRoleFromMessage(Message msg) {
    if (msg == null) {
      return null;
    }

    MessageType type = msg.getMessageType();
    return switch (type) {
      case USER -> USER;
      case ASSISTANT -> ASSISTANT;
      case SYSTEM -> SYSTEM;
      default -> null;
    };
  }

  protected Message toSpringAiMessage(MessageEntity entity) {
    if (entity == null || entity.getRole() == null) {
      return null;
    }

    String content = entity.getContent();
    if (content == null) {
      content = "";
    }

    return switch (entity.getRole().toLowerCase()) {
      case USER -> new UserMessage(content);
      case ASSISTANT -> new AssistantMessage(content);
      case SYSTEM -> new SystemMessage(content);
      default -> {
        log.warn("未知角色: {}", entity.getRole());
        yield null;
      }
    };
  }

  public interface MessageEntity {
    String getRole();

    String getContent();
  }
}
