package com.example.streaming;

import com.example.config.ChatOptionsProperties;
import com.example.config.MultiModelProperties;
import com.example.integration.ai.greatwall.GreatWallChatOptions;
import com.example.manager.ChatClientManager;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.AbstractChatOptionsFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatModuleOptionsFactory extends AbstractChatOptionsFactory {

  private final ChatClientManager chatClientManager;

  public ChatModuleOptionsFactory(
      MultiModelProperties multiModelProperties,
      ChatOptionsProperties chatOptionsProperties,
      ChatClientManager chatClientManager) {
    super(multiModelProperties, chatOptionsProperties);
    this.chatClientManager = chatClientManager;
  }

  private static final String PROVIDER_GREATWALL = "greatwall";

  @Override
  protected ChatOptions buildProviderSpecificOptions(
      String provider,
      String model,
      double temperature,
      Integer maxTokens,
      Double topP,
      TextStreamRequest request,
      MultiModelProperties.ModelConfig modelConfig) {

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

    if (topP != null) {
      builder.topP(topP);
    }

    boolean modelSupportsTools = modelConfig != null && modelConfig.isSupportsTools();
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

