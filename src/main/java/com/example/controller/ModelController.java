package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.ModelInfo;
import com.example.dto.ProviderInfo;
import com.example.dto.UserModelPreferenceDto;
import com.example.service.ModelManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型管理控制器
 * 
 * @author xupeng
 */
@Slf4j
@RestController
@RequestMapping("/api/models")
@CrossOrigin(origins = "*")
public class ModelController {

    @Autowired
    private ModelManagementService modelManagementService;

    /**
     * 获取所有可用的模型提供者
     * 
     * @return 提供者列表
     */
    @GetMapping("/providers")
    public ApiResponse<List<ProviderInfo>> getAvailableProviders() {
        log.info("获取可用提供者列表");
        
        try {
            List<ProviderInfo> providers = modelManagementService.getAvailableProviders();
            log.info("成功获取 {} 个可用提供者", providers.size());
            return ApiResponse.success(providers);
        } catch (Exception e) {
            log.error("获取可用提供者失败", e);
            return ApiResponse.error("获取提供者列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定提供者的模型列表
     * 
     * @param providerName 提供者名称
     * @return 模型列表
     */
    @GetMapping("/providers/{providerName}/models")
    public ApiResponse<List<ModelInfo>> getProviderModels(@PathVariable String providerName) {
        log.info("获取提供者 {} 的模型列表", providerName);
        
        try {
            List<ModelInfo> models = modelManagementService.getProviderModels(providerName);
            log.info("成功获取提供者 {} 的 {} 个模型", providerName, models.size());
            return ApiResponse.success(models);
        } catch (Exception e) {
            log.error("获取提供者模型失败，提供者: {}", providerName, e);
            return ApiResponse.error("获取模型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有可用模型（按提供者分组）
     * 
     * @return 按提供者分组的模型列表
     */
    @GetMapping("/available")
    public ApiResponse<List<ProviderInfo>> getAllAvailableModels() {
        log.info("获取所有可用模型列表");
        
        try {
            List<ProviderInfo> providersWithModels = modelManagementService.getAllAvailableModels();
            int totalModels = providersWithModels.stream()
                    .mapToInt(provider -> provider.getModels() != null ? provider.getModels().size() : 0)
                    .sum();
            log.info("成功获取 {} 个提供者的 {} 个模型", providersWithModels.size(), totalModels);
            return ApiResponse.success(providersWithModels);
        } catch (Exception e) {
            log.error("获取所有可用模型失败", e);
            return ApiResponse.error("获取模型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取模型详细信息
     * 
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 模型详细信息
     */
    @GetMapping("/providers/{providerName}/models/{modelName}")
    public ApiResponse<ModelInfo> getModelInfo(@PathVariable String providerName, 
                                              @PathVariable String modelName) {
        log.info("获取模型详细信息: {}-{}", providerName, modelName);
        
        try {
            ModelInfo modelInfo = modelManagementService.getModelInfo(providerName, modelName);
            if (modelInfo != null) {
                log.info("成功获取模型信息: {}", modelInfo.getDisplayName());
                return ApiResponse.success(modelInfo);
            } else {
                log.warn("模型不存在: {}-{}", providerName, modelName);
                return ApiResponse.error("模型不存在");
            }
        } catch (Exception e) {
            log.error("获取模型信息失败: {}-{}", providerName, modelName, e);
            return ApiResponse.error("获取模型信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查模型是否可用
     * 
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 是否可用
     */
    @GetMapping("/providers/{providerName}/models/{modelName}/available")
    public ApiResponse<Boolean> checkModelAvailability(@PathVariable String providerName, 
                                                      @PathVariable String modelName) {
        log.debug("检查模型是否可用: {}-{}", providerName, modelName);
        
        try {
            boolean available = modelManagementService.isModelAvailable(providerName, modelName);
            log.debug("模型 {}-{} 可用性: {}", providerName, modelName, available);
            return ApiResponse.success(available);
        } catch (Exception e) {
            log.error("检查模型可用性失败: {}-{}", providerName, modelName, e);
            return ApiResponse.error("检查模型可用性失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的默认模型偏好
     * 
     * @param userId 用户ID
     * @return 用户默认模型偏好
     */
    @GetMapping("/users/{userId}/default")
    public ApiResponse<UserModelPreferenceDto> getUserDefaultModel(@PathVariable Long userId) {
        log.info("获取用户 {} 的默认模型偏好", userId);
        
        try {
            UserModelPreferenceDto preference = modelManagementService.getUserDefaultModel(userId);
            if (preference != null) {
                log.info("用户 {} 的默认模型: {}-{}", userId, preference.getProviderName(), preference.getModelName());
                return ApiResponse.success(preference);
            } else {
                log.info("用户 {} 没有设置默认模型", userId);
                return ApiResponse.success(null);
            }
        } catch (Exception e) {
            log.error("获取用户默认模型失败，用户ID: {}", userId, e);
            return ApiResponse.error("获取用户默认模型失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有模型偏好
     * 
     * @param userId 用户ID
     * @return 用户模型偏好列表
     */
    @GetMapping("/users/{userId}/preferences")
    public ApiResponse<List<UserModelPreferenceDto>> getUserModelPreferences(@PathVariable Long userId) {
        log.info("获取用户 {} 的所有模型偏好", userId);
        
        try {
            List<UserModelPreferenceDto> preferences = modelManagementService.getUserModelPreferences(userId);
            log.info("用户 {} 共有 {} 个模型偏好", userId, preferences.size());
            return ApiResponse.success(preferences);
        } catch (Exception e) {
            log.error("获取用户模型偏好失败，用户ID: {}", userId, e);
            return ApiResponse.error("获取用户模型偏好失败: " + e.getMessage());
        }
    }

    /**
     * 保存用户模型偏好
     * 
     * @param userId 用户ID
     * @param request 模型偏好请求
     * @return 是否保存成功
     */
    @PostMapping("/users/{userId}/preferences")
    public ApiResponse<Boolean> saveUserModelPreference(@PathVariable Long userId, 
                                                       @RequestBody UserModelPreferenceRequest request) {
        log.info("保存用户模型偏好，用户ID: {}, 模型: {}-{}, 是否默认: {}", 
                userId, request.getProviderName(), request.getModelName(), request.getIsDefault());
        
        try {
            boolean success = modelManagementService.saveUserModelPreference(
                    userId, request.getProviderName(), request.getModelName(), request.getIsDefault());
            
            if (success) {
                log.info("用户模型偏好保存成功");
                return ApiResponse.success(true);
            } else {
                log.warn("用户模型偏好保存失败");
                return ApiResponse.error("保存模型偏好失败");
            }
        } catch (Exception e) {
            log.error("保存用户模型偏好时发生错误", e);
            return ApiResponse.error("保存模型偏好失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户模型偏好
     * 
     * @param userId 用户ID
     * @param providerName 提供者名称
     * @param modelName 模型名称
     * @return 是否删除成功
     */
    @DeleteMapping("/users/{userId}/preferences/{providerName}/{modelName}")
    public ApiResponse<Boolean> deleteUserModelPreference(@PathVariable Long userId, 
                                                         @PathVariable String providerName,
                                                         @PathVariable String modelName) {
        log.info("删除用户模型偏好，用户ID: {}, 模型: {}-{}", userId, providerName, modelName);
        
        try {
            boolean success = modelManagementService.deleteUserModelPreference(userId, providerName, modelName);
            
            if (success) {
                log.info("用户模型偏好删除成功");
                return ApiResponse.success(true);
            } else {
                log.warn("用户模型偏好删除失败");
                return ApiResponse.error("删除模型偏好失败");
            }
        } catch (Exception e) {
            log.error("删除用户模型偏好时发生错误", e);
            return ApiResponse.error("删除模型偏好失败: " + e.getMessage());
        }
    }

    /**
     * 用户模型偏好请求DTO
     */
    public static class UserModelPreferenceRequest {
        private String providerName;
        private String modelName;
        private Boolean isDefault;

        // Getters and Setters
        public String getProviderName() {
            return providerName;
        }

        public void setProviderName(String providerName) {
            this.providerName = providerName;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }
    }
}