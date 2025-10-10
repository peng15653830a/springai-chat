package com.example.ordersystem.client.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器，处理AI聊天请求和自然语言订单
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    @Resource
    private ChatClient chatClient;

    @GetMapping("/chat")
    public String chat(@RequestParam String userInput) {
        return this.chatClient.prompt().user(userInput).call().content();
    }
    
    @PostMapping("/nl-orders")
    public String processOrder(@RequestBody String userInput) {
        // ChatClient会自动调用MCP工具来处理订单
        String prompt = "请根据用户的自然语言指令创建订单: " + userInput;
        return this.chatClient.prompt().user(prompt).call().content();
    }
} 