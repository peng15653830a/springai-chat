package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ModelScope API相关配置属性
 * 
 * @author xupeng
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.openai")
public class ModelScopeProperties {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl = "https://api-inference.modelscope.cn/v1";

    /**
     * 聊天配置
     */
    private Chat chat = new Chat();

    @Data
    public static class Chat {
        /**
         * 聊天选项配置
         */
        private Options options = new Options();

        @Data
        public static class Options {
            /**
             * 默认模型
             */
            private String model = "Qwen/Qwen3-235B-A22B-Thinking-2507";

            /**
             * 温度参数
             */
            private double temperature = 0.7;

            /**
             * 最大令牌数
             */
            private int maxTokens = 2000;

            /**
             * 是否启用思考模式
             */
            private boolean enableThinking = true;

            /**
             * 思考预算
             */
            private int thinkingBudget = 50000;
        }
    }
}