package com.example.service.dto;

import lombok.Data;

/**
 * 聊天请求DTO
 * 统一封装所有AI模型提供者的聊天请求参数
 * 
 * @author xupeng
 */
@Data
public class ChatRequest {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 用户消息内容
     */
    private String userMessage;

    /**
     * 完整的提示文本（包含历史对话和搜索上下文）
     */
    private String fullPrompt;

    /**
     * 是否启用推理模式
     */
    private boolean deepThinking;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 是否启用流式输出
     */
    private boolean streaming = true;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeoutMs;

    /**
     * 推理预算（仅推理模式有效）
     */
    private Integer thinkingBudget;

    /**
     * 构建者模式的静态方法
     * 
     * @return ChatRequest构建者
     */
    public static ChatRequestBuilder builder() {
        return new ChatRequestBuilder();
    }

    /**
     * ChatRequest构建者类
     */
    public static class ChatRequestBuilder {
        private final ChatRequest request = new ChatRequest();

        public ChatRequestBuilder conversationId(Long conversationId) {
            request.conversationId = conversationId;
            return this;
        }

        public ChatRequestBuilder modelName(String modelName) {
            request.modelName = modelName;
            return this;
        }

        public ChatRequestBuilder userMessage(String userMessage) {
            request.userMessage = userMessage;
            return this;
        }

        public ChatRequestBuilder fullPrompt(String fullPrompt) {
            request.fullPrompt = fullPrompt;
            return this;
        }

        public ChatRequestBuilder deepThinking(boolean deepThinking) {
            request.deepThinking = deepThinking;
            return this;
        }

        public ChatRequestBuilder temperature(Double temperature) {
            request.temperature = temperature;
            return this;
        }

        public ChatRequestBuilder maxTokens(Integer maxTokens) {
            request.maxTokens = maxTokens;
            return this;
        }

        public ChatRequestBuilder streaming(boolean streaming) {
            request.streaming = streaming;
            return this;
        }

        public ChatRequestBuilder timeoutMs(Integer timeoutMs) {
            request.timeoutMs = timeoutMs;
            return this;
        }

        public ChatRequestBuilder thinkingBudget(Integer thinkingBudget) {
            request.thinkingBudget = thinkingBudget;
            return this;
        }

        public ChatRequest build() {
            return request;
        }
    }
}