package com.example.config;

import com.example.client.SystemPromptProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Chat模块的SystemPromptProvider实现
 * 针对不同provider提供定制化的system prompt
 */
@Component
@ConditionalOnProperty(
    prefix = "chat.system-prompt",
    name = "custom-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ChatSystemPromptProvider implements SystemPromptProvider {

  @Value("${chat.system-prompt.max-tool-calls:3}")
  private int maxToolCalls;

  @Override
  public String getSystemPrompt(String provider) {
    if ("deepseek".equalsIgnoreCase(provider)) {
      return buildDeepSeekPrompt();
    }

    return buildDefaultChatPrompt();
  }

  private String buildDeepSeekPrompt() {
    return (
            """
        你是一个智能AI助手。直接、准确回答用户问题。
        
        说明：
        - 如需最新信息，可调用可用的搜索工具（每条用户消息最多调用 %d 次）。
        - 不要对输出施加固定格式要求（例如不要强制使用 Markdown/HTML 等），按内容自然表达即可。
                """.formatted(maxToolCalls))
        .trim();
  }

  private String buildDefaultChatPrompt() {
    return (
            """
        你是一个智能AI助手。请以清晰、可读的 Markdown 作答（无需 HTML）。
        
        原则：
        - 开头先给简短的自然段总览，直接进入主题；非必要不使用总标题。
        - 需要分结构时，使用二级及以下标题，适度组织，避免过度格式化。
        - 列表/表格按常规 Markdown 书写，优先保证可读性与信息准确性。
        - 不确定时优先用自然段清晰表述，再视需要添加简单小节或列表。
        
        能力：
        - 🔍 需要最新信息时调用搜索工具（每条用户消息最多调用 %d 次）。
        - 💭 准确理解问题并给出有用答案。
        
        风格：准确、有用、友好；必要时在结尾列出参考来源。
                """.formatted(maxToolCalls))
        .trim();
  }
}
