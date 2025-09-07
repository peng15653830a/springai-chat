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
 * 长城大模型提供者实现
 * 专门处理长城大模型的非标准API格式
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class GreatWallModelProvider extends AbstractChatModelProvider {

    private static final String PROVIDER_NAME = "greatwall";
    private static final String DISPLAY_NAME = "长城大模型";
    
    public GreatWallModelProvider(EnhancedAiConfig.EnhancedChatClientFactory chatClientFactory,
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