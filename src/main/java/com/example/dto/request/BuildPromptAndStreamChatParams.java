package com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 构建提示并执行流式聊天参数对象
 * 用于封装buildPromptAndStreamChatWithModel方法的参数
 * 
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildPromptAndStreamChatParams {
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 用户消息
     */
    private String userMessage;
    
    /**
     * 是否启用搜索
     */
    private boolean searchEnabled;
    
    /**
     * 是否启用深度思考
     */
    private boolean deepThinking;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 模型提供者名称
     */
    private String providerName;
    
    /**
     * 模型名称
     */
    private String modelName;
}