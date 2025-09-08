package com.example.ordersystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 订单创建请求DTO，用于Spring AI Function Calling
 */
public record OrderRequest(
    @JsonProperty("product_name") String productName,
    @JsonProperty("quantity") Integer quantity,
    @JsonProperty("user_id") Long userId
) {
    public OrderRequest {
        // 默认用户ID为1（简化处理）
        if (userId == null) {
            userId = 1L;
        }
        
        // 验证必填字段
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
    }
}