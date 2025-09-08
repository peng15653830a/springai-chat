package com.example.ordersystem.service;

import com.example.ordersystem.entity.Order;
import com.example.ordersystem.entity.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 使用Spring AI ChatClient的订单处理服务
 */
@Service
public class SpringAIOrderService {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Order processNaturalLanguageOrder(String instruction) {
        System.out.println("=== Spring AI 处理订单 ===");
        System.out.println("用户指令: " + instruction);
        
        try {
            String promptText = """
                你是一个订单处理助手。请将以下用户的自然语言指令转换为结构化的订单信息。
                用户指令: %s
                
                请按照以下JSON格式回复:
                {
                  "product": "产品名称",
                  "quantity": 数量
                }
                
                只回复JSON，不要包含其他内容。
                """.formatted(instruction);
            // 使用Spring AI ChatClient处理自然语言指令
            String response = chatClient.prompt()
//                .system("你是一个订单处理助手。请将用户的自然语言指令解析为JSON格式，包含product_name和quantity字段。例如：{\"product_name\": \"苹果\", \"quantity\": 3}")
                .system(promptText)
                .user(instruction)
                .call()
                .content();
            
            System.out.println("AI响应: " + response);
            
            // 解析响应并返回订单
            return parseOrderFromResponse(response);
            
        } catch (Exception e) {
            System.err.println("Spring AI处理失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Spring AI订单处理失败: " + e.getMessage(), e);
        }
    }
    
    private Order parseOrderFromResponse(String response) {
        try {
            System.out.println("解析AI响应: " + response);
            
            // 清理可能的代码块标记
            String cleanedResponse = response;
            if (cleanedResponse.contains("```json")) {
                cleanedResponse = cleanedResponse.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");
            } else if (cleanedResponse.contains("```")) {
                cleanedResponse = cleanedResponse.replaceAll("```\\s*", "");
            }
            
            // 解析JSON响应
            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);
            
            String productName = jsonNode.get("product").asText();
            int quantity = jsonNode.get("quantity").asInt();
            
            System.out.println("解析结果 - 产品: " + productName + ", 数量: " + quantity);
            
            // 调用业务逻辑创建订单
            return createOrderFromParsedData(productName, quantity);
            
        } catch (Exception e) {
            System.err.println("解析AI响应失败: " + e.getMessage());
            throw new RuntimeException("AI响应解析失败: " + e.getMessage(), e);
        }
    }
    
    private Order createOrderFromParsedData(String productName, int quantity) {
        // 查找产品
        Product product = productService.findByName(productName);
        if (product == null) {
            throw new RuntimeException("产品不存在: " + productName);
        }
        
        // 检查库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存: " + product.getStock());
        }
        
        // 创建订单
        Order order = new Order(
            1L, // 默认用户ID
            product.getId(),
            quantity,
            product.getPrice().multiply(BigDecimal.valueOf(quantity))
        );
        
        return orderService.saveOrder(order);
    }
}