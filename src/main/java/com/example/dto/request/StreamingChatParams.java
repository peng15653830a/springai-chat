package com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式聊天执行参数对象
 * 用于封装executeStreamingChatWithModel方法的参数
 * 
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamingChatParams {
    
    /**
     * 完整提示内容
     */
    private String prompt;
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 是否启用深度思考
     */
    private boolean deepThinking;
    
    /**
     * 模型提供者名称
     */
    private String providerName;
    
    /**
     * 模型名称
     */
    private String modelName;
}