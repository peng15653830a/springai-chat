package com.example.ai.chat;

import com.example.ai.api.ChatApi;
import com.example.ai.api.ChatCompletionRequest;
import com.example.ai.api.ChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 长城大模型ChatModel实现
 * 使用标准化的ChatApi接口，符合Spring AI设计理念
 *
 * @author xupeng
 */
@Slf4j
public class GreatWallChatModel implements ChatModel {

    private final ChatApi chatApi;
    private final GreatWallChatOptions defaultOptions;

    public GreatWallChatModel(ChatApi chatApi, GreatWallChatOptions defaultOptions) {
        this.chatApi = chatApi;
        this.defaultOptions = defaultOptions;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        // 同步调用，通过阻塞流式调用实现
        return stream(prompt).blockLast();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        log.info("🚀 长城大模型开始流式聊天");

        try {
            // 构建统一的API请求
            ChatCompletionRequest request = buildChatCompletionRequest(prompt);
            
            // 调用统一API并转换为Spring AI ChatResponse
            return chatApi.chatCompletionStream(request)
                .map(this::toChatResponse)
                .doOnComplete(() -> log.info("✅ 长城大模型流式聊天完成"));

        } catch (Exception e) {
            log.error("❌ 长城大模型调用失败", e);
            return Flux.error(e);
        }
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return defaultOptions;
    }

    /**
     * 构建ChatCompletionRequest
     */
    private ChatCompletionRequest buildChatCompletionRequest(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        
        // 处理null消息的情况
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }
        
        ChatOptions promptOptions = prompt.getOptions();
        GreatWallChatOptions mergedOptions = mergeOptions(promptOptions);
        
        // 转换消息格式
        List<ChatCompletionRequest.ChatMessage> apiMessages = messages.stream()
            .map(this::toApiMessage)
            .collect(Collectors.toList());
        
        return ChatCompletionRequest.builder()
            .model(mergedOptions.getModel())
            .messages(apiMessages)
            .temperature(mergedOptions.getTemperature())
            .maxTokens(mergedOptions.getMaxTokens())
            .stream(true)
            .build();
    }
    
    /**
     * 转换Spring AI Message为API Message
     */
    private ChatCompletionRequest.ChatMessage toApiMessage(Message message) {
        String role = mapMessageRole(message);
        return ChatCompletionRequest.ChatMessage.builder()
            .role(role)
            .content(message.getText())
            .build();
    }
    
    /**
     * 映射消息角色
     */
    private String mapMessageRole(Message message) {
        String messageType = message.getClass().getSimpleName().toLowerCase();
        switch (messageType) {
            case "usermessage":
                return "user";
            case "assistantmessage":
                return "assistant";
            case "systemmessage":
                return "system";
            default:
                return "user";
        }
    }
    
    /**
     * 转换API响应为Spring AI ChatResponse
     */
    private ChatResponse toChatResponse(ChatCompletionResponse apiResponse) {
        if (apiResponse.getChoices() == null || apiResponse.getChoices().isEmpty()) {
            return new ChatResponse(Collections.emptyList());
        }
        
        ChatCompletionResponse.Choice choice = apiResponse.getChoices().get(0);
        ChatCompletionResponse.Delta delta = choice.getDelta();
        
        if (delta == null || delta.getContent() == null) {
            return new ChatResponse(Collections.emptyList());
        }
        
        // 创建AssistantMessage
        AssistantMessage assistantMessage = new AssistantMessage(delta.getContent());
        Generation generation = new Generation(assistantMessage);
        
        return new ChatResponse(Collections.singletonList(generation));
    }
    
    /**
     * 合并聊天选项
     */
    private GreatWallChatOptions mergeOptions(ChatOptions promptOptions) {
        if (promptOptions == null) {
            return defaultOptions;
        }

        // 如果传入的就是GreatWallChatOptions，直接返回
        if (promptOptions instanceof GreatWallChatOptions) {
            return (GreatWallChatOptions) promptOptions;
        }

        // 否则使用默认选项并应用通用设置
        GreatWallChatOptions mergedOptions = defaultOptions.copy();
        if (promptOptions.getModel() != null) {
            mergedOptions.setModel(promptOptions.getModel());
        }
        if (promptOptions.getTemperature() != null) {
            mergedOptions.setTemperature(promptOptions.getTemperature());
        }
        if (promptOptions.getMaxTokens() != null) {
            mergedOptions.setMaxTokens(promptOptions.getMaxTokens());
        }

        return mergedOptions;
    }
}