package com.example.novel.dto.request;

import lombok.Data;

@Data
public class RagSearchRequest {
    private String query;
    private Integer topK = 5;
    private Double minSimilarity = 0.3;
    // 可选：用于将检索引用归档到指定会话/消息
    private Long sessionId;
    private Long messageId;
}
