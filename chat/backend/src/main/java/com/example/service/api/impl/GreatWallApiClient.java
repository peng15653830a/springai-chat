package com.example.service.api.impl;

import com.example.config.GreatWallProperties;
import com.example.config.MultiModelProperties;
import com.example.service.api.ApiConfiguration;
import com.example.service.api.ModelApiClient;
import com.example.service.sse.impl.GreatWallSseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 长城大模型API客户端
 * 封装长城大模型的API调用逻辑，提供统一接口
 *
 * @author xupeng
 */
@Slf4j
@Component
public class GreatWallApiClient implements ModelApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final GreatWallSseParser sseParser;
    private final MultiModelProperties multiModelProperties;
    private final GreatWallProperties greatWallProperties;
    private final ApiConfiguration apiConfiguration;

    public GreatWallApiClient(WebClient.Builder webClientBuilder,
                              ObjectMapper objectMapper,
                              GreatWallSseParser sseParser,
                              MultiModelProperties multiModelProperties,
                              GreatWallProperties greatWallProperties) {
        this.objectMapper = objectMapper;
        this.sseParser = sseParser;
        this.multiModelProperties = multiModelProperties;
        this.greatWallProperties = greatWallProperties;
        
        // 创建API配置
        this.apiConfiguration = createApiConfiguration();
        
        // 创建WebClient
        this.webClient = createWebClient(webClientBuilder);
        
        log.info("🏗️ 初始化长城大模型API客户端完成");
    }

    @Override
    public String getProviderName() {
        return "greatwall";
    }

    @Override
    public Flux<ChatResponse> chatCompletionStream(List<Message> messages,
                                                  String modelName,
                                                  Double temperature,
                                                  Integer maxTokens,
                                                  Boolean enableThinking) {
        log.info("🚀 长城大模型流式聊天开始，模型: {}", modelName);

        try {
            Map<String, Object> requestBody = buildRequestBody(messages, modelName, temperature, maxTokens, enableThinking);
            String apiUrl = buildApiUrl(modelName);
            
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
                            log.debug("🔍 收到长城大模型原始SSE行: '{}'", line);
                        }
                    });

            return sseParser.parseStream(sseStream)
                    .doOnError(error -> log.error("❌ 长城大模型API调用失败", error))
                    .retry(apiConfiguration.getRetryAttempts());

        } catch (Exception e) {
            log.error("❌ 长城大模型API请求构建失败", e);
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
        return apiConfiguration.getBaseUrl();
    }

    /**
     * 创建API配置
     */
    private ApiConfiguration createApiConfiguration() {
        Map<String, MultiModelProperties.ProviderConfig> providers = 
            multiModelProperties.getProviders();
        
        if (providers == null) {
            throw new IllegalStateException("长城大模型配置未找到");
        }
        
        MultiModelProperties.ProviderConfig providerConfig = 
            providers.get(getProviderName());
        
        if (providerConfig == null) {
            throw new IllegalStateException("长城大模型配置未找到");
        }
        
        // 处理SSL配置为null的情况
        boolean skipSslVerification = false;
        if (greatWallProperties.getSsl() != null) {
            skipSslVerification = greatWallProperties.getSsl().isSkipVerification();
        }

        return ApiConfiguration.builder()
                .baseUrl(providerConfig.getBaseUrl())
                .apiKey(multiModelProperties.getApiKey(getProviderName()))
                .connectTimeout(Duration.ofMillis(providerConfig.getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(providerConfig.getReadTimeoutMs()))
                .skipSslVerification(skipSslVerification)
                .enableDebugLog(log.isDebugEnabled())
                .retryAttempts(3) // 长城大模型重试次数
                .build();
    }

    /**
     * 创建WebClient（支持SSL配置）
     */
    private WebClient createWebClient(WebClient.Builder webClientBuilder) {
        if (apiConfiguration.isSkipSslVerification()) {
            log.info("🔓 长城大模型：跳过SSL证书验证（开发环境）");
            return createInsecureWebClient(webClientBuilder);
        } else {
            log.info("🔒 长城大模型：使用标准SSL验证");
            return webClientBuilder
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                    .build();
        }
    }

    /**
     * 创建跳过SSL验证的WebClient
     */
    private WebClient createInsecureWebClient(WebClient.Builder webClientBuilder) {
        try {
            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(apiConfiguration.getReadTimeout())
                    .secure(sslSpec -> {
                        try {
                            sslSpec.sslContext(SslContextBuilder.forClient()
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                    .build());
                        } catch (SSLException e) {
                            log.warn("⚠️ 长城大模型SSL配置失败: {}", e.getMessage());
                        }
                    });

            return webClientBuilder
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        } catch (Exception e) {
            log.warn("⚠️ 长城大模型WebClient配置失败，使用默认配置: {}", e.getMessage());
            return webClientBuilder.build();
        }
    }

    /**
     * 构建长城大模型请求体
     */
    private Map<String, Object> buildRequestBody(List<Message> messages,
                                                 String modelName,
                                                 Double temperature,
                                                 Integer maxTokens,
                                                 Boolean enableThinking) {
        MultiModelProperties.ModelConfig modelConfig = getModelConfig(modelName);
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // 长城大模型特有字段
        requestBody.put("tpuid", modelConfig.getTpuidPrefix() + System.currentTimeMillis() % 1000);
        requestBody.put("doc_list", new ArrayList<>());
        requestBody.put("image_url", "");
        requestBody.put("query", extractUserPrompt(messages)); // 长城大模型只支持单轮对话
        requestBody.put("session_id", "");
        requestBody.put("stream", true);
        
        log.debug("🔧 构建长城大模型请求体，用户ID: {}", requestBody.get("tpuid"));
        return requestBody;
    }

    /**
     * 构建API URL
     */
    private String buildApiUrl(String modelName) {
        MultiModelProperties.ModelConfig modelConfig = getModelConfig(modelName);
        return apiConfiguration.getBaseUrl() + "/aicoapi/gateway/v2/chatbot/api_run/" + modelConfig.getApiRunId();
    }

    /**
     * 获取模型配置
     */
    private MultiModelProperties.ModelConfig getModelConfig(String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(getProviderName());
        
        if (providerConfig == null) {
            throw new IllegalArgumentException("未找到长城大模型提供者配置: " + getProviderName());
        }
        
        // 处理modelName为null或空的情况
        if (modelName == null || modelName.trim().isEmpty()) {
            // 返回第一个可用的模型作为默认模型
            return providerConfig.getModels().stream()
                    .filter(model -> model.isEnabled())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("未找到可用的长城大模型"));
        }
        
        return providerConfig.getModels().stream()
                .filter(model -> modelName.equals(model.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到长城大模型配置: " + modelName));
    }

    /**
     * 提取用户提示词（长城大模型只支持单轮对话）
     */
    private String extractUserPrompt(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        
        // 合并所有消息为单个提示词
        StringBuilder prompt = new StringBuilder();
        for (Message message : messages) {
            // Spring AI 1.0中Message使用getText()方法获取内容
            String content = message.getText();
            if (content != null && !content.trim().isEmpty()) {
                prompt.append(content).append("\n");
            }
        }
        
        return prompt.toString().trim();
    }
}