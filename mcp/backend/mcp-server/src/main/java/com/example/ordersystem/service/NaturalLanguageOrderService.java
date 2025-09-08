package com.example.ordersystem.service;

import com.example.ordersystem.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NaturalLanguageOrderService {
    
    @Autowired
    private SpringAIOrderService springAIOrderService;

    public Order processNaturalLanguageOrder(String instruction) {
        System.out.println("=== Processing Natural Language Order ===");
        System.out.println("Input instruction: " + instruction);
        
        if (instruction == null || instruction.trim().isEmpty()) {
            throw new RuntimeException("Empty instruction provided");
        }
        
        try {
            // 直接使用Spring AI处理订单
            return springAIOrderService.processNaturalLanguageOrder(instruction);
        } catch (Exception e) {
            System.err.println("=== Order Processing Failed ===");
            System.err.println("Instruction: " + instruction);
            System.err.println("Error Type: " + e.getClass().getSimpleName());
            System.err.println("Error Message: " + e.getMessage());
            
            if (e.getCause() != null) {
                System.err.println("Root Cause: " + e.getCause().getMessage());
            }

            // 提供更友好的错误信息
            String userFriendlyMessage = getUserFriendlyErrorMessage(e);
            throw new RuntimeException(userFriendlyMessage, e);
        }
    }
    
    private String getUserFriendlyErrorMessage(Exception e) {
        String message = e.getMessage().toLowerCase();
        
        if (message.contains("product not found")) {
            return "抱歉，找不到您要订购的商品。请检查商品名称是否正确。";
        } else if (message.contains("user not found")) {
            return "用户信息错误，请重新登录后再试。";
        } else if (message.contains("empty") || message.contains("missing")) {
            return "AI解析失败，请尝试更清楚地描述您的订单需求。";
        } else if (message.contains("http") || message.contains("api")) {
            return "AI服务暂时不可用，请稍后再试。";
        } else if (message.contains("json") || message.contains("parse")) {
            return "订单信息解析失败，请重新描述您的需求。";
        } else {
            return "订单处理失败：" + e.getMessage();
        }
    }
}