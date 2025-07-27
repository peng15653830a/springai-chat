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
        
        try {
            List<Conversation> conversations = conversationService.getUserConversations(userId);
            log.debug("获取对话列表成功，用户ID: {}, 对话数量: {}", userId, conversations.size());
            return ApiResponse.success(conversations);
        } catch (Exception e) {
            log.error("获取对话列表失败，用户ID: {}", userId, e);
            return ApiResponse.error("获取对话列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Conversation> createConversation(@RequestParam Long userId, 
                                                      @RequestBody ConversationRequest request) {
        log.info("创建新对话，用户ID: {}, 标题: {}", userId, request.getTitle());
        
        try {
            Conversation conversation = conversationService.createConversation(userId, request.getTitle());
            log.info("对话创建成功，对话ID: {}", conversation.getId());
            return ApiResponse.success("创建对话成功", conversation);
        } catch (Exception e) {
            log.error("创建对话失败，用户ID: {}", userId, e);
            return ApiResponse.error("创建对话失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Conversation> getConversation(@PathVariable Long id) {
        try {
            Conversation conversation = conversationService.getConversationById(id);
            if (conversation == null) {
                return ApiResponse.error("对话不存在");
            }
            return ApiResponse.success(conversation);
        } catch (Exception e) {
            log.error("获取对话失败，对话ID: {}", id, e);
            return ApiResponse.error("获取对话失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConversation(@PathVariable Long id) {
        log.info("删除对话请求，对话ID: {}", id);
        
        try {
            conversationService.deleteConversation(id);
            log.info("对话删除成功，对话ID: {}", id);
            return ApiResponse.success("删除对话成功", null);
        } catch (Exception e) {
            log.error("删除对话失败，对话ID: {}", id, e);
            return ApiResponse.error("删除对话失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}/messages")
    public ApiResponse<List<Message>> getMessages(@PathVariable Long id) {
        try {
            List<Message> messages = conversationService.getConversationMessages(id);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            log.error("获取消息失败，对话ID: {}", id, e);
            return ApiResponse.error("获取消息失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/messages")
    public ApiResponse<Message> sendMessage(@PathVariable Long id, 
                                          @RequestBody MessageRequest request) {
        try {
            Message userMessage = messageService.saveMessage(id, "user", request.getContent());
            return ApiResponse.success("消息发送成功", userMessage);
        } catch (Exception e) {
            log.error("发送消息失败，对话ID: {}", id, e);
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }
}