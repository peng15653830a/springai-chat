package com.example.strategy.model;

import lombok.extern.slf4j.Slf4j;

/**
 * 简单的模型选择器实现 - 不依赖复杂的配置和服务
 * 适用于单一Provider场景（如Novel模块的Ollama）
 */
@Slf4j
public class SimpleModelSelector implements ModelSelector {

  private final String defaultProvider;
  private final String defaultModel;

  public SimpleModelSelector(String defaultProvider, String defaultModel) {
    this.defaultProvider = defaultProvider;
    this.defaultModel = defaultModel;
  }

  @Override
  public String getActualProviderName(String providerName) {
    if (providerName != null && !providerName.trim().isEmpty()) {
      return providerName;
    }
    return defaultProvider;
  }

  @Override
  public String getActualModelName(String providerName, String modelName) {
    if (modelName != null && !modelName.trim().isEmpty()) {
      return modelName;
    }
    log.debug("未指定模型，使用默认模型: {}", defaultModel);
    return defaultModel;
  }

  @Override
  public ModelSelection selectModelForUser(Long userId, String providerName, String modelName) {
    String actualProvider = getActualProviderName(providerName);
    String actualModel = getActualModelName(actualProvider, modelName);

    log.debug("选择模型: {} -> {}", actualProvider, actualModel);
    return new ModelSelection(actualProvider, actualModel);
  }
}
