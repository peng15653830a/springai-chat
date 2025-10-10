package com.example.novel.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class McpExecuteRequest {
    private String toolName;
    private Map<String, Object> parameters;
    // 可选：将工具调用关联到创作会话/消息
    private Long sessionId;
    private Long messageId;
}
