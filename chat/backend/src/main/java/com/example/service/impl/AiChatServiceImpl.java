package com.example.service.impl;

import com.example.config.ChatStreamingProperties;
import com.example.dto.request.StreamChatRequest;
import com.example.dto.stream.ChatEvent;
import com.example.dto.response.SearchResult;
import com.example.dto.request.MessageSaveRequest;
import com.example.manager.ChatClientManager;
import com.example.service.*;
import com.example.tool.WebSearchTool;
import com.example.config.MultiModelProperties;
import org.springframework.ai.openai.OpenAiChatOptions;
import com.example.integration.ai.greatwall.GreatWallChatOptions;
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

    private static final String PROVIDER_GREATWALL = "greatwall";

    // 去除 ModelStreamContext，采用“流式优先”并发支路聚合方案

    private final ChatStreamingProperties streamingProperties;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ChatClientManager chatClientManager;
    private final ModelSelector modelSelector;
    private final PromptBuilder promptBuilder;
    private final ChatErrorHandler errorHandler;
    private final SseEventPublisher sseEventPublisher;
    private final MessageToolResultService messageToolResultService;
    private final MultiModelProperties multiModelProperties;

    @Override
    public Flux<ChatEvent> streamChat(StreamChatRequest request) {
        log.info("开始流式聊天，会话ID: {}, 消息长度: {}, 搜索开启: {}, 深度思考: {}, 用户ID: {}, 指定模型: {}-{}", 
                request.getConversationId(), 
                request.getMessage() != null ? request.getMessage().length() : 0, 
                request.isSearchEnabled(), 
                request.isDeepThinking(), 
                request.getUserId(), 
                request.getProvider(), 
                request.getModel());

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
            sseEventPublisher.removeConversation(request.getConversationId());
            log.debug("🧹 清理SseEventPublisher事件发射器，会话ID: {}", request.getConversationId());
        })
        .onErrorResume(errorHandler::handleChatError);
    }

    // ========================= 第一层：主流程控制 =========================

    /**
     * 准备阶段：处理输入和上下文
     */
    private Flux<ChatEvent> prepareContext(StreamChatRequest request) {
        log.debug("开始准备聊天上下文，会话ID: {}", request.getConversationId());
        
        return Flux.concat(
            // 生成标题（异步）
            generateTitleAsync(request)
        );
    }

    /**
     * 执行阶段：与AI模型交互
     */
    private Flux<ChatEvent> processChat(StreamChatRequest request) {
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
    private Flux<ChatEvent> finishChat(StreamChatRequest request) {
        log.debug("完成聊天处理，会话ID: {}", request.getConversationId());
        
        // 在processChat阶段已经处理了响应保存，这里返回空流
        return Flux.empty();
    }

    // ========================= 第二层：各阶段具体实现 =========================

    /**
     * 生成标题（异步执行）
     */
    private Flux<ChatEvent> generateTitleAsync(StreamChatRequest request) {
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
    private Flux<ChatEvent> streamFromAi(ModelSelector.ModelSelection modelSelection,
                                         StreamChatRequest request,
                                         Long userMessageId) {
        log.info("🚀 使用{}提供者，模型: {}, 深度思考: {}, userMessageId: {}",
            modelSelection.providerName(), modelSelection.modelName(), request.isDeepThinking(), userMessageId);

        Long conversationId = request.getConversationId();
        String conversationIdStr = conversationId.toString();

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

                org.reactivestreams.Publisher<ChatEvent> streamPublisher = Flux.defer(() -> buildPrompt(request).flatMapMany(prompt -> {
                    Flux<String> source = getChatClientForModel(modelSelection)
                        .prompt()
                        .user(prompt)
                        .options(buildChatOptions(modelSelection, request))
                        .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationIdStr))
                        .toolContext(java.util.Map.of("conversationId", conversationId, "messageId", assistantMessageId))
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
                                    return content;
                                }
                            }
                            return null;
                        })
                        .filter(Objects::nonNull);

                    Flux<String> hot = source.replay().autoConnect(2);

                    return Flux.merge(
                        hot.map(ChatEvent::chunk),
                        hot
                            .scanWith(StringBuilder::new, (sb, c) -> { sb.append(c); return sb; })
                            .takeLast(1)
                            .flatMap(sb -> Mono.fromCallable(() -> {
                                String finalContent = sb.toString();
                                try {
                                    messageService.updateMessageContent(assistantMessageId, finalContent, null);
                                    log.info("✅ 助手消息内容已更新，messageId: {}，长度: {}", assistantMessageId, finalContent.length());
                                    updated.set(true);
                                } catch (Exception e) {
                                    log.warn("更新助手消息内容失败，messageId: {}，错误: {}", assistantMessageId, e.getMessage());
                                    throw e;
                                }
                                return ChatEvent.end(assistantMessageId);
                            }))
                    );
                }));

                return Flux.concat(
                        Mono.just(ChatEvent.start("AI正在思考中...")),
                        Flux.from(streamPublisher)
                )
                .timeout(streamingProperties.getResponseTimeout())
                .onErrorResume(ex -> handleStreamError(conversationId, assistantMessageId, updated, ex));
            })
        .onErrorResume(errorHandler::handleChatError)
        .doFinally(signalType -> {
            log.debug("🧹 聊天请求处理完成");
        });
    }

    private Flux<ChatEvent> handleStreamError(Long conversationId,
                                              Long assistantMessageId,
                                              java.util.concurrent.atomic.AtomicBoolean updated,
                                              Throwable ex) {
        if (!updated.get()) {
            try {
                try {
                    messageToolResultService.deleteMessageToolResults(assistantMessageId);
                } catch (Exception ignore) {
                    // ignore
                }
                messageService.deleteMessage(assistantMessageId);
                log.info("🧹 已清理失败对话产生的占位消息及其相关工具记录，messageId: {}", assistantMessageId);
            } catch (Exception cleanEx) {
                log.warn("清理占位消息失败，messageId: {}，错误: {}", assistantMessageId, cleanEx.getMessage());
            }
        }
        return errorHandler.handleChatError(ex);
    }
    
    /**
     * 获取指定模型的ChatClient
     * 现在所有模型的ChatClient都配置了WebSearchTool和MessageHistoryAdvisor
     */
    private ChatClient getChatClientForModel(ModelSelector.ModelSelection modelSelection) {
        // 使用ChatClientManager，每个ChatClient都已配置WebSearchTool和MessageHistoryAdvisor
        return chatClientManager.getChatClient(modelSelection.providerName());
    }

    /**
     * 基于 provider/model 及请求参数构建本次调用的 ChatOptions。
     */
    private org.springframework.ai.chat.prompt.ChatOptions buildChatOptions(ModelSelector.ModelSelection modelSelection,
                                                                            StreamChatRequest request) {
        String provider = modelSelection.providerName();
        String model = modelSelection.modelName();

        MultiModelProperties.ProviderConfig p = multiModelProperties.getProviders().get(provider);
        MultiModelProperties.ModelConfig m = null;
        if (p != null && p.getModels() != null) {
            m = p.getModels().stream().filter(x -> model.equals(x.getName())).findFirst().orElse(null);
        }

        double temperature = m != null && m.getTemperature() != null ? m.getTemperature().doubleValue()
                : multiModelProperties.getDefaults().getTemperature().doubleValue();
        Integer maxTokens = m != null && m.getMaxTokens() != null ? m.getMaxTokens()
                : multiModelProperties.getDefaults().getMaxTokens();

        if (PROVIDER_GREATWALL.equalsIgnoreCase(provider)) {
            GreatWallChatOptions opts = GreatWallChatOptions.create();
            opts.setModel(model);
            opts.setTemperature(temperature);
            opts.setMaxTokens(maxTokens);
            // 仅在模型支持且请求开启时启用 thinking
            boolean enableThinking = request.isDeepThinking() && chatClientManager.supportsThinking(provider, model);
            opts.setEnableThinking(enableThinking);
            return opts;
        }

        // 其他 OpenAI 兼容模型（openai/qwen/kimi2/deepseek 等）
        return OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }
}
