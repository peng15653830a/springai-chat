package com.example.service.provider.impl;

import com.example.config.EnhancedAiConfig;
import com.example.config.MultiModelProperties;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.MessageService;
import com.example.service.provider.AbstractChatModelProvider;
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
    
    public QwenModelProvider(EnhancedAiConfig.EnhancedChatClientFactory chatClientFactory,
                            ObjectMapper objectMapper,
                            MessageService messageService,
                            MultiModelProperties multiModelProperties) {
        super(chatClientFactory, objectMapper, messageService, multiModelProperties);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
    
}