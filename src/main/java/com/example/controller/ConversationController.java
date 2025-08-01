package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.ConversationRequest;
import com.example.dto.MessageRequest;
import com.example.entity.Conversation;
import com.example.entity.Message;
import com.example.service.ConversationService;
import com.example.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private MessageService messageService;
    
    @GetMapping
    public ApiResponse<List<Conversation>> getConversations(@RequestParam Long userId) {
        log.debug("获取用户对话列表，用户ID: {}", userId);
        
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        List<Conversation> conversations = conversationService.getUserConversations(userId);
        log.debug("获取对话列表成功，用户ID: {}, 对话数量: {}", userId, conversations.size());
        return ApiResponse.success(conversations);
    }
    
    @PostMapping
    public ApiResponse<Conversation> createConversation(@RequestParam Long userId, 
                                                      @RequestBody ConversationRequest request) {
        log.info("创建新对话，用户ID: {}, 标题: {}", userId, request.getTitle());
        
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("对话标题不能为空");
        }
        
        Conversation conversation = conversationService.createConversation(userId, request.getTitle());
        log.info("对话创建成功，对话ID: {}", conversation.getId());
        return ApiResponse.success("创建对话成功", conversation);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Conversation> getConversation(@PathVariable Long id) {
        Conversation conversation = conversationService.getConversationById(id);
        return ApiResponse.success(conversation);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConversation(@PathVariable Long id) {
        log.info("删除对话请求，对话ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("对话ID无效");
        }
        
        conversationService.deleteConversation(id);
        log.info("对话删除成功，对话ID: {}", id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}/messages")
    public ApiResponse<List<Message>> getMessages(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("对话ID无效");
        }
        
        List<Message> messages = conversationService.getConversationMessages(id);
        return ApiResponse.success(messages);
    }
    
    @PostMapping("/{id}/messages")
    public ApiResponse<Message> sendMessage(@PathVariable Long id, 
                                          @RequestBody MessageRequest request) {
        Message userMessage = messageService.saveMessage(id, "user", request.getContent());
        return ApiResponse.success("消息发送成功", userMessage);
    }
}