package com.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultiModelPropertiesTest {

    private MultiModelProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MultiModelProperties();
    }

    @Test
    void testDefaultValues() {
        // When & Then
        assertTrue(properties.isEnabled());
        assertEquals("qwen", properties.getDefaultProvider());
        assertEquals("Qwen/Qwen3-235B-A22B-Thinking-2507", properties.getDefaultModel());
        assertNotNull(properties.getDefaults());
        assertNotNull(properties.getProviders());
        assertTrue(properties.getProviders().isEmpty());
    }

    @Test
    void testGlobalDefaults() {
        // Given
        MultiModelProperties.GlobalDefaults defaults = properties.getDefaults();

        // When & Then
        assertEquals(BigDecimal.valueOf(0.7), defaults.getTemperature());
        assertEquals(Integer.valueOf(2000), defaults.getMaxTokens());
        assertEquals(Integer.valueOf(30000), defaults.getTimeoutMs());
        assertEquals(Integer.valueOf(50000), defaults.getThinkingBudget());
        assertTrue(defaults.isStreamEnabled());
    }

    @Test
    void testProviderConfig() {
        // Given
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();

        // When & Then
        assertTrue(providerConfig.isEnabled());
        assertNull(providerConfig.getDisplayName());
        assertNull(providerConfig.getApiKey());
        assertNull(providerConfig.getBaseUrl());
        assertEquals(Integer.valueOf(10000), providerConfig.getConnectTimeoutMs());
        assertEquals(Integer.valueOf(30000), providerConfig.getReadTimeoutMs());
        assertNull(providerConfig.getModels());
    }

    @Test
    void testModelConfig() {
        // Given
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();

        // When & Then
        assertNull(modelConfig.getName());
        assertNull(modelConfig.getDisplayName());
        assertNull(modelConfig.getMaxTokens());
        assertNull(modelConfig.getTemperature());
        assertFalse(modelConfig.isSupportsThinking());
        assertTrue(modelConfig.isSupportsStreaming());
        assertTrue(modelConfig.isEnabled());
        assertEquals(Integer.valueOf(0), modelConfig.getSortOrder());
        assertNull(modelConfig.getThinkingBudget());
        assertFalse(modelConfig.isNonStandardApi());
        assertNull(modelConfig.getApiRunId());
        assertEquals("guest", modelConfig.getTpuidPrefix());
    }

    @Test
    void testGetApiKey() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setApiKey("TEST_API_KEY");
        providers.put("testProvider", providerConfig);
        
        properties.getProviders().putAll(providers);
        
        // When
        String apiKey = properties.getApiKey("testProvider");
        
        // Then
        // 修正：getApiKey方法直接返回配置的API密钥，不再从环境变量获取
        assertEquals("TEST_API_KEY", apiKey);
    }

    @Test
    void testGetApiKey_ProviderNotFound() {
        // When
        String apiKey = properties.getApiKey("nonExistentProvider");

        // Then
        assertNull(apiKey);
    }

    @Test
    void testGetApiKey_NoApiKeyEnv() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        // Not setting apiKeyEnv
        providers.put("testProvider", providerConfig);
        
        properties.getProviders().putAll(providers);

        // When
        String apiKey = properties.getApiKey("testProvider");

        // Then
        assertNull(apiKey);
    }

    @Test
    void testIsProviderAvailable() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setApiKey("TEST_API_KEY");
        providers.put("testProvider", providerConfig);
        
        properties.getProviders().putAll(providers);

        // When
        boolean available = properties.isProviderAvailable("testProvider");

        // Then
        // In test environment, this should return true even without API key
        assertTrue(available);
    }

    @Test
    void testIsProviderAvailable_ProviderNotFound() {
        // When
        boolean available = properties.isProviderAvailable("nonExistentProvider");

        // Then
        assertFalse(available);
    }

    @Test
    void testIsProviderAvailable_ProviderDisabled() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(false);
        providers.put("disabledProvider", providerConfig);
        
        properties.getProviders().putAll(providers);

        // When
        boolean available = properties.isProviderAvailable("disabledProvider");

        // Then
        assertFalse(available);
    }

    @Test
    void testIsProviderAvailable_NoApiKey() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        // Not setting apiKeyEnv
        providers.put("testProvider", providerConfig);
        
        properties.getProviders().putAll(providers);

        // When
        boolean available = properties.isProviderAvailable("testProvider");

        // Then
        // In test environment, this should return true even without API key
        assertTrue(available);
    }
}