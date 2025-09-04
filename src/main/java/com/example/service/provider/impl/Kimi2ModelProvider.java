package com.example.service.provider.impl;

import com.example.config.AiConfig;
import com.example.config.MultiModelProperties;
import com.example.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kimi2模型提供者实现
 * 重构为使用ChatClient，继承通用基类
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class Kimi2ModelProvider extends AbstractChatModelProvider {

    private static final String PROVIDER_NAME = "kimi2";
    private static final String DISPLAY_NAME = "Kimi2";

    public Kimi2ModelProvider(AiConfig.ChatClientFactory chatClientFactory,
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