package com.example.strategy.model;

/**
 * 模型选择器接口：根据用户偏好和可用性选择提供者/模型。
 */
public interface ModelSelector {

  String getActualProviderName(String providerName);

  String getActualModelName(String providerName, String modelName);

  ModelSelection selectModelForUser(Long userId, String providerName, String modelName);

  record ModelSelection(String providerName, String modelName) {}
}

