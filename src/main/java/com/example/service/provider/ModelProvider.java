package com.example.service.provider;

import com.example.dto.ModelInfo;
import com.example.service.dto.ChatRequest;
import com.example.service.dto.SseEventResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI模型提供者接口
 * 定义了所有AI模型提供者必须实现的统一接口
 * 
 * @author xupeng
 */
public interface ModelProvider {

    /**
     * 获取提供者名称
     * 
     * @return 提供者名称（唯一标识）
     */
    String getProviderName();

    /**
     * 获取提供者显示名称
     * 
     * @return 提供者显示名称
     */
    String getDisplayName();

    /**
     * 获取该提供者下所有可用的模型列表
     * 
     * @return 模型列表
     */
    List<ModelInfo> getAvailableModels();

    /**
     * 执行流式聊天
     * 
     * @param request 聊天请求
     * @return 响应式SSE事件流
     */
    Flux<SseEventResponse> streamChat(ChatRequest request);

    /**
     * 检查提供者是否可用
     * 主要检查API密钥等关键配置是否正确
     * 
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 检查指定模型是否支持推理模式
     * 
     * @param modelName 模型名称
     * @return 是否支持推理模式
     */
    boolean supportsThinking(String modelName);

    /**
     * 检查指定模型是否支持流式输出
     * 
     * @param modelName 模型名称
     * @return 是否支持流式输出
     */
    boolean supportsStreaming(String modelName);

    /**
     * 获取模型的默认配置
     * 
     * @param modelName 模型名称
     * @return 模型配置信息
     */
    ModelInfo getModelInfo(String modelName);
}