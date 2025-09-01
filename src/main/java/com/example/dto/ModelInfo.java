package com.example.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 模型信息DTO
 * 
 * @author xupeng
 */
@Data
public class ModelInfo {

    /**
     * 模型ID
     */
    private Long id;

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
     * 是否可用
     */
    private Boolean available;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 获取完整的模型标识符
     * 
     * @param providerId 提供者ID
     * @return 格式：providerId-modelName
     */
    public String getFullModelId(Long providerId) {
        return providerId + "-" + name;
    }
}