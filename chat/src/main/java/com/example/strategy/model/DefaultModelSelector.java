package com.example.strategy.model;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.dto.common.UserModelPreferenceDto;
import com.example.service.ChatModelCatalogService;
import com.example.service.UserModelPreferenceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 默认的模型选择器实现
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultModelSelector implements ModelSelector {

  private final ChatModelCatalogService chatModelCatalogService;
  private final com.example.client.UnifiedChatClientManager unifiedChatClientManager;
  private final UserModelPreferenceService userModelPreferenceService;
  private final MultiModelProperties properties;

  @Override
  public String getActualProviderName(String providerName) {
    if (providerName != null && !providerName.trim().isEmpty()) {
      // 检查指定的提供者是否可用
      if (unifiedChatClientManager.isAvailable(providerName)) {
        return providerName;
      } else {
        log.warn("指定的提供者 {} 不可用，使用默认提供者", providerName);
      }
    }
    // 使用默认提供者
    return properties.getDefaultProvider();
  }

  @Override
  public String getActualModelName(String providerName, String modelName) {
    if (modelName != null && !modelName.trim().isEmpty()) {
      // 验证指定的模型是否可用
      List<ModelInfo> availableModels = chatModelCatalogService.getModels(providerName);
      boolean modelExists =
          availableModels.stream().anyMatch(model -> modelName.equals(model.getName()));

      if (modelExists) {
        log.debug("使用指定模型: {}", modelName);
        return modelName;
      } else {
        log.warn("指定的模型 {} 不可用，使用默认模型", modelName);
      }
    }

    // 使用该提供者的第一个可用模型
    List<ModelInfo> availableModels = chatModelCatalogService.getModels(providerName);
    if (!availableModels.isEmpty()) {
      String defaultModel = availableModels.get(0).getName();
      log.debug("使用默认模型: {}", defaultModel);
      return defaultModel;
    }

    // 如果都没有，使用全局默认模型
    return properties.getDefaultModel();
  }

  @Override
  public ModelSelection selectModelForUser(Long userId, String providerName, String modelName) {
    log.debug("为用户 {} 选择模型，指定提供者: {}, 指定模型: {}", userId, providerName, modelName);

    // 如果明确指定了提供者和模型，直接使用
    if (providerName != null && modelName != null) {
      String actualProviderName = getActualProviderName(providerName);
      String actualModelName = getActualModelName(actualProviderName, modelName);
      return new ModelSelection(actualProviderName, actualModelName);
    }

    // 尝试获取用户偏好
    if (userId != null) {
      try {
        UserModelPreferenceDto userPreference =
            userModelPreferenceService.getUserDefaultModel(userId);
        if (userPreference != null) {
          String preferredProvider = userPreference.getProviderName();
          String preferredModel = userPreference.getModelName();

          log.debug("使用用户偏好模型: {} - {}", preferredProvider, preferredModel);

          String actualProviderName = getActualProviderName(preferredProvider);
          String actualModelName = getActualModelName(actualProviderName, preferredModel);
          return new ModelSelection(actualProviderName, actualModelName);
        }
      } catch (Exception e) {
        log.warn("获取用户模型偏好失败，使用默认模型", e);
      }
    }

    // 使用系统默认选择
    String actualProviderName = getActualProviderName(providerName);
    String actualModelName = getActualModelName(actualProviderName, modelName);
    return new ModelSelection(actualProviderName, actualModelName);
  }
}
