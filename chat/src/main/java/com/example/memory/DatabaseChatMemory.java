package com.example.memory;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.service.MessageToolResultService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Chat模块的数据库ChatMemory实现
 * 继承AbstractDatabaseChatMemory，复用通用逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseChatMemory extends com.example.memory.AbstractDatabaseChatMemory {

  private final MessageMapper messageMapper;
  private final MessageToolResultService messageToolResultService;

  @Override
  protected void saveMessage(Long conversationId, String role, String content) {
    if ("user".equals(role) || "assistant".equals(role)) {
      return;
    }

    Message entity = new Message();
    entity.setConversationId(conversationId);
    entity.setRole(role);
    entity.setContent(content);
    messageMapper.insert(entity);
  }

  @Override
  protected List<MessageEntity> loadMessages(Long conversationId) {
    List<Message> messages = messageMapper.selectByConversationId(conversationId);
    return messages.stream()
        .map(
            msg ->
                new MessageEntity() {
                  @Override
                  public String getRole() {
                    return msg.getRole();
                  }

                  @Override
                  public String getContent() {
                    return msg.getContent();
                  }
                })
        .collect(Collectors.toList());
  }

  @Override
  protected void deleteMessages(Long conversationId) {
    messageMapper.deleteByConversationId(conversationId);
  }

  @Override
  protected void afterClear(Long conversationId) {
    try {
      List<Message> messages = messageMapper.selectByConversationId(conversationId);
      if (messages != null && !messages.isEmpty()) {
        List<Long> ids = messages.stream().map(Message::getId).toList();
        messageToolResultService.deleteMessageToolResultsByMessageIds(ids);
      }
    } catch (Exception e) {
      log.warn("清理tool results失败: {}", e.getMessage());
    }
  }
}

