package com.example.service.impl;

import com.example.dto.common.UserModelPreferenceDto;
import com.example.dto.request.UserModelPreferenceRequest;
import com.example.service.UserModelPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 用户模型偏好服务实现
 * 专注于用户模型偏好的存储和管理，不涉及模型查询逻辑
 * 
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserModelPreferenceServiceImpl implements UserModelPreferenceService {

    // 简化的用户偏好缓存（生产环境可考虑使用Redis）
    private final ConcurrentMap<String, String> userDefaultModelCache = new ConcurrentHashMap<>();

    @Override
    public UserModelPreferenceDto getUserDefaultModel(Long userId) {
        log.debug("获取用户 {} 的默认模型偏好", userId);
        
        if (userId == null) {
            return null;
        }
        
        // 从缓存获取默认模型
        String defaultModel = userDefaultModelCache.get(String.valueOf(userId));
        if (defaultModel != null) {
            String[] parts = defaultModel.split(":");
            if (parts.length == 2) {
                UserModelPreferenceDto preference = new UserModelPreferenceDto();
                preference.setUserId(userId);
                preference.setProviderName(parts[0]);
                preference.setModelName(parts[1]);
                preference.setIsDefault(true);
                log.info("用户 {} 的默认模型: {}-{}", userId, parts[0], parts[1]);
                return preference;
            }
        }
        
        return null;
    }

    @Override
    public boolean saveUserModelPreference(UserModelPreferenceRequest request) {
        log.info("保存用户模型偏好: 用户ID={}, 提供者={}, 模型={}", 
                request.getUserId(), request.getProviderName(), request.getModelName());
        
        if (request.getUserId() == null || request.getProviderName() == null || request.getModelName() == null) {
            log.warn("用户模型偏好参数不完整");
            return false;
        }
        
        try {
            String key = String.valueOf(request.getUserId());
            String value = request.getProviderName() + ":" + request.getModelName();
            userDefaultModelCache.put(key, value);
            
            log.info("✅ 用户模型偏好保存成功");
            return true;
        } catch (Exception e) {
            log.error("❌ 保存用户模型偏好失败", e);
            return false;
        }
    }

    @Override
    public List<UserModelPreferenceDto> getUserModelPreferences(Long userId) {
        log.debug("获取用户 {} 的所有模型偏好", userId);
        
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 当前实现只支持一个默认偏好，可以根据需要扩展
        UserModelPreferenceDto defaultPreference = getUserDefaultModel(userId);
        if (defaultPreference != null) {
            return List.of(defaultPreference);
        }
        
        return Collections.emptyList();
    }

    @Override
    public boolean deleteUserModelPreference(com.example.dto.request.DeleteUserModelPreferenceRequest request) {
        log.info("删除用户模型偏好: 用户ID={}, 提供者={}, 模型={}", 
                request.getUserId(), request.getProviderName(), request.getModelName());
        
        if (request.getUserId() == null) {
            log.warn("用户ID不能为空");
            return false;
        }
        
        try {
            String key = String.valueOf(request.getUserId());
            String removed = userDefaultModelCache.remove(key);
            
            if (removed != null) {
                log.info("✅ 删除用户模型偏好成功");
                return true;
            } else {
                log.info("⚠️ 用户 {} 没有设置模型偏好", request.getUserId());
                return false;
            }
        } catch (Exception e) {
            log.error("❌ 删除用户模型偏好失败", e);
            return false;
        }
    }
}