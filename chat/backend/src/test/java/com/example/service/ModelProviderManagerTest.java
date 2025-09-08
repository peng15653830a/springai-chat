package com.example.service;

import com.example.config.MultiModelProperties;
import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import com.example.service.provider.ModelProvider;
import com.example.service.provider.ModelProviderManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelProviderManagerTest {

    @Mock
    private MultiModelProperties multiModelProperties;

    @Mock
    private ModelProvider provider1;

    @Mock
    private ModelProvider provider2;

    private ModelProviderManager manager;
    private List<ModelProvider> providers;

    @BeforeEach
    void setUp() {
        providers = Arrays.asList(provider1, provider2);
        manager = new ModelProviderManager(providers, multiModelProperties);
    }

    @Test
    void testGetProvider_Success() {
        // Given
        String providerName = "testProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.getProvider(providerName);

        // Then
        assertEquals(provider1, result);
        verify(provider1).getProviderName();
        verify(provider1).isAvailable();
    }

    @Test
    void testGetProvider_ProviderNotFound() {
        // Given
        String providerName = "nonExistentProvider";
        when(provider1.getProviderName()).thenReturn("provider1");
        when(provider2.getProviderName()).thenReturn("provider2");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.getProvider(providerName));
    }

    @Test
    void testGetProvider_ProviderNotAvailable() {
        // Given
        String providerName = "unavailableProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.getProvider(providerName));
    }

    @Test
    void testSelectProvider_WithProviderName() {
        // Given
        String providerName = "testProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(providerName, null);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testSelectProvider_WithModelName() {
        // Given
        String modelName = "testModel";
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName(modelName);
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelProvider result = manager.selectProvider(null, modelName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testSelectProvider_WithDefaultProvider() {
        // Given
        String defaultProvider = "defaultProvider";
        when(multiModelProperties.getDefaultProvider()).thenReturn(defaultProvider);
        when(provider1.getProviderName()).thenReturn(defaultProvider);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(null, null);

        // Then
        assertEquals(provider1, result);
        verify(multiModelProperties).getDefaultProvider();
    }

    @Test
    void testSelectProvider_FirstAvailableProvider() {
        // Given
        when(multiModelProperties.getDefaultProvider()).thenReturn(null);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(null, null);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testSelectProvider_NoAvailableProviders() {
        // Given
        when(multiModelProperties.getDefaultProvider()).thenReturn(null);
        when(provider1.isAvailable()).thenReturn(false);
        when(provider2.isAvailable()).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.selectProvider(null, null));
    }

    @Test
    void testGetAvailableProviders() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(false);
        
        ProviderInfo providerInfo1 = new ProviderInfo();
        providerInfo1.setName("provider1");
        providerInfo1.setDisplayName("Provider 1");
        
        when(provider1.getProviderName()).thenReturn("provider1");
        when(provider1.getDisplayName()).thenReturn("Provider 1");
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList());

        // When
        List<ProviderInfo> result = manager.getAvailableProviders();

        // Then
        assertEquals(1, result.size());
        assertEquals("provider1", result.get(0).getName());
        assertEquals("Provider 1", result.get(0).getDisplayName());
    }

    @Test
    void testGetAllAvailableModels() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(true);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("model1");
        ModelInfo model2 = new ModelInfo();
        model2.setName("model2");
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(model1));
        when(provider2.getAvailableModels()).thenReturn(Arrays.asList(model2));

        // When
        List<ModelInfo> result = manager.getAllAvailableModels();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "model1".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "model2".equals(m.getName())));
    }

    @Test
    void testGetProviderModels() {
        // Given
        String providerName = "testProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("model1");
        ModelInfo model2 = new ModelInfo();
        model2.setName("model2");
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(model1, model2));

        // When
        List<ModelInfo> result = manager.getProviderModels(providerName);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "model1".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "model2".equals(m.getName())));
    }

    @Test
    void testIsProviderAvailable_True() {
        // Given
        String providerName = "availableProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        boolean result = manager.isProviderAvailable(providerName);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsProviderAvailable_False() {
        // Given
        String providerName = "unavailableProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(false);

        // When
        boolean result = manager.isProviderAvailable(providerName);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsModelAvailable_True() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName(modelName);
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        boolean result = manager.isModelAvailable(providerName, modelName);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsModelAvailable_False() {
        // Given
        String providerName = "testProvider";
        String modelName = "nonExistentModel";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("otherModel");
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        boolean result = manager.isModelAvailable(providerName, modelName);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetModelInfo() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName(modelName);
        modelInfo.setDisplayName("Test Model");
        
        when(provider1.getModelInfo(modelName)).thenReturn(modelInfo);

        // When
        ModelInfo result = manager.getModelInfo(providerName, modelName);

        // Then
        assertNotNull(result);
        assertEquals(modelName, result.getName());
        assertEquals("Test Model", result.getDisplayName());
    }

    @Test
    void testGetProviderCount() {
        // When
        int result = manager.getProviderCount();

        // Then
        assertEquals(2, result);
    }

    @Test
    void testGetAvailableProviderCount() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(false);

        // When
        int result = manager.getAvailableProviderCount();

        // Then
        assertEquals(1, result);
    }

    @Test
    void testGetProvider_WithNullName() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.getProvider(null));
    }

    @Test
    void testGetProvider_WithEmptyName() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.getProvider(""));
    }

    @Test
    void testGetProvider_WithWhitespaceName() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.getProvider("   "));
    }

    @Test
    void testSelectProvider_WithInvalidProviderName() {
        // Given
        String providerName = "invalidProvider";
        when(provider1.getProviderName()).thenReturn("provider1");
        when(provider2.getProviderName()).thenReturn("provider2");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.selectProvider(providerName, null));
    }

    @Test
    void testSelectProvider_WithInvalidModelName() {
        // Given
        String modelName = "invalidModel";
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(true);
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList());
        when(provider2.getAvailableModels()).thenReturn(Arrays.asList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.selectProvider(null, modelName));
    }

    @Test
    void testSelectProvider_WithUnavailableDefaultProvider() {
        // Given
        String defaultProvider = "unavailableProvider";
        when(multiModelProperties.getDefaultProvider()).thenReturn(defaultProvider);
        when(provider1.getProviderName()).thenReturn(defaultProvider);
        when(provider1.isAvailable()).thenReturn(false);
        when(provider2.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(null, null);

        // Then
        assertEquals(provider2, result);
    }

    @Test
    void testGetProviderModels_ProviderNotFound() {
        // Given
        String providerName = "nonExistentProvider";
        when(provider1.getProviderName()).thenReturn("provider1");
        when(provider2.getProviderName()).thenReturn("provider2");

        // When
        List<ModelInfo> result = manager.getProviderModels(providerName);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetProviderModels_ProviderNotAvailable() {
        // Given
        String providerName = "unavailableProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(false);

        // When
        List<ModelInfo> result = manager.getProviderModels(providerName);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetProviderModels_Exception() {
        // Given
        String providerName = "testProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        when(provider1.getAvailableModels()).thenThrow(new RuntimeException("Provider error"));

        // When
        List<ModelInfo> result = manager.getProviderModels(providerName);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsProviderAvailable_Exception() {
        // Given
        String providerName = "testProvider";
        when(provider1.getProviderName()).thenThrow(new RuntimeException("Provider error"));

        // When
        boolean result = manager.isProviderAvailable(providerName);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsModelAvailable_Exception() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenThrow(new RuntimeException("Provider error"));

        // When
        boolean result = manager.isModelAvailable(providerName, modelName);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetModelInfo_Exception() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        when(provider1.getModelInfo(modelName)).thenThrow(new RuntimeException("Provider error"));

        // When
        ModelInfo result = manager.getModelInfo(providerName, modelName);

        // Then
        assertNull(result);
    }

    @Test
    void testGetAllAvailableModels_Exception() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(true);
        
        when(provider1.getAvailableModels()).thenThrow(new RuntimeException("Provider1 error"));
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("model2");
        when(provider2.getAvailableModels()).thenReturn(Arrays.asList(model2));

        // When
        List<ModelInfo> result = manager.getAllAvailableModels();

        // Then
        assertEquals(1, result.size());
        assertEquals("model2", result.get(0).getName());
    }

    @Test
    void testGetAvailableProviders_Exception() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(true);
        
        when(provider1.getProviderName()).thenThrow(new RuntimeException("Provider1 error"));
        
        ProviderInfo providerInfo2 = new ProviderInfo();
        providerInfo2.setName("provider2");
        providerInfo2.setDisplayName("Provider 2");
        when(provider2.getProviderName()).thenReturn("provider2");
        when(provider2.getDisplayName()).thenReturn("Provider 2");
        when(provider2.getAvailableModels()).thenReturn(Arrays.asList());

        // When
        List<ProviderInfo> result = manager.getAvailableProviders();

        // Then
        assertEquals(1, result.size());
        assertEquals("provider2", result.get(0).getName());
        assertEquals("Provider 2", result.get(0).getDisplayName());
    }

    @Test
    void testGetProvider_WithSpecialCharacters() {
        // Given
        String providerName = "特殊-provider🌟";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.getProvider(providerName);

        // Then
        assertEquals(provider1, result);
        verify(provider1).getProviderName();
        verify(provider1).isAvailable();
    }

    @Test
    void testGetProvider_WithLongName() {
        // Given
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("long-provider-name");
        }
        String providerName = longName.toString();
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.getProvider(providerName);

        // Then
        assertEquals(provider1, result);
        verify(provider1).getProviderName();
        verify(provider1).isAvailable();
    }

    @Test
    void testGetProvider_WithUnicode() {
        // Given
        String providerName = "Unicode提供者测试";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.getProvider(providerName);

        // Then
        assertEquals(provider1, result);
        verify(provider1).getProviderName();
        verify(provider1).isAvailable();
    }

    @Test
    void testSelectProvider_WithSpecialCharacters() {
        // Given
        String providerName = "特殊-provider🌟";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(providerName, null);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testSelectProvider_WithLongValues() {
        // Given
        StringBuilder longProviderName = new StringBuilder();
        StringBuilder longModelName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longProviderName.append("long-provider-name");
            longModelName.append("long-model-name");
        }
        String providerName = longProviderName.toString();
        String modelName = longModelName.toString();
        
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(providerName, modelName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testSelectProvider_WithUnicode() {
        // Given
        String providerName = "Unicode提供者测试";
        String modelName = "Unicode模型测试";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(providerName, modelName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testGetAvailableProviders_WithSpecialCharacters() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(false);
        
        ProviderInfo providerInfo1 = new ProviderInfo();
        providerInfo1.setName("特殊-provider🌟");
        providerInfo1.setDisplayName("特殊提供者🌟");
        
        when(provider1.getProviderName()).thenReturn("特殊-provider🌟");
        when(provider1.getDisplayName()).thenReturn("特殊提供者🌟");
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList());

        // When
        List<ProviderInfo> result = manager.getAvailableProviders();

        // Then
        assertEquals(1, result.size());
        assertEquals("特殊-provider🌟", result.get(0).getName());
        assertEquals("特殊提供者🌟", result.get(0).getDisplayName());
    }

    @Test
    void testGetAllAvailableModels_WithSpecialCharacters() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(true);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("特殊-model🌟");
        ModelInfo model2 = new ModelInfo();
        model2.setName("特殊-model2🔍");
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(model1));
        when(provider2.getAvailableModels()).thenReturn(Arrays.asList(model2));

        // When
        List<ModelInfo> result = manager.getAllAvailableModels();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "特殊-model🌟".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "特殊-model2🔍".equals(m.getName())));
    }

    @Test
    void testGetProviderModels_WithSpecialCharacters() {
        // Given
        String providerName = "特殊-provider🌟";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("特殊-model🌟");
        ModelInfo model2 = new ModelInfo();
        model2.setName("特殊-model2🔍");
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(model1, model2));

        // When
        List<ModelInfo> result = manager.getProviderModels(providerName);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "特殊-model🌟".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "特殊-model2🔍".equals(m.getName())));
    }

    @Test
    void testIsProviderAvailable_WithSpecialCharacters() {
        // Given
        String providerName = "特殊-provider🌟";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        boolean result = manager.isProviderAvailable(providerName);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsModelAvailable_WithSpecialCharacters() {
        // Given
        String providerName = "特殊-provider🌟";
        String modelName = "特殊-model🌟";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName(modelName);
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        boolean result = manager.isModelAvailable(providerName, modelName);

        // Then
        assertTrue(result);
    }

    @Test
    void testGetModelInfo_WithSpecialCharacters() {
        // Given
        String providerName = "特殊-provider🌟";
        String modelName = "特殊-model🌟";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName(modelName);
        modelInfo.setDisplayName("特殊模型🌟");
        
        when(provider1.getModelInfo(modelName)).thenReturn(modelInfo);

        // When
        ModelInfo result = manager.getModelInfo(providerName, modelName);

        // Then
        assertNotNull(result);
        assertEquals(modelName, result.getName());
        assertEquals("特殊模型🌟", result.getDisplayName());
    }

    @Test
    void testGetProvider_WithZeroId() {
        // Given
        String providerName = "provider0";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.getProvider(providerName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testGetProvider_WithNegativeId() {
        // Given
        String providerName = "provider-1";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.getProvider(providerName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testSelectProvider_WithZeroValues() {
        // Given
        String providerName = "provider0";
        String modelName = "model0";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(providerName, modelName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testSelectProvider_WithNegativeValues() {
        // Given
        String providerName = "provider-1";
        String modelName = "model-1";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(providerName, modelName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testGetAvailableProviders_EmptyResult() {
        // Given
        when(provider1.isAvailable()).thenReturn(false);
        when(provider2.isAvailable()).thenReturn(false);

        // When
        List<ProviderInfo> result = manager.getAvailableProviders();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllAvailableModels_EmptyResult() {
        // Given
        when(provider1.isAvailable()).thenReturn(false);
        when(provider2.isAvailable()).thenReturn(false);

        // When
        List<ModelInfo> result = manager.getAllAvailableModels();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetProviderModels_EmptyResult() {
        // Given
        String providerName = "emptyProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList());

        // When
        List<ModelInfo> result = manager.getProviderModels(providerName);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsProviderAvailable_EmptyName() {
        // Given
        String providerName = "";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        boolean result = manager.isProviderAvailable(providerName);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsModelAvailable_EmptyNames() {
        // Given
        String providerName = "";
        String modelName = "";

        // When
        boolean result = manager.isModelAvailable(providerName, modelName);

        // Then
        assertTrue(result);
    }

    @Test
    void testGetModelInfo_EmptyNames() {
        // Given
        String providerName = "";
        String modelName = "";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> manager.getModelInfo(providerName, modelName));
    }

    @Test
    void testSelectProvider_WithWhitespaceNames() {
        // Given
        String providerName = "  provider  ";
        String modelName = "  model  ";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);

        // When
        ModelProvider result = manager.selectProvider(providerName, modelName);

        // Then
        assertEquals(provider1, result);
    }

    @Test
    void testGetAvailableProviders_WithMultipleProviders() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        when(provider2.isAvailable()).thenReturn(true);
        
        ProviderInfo providerInfo1 = new ProviderInfo();
        providerInfo1.setName("provider1");
        providerInfo1.setDisplayName("Provider 1");
        
        ProviderInfo providerInfo2 = new ProviderInfo();
        providerInfo2.setName("provider2");
        providerInfo2.setDisplayName("Provider 2");
        
        when(provider1.getProviderName()).thenReturn("provider1");
        when(provider1.getDisplayName()).thenReturn("Provider 1");
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList());
        
        when(provider2.getProviderName()).thenReturn("provider2");
        when(provider2.getDisplayName()).thenReturn("Provider 2");
        when(provider2.getAvailableModels()).thenReturn(Arrays.asList());

        // When
        List<ProviderInfo> result = manager.getAvailableProviders();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> "provider1".equals(p.getName())));
        assertTrue(result.stream().anyMatch(p -> "provider2".equals(p.getName())));
    }

    @Test
    void testGetAllAvailableModels_WithMultipleModels() {
        // Given
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("model1");
        ModelInfo model2 = new ModelInfo();
        model2.setName("model2");
        ModelInfo model3 = new ModelInfo();
        model3.setName("model3");
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(model1, model2, model3));

        // When
        List<ModelInfo> result = manager.getAllAvailableModels();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(m -> "model1".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "model2".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "model3".equals(m.getName())));
    }

    @Test
    void testGetProviderModels_WithMultipleModels() {
        // Given
        String providerName = "multiModelProvider";
        when(provider1.getProviderName()).thenReturn(providerName);
        when(provider1.isAvailable()).thenReturn(true);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("model1");
        ModelInfo model2 = new ModelInfo();
        model2.setName("model2");
        ModelInfo model3 = new ModelInfo();
        model3.setName("model3");
        
        when(provider1.getAvailableModels()).thenReturn(Arrays.asList(model1, model2, model3));

        // When
        List<ModelInfo> result = manager.getProviderModels(providerName);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(m -> "model1".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "model2".equals(m.getName())));
        assertTrue(result.stream().anyMatch(m -> "model3".equals(m.getName())));
    }
}