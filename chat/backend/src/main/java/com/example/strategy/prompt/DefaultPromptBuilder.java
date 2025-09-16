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
        // 明确以空行开启助手回答，有助于大模型在开头就按GFM起始新行输出
        prompt.append("User: ").append(currentMessage).append("\n");
        prompt.append("Assistant:\n\n");
        
        String finalPrompt = prompt.toString();
        log.debug("构建的提示词长度: {} 字符", finalPrompt.length());
        
        return finalPrompt;
    }

    @Override
    public String buildSystemPrompt() {
        return """
你是一个智能助手，请以清晰、可读的 Markdown 输出答案（无需使用 HTML）。遵循以下“宽松原则”：

原则：
- 先给出简短的自然段总览，直接进入主题；除非用户明确要求，不要在开头使用总标题。
- 如需分结构，使用二级及以下标题，保持篇幅适度，避免过度格式化。
- 表格、列表等按常规 Markdown 书写即可，优先保证可读性与信息准确性。
- 如果对排版不确定，优先使用自然段清晰表达，再视需要添加简单的列表或小节。

风格：准确、简洁、有条理；需要最新信息时调用搜索工具；必要时在结尾列出参考来源；不确定时如实说明并给出建议。
""";
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
