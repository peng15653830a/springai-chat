package com.example.service.catalog;

import com.example.dto.common.ModelInfo;
import java.util.List;
import java.util.Optional;

/**
 * 模型目录服务：统一查询所有 provider 与模型元数据。
 */
public interface ModelCatalogService {

  /**
   * 获取当前可用的模型列表。
   */
  List<ModelInfo> listModels();

  /**
   * 根据模型名称查询元信息。
   */
  Optional<ModelInfo> findByName(String modelName);

  /**
   * 按排序规则返回首选模型。
   */
  default Optional<ModelInfo> defaultModel() {
    return listModels().stream().sorted(this::compare).findFirst();
  }

  /**
   * 若需要自定义排序，可覆盖本比较逻辑。
   */
  default int compare(ModelInfo a, ModelInfo b) {
    int orderCompare = compareNullable(a.getSortOrder(), b.getSortOrder());
    if (orderCompare != 0) {
      return orderCompare;
    }
    return compareNullable(a.getId(), b.getId());
  }

  private static int compareNullable(Number a, Number b) {
    if (a == null && b == null) {
      return 0;
    }
    if (a == null) {
      return 1;
    }
    if (b == null) {
      return -1;
    }
    return Long.compare(a.longValue(), b.longValue());
  }
}
