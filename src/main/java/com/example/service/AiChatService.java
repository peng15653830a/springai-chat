package com.example.service;

import com.example.entity.Message;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiChatService {
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.model}")
    private String model;
    
    @Value("${spring.ai.openai.temperature:0.7}")
    private double temperature;
    
    @Value("${spring.ai.openai.max-tokens:1000}")
    private int maxTokens;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private SseEmitterManager sseEmitterManager;
    
    @Autowired
    private CloseableHttpClient httpClient;
    
    /**
     * 与AI聊天
     */
    public AiResponse chat(Long conversationId, String userMessage, String searchContext) {
        log.info("开始AI聊天，会话ID: {}, 消息长度: {}, 搜索上下文长度: {}", 
                conversationId, userMessage.length(), searchContext != null ? searchContext.length() : 0);
        
        // 获取对话历史
        List<Message> history = getConversationHistory(conversationId);
        List<Map<String, String>> conversationHistory = history.stream()
                .map(msg -> {
                    Map<String, String> msgMap = new HashMap<>();
                    msgMap.put("role", msg.getRole());
                    msgMap.put("content", msg.getContent());
                    return msgMap;
                })
                .collect(Collectors.toList());
        
        return chatWithAI(userMessage, conversationHistory, searchContext);
    }
    
    public AiResponse chatWithAI(String userMessage, List<Map<String, String>> conversationHistory) {
        return chatWithAI(userMessage, conversationHistory, null);
    }
    
    public AiResponse chatWithAI(String userMessage, List<Map<String, String>> conversationHistory, String searchContext) {
        try {
            HttpPost httpPost = new HttpPost(baseUrl + "/chat/completions");
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            String systemContent = "你是一个智能助手，能够进行友好而有帮助的对话。";
            if (searchContext != null && !searchContext.trim().isEmpty()) {
                systemContent += "\n\n以下是相关的搜索信息，请结合这些信息回答用户的问题：\n" + searchContext;
            }
            systemMessage.put("content", systemContent);
            messages.add(systemMessage);
            
            // 添加对话历史
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                messages.addAll(conversationHistory);
            }
            
            // 添加用户当前消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));
            
            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 解析响应
                    Map<String, Object> responseMap = objectMapper.readValue(responseString, Map.class);
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        String content = (String) message.get("content");
                        String thinking = (String) message.get("thinking"); // 一些AI模型支持这个字段
                        return new AiResponse(content, thinking);
                    }
                }
                
                return new AiResponse("抱歉，AI服务暂时不可用，请稍后再试。", null);
            }
        } catch (IOException e) {
            return new AiResponse("网络连接错误，请检查网络设置后重试。", null);
        } catch (Exception e) {
            return new AiResponse("AI服务出现异常: " + e.getMessage(), null);
        }
    }
    
    public List<String> splitResponseForStreaming(String response) {
        List<String> chunks = new ArrayList<>();
        
        // 处理null或空字符串
        if (response == null || response.isEmpty()) {
            return chunks;
        }
        
        // 保持原始格式，按字符数分割而不是按单词分割
        int chunkSize = 50; // 增加chunk大小以获得更好的用户体验
        
        for (int i = 0; i < response.length(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, response.length());
            chunks.add(response.substring(i, endIndex));
        }
        
        return chunks;
    }
    
    /**
     * 获取对话历史
     */
    public List<Message> getConversationHistory(Long conversationId) {
        return messageService.getMessagesByConversationId(conversationId);
    }
    
    /**
     * 发送消息并处理AI回复
     */
    public Message sendMessage(Long conversationId, String content, boolean searchEnabled) {
        // 验证会话ID
        if (conversationId == null || conversationId <= 0) {
            throw new IllegalArgumentException("会话ID无效");
        }
        
        // 验证消息内容
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        // 保存用户消息
        Message userMessage = messageService.saveMessage(conversationId, "user", content);
        
        // 异步处理AI回复
        processAiResponseAsync(conversationId, content, searchEnabled);
        
        return userMessage;
    }
    
    /**
     * 异步处理AI回复
     */
    private void processAiResponseAsync(Long conversationId, String userMessage, boolean searchEnabled) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始处理AI回复，会话ID: {}, 搜索开启: {}", conversationId, searchEnabled);
                
                // 发送开始事件
                sseEmitterManager.sendMessage(conversationId, "start", "开始生成回复");
                
                String searchContext = "";
                if (searchEnabled) {
                    log.info("开始搜索相关信息，会话ID: {}", conversationId);
                    Map<String, String> searchStartEvent = new HashMap<>();
                    searchStartEvent.put("type", "start");
                    sseEmitterManager.sendMessage(conversationId, "search", searchStartEvent);
                    List<Map<String, String>> searchResults = searchService.searchMetaso(userMessage);
                    searchContext = searchService.formatSearchResults(searchResults);
                    Map<String, String> searchCompleteEvent = new HashMap<>();
                    searchCompleteEvent.put("type", "complete");
                    sseEmitterManager.sendMessage(conversationId, "search", searchCompleteEvent);
                    log.debug("搜索完成，上下文长度: {}, 会话ID: {}", searchContext.length(), conversationId);
                }
                
                // 流式调用AI服务
                log.info("开始流式调用AI服务，会话ID: {}", conversationId);
                String fullResponse = streamChatWithAI(conversationId, userMessage, searchContext);
                log.info("AI服务流式调用完成，回复长度: {}, 会话ID: {}", fullResponse.length(), conversationId);
                
                // 保存AI回复
                Message aiMessage = messageService.saveMessage(conversationId, "assistant", fullResponse);
                log.debug("AI回复保存成功，消息ID: {}", aiMessage.getId());
                
                // 发送结束事件
                Map<String, Object> endEvent = new HashMap<>();
                endEvent.put("messageId", aiMessage.getId());
                sseEmitterManager.sendMessage(conversationId, "end", endEvent);
                log.debug("AI回复流式发送完成，会话ID: {}", conversationId);
                
            } catch (Exception e) {
                log.error("处理AI回复时发生异常，会话ID: {}", conversationId, e);
                try {
                    sseEmitterManager.sendMessage(conversationId, "error", "处理消息时发生错误: " + e.getMessage());
                } catch (Exception sseException) {
                    log.error("发送SSE错误消息失败，会话ID: {}", conversationId, sseException);
                }
            }
        });
    }
    
    /**
     * 流式调用AI服务
     */
    private String streamChatWithAI(Long conversationId, String userMessage, String searchContext) {
        StringBuilder fullResponse = new StringBuilder();
        
        try {
            HttpPost httpPost = new HttpPost(baseUrl + "/chat/completions");
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "text/event-stream");
            
            // 获取对话历史
            List<Message> history = getConversationHistory(conversationId);
            List<Map<String, String>> conversationHistory = history.stream()
                    .map(msg -> {
                        Map<String, String> msgMap = new HashMap<>();
                        msgMap.put("role", msg.getRole());
                        msgMap.put("content", msg.getContent());
                        return msgMap;
                    })
                    .collect(Collectors.toList());
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            String systemContent = "你是一个智能助手，能够进行友好而有帮助的对话。";
            if (searchContext != null && !searchContext.trim().isEmpty()) {
                systemContent += "\n\n以下是相关的搜索信息，请结合这些信息回答用户的问题：\n" + searchContext;
            }
            systemMessage.put("content", systemContent);
            messages.add(systemMessage);
            
            // 添加对话历史
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                messages.addAll(conversationHistory);
            }
            
            // 添加用户当前消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            // 构建请求体，启用流式响应
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("stream", true); // 启用流式响应
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));
            
            // 发送请求并处理流式响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                        
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                
                                // 检查是否是结束标记
                                if ("[DONE]".equals(data)) {
                                    break;
                                }
                                
                                try {
                                    // 解析SSE数据
                                    Map<String, Object> sseData = objectMapper.readValue(data, Map.class);
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) sseData.get("choices");
                                    
                                    if (choices != null && !choices.isEmpty()) {
                                        Map<String, Object> choice = choices.get(0);
                                        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                        
                                        if (delta != null && delta.containsKey("content")) {
                                            String content = (String) delta.get("content");
                                            if (content != null) {
                                                fullResponse.append(content);
                                                // 实时发送chunk到前端
                                                sseEmitterManager.sendMessage(conversationId, "chunk", content);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("解析SSE数据失败: {}", data, e);
                                }
                            }
                        }
                    }
                } else {
                    log.error("AI API调用失败，状态码: {}", response.getStatusLine().getStatusCode());
                    throw new RuntimeException("AI服务调用失败");
                }
            }
        } catch (IOException e) {
            log.error("AI API调用异常", e);
            throw new RuntimeException("网络连接错误，请检查网络设置后重试", e);
        } catch (Exception e) {
            log.error("AI服务异常", e);
            throw new RuntimeException("AI服务出现异常: " + e.getMessage(), e);
        }
        
        return fullResponse.toString();
    }
}