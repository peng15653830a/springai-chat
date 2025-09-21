package com.example.memory;

import static com.example.constant.AiChatConstants.ROLE_ASSISTANT;
import static com.example.constant.AiChatConstants.ROLE_SYSTEM;
import static com.example.constant.AiChatConstants.ROLE_USER;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.service.MessageToolResultService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * 持久化 ChatMemory 实现：通过 MessageMapper 读写数据库。
 *
 * @author xupeng
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseChatMemory implements ChatMemory {

  private final MessageMapper messageMapper;
  private final MessageToolResultService messageToolResultService;

  @Override
  public List<org.springframework.ai.chat.messages.Message> get(String conversationId) {
    Long cid = parseConversationId(conversationId);
    if (cid == null) {
      return List.of();
    }

    List<Message> history = messageMapper.selectByConversationId(cid);
    List<org.springframework.ai.chat.messages.Message> result = new ArrayList<>();
    for (Message m : history) {
      if (Objects.equals(m.getRole(), ROLE_USER)) {
        result.add(new UserMessage(m.getContent()));
      } else if (Objects.equals(m.getRole(), ROLE_ASSISTANT)) {
        result.add(new AssistantMessage(m.getContent()));
      } else if (Objects.equals(m.getRole(), ROLE_SYSTEM)) {
        result.add(new SystemMessage(m.getContent()));
      }
    }
    return result;
  }

  @Override
  public void add(
      String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
    log.info(
        "🔥 DatabaseChatMemory.add() 被调用: conversationId={}, messages数量={}",
        conversationId,
        messages != null ? messages.size() : 0);

    Long cid = parseConversationId(conversationId);
    if (cid == null || messages == null || messages.isEmpty()) {
      log.warn("⚠️ 参数验证失败: cid={}, messages={}", cid, messages);
      return;
    }

    for (org.springframework.ai.chat.messages.Message msg : messages) {
      try {
        Message entity = new Message();
        entity.setConversationId(cid);

        if (msg.getMessageType() == MessageType.USER) {
          // 跳过用户消息保存，由应用层手动保存以获取真实messageId
          log.debug("跳过用户消息保存（应用层已处理）: {}", ((UserMessage) msg).getText());
          continue;
        } else if (msg.getMessageType() == MessageType.ASSISTANT) {
          // 实验项目：助手消息改由应用层预创建并在结束时更新内容，这里不再持久化，避免重复
          log.info("🛑 跳过助手消息持久化，由应用层负责更新内容");
          continue;
        } else if (msg.getMessageType() == MessageType.SYSTEM) {
          entity.setRole(ROLE_SYSTEM);
          entity.setContent(((SystemMessage) msg).getText());
        } else {
          continue;
        }

        messageMapper.insert(entity);
      } catch (Exception e) {
        log.error("保存聊天消息到数据库失败: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public void clear(String conversationId) {
    Long cid = parseConversationId(conversationId);
    if (cid == null) {
      return;
    }
    try {
      // 先清理该会话下所有消息的工具调用记录，再删除消息
      List<Message> messages = messageMapper.selectByConversationId(cid);
      if (messages != null && !messages.isEmpty()) {
        java.util.List<Long> ids = messages.stream().map(Message::getId).toList();
        try {
          messageToolResultService.deleteMessageToolResultsByMessageIds(ids);
        } catch (Exception ignore) {
        }
      }
      messageMapper.deleteByConversationId(cid);
    } catch (Exception e) {
      log.warn("清理会话历史失败: {}", e.getMessage());
    }
  }

  private Long parseConversationId(String conversationId) {
    try {
      return Long.valueOf(conversationId);
    } catch (Exception e) {
      log.warn("非法的conversationId: {}", conversationId);
      return null;
    }
  }
}
