package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private String thinking; // AI推理过程
    private String searchResults;
    private LocalDateTime createdAt;
}