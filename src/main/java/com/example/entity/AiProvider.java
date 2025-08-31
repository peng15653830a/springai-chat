package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI模型提供者实体类
 * 
 * @author xupeng
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiProvider {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 提供者名称（唯一标识）
     */
    private String name;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * API密钥环境变量名
     */
    private String apiKeyEnv;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 配置JSON（存储提供者特有的配置信息）
     */
    private String configJson;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 获取API密钥
     * 
     * @return API密钥
     */
    public String getApiKey() {
        if (apiKeyEnv == null || apiKeyEnv.trim().isEmpty()) {
            return null;
        }
        return System.getenv(apiKeyEnv);
    }

    /**
     * 检查提供者是否可用
     * 
     * @return 是否可用
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(enabled) && getApiKey() != null && !getApiKey().trim().isEmpty();
    }
}