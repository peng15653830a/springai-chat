package com.example.service;

import com.example.dto.request.ChatRequest;
import com.example.dto.response.SseEventResponse;
import reactor.core.publisher.Flux;

/**
 * 聊天模型服务接口
 * 负责处理实际的AI模型聊天功能，使用Spring AI的ChatClient
 * 
 * @author xupeng
 */
public interface ChatModelService {

    /**
     * 执行流式聊天
     * 
     * @param request 聊天请求
     * @return 响应式SSE事件流
     */
    Flux<SseEventResponse> streamChat(ChatRequest request);

    /**
     * 检查指定提供者和模型是否可用
     * 
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 是否可用
     */
    boolean isModelAvailable(String providerName, String modelName);
}