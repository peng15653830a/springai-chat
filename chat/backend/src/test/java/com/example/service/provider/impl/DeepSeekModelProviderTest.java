package com.example.service.provider.impl;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.service.provider.AbstractModelRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepSeekModelProviderTest {

    @Mock
    private MultiModelProperties multiModelProperties;

    private DeepSeekModelProvider provider;

    @BeforeEach
    void setUp() {
        // 修复构造函数调用，只传递所需的 MultiModelProperties 参数
        provider = new DeepSeekModelProvider(multiModelProperties);
    }

    @Test
    void testGetProviderName() {
        // When
        String providerName = provider.getProviderName();

        // Then
        assertEquals("DeepSeek", providerName);
    }

    @Test
    void testGetDisplayName() {
        // When
        String displayName = provider.getDisplayName();

        // Then
        assertEquals("DeepSeek", displayName);
    }

    @Test
    void testGetAvailableModels_WithValidConfig() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setDisplayName("DeepSeek");

        MultiModelProperties.ModelConfig modelConfig1 = new MultiModelProperties.ModelConfig();
        modelConfig1.setName("deepseek-chat");
        modelConfig1.setDisplayName("DeepSeek Chat");
        modelConfig1.setEnabled(true);
        modelConfig1.setSupportsThinking(true);
        modelConfig1.setSupportsStreaming(true);
        modelConfig1.setMaxTokens(4096);
        modelConfig1.setTemperature(BigDecimal.valueOf(0.7));
        modelConfig1.setSortOrder(1);

        MultiModelProperties.ModelConfig modelConfig2 = new MultiModelProperties.ModelConfig();
        modelConfig2.setName("deepseek-coder");
        modelConfig2.setDisplayName("DeepSeek Coder");
        modelConfig2.setEnabled(true);
        modelConfig2.setSupportsThinking(false);
        modelConfig2.setSupportsStreaming(true);
        modelConfig2.setMaxTokens(8192);
        modelConfig2.setTemperature(BigDecimal.valueOf(0.8));
        modelConfig2.setSortOrder(2);

        providerConfig.setModels(Arrays.asList(modelConfig1, modelConfig2));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.isProviderAvailable("DeepSeek")).thenReturn(true);

        // When
        List<ModelInfo> models = provider.getAvailableModels();

        // Then
        assertNotNull(models);
        assertEquals(2, models.size());

        ModelInfo model1 = models.get(0);
        assertEquals("deepseek-chat", model1.getName());
        assertEquals("DeepSeek Chat", model1.getDisplayName());
        assertEquals(Integer.valueOf(4096), model1.getMaxTokens());
        assertEquals(BigDecimal.valueOf(0.7), model1.getTemperature());
        assertTrue(model1.getSupportsThinking());
        assertTrue(model1.getSupportsStreaming());
        assertTrue(model1.getAvailable());
        assertEquals(Integer.valueOf(1), model1.getSortOrder());

        ModelInfo model2 = models.get(1);
        assertEquals("deepseek-coder", model2.getName());
        assertEquals("DeepSeek Coder", model2.getDisplayName());
        assertEquals(Integer.valueOf(8192), model2.getMaxTokens());
        assertEquals(BigDecimal.valueOf(0.8), model2.getTemperature());
        assertFalse(model2.getSupportsThinking());
        assertTrue(model2.getSupportsStreaming());
        assertTrue(model2.getAvailable());
        assertEquals(Integer.valueOf(2), model2.getSortOrder());
    }

    @Test
    void testGetAvailableModels_WithDisabledProvider() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(false);
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        List<ModelInfo> models = provider.getAvailableModels();

        // Then
        assertNotNull(models);
        assertTrue(models.isEmpty());
    }

    @Test
    void testGetAvailableModels_WithNullProviders() {
        // Given
        when(multiModelProperties.getProviders()).thenReturn(null);

        // When
        List<ModelInfo> models = provider.getAvailableModels();

        // Then
        assertNotNull(models);
        assertTrue(models.isEmpty());
    }

    @Test
    void testGetAvailableModels_WithDisabledModels() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setDisplayName("DeepSeek");

        MultiModelProperties.ModelConfig modelConfig1 = new MultiModelProperties.ModelConfig();
        modelConfig1.setName("deepseek-chat");
        modelConfig1.setDisplayName("DeepSeek Chat");
        modelConfig1.setEnabled(false); // Disabled model

        MultiModelProperties.ModelConfig modelConfig2 = new MultiModelProperties.ModelConfig();
        modelConfig2.setName("deepseek-coder");
        modelConfig2.setDisplayName("DeepSeek Coder");
        modelConfig2.setEnabled(true);

        providerConfig.setModels(Arrays.asList(modelConfig1, modelConfig2));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.isProviderAvailable("DeepSeek")).thenReturn(true);

        // When
        List<ModelInfo> models = provider.getAvailableModels();

        // Then
        assertNotNull(models);
        assertEquals(1, models.size());
        assertEquals("deepseek-coder", models.get(0).getName());
    }

    @Test
    void testIsAvailable_WithValidConfig() {
        // Given
        when(multiModelProperties.isProviderAvailable("DeepSeek")).thenReturn(true);

        // When
        boolean available = provider.isAvailable();

        // Then
        assertTrue(available);
    }

    @Test
    void testIsAvailable_WithInvalidConfig() {
        // Given
        when(multiModelProperties.isProviderAvailable("DeepSeek")).thenReturn(false);

        // When
        boolean available = provider.isAvailable();

        // Then
        assertFalse(available);
    }

    @Test
    void testSupportsThinking_WithSupportedModel() {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setSupportsThinking(true);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsThinking = provider.supportsThinking(modelName);

        // Then
        assertTrue(supportsThinking);
    }

    @Test
    void testSupportsThinking_WithUnsupportedModel() {
        // Given
        String modelName = "deepseek-coder";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setSupportsThinking(false);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsThinking = provider.supportsThinking(modelName);

        // Then
        assertFalse(supportsThinking);
    }

    @Test
    void testSupportsThinking_WithNonExistentModel() {
        // Given
        String modelName = "non-existent-model";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");
        modelConfig.setSupportsThinking(true);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsThinking = provider.supportsThinking(modelName);

        // Then
        assertFalse(supportsThinking);
    }

    @Test
    void testSupportsStreaming_WithSupportedModel() {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setSupportsStreaming(true);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsStreaming = provider.supportsStreaming(modelName);

        // Then
        assertTrue(supportsStreaming);
    }

    @Test
    void testSupportsStreaming_WithUnsupportedModel() {
        // Given
        String modelName = "deepseek-coder";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setSupportsStreaming(false);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsStreaming = provider.supportsStreaming(modelName);

        // Then
        assertFalse(supportsStreaming);
    }

    @Test
    void testSupportsStreaming_WithNonExistentModel() {
        // Given
        String modelName = "non-existent-model";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");
        modelConfig.setSupportsStreaming(true);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsStreaming = provider.supportsStreaming(modelName);

        // Then
        // According to the implementation, non-existent models default to true
        assertTrue(supportsStreaming);
    }

    @Test
    void testGetModelInfo_WithValidModel() {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setDisplayName("DeepSeek Chat");
        modelConfig.setMaxTokens(4096);
        modelConfig.setTemperature(BigDecimal.valueOf(0.7));
        modelConfig.setSupportsThinking(true);
        modelConfig.setSupportsStreaming(true);
        modelConfig.setEnabled(true);
        modelConfig.setSortOrder(1);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.isProviderAvailable("DeepSeek")).thenReturn(true);

        // When
        ModelInfo modelInfo = provider.getModelInfo(modelName);

        // Then
        assertNotNull(modelInfo);
        assertEquals(modelName, modelInfo.getName());
        assertEquals("DeepSeek Chat", modelInfo.getDisplayName());
        assertEquals(Integer.valueOf(4096), modelInfo.getMaxTokens());
        assertEquals(BigDecimal.valueOf(0.7), modelInfo.getTemperature());
        assertTrue(modelInfo.getSupportsThinking());
        assertTrue(modelInfo.getSupportsStreaming());
        assertTrue(modelInfo.getAvailable());
        assertEquals(Integer.valueOf(1), modelInfo.getSortOrder());
    }

    @Test
    void testGetModelInfo_WithNonExistentModel() {
        // Given
        String modelName = "non-existent-model";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        ModelInfo modelInfo = provider.getModelInfo(modelName);

        // Then
        assertNull(modelInfo);
    }

    @Test
    void testConvertToModelInfo() throws Exception {
        // Given
        MultiModelProperties.ModelConfig config = new MultiModelProperties.ModelConfig();
        config.setName("deepseek-chat");
        config.setDisplayName("DeepSeek Chat");
        config.setMaxTokens(4096);
        config.setTemperature(BigDecimal.valueOf(0.7));
        config.setSupportsThinking(true);
        config.setSupportsStreaming(true);
        config.setEnabled(true);
        config.setSortOrder(1);

        when(multiModelProperties.isProviderAvailable("DeepSeek")).thenReturn(true);

        // When
        // Use reflection to access the protected method
        java.lang.reflect.Method method = AbstractModelRegistry.class.getDeclaredMethod("convertToModelInfo", MultiModelProperties.ModelConfig.class);
        method.setAccessible(true);
        ModelInfo result = (ModelInfo) method.invoke(provider, config);

        // Then
        assertNotNull(result);
        assertEquals("deepseek-chat", result.getName());
        assertEquals("DeepSeek Chat", result.getDisplayName());
        assertEquals(Integer.valueOf(4096), result.getMaxTokens());
        assertEquals(BigDecimal.valueOf(0.7), result.getTemperature());
        assertTrue(result.getSupportsThinking());
        assertTrue(result.getSupportsStreaming());
        assertTrue(result.getAvailable());
        assertEquals(Integer.valueOf(1), result.getSortOrder());
    }

    @Test
    void testGetModelConfig_WithValidModel() throws Exception {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setDisplayName("DeepSeek Chat");

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        // Use reflection to access the protected method
        java.lang.reflect.Method method = AbstractModelRegistry.class.getDeclaredMethod("getModelConfig", String.class);
        method.setAccessible(true);
        Optional<MultiModelProperties.ModelConfig> result = (Optional<MultiModelProperties.ModelConfig>) method.invoke(provider, modelName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(modelName, result.get().getName());
        assertEquals("DeepSeek Chat", result.get().getDisplayName());
    }

    @Test
    void testGetModelConfig_WithNonExistentModel() throws Exception {
        // Given
        String modelName = "non-existent-model";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("deepseek-chat");

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        // Use reflection to access the protected method
        java.lang.reflect.Method method = AbstractModelRegistry.class.getDeclaredMethod("getModelConfig", String.class);
        method.setAccessible(true);
        Optional<MultiModelProperties.ModelConfig> result = (Optional<MultiModelProperties.ModelConfig>) method.invoke(provider, modelName);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetDefaultTemperature_WithModelConfig() throws Exception {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.GlobalDefaults defaults = new MultiModelProperties.GlobalDefaults();
        defaults.setTemperature(BigDecimal.valueOf(0.7));
        when(multiModelProperties.getDefaults()).thenReturn(defaults);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setTemperature(BigDecimal.valueOf(0.8));

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        // Use reflection to access the protected method
        java.lang.reflect.Method method = AbstractModelRegistry.class.getDeclaredMethod("getDefaultTemperature", String.class);
        method.setAccessible(true);
        double result = (double) method.invoke(provider, modelName);

        // Then
        assertEquals(0.8, result, 0.001);
    }

    @Test
    void testGetDefaultTemperature_WithoutModelConfig() throws Exception {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.GlobalDefaults defaults = new MultiModelProperties.GlobalDefaults();
        defaults.setTemperature(BigDecimal.valueOf(0.7));
        when(multiModelProperties.getDefaults()).thenReturn(defaults);

        providerConfig.setModels(Collections.emptyList());
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        // Use reflection to access the protected method
        java.lang.reflect.Method method = AbstractModelRegistry.class.getDeclaredMethod("getDefaultTemperature", String.class);
        method.setAccessible(true);
        double result = (double) method.invoke(provider, modelName);

        // Then
        assertEquals(0.7, result, 0.001);
    }

    @Test
    void testGetDefaultMaxTokens_WithModelConfig() throws Exception {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.GlobalDefaults defaults = new MultiModelProperties.GlobalDefaults();
        defaults.setMaxTokens(2048);
        when(multiModelProperties.getDefaults()).thenReturn(defaults);

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setMaxTokens(4096);

        providerConfig.setModels(Collections.singletonList(modelConfig));
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        // Use reflection to access the protected method
        java.lang.reflect.Method method = AbstractModelRegistry.class.getDeclaredMethod("getDefaultMaxTokens", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(provider, modelName);

        // Then
        assertEquals(4096, result);
    }

    @Test
    void testGetDefaultMaxTokens_WithoutModelConfig() throws Exception {
        // Given
        String modelName = "deepseek-chat";
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);

        MultiModelProperties.GlobalDefaults defaults = new MultiModelProperties.GlobalDefaults();
        defaults.setMaxTokens(2048);
        when(multiModelProperties.getDefaults()).thenReturn(defaults);

        providerConfig.setModels(Collections.emptyList());
        providers.put("DeepSeek", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        // Use reflection to access the protected method
        java.lang.reflect.Method method = AbstractModelRegistry.class.getDeclaredMethod("getDefaultMaxTokens", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(provider, modelName);

        // Then
        assertEquals(2048, result);
    }
}