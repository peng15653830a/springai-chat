package com.example.novel.streaming;

import com.example.config.MultiModelProperties;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.ChatOptionsFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

/**
 * Novel模块的ChatOptions工厂
 * - 对 Ollama 使用 OllamaOptions
 * - 其他 Provider 走通用的 OpenAiChatOptions（适配OpenAI风格API的模型）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NovelOptionsFactory implements ChatOptionsFactory {

  private final MultiModelProperties multiModelProperties;

  @Override
  public ChatOptions build(String provider, String model, TextStreamRequest request) {
    double temperature =
        request.getTemperature() != null
            ? request.getTemperature()
            : multiModelProperties.getDefaults().getTemperature().doubleValue();
    Integer maxTokens =
        request.getMaxTokens() != null
            ? request.getMaxTokens()
            : multiModelProperties.getDefaults().getMaxTokens();

    if ("ollama".equalsIgnoreCase(provider)) {
      Double topP = request.getTopP();
      return OllamaOptions.builder()
          .model(model)
          .temperature(temperature)
          .numPredict(maxTokens)
          .topP(topP)
          .build();
    }

    return OpenAiChatOptions.builder()
        .model(model)
        .temperature(temperature)
        .maxTokens(maxTokens)
        .build();
  }
}
