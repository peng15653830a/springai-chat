package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户模型偏好实体类
 * 
 * @author xupeng
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModelPreference {

    /**
     * 主键ID
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
     * 模型ID
     */
    private Long modelId;

    /**
     * 是否为默认选择
     */
    private Boolean isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}