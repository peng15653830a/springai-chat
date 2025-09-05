package com.example.service;

import com.example.config.GreatWallProperties;
import com.example.config.MultiModelProperties;
import com.example.dto.response.SseEventResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 长城大模型直接API服务
 * 处理长城大模型的非标准API格式和SSE响应
 * 
 * @author xupeng
 */
@Slf4j
@Service
public class GreatWallDirectService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final MultiModelProperties multiModelProperties;
    private final GreatWallProperties greatWallProperties;

    public GreatWallDirectService(WebClient.Builder webClientBuilder,
                                  ObjectMapper objectMapper,
                                  MessageService messageService,
                                  MultiModelProperties multiModelProperties,
                                  GreatWallProperties greatWallProperties) {
        // 根据配置决定是否跳过SSL验证
        this.webClient = createWebClient(webClientBuilder, greatWallProperties);
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.multiModelProperties = multiModelProperties;
        this.greatWallProperties = greatWallProperties;
    }

    /**
     * 根据配置创建WebClient（可选择跳过SSL验证）
     */
    private WebClient createWebClient(WebClient.Builder webClientBuilder, GreatWallProperties properties) {
        if (properties.getSsl().isSkipVerification()) {
            log.info("🔓 长城大模型：跳过SSL证书验证（开发环境）");
            return createInsecureWebClient(webClientBuilder);
        } else {
            log.info("🔒 长城大模型：使用标准SSL验证");
            return webClientBuilder.build();
        }
    }

    /**
     * 创建跳过SSL验证的WebClient（仅用于开发环境）
     */
    private WebClient createInsecureWebClient(WebClient.Builder webClientBuilder) {
        try {
            HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> {
                    try {
                        sslSpec.sslContext(SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                            .build());
                    } catch (SSLException e) {
                        log.warn("⚠️ 长城大模型SSL配置失败，使用默认配置: {}", e.getMessage());
                    }
                });

            return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        } catch (Exception e) {
            log.warn("⚠️ 长城大模型WebClient配置失败，使用默认配置: {}", e.getMessage());
            return webClientBuilder.build();
        }
    }

    /**
     * 直接调用长城大模型API，处理非标准格式
     */
    public Flux<SseEventResponse> executeDirectStreaming(String prompt, Long conversationId, String modelName) {
        log.info("🏗️ 开始直接调用长城大模型API，模型: {}", modelName);

        StringBuilder contentBuilder = new StringBuilder();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("长城大模型正在思考中...")),
            
            // 2. 调用长城大模型API并处理原始响应
            callGreatWallApi(prompt, modelName)
                .doOnNext(event -> {
                    // 收集内容用于保存
                    if ("chunk".equals(event.getType()) && event.getData() != null) {
                        // 修复：正确提取chunk数据
                        if (event.getData() instanceof SseEventResponse.ChunkData) {
                            contentBuilder.append(((SseEventResponse.ChunkData) event.getData()).getContent());
                        } else if (event.getData() instanceof String) {
                            contentBuilder.append((String) event.getData());
                        }
                    }
                })
                .concatWith(
                    // 3. 保存消息并发送结束事件
                    saveMessageAndGenerateEndEvent(conversationId, contentBuilder.toString())
                )
        );
    }

    /**
     * 调用长城大模型API并解析非标准SSE响应
     */
    private Flux<SseEventResponse> callGreatWallApi(String prompt, String modelName) {
        Map<String, Object> requestBody = buildGreatWallRequestBody(prompt, modelName);
        
        MultiModelProperties.ProviderConfig greatWallConfig = multiModelProperties.getProviders().get("greatwall");
        String apiKey = multiModelProperties.getApiKey("greatwall");
        
        // 获取模型配置中的API运行ID
        MultiModelProperties.ModelConfig modelConfig = getModelConfig(modelName);
        String apiUrl = buildGreatWallApiUrl(greatWallConfig.getBaseUrl(), modelConfig.getApiRunId());
        
        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(line -> log.debug("🔍 收到长城大模型原始SSE行: '{}'", line))
                .filter(this::isValidSseLine)
                .flatMap(this::parseGreatWallSseLine)
                .onErrorResume(error -> {
                    log.error("❌ 长城大模型API调用失败", error);
                    return Flux.just(SseEventResponse.error("长城大模型服务暂时不可用：" + error.getMessage()));
                });
    }

    /**
     * 构建长城大模型请求体
     */
    private Map<String, Object> buildGreatWallRequestBody(String prompt, String modelName) {
        MultiModelProperties.ModelConfig modelConfig = getModelConfig(modelName);
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // 长城大模型特有字段
        requestBody.put("tpuid", modelConfig.getTpuidPrefix() + System.currentTimeMillis() % 1000);
        requestBody.put("doc_list", new ArrayList<>());
        requestBody.put("image_url", "");
        requestBody.put("query", prompt);
        requestBody.put("session_id", "");
        requestBody.put("stream", true);
        
        log.debug("🔧 构建长城大模型请求体完成，用户ID: {}", requestBody.get("tpuid"));
        return requestBody;
    }

    /**
     * 构建长城大模型API URL
     */
    private String buildGreatWallApiUrl(String baseUrl, String apiRunId) {
        return baseUrl + "/aicoapi/gateway/v2/chatbot/api_run/" + apiRunId;
    }

    /**
     * 获取模型配置
     */
    private MultiModelProperties.ModelConfig getModelConfig(String modelName) {
        MultiModelProperties.ProviderConfig greatWallConfig = multiModelProperties.getProviders().get("greatwall");
        return greatWallConfig.getModels().stream()
                .filter(model -> modelName.equals(model.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到长城大模型配置: " + modelName));
    }

    /**
     * 检查是否为有效的SSE行
     */
    private boolean isValidSseLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        
        // 长城大模型使用完整的JSON格式，而不是标准的event:和data:格式
        // 检查是否为有效的JSON对象
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return true;
        }
        
        // 保留原有的检查逻辑
        boolean isValid = trimmed.startsWith("event:") || trimmed.startsWith("data:");
        if (!isValid) {
            log.debug("🚫 跳过无效行: '{}'", line);
        }
        return isValid;
    }

    /**
     * 解析长城大模型SSE行
     */
    private Flux<SseEventResponse> parseGreatWallSseLine(String line) {
        try {
            String trimmed = line.trim();
            
            // 处理完整的JSON格式（长城大模型的格式）
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return parseGreatWallJsonLine(trimmed);
            }
            
            // 处理标准的event:和data:格式
            if (trimmed.startsWith("event:")) {
                // 处理事件类型行
                String eventType = trimmed.substring(6).trim();
                log.debug("📨 收到事件类型: {}", eventType);
                return Flux.empty(); // 事件类型行不产生输出
            }
            
            if (trimmed.startsWith("data:")) {
                // 处理数据行
                String jsonData = trimmed.substring(5).trim();
                return parseGreatWallJsonData(jsonData);
            }
            
            return Flux.empty();
            
        } catch (Exception e) {
            log.error("❌ 解析长城大模型SSE行失败: {}", line, e);
            // 返回空的Flux而不是null
            return Flux.empty();
        }
    }

    /**
     * 解析长城大模型的完整JSON行
     */
    private Flux<SseEventResponse> parseGreatWallJsonLine(String jsonLine) {
        try {
            log.debug("🔍 解析长城大模型完整JSON行: {}", jsonLine);
            
            JsonNode dataNode = objectMapper.readTree(jsonLine);
            String event = dataNode.path("event").asText("");
            
            List<SseEventResponse> events = new ArrayList<>();
            
            switch (event) {
                case "message_start":
                    log.info("🚀 长城大模型开始响应");
                    // 添加start事件通知前端
                    events.add(SseEventResponse.start("长城大模型正在思考中..."));
                    break;
                    
                case "llm_chunk":
                    // 提取流式内容
                    JsonNode choices = dataNode.path("data").path("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode delta = choices.get(0).path("delta");
                        String content = delta.path("content").asText("");
                        
                        if (!content.isEmpty()) {
                            // 使用正确的SseEventResponse格式
                            events.add(SseEventResponse.chunk(content));
                            log.debug("💬 提取到长城大模型内容片段，长度: {}", content.length());
                        }
                    }
                    break;
                    
                case "llm_finished":
                    log.info("🏁 长城大模型响应完成");
                    break;
                    
                case "message_finished":
                    // 获取完整输出
                    String output = dataNode.path("data").path("output").asText("");
                    if (!output.isEmpty()) {
                        log.info("📝 长城大模型完整输出长度: {}", output.length());
                        // 对于完整输出，我们也可以将其作为chunk事件发送
                        events.add(SseEventResponse.chunk(output));
                    }
                    break;
                    
                default:
                    log.debug("🔄 未处理的长城大模型事件类型: {}", event);
                    break;
            }
            
            return Flux.fromIterable(events);
            
        } catch (Exception e) {
            log.error("❌ 解析长城大模型JSON行失败: {}", jsonLine, e);
            // 返回空的Flux而不是null
            return Flux.empty();
        }
    }

    /**
     * 解析长城大模型JSON数据
     */
    private Flux<SseEventResponse> parseGreatWallJsonData(String jsonData) {
        try {
            log.debug("🔍 解析长城大模型JSON数据: {}", jsonData);
            
            JsonNode dataNode = objectMapper.readTree(jsonData);
            String event = dataNode.path("event").asText("");
            
            List<SseEventResponse> events = new ArrayList<>();
            
            switch (event) {
                case "message_start":
                    log.info("🚀 长城大模型开始响应");
                    // 添加start事件通知前端
                    events.add(SseEventResponse.start("长城大模型正在思考中..."));
                    break;
                    
                case "llm_chunk":
                    // 提取流式内容
                    JsonNode choices = dataNode.path("data").path("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode delta = choices.get(0).path("delta");
                        String content = delta.path("content").asText("");
                        
                        if (!content.isEmpty()) {
                            // 使用正确的SseEventResponse格式
                            events.add(SseEventResponse.chunk(content));
                            log.debug("💬 提取到长城大模型内容片段，长度: {}", content.length());
                        }
                    }
                    break;
                    
                case "llm_finished":
                    log.info("🏁 长城大模型响应完成");
                    break;
                    
                case "message_finished":
                    // 获取完整输出
                    String output = dataNode.path("data").path("output").asText("");
                    if (!output.isEmpty()) {
                        log.info("📝 长城大模型完整输出长度: {}", output.length());
                    }
                    break;
                    
                default:
                    log.debug("🔄 未处理的长城大模型事件类型: {}", event);
                    break;
            }
            
            return Flux.fromIterable(events);
            
        } catch (Exception e) {
            log.error("❌ 解析长城大模型JSON数据失败: {}", jsonData, e);
            // 返回空的Flux而不是null
            return Flux.empty();
        }
    }

    /**
     * 保存消息并生成结束事件
     */
    private Mono<SseEventResponse> saveMessageAndGenerateEndEvent(Long conversationId, String content) {
        log.info("💾 准备保存长城大模型响应，会话ID: {}, 内容长度: {}", 
            conversationId, content.length());
        
        if (content == null || content.trim().isEmpty()) {
            log.warn("⚠️ 长城大模型响应内容为空，会话ID: {}", conversationId);
            return Mono.just(SseEventResponse.end(null));
        }
        
        // 修复：正确处理消息保存并返回结束事件
        return messageService.saveAiMessageAsync(conversationId, content.trim(), null)
            .onErrorResume(error -> {
                log.error("❌ 保存长城大模型响应失败", error);
                return Mono.just(SseEventResponse.error("保存长城大模型响应失败"));
            })
            .map(message -> {
                log.info("✅ 长城大模型响应保存成功，消息ID: {}", conversationId);
                return SseEventResponse.end(conversationId);
            });
    }
}