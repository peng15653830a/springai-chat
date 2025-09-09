package com.example.ai.api.impl;

import com.example.ai.api.ChatApi;
import com.example.ai.api.ChatCompletionRequest;
import com.example.ai.api.ChatCompletionResponse;
import com.example.config.GreatWallProperties;
import com.example.config.MultiModelProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 长城大模型Chat API实现
 * 标准化的API接口，遵循Spring AI设计理念，内嵌SSE解析逻辑
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class GreatWallChatApi implements ChatApi {

    private static final String PROVIDER_NAME = "greatwall";
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MultiModelProperties multiModelProperties;
    private final GreatWallProperties greatWallProperties;

    public GreatWallChatApi(WebClient.Builder webClientBuilder,
                           ObjectMapper objectMapper,
                           MultiModelProperties multiModelProperties,
                           GreatWallProperties greatWallProperties) {
        this.objectMapper = objectMapper;
        this.multiModelProperties = multiModelProperties;
        this.greatWallProperties = greatWallProperties;
        
        // 创建支持SSL跳过验证的WebClient
        this.webClient = createWebClient(webClientBuilder);
        
        log.info("🏗️ 初始化长城大模型Chat API完成");
    }

    @Override
    public Flux<ChatCompletionResponse> chatCompletionStream(ChatCompletionRequest request) {
        log.info("🚀 长城大模型API流式聊天开始，模型: {}", request.getModel());

        try {
            String requestBody = buildRequestBody(request);
            String apiUrl = buildApiUrl(request.getModel());
            
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
                    .flatMap(this::parseJsonLine)
                    .doOnNext(response -> log.debug("💬 收到长城大模型响应: {}", response.getId()))
                    .doOnError(error -> log.error("❌ 长城大模型API调用失败", error))
                    .retry(3);

        } catch (Exception e) {
            log.error("❌ 长城大模型API请求构建失败", e);
            return Flux.error(e);
        }
    }

    @Override
    public boolean isAvailable() {
        MultiModelProperties.ProviderConfig providerConfig = getProviderConfig();
        String apiKey = multiModelProperties.getApiKey(PROVIDER_NAME);
        return providerConfig != null && providerConfig.isEnabled() && 
               apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public String getApiEndpoint() {
        MultiModelProperties.ProviderConfig providerConfig = getProviderConfig();
        return providerConfig != null ? providerConfig.getBaseUrl() : null;
    }

    /**
     * 创建支持SSL跳过验证的WebClient
     */
    private WebClient createWebClient(WebClient.Builder webClientBuilder) {
        try {
            boolean skipSslVerification = greatWallProperties != null && greatWallProperties.getSsl().isSkipVerification();
            
            if (skipSslVerification) {
                log.warn("⚠️ 长城大模型跳过SSL证书验证（仅用于开发环境）");
                
                HttpClient httpClient = HttpClient.create()
                        .secure(spec -> {
                            try {
                                spec.sslContext(SslContextBuilder.forClient()
                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                        .build());
                            } catch (SSLException e) {
                                log.error("创建不安全SSL上下文失败", e);
                            }
                        });
                
                return webClientBuilder
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build();
            } else {
                return webClientBuilder
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build();
            }
        } catch (Exception e) {
            log.error("创建WebClient失败，使用默认配置", e);
            return webClientBuilder
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        }
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(ChatCompletionRequest request) throws JsonProcessingException {
        MultiModelProperties.ModelConfig modelConfig = getModelConfig(request.getModel());
        
        Map<String, Object> requestBody = new HashMap<>();
        
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
        
        // 长城大模型特有参数
        String tpuid = generateTpuid(modelConfig);
        requestBody.put("tpuid", tpuid);
        
        log.debug("🔧 构建长城大模型请求体，TPUID: {}", tpuid);
        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * 构建API URL
     */
    private String buildApiUrl(String modelName) {
        MultiModelProperties.ModelConfig modelConfig = getModelConfig(modelName);
        String baseUrl = getApiEndpoint();
        String apiRunId = modelConfig != null ? modelConfig.getApiRunId() : "default";
        return baseUrl + "/v1/ai_serve/run/" + apiRunId + "/stream_call";
    }

    /**
     * 生成TPUID
     */
    private String generateTpuid(MultiModelProperties.ModelConfig modelConfig) {
        String prefix = modelConfig != null ? modelConfig.getTpuidPrefix() : "DefaultPrefix";
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 检查是否为有效的SSE行
     */
    private boolean isValidSseLine(String line) {
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

    /**
     * 解析JSON行（整合原GreatWallSseParser逻辑）
     */
    private Flux<ChatCompletionResponse> parseJsonLine(String line) {
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
    private Flux<ChatCompletionResponse> parseChunkContent(JsonNode dataNode) {
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

        // 转换为标准化响应
        ChatCompletionResponse.Delta deltaObj = ChatCompletionResponse.Delta.builder()
                .content(content)
                .build();
        
        ChatCompletionResponse.Choice choice = ChatCompletionResponse.Choice.builder()
                .index(0)
                .delta(deltaObj)
                .build();

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .id("greatwall-" + UUID.randomUUID())
                .object("chat.completion.chunk")
                .created(System.currentTimeMillis() / 1000)
                .model(dataNode.path("model").asText("greatwall"))
                .choices(java.util.Collections.singletonList(choice))
                .build();
        
        return Flux.just(response);
    }

    /**
     * 解析完整内容
     */
    private Flux<ChatCompletionResponse> parseCompleteContent(JsonNode dataNode) {
        String output = dataNode.path("data").path("output").asText("");
        
        if (output.isEmpty()) {
            return Flux.empty();
        }

        log.debug("📝 长城大模型完整输出: {}", output);

        // 转换为标准化响应（完成标记）
        ChatCompletionResponse.Choice choice = ChatCompletionResponse.Choice.builder()
                .index(0)
                .finishReason("stop")
                .build();

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .id("greatwall-" + UUID.randomUUID())
                .object("chat.completion")
                .created(System.currentTimeMillis() / 1000)
                .model(dataNode.path("model").asText("greatwall"))
                .choices(java.util.Collections.singletonList(choice))
                .build();
        
        return Flux.just(response);
    }

    /**
     * 获取提供者配置
     */
    private MultiModelProperties.ProviderConfig getProviderConfig() {
        return multiModelProperties.getProviders().get(PROVIDER_NAME);
    }

    /**
     * 获取模型配置
     */
    private MultiModelProperties.ModelConfig getModelConfig(String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = getProviderConfig();
        if (providerConfig == null || providerConfig.getModels() == null) {
            return null;
        }
        
        return providerConfig.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .findFirst()
                .orElse(null);
    }
}