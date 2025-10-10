package com.example.novel.dto.response;

import lombok.Data;

@Data
public class McpExecuteResponse {
    private Boolean success;
    private String message;
    private Object result;
    private String error;
}