package com.example.integration.ai.api;

import com.example.dto.request.ChatCompletionRequest;
import com.example.dto.response.ChatCompletionResponse;
import reactor.core.publisher.Flux;

/**
 * 统一的聊天API接口
 * 参考Spring AI中OpenAiApi的设计，为不同的AI提供商提供统一的API接口
 * 
 * @author xupeng
 */
public interface ChatApi {

    /**
     * 流式聊天补全
     * 
     * @param request 聊天补全请求
     * @return 响应流
     */
    Flux<ChatCompletionResponse> chatCompletionStream(ChatCompletionRequest request);

    /**
     * 同步聊天补全
     * 
     * @param request 聊天补全请求
     * @return 响应
     */
    default ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        // 默认实现：通过流式接口获取最后一个响应
        return chatCompletionStream(ChatCompletionRequest.builder()
            .model(request.getModel())
            .messages(request.getMessages())
            .temperature(request.getTemperature())
            .maxTokens(request.getMaxTokens())
            .stream(false)
            .build())
            .blockLast();
    }

    /**
     * 检查API是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取API端点
     * 
     * @return API端点URL
     */
    String getApiEndpoint();
}