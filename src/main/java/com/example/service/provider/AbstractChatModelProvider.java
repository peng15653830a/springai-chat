package com.example.service.provider;

import com.example.config.EnhancedAiConfig;
import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ChatModel提供者抽象基类
 * 使用Spring AI的ChatClient实现统一的聊天功能
 * 
 * @author xupeng
 */
@Slf4j
public abstract class AbstractChatModelProvider implements ModelProvider {

    protected final EnhancedAiConfig.EnhancedChatClientFactory chatClientFactory;
    protected final ObjectMapper objectMapper;
    protected final MessageService messageService;
    protected final MultiModelProperties multiModelProperties;

    public AbstractChatModelProvider(EnhancedAiConfig.EnhancedChatClientFactory chatClientFactory,
                                    ObjectMapper objectMapper,
                                    MessageService messageService,
                                    MultiModelProperties multiModelProperties) {
        this.chatClientFactory = chatClientFactory;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.multiModelProperties = multiModelProperties;
    }

    @Override
    public List<ModelInfo> getAvailableModels() {
        Map<String, MultiModelProperties.ProviderConfig> providers = multiModelProperties.getProviders();
        if (providers == null) {
            return Collections.emptyList();
        }
        
        MultiModelProperties.ProviderConfig providerConfig = providers.get(getProviderName());
        
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
        log.info("🚀 {}开始流式聊天，模型: {}", getDisplayName(), request.getModelName());

        StringBuilder contentBuilder = new StringBuilder();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("AI正在思考中...")),
            
            // 2. 调用AI模型并处理响应
            callModelWithChatClient(request)
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
        return multiModelProperties.isProviderAvailable(getProviderName());
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
     * 使用ChatClient调用AI模型
     */
    protected Flux<SseEventResponse> callModelWithChatClient(ChatRequest request) {
        try {
            // 获取对应的ChatClient
            ChatClient chatClient = chatClientFactory.getChatClient(getProviderName(), request.getModelName());
            
            // 构建聊天请求并流式执行
            return chatClient.prompt()
                .user(request.getFullPrompt())
                .stream()
                .content()
                .map(content -> {
                    log.debug("💬 收到内容片段，长度: {}", content.length());
                    return SseEventResponse.chunk(content);
                })
                .onErrorResume(error -> {
                    log.error("❌ {} API调用失败", getDisplayName(), error);
                    return Flux.just(SseEventResponse.error("AI服务暂时不可用：" + error.getMessage()));
                });
                
        } catch (Exception e) {
            log.error("❌ 创建ChatClient失败", e);
            return Flux.just(SseEventResponse.error("初始化AI服务失败：" + e.getMessage()));
        }
    }

    /**
     * 保存消息并生成结束事件
     */
    protected Mono<SseEventResponse> saveMessageAndGenerateEndEvent(Long conversationId, 
                                                                   String content, 
                                                                   String thinking) {
        log.info("💾 准备保存AI响应，会话ID: {}, 内容长度: {}", 
            conversationId, content != null ? content.length() : 0);
        
        if (content == null || content.trim().isEmpty()) {
            log.warn("⚠️ AI响应内容为空，会话ID: {}", conversationId);
            return Mono.just(SseEventResponse.end(null));
        }
        
        return messageService.saveAiMessageAsync(conversationId, content.trim(), thinking)
            .onErrorReturn(SseEventResponse.error("保存AI响应失败"));
    }

    /**
     * 将配置转换为ModelInfo
     */
    protected ModelInfo convertToModelInfo(MultiModelProperties.ModelConfig config) {
        ModelInfo info = new ModelInfo();
        info.setId((long) config.getName().hashCode());
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
    protected Optional<MultiModelProperties.ModelConfig> getModelConfig(String modelName) {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(getProviderName());
        
        if (providerConfig == null) {
            return Optional.empty();
        }

        return providerConfig.getModels().stream()
                .filter(model -> model.getName().equals(modelName))
                .findFirst();
    }

    /**
     * 获取默认温度参数
     */
    protected double getDefaultTemperature(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::getTemperature)
                .map(BigDecimal::doubleValue)
                .orElse(multiModelProperties.getDefaults().getTemperature().doubleValue());
    }

    /**
     * 获取默认最大token数
     */
    protected int getDefaultMaxTokens(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::getMaxTokens)
                .orElse(multiModelProperties.getDefaults().getMaxTokens());
    }
}