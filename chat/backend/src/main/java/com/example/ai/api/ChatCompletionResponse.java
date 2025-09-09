package com.example.ai.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统一的聊天补全响应模型
 * 参考OpenAI ChatCompletion API设计，支持各种AI提供商
 * 
 * @author xupeng
 */
@Data
@Builder
public class ChatCompletionResponse {

    /**
     * 响应ID
     */
    private String id;

    /**
     * 对象类型，通常为"chat.completion"或"chat.completion.chunk"
     */
    private String object;

    /**
     * 创建时间戳
     */
    private Long created;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * 使用统计
     */
    private Usage usage;

    /**
     * 扩展数据，支持各提供商的特定返回字段
     */
    private Map<String, Object> extra;

    /**
     * 选择项
     */
    @Data
    @Builder
    public static class Choice {
        /**
         * 选择索引
         */
        private Integer index;

        /**
         * 消息内容（非流式）
         */
        private ChatCompletionRequest.ChatMessage message;

        /**
         * 增量内容（流式）
         */
        private Delta delta;

        /**
         * 结束原因: stop, length, content_filter等
         */
        private String finishReason;

        /**
         * 扩展数据
         */
        private Map<String, Object> extra;
    }

    /**
     * 流式响应的增量内容
     */
    @Data
    @Builder
    public static class Delta {
        /**
         * 角色（通常只在第一个chunk中出现）
         */
        private String role;

        /**
         * 增量内容
         */
        private String content;

        /**
         * 思考内容（DeepSeek推理模式）
         */
        private String reasoning;

        /**
         * 扩展数据
         */
        private Map<String, Object> extra;
    }

    /**
     * 使用统计
     */
    @Data
    @Builder
    public static class Usage {
        /**
         * 提示token数
         */
        private Integer promptTokens;

        /**
         * 补全token数
         */
        private Integer completionTokens;

        /**
         * 总token数
         */
        private Integer totalTokens;

        /**
         * 扩展统计信息
         */
        private Map<String, Object> extra;
    }
}