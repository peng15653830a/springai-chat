package com.example.ai.chat;

import com.example.service.api.impl.GreatWallApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 长城大模型ChatModel实现
 * 集成Spring AI框架，提供统一的聊天接口
 *
 * @author xupeng
 */
@Slf4j
public class GreatWallChatModel implements ChatModel {

    private final GreatWallApiClient apiClient;
    private final GreatWallChatOptions defaultOptions;

    public GreatWallChatModel(GreatWallApiClient apiClient, GreatWallChatOptions defaultOptions) {
        this.apiClient = apiClient;
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
            // 提取消息和选项
            List<Message> messages = prompt.getInstructions();
            ChatOptions promptOptions = prompt.getOptions();
            
            // 合并选项
            GreatWallChatOptions mergedOptions = mergeOptions(promptOptions);
            
            // 调用API客户端
            return apiClient.chatCompletionStream(
                messages,
                mergedOptions.getModel(),
                mergedOptions.getTemperature(),
                mergedOptions.getMaxTokens(),
                mergedOptions.getEnableThinking()
            ).doOnComplete(() -> log.info("✅ 长城大模型流式聊天完成"));

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