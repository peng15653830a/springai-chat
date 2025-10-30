package com.example.converter;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ModelInfoConverter implements Converter<MultiModelProperties.ModelConfig, ModelInfo> {

  @Override
  public ModelInfo convert(MultiModelProperties.ModelConfig source) {
    ModelInfo info = new ModelInfo();
    info.setId((long) source.getName().hashCode());
    info.setName(source.getName());
    info.setDisplayName(source.getDisplayName());
    info.setMaxTokens(source.getMaxTokens());
    info.setTemperature(source.getTemperature());
    info.setSupportsThinking(source.isSupportsThinking());
    info.setSupportsStreaming(source.isSupportsStreaming());
    info.setAvailable(source.isEnabled());
    info.setSortOrder(source.getSortOrder());
    return info;
  }
}
