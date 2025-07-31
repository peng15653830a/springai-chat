package com.example.service;

public class AiResponse {
    private String content;
    private String thinking;
    
    public AiResponse(String content, String thinking) {
        this.content = content;
        this.thinking = thinking;
    }
    
    public String getContent() { 
        return content; 
    }
    
    public String getThinking() { 
        return thinking; 
    }
}