package com.example.service.factory;

import com.example.config.MultiModelProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModelProviderFactory {

  private final Map<String, ChatModel> chatModels;
  private final MultiModelProperties multiModelProperties;

  public List<String> getAvailableProviders() {
    return multiModelProperties.getProviders().entrySet().stream()
        .filter(entry -> entry.getValue().isEnabled())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  public ChatModel getChatModel(String providerName) {
    if (providerName == null || providerName.trim().isEmpty()) {
      throw new IllegalArgumentException("Provider名称不能为空");
    }

    if (!isProviderEnabled(providerName)) {
      throw new IllegalArgumentException("Provider未启用: " + providerName);
    }

    String standardBeanName = providerName.toLowerCase() + "ChatModel";

    ChatModel chatModel = chatModels.get(standardBeanName);
    if (chatModel != null) {
      log.debug("✅ 找到ChatModel: {} for provider: {}", standardBeanName, providerName);
      return chatModel;
    }

    for (Map.Entry<String, ChatModel> entry : chatModels.entrySet()) {
      if (entry.getKey().toLowerCase().contains(providerName.toLowerCase())) {
        log.warn(
            "⚠️ 使用兼容性匹配的ChatModel: {} for provider: {}，建议重命名为: {}",
            entry.getKey(),
            providerName,
            standardBeanName);
        return entry.getValue();
      }
    }

    throw new IllegalArgumentException("找不到Provider的ChatModel: " + providerName);
  }

  public boolean isProviderEnabled(String providerName) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName.toLowerCase());
    return providerConfig != null && providerConfig.isEnabled();
  }

  public String getProviderDisplayName(String providerName) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName.toLowerCase());
    return providerConfig != null ? providerConfig.getDisplayName() : providerName;
  }

  public Map<String, ModelInfo> getAvailableModels() {
    return getAvailableProviders().stream()
        .collect(
            Collectors.toMap(
                provider -> provider,
                provider -> {
                  MultiModelProperties.ProviderConfig config =
                      multiModelProperties.getProviders().get(provider);
                  return new ModelInfo(
                      provider, config.getDisplayName(), config.getModels(), config.isEnabled());
                }));
  }

  public record ModelInfo(
      String providerName,
      String displayName,
      java.util.List<com.example.config.MultiModelProperties.ModelConfig> models,
      boolean enabled) {}
}

