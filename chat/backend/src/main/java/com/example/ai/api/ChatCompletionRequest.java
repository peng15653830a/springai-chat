package com.example.ai.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统一的聊天补全请求模型
 * 参考OpenAI ChatCompletion API设计，支持各种AI提供商
 * 
 * @author xupeng
 */
@Data
@Builder
public class ChatCompletionRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    private List<ChatMessage> messages;

    /**
     * 温度参数 (0.0 - 2.0)
     */
    private Double temperature;

    /**
     * 最大输出token数
     */
    private Integer maxTokens;

    /**
     * 是否流式输出
     */
    @Builder.Default
    private Boolean stream = true;

    /**
     * Top-p核心采样参数
     */
    private Double topP;

    /**
     * 停止序列
     */
    private List<String> stop;

    /**
     * 频率惩罚参数
     */
    private Double frequencyPenalty;

    /**
     * 存在惩罚参数
     */
    private Double presencePenalty;

    /**
     * 扩展参数，支持提供商特定的参数
     * 例如：DeepSeek的enable_thinking, thinking_budget
     *      长城大模型的特定参数等
     */
    private Map<String, Object> extra;

    /**
     * 聊天消息
     */
    @Data
    @Builder
    public static class ChatMessage {
        /**
         * 角色: system, user, assistant
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 扩展内容，支持多模态等
         */
        private Map<String, Object> extra;
    }
}