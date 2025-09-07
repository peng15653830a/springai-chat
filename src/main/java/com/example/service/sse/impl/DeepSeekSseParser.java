package com.example.service.sse.impl;

import com.example.service.sse.SseResponseParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek推理模型SSE响应解析器
 * 支持推理内容(reasoning_content)的提取和处理
 *
 * @author xupeng
 */
@Slf4j
@Component
public class DeepSeekSseParser implements SseResponseParser {

    private final ObjectMapper objectMapper;

    public DeepSeekSseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<ChatResponse> parseStream(Flux<String> sseLines) {
        return sseLines
                .filter(this::isValidSseLine)
                .map(this::extractJsonData)
                .filter(json -> json != null && !json.trim().isEmpty() && !json.equals("[DONE]"))
                .flatMap(this::parseJsonChunk)
                .doOnNext(response -> {
                    String content = response.getResult().getOutput().getText();
                    log.debug("🔄 解析DeepSeek响应: {}", content.length() > 50 ? content.substring(0, 50) + "..." : content);
                })
                .onErrorResume(error -> {
                    log.error("❌ 解析DeepSeek SSE响应失败", error);
                    return Flux.empty();
                });
    }

    @Override
    public boolean isValidSseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = line.trim();
        
        // ModelScope直接返回JSON格式
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return true;
        }
        
        // 结束标记
        if (trimmed.equals("[DONE]")) {
            return true;
        }
        
        // 其他无效行
        return false;
    }

    @Override
    public String getResponseFormat() {
        return "ModelScope-JSON";
    }

    /**
     * 提取JSON数据
     */
    private String extractJsonData(String line) {
        String trimmed = line.trim();
        
        // 完整JSON格式
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        
        // 结束标记
        if (trimmed.equals("[DONE]")) {
            log.debug("🏁 收到DeepSeek结束标记");
            return null;
        }
        
        return null;
    }

    /**
     * 解析JSON chunk，支持推理内容提取
     */
    private Flux<ChatResponse> parseJsonChunk(String json) {
        try {
            log.debug("🔍 解析DeepSeek JSON: {}", json.length() > 100 ? json.substring(0, 100) + "..." : json);
            
            JsonNode chunk = objectMapper.readTree(json);
            JsonNode choices = chunk.path("choices");
            
            if (!choices.isArray() || choices.size() == 0) {
                return Flux.empty();
            }

            JsonNode delta = choices.get(0).path("delta");
            
            // 提取推理内容
            String reasoningContent = delta.path("reasoning_content").asText("");
            
            // 提取普通内容
            String content = delta.path("content").asText("");
            
            // 创建响应列表
            Flux<ChatResponse> responses = Flux.empty();
            
            // 处理推理内容
            if (!reasoningContent.isEmpty()) {
                log.debug("🧠 提取到DeepSeek推理内容，长度: {}", reasoningContent.length());
                
                // 创建带推理元数据的ChatResponse
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("reasoning_content", reasoningContent);
                metadata.put("type", "thinking");
                
                Generation thinkingGeneration = new Generation(new AssistantMessage(reasoningContent));
                
                ChatResponse thinkingResponse = new ChatResponse(List.of(thinkingGeneration));
                responses = responses.concatWith(Flux.just(thinkingResponse));
            }
            
            // 处理普通内容
            if (!content.isEmpty()) {
                log.debug("💬 提取到DeepSeek内容，长度: {}", content.length());
                
                Generation contentGeneration = new Generation(new AssistantMessage(content));
                ChatResponse contentResponse = new ChatResponse(List.of(contentGeneration));
                responses = responses.concatWith(Flux.just(contentResponse));
            }
            
            return responses;
            
        } catch (Exception e) {
            log.error("❌ 解析DeepSeek JSON chunk失败: {}", json, e);
            return Flux.empty();
        }
    }
}