package com.example.streaming;

import com.example.config.MultiModelProperties;
import com.example.integration.ai.greatwall.GreatWallChatOptions;
import com.example.manager.ChatClientManager;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.ChatOptionsFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatModuleOptionsFactory implements ChatOptionsFactory {

  private final MultiModelProperties multiModelProperties;
  private final ChatClientManager chatClientManager;

  private static final String PROVIDER_GREATWALL = "greatwall";

  @Override
  public org.springframework.ai.chat.prompt.ChatOptions build(
      String provider, String model, TextStreamRequest request) {

    MultiModelProperties.ProviderConfig p = multiModelProperties.getProviders().get(provider);
    MultiModelProperties.ModelConfig m = null;
    if (p != null && p.getModels() != null) {
      m = p.getModels().stream().filter(x -> model.equals(x.getName())).findFirst().orElse(null);
    }

    double temperature =
        request.getTemperature() != null
            ? request.getTemperature()
            : (m != null && m.getTemperature() != null
                ? m.getTemperature().doubleValue()
                : multiModelProperties.getDefaults().getTemperature().doubleValue());
    Integer maxTokens =
        request.getMaxTokens() != null
            ? request.getMaxTokens()
            : (m != null && m.getMaxTokens() != null
                ? m.getMaxTokens()
                : multiModelProperties.getDefaults().getMaxTokens());

    if (PROVIDER_GREATWALL.equalsIgnoreCase(provider)) {
      GreatWallChatOptions opts = GreatWallChatOptions.create();
      opts.setModel(model);
      opts.setTemperature(temperature);
      opts.setMaxTokens(maxTokens);
      boolean enableThinking =
          request.isDeepThinking() && chatClientManager.supportsThinking(provider, model);
      opts.setEnableThinking(enableThinking);
      return opts;
    }

    var builder =
        OpenAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens);

    boolean modelSupportsTools = m != null && m.isSupportsTools();
    if (modelSupportsTools && request.isSearchEnabled()) {
      try {
        java.lang.reflect.Method toolChoiceMethod = null;
        for (var method : builder.getClass().getMethods()) {
          if (method.getName().equals("toolChoice") && method.getParameterCount() == 1) {
            toolChoiceMethod = method;
            break;
          }
        }
        if (toolChoiceMethod != null) {
          String paramType = toolChoiceMethod.getParameterTypes()[0].getName();
          if (paramType.equals("java.lang.String")) {
            toolChoiceMethod.invoke(builder, "auto");
          } else {
            try {
              toolChoiceMethod.invoke(builder, "auto");
            } catch (Exception ignore) {
            }
          }
        }
      } catch (Exception ignore) {
      }
    }

    return builder.build();
  }
}

