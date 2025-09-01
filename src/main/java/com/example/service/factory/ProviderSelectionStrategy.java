package com.example.service.factory;

import com.example.service.provider.ModelProvider;

import java.util.List;
import java.util.Optional;

/**
 * 模型提供者选择策略接口
 * 定义了不同的提供者选择策略
 *
 * @author xupeng
 */
public interface ProviderSelectionStrategy {

    /**
     * 根据策略选择最佳的模型提供者
     * 
     * @param availableProviders 可用的提供者列表
     * @param providerName 指定的提供者名称（可选）
     * @param modelName 指定的模型名称（可选）
     * @return 选中的提供者
     */
    Optional<ModelProvider> selectProvider(List<ModelProvider> availableProviders, 
                                          String providerName, 
                                          String modelName);

    /**
     * 策略名称
     * 
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 策略描述
     * 
     * @return 策略描述
     */
    String getDescription();
}