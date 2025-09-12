package com.example.integration.ai.deepseek;

import com.example.config.MultiModelProperties;
import com.example.dto.request.ChatCompletionRequest;
import com.example.dto.response.ChatCompletionResponse;
import com.example.integration.ai.api.ChatApi;
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

    /**
     * JSON开始标记
     */
    private static final String JSON_START = "{";
    /**
     * JSON结束标记
     */
    private static final String JSON_END = "}";
    /**
     * 完成标记
     */
    private static final String DONE_MARKER = "[DONE]";
    /**
     * 聊天完成块类型
     */
    private static final String CHAT_COMPLETION_CHUNK = "chat.completion.chunk";
    /**
     * DeepSeek模型名称
     */
    private static final String DEEPSEEK_MODEL = "deepseek";
    /**
     * 开括号标记
     */
    private static final String OPEN_BRACE = "{";
    /**
     * 完成标记
     */
    private static final String DONE = "[DONE]";
    /**
     * 推理内容字段名
     */
    private static final String REASONING_CONTENT = "reasoning_content";
    /**
     * 内容字段名
     */
    private static final String CONTENT = "content";
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MultiModelProperties multiModelProperties;

    public DeepSeekChatApi(WebClient.Builder webClientBuilder,
                          ObjectMapper objectMapper,
                          MultiModelProperties multiModelProperties) {
        this.objectMapper = objectMapper;
        this.multiModelProperties = multiModelProperties;
        
        // 创建WebClient，添加空值检查
        if (webClientBuilder != null) {
            this.webClient = webClientBuilder
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        } else {
            this.webClient = null;
        }
        
        log.info("🏗️ 初始化DeepSeek Chat API完成");
    }

    @Override
    public Flux<ChatCompletionResponse> chatCompletionStream(ChatCompletionRequest request) {
        log.info("🚀 DeepSeek API流式聊天开始，模型: {}", request.getModel());

        // 检查webClient是否已初始化
        if (webClient == null) {
            log.error("❌ DeepSeek API未正确初始化");
            return Flux.error(new IllegalStateException("WebClient not initialized"));
        }

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
                    .timeout(Duration.ofMillis(providerConfig != null ? providerConfig.getReadTimeoutMs() : 30000))
                    .filter(this::isValidSseLine)
                    // 过滤[DONE]标记
                    .filter(line -> !DONE_MARKER.equals(line.trim()))
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
        try {
            MultiModelProperties.ProviderConfig providerConfig = getProviderConfig();
            // 返回false而不是抛出异常
            if (providerConfig == null) {
                return false;
            }
            String apiKey = multiModelProperties.getApiKey(PROVIDER_NAME);
            return providerConfig.isEnabled() &&
                   apiKey != null && !apiKey.trim().isEmpty();
        } catch (Exception e) {
            log.warn("检查DeepSeek可用性时出错: {}", e.getMessage());
            return false;
        }
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
        if (trimmed.startsWith(OPEN_BRACE)) {
            return trimmed;
        }
        
        // 结束标记
        if (trimmed.equals(DONE)) {
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
            
            // 即使没有choices，也要返回一个响应以确保流继续
            if (!choices.isArray() || choices.size() == 0) {
                ChatCompletionResponse emptyResponse = ChatCompletionResponse.builder()
                        .id(chunk.path("id").asText("deepseek-" + java.util.UUID.randomUUID()))
                        .object("chat.completion.chunk")
                        .created(chunk.path("created").asLong(System.currentTimeMillis() / 1000))
                        .model(chunk.path("model").asText("deepseek"))
                        .choices(java.util.Collections.emptyList())
                        .build();
                return Flux.just(emptyResponse);
            }

            JsonNode delta = choices.get(0).path("delta");
            
            // 提取推理内容
            String reasoningContent = "";
            if (delta.has(REASONING_CONTENT)) {
                reasoningContent = delta.path(REASONING_CONTENT).asText("");
            }
            
            // 提取普通内容
            String content = "";
            if (delta.has(CONTENT)) {
                content = delta.path(CONTENT).asText("");
            }
            
            // 创建响应
            ChatCompletionResponse.Delta responseDelta = ChatCompletionResponse.Delta.builder()
                    .content(content)
                    .reasoning(reasoningContent)
                    .build();
            
            ChatCompletionResponse.Choice choice = ChatCompletionResponse.Choice.builder()
                    .index(0)
                    .delta(responseDelta)
                    .finishReason(choices.get(0).path("finish_reason").asText(null))
                    .build();

            ChatCompletionResponse response = ChatCompletionResponse.builder()
                    .id(chunk.path("id").asText("deepseek-" + java.util.UUID.randomUUID()))
                    .object("chat.completion.chunk")
                    .created(chunk.path("created").asLong(System.currentTimeMillis() / 1000))
                    .model(chunk.path("model").asText("deepseek"))
                    .choices(java.util.Collections.singletonList(choice))
                    .build();
            
            return Flux.just(response);
            
        } catch (Exception e) {
            log.error("❌ 解析DeepSeek JSON chunk失败: {}", json, e);
            // 即使解析失败，也要返回一个空响应以确保流继续
            ChatCompletionResponse errorResponse = ChatCompletionResponse.builder()
                    .id("deepseek-error-" + java.util.UUID.randomUUID())
                    .object("chat.completion.chunk")
                    .created(System.currentTimeMillis() / 1000)
                    .model("deepseek")
                    .choices(java.util.Collections.emptyList())
                    .build();
            return Flux.just(errorResponse);
        }
    }

    /**
     * 获取提供者配置
     */
    private MultiModelProperties.ProviderConfig getProviderConfig() {
        Map<String, MultiModelProperties.ProviderConfig> providers = multiModelProperties.getProviders();
        if (providers == null) {
            return null;
        }
        return providers.get(PROVIDER_NAME);
    }
}