package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.MessageRequest;
import com.example.entity.Message;
import com.example.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private SseEmitterManager sseEmitterManager;
    
    @Autowired
    private AiChatService aiChatService;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ConversationService conversationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@PathVariable Long conversationId) {
        log.info("创建SSE连接，会话ID: {}", conversationId);
        try {
            SseEmitter emitter = sseEmitterManager.createEmitter(conversationId);
            log.debug("SSE连接创建成功，会话ID: {}", conversationId);
            return emitter;
        } catch (Exception e) {
            log.error("创建SSE连接失败，会话ID: {}", conversationId, e);
            throw e;
        }
    }
    
    @PostMapping("/conversations/{id}/messages")
    public ApiResponse<Message> sendMessage(@PathVariable Long id, 
                                          @RequestBody MessageRequest request) {
        log.info("接收到消息发送请求，会话ID: {}, 消息长度: {}, 搜索开启: {}", 
                   id, request.getContent() != null ? request.getContent().length() : 0, request.getSearchEnabled());
        
        try {
            // 保存用户消息
            Message userMessage = messageService.saveMessage(id, "user", request.getContent());
            log.debug("用户消息保存成功，消息ID: {}", userMessage.getId());
            
            // 异步处理AI回复，传递搜索开关参数
            processAiResponse(id, request.getContent(), request.getSearchEnabled());
            
            log.info("消息发送处理完成，会话ID: {}", id);
            return ApiResponse.success("消息发送成功", userMessage);
        } catch (Exception e) {
            log.error("发送消息失败，会话ID: {}", id, e);
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }
    
    @Async
    public void processAiResponse(Long conversationId, String userMessage, Boolean searchEnabled) {
        log.info("开始处理AI回复，会话ID: {}, 搜索开启: {}", conversationId, searchEnabled);
        
        try {
            // 检查是否需要搜索 (同时检查用户设置和系统判断)
            List<Map<String, String>> searchResults = null;
            String enhancedMessage = userMessage;
            
            if (searchEnabled != null && searchEnabled && searchService.shouldSearch(userMessage)) {
                log.info("开始搜索相关信息，会话ID: {}", conversationId);
                // 发送搜索开始事件
                sendSseEvent(conversationId, "search", createEventData("start", "正在搜索相关信息..."));
                
                searchResults = searchService.searchGoogle(userMessage);
                log.debug("搜索完成，结果数量: {}, 会话ID: {}", 
                           searchResults != null ? searchResults.size() : 0, conversationId);
                
                String searchResultsText = searchService.formatSearchResults(searchResults);
                enhancedMessage = userMessage + "\n\n参考信息：\n" + searchResultsText;
                
                // 发送搜索完成事件
                sendSseEvent(conversationId, "search", createEventData("complete", "搜索完成"));
            } else if (searchEnabled != null && !searchEnabled) {
                log.debug("用户关闭了搜索，会话ID: {}", conversationId);
                // 用户关闭了搜索，发送提示
                sendSseEvent(conversationId, "search", createEventData("disabled", "联网搜索已关闭"));
            } else {
                log.debug("不需要搜索，会话ID: {}", conversationId);
            }
            
            // 获取对话历史
            List<Message> recentMessages = conversationService.getRecentMessages(conversationId, 10);
            log.debug("获取到历史消息数量: {}, 会话ID: {}", recentMessages.size(), conversationId);
            
            // 发送AI响应开始事件
            sendSseEvent(conversationId, "message", createEventData("start", ""));
            log.debug("已发送AI响应开始事件，会话ID: {}", conversationId);
            
            // 调用AI获取回复
            log.info("开始调用AI服务，会话ID: {}", conversationId);
            String aiResponse = aiChatService.chatWithAI(enhancedMessage, convertMessagesToHistory(recentMessages));
            log.info("AI服务调用完成，回复长度: {}, 会话ID: {}", 
                       aiResponse != null ? aiResponse.length() : 0, conversationId);
            
            // 流式发送AI回复
            List<String> chunks = aiChatService.splitResponseForStreaming(aiResponse);
            log.debug("AI回复分割为 {} 个块，会话ID: {}", chunks.size(), conversationId);
            
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                sendSseEvent(conversationId, "message", createEventData("chunk", chunk));
                if (i < chunks.size() - 1) { // 不在最后一个块后sleep
                    Thread.sleep(100); // 模拟打字效果
                }
            }
            
            // 保存AI回复到数据库
            String searchResultsJson = null;
            if (searchResults != null) {
                searchResultsJson = objectMapper.writeValueAsString(searchResults);
            }
            Message aiMessage = messageService.saveMessage(conversationId, "assistant", aiResponse, searchResultsJson);
            log.debug("AI回复保存成功，消息ID: {}, 会话ID: {}", aiMessage.getId(), conversationId);
            
            // 发送完成事件
            Map<String, Object> endData = createEventData("end", "");
            endData.put("messageId", aiMessage.getId());
            sendSseEvent(conversationId, "message", endData);
            
            log.info("AI回复处理完成，会话ID: {}", conversationId);
            
        } catch (Exception e) {
            log.error("处理AI回复时发生异常，会话ID: {}", conversationId, e);
            // 发送错误事件
            sendSseEvent(conversationId, "error", createEventData("error", "AI服务异常: " + e.getMessage()));
        }
    }
    
    private void sendSseEvent(Long conversationId, String eventName, Map<String, Object> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            sseEmitterManager.sendMessage(conversationId, eventName, jsonData);
            log.trace("SSE事件发送成功，会话ID: {}, 事件: {}", conversationId, eventName);
        } catch (Exception e) {
            log.error("发送SSE事件失败，会话ID: {}, 事件: {}", conversationId, eventName, e);
        }
    }
    
    private Map<String, Object> createEventData(String type, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("content", content);
        data.put("timestamp", System.currentTimeMillis());
        return data;
    }
    
    private List<Map<String, String>> convertMessagesToHistory(List<Message> messages) {
        List<Map<String, String>> history = new java.util.ArrayList<>();
        for (Message msg : messages) {
            Map<String, String> historyMsg = new HashMap<>();
            historyMsg.put("role", msg.getRole());
            historyMsg.put("content", msg.getContent());
            history.add(historyMsg);
        }
        return history;
    }
}