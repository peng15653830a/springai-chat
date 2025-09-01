package com.example.dto.common;

import lombok.Data;

/**
 * 用户模型偏好DTO
 *
 * @author xupeng
 */
@Data
public class UserModelPreferenceDto {
    private Long userId;
    private Long providerId;
    private Long modelId;
    private String providerName;
    private String modelName;
}