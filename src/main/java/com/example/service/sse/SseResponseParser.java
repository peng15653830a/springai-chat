package com.example.service.sse;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * SSE响应解析器接口
 * 用于解析不同AI提供商的SSE流式响应格式
 *
 * @author xupeng
 */
public interface SseResponseParser {

    /**
     * 解析原始SSE行为ChatResponse流
     *
     * @param sseLines 原始SSE行流
     * @return ChatResponse流
     */
    Flux<ChatResponse> parseStream(Flux<String> sseLines);

    /**
     * 检查SSE行是否有效
     */
    boolean isValidSseLine(String line);

    /**
     * 获取支持的响应格式名称
     */
    String getResponseFormat();
}