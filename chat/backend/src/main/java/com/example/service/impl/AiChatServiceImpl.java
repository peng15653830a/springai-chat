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
import org.springframework.ai.chat.memory.ChatMemory;
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
    private final MessageToolResultService messageToolResultService;

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
            // 搜索相关的SSE事件流
            searchEventFlux,
            Flux.concat(
                // 准备阶段：处理输入和上下文
                prepareContext(request),
                // 执行阶段：与AI模型交互（Spring AI自动处理Tool Calling）
                processChat(request),
                // 完成阶段：保存结果
                finishChat(request)
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
            // 生成标题（异步）
            generateTitleAsync(request)
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
                    return streamFromAi( modelSelection, request, realMessageId);
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
     * 生成标题（异步执行）
     */
    private Flux<SseEventResponse> generateTitleAsync(StreamChatRequest request) {
        // 异步生成标题，不阻塞主流程
        conversationService.generateTitleIfNeededAsync(request.getConversationId(), request.getMessage())
            .subscribe();
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
    private Flux<SseEventResponse> streamFromAi( ModelSelector.ModelSelection modelSelection,
                                               StreamChatRequest request, Long userMessageId) {
        log.info("🚀 使用{}提供者，模型: {}, 深度思考: {}, userMessageId: {}",
            modelSelection.providerName(), modelSelection.modelName(), request.isDeepThinking(), userMessageId);

        Long conversationId = request.getConversationId();
        String conversationIdStr = conversationId.toString();
        final StringBuilder contentBuffer = new StringBuilder();

        // 先创建一个占位的助手消息以便在工具调用期即可记录到具体messageId
        return Mono.fromCallable(() -> {
                com.example.entity.Message draft = messageService.saveMessage(
                    com.example.dto.request.MessageSaveRequest.builder()
                        .conversationId(conversationId)
                        .role(com.example.constant.AiChatConstants.ROLE_ASSISTANT)
                        .content("[draft]")
                        .build()
                );
                log.info("📝 已创建占位助手消息，messageId: {}", draft.getId());
                return draft.getId();
            })
            .flatMapMany(assistantMessageId -> {
                java.util.concurrent.atomic.AtomicBoolean updated = new java.util.concurrent.atomic.AtomicBoolean(false);
                return Flux.concat(
                    // 1. 发送开始事件
                    Mono.just(SseEventResponse.start("AI正在思考中...")),

                    // 2. 流式调用模型（向工具传递conversationId与messageId）
                    buildPrompt(request)
                        .flatMapMany(prompt ->
                            getChatClientForModel(modelSelection)
                                .prompt()
                                .user(prompt)
                                .advisors(advisorSpec -> advisorSpec
                                    .param(ChatMemory.CONVERSATION_ID, conversationIdStr))
                                .toolContext(java.util.Map.of(
                                    "conversationId", conversationId,
                                    "messageId", assistantMessageId
                                ))
                                .stream()
                                .chatResponse()
                                .mapNotNull(chatResponse -> {
                                    var result = chatResponse.getResult();
                                    if (result != null && result.getOutput() != null) {
                                        String content = result.getOutput().getText();
                                        if (content != null && !content.trim().isEmpty()) {
                                            if (log.isDebugEnabled()) {
                                                String escaped = content.replace("\n", "\\n");
                                                log.debug("📦 Chunk(escaped) preview: {}", escaped.length() > 200 ? escaped.substring(0, 200) + "..." : escaped);
                                            }
                                            contentBuffer.append(content);
                                            return SseEventResponse.chunk(content);
                                        }
                                    }
                                    return null;
                                })
                                .filter(Objects::nonNull)
                        ),

                    // 3. 结束：更新占位消息的最终内容，并返回messageId
                    Mono.fromCallable(() -> {
                        String finalContent = contentBuffer.toString();
                        try {
                            messageService.updateMessageContent(assistantMessageId, finalContent, null);
                            log.info("✅ 助手消息内容已更新，messageId: {}，长度: {}", assistantMessageId, finalContent.length());
                            updated.set(true);
                        } catch (Exception e) {
                            log.warn("更新助手消息内容失败，messageId: {}，错误: {}", assistantMessageId, e.getMessage());
                            throw e;
                        }
                        return SseEventResponse.end(assistantMessageId);
                    })
                )
                // 将超时放到内部链路，便于统一清理占位消息
                .timeout(streamingProperties.getResponseTimeout())
                .onErrorResume(ex -> {
                    if (!updated.get()) {
                        try {
                            // 先清理该消息的工具调用记录，再删消息
                            try {
                                messageToolResultService.deleteMessageToolResults(assistantMessageId);
                            } catch (Exception ignore) {
                                // 忽略工具结果清理的异常，继续清理消息
                            }
                            messageService.deleteMessage(assistantMessageId);
                            log.info("🧹 已清理失败对话产生的占位消息及其相关工具记录，messageId: {}", assistantMessageId);
                        } catch (Exception cleanEx) {
                            log.warn("清理占位消息失败，messageId: {}，错误: {}", assistantMessageId, cleanEx.getMessage());
                        }
                    }
                    return errorHandler.handleChatError(ex);
                });
            })
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
