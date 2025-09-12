package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.manager.ChatClientManager;
import com.example.service.*;
import com.example.handler.ChatErrorHandler;
import com.example.strategy.model.ModelSelector;
import com.example.strategy.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 重构后的AI聊天服务实现类
 * 按照同一抽象层次原则重新组织：准备→执行→完成
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
    private final ChatClientManager chatClientManager;
    private final ModelSelector modelSelector;
    private final PromptBuilder promptBuilder;
    private final ChatErrorHandler errorHandler;

    @Override
    public Flux<SseEventResponse> streamChat(StreamChatRequest request) {
        log.info("开始流式聊天，会话ID: {}, 消息长度: {}, 搜索开启: {}, 深度思考: {}, 用户ID: {}, 指定模型: {}-{}", 
                request.getConversationId(), 
                request.getMessage() != null ? request.getMessage().length() : 0, 
                request.isSearchEnabled(), 
                request.isDeepThinking(), 
                request.getUserId(), 
                request.getProvider(), 
                request.getModel());

        // 准备阶段：处理输入和上下文
        // 执行阶段：与AI模型交互
        // 完成阶段：保存结果
        return Flux.concat(
            prepareContext(request),
            processChat(request),
            finishChat(request)
        )
        .onErrorResume(errorHandler::handleChatError);
    }

    // ========================= 第一层：主流程控制 =========================

    /**
     * 准备阶段：处理输入和上下文
     */
    private Flux<SseEventResponse> prepareContext(StreamChatRequest request) {
        log.debug("开始准备聊天上下文，会话ID: {}", request.getConversationId());
        
        // 保存用户消息
        // 生成标题（异步）
        // 搜索增强（可选）
        return Flux.concat(
            saveUserMessage(request),
            generateTitleAsync(request),
            enrichWithSearch(request)
        );
    }

    /**
     * 执行阶段：与AI模型交互
     */
    private Flux<SseEventResponse> processChat(StreamChatRequest request) {
        log.debug("开始处理AI聊天，会话ID: {}", request.getConversationId());
        
        return buildPrompt(request)
            .flatMapMany(prompt -> {
                // 选择模型并执行流式聊天
                ModelSelector.ModelSelection modelSelection = selectModel(request);
                return streamFromAi(prompt, modelSelection, request);
            });
    }

    /**
     * 完成阶段：保存结果
     */
    private Flux<SseEventResponse> finishChat(StreamChatRequest request) {
        log.debug("完成聊天处理，会话ID: {}", request.getConversationId());
        
        // 在processChat阶段已经处理了响应保存，这里返回空流
        return Flux.empty();
    }

    // ========================= 第二层：各阶段具体实现 =========================

    /**
     * 保存用户消息
     */
    private Flux<SseEventResponse> saveUserMessage(StreamChatRequest request) {
        return messageService.saveUserMessageAsync(request.getConversationId(), request.getMessage())
            .then(Mono.<SseEventResponse>empty())
            .flux();
    }

    /**
     * 生成标题（异步执行）
     */
    private Flux<SseEventResponse> generateTitleAsync(StreamChatRequest request) {
        // 异步生成标题，不阻塞主流程
        conversationService.generateTitleIfNeededAsync(request.getConversationId(), request.getMessage())
            .subscribe();
        return Flux.empty();
    }

    /**
     * 搜索增强（可选）
     */
    private Flux<SseEventResponse> enrichWithSearch(StreamChatRequest request) {
        return searchService.performSearchWithEvents(request.getMessage(), request.isSearchEnabled())
            .flatMapMany(SearchService.SearchContextResult::getSearchEvents);
    }

    /**
     * 构建提示词
     */
    private Mono<String> buildPrompt(StreamChatRequest request) {
        return promptBuilder.buildPrompt(
            request.getConversationId(), 
            request.getMessage(), 
            request.isSearchEnabled()
        );
    }

    /**
     * 选择模型
     */
    private ModelSelector.ModelSelection selectModel(StreamChatRequest request) {
        if (request.getUserId() != null) {
            // 使用用户偏好选择模型
            return modelSelector.selectModelForUser(
                request.getUserId(), 
                request.getProvider(), 
                request.getModel()
            );
        } else {
            // 直接使用指定模型或默认模型
            String actualProviderName = modelSelector.getActualProviderName(request.getProvider());
            String actualModelName = modelSelector.getActualModelName(actualProviderName, request.getModel());
            return new ModelSelector.ModelSelection(actualProviderName, actualModelName);
        }
    }

    /**
     * 从AI模型流式获取响应
     */
    private Flux<SseEventResponse> streamFromAi(String prompt, ModelSelector.ModelSelection modelSelection, 
                                               StreamChatRequest request) {
        log.info("🚀 使用{}提供者，模型: {}, 深度思考: {}", 
            modelSelection.providerName(), modelSelection.modelName(), request.isDeepThinking());

        StringBuilder contentBuilder = new StringBuilder();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("AI正在思考中...")),
            
            // 2. 调用AI模型并处理响应
            callAiModel(prompt, modelSelection, request)
                .doOnNext(event -> {
                    // 收集内容用于保存
                    if (SseEventResponse.CHUNK_TYPE.equals(event.getType()) && event.getData() != null) {
                        contentBuilder.append(event.getData().toString());
                    }
                }),
            
            // 3. 保存消息并发送结束事件
            saveAiResponse(request.getConversationId(), contentBuilder.toString())
        )
        .timeout(streamingProperties.getResponseTimeout())
        .onErrorResume(errorHandler::handleChatError);
    }

    // ========================= 第三层：具体实现细节 =========================

    /**
     * 调用AI模型
     */
    private Flux<SseEventResponse> callAiModel(String prompt, ModelSelector.ModelSelection modelSelection, 
                                              StreamChatRequest request) {
        try {
            ChatClient chatClient = chatClientManager.getChatClient(modelSelection.providerName());
            
            return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .map(content -> {
                    log.debug("💬 收到内容片段，长度: {}", content.length());
                    return SseEventResponse.chunk(content);
                })
                .onErrorResume(error -> {
                    log.error("❌ {} API调用失败", modelSelection.providerName(), error);
                    return Flux.just(SseEventResponse.error("AI服务暂时不可用：" + error.getMessage()));
                });
                
        } catch (Exception e) {
            log.error("❌ 创建ChatClient失败", e);
            return Flux.just(SseEventResponse.error("初始化AI服务失败：" + e.getMessage()));
        }
    }

    /**
     * 保存AI响应
     */
    private Mono<SseEventResponse> saveAiResponse(Long conversationId, String content) {
        log.info("💾 准备保存AI响应，会话ID: {}, 内容长度: {}", 
            conversationId, content != null ? content.length() : 0);
        
        if (content == null || content.trim().isEmpty()) {
            log.warn("⚠️ AI响应内容为空，会话ID: {}", conversationId);
            return Mono.just(SseEventResponse.end(null));
        }
        
        return messageService.saveAiMessageAsync(conversationId, content.trim(), null)
            .onErrorReturn(SseEventResponse.error("保存AI响应失败"));
    }
}