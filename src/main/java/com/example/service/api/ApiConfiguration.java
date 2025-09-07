package com.example.service.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * API客户端配置类
 * 封装API调用相关的配置参数
 *
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiConfiguration {

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 连接超时时间
     */
    @Builder.Default
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * 读取超时时间
     */
    @Builder.Default
    private Duration readTimeout = Duration.ofSeconds(30);

    /**
     * 重试次数
     */
    @Builder.Default
    private int retryAttempts = 3;

    /**
     * 是否跳过SSL验证（仅开发环境）
     */
    @Builder.Default
    private boolean skipSslVerification = false;

    /**
     * 用户代理字符串
     */
    @Builder.Default
    private String userAgent = "SpringAI-ChatClient/1.0.0";

    /**
     * 是否启用详细日志
     */
    @Builder.Default
    private boolean enableDebugLog = false;

    /**
     * 创建配置的构建器
     */
    public static ApiConfigurationBuilder builder() {
        return new ApiConfigurationBuilder();
    }

    /**
     * 验证配置完整性
     */
    public void validate() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("API baseUrl cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        if (connectTimeout == null || connectTimeout.isNegative()) {
            throw new IllegalArgumentException("Connect timeout must be positive");
        }
        if (readTimeout == null || readTimeout.isNegative()) {
            throw new IllegalArgumentException("Read timeout must be positive");
        }
        if (retryAttempts < 0) {
            throw new IllegalArgumentException("Retry attempts cannot be negative");
        }
    }
}