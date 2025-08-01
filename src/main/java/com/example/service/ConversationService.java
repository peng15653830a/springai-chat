package com.example.service;

import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.mapper.ConversationMapper;
import com.example.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {
    
    @Autowired
    private ConversationMapper conversationMapper;
    
    @Autowired
    private MessageMapper messageMapper;
    
    public Conversation createConversation(Long userId, String title) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("对话标题不能为空");
        }
        
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversationMapper.insert(conversation);
        return conversation;
    }
    
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
    
    public List<Conversation> getUserConversations(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        return conversationMapper.selectByUserId(userId);
    }
    
    public List<Conversation> getRecentConversations(Long userId, int limit) {
        return conversationMapper.selectRecentByUserId(userId, limit);
    }
    
    public void updateConversationTitle(Long conversationId, String title) {
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setTitle(title);
        conversationMapper.updateById(conversation);
    }
    
    @Transactional
    public void deleteConversation(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new IllegalArgumentException("对话ID无效");
        }
        messageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteById(conversationId);
    }
    
    public List<Message> getConversationMessages(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new IllegalArgumentException("对话ID无效");
        }
        return messageMapper.selectByConversationId(conversationId);
    }
    
    public List<Message> getRecentMessages(Long conversationId, int limit) {
        return messageMapper.selectRecentMessages(conversationId, limit);
    }
}