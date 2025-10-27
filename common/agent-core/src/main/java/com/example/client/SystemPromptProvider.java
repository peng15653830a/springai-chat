package com.example.client;

/**
 * System Prompt提供者接口
 * 允许不同模块（chat、novel）提供自定义的system prompt
 */
public interface SystemPromptProvider {

  /**
   * 获取指定provider的system prompt
   *
   * @param provider provider名称（如openai、deepseek等）
   * @return system prompt文本
   */
  String getSystemPrompt(String provider);

  /**
   * 获取默认的system prompt（当provider没有特定配置时使用）
   *
   * @return 默认system prompt
   */
  default String getDefaultSystemPrompt() {
    return """
        你是一个智能AI助手。请以清晰、可读的方式回答用户问题。
        
        原则：
        - 准确理解问题并给出有用答案
        - 按需使用合适的格式（自然段、列表、代码块等）
        - 不确定时诚实说明，不要编造信息
        
        风格：准确、有用、友好
        """.trim();
  }
}
