package com.example.ordersystem.server.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPConfiguration {
    
    @Bean
    public ToolCallbackProvider toolCallbackProvider(OrderSystemTools orderSystemTools) {
        return MethodToolCallbackProvider.builder().toolObjects(orderSystemTools).build();
    }
}