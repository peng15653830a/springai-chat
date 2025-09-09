package com.example.ordersystem.server.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MCPController {
    
    @GetMapping("/health")
    public String healthCheck() {
        return "MCP Server is running - Order System Tools Available";
    }
    
    @GetMapping("/tools")
    public String listTools() {
        return "Available tools: create_order, get_order, search_products, get_orders";
    }
}