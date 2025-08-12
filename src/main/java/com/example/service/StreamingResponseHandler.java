package com.example.service;

import com.example.service.dto.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * 流式响应处理器，用于处理AI服务的流式响应
 *
 * @author xupeng
 */
@Slf4j
public class StreamingResponseHandler {

    private final ObjectMapper objectMapper;
    private final Consumer<String> onChunk;
    private final Runnable onComplete;
    private final Consumer<Exception> onError;

    public StreamingResponseHandler(ObjectMapper objectMapper, 
                                  Consumer<String> onChunk, 
                                  Runnable onComplete, 
                                  Consumer<Exception> onError) {
        this.objectMapper = objectMapper;
        this.onChunk = onChunk;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    /**
     * 处理流式HTTP响应
     */
    public void handleResponse(CloseableHttpResponse response) {
        boolean completed = false;
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                onError.accept(new IOException("AI服务返回状态码: " + statusCode));
                return;
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                onError.accept(new IOException("AI服务响应为空"));
                return;
            }

            // 读取流式响应
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                
                String line;
                StringBuilder contentBuffer = new StringBuilder();
                
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 处理SSE数据行
                    if (line.startsWith("data: ")) {
                        String jsonData = line.substring(6).trim();
                        
                        // 检查是否是结束标记
                        if ("[DONE]".equals(jsonData)) {
                            if (!completed) {
                                completed = true;
                                onComplete.run();
                            }
                            break;
                        }
                        
                        // 解析JSON数据并提取内容
                        try {
                            ChatResponse chatResponse = objectMapper.readValue(jsonData, ChatResponse.class);
                            String content = extractContent(chatResponse);
                            
                            if (content != null && !content.isEmpty()) {
                                contentBuffer.append(content);
                                onChunk.accept(content);
                            }
                            
                        } catch (Exception e) {
                            log.warn("解析流式响应数据失败: {}, 数据: {}", e.getMessage(), jsonData);
                        }
                    }
                }
                
                // 如果没有收到[DONE]标记，手动触发完成
                if (!completed && !contentBuffer.toString().isEmpty()) {
                    completed = true;
                    onComplete.run();
                }
                
            }
            
        } catch (IOException e) {
            log.error("处理流式响应时发生IOException: {}", e.getMessage(), e);
            if (!completed) {
                onError.accept(e);
            }
        } catch (Exception e) {
            log.error("处理流式响应时发生异常: {}", e.getMessage(), e);
            if (!completed) {
                onError.accept(e);
            }
        }
    }

    /**
     * 从ChatResponse中提取内容
     */
    private String extractContent(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
            return null;
        }
        
        ChatResponse.Choice firstChoice = chatResponse.getChoices().get(0);
        if (firstChoice == null || firstChoice.getDelta() == null) {
            return null;
        }
        
        return firstChoice.getDelta().getContent();
    }
}