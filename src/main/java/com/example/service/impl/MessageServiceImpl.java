package com.example.service.impl;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import com.example.service.MessageService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息服务实现类
 *
 * @author xupeng
 */
@Service
public class MessageServiceImpl implements MessageService {

  @Autowired private MessageMapper messageMapper;

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
}
