package com.example.service.impl;

import com.example.dto.ModelInfo;
import com.example.dto.ProviderInfo;
import com.example.dto.UserModelPreferenceDto;
import com.example.entity.AiModel;
import com.example.entity.AiProvider;
import com.example.entity.UserModelPreference;
import com.example.mapper.AiModelMapper;
import com.example.mapper.AiProviderMapper;
import com.example.mapper.UserModelPreferenceMapper;
import com.example.service.ModelManagementService;
import com.example.service.factory.ModelProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模型管理服务实现类
 * 
 * @author xupeng
 */
@Slf4j
@Service
public class ModelManagementServiceImpl implements ModelManagementService {

    @Autowired
    private AiProviderMapper aiProviderMapper;
    
    @Autowired
    private AiModelMapper aiModelMapper;
    
    @Autowired
    private UserModelPreferenceMapper userModelPreferenceMapper;
    
    @Autowired
    private ModelProviderFactory modelProviderFactory;

    @Override
    public List<ProviderInfo> getAvailableProviders() {
        log.debug("获取所有可用的提供者列表");
        
        // 优先从工厂获取动态注册的提供者信息
        List<ProviderInfo> factoryProviders = modelProviderFactory.getAvailableProviders();
        if (!factoryProviders.isEmpty()) {
            log.info("从工厂获取到 {} 个可用提供者", factoryProviders.size());
            return factoryProviders;
        }
        
        // 备选方案：从数据库获取
        List<AiProvider> dbProviders = aiProviderMapper.findEnabledProviders();
        return dbProviders.stream()
                .filter(AiProvider::isAvailable)
                .map(this::convertToProviderInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelInfo> getProviderModels(String providerName) {
        log.debug("获取提供者 {} 的可用模型列表", providerName);
        
        try {
            // 优先从工厂获取
            return modelProviderFactory.getProvider(providerName).getAvailableModels();
        } catch (Exception e) {
            log.warn("从工厂获取提供者模型失败，尝试从数据库获取: {}", e.getMessage());
            
            // 备选方案：从数据库获取
            AiProvider provider = aiProviderMapper.findByName(providerName);
            if (provider != null && provider.isAvailable()) {
                return aiModelMapper.findAvailableModelInfoByProvider(providerName);
            }
        }
        
        return List.of();
    }

    @Override
    public List<ProviderInfo> getAllAvailableModels() {
        log.debug("获取所有可用模型列表");
        
        List<ProviderInfo> providers = getAvailableProviders();
        
        // 为每个提供者加载模型列表
        providers.forEach(provider -> {
            List<ModelInfo> models = getProviderModels(provider.getName());
            provider.setModels(models);
        });
        
        log.info("总共获取到 {} 个提供者的模型信息", providers.size());
        return providers;
    }

    @Override
    public UserModelPreferenceDto getUserDefaultModel(Long userId) {
        log.debug("获取用户 {} 的默认模型偏好", userId);
        
        if (userId == null) {
            return null;
        }
        
        try {
            UserModelPreferenceDto preference = userModelPreferenceMapper.findUserDefaultModel(userId);
            if (preference != null) {
                log.info("用户 {} 的默认模型: {}-{}", userId, preference.getProviderName(), preference.getModelName());
            }
            return preference;
        } catch (Exception e) {
            log.error("获取用户默认模型失败，用户ID: {}", userId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean saveUserModelPreference(Long userId, String providerName, String modelName, boolean isDefault) {
        log.info("保存用户模型偏好，用户ID: {}, 模型: {}-{}, 是否默认: {}", 
                userId, providerName, modelName, isDefault);
        
        if (userId == null || providerName == null || modelName == null) {
            log.warn("参数不完整，无法保存用户模型偏好");
            return false;
        }
        
        try {
            // 获取提供者和模型ID
            AiProvider provider = aiProviderMapper.findByName(providerName);
            if (provider == null) {
                log.warn("提供者不存在: {}", providerName);
                return false;
            }
            
            AiModel model = aiModelMapper.findByProviderAndName(provider.getId(), modelName);
            if (model == null) {
                log.warn("模型不存在: {}-{}", providerName, modelName);
                return false;
            }
            
            // 如果设置为默认，先清除用户的其他默认设置
            if (isDefault) {
                userModelPreferenceMapper.clearUserDefaults(userId);
            }
            
            // 保存或更新偏好
            UserModelPreference preference = new UserModelPreference();
            preference.setUserId(userId);
            preference.setProviderId(provider.getId());
            preference.setModelId(model.getId());
            preference.setIsDefault(isDefault);
            
            int result = userModelPreferenceMapper.saveOrUpdate(preference);
            
            if (result > 0) {
                log.info("用户模型偏好保存成功");
                return true;
            } else {
                log.warn("用户模型偏好保存失败");
                return false;
            }
            
        } catch (Exception e) {
            log.error("保存用户模型偏好时发生错误", e);
            return false;
        }
    }

    @Override
    public List<UserModelPreferenceDto> getUserModelPreferences(Long userId) {
        log.debug("获取用户 {} 的所有模型偏好", userId);
        
        if (userId == null) {
            return List.of();
        }
        
        try {
            List<UserModelPreferenceDto> preferences = userModelPreferenceMapper.findUserModelPreferences(userId);
            log.info("用户 {} 共有 {} 个模型偏好", userId, preferences.size());
            return preferences;
        } catch (Exception e) {
            log.error("获取用户模型偏好失败，用户ID: {}", userId, e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public boolean deleteUserModelPreference(Long userId, String providerName, String modelName) {
        log.info("删除用户模型偏好，用户ID: {}, 模型: {}-{}", userId, providerName, modelName);
        
        if (userId == null || providerName == null || modelName == null) {
            log.warn("参数不完整，无法删除用户模型偏好");
            return false;
        }
        
        try {
            // 获取提供者和模型ID
            AiProvider provider = aiProviderMapper.findByName(providerName);
            if (provider == null) {
                log.warn("提供者不存在: {}", providerName);
                return false;
            }
            
            AiModel model = aiModelMapper.findByProviderAndName(provider.getId(), modelName);
            if (model == null) {
                log.warn("模型不存在: {}-{}", providerName, modelName);
                return false;
            }
            
            int result = userModelPreferenceMapper.deleteByUserAndProviderAndModel(
                    userId, provider.getId(), model.getId());
            
            if (result > 0) {
                log.info("用户模型偏好删除成功");
                return true;
            } else {
                log.warn("用户模型偏好删除失败，可能不存在");
                return false;
            }
            
        } catch (Exception e) {
            log.error("删除用户模型偏好时发生错误", e);
            return false;
        }
    }

    @Override
    public ModelInfo getModelInfo(String providerName, String modelName) {
        log.debug("获取模型详细信息: {}-{}", providerName, modelName);
        
        try {
            return modelProviderFactory.getProvider(providerName).getModelInfo(modelName);
        } catch (Exception e) {
            log.warn("从工厂获取模型信息失败，尝试从数据库获取: {}", e.getMessage());
            
            // 备选方案：从数据库获取
            AiProvider provider = aiProviderMapper.findByName(providerName);
            if (provider != null) {
                AiModel model = aiModelMapper.findByProviderAndName(provider.getId(), modelName);
                if (model != null) {
                    return convertToModelInfo(model);
                }
            }
        }
        
        return null;
    }

    @Override
    public boolean isModelAvailable(String providerName, String modelName) {
        log.debug("检查模型是否可用: {}-{}", providerName, modelName);
        
        try {
            String fullModelId = modelProviderFactory.getProvider(providerName)
                    .getAvailableModels().stream()
                    .filter(model -> model.getName().equals(modelName))
                    .findFirst()
                    .map(model -> model.getFullModelId(getProviderId(providerName)))
                    .orElse(null);
                    
            return fullModelId != null && modelProviderFactory.isModelAvailable(fullModelId);
        } catch (Exception e) {
            log.warn("检查模型可用性失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将AiProvider转换为ProviderInfo
     */
    private ProviderInfo convertToProviderInfo(AiProvider provider) {
        ProviderInfo info = new ProviderInfo();
        info.setId(provider.getId());
        info.setName(provider.getName());
        info.setDisplayName(provider.getDisplayName());
        info.setAvailable(provider.isAvailable());
        
        // 获取该提供者的模型列表
        List<ModelInfo> models = aiModelMapper.findAvailableModelInfoByProvider(provider.getName());
        info.setModels(models);
        
        return info;
    }

    /**
     * 将AiModel转换为ModelInfo
     */
    private ModelInfo convertToModelInfo(AiModel model) {
        ModelInfo info = new ModelInfo();
        info.setId(model.getId());
        info.setName(model.getName());
        info.setDisplayName(model.getDisplayName());
        info.setMaxTokens(model.getMaxTokens());
        info.setTemperature(model.getTemperature());
        info.setSupportsThinking(model.getSupportsThinking());
        info.setSupportsStreaming(model.getSupportsStreaming());
        info.setAvailable(model.isAvailable());
        info.setSortOrder(model.getSortOrder());
        return info;
    }

    /**
     * 获取提供者ID（临时实现）
     */
    private Long getProviderId(String providerName) {
        AiProvider provider = aiProviderMapper.findByName(providerName);
        return provider != null ? provider.getId() : (long) providerName.hashCode();
    }
}