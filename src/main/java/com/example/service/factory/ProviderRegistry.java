package com.example.service.factory;

import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import com.example.service.provider.ModelProvider;

import java.util.List;
import java.util.Optional;

/**
 * 模型提供者注册接口
 * 定义了提供者注册和查找的统一接口
 *
 * @author xupeng
 */
public interface ProviderRegistry {

    /**
     * 注册模型提供者
     * 
     * @param provider 提供者实例
     * @throws IllegalArgumentException 如果提供者已存在或无效
     */
    void register(ModelProvider provider);

    /**
     * 注销模型提供者
     * 
     * @param providerName 提供者名称
     */
    void unregister(String providerName);

    /**
     * 根据名称获取提供者
     * 
     * @param providerName 提供者名称
     * @return 提供者实例的Optional包装
     */
    Optional<ModelProvider> findByName(String providerName);

    /**
     * 根据完整模型ID获取提供者
     * 
     * @param fullModelId 完整模型ID (providerId-modelName)
     * @return 提供者实例的Optional包装
     */
    Optional<ModelProvider> findByModelId(String fullModelId);

    /**
     * 获取所有可用的提供者
     * 
     * @return 可用提供者列表
     */
    List<ModelProvider> findAllAvailable();

    /**
     * 获取所有提供者信息
     * 
     * @return 提供者信息列表
     */
    List<ProviderInfo> getAllProviderInfo();

    /**
     * 获取所有可用模型信息
     * 
     * @return 模型信息列表
     */
    List<ModelInfo> getAllModelInfo();

    /**
     * 检查提供者是否已注册且可用
     * 
     * @param providerName 提供者名称
     * @return 是否可用
     */
    boolean isAvailable(String providerName);

    /**
     * 检查模型是否可用
     * 
     * @param fullModelId 完整模型ID
     * @return 是否可用
     */
    boolean isModelAvailable(String fullModelId);

    /**
     * 获取注册的提供者数量
     * 
     * @return 提供者数量
     */
    int size();
}