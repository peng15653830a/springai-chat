package com.example.ai.api.impl;

import com.example.ai.api.ChatApi;
import com.example.ai.api.ChatCompletionRequest;
import com.example.ai.api.ChatCompletionResponse;
import com.example.config.MultiModelProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * DeepSeek Chat API实现
 * 标准化的API接口，遵循Spring AI设计理念
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class DeepSeekChatApi implements ChatApi {

    private static final String PROVIDER_NAME = "DeepSeek";

    // 魔法常量定义
    private static final String JSON_START = "{";
    private static final String JSON_END = "}";
    private static final String DONE_MARKER = "[DONE]";
    private static final String CHAT_COMPLETION_CHUNK = "chat.completion.chunk";
    private static final String DEEPSEEK_MODEL = "deepseek";
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MultiModelProperties multiModelProperties;

    public DeepSeekChatApi(WebClient.Builder webClientBuilder,
                          ObjectMapper objectMapper,
                          MultiModelProperties multiModelProperties) {
        this.objectMapper = objectMapper;
        this.multiModelProperties = multiModelProperties;
        
        // 创建WebClient
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        // 初始化完成
        
        log.info("🏗️ 初始化DeepSeek Chat API完成");
    }

    @Override
    public Flux<ChatCompletionResponse> chatCompletionStream(ChatCompletionRequest request) {
        log.info("🚀 DeepSeek API流式聊天开始，模型: {}", request.getModel());

        try {
            String requestBody = buildRequestBody(request);
            String apiUrl = getApiEndpoint() + "/v1/chat/completions";
            
            MultiModelProperties.ProviderConfig providerConfig = getProviderConfig();
            String apiKey = multiModelProperties.getApiKey(PROVIDER_NAME);
            
            return webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "SpringAI-Chat/1.0")
                    .bodyValue(requestBody)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(Duration.ofMillis(providerConfig.getReadTimeoutMs()))
                    .filter(this::isValidSseLine)
                    .filter(line -> !DONE_MARKER.equals(line.trim())) // 过滤[DONE]标记
                    .map(this::extractJsonData)
                    .filter(json -> json != null && !json.trim().isEmpty())
                    .concatMap(this::parseJsonChunk)
                    .filter(response -> response != null)
                    .doOnNext(response -> log.debug("💬 收到DeepSeek响应: {}", response.getId()))
                    .doOnError(error -> log.error("❌ DeepSeek API调用失败", error))
                    .retry(3);

        } catch (Exception e) {
            log.error("❌ DeepSeek API请求构建失败", e);
            return Flux.error(e);
        }
    }

    @Override
    public boolean isAvailable() {
        MultiModelProperties.ProviderConfig providerConfig = getProviderConfig();
        if (providerConfig == null) {
            throw new NullPointerException("Provider config not found");
        }
        String apiKey = multiModelProperties.getApiKey(PROVIDER_NAME);
        return providerConfig.isEnabled() &&
               apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public String getApiEndpoint() {
        MultiModelProperties.ProviderConfig providerConfig = getProviderConfig();
        return providerConfig != null ? providerConfig.getBaseUrl() : null;
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(ChatCompletionRequest request) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>(8);
        
        // 基本参数
        requestBody.put("model", request.getModel());
        requestBody.put("messages", request.getMessages());
        requestBody.put("stream", request.getStream());
        
        if (request.getTemperature() != null) {
            requestBody.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            requestBody.put("max_tokens", request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            requestBody.put("top_p", request.getTopP());
        }
        
        // DeepSeek特定参数
        if (request.getExtra() != null) {
            Boolean enableThinking = (Boolean) request.getExtra().get("enable_thinking");
            Integer thinkingBudget = (Integer) request.getExtra().get("thinking_budget");
            
            if (enableThinking != null && enableThinking) {
                requestBody.put("enable_thinking", true);
                if (thinkingBudget != null) {
                    requestBody.put("thinking_budget", thinkingBudget);
                }
                log.info("🧠 DeepSeek启用推理模式，thinking_budget: {}", thinkingBudget);
            }
        }
        
        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * 检查是否为有效的SSE行（整合自DeepSeekSseParser）
     */
    private boolean isValidSseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = line.trim();
        
        // ModelScope直接返回JSON格式
        if (trimmed.startsWith(JSON_START) && trimmed.endsWith(JSON_END)) {
            return true;
        }

        // 结束标记
        if (trimmed.equals(DONE_MARKER)) {
            return true;
        }
        
        return false;
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
        
        // 结束标记
        if (trimmed.equals("[DONE]")) {
            log.debug("🏁 收到DeepSeek结束标记");
            return null;
        }
        
        return null;
    }

    /**
     * 解析JSON chunk，支持推理内容提取（整合自DeepSeekSseParser）
     */
    private Flux<ChatCompletionResponse> parseJsonChunk(String json) {
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
            Flux<ChatCompletionResponse> responses = Flux.empty();
            
            // 处理推理内容
            if (!reasoningContent.isEmpty()) {
                log.debug("🧠 提取到DeepSeek推理内容，长度: {}", reasoningContent.length());
                
                ChatCompletionResponse.Delta reasoningDelta = ChatCompletionResponse.Delta.builder()
                        .reasoning(reasoningContent)
                        .build();
                
                ChatCompletionResponse.Choice reasoningChoice = ChatCompletionResponse.Choice.builder()
                        .index(0)
                        .delta(reasoningDelta)
                        .build();

                ChatCompletionResponse reasoningResponse = ChatCompletionResponse.builder()
                        .id(chunk.path("id").asText("deepseek-" + java.util.UUID.randomUUID()))
                        .object("chat.completion.chunk")
                        .created(chunk.path("created").asLong(System.currentTimeMillis() / 1000))
                        .model(chunk.path("model").asText("deepseek"))
                        .choices(java.util.Collections.singletonList(reasoningChoice))
                        .build();
                        
                responses = responses.concatWith(Flux.just(reasoningResponse));
            }
            
            // 处理普通内容
            if (!content.isEmpty()) {
                log.debug("💬 提取到DeepSeek内容，长度: {}", content.length());
                
                ChatCompletionResponse.Delta contentDelta = ChatCompletionResponse.Delta.builder()
                        .content(content)
                        .build();
                
                ChatCompletionResponse.Choice contentChoice = ChatCompletionResponse.Choice.builder()
                        .index(0)
                        .delta(contentDelta)
                        .finishReason(choices.get(0).path("finish_reason").asText(null))
                        .build();

                ChatCompletionResponse contentResponse = ChatCompletionResponse.builder()
                        .id(chunk.path("id").asText("deepseek-" + java.util.UUID.randomUUID()))
                        .object("chat.completion.chunk")
                        .created(chunk.path("created").asLong(System.currentTimeMillis() / 1000))
                        .model(chunk.path("model").asText("deepseek"))
                        .choices(java.util.Collections.singletonList(contentChoice))
                        .build();
                        
                responses = responses.concatWith(Flux.just(contentResponse));
            }
            
            return responses.filter(response -> response != null);
            
        } catch (Exception e) {
            log.error("❌ 解析DeepSeek JSON chunk失败: {}", json, e);
            return Flux.empty();
        }
    }

    /**
     * 获取提供者配置
     */
    private MultiModelProperties.ProviderConfig getProviderConfig() {
        return multiModelProperties.getProviders().get(PROVIDER_NAME);
    }
}
