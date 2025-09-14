package com.example.strategy.prompt;

import com.example.entity.Message;
import com.example.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的提示词构建服务实现
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultPromptBuilder implements PromptBuilder {

    private final MessageService messageService;

    @Override
    public Mono<String> buildPrompt(Long conversationId, String userMessage, boolean searchEnabled) {
        log.debug("开始构建对话 {} 的提示词，搜索由Tool Calling自动处理", conversationId);

        // 获取历史消息
        return messageService.getConversationHistoryAsync(conversationId)
                .map(messages -> {
                    // 不再在这里进行搜索，搜索由Spring AI Tool Calling自动处理
                    return buildPromptFromMessages(messages, userMessage, null);
                });
    }

    @Override
    public String buildPromptFromMessages(List<Message> messages, String currentMessage, String searchContext) {
        StringBuilder prompt = new StringBuilder();
        
        // 1. 添加系统提示
        prompt.append(buildSystemPrompt()).append("\n\n");
        
        // 2. 添加搜索上下文（如果有）
        if (searchContext != null && !searchContext.trim().isEmpty()) {
            prompt.append("搜索相关信息：\n")
                  .append(searchContext)
                  .append("\n\n");
        }
        
        // 3. 添加历史对话
        if (messages != null && !messages.isEmpty()) {
            prompt.append("历史对话：\n");
            for (Message message : messages) {
                String role = "assistant".equals(message.getRole()) ? "Assistant" : "User";
                prompt.append(role).append(": ").append(message.getContent()).append("\n");
            }
            prompt.append("\n");
        }
        
        // 4. 添加当前用户消息
        prompt.append("User: ").append(currentMessage).append("\n");
        prompt.append("Assistant: ");
        
        String finalPrompt = prompt.toString();
        log.debug("构建的提示词长度: {} 字符", finalPrompt.length());
        
        return finalPrompt;
    }

    @Override
    public String buildSystemPrompt() {
        return """
                你是一个智能助手，请根据用户的问题提供准确、有用的回答。
                你具有网络搜索能力，会自动搜索最新信息来回答问题。
                
                回答要求：
                1. 回答要准确、简洁、有条理
                2. 总是使用搜索工具获取最新信息
                3. 基于搜索结果提供准确回答
                4. 如果不确定答案，请诚实说明
                5. 使用友好、专业的语气
                """;
    }

    @Override
    public String formatSearchContext(String searchResults) {
        if (searchResults == null || searchResults.trim().isEmpty()) {
            return "";
        }
        
        // 简单格式化搜索结果
        return searchResults.lines()
                .map(line -> "- " + line.trim())
                .filter(line -> line.length() > 2) // 过滤空行
                .collect(Collectors.joining("\n"));
    }
}