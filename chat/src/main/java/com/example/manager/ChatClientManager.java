package com.example.manager;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.service.catalog.ModelCatalogService;
import com.example.service.factory.ModelProviderFactory;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Chat模块的模型目录服务
 * 提供模型列表、模型信息查询等功能
 * 
 * <p>注意：ChatClient管理已迁移到 {@link com.example.client.UnifiedChatClientManager}
 *
 * @author xupeng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatClientManager implements ModelCatalogService {

  private final MultiModelProperties properties;
  private final ModelProviderFactory modelProviderFactory;

  @Override
  public List<ModelInfo> listModels() {
    List<ModelInfo> models =
        properties.getProviders().values().stream()
            .filter(Objects::nonNull)
            .filter(MultiModelProperties.ProviderConfig::isEnabled)
            .flatMap(
                providerConfig ->
                    providerConfig.getModels() == null
                        ? java.util.stream.Stream.empty()
                        : providerConfig.getModels().stream()
                            .filter(MultiModelProperties.ModelConfig::isEnabled))
            .map(this::convertToModelInfo)
            .collect(Collectors.toList());

    models.sort(ModelCatalogService.super::compare);
    return models;
  }

  @Override
  public java.util.Optional<ModelInfo> findByName(String modelName) {
    if (modelName == null || modelName.trim().isEmpty()) {
      return java.util.Optional.empty();
    }

    return properties.getProviders().values().stream()
        .filter(Objects::nonNull)
        .filter(MultiModelProperties.ProviderConfig::isEnabled)
        .flatMap(
            providerConfig ->
                providerConfig.getModels() == null
                    ? java.util.stream.Stream.empty()
                    : providerConfig.getModels().stream()
                        .filter(MultiModelProperties.ModelConfig::isEnabled))
        .filter(model -> modelName.equals(model.getName()))
        .findFirst()
        .map(this::convertToModelInfo);
  }

  public List<ModelInfo> getModels(String provider) {
    MultiModelProperties.ProviderConfig config = properties.getProviders().get(provider);

    if (config == null || !config.isEnabled()) {
      return Collections.emptyList();
    }

    return config.getModels().stream()
        .filter(MultiModelProperties.ModelConfig::isEnabled)
        .map(this::convertToModelInfo)
        .collect(Collectors.toList());
  }

  public ModelInfo getModelInfo(String provider, String modelName) {
    MultiModelProperties.ProviderConfig config = properties.getProviders().get(provider);

    if (config == null) {
      return null;
    }

    return config.getModels().stream()
        .filter(model -> model.getName().equals(modelName))
        .map(this::convertToModelInfo)
        .findFirst()
        .orElse(null);
  }

  public boolean supportsThinking(String provider, String modelName) {
    return getModelConfig(provider, modelName)
        .map(MultiModelProperties.ModelConfig::isSupportsThinking)
        .orElse(false);
  }

  public boolean supportsStreaming(String provider, String modelName) {
    return getModelConfig(provider, modelName)
        .map(MultiModelProperties.ModelConfig::isSupportsStreaming)
        .orElse(true);
  }

  private ModelInfo convertToModelInfo(MultiModelProperties.ModelConfig config) {
    ModelInfo info = new ModelInfo();
    info.setId((long) config.getName().hashCode());
    info.setName(config.getName());
    info.setDisplayName(config.getDisplayName());
    info.setMaxTokens(config.getMaxTokens());
    info.setTemperature(config.getTemperature());
    info.setSupportsThinking(config.isSupportsThinking());
    info.setSupportsStreaming(config.isSupportsStreaming());
    info.setAvailable(config.isEnabled());
    info.setSortOrder(config.getSortOrder());
    return info;
  }

  private Optional<MultiModelProperties.ModelConfig> getModelConfig(
      String provider, String modelName) {
    MultiModelProperties.ProviderConfig providerConfig = properties.getProviders().get(provider);

    if (providerConfig == null) {
      return Optional.empty();
    }

    return providerConfig.getModels().stream()
        .filter(model -> model.getName().equals(modelName))
        .findFirst();
  }
}
