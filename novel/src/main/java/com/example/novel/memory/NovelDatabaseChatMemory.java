package com.example.novel.memory;

import com.example.novel.entity.NovelMessage;
import com.example.novel.mapper.NovelMessageMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * Novel模块的数据库会话记忆实现
 * 基于 novel_messages 表存储会话历史
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NovelDatabaseChatMemory implements ChatMemory {

  private final NovelMessageMapper novelMessageMapper;

  @Override
  public void add(String conversationId, List<Message> messages) {
    if (conversationId == null || messages == null || messages.isEmpty()) {
      return;
    }

    try {
      Long sessionId = Long.parseLong(conversationId);

      for (Message message : messages) {
        NovelMessage entity = new NovelMessage();
        entity.setSessionId(sessionId);

        if (message instanceof UserMessage) {
          entity.setRole("user");
          entity.setContent(message.getText());
        } else if (message instanceof AssistantMessage) {
          entity.setRole("assistant");
          entity.setContent(message.getText());
        } else {
          entity.setRole("system");
          entity.setContent(message.getText());
        }

        novelMessageMapper.insert(entity);
        log.debug("Saved message to conversation {}: role={}", conversationId, entity.getRole());
      }
    } catch (Exception e) {
      log.error("Failed to save messages to conversation {}", conversationId, e);
    }
  }

  @Override
  public List<Message> get(String conversationId) {
    // 默认获取最后10条
    return get(conversationId, 10);
  }

  public List<Message> get(String conversationId, int lastN) {
    if (conversationId == null) {
      return List.of();
    }

    try {
      Long sessionId = Long.parseLong(conversationId);
      List<NovelMessage> messages = novelMessageMapper.findBySessionId(sessionId);

      // 只取最后N条
      int startIndex = Math.max(0, messages.size() - lastN);

      return messages.stream()
          .skip(startIndex)
          .map(this::toMessage)
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Failed to retrieve messages from conversation {}", conversationId, e);
      return List.of();
    }
  }

  @Override
  public void clear(String conversationId) {
    if (conversationId == null) {
      return;
    }

    try {
      Long sessionId = Long.parseLong(conversationId);
      novelMessageMapper.deleteBySessionId(sessionId);
      log.info("Cleared conversation {}", conversationId);
    } catch (Exception e) {
      log.error("Failed to clear conversation {}", conversationId, e);
    }
  }

  private Message toMessage(NovelMessage entity) {
    String role = entity.getRole();
    String content = entity.getContent();

    return switch (role.toLowerCase()) {
      case "user" -> new UserMessage(content);
      case "assistant" -> new AssistantMessage(content);
      default -> new org.springframework.ai.chat.messages.SystemMessage(content);
    };
  }
}
