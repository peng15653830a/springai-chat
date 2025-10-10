package com.example.novel.service.mcp;

import com.example.novel.dto.request.McpExecuteRequest;
import com.example.novel.dto.response.McpExecuteResponse;
import com.example.novel.dto.response.McpToolListResponse;
import reactor.core.publisher.Mono;

public interface McpService {
    Mono<McpToolListResponse> getAvailableTools();
    Mono<McpExecuteResponse> executeTool(McpExecuteRequest request);
}