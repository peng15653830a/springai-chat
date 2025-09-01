package com.example.dto;

import lombok.Data;

/**
 * 用户模型偏好DTO
 * 
 * @author xupeng
 */
@Data
public class UserModelPreferenceDto {

    /**
     * 偏好ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 提供者ID
     */
    private Long providerId;

    /**
     * 提供者名称
     */
    private String providerName;

    /**
     * 提供者显示名称
     */
    private String providerDisplayName;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型显示名称
     */
    private String modelDisplayName;

    /**
     * 是否为默认选择
     */
    private Boolean isDefault;

    /**
     * 是否支持推理模式
     */
    private Boolean supportsThinking;

    /**
     * 获取完整的模型标识符
     * 
     * @return 格式：providerId-modelName
     */
    public String getFullModelId() {
        return providerId + "-" + modelName;
    }
}