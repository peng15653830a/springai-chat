package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.dto.response.SearchResult;
import com.example.dto.request.MessageSaveRequest;
import com.example.manager.ChatClientManager;
import com.example.service.*;
import com.example.tool.WebSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import com.example.handler.ChatErrorHandler;
import com.example.strategy.model.ModelSelector;
import com.example.strategy.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

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
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ChatClientManager chatClientManager;
    private final ModelSelector modelSelector;
    private final PromptBuilder promptBuilder;
    private final ChatErrorHandler errorHandler;
    private final SseEventPublisher sseEventPublisher;

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

        // 设置当前会话ID到SseEventPublisher，确保WebSearchTool能发送SSE事件
        sseEventPublisher.setCurrentConversationId(request.getConversationId());
        log.debug("🔧 设置会话ID到SseEventPublisher: {}", request.getConversationId());

        // 获取SseEventPublisher的事件流，用于合并搜索事件
        var searchEventFlux = sseEventPublisher.registerConversationFlux(request.getConversationId());

        // 合并搜索事件流和主聊天流
        return Flux.merge(
            searchEventFlux,  // 搜索相关的SSE事件流
            Flux.concat(
                prepareContext(request),  // 准备阶段：处理输入和上下文
                processChat(request),     // 执行阶段：与AI模型交互（Spring AI自动处理Tool Calling）
                finishChat(request)       // 完成阶段：保存结果
            )
        )
        .doFinally(signalType -> {
            // 清理SseEventPublisher的当前会话ID和事件发射器
            sseEventPublisher.clearCurrentConversationId();
            sseEventPublisher.removeConversation(request.getConversationId());
            log.debug("🧹 清理SseEventPublisher会话ID: {}", request.getConversationId());
        })
        .onErrorResume(errorHandler::handleChatError);
    }

    // ========================= 第一层：主流程控制 =========================

    /**
     * 准备阶段：处理输入和上下文
     */
    private Flux<SseEventResponse> prepareContext(StreamChatRequest request) {
        log.debug("开始准备聊天上下文，会话ID: {}", request.getConversationId());
        
        return Flux.concat(
            generateTitleAsync(request),        // 生成标题（异步）
            enrichWithSearch(request)           // 搜索增强（可选）
        );
    }

    /**
     * 执行阶段：与AI模型交互
     */
    private Flux<SseEventResponse> processChat(StreamChatRequest request) {
        log.debug("开始处理AI聊天，会话ID: {}", request.getConversationId());

        String userMessage = request.getMessage();
        return Flux.defer(() -> {
            ModelSelector.ModelSelection modelSelection = selectModel(request);

            // 先保存用户消息获取真实messageId，用于工具调用关联
            return messageService.saveUserMessageAsync(request.getConversationId(), userMessage)
                .flatMapMany(savedUserMessage -> {
                    Long realMessageId = savedUserMessage.getId();
                    log.info("✅ 已保存用户消息，获得真实messageId: {}", realMessageId);
                    return streamFromAI(userMessage, modelSelection, request, realMessageId);
                });
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
     * 搜索增强（Spring AI自动处理Tool Calling）
     */
    private Flux<SseEventResponse> enrichWithSearch(StreamChatRequest request) {
        // Spring AI会根据需要自动调用WebSearchTool
        return Flux.empty();
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
     * 从AI模型流式获取响应 - 使用Spring AI 1.0标准ToolContext传递消息ID
     */
    private Flux<SseEventResponse> streamFromAI(String userMessage, ModelSelector.ModelSelection modelSelection,
                                               StreamChatRequest request, Long messageId) {
        log.info("🚀 使用{}提供者，模型: {}, 深度思考: {}, messageId: {}",
            modelSelection.providerName(), modelSelection.modelName(), request.isDeepThinking(), messageId);

        Long conversationId = request.getConversationId();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("AI正在思考中...")),

            // 2. 使用Spring AI ChatClient流式调用（自动处理Tool Calling和Advisor消息保存）
            getChatClientForModel(modelSelection)
                .prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                    // 直接传递会话ID字符串，不用参数键
                    .param(conversationId.toString()))
                // 使用Spring AI 1.0标准ToolContext传递上下文给工具
                .toolContext(java.util.Map.of(
                    "conversationId", conversationId,
                    "messageId", messageId  // 传递真实messageId用于工具调用关联
                ))
                .stream()
                .chatResponse()
                .mapNotNull(chatResponse -> {
                    // 提取响应内容并创建SSE事件
                    var result = chatResponse.getResult();
                    if (result != null && result.getOutput() != null) {
                        // 使用getText()方法获取纯文本内容
                        String content = result.getOutput().getText();
                        return content != null && !content.trim().isEmpty() ?
                            SseEventResponse.chunk(content) : null;
                    }
                    return null;
                })
                .filter(Objects::nonNull),

            // 3. 发送结束事件（消息保存由Advisor自动处理）
            Mono.just(SseEventResponse.end(null))
        )
        .timeout(streamingProperties.getResponseTimeout())
        .onErrorResume(errorHandler::handleChatError)
        .doFinally(signalType -> {
            log.debug("🧹 聊天请求处理完成");
        });
    }
    
    /**
     * 获取指定模型的ChatClient
     * 现在所有模型的ChatClient都配置了WebSearchTool和MessageHistoryAdvisor
     */
    private ChatClient getChatClientForModel(ModelSelector.ModelSelection modelSelection) {
        // 使用ChatClientManager，每个ChatClient都已配置WebSearchTool和MessageHistoryAdvisor
        return chatClientManager.getChatClient(modelSelection.providerName());
    }
}
