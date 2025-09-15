package com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息保存请求对象
 * 统一封装所有saveMessage方法的参数
 * 
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSaveRequest {
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 消息角色（user/assistant）
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 思考过程（可选，主要用于AI消息）
     */
    private String thinking;
    
    
    // ========================= 便利方法 =========================
    
    /**
     * 创建用户消息保存请求的便利方法
     */
    public static MessageSaveRequest forUser(Long conversationId, String content) {
        return MessageSaveRequest.builder()
                .conversationId(conversationId)
                .role("user")
                .content(content)
                .build();
    }
    
    /**
     * 创建AI消息保存请求的便利方法
     */
    public static MessageSaveRequest forAssistant(Long conversationId, String content) {
        return MessageSaveRequest.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(content)
                .build();
    }
    
    /**
     * 创建包含思考过程的AI消息保存请求的便利方法
     */
    public static MessageSaveRequest forAssistantWithThinking(Long conversationId, String content, String thinking) {
        return MessageSaveRequest.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(content)
                .thinking(thinking)
                .build();
    }
    
    
}