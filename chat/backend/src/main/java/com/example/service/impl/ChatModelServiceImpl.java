package com.example.service.impl;

import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import com.example.service.ChatModelRegistry;
import com.example.service.ChatModelService;
import com.example.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 聊天模型服务实现
 * 使用ChatModelRegistry管理的ChatClient进行统一的聊天调用
 * 
 * @author xupeng
 */
@Slf4j
@Service
public class ChatModelServiceImpl implements ChatModelService {

    private final ChatModelRegistry chatModelRegistry;
    private final MessageService messageService;

    public ChatModelServiceImpl(ChatModelRegistry chatModelRegistry,
                               MessageService messageService) {
        this.chatModelRegistry = chatModelRegistry;
        this.messageService = messageService;
    }

    @Override
    public Flux<SseEventResponse> streamChat(ChatRequest request) {
        log.info("🚀 开始流式聊天，提供者: {}, 模型: {}", 
            request.getProviderName(), request.getModelName());

        StringBuilder contentBuilder = new StringBuilder();

        return Flux.concat(
            // 1. 发送开始事件
            Mono.just(SseEventResponse.start("AI正在思考中...")),
            
            // 2. 调用AI模型并处理响应
            callChatModel(request)
                .doOnNext(event -> {
                    // 收集内容用于保存
                    if ("chunk".equals(event.getType()) && event.getData() != null) {
                        contentBuilder.append(event.getData().toString());
                    }
                })
                .concatWith(
                    // 3. 保存消息并发送结束事件
                    saveMessageAndGenerateEndEvent(request.getConversationId(), 
                        contentBuilder.toString(), null)
                )
        );
    }

    @Override
    public boolean isModelAvailable(String providerName, String modelName) {
        return chatModelRegistry.isModelAvailable(providerName, modelName);
    }

    /**
     * 调用ChatModel进行聊天
     */
    private Flux<SseEventResponse> callChatModel(ChatRequest request) {
        try {
            // 获取对应的ChatClient
            ChatClient chatClient = chatModelRegistry.getChatClient(
                request.getProviderName(), request.getModelName());
            
            // 构建聊天请求并流式执行
            return chatClient.prompt()
                .user(request.getFullPrompt())
                .stream()
                .content()
                .map(content -> {
                    log.debug("💬 收到内容片段，长度: {}", content.length());
                    return SseEventResponse.chunk(content);
                })
                .onErrorResume(error -> {
                    log.error("❌ {} API调用失败", request.getProviderName(), error);
                    return Flux.just(SseEventResponse.error("AI服务暂时不可用：" + error.getMessage()));
                });
                
        } catch (Exception e) {
            log.error("❌ 创建ChatClient失败", e);
            return Flux.just(SseEventResponse.error("初始化AI服务失败：" + e.getMessage()));
        }
    }

    /**
     * 保存消息并生成结束事件
     */
    private Mono<SseEventResponse> saveMessageAndGenerateEndEvent(Long conversationId, 
                                                                 String content, 
                                                                 String thinking) {
        log.info("💾 准备保存AI响应，会话ID: {}, 内容长度: {}", 
            conversationId, content != null ? content.length() : 0);
        
        if (content == null || content.trim().isEmpty()) {
            log.warn("⚠️ AI响应内容为空，会话ID: {}", conversationId);
            return Mono.just(SseEventResponse.end(null));
        }
        
        return messageService.saveAiMessageAsync(conversationId, content.trim(), thinking)
            .onErrorReturn(SseEventResponse.error("保存AI响应失败"));
    }
}