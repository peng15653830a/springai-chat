package com.example.service.impl;

import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import com.example.service.ConversationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话服务实现类
 *
 * @author xupeng
 */
@Service
public class ConversationServiceImpl implements ConversationService {

  @Autowired private ConversationMapper conversationMapper;

  @Autowired private MessageMapper messageMapper;

  @Override
  public Conversation createConversation(Long userId, String title) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("用户ID无效");
    }

    // 验证标题是否有效
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("对话标题不能为空");
    }

    Conversation conversation = new Conversation();
    conversation.setUserId(userId);
    // 标题不能为空，需要去除首尾空格
    conversation.setTitle(title.trim());
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
    return messageMapper.selectRecentMessages(conversationId, limit);
  }
}
