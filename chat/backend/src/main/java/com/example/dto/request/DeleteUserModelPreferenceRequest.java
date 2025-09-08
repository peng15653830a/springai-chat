package com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除用户模型偏好请求对象
 * 
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserModelPreferenceRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 模型提供者名称
     */
    private String providerName;
    
    /**
     * 模型名称
     */
    private String modelName;
}