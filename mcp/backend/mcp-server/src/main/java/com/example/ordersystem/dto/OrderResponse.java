package com.example.ordersystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 订单创建响应DTO，用于Spring AI Function Calling
 */
public record OrderResponse(
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("message") String message
) {
    public boolean isSuccess() {
        return orderId != null;
    }
}