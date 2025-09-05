package com.example.service.provider.impl;

import com.example.config.AiConfig;
import com.example.config.MultiModelProperties;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.MessageService;
import com.example.service.ModelScopeDirectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 通义千问模型提供者实现
 * 支持推理过程解析的ChatClient实现
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class QwenModelProvider extends AbstractChatModelProvider {

    private static final String PROVIDER_NAME = "qwen";
    private static final String DISPLAY_NAME = "通义千问";
    
    private final ModelScopeDirectService modelScopeDirectService;

    public QwenModelProvider(AiConfig.ChatClientFactory chatClientFactory,
                            ObjectMapper objectMapper,
                            MessageService messageService,
                            MultiModelProperties multiModelProperties,
                            ModelScopeDirectService modelScopeDirectService) {
        super(chatClientFactory, objectMapper, messageService, multiModelProperties);
        this.modelScopeDirectService = modelScopeDirectService;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
    
    /**
     * QWen模型的智能路由策略：
     * 1. 推理模式：使用DirectService直接处理ModelScope API的reasoning_content
     * 2. 普通模式：使用Spring AI ChatClient保持优雅性
     * 
     * 这种混合模式既保证了推理功能的完整性，又保持了Spring AI框架的优雅性
     */
    @Override
    protected Flux<SseEventResponse> callChatClientStream(ChatRequest request) {
        try {
            // 智能路由：根据是否需要推理功能选择实现方式
            if (supportsThinking(request.getModelName()) && request.isDeepThinking()) {
                log.info("🧠 QWen推理模式：直接调用ModelScope API以支持reasoning_content");
                
                // 推理模式：使用DirectService获得完整的reasoning_content支持
                return modelScopeDirectService.executeDirectStreaming(
                    request.getFullPrompt(), 
                    request.getConversationId(), 
                    true
                );
            } else {
                // 普通模式：使用Spring AI ChatClient，保持框架一致性
                log.info("💭 QWen普通模式：使用Spring AI ChatClient框架");
                return super.callChatClientStream(request);
            }
            
        } catch (Exception e) {
            log.error("❌ QWen智能路由失败", e);
            return Flux.just(SseEventResponse.error("QWen服务暂时不可用：" + e.getMessage()));
        }
    }
}