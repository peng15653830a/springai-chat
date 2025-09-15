package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
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
         * API密钥
         */
        private String apiKey;

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

        /**
         * 是否为非标准API（如长城大模型）
         */
        private boolean nonStandardApi = false;

        /**
         * API运行ID（长城大模型专用）
         */
        private String apiRunId;

        /**
         * 用户ID前缀（长城大模型专用）
         */
        private String tpuidPrefix = "guest";
    }

    /**
     * 获取指定提供者的API密钥
     * 
     * @param providerName 提供者名称
     * @return API密钥
     */
    public String getApiKey(String providerName) {
        ProviderConfig provider = providers.get(providerName);
        if (provider == null || provider.getApiKey() == null) {
            return null;
        }
        // 直接返回配置的API密钥
        return provider.getApiKey();
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
        
        // 在开发环境中，即使没有API密钥也认为可用
        String apiKey = getApiKey(providerName);
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return true;
        }
        
        // 检查是否为开发环境（根据Spring的默认profile判断）
        // 简化处理，实际应该从Spring环境中获取
        String[] activeProfiles = {"default"};
        boolean isDevEnvironment = Arrays.asList(activeProfiles).contains("dev") || 
                                  Arrays.asList(activeProfiles).contains("development") ||
                                  Arrays.asList(activeProfiles).contains("default");
        
        // 在开发环境中，允许没有API密钥
        return isDevEnvironment;
    }
}