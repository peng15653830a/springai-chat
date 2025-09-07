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

import java.util.List;

/**
 * 长城大模型SSE响应解析器
 * 处理长城大模型特殊的JSON流式响应格式
 *
 * @author xupeng
 */
@Slf4j
@Component
public class GreatWallSseParser implements SseResponseParser {

    private final ObjectMapper objectMapper;

    public GreatWallSseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<ChatResponse> parseStream(Flux<String> sseLines) {
        return sseLines
                .filter(this::isValidSseLine)
                .flatMap(this::parseJsonLine)
                .doOnNext(response -> log.debug("🔄 解析长城大模型响应: {}", 
                    response.getResult().getOutput().getText()))
                .onErrorResume(error -> {
                    log.error("❌ 解析长城大模型SSE响应失败", error);
                    return Flux.empty();
                });
    }

    @Override
    public boolean isValidSseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = line.trim();
        
        // 长城大模型使用完整的JSON格式
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return true;
        }
        
        // 也支持标准的data:格式
        return trimmed.startsWith("data:");
    }

    @Override
    public String getResponseFormat() {
        return "GreatWall-JSON";
    }

    /**
     * 解析长城大模型JSON行
     */
    private Flux<ChatResponse> parseJsonLine(String line) {
        try {
            String jsonData = extractJsonData(line);
            if (jsonData == null || jsonData.trim().isEmpty()) {
                return Flux.empty();
            }

            JsonNode dataNode = objectMapper.readTree(jsonData);
            String event = dataNode.path("event").asText("");
            
            log.debug("🔍 长城大模型事件类型: {}", event);

            switch (event) {
                case "message_start":
                    log.debug("🚀 长城大模型开始响应");
                    return Flux.empty(); // start事件不产生内容

                case "llm_chunk":
                    return parseChunkContent(dataNode);

                case "message_finished":
                    return parseCompleteContent(dataNode);

                case "llm_finished":
                    log.debug("🏁 长城大模型响应完成");
                    return Flux.empty();

                default:
                    log.debug("🔄 未处理的长城大模型事件: {}", event);
                    return Flux.empty();
            }
            
        } catch (Exception e) {
            log.error("❌ 解析长城大模型JSON行失败: {}", line, e);
            return Flux.empty();
        }
    }

    /**
     * 提取JSON数据部分
     */
    private String extractJsonData(String line) {
        String trimmed = line.trim();
        
        // 完整JSON格式
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        
        // data:格式
        if (trimmed.startsWith("data:")) {
            return trimmed.substring(5).trim();
        }
        
        return null;
    }

    /**
     * 解析chunk内容
     */
    private Flux<ChatResponse> parseChunkContent(JsonNode dataNode) {
        JsonNode choices = dataNode.path("data").path("choices");
        
        if (!choices.isArray() || choices.size() == 0) {
            return Flux.empty();
        }

        JsonNode delta = choices.get(0).path("delta");
        String content = delta.path("content").asText("");

        if (content.isEmpty()) {
            return Flux.empty();
        }

        log.debug("💬 长城大模型内容片段: {}", content);

        // 创建ChatResponse
        Generation generation = new Generation(new AssistantMessage(content));
        ChatResponse response = new ChatResponse(List.of(generation));
        
        return Flux.just(response);
    }

    /**
     * 解析完整内容
     */
    private Flux<ChatResponse> parseCompleteContent(JsonNode dataNode) {
        String output = dataNode.path("data").path("output").asText("");
        
        if (output.isEmpty()) {
            return Flux.empty();
        }

        log.debug("📝 长城大模型完整输出: {}", output);

        // 创建ChatResponse（标记为完成）
        Generation generation = new Generation(new AssistantMessage(output));
        ChatResponse response = new ChatResponse(List.of(generation));
        
        return Flux.just(response);
    }
}