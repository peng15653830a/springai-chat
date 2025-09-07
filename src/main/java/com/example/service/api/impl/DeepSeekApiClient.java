package com.example.service.api.impl;

import com.example.config.MultiModelProperties;
import com.example.service.api.ApiConfiguration;
import com.example.service.api.ModelApiClient;
import com.example.service.sse.impl.DeepSeekSseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek推理模型API客户端
 * 封装ModelScope API调用，支持推理内容提取
 *
 * @author xupeng
 */
@Slf4j
@Component
public class DeepSeekApiClient implements ModelApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final DeepSeekSseParser sseParser;
    private final MultiModelProperties multiModelProperties;
    private final ApiConfiguration apiConfiguration;

    public DeepSeekApiClient(WebClient.Builder webClientBuilder,
                             ObjectMapper objectMapper,
                             DeepSeekSseParser sseParser,
                             MultiModelProperties multiModelProperties) {
        this.objectMapper = objectMapper;
        this.sseParser = sseParser;
        this.multiModelProperties = multiModelProperties;
        
        // 创建API配置
        this.apiConfiguration = createApiConfiguration();
        
        // 创建WebClient
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
        
        log.info("🏗️ 初始化DeepSeek API客户端完成");
    }

    @Override
    public String getProviderName() {
        return "DeepSeek"; // 注意这里使用大写D，与配置保持一致
    }

    @Override
    public Flux<ChatResponse> chatCompletionStream(List<Message> messages,
                                                  String modelName,
                                                  Double temperature,
                                                  Integer maxTokens,
                                                  Boolean enableThinking) {
        log.info("🚀 DeepSeek推理模型流式聊天开始，模型: {}，推理模式: {}", modelName, enableThinking);

        try {
            Map<String, Object> requestBody = buildRequestBody(messages, modelName, temperature, maxTokens, enableThinking);
            String apiUrl = apiConfiguration.getBaseUrl() + "/v1/chat/completions";
            
            Flux<String> sseStream = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiConfiguration.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("User-Agent", apiConfiguration.getUserAgent())
                    .bodyValue(requestBody)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(apiConfiguration.getReadTimeout())
                    .doOnNext(line -> {
                        if (apiConfiguration.isEnableDebugLog()) {
                            log.debug("🔍 收到DeepSeek原始SSE行: '{}'", line);
                        }
                    });

            return sseParser.parseStream(sseStream)
                    .doOnError(error -> log.error("❌ DeepSeek API调用失败", error))
                    .retry(apiConfiguration.getRetryAttempts());

        } catch (Exception e) {
            log.error("❌ DeepSeek API请求构建失败", e);
            return Flux.error(e);
        }
    }

    @Override
    public boolean isAvailable() {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(getProviderName());
        return providerConfig != null && providerConfig.isEnabled() && 
               apiConfiguration.getApiKey() != null && !apiConfiguration.getApiKey().trim().isEmpty();
    }

    @Override
    public String getApiEndpoint() {
        return apiConfiguration.getBaseUrl() + "/v1/chat/completions";
    }

    /**
     * 创建API配置
     */
    private ApiConfiguration createApiConfiguration() {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(getProviderName());
        
        if (providerConfig == null) {
            throw new IllegalStateException("DeepSeek配置未找到");
        }

        return ApiConfiguration.builder()
                .baseUrl(providerConfig.getBaseUrl())
                .apiKey(multiModelProperties.getApiKey(getProviderName()))
                .connectTimeout(Duration.ofMillis(providerConfig.getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(providerConfig.getReadTimeoutMs()))
                .enableDebugLog(log.isDebugEnabled())
                .retryAttempts(3) // DeepSeek重试次数
                .build();
    }

    /**
     * 构建DeepSeek请求体
     */
    private Map<String, Object> buildRequestBody(List<Message> messages,
                                                 String modelName,
                                                 Double temperature,
                                                 Integer maxTokens,
                                                 Boolean enableThinking) {
        MultiModelProperties.ModelConfig modelConfig = getModelConfig(modelName);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("temperature", temperature != null ? temperature : modelConfig.getTemperature());
        requestBody.put("max_tokens", maxTokens != null ? maxTokens : modelConfig.getMaxTokens());
        requestBody.put("stream", true);
        
        // DeepSeek推理模式配置
        if (enableThinking != null && enableThinking && modelConfig.isSupportsThinking()) {
            requestBody.put("enable_thinking", true);
            if (modelConfig.getThinkingBudget() != null) {
                requestBody.put("thinking_budget", modelConfig.getThinkingBudget());
            }
            log.info("🧠 DeepSeek启用推理模式，thinking_budget: {}", modelConfig.getThinkingBudget());
        } else {
            log.info("💭 DeepSeek普通模式：不启用推理功能");
        }
        
        // 转换消息格式
        List<Map<String, String>> messageList = new ArrayList<>();
        for (Message message : messages) {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("role", mapMessageRole(message));
            messageMap.put("content", message.getText());
            messageList.add(messageMap);
        }
        requestBody.put("messages", messageList);
        
        log.debug("🔧 构建DeepSeek请求体，消息数量: {}", messageList.size());
        return requestBody;
    }

    /**
     * 映射消息角色
     */
    private String mapMessageRole(Message message) {
        String messageType = message.getClass().getSimpleName().toLowerCase();
        switch (messageType) {
            case "usermessage":
                return "user";
            case "assistantmessage":
                return "assistant";
            case "systemmessage":
                return "system";
            default:
                return "user"; // 默认为用户消息
        }
    }

    /**
     * 获取模型配置
     */
    private MultiModelProperties.ModelConfig getModelConfig(String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(getProviderName());
        
        return providerConfig.getModels().stream()
                .filter(model -> modelName.equals(model.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到DeepSeek模型配置: " + modelName));
    }
}