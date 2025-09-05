package com.example.service;

import com.example.config.MultiModelProperties;
import com.example.dto.response.SseEventResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 直接处理ModelScope API的原始响应，提取reasoning_content字段
 * 
 * @author xupeng
 */
@Slf4j
@Service
public class ModelScopeDirectService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    private final MultiModelProperties multiModelProperties;

    public ModelScopeDirectService(WebClient.Builder webClientBuilder, 
                                  ObjectMapper objectMapper,
                                  MessageService messageService,
                                  MultiModelProperties multiModelProperties) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.multiModelProperties = multiModelProperties;
    }

    /**
     * 直接调用ModelScope API，提取reasoning_content
     */
    public Flux<SseEventResponse> executeDirectStreaming(String prompt, Long conversationId, boolean deepThinking) {
        log.info("🚀 开始直接调用ModelScope API，深度思考: {}", deepThinking);

        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder thinkingBuilder = new StringBuilder();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("AI正在思考中...")),
            
            // 2. 调用ModelScope API并处理原始响应
            callModelScopeApi(prompt, deepThinking)
                .doOnNext(event -> {
                    // 收集内容用于保存
                    if ("chunk".equals(event.getType()) && event.getData() != null) {
                        contentBuilder.append(event.getData().toString());
                    } else if ("thinking".equals(event.getType()) && event.getData() != null) {
                        thinkingBuilder.append(event.getData().toString());
                    }
                })
                .concatWith(
                    // 3. 保存消息并发送结束事件
                    saveMessageAndGenerateEndEvent(conversationId, contentBuilder.toString(), 
                        thinkingBuilder.length() > 0 ? thinkingBuilder.toString() : null)
                )
        );
    }

    /**
     * 调用ModelScope API并解析原始SSE响应
     */
    private Flux<SseEventResponse> callModelScopeApi(String prompt, boolean deepThinking) {
        Map<String, Object> requestBody = buildRequestBody(prompt, deepThinking);
        
        MultiModelProperties.ProviderConfig qwenConfig = multiModelProperties.getProviders().get("qwen");
        String apiKey = multiModelProperties.getApiKey("qwen");
        
        return webClient.post()
                .uri(qwenConfig.getBaseUrl() + "/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(line -> log.debug("🔍 收到原始SSE行: '{}'", line))
                .filter(line -> {
                    // ModelScope API 直接返回JSON，不使用标准SSE格式
                    String trimmed = line.trim();
                    boolean isValid = !trimmed.isEmpty() && 
                                    (trimmed.startsWith("{") || trimmed.equals("[DONE]"));
                    if (!isValid && !trimmed.isEmpty()) {
                        log.debug("🚫 跳过无效行: '{}'", line);
                    }
                    return isValid;
                })
                .map(line -> {
                    String json = line.trim();
                    log.debug("🔄 提取JSON: '{}'", json);
                    return json;
                })
                .filter(json -> {
                    boolean isDone = json.equals("[DONE]");
                    if (isDone) {
                        log.debug("🏁 收到结束标记: [DONE]");
                    }
                    return !isDone;
                })
                .flatMap(this::parseJsonChunk)
                .onErrorResume(error -> {
                    log.error("❌ ModelScope API调用失败", error);
                    return Flux.just(SseEventResponse.error("AI服务暂时不可用：" + error.getMessage()));
                });
    }

    /**
     * 解析JSON chunk，提取content和reasoning_content
     */
    private Flux<SseEventResponse> parseJsonChunk(String json) {
        try {
            log.debug("🔍 收到JSON chunk: {}", json);
            
            JsonNode chunk = objectMapper.readTree(json);
            log.debug("🔍 解析后的JSON结构: {}", chunk.toPrettyString());
            
            JsonNode choices = chunk.path("choices");
            log.debug("🔍 choices节点: {}", choices);
            
            if (choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).path("delta");
                log.debug("🔍 delta节点: {}", delta);
                
                List<SseEventResponse> events = new ArrayList<>();
                
                // 提取推理内容
                String reasoningContent = delta.path("reasoning_content").asText("");
                log.debug("🔍 reasoning_content字段值: '{}'", reasoningContent);
                if (!reasoningContent.isEmpty()) {
                    events.add(SseEventResponse.thinking(reasoningContent));
                    log.info("🧠 提取到推理内容，长度: {}", reasoningContent.length());
                }
                
                // 提取普通内容
                String content = delta.path("content").asText("");
                log.debug("🔍 content字段值: '{}'", content);
                if (!content.isEmpty()) {
                    events.add(SseEventResponse.chunk(content));
                    log.info("💬 提取到内容，长度: {}", content.length());
                }
                
                // 如果两个都为空，记录所有字段名
                if (reasoningContent.isEmpty() && content.isEmpty()) {
                    log.warn("⚠️ delta节点中没有找到content或reasoning_content，所有字段: {}", 
                        delta.fieldNames().hasNext() ? 
                        StreamSupport.stream(Spliterators.spliteratorUnknownSize(delta.fieldNames(), 0), false)
                            .collect(Collectors.toList()) : "无字段");
                }
                
                return Flux.fromIterable(events);
            }
            
            return Flux.empty();
            
        } catch (Exception e) {
            log.error("❌ 解析JSON chunk失败: {}", json, e);
            return Flux.empty();
        }
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String prompt, boolean deepThinking) {
        MultiModelProperties.ProviderConfig qwenConfig = multiModelProperties.getProviders().get("qwen");
        MultiModelProperties.ModelConfig defaultModel = qwenConfig.getModels().get(0); // 使用第一个模型作为默认
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", defaultModel.getName());
        requestBody.put("temperature", defaultModel.getTemperature());
        requestBody.put("max_tokens", defaultModel.getMaxTokens());
        requestBody.put("stream", true);
        
        // 推理模式配置
        if (deepThinking && defaultModel.isSupportsThinking()) {
            requestBody.put("enable_thinking", true);
            requestBody.put("thinking_budget", defaultModel.getThinkingBudget());
            log.info("🧠 启用推理模式，thinking_budget: {}", defaultModel.getThinkingBudget());
        } else {
            // 普通模式：不添加enable_thinking参数，让API使用默认行为
            log.info("💭 普通模式：不启用推理功能");
        }
        
        // 构建消息
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个有用的AI助手。");
        messages.add(systemMessage);
        
        // 用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        log.debug("🔧 构建请求体完成，深度思考: {}, 消息数: {}", deepThinking, messages.size());
        return requestBody;
    }

    /**
     * 保存消息并生成结束事件
     */
    private Mono<SseEventResponse> saveMessageAndGenerateEndEvent(Long conversationId, String content, String thinking) {
        log.info("💾 准备保存AI响应，会话ID: {}, 内容长度: {}, 推理长度: {}", 
            conversationId, content.length(), thinking != null ? thinking.length() : 0);
        
        if (content == null || content.trim().isEmpty()) {
            log.warn("⚠️ AI响应内容为空，会话ID: {}", conversationId);
            return Mono.just(SseEventResponse.end(null));
        }
        
        return messageService.saveAiMessageAsync(conversationId, content.trim(), thinking)
            .onErrorReturn(SseEventResponse.error("保存AI响应失败"));
    }
}