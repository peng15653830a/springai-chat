package com.example.config;

import com.example.ai.chat.DeepSeekChatModel;
import com.example.ai.chat.GreatWallChatModel;
import com.example.service.api.impl.DeepSeekApiClient;
import com.example.service.api.impl.GreatWallApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedAiConfigTest {

    @Mock
    private MultiModelProperties multiModelProperties;

    @Mock
    private GreatWallApiClient greatWallApiClient;

    @Mock
    private DeepSeekApiClient deepSeekApiClient;

    private EnhancedAiConfig config;

    @BeforeEach
    void setUp() {
        config = new EnhancedAiConfig(multiModelProperties, greatWallApiClient, deepSeekApiClient);
    }

    @Test
    void testEnhancedChatClientFactoryCreation() {
        // When
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();

        // Then
        assertNotNull(factory);
    }

    @Test
    void testCreateChatModel_ProviderNotAvailable() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "unavailableProvider";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(false);
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> factory.getChatModel(providerName, modelName));
        assertEquals("Provider not available: " + providerName, exception.getMessage());
    }

    @Test
    void testCreateChatModel_ApiKeyNotFound() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "testProvider";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn(null);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> factory.getChatModel(providerName, modelName));
        assertEquals("API key not found for provider: " + providerName, exception.getMessage());
    }

    @Test
    void testCreateGreatWallChatModel() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        assertTrue(model instanceof GreatWallChatModel);
    }

    @Test
    void testCreateDeepSeekChatModel() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "deepseek";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        assertTrue(model instanceof DeepSeekChatModel);
    }

    @Test
    void testCreateOpenAiCompatibleChatModel() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "openai";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("http://test-base-url");
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        // We can't easily test the specific type due to mocking limitations
    }

    @Test
    void testCreateQwenChatModel() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "qwen";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("http://test-base-url");
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        // We can't easily test the specific type due to mocking limitations
    }

    @Test
    void testCreateKimi2ChatModel() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "kimi2";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl("http://test-base-url");
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        // We can't easily test the specific type due to mocking limitations
    }

    @Test
    void testGetModelConfig_ModelNotFound() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "testProvider";
        String modelName = "nonExistentModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setModels(new ArrayList<>()); // Empty model list
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNull(model);
    }

    @Test
    void testGetChatClient() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatClient client = factory.getChatClient(providerName, modelName);
        
        // Then
        assertNotNull(client);
    }

    @Test
    void testCreateChatModel_DefaultOptions() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config without specific model config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setModels(new ArrayList<>()); // Empty model list
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        
        // Mock global defaults
        MultiModelProperties.GlobalDefaults defaults = new MultiModelProperties.GlobalDefaults();
        defaults.setTemperature(java.math.BigDecimal.valueOf(0.8));
        defaults.setMaxTokens(1500);
        when(multiModelProperties.getDefaults()).thenReturn(defaults);
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        assertTrue(model instanceof GreatWallChatModel);
    }

    @Test
    void testCreateChatModel_WithNullModels() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config with null models
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setModels(null); // Null model list
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        assertTrue(model instanceof GreatWallChatModel);
    }

    @Test
    void testCreateChatModel_WithNullProvider() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "nonexistent";
        String modelName = "testModel";
        
        // Mock with empty providers map
        when(multiModelProperties.getProviders()).thenReturn(new HashMap<>());
        
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> factory.getChatModel(providerName, modelName));
        assertEquals("Provider not available: " + providerName, exception.getMessage());
    }

    @Test
    void testCreateChatModel_EmptyApiKey() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn(""); // Empty API key
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> factory.getChatModel(providerName, modelName));
        assertEquals("API key not found for provider: " + providerName, exception.getMessage());
    }

    @Test
    void testCreateChatModel_NullApiKey() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn(null); // Null API key
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> factory.getChatModel(providerName, modelName));
        assertEquals("API key not found for provider: " + providerName, exception.getMessage());
    }

    @Test
    void testGetChatClient_Caching() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatClient client1 = factory.getChatClient(providerName, modelName);
        ChatClient client2 = factory.getChatClient(providerName, modelName);
        
        // Then
        assertNotNull(client1);
        assertSame(client1, client2); // Should return the same instance from cache
    }

    @Test
    void testGetChatModel_Caching() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        when(multiModelProperties.getDefaults()).thenReturn(new MultiModelProperties.GlobalDefaults());
        
        // When
        ChatModel model1 = factory.getChatModel(providerName, modelName);
        ChatModel model2 = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model1);
        assertSame(model1, model2); // Should return the same instance from cache
    }

    @Test
    void testCreateOpenAiCompatibleChatModel_Exception() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "openai";
        String modelName = "testModel";
        
        // Mock the provider config with null baseUrl to cause exception
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        providerConfig.setBaseUrl(null); // This will cause exception
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> factory.getChatModel(providerName, modelName));
        assertTrue(exception.getMessage().contains("Failed to create ChatModel"));
    }

    @Test
    void testGetModelConfig_NullProviderConfig() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "nullProvider";
        String modelName = "testModel";
        
        // Mock with null provider config
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, null);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        
        // When & Then
        // 由于提供者配置为null，createChatModel方法会抛出IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getChatModel(providerName, modelName);
        });
    }

    @Test
    void testGetTemperature_WithDefaultValues() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config with null temperature in model config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setTemperature(null); // Null temperature
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        
        // Mock global defaults with specific temperature
        MultiModelProperties.GlobalDefaults defaults = new MultiModelProperties.GlobalDefaults();
        defaults.setTemperature(java.math.BigDecimal.valueOf(0.9));
        defaults.setMaxTokens(2000);
        when(multiModelProperties.getDefaults()).thenReturn(defaults);
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        assertTrue(model instanceof GreatWallChatModel);
    }

    @Test
    void testGetMaxTokens_WithDefaultValues() {
        // Given
        EnhancedAiConfig.EnhancedChatClientFactory factory = config.enhancedChatClientFactory();
        String providerName = "greatwall";
        String modelName = "testModel";
        
        // Mock the provider config with null maxTokens in model config
        MultiModelProperties.ProviderConfig providerConfig = new MultiModelProperties.ProviderConfig();
        providerConfig.setEnabled(true);
        MultiModelProperties.ModelConfig modelConfig = new MultiModelProperties.ModelConfig();
        modelConfig.setName(modelName);
        modelConfig.setMaxTokens(null); // Null maxTokens
        providerConfig.setModels(Arrays.asList(modelConfig));
        
        Map<String, MultiModelProperties.ProviderConfig> providers = new HashMap<>();
        providers.put(providerName, providerConfig);
        when(multiModelProperties.getProviders()).thenReturn(providers);
        when(multiModelProperties.getApiKey(providerName)).thenReturn("test-api-key");
        
        // Mock global defaults with specific maxTokens
        MultiModelProperties.GlobalDefaults defaults = new MultiModelProperties.GlobalDefaults();
        defaults.setTemperature(java.math.BigDecimal.valueOf(0.7));
        defaults.setMaxTokens(3000);
        when(multiModelProperties.getDefaults()).thenReturn(defaults);
        
        // When
        ChatModel model = factory.getChatModel(providerName, modelName);
        
        // Then
        assertNotNull(model);
        assertTrue(model instanceof GreatWallChatModel);
    }
}