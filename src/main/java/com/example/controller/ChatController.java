package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.MessageRequest;
import com.example.entity.Message;
import com.example.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return sseEmitterManager.createEmitter(conversationId);
    }
    
    @PostMapping("/conversations/{id}/messages")
    public ApiResponse<Message> sendMessage(@PathVariable Long id, 
                                          @RequestBody MessageRequest request) {
        try {
            // 保存用户消息
            Message userMessage = messageService.saveMessage(id, "user", request.getContent());
            
            // 异步处理AI回复，传递搜索开关参数
            processAiResponse(id, request.getContent(), request.getSearchEnabled());
            
            return ApiResponse.success("消息发送成功", userMessage);
        } catch (Exception e) {
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }
    
    @Async
    public void processAiResponse(Long conversationId, String userMessage, Boolean searchEnabled) {
        try {
            // 检查是否需要搜索 (同时检查用户设置和系统判断)
            List<Map<String, String>> searchResults = null;
            String enhancedMessage = userMessage;
            
            if (searchEnabled != null && searchEnabled && searchService.shouldSearch(userMessage)) {
                // 发送搜索开始事件
                sendSseEvent(conversationId, "search", createEventData("start", "正在搜索相关信息..."));
                
                searchResults = searchService.searchGoogle(userMessage);
                String searchResultsText = searchService.formatSearchResults(searchResults);
                enhancedMessage = userMessage + "\n\n参考信息：\n" + searchResultsText;
                
                // 发送搜索完成事件
                sendSseEvent(conversationId, "search", createEventData("complete", "搜索完成"));
            } else if (searchEnabled != null && !searchEnabled) {
                // 用户关闭了搜索，发送提示
                sendSseEvent(conversationId, "search", createEventData("disabled", "联网搜索已关闭"));
            }
            
            // 获取对话历史
            List<Message> recentMessages = conversationService.getRecentMessages(conversationId, 10);
            
            // 发送AI响应开始事件
            sendSseEvent(conversationId, "message", createEventData("start", ""));
            
            // 调用AI获取回复
            String aiResponse = aiChatService.chatWithAI(enhancedMessage, convertMessagesToHistory(recentMessages));
            
            // 流式发送AI回复
            List<String> chunks = aiChatService.splitResponseForStreaming(aiResponse);
            for (String chunk : chunks) {
                sendSseEvent(conversationId, "message", createEventData("chunk", chunk));
                Thread.sleep(100); // 模拟打字效果
            }
            
            // 保存AI回复到数据库
            String searchResultsJson = null;
            if (searchResults != null) {
                searchResultsJson = objectMapper.writeValueAsString(searchResults);
            }
            Message aiMessage = messageService.saveMessage(conversationId, "assistant", aiResponse, searchResultsJson);
            
            // 发送完成事件
            Map<String, Object> endData = createEventData("end", "");
            endData.put("messageId", aiMessage.getId());
            sendSseEvent(conversationId, "message", endData);
            
        } catch (Exception e) {
            // 发送错误事件
            sendSseEvent(conversationId, "error", createEventData("error", "AI服务异常: " + e.getMessage()));
        }
    }
    
    private void sendSseEvent(Long conversationId, String eventName, Map<String, Object> data) {
        try {
            sseEmitterManager.sendMessage(conversationId, eventName, objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            e.printStackTrace();
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