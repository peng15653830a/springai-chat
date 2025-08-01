package com.example.service;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    public Message saveMessage(Long conversationId, String role, String content) {
        return saveMessage(conversationId, role, content, null);
    }
    
    public Message saveMessage(Long conversationId, String role, String content, String searchResults) {
        return saveMessage(conversationId, role, content, null, searchResults);
    }
    
    public Message saveMessage(Long conversationId, String role, String content, String thinking, String searchResults) {
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
    
    public Message getMessageById(Long messageId) {
        return messageMapper.selectById(messageId);
    }
    
    public java.util.List<com.example.entity.Message> getMessagesByConversationId(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new IllegalArgumentException("对话ID无效");
        }
        return messageMapper.selectByConversationId(conversationId);
    }
}