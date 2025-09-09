package com.example.service.provider.impl;

import com.example.config.MultiModelProperties;
import com.example.service.provider.AbstractModelRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 长城大模型提供者实现
 * 专门处理长城大模型的非标准API格式
 * 
 * @author xupeng
 */
@Slf4j
@Component
public class GreatWallModelProvider extends AbstractModelRegistry {

    private static final String PROVIDER_NAME = "greatwall";
    private static final String DISPLAY_NAME = "长城大模型";
    
    public GreatWallModelProvider(MultiModelProperties multiModelProperties) {
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