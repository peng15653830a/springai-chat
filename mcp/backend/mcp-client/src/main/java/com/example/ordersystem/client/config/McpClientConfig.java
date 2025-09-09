package com.example.ordersystem.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xupeng
 */
@Configuration
public class McpClientConfig {
    /**
     * 注入ChatClient
     *
     * @param chatModel
     * @param toolCallbackProvider 报红不用管，没有任何问题
     * @return
     */
    @Bean
    ChatClient chatClient(OpenAiChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(chatModel).defaultToolCallbacks(toolCallbackProvider.getToolCallbacks()).build();
    }
}
