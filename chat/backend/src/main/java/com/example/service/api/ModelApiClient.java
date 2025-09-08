package com.example.service.api;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 统一的AI模型API客户端接口
 * 封装不同AI提供商的API调用细节
 *
 * @author xupeng
 */
public interface ModelApiClient {

    /**
     * 获取提供商名称
     */
    String getProviderName();

    /**
     * 流式聊天完成
     *
     * @param messages 聊天消息列表
     * @param modelName 模型名称
     * @param temperature 温度参数
     * @param maxTokens 最大token数
     * @param enableThinking 是否启用推理模式
     * @return ChatResponse流
     */
    Flux<ChatResponse> chatCompletionStream(List<Message> messages, 
                                          String modelName,
                                          Double temperature,
                                          Integer maxTokens,
                                          Boolean enableThinking);

    /**
     * 检查API客户端是否可用
     */
    boolean isAvailable();

    /**
     * 获取API端点URL
     */
    String getApiEndpoint();
}