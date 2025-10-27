package com.example.novel.memory;

import com.example.novel.entity.NovelMessage;
import com.example.novel.mapper.NovelMessageMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Novel模块的数据库会话记忆实现
 * 基于 novel_messages 表存储会话历史
 * 继承AbstractDatabaseChatMemory，复用通用逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NovelDatabaseChatMemory extends com.example.memory.AbstractDatabaseChatMemory {

  private final NovelMessageMapper novelMessageMapper;

  @Override
  protected void saveMessage(Long conversationId, String role, String content) {
    NovelMessage entity = new NovelMessage();
    entity.setSessionId(conversationId);
    entity.setRole(role);
    entity.setContent(content);
    novelMessageMapper.insert(entity);
  }

  @Override
  protected List<MessageEntity> loadMessages(Long conversationId) {
    List<NovelMessage> messages = novelMessageMapper.findBySessionId(conversationId);
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
    novelMessageMapper.deleteBySessionId(conversationId);
  }
}
