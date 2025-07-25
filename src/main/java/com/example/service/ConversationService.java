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
        if (title == null || title.trim().isEmpty()) {
            title = "新对话";
        }
        
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversationMapper.insert(conversation);
        return conversation;
    }
    
    public Conversation getConversationById(Long conversationId) {
        return conversationMapper.selectById(conversationId);
    }
    
    public List<Conversation> getUserConversations(Long userId) {
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
        messageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteById(conversationId);
    }
    
    public List<Message> getConversationMessages(Long conversationId) {
        return messageMapper.selectByConversationId(conversationId);
    }
    
    public List<Message> getRecentMessages(Long conversationId, int limit) {
        return messageMapper.selectRecentMessages(conversationId, limit);
    }
}