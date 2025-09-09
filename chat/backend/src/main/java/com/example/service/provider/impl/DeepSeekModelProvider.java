package com.example.service.provider.impl;

import com.example.config.MultiModelProperties;
import com.example.service.provider.AbstractModelRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DeepSeek模型注册表实现
 * 只负责提供模型信息和元数据
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class DeepSeekModelProvider extends AbstractModelRegistry {

    private static final String PROVIDER_NAME = "DeepSeek";
    private static final String DISPLAY_NAME = "DeepSeek";

    public DeepSeekModelProvider(MultiModelProperties multiModelProperties) {
        super(multiModelProperties);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}