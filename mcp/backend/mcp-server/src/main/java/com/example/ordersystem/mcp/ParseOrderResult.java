package com.example.ordersystem.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 解析订单结果DTO
 */
public class ParseOrderResult {
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("message")
    private String message;
    
    public ParseOrderResult() {}
    
    public ParseOrderResult(String productName, Integer quantity, String message) {
        this.productName = productName;
        this.quantity = quantity;
        this.message = message;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}