package com.example.util;

import com.example.config.MultiModelProperties;
import java.util.List;
import java.util.Optional;

public class ModelConfigHelper {

  private ModelConfigHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static MultiModelProperties.ModelConfig getDefaultModelConfig(
      MultiModelProperties multiModelProperties, String providerName) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName);
    if (providerConfig == null || providerConfig.getModels() == null) {
      return null;
    }
    return providerConfig.getModels().stream().findFirst().orElse(null);
  }

  public static Optional<MultiModelProperties.ModelConfig> getModelConfig(
      MultiModelProperties multiModelProperties, String providerName, String modelName) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName);
    if (providerConfig == null || providerConfig.getModels() == null) {
      return Optional.empty();
    }
    return providerConfig.getModels().stream()
        .filter(model -> model.getName().equals(modelName))
        .findFirst();
  }

  public static List<MultiModelProperties.ModelConfig> getEnabledModels(
      MultiModelProperties multiModelProperties, String providerName) {
    MultiModelProperties.ProviderConfig providerConfig =
        multiModelProperties.getProviders().get(providerName);
    if (providerConfig == null
        || !providerConfig.isEnabled()
        || providerConfig.getModels() == null) {
      return List.of();
    }
    return providerConfig.getModels().stream()
        .filter(MultiModelProperties.ModelConfig::isEnabled)
        .toList();
  }

  public static Double getTemperature(
      MultiModelProperties.ModelConfig modelConfig, MultiModelProperties multiModelProperties) {
    if (modelConfig != null && modelConfig.getTemperature() != null) {
      return modelConfig.getTemperature().doubleValue();
    }
    return multiModelProperties.getDefaults().getTemperature().doubleValue();
  }

  public static Integer getMaxTokens(
      MultiModelProperties.ModelConfig modelConfig, MultiModelProperties multiModelProperties) {
    if (modelConfig != null && modelConfig.getMaxTokens() != null) {
      return modelConfig.getMaxTokens();
    }
    return multiModelProperties.getDefaults().getMaxTokens();
  }

  public static Integer getThinkingBudget(
      MultiModelProperties.ModelConfig modelConfig, MultiModelProperties multiModelProperties) {
    if (modelConfig != null && modelConfig.getThinkingBudget() != null) {
      return modelConfig.getThinkingBudget();
    }
    return multiModelProperties.getDefaults().getThinkingBudget();
  }
}
