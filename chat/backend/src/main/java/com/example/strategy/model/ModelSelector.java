package com.example.strategy.model;

/**
 * 模型选择器接口 负责根据用户偏好和模型可用性选择合适的AI模型
 *
 * @author xupeng
 */
public interface ModelSelector {

  /**
   * 获取实际使用的提供者名称
   *
   * @param providerName 指定的提供者名称（可选）
   * @return 实际使用的提供者名称
   */
  String getActualProviderName(String providerName);

  /**
   * 获取实际的模型名称
   *
   * @param providerName 提供者名称
   * @param modelName 指定的模型名称（可选）
   * @return 实际使用的模型名称
   */
  String getActualModelName(String providerName, String modelName);

  /**
   * 根据用户偏好选择模型
   *
   * @param userId 用户ID
   * @param providerName 指定的提供者名称（可选）
   * @param modelName 指定的模型名称（可选）
   * @return 选中的模型提供者和模型名称
   */
  ModelSelection selectModelForUser(Long userId, String providerName, String modelName);

  /** 模型选择结果 */
  record ModelSelection(String providerName, String modelName) {}
}
