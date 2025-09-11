package com.example.handler;

import com.example.dto.response.SseEventResponse;
import reactor.core.publisher.Flux;

/**
 * 聊天错误处理器接口
 * 负责处理聊天过程中的各种错误情况
 *
 * @author xupeng
 */
public interface ChatErrorHandler {

    /**
     * 处理聊天错误
     * 
     * @param error 错误信息
     * @return 错误响应事件流
     */
    Flux<SseEventResponse> handleChatError(Throwable error);

    /**
     * 获取用户友好的错误消息
     * 
     * @param error 错误信息
     * @return 用户友好的错误消息
     */
    String getErrorMessage(Throwable error);

    /**
     * 判断是否为可重试的错误
     * 
     * @param error 错误信息
     * @return 是否可重试
     */
    boolean isRetryableError(Throwable error);

    /**
     * 获取错误类型
     * 
     * @param error 错误信息
     * @return 错误类型
     */
    ErrorType getErrorType(Throwable error);

    /**
     * 错误类型枚举
     */
    enum ErrorType {
        /** 网络错误 */
        NETWORK_ERROR,
        /** 超时错误 */
        TIMEOUT_ERROR,
        /** API密钥错误 */
        API_KEY_ERROR,
        /** 配额不足 */
        QUOTA_EXCEEDED,
        /** 模型不可用 */
        MODEL_UNAVAILABLE,
        /** 请求参数错误 */
        INVALID_REQUEST,
        /** 系统内部错误 */
        INTERNAL_ERROR,
        /** 未知错误 */
        UNKNOWN_ERROR
    }
}