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
        
        // 仅添加可选的搜索上下文（系统提示与历史由 ChatClient.defaultSystem + Memory 注入）
        // 1) 添加搜索上下文（如果有）
        if (searchContext != null && !searchContext.trim().isEmpty()) {
            prompt.append("搜索相关信息：\n")
                  .append(searchContext)
                  .append("\n\n");
        }
        // 2) 仅添加当前用户消息（历史由 Memory 注入）
        // 明确以空行开启助手回答，有助于大模型在开头就按GFM起始新行输出
        prompt.append("User: ").append(currentMessage).append("\n");
        prompt.append("Assistant:\n\n");
        
        String finalPrompt = prompt.toString();
        log.debug("构建的提示词长度: {} 字符", finalPrompt.length());
        
        return finalPrompt;
    }

    @Override
    public String buildSystemPrompt() {
        // 系统提示改由 ChatClientManager.defaultSystem 注入
        return "你是一个有用的AI助理。";
    }

    @Override
    public String formatSearchContext(String searchResults) {
        if (searchResults == null || searchResults.trim().isEmpty()) {
            return "";
        }
        
        // 简单格式化搜索结果
        // 过滤空行
        return searchResults.lines()
                .map(line -> "- " + line.trim())
                .filter(line -> line.length() > 2)
                .collect(Collectors.joining("\n"));
    }
}
