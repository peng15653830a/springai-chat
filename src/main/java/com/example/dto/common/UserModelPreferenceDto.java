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
    private String providerName;
    private String modelName;
    private String providerDisplayName;
    private String modelDisplayName;
    private Boolean isDefault;
    private Boolean supportsThinking;
    private Boolean supportsStreaming;
}