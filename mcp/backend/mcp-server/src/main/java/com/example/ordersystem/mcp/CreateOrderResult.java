package com.example.ordersystem.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 创建订单结果DTO
 */
public class CreateOrderResult {
    
    @JsonProperty("orderId")
    private Long orderId;
    
    @JsonProperty("message")
    private String message;
    
    public CreateOrderResult() {}
    
    public CreateOrderResult(Long orderId, String message) {
        this.orderId = orderId;
        this.message = message;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}