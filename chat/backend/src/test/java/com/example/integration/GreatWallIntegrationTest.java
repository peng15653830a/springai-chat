package com.example.integration;

import com.example.config.MultiModelProperties;
import com.example.service.provider.impl.GreatWallModelProvider;
import com.example.service.api.impl.GreatWallApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 长城大模型集成测试
 * 验证长城大模型的配置和服务是否正确加载
 * 
 * @author xupeng
 */
@SpringBootTest(classes = com.example.springai.SpringaiApplication.class)
@ActiveProfiles("test")
public class GreatWallIntegrationTest {

    @Autowired(required = false)
    private MultiModelProperties multiModelProperties;
    
    @Autowired(required = false)
    private GreatWallModelProvider greatWallModelProvider;
    
    @Autowired(required = false)
    private GreatWallApiClient greatWallApiClient;

    @Test
    void shouldLoadMultiModelProperties() {
        assertNotNull(multiModelProperties, "MultiModelProperties应该被正确加载");
        assertTrue(multiModelProperties.getProviders().containsKey("greatwall"), 
                  "应该包含长城大模型配置");
        
        MultiModelProperties.ProviderConfig greatWallConfig = 
            multiModelProperties.getProviders().get("greatwall");
        assertNotNull(greatWallConfig, "长城大模型配置不应为空");
        assertEquals("长城大模型", greatWallConfig.getDisplayName(), 
                    "显示名称应为'长城大模型'");
    }

    @Test  
    void shouldLoadGreatWallServices() {
        assertNotNull(greatWallModelProvider, "GreatWallModelProvider应该被正确加载");
        assertNotNull(greatWallApiClient, "GreatWallApiClient应该被正确加载");
        
        assertEquals("greatwall", greatWallModelProvider.getProviderName(), 
                    "提供者名称应为'greatwall'");
        assertEquals("长城大模型", greatWallModelProvider.getDisplayName(), 
                    "显示名称应为'长城大模型'");
    }

    @Test
    void shouldConfigureNonStandardApiModel() {
        MultiModelProperties.ProviderConfig greatWallConfig = 
            multiModelProperties.getProviders().get("greatwall");
        
        assertNotNull(greatWallConfig.getModels(), "模型列表不应为空");
        assertFalse(greatWallConfig.getModels().isEmpty(), "应该至少有一个模型");
        
        MultiModelProperties.ModelConfig model = greatWallConfig.getModels().get(0);
        assertTrue(model.isNonStandardApi(), "应该标记为非标准API");
        assertNotNull(model.getApiRunId(), "API运行ID不应为空");
        assertEquals("guest", model.getTpuidPrefix(), "用户ID前缀应为'guest'");
    }
}