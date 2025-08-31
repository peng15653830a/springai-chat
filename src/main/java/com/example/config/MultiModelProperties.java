package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多模型配置属性类
 * 
 * @author xupeng
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.models")
public class MultiModelProperties {

    /**
     * 是否启用多模型功能
     */
    private boolean enabled = true;

    /**
     * 默认提供者名称
     */
    private String defaultProvider = "qwen";

    /**
     * 默认模型名称
     */
    private String defaultModel = "Qwen/Qwen3-235B-A22B-Thinking-2507";

    /**
     * 全局默认配置
     */
    private GlobalDefaults defaults = new GlobalDefaults();

    /**
     * 提供者配置映射
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    /**
     * 全局默认配置
     */
    @Data
    public static class GlobalDefaults {
        /**
         * 默认温度参数
         */
        private BigDecimal temperature = BigDecimal.valueOf(0.7);

        /**
         * 默认最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 默认请求超时时间（毫秒）
         */
        private Integer timeoutMs = 30000;

        /**
         * 默认推理预算
         */
        private Integer thinkingBudget = 50000;

        /**
         * 默认是否启用流式输出
         */
        private boolean streamEnabled = true;
    }

    /**
     * 提供者配置
     */
    @Data
    public static class ProviderConfig {
        /**
         * 是否启用该提供者
         */
        private boolean enabled = true;

        /**
         * 显示名称
         */
        private String displayName;

        /**
         * API密钥环境变量名
         */
        private String apiKeyEnv;

        /**
         * API基础URL
         */
        private String baseUrl;

        /**
         * 连接超时时间（毫秒）
         */
        private Integer connectTimeoutMs = 10000;

        /**
         * 读取超时时间（毫秒）
         */
        private Integer readTimeoutMs = 30000;

        /**
         * 该提供者下的模型列表
         */
        private List<ModelConfig> models;
    }

    /**
     * 模型配置
     */
    @Data
    public static class ModelConfig {
        /**
         * 模型名称
         */
        private String name;

        /**
         * 显示名称
         */
        private String displayName;

        /**
         * 最大token数
         */
        private Integer maxTokens;

        /**
         * 温度参数
         */
        private BigDecimal temperature;

        /**
         * 是否支持推理模式
         */
        private boolean supportsThinking = false;

        /**
         * 是否支持流式输出
         */
        private boolean supportsStreaming = true;

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 排序顺序
         */
        private Integer sortOrder = 0;

        /**
         * 推理预算（仅推理模式有效）
         */
        private Integer thinkingBudget;
    }

    /**
     * 获取指定提供者的API密钥
     * 
     * @param providerName 提供者名称
     * @return API密钥
     */
    public String getApiKey(String providerName) {
        ProviderConfig provider = providers.get(providerName);
        if (provider == null || provider.getApiKeyEnv() == null) {
            return null;
        }
        // 使用标准环境变量获取
        return System.getenv(provider.getApiKeyEnv());
    }

    /**
     * 检查指定提供者是否可用
     * 
     * @param providerName 提供者名称
     * @return 是否可用
     */
    public boolean isProviderAvailable(String providerName) {
        ProviderConfig provider = providers.get(providerName);
        if (provider == null || !provider.isEnabled()) {
            return false;
        }
        String apiKey = getApiKey(providerName);
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}