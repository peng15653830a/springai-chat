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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations(@RequestParam Long userId) {
        log.debug("获取用户对话列表，用户ID: {}", userId);
        
        // 验证用户ID
        if (userId == null || userId <= 0) {
            log.warn("用户ID无效: {}", userId);
            return ResponseEntity.badRequest().body(ApiResponse.error("用户ID无效"));
        }
        
        try {
            List<Conversation> conversations = conversationService.getUserConversations(userId);
            log.debug("获取对话列表成功，用户ID: {}, 对话数量: {}", userId, conversations.size());
            return ResponseEntity.ok(ApiResponse.success(conversations));
        } catch (Exception e) {
            log.error("获取对话列表失败，用户ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取对话列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Conversation>> createConversation(@RequestParam Long userId, 
                                                      @RequestBody ConversationRequest request) {
        log.info("创建新对话，用户ID: {}, 标题: {}", userId, request.getTitle());
        
        // 验证用户ID
        if (userId == null || userId <= 0) {
            log.warn("用户ID无效: {}", userId);
            return ResponseEntity.badRequest().body(ApiResponse.error("用户ID无效"));
        }
        
        // 验证对话标题
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("对话标题为空，用户ID: {}", userId);
            return ResponseEntity.badRequest().body(ApiResponse.error("对话标题不能为空"));
        }
        
        try {
            Conversation conversation = conversationService.createConversation(userId, request.getTitle());
            log.info("对话创建成功，对话ID: {}", conversation.getId());
            return ResponseEntity.ok(ApiResponse.success("创建对话成功", conversation));
        } catch (Exception e) {
            log.error("创建对话失败，用户ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建对话失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Conversation>> getConversation(@PathVariable Long id) {
        try {
            Conversation conversation = conversationService.getConversationById(id);
            if (conversation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("对话不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(conversation));
        } catch (Exception e) {
            log.error("获取对话失败，对话ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取对话失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable Long id) {
        log.info("删除对话请求，对话ID: {}", id);
        
        // 验证对话ID
        if (id == null || id <= 0) {
            log.warn("对话ID无效: {}", id);
            return ResponseEntity.badRequest().body(ApiResponse.error("对话ID无效"));
        }
        
        try {
            conversationService.deleteConversation(id);
            log.info("对话删除成功，对话ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            log.error("删除对话失败，对话ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<Message>>> getMessages(@PathVariable Long id) {
        // 验证对话ID
        if (id == null || id <= 0) {
            log.warn("对话ID无效: {}", id);
            return ResponseEntity.badRequest().body(ApiResponse.error("对话ID无效"));
        }
        
        try {
            List<Message> messages = conversationService.getConversationMessages(id);
            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (Exception e) {
            log.error("获取消息失败，对话ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<Message>> sendMessage(@PathVariable Long id, 
                                          @RequestBody MessageRequest request) {
        try {
            Message userMessage = messageService.saveMessage(id, "user", request.getContent());
            return ResponseEntity.ok(ApiResponse.success("消息发送成功", userMessage));
        } catch (Exception e) {
            log.error("发送消息失败，对话ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("发送消息失败: " + e.getMessage()));
        }
    }
}