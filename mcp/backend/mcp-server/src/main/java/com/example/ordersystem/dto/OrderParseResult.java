package com.example.ordersystem.dto;

public class OrderParseResult {
    private boolean success;
    private String productName;
    private Integer quantity;
    private String errorMessage;
    
    private OrderParseResult(boolean success, String productName, Integer quantity, String errorMessage) {
        this.success = success;
        this.productName = productName;
        this.quantity = quantity;
        this.errorMessage = errorMessage;
    }
    
    public static OrderParseResult success(String productName, Integer quantity) {
        return new OrderParseResult(true, productName, quantity, null);
    }
    
    public static OrderParseResult error(String errorMessage) {
        return new OrderParseResult(false, null, null, errorMessage);
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        return "OrderParseResult{" +
                "success=" + success +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}