package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型实体类
 * 
 * @author xupeng
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiModel {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 提供者ID
     */
    private Long providerId;

    /**
     * 模型名称（API调用时使用）
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
    private Boolean supportsThinking;

    /**
     * 是否支持流式输出
     */
    private Boolean supportsStreaming;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 配置JSON（存储模型特有的配置信息）
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
     * 检查模型是否可用
     * 
     * @return 是否可用
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * 获取完整的模型标识符
     * 
     * @return 格式：providerId-modelName
     */
    public String getFullModelId() {
        return providerId + "-" + name;
    }
}