package com.example.service.factory;

import com.example.config.MultiModelProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 模型提供者工厂 - 简化Provider和Model管理
 *
 * @author xupeng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelProviderFactory {

  private final Map<String, ChatModel> chatModels;
  private final MultiModelProperties multiModelProperties;

  /**
   * 获取可用的Provider列表
   *
   * @return 可用的Provider名称列表
   */
  public List<String> getAvailableProviders() {
    return multiModelProperties.getProviders().entrySet().stream()
        .filter(entry -> entry.getValue().isEnabled())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  /**
   * 根据Provider名称获取ChatModel
   *
   * @param providerName Provider名称
   * @return ChatModel实例
   * @throws IllegalArgumentException 如果Provider不存在或不可用
   */
  public ChatModel getChatModel(String providerName) {
    if (providerName == null || providerName.trim().isEmpty()) {
      throw new IllegalArgumentException("Provider名称不能为空");
    }

    // 检查Provider是否启用
    if (!isProviderEnabled(providerName)) {
      throw new IllegalArgumentException("Provider未启用: " + providerName);
    }

    // 使用标准命名规则查找ChatModel
    String standardBeanName = providerName.toLowerCase() + "ChatModel";

    // 精确匹配
    ChatModel chatModel = chatModels.get(standardBeanName);
    if (chatModel != null) {
      log.debug("✅ 找到ChatModel: {} for provider: {}", standardBeanName, providerName);
      return chatModel;
    }

    // 兼容性匹配
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

  /**
   * 检查Provider是否启用
   *
   * @param providerName Provider名称
   * @return 是否启用
   */
  public boolean isProviderEnabled(String providerName) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName.toLowerCase());
    return providerConfig != null && providerConfig.isEnabled();
  }

  /**
   * 获取Provider的显示名称
   *
   * @param providerName Provider名称
   * @return 显示名称
   */
  public String getProviderDisplayName(String providerName) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName.toLowerCase());
    return providerConfig != null ? providerConfig.getDisplayName() : providerName;
  }

  /**
   * 获取所有可用ChatModel的信息
   *
   * @return ChatModel信息映射
   */
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

  /** 模型信息记录类 */
  public record ModelInfo(
      String providerName,
      String displayName,
      List<MultiModelProperties.ModelConfig> models,
      boolean enabled) {}
}
