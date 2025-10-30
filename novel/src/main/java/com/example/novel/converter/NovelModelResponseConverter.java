package com.example.novel.converter;

import com.example.dto.common.ModelInfo;
import com.example.novel.dto.response.ModelListResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NovelModelResponseConverter
    implements Converter<ModelInfo, ModelListResponse.ModelInfo> {

  @Override
  public ModelListResponse.ModelInfo convert(ModelInfo source) {
    ModelListResponse.ModelInfo dto = new ModelListResponse.ModelInfo();
    dto.setName(source.getName());
    dto.setDisplayName(
        source.getDisplayName() != null ? source.getDisplayName() : source.getName());
    dto.setAvailable(source.getAvailable() == null ? Boolean.TRUE : source.getAvailable());
    dto.setSize(null);
    dto.setModifiedAt(null);
    return dto;
  }
}
