package com.example.ordersystem.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class MCPController {
    
    // 使用Spring AI MCP Starter提供的标准实现
    // MCP功能将通过自动配置的端点提供服务
    // 默认情况下，MCP Server会提供 /mcp 相关的端点
    
    @GetMapping("/health")
    public String healthCheck() {
        return "MCP service is running";
    }
}