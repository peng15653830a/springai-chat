package com.example.service.provider.impl;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.service.MessageService;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.factory.ModelProviderFactory;
import com.example.service.provider.ModelProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OpenAI模型提供者实现
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class OpenaiModelProvider implements ModelProvider {

    private static final String PROVIDER_NAME = "openai";
    private static final String DISPLAY_NAME = "OpenAI";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final MultiModelProperties multiModelProperties;

    @Autowired
    private ModelProviderFactory modelProviderFactory;

    public OpenaiModelProvider(WebClient.Builder webClientBuilder,
                              ObjectMapper objectMapper,
                              MessageService messageService,
                              MultiModelProperties multiModelProperties) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.multiModelProperties = multiModelProperties;
    }

    @PostConstruct
    public void init() {
        // 自动注册到工厂
        modelProviderFactory.registerProvider(this);
        log.info("OpenAI模型提供者已注册: {}", PROVIDER_NAME);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public List<ModelInfo> getAvailableModels() {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(PROVIDER_NAME);
        
        if (providerConfig == null || !providerConfig.isEnabled()) {
            return Collections.emptyList();
        }

        return providerConfig.getModels().stream()
                .filter(MultiModelProperties.ModelConfig::isEnabled)
                .map(this::convertToModelInfo)
                .collect(Collectors.toList());
    }

    @Override
    public Flux<SseEventResponse> streamChat(ChatRequest request) {
        log.info("🚀 OpenAI开始流式聊天，模型: {}", request.getModelName());

        StringBuilder contentBuilder = new StringBuilder();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("AI正在思考中...")),
            
            // 2. 调用OpenAI API并处理响应
            callOpenaiApi(request)
                .doOnNext(event -> {
                    // 收集内容用于保存
                    if ("chunk".equals(event.getType()) && event.getData() != null) {
                        contentBuilder.append(event.getData().toString());
                    }
                })
                .concatWith(
                    // 3. 保存消息并发送结束事件
                    saveMessageAndGenerateEndEvent(request.getConversationId(), 
                        contentBuilder.toString(), null)
                )
        );
    }

    @Override
    public boolean isAvailable() {
        return multiModelProperties.isProviderAvailable(PROVIDER_NAME);
    }

    @Override
    public boolean supportsThinking(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsThinking)
                .orElse(false);
    }

    @Override
    public boolean supportsStreaming(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::isSupportsStreaming)
                .orElse(true);
    }

    @Override
    public ModelInfo getModelInfo(String modelName) {
        return getModelConfig(modelName)
                .map(this::convertToModelInfo)
                .orElse(null);
    }

    /**
     * 调用OpenAI API
     */
    private Flux<SseEventResponse> callOpenaiApi(ChatRequest request) {
        Map<String, Object> requestBody = buildRequestBody(request);
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        return webClient.post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(line -> log.debug("🔍 收到原始SSE行: '{}'", line))
                .filter(this::isValidJsonLine)
                .map(String::trim)
                .filter(json -> !json.equals("[DONE]"))
                .flatMap(this::parseJsonChunk)
                .onErrorResume(error -> {
                    log.error("❌ OpenAI API调用失败", error);
                    return Flux.just(SseEventResponse.error("AI服务暂时不可用：" + error.getMessage()));
                });
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(ChatRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", request.getModelName());
        requestBody.put("stream", true);

        // 设置参数
        requestBody.put("temperature", request.getTemperature() != null ? 
            request.getTemperature() : getDefaultTemperature(request.getModelName()));
        requestBody.put("max_tokens", request.getMaxTokens() != null ? 
            request.getMaxTokens() : getDefaultMaxTokens(request.getModelName()));

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
        userMessage.put("content", request.getFullPrompt());
        messages.add(userMessage);
        
        requestBody.put("messages", messages);

        log.debug("🔧 构建请求体完成，消息数: {}", messages.size());
        return requestBody;
    }

    /**
     * 解析JSON chunk
     */
    private Flux<SseEventResponse> parseJsonChunk(String json) {
        try {
            log.debug("🔍 收到JSON chunk: {}", json);
            
            JsonNode chunk = objectMapper.readTree(json);
            JsonNode choices = chunk.path("choices");
            
            if (choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).path("delta");
                
                // 提取内容
                String content = delta.path("content").asText("");
                if (!content.isEmpty()) {
                    log.info("💬 提取到内容，长度: {}", content.length());
                    return Flux.just(SseEventResponse.chunk(content));
                }
            }
            
            return Flux.empty();
            
        } catch (Exception e) {
            log.error("❌ 解析JSON chunk失败: {}", json, e);
            return Flux.empty();
        }
    }

    /**
     * 保存消息并生成结束事件
     */
    private Mono<SseEventResponse> saveMessageAndGenerateEndEvent(Long conversationId, 
                                                                 String content, 
                                                                 String thinking) {
        log.info("💾 准备保存AI响应，会话ID: {}, 内容长度: {}", 
            conversationId, content.length());
        
        if (content == null || content.trim().isEmpty()) {
            log.warn("⚠️ AI响应内容为空，会话ID: {}", conversationId);
            return Mono.just(SseEventResponse.end(null));
        }
        
        return messageService.saveAiMessageAsync(conversationId, content.trim(), thinking)
            .onErrorReturn(SseEventResponse.error("保存AI响应失败"));
    }

    /**
     * 检查是否为有效的JSON行
     */
    private boolean isValidJsonLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        String trimmed = line.trim();
        boolean isValid = trimmed.startsWith("{") || trimmed.equals("[DONE]");
        if (!isValid && !trimmed.isEmpty()) {
            log.debug("🚫 跳过无效行: '{}'", line);
        }
        return isValid;
    }

    /**
     * 将配置转换为ModelInfo
     */
    private ModelInfo convertToModelInfo(MultiModelProperties.ModelConfig config) {
        ModelInfo info = new ModelInfo();
        info.setId((long) config.getName().hashCode()); // 临时ID，实际应从数据库获取
        info.setName(config.getName());
        info.setDisplayName(config.getDisplayName());
        info.setMaxTokens(config.getMaxTokens());
        info.setTemperature(config.getTemperature());
        info.setSupportsThinking(config.isSupportsThinking());
        info.setSupportsStreaming(config.isSupportsStreaming());
        info.setAvailable(config.isEnabled() && isAvailable());
        info.setSortOrder(config.getSortOrder());
        return info;
    }

    /**
     * 获取模型配置
     */
    private Optional<MultiModelProperties.ModelConfig> getModelConfig(String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(PROVIDER_NAME);
        
        if (providerConfig == null) {
            return Optional.empty();
        }

        return providerConfig.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .findFirst();
    }

    /**
     * 获取API密钥
     */
    private String getApiKey() {
        return multiModelProperties.getApiKey(PROVIDER_NAME);
    }

    /**
     * 获取基础URL
     */
    private String getBaseUrl() {
        MultiModelProperties.ProviderConfig config = 
            multiModelProperties.getProviders().get(PROVIDER_NAME);
        return config != null ? config.getBaseUrl() : 
            "https://api-inference.modelscope.cn/v1";
    }

    /**
     * 获取默认温度参数
     */
    private double getDefaultTemperature(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::getTemperature)
                .map(BigDecimal::doubleValue)
                .orElse(multiModelProperties.getDefaults().getTemperature().doubleValue());
    }

    /**
     * 获取默认最大token数
     */
    private int getDefaultMaxTokens(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::getMaxTokens)
                .orElse(multiModelProperties.getDefaults().getMaxTokens());
    }
}