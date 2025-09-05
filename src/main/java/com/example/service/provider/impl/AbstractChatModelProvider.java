package com.example.service.provider.impl;

import com.example.config.AiConfig;
import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.service.MessageService;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.factory.ModelProviderFactory;
import com.example.service.provider.ModelProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 抽象ChatClient模型提供者基类
 * 提供通用的ChatClient集成逻辑，减少代码重复
 * 
 * @author xupeng
 */
@Slf4j
public abstract class AbstractChatModelProvider implements ModelProvider {

    protected final AiConfig.ChatClientFactory chatClientFactory;
    protected final ObjectMapper objectMapper;
    protected final MessageService messageService;
    protected final MultiModelProperties multiModelProperties;

    @Autowired
    private ModelProviderFactory modelProviderFactory;

    public AbstractChatModelProvider(AiConfig.ChatClientFactory chatClientFactory,
                                    ObjectMapper objectMapper,
                                    MessageService messageService,
                                    MultiModelProperties multiModelProperties) {
        this.chatClientFactory = chatClientFactory;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.multiModelProperties = multiModelProperties;
    }

    @PostConstruct
    public void init() {
        // 自动注册到工厂
        modelProviderFactory.registerProvider(this);
        log.info("{}模型提供者已注册: {}", getDisplayName(), getProviderName());
    }

    @Override
    public List<ModelInfo> getAvailableModels() {
        MultiModelProperties.ProviderConfig providerConfig = 
            multiModelProperties.getProviders().get(getProviderName());
        
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
        log.info("🚀 {}开始流式聊天，模型: {}, 推理模式: {}", 
                getDisplayName(), request.getModelName(), request.isDeepThinking());

        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder thinkingBuilder = new StringBuilder();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("AI正在思考中...")),
            
            // 2. 使用ChatClient进行流式聊天
            callChatClientStream(request)
                .doOnNext(event -> {
                    // 收集内容用于保存
                    if ("chunk".equals(event.getType()) && event.getData() != null) {
                        // 修复：正确提取chunk数据
                        if (event.getData() instanceof SseEventResponse.ChunkData) {
                            contentBuilder.append(((SseEventResponse.ChunkData) event.getData()).getContent());
                        } else if (event.getData() instanceof String) {
                            contentBuilder.append((String) event.getData());
                        }
                    } else if ("thinking".equals(event.getType()) && event.getData() != null) {
                        // 修复：正确提取thinking数据
                        if (event.getData() instanceof SseEventResponse.ChunkData) {
                            thinkingBuilder.append(((SseEventResponse.ChunkData) event.getData()).getContent());
                        } else if (event.getData() instanceof String) {
                            thinkingBuilder.append((String) event.getData());
                        }
                    }
                })
                .concatWith(
                    // 3. 保存消息并发送结束事件
                    saveMessageAndGenerateEndEvent(request.getConversationId(), 
                        contentBuilder.toString(),
                        thinkingBuilder.length() > 0 ? thinkingBuilder.toString() : null)
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
     * 使用ChatClient进行流式聊天
     */
    protected Flux<SseEventResponse> callChatClientStream(ChatRequest request) {
        try {
            // 获取ChatClient实例
            ChatClient chatClient = chatClientFactory.getChatClient(getProviderName(), request.getModelName());
            
            // 构建Prompt
            UserMessage userMessage = new UserMessage(request.getFullPrompt());
            Prompt prompt = new Prompt(List.of(userMessage));

            log.info("🚀 使用ChatClient开始流式聊天，模型: {}", request.getModelName());
            
            // 使用ChatClient的流式API
            return chatClient.prompt(prompt)
                .stream()
                .chatResponse()
                .mapNotNull(chatResponse -> {
                    // 添加空值检查
                    if (chatResponse != null && 
                        chatResponse.getResult() != null && 
                        chatResponse.getResult().getOutput() != null) {
                        String content = chatResponse.getResult().getOutput().getText();
                        if (content != null && !content.isEmpty()) {
                            log.debug("📝 接收到内容: {}", content);
                            return SseEventResponse.chunk(content);
                        }
                    }
                    return SseEventResponse.chunk("");
                })
                .filter(Objects::nonNull)
                .onErrorResume(error -> {
                    log.error("❌ ChatClient调用失败", error);
                    return Flux.just(SseEventResponse.error("AI服务暂时不可用：" + error.getMessage()));
                });
                
        } catch (Exception e) {
            log.error("❌ 创建ChatClient失败", e);
            return Flux.just(SseEventResponse.error("AI服务配置错误：" + e.getMessage()));
        }
    }

    /**
     * 保存消息并生成结束事件
     */
    protected Mono<SseEventResponse> saveMessageAndGenerateEndEvent(Long conversationId, 
                                                                   String content, 
                                                                   String thinking) {
        log.info("💾 准备保存AI响应，会话ID: {}, 内容长度: {}, 推理长度: {}", 
            conversationId, content.length(), thinking != null ? thinking.length() : 0);
        
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
     * 获取API密钥
     */
    protected String getApiKey() {
        return multiModelProperties.getApiKey(getProviderName());
    }

    /**
     * 获取基础URL
     */
    protected String getBaseUrl() {
        MultiModelProperties.ProviderConfig config = 
            multiModelProperties.getProviders().get(getProviderName());
        return config != null ? config.getBaseUrl() : "";
    }

    /**
     * 获取默认温度参数
     */
    protected double getDefaultTemperature(String modelName) {
        return getModelConfig(modelName)
                .map(MultiModelProperties.ModelConfig::getTemperature)
                .map(temp -> temp.doubleValue())
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

    /**
     * 获取默认推理预算
     */
    protected int getDefaultThinkingBudget() {
        return multiModelProperties.getDefaults().getThinkingBudget();
    }
}