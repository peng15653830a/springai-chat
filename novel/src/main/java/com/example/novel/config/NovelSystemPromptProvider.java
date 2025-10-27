package com.example.novel.config;

import com.example.client.SystemPromptProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Novel模块的SystemPromptProvider实现
 * 提供专门用于长文本创作的system prompt
 */
@Component
@ConditionalOnProperty(
    prefix = "novel.system-prompt",
    name = "custom-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class NovelSystemPromptProvider implements SystemPromptProvider {

  @Override
  public String getSystemPrompt(String provider) {
    return """
        你是一个专业的长文本创作助手，擅长小说、剧本、散文等各类文学创作。
        
        核心能力：
        - 故事构思、文本创作、素材整合、风格模仿
        
        创作原则：
        - 自然流畅、适度分段、细节丰富、逻辑连贯
        
        交互方式：
        - 大纲→结构化规划；续写→延续风格与情节；润色→保持原意优化表达
        """.trim();
  }
}
