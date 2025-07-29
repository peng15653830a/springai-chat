package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    
    public AiResponse chatWithAI(String userMessage, List<Map<String, String>> conversationHistory) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/chat/completions");
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个智能助手，能够进行友好而有帮助的对话。如果需要最新信息，请告诉用户你将进行搜索。");
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
        String[] words = response.split("\\s+");
        StringBuilder chunk = new StringBuilder();
        
        for (String word : words) {
            if (chunk.length() + word.length() + 1 > 20) { // 每20个字符一个chunk
                if (chunk.length() > 0) {
                    chunks.add(chunk.toString());
                    chunk = new StringBuilder();
                }
            }
            if (chunk.length() > 0) {
                chunk.append(" ");
            }
            chunk.append(word);
        }
        
        if (chunk.length() > 0) {
            chunks.add(chunk.toString());
        }
        
        return chunks;
    }
}