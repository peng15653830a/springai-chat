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
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setSearchResults(searchResults);
        messageMapper.insert(message);
        return message;
    }
    
    public Message getMessageById(Long messageId) {
        return messageMapper.selectById(messageId);
    }
}