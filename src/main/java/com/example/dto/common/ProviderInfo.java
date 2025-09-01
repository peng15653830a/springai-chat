package com.example.dto.common;

import lombok.Data;

import java.util.List;

/**
 * 提供者信息DTO
 * 
 * @author xupeng
 */
@Data
public class ProviderInfo {

    /**
     * 提供者ID
     */
    private Long id;

    /**
     * 提供者名称
     */
    private String name;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 是否可用
     */
    private Boolean available;

    /**
     * 该提供者下的模型列表
     */
    private List<ModelInfo> models;

    /**
     * 获取可用模型数量
     * 
     * @return 可用模型数量
     */
    public int getAvailableModelCount() {
        if (models == null || models.isEmpty()) {
            return 0;
        }
        return (int) models.stream()
                .filter(model -> Boolean.TRUE.equals(model.getAvailable()))
                .count();
    }
}