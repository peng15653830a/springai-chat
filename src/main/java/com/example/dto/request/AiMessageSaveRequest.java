package com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI消息保存请求对象
 * 用于封装saveAiMessageWithSearchAsync方法的参数
 * 
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageSaveRequest {
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * AI响应内容
     */
    private String content;
    
    /**
     * 思考过程内容（可选）
     */
    private String thinking;
    
    /**
     * 搜索结果列表（可选）
     */
    private List<?> searchResults;
}