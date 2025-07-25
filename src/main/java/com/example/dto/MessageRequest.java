package com.example.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String content;
    private Boolean searchEnabled = true; // 默认开启搜索
}