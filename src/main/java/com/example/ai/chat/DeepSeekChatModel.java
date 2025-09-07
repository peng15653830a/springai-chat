package com.example.ai.chat;

import com.example.service.api.impl.DeepSeekApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * DeepSeek推理模型ChatModel实现
 * 集成Spring AI框架，支持推理内容提取
 *
 * @author xupeng
 */
@Slf4j
public class DeepSeekChatModel implements ChatModel {

    private final DeepSeekApiClient apiClient;
    private final DeepSeekChatOptions defaultOptions;

    public DeepSeekChatModel(DeepSeekApiClient apiClient, DeepSeekChatOptions defaultOptions) {
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
        log.info("🚀 DeepSeek推理模型开始流式聊天");

        try {
            // 提取消息和选项
            List<Message> messages = prompt.getInstructions();
            ChatOptions promptOptions = prompt.getOptions();
            
            // 合并选项
            DeepSeekChatOptions mergedOptions = mergeOptions(promptOptions);
            
            // 调用API客户端
            return apiClient.chatCompletionStream(
                messages,
                mergedOptions.getModel(),
                mergedOptions.getTemperature(),
                mergedOptions.getMaxTokens(),
                mergedOptions.getEnableThinking()
            ).doOnComplete(() -> log.info("✅ DeepSeek推理模型流式聊天完成"));

        } catch (Exception e) {
            log.error("❌ DeepSeek推理模型调用失败", e);
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
    private DeepSeekChatOptions mergeOptions(ChatOptions promptOptions) {
        if (promptOptions == null) {
            return defaultOptions;
        }

        // 如果传入的就是DeepSeekChatOptions，直接返回
        if (promptOptions instanceof DeepSeekChatOptions) {
            return (DeepSeekChatOptions) promptOptions;
        }

        // 否则使用默认选项并应用通用设置
        DeepSeekChatOptions mergedOptions = defaultOptions.copy();
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