package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.entity.Message;
import com.example.service.AiChatService;
import com.example.service.ConversationService;
import com.example.service.MessageService;
import com.example.service.SearchService;
import com.example.service.chat.ChatErrorHandler;
import com.example.service.chat.ModelSelector;
import com.example.service.chat.PromptBuilder;
import com.example.service.provider.ModelProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 重构后的AI聊天服务实现类
 * 职责简化为流程协调，具体功能委托给专门的组件
 * 
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ChatStreamingProperties streamingProperties;
    private final SearchService searchService;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ModelSelector modelSelector;
    private final PromptBuilder promptBuilder;
    private final ChatErrorHandler errorHandler;

    @Override
    public Flux<SseEventResponse> streamChat(Long conversationId, String userMessage, 
                                           boolean searchEnabled, boolean deepThinking) {
        return streamChatWithModel(conversationId, userMessage, searchEnabled, deepThinking, 
                                  null, null, null);
    }

    @Override
    public Flux<SseEventResponse> streamChatWithModel(Long conversationId, String userMessage, 
                                                     boolean searchEnabled, boolean deepThinking,
                                                     Long userId, String providerName, String modelName) {
        log.info("开始响应式流式聊天，会话ID: {}, 消息长度: {}, 搜索开启: {}, 深度思考: {}, 用户ID: {}, 指定模型: {}-{}", 
                conversationId, userMessage.length(), searchEnabled, deepThinking, userId, providerName, modelName);

        return Flux.concat(
            // 1. 保存用户消息并生成标题
            saveUserMessageAndGenerateTitle(conversationId, userMessage),
            
            // 2. 执行搜索（如果启用）
            performSearchStep(userMessage, searchEnabled),
            
            // 3. 构建提示并执行流式聊天
            buildPromptAndStreamChatWithModel(conversationId, userMessage, searchEnabled, 
                                            deepThinking, userId, providerName, modelName)
        )
        .onErrorResume(errorHandler::handleChatError);
    }

    @Override
    public Flux<SseEventResponse> executeStreamingChat(String prompt, Long conversationId, boolean deepThinking) {
        return executeStreamingChatWithModel(prompt, conversationId, deepThinking, null, null);
    }

    @Override
    public Flux<SseEventResponse> handleChatError(Throwable error) {
        return errorHandler.handleChatError(error);
    }

    /**
     * 使用指定模型执行流式聊天
     */
    private Flux<SseEventResponse> executeStreamingChatWithModel(String prompt, Long conversationId, 
                                                               boolean deepThinking, 
                                                               String providerName, String modelName) {
        log.debug("开始执行流式AI聊天，提示长度: {}, 会话ID: {}, 深度思考: {}, 模型: {}-{}", 
                 prompt.length(), conversationId, deepThinking, providerName, modelName);

        try {
            // 选择模型提供者
            ModelProvider provider = modelSelector.getModelProvider(providerName);
            String actualModelName = modelSelector.getActualModelName(provider, modelName);
            
            // 构建聊天请求
            ChatRequest request = ChatRequest.builder()
                    .conversationId(conversationId)
                    .modelName(actualModelName)
                    .fullPrompt(prompt)
                    .deepThinking(deepThinking)
                    .build();

            log.info("🚀 使用{}提供者，模型: {}, 深度思考: {}", provider.getDisplayName(), actualModelName, deepThinking);
            
            return provider.streamChat(request)
                    .timeout(streamingProperties.getResponseTimeout())
                    .onErrorResume(errorHandler::handleChatError);
                    
        } catch (Exception e) {
            log.error("获取模型提供者失败", e);
            return errorHandler.handleChatError(e);
        }
    }

    /**
     * 保存用户消息并生成标题
     */
    private Flux<SseEventResponse> saveUserMessageAndGenerateTitle(Long conversationId, String userMessage) {
        return messageService.saveUserMessageAsync(conversationId, userMessage)
                .doOnNext(message -> {
                    // 异步生成标题，不阻塞主流程
                    conversationService.generateTitleIfNeededAsync(conversationId, userMessage)
                            .subscribe();
                })
                .then(Mono.<SseEventResponse>empty())
                .flux();
    }

    /**
     * 执行搜索步骤
     */
    private Flux<SseEventResponse> performSearchStep(String userMessage, boolean searchEnabled) {
        return searchService.performSearchWithEvents(userMessage, searchEnabled)
                .flatMapMany(SearchService.SearchContextResult::getSearchEvents);
    }

    /**
     * 构建提示并执行流式聊天
     */
    private Flux<SseEventResponse> buildPromptAndStreamChatWithModel(Long conversationId, String userMessage, 
                                                                   boolean searchEnabled, boolean deepThinking,
                                                                   Long userId, String providerName, String modelName) {
        return promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled)
                .flatMapMany(prompt -> {
                    // 如果有用户ID，使用用户偏好选择模型
                    if (userId != null) {
                        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);
                        return executeStreamingChatWithModel(prompt, conversationId, deepThinking, 
                                                           selection.provider().getProviderName(), selection.modelName());
                    } else {
                        return executeStreamingChatWithModel(prompt, conversationId, deepThinking, providerName, modelName);
                    }
                });
    }
}