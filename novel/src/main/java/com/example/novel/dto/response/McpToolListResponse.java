package com.example.novel.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class McpToolListResponse {
    private Boolean success;
    private String message;
    private List<McpTool> tools;

    @Data
    public static class McpTool {
        private String name;
        private String description;
        private Map<String, Object> inputSchema;
    }
}