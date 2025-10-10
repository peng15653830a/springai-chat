package com.example.novel.dto.request;

import lombok.Data;

@Data
public class NovelStreamRequest {
    private String model;
    private String prompt;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private Double topP = 1.0;
    private String context;
}