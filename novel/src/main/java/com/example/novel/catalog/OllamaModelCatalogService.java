package com.example.novel.catalog;

import com.example.config.MultiModelProperties;
import com.example.converter.ModelInfoConverter;
import com.example.dto.common.ModelInfo;
import com.example.service.catalog.ModelCatalogService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaModelCatalogService implements ModelCatalogService {

  private final MultiModelProperties properties;
  private final ModelInfoConverter modelInfoConverter;

  @Override
  public List<ModelInfo> listModels() {
    if (properties.getProviders() == null || properties.getProviders().isEmpty()) {
      return Collections.emptyList();
    }
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
            .map(modelInfoConverter::convert)
            .collect(Collectors.toList());
    models.sort(ModelCatalogService.super::compare);
    return models;
  }

  @Override
  public Optional<ModelInfo> findByName(String modelName) {
    if (modelName == null || modelName.trim().isEmpty()) {
      return Optional.empty();
    }
    return listModels().stream().filter(m -> modelName.equals(m.getName())).findFirst();
  }
}
