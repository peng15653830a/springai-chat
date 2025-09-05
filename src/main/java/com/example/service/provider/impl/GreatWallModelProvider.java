package com.example.service.provider.impl;

import com.example.config.AiConfig;
import com.example.config.MultiModelProperties;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.GreatWallDirectService;
import com.example.service.MessageService;
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
    
    private final GreatWallDirectService greatWallDirectService;

    public GreatWallModelProvider(AiConfig.ChatClientFactory chatClientFactory,
                                  ObjectMapper objectMapper,
                                  MessageService messageService,
                                  MultiModelProperties multiModelProperties,
                                  GreatWallDirectService greatWallDirectService) {
        super(chatClientFactory, objectMapper, messageService, multiModelProperties);
        this.greatWallDirectService = greatWallDirectService;
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
     * 长城大模型由于API格式完全不兼容Spring AI标准，
     * 必须使用DirectService直接处理其非标准API
     */
    @Override
    protected Flux<SseEventResponse> callChatClientStream(ChatRequest request) {
        try {
            log.info("🏗️ 长城大模型：使用DirectService处理非标准API格式");
            
            // 检查是否为非标准API模型
            if (isNonStandardApiModel(request.getModelName())) {
                // 非标准API：使用DirectService处理长城大模型特殊格式
                return greatWallDirectService.executeDirectStreaming(
                    request.getFullPrompt(), 
                    request.getConversationId(),
                    request.getModelName()
                );
            } else {
                // 标准API：理论上长城大模型都是非标准的，但保留扩展性
                log.info("💭 长城大模型标准模式：使用Spring AI ChatClient框架");
                return super.callChatClientStream(request);
            }
            
        } catch (Exception e) {
            log.error("❌ 长城大模型调用失败", e);
            return Flux.just(SseEventResponse.error("长城大模型服务暂时不可用：" + e.getMessage()));
        }
    }

    /**
     * 检查是否为非标准API模型
     */
    private boolean isNonStandardApiModel(String modelName) {
        try {
            MultiModelProperties.ProviderConfig providerConfig = 
                multiModelProperties.getProviders().get(PROVIDER_NAME);
            
            if (providerConfig == null) {
                return false;
            }

            return providerConfig.getModels().stream()
                .filter(model -> modelName.equals(model.getName()))
                .findFirst()
                .map(MultiModelProperties.ModelConfig::isNonStandardApi)
                .orElse(false);
                
        } catch (Exception e) {
            log.warn("⚠️ 检查长城大模型API类型失败: {}", e.getMessage());
            return true; // 默认认为是非标准API
        }
    }
}