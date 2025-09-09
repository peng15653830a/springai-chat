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
class OpenaiModelProviderTest {

    @Mock
    private EnhancedAiConfig.EnhancedChatClientFactory chatClientFactory;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageService messageService;

    @Mock
    private MultiModelProperties multiModelProperties;

    private OpenaiModelProvider provider;

    @BeforeEach
    void setUp() {
        // 修复构造函数调用，只传递所需的 MultiModelProperties 参数
        provider = new OpenaiModelProvider(multiModelProperties);
    }

    @Test
    void testGetProviderName() {
        // When
        String providerName = provider.getProviderName();

        // Then
        assertEquals("openai", providerName);
    }

    @Test
    void testGetDisplayName() {
        // When
        String displayName = provider.getDisplayName();

        // Then
        assertEquals("OpenAI", displayName);
    }

    @Test
    void testGetAvailableModels_WithValidConfig() {
        // Given
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setDisplayName("OpenAI");

        MultiModelProperties.ModelConfig modelConfig1 = new MultiModelProperties.ModelConfig();
        modelConfig1.setName("gpt-4");
        modelConfig1.setDisplayName("GPT-4");
        modelConfig1.setEnabled(true);
        modelConfig1.setSupportsThinking(false);
        modelConfig1.setSupportsStreaming(true);
        modelConfig1.setMaxTokens(8192);
        modelConfig1.setTemperature(BigDecimal.valueOf(0.7));
        modelConfig1.setSortOrder(1);

        MultiModelProperties.ModelConfig modelConfig2 = new MultiModelProperties.ModelConfig();
        modelConfig2.setName("gpt-3.5-turbo");
        modelConfig2.setDisplayName("GPT-3.5 Turbo");
        modelConfig2.setEnabled(true);
        modelConfig2.setSupportsThinking(false);
        modelConfig2.setSupportsStreaming(true);
        modelConfig2.setMaxTokens(4096);
        modelConfig2.setTemperature(BigDecimal.valueOf(0.7));
        modelConfig2.setSortOrder(2);

        providerConfig.setModels(Arrays.asList(modelConfig1, modelConfig2));
        providers.put("openai", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.isProviderAvailable("openai")).thenReturn(true);

        // When
        List<ModelInfo> models = provider.getAvailableModels();

        // Then
        assertNotNull(models);
        assertEquals(2, models.size());

        ModelInfo model1 = models.get(0);
        assertEquals("gpt-4", model1.getName());
        assertEquals("GPT-4", model1.getDisplayName());
        assertEquals(Integer.valueOf(8192), model1.getMaxTokens());
        assertEquals(BigDecimal.valueOf(0.7), model1.getTemperature());
        assertFalse(model1.getSupportsThinking());
        assertTrue(model1.getSupportsStreaming());
        assertTrue(model1.getAvailable());
        assertEquals(Integer.valueOf(1), model1.getSortOrder());

        ModelInfo model2 = models.get(1);
        assertEquals("gpt-3.5-turbo", model2.getName());
        assertEquals("GPT-3.5 Turbo", model2.getDisplayName());
        assertEquals(Integer.valueOf(4096), model2.getMaxTokens());
        assertEquals(BigDecimal.valueOf(0.7), model2.getTemperature());
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
        providers.put("openai", providerConfig);

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
        providerConfig.setDisplayName("OpenAI");

        MultiModelProperties.ModelConfig modelConfig1 = new MultiModelProperties.ModelConfig();
        modelConfig1.setName("gpt-4");
        modelConfig1.setDisplayName("GPT-4");
        modelConfig1.setEnabled(false); // Disabled model

        MultiModelProperties.ModelConfig modelConfig2 = new MultiModelProperties.ModelConfig();
        modelConfig2.setName("gpt-3.5-turbo");
        modelConfig2.setDisplayName("GPT-3.5 Turbo");
        modelConfig2.setEnabled(true);

        providerConfig.setModels(Arrays.asList(modelConfig1, modelConfig2));
        providers.put("openai", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.isProviderAvailable("openai")).thenReturn(true);

        // When
        List<ModelInfo> models = provider.getAvailableModels();

        // Then
        assertNotNull(models);
        assertEquals(1, models.size());
        assertEquals("gpt-3.5-turbo", models.get(0).getName());
    }

    @Test
    void testIsAvailable_WithValidConfig() {
        // Given
        when(multiModelProperties.isProviderAvailable("openai")).thenReturn(true);

        // When
        boolean available = provider.isAvailable();

        // Then
        assertTrue(available);
    }

    @Test
    void testIsAvailable_WithInvalidConfig() {
        // Given
        when(multiModelProperties.isProviderAvailable("openai")).thenReturn(false);

        // When
        boolean available = provider.isAvailable();

        // Then
        assertFalse(available);
    }

    @Test
    void testSupportsThinking_WithValidModel() {
        // Given
        String modelName = "gpt-4";
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        List<MultiModelProperties.ModelConfig> models = new ArrayList<>();

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setSupportsThinking(true);
        models.add(modelConfig);

        providerConfig.setModels(models);
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put("openai", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsThinking = provider.supportsThinking(modelName);

        // Then
        assertTrue(supportsThinking);
    }

    @Test
    void testSupportsThinking_WithInvalidModel() {
        // Given
        String modelName = "non-existent-model";
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        List<MultiModelProperties.ModelConfig> models = new ArrayList<>();

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("gpt-4");
        modelConfig.setSupportsThinking(true);
        models.add(modelConfig);

        providerConfig.setModels(models);
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put("openai", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsThinking = provider.supportsThinking(modelName);

        // Then
        assertFalse(supportsThinking);
    }

    @Test
    void testSupportsStreaming_WithValidModel() {
        // Given
        String modelName = "gpt-4";
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        List<MultiModelProperties.ModelConfig> models = new ArrayList<>();

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setSupportsStreaming(true);
        models.add(modelConfig);

        providerConfig.setModels(models);
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put("openai", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        boolean supportsStreaming = provider.supportsStreaming(modelName);

        // Then
        assertTrue(supportsStreaming);
    }

    @Test
    void testSupportsStreaming_WithInvalidModel() {
        // Given
        String modelName = "non-existent-model";
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        List<MultiModelProperties.ModelConfig> models = new ArrayList<>();

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("gpt-4");
        modelConfig.setSupportsStreaming(true);
        models.add(modelConfig);

        providerConfig.setModels(models);
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put("openai", providerConfig);

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
        String modelName = "gpt-4";
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        List<MultiModelProperties.ModelConfig> models = new ArrayList<>();

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setDisplayName("GPT-4");
        modelConfig.setSupportsThinking(false);
        modelConfig.setSupportsStreaming(true);
        modelConfig.setMaxTokens(8192);
        modelConfig.setTemperature(BigDecimal.valueOf(0.7));
        modelConfig.setEnabled(true);
        modelConfig.setSortOrder(1);
        models.add(modelConfig);

        providerConfig.setModels(models);
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put("openai", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.isProviderAvailable("openai")).thenReturn(true);

        // When
        ModelInfo modelInfo = provider.getModelInfo(modelName);

        // Then
        assertNotNull(modelInfo);
        assertEquals(modelName, modelInfo.getName());
        assertEquals("GPT-4", modelInfo.getDisplayName());
        assertEquals(Integer.valueOf(8192), modelInfo.getMaxTokens());
        assertEquals(BigDecimal.valueOf(0.7), modelInfo.getTemperature());
        assertFalse(modelInfo.getSupportsThinking());
        assertTrue(modelInfo.getSupportsStreaming());
        assertTrue(modelInfo.getAvailable());
        assertEquals(Integer.valueOf(1), modelInfo.getSortOrder());
    }

    @Test
    void testGetModelInfo_WithInvalidModel() {
        // Given
        String modelName = "non-existent-model";
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        List<MultiModelProperties.ModelConfig> models = new ArrayList<>();

        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName("gpt-4");
        modelConfig.setDisplayName("GPT-4");
        models.add(modelConfig);

        providerConfig.setModels(models);
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put("openai", providerConfig);

        when(multiModelProperties.getProviders()).thenReturn(providers);

        // When
        ModelInfo modelInfo = provider.getModelInfo(modelName);

        // Then
        assertNull(modelInfo);
    }
}