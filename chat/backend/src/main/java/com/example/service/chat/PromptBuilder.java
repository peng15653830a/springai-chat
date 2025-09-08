package com.example.service.chat;

import com.example.entity.Message;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 提示词构建服务接口
 * 负责根据对话历史和搜索上下文构建AI聊天提示
 *
 * @author xupeng
 */
public interface PromptBuilder {

    /**
     * 构建聊天提示词
     * 
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @param searchEnabled 是否启用搜索
     * @return 构建好的提示词
     */
    Mono<String> buildPrompt(Long conversationId, String userMessage, boolean searchEnabled);

    /**
     * 基于历史消息构建提示词
     * 
     * @param messages 历史消息列表
     * @param currentMessage 当前用户消息
     * @param searchContext 搜索上下文（可选）
     * @return 构建好的提示词
     */
    String buildPromptFromMessages(List<Message> messages, String currentMessage, String searchContext);

    /**
     * 构建系统提示词
     * 
     * @return 系统提示词
     */
    String buildSystemPrompt();

    /**
     * 格式化搜索上下文
     * 
     * @param searchResults 搜索结果
     * @return 格式化后的搜索上下文
     */
    String formatSearchContext(String searchResults);
}