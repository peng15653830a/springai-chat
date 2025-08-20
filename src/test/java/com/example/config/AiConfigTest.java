package com.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AiConfig测试类
 *
 * @author xupeng
 */
class AiConfigTest {

  private AiConfig aiConfig;

  @BeforeEach
  void setUp() {
    aiConfig = new AiConfig();
  }

  @Test
  void shouldCreateAiConfigWithDefaultValues() {
    // Then
    assertEquals(0.7, aiConfig.getTemperature());
    assertEquals(1000, aiConfig.getMaxTokens());
    assertEquals(30000, aiConfig.getTimeoutMs());
    assertEquals(3, aiConfig.getMaxRetries());
    assertFalse(aiConfig.isStreamEnabled());
    assertNotNull(aiConfig.getHttp());
  }

  @Test
  void shouldSetAndGetApiKey() {
    // Given
    String apiKey = "test-api-key-123";

    // When
    aiConfig.setApiKey(apiKey);

    // Then
    assertEquals(apiKey, aiConfig.getApiKey());
  }

  @Test
  void shouldSetAndGetBaseUrl() {
    // Given
    String baseUrl = "https://api.test.com/v1";

    // When
    aiConfig.setBaseUrl(baseUrl);

    // Then
    assertEquals(baseUrl, aiConfig.getBaseUrl());
  }

  @Test
  void shouldSetAndGetModel() {
    // Given
    String model = "gpt-4";

    // When
    aiConfig.setModel(model);

    // Then
    assertEquals(model, aiConfig.getModel());
  }

  @Test
  void shouldSetAndGetTemperature() {
    // Given
    double temperature = 0.5;

    // When
    aiConfig.setTemperature(temperature);

    // Then
    assertEquals(temperature, aiConfig.getTemperature());
  }

  @Test
  void shouldSetAndGetMaxTokens() {
    // Given
    int maxTokens = 2000;

    // When
    aiConfig.setMaxTokens(maxTokens);

    // Then
    assertEquals(maxTokens, aiConfig.getMaxTokens());
  }

  @Test
  void shouldSetAndGetTimeoutMs() {
    // Given
    int timeout = 60000;

    // When
    aiConfig.setTimeoutMs(timeout);

    // Then
    assertEquals(timeout, aiConfig.getTimeoutMs());
  }

  @Test
  void shouldSetAndGetMaxRetries() {
    // Given
    int maxRetries = 5;

    // When
    aiConfig.setMaxRetries(maxRetries);

    // Then
    assertEquals(maxRetries, aiConfig.getMaxRetries());
  }

  @Test
  void shouldSetAndGetStreamEnabled() {
    // When
    aiConfig.setStreamEnabled(true);

    // Then
    assertTrue(aiConfig.isStreamEnabled());

    // When
    aiConfig.setStreamEnabled(false);

    // Then
    assertFalse(aiConfig.isStreamEnabled());
  }

  @Test
  void shouldSetAndGetHttpConfig() {
    // Given
    AiConfig.HttpConfig httpConfig = new AiConfig.HttpConfig();
    httpConfig.setConnectTimeoutMs(5000);

    // When
    aiConfig.setHttp(httpConfig);

    // Then
    assertEquals(httpConfig, aiConfig.getHttp());
    assertEquals(5000, aiConfig.getHttp().getConnectTimeoutMs());
  }

  @Test
  void shouldValidateValidConfig() {
    // Given
    aiConfig.setApiKey("valid-api-key");
    aiConfig.setBaseUrl("https://api.openai.com/v1");
    aiConfig.setModel("gpt-4");

    // When & Then
    assertTrue(aiConfig.isValid());
  }

  @Test
  void shouldInvalidateConfigWithNullApiKey() {
    // Given
    aiConfig.setApiKey(null);
    aiConfig.setBaseUrl("https://api.openai.com/v1");
    aiConfig.setModel("gpt-4");

    // When & Then
    assertFalse(aiConfig.isValid());
  }

  @Test
  void shouldInvalidateConfigWithEmptyApiKey() {
    // Given
    aiConfig.setApiKey("   ");
    aiConfig.setBaseUrl("https://api.openai.com/v1");
    aiConfig.setModel("gpt-4");

    // When & Then
    assertFalse(aiConfig.isValid());
  }

  @Test
  void shouldInvalidateConfigWithNullBaseUrl() {
    // Given
    aiConfig.setApiKey("valid-api-key");
    aiConfig.setBaseUrl(null);
    aiConfig.setModel("gpt-4");

    // When & Then
    assertFalse(aiConfig.isValid());
  }

  @Test
  void shouldInvalidateConfigWithEmptyBaseUrl() {
    // Given
    aiConfig.setApiKey("valid-api-key");
    aiConfig.setBaseUrl("");
    aiConfig.setModel("gpt-4");

    // When & Then
    assertFalse(aiConfig.isValid());
  }

  @Test
  void shouldInvalidateConfigWithNullModel() {
    // Given
    aiConfig.setApiKey("valid-api-key");
    aiConfig.setBaseUrl("https://api.openai.com/v1");
    aiConfig.setModel(null);

    // When & Then
    assertFalse(aiConfig.isValid());
  }

  @Test
  void shouldInvalidateConfigWithEmptyModel() {
    // Given
    aiConfig.setApiKey("valid-api-key");
    aiConfig.setBaseUrl("https://api.openai.com/v1");
    aiConfig.setModel("  ");

    // When & Then
    assertFalse(aiConfig.isValid());
  }

  @Test
  void shouldGenerateChatApiUrlWithTrailingSlash() {
    // Given
    aiConfig.setBaseUrl("https://api.openai.com/v1/");

    // When
    String chatApiUrl = aiConfig.getChatApiUrl();

    // Then
    assertEquals("https://api.openai.com/v1/chat/completions", chatApiUrl);
  }

  @Test
  void shouldGenerateChatApiUrlWithoutTrailingSlash() {
    // Given
    aiConfig.setBaseUrl("https://api.openai.com/v1");

    // When
    String chatApiUrl = aiConfig.getChatApiUrl();

    // Then
    assertEquals("https://api.openai.com/v1/chat/completions", chatApiUrl);
  }

  @Test
  void shouldCreateChatClient() {
    // Given
    ChatModel chatModel = mock(ChatModel.class);

    // When
    ChatClient chatClient = aiConfig.chatClient(chatModel);

    // Then
    assertNotNull(chatClient);
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setBaseUrl("https://test.com");
    config1.setModel("gpt-4");

    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setBaseUrl("https://test.com");
    config2.setModel("gpt-4");

    AiConfig config3 = new AiConfig();
    config3.setApiKey("different");
    config3.setBaseUrl("https://test.com");
    config3.setModel("gpt-4");

    // Then
    assertEquals(config1, config2);
    assertNotEquals(config1, config3);
    assertEquals(config1.hashCode(), config2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    aiConfig.setApiKey("test-key");
    aiConfig.setBaseUrl("https://test.com");
    aiConfig.setModel("gpt-4");

    // When
    String toString = aiConfig.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("test-key"));
    assertTrue(toString.contains("https://test.com"));
    assertTrue(toString.contains("gpt-4"));
  }

  @Test
  void shouldTestHttpConfigDefaultValues() {
    // Given
    AiConfig.HttpConfig httpConfig = new AiConfig.HttpConfig();

    // Then
    assertEquals(10000, httpConfig.getConnectTimeoutMs());
    assertEquals(30000, httpConfig.getReadTimeoutMs());
    assertEquals("SpringAI-ChatBot/1.0", httpConfig.getUserAgent());
  }

  @Test
  void shouldSetHttpConfigValues() {
    // Given
    AiConfig.HttpConfig httpConfig = new AiConfig.HttpConfig();

    // When
    httpConfig.setConnectTimeoutMs(5000);
    httpConfig.setReadTimeoutMs(60000);
    httpConfig.setUserAgent("CustomAgent/2.0");

    // Then
    assertEquals(5000, httpConfig.getConnectTimeoutMs());
    assertEquals(60000, httpConfig.getReadTimeoutMs());
    assertEquals("CustomAgent/2.0", httpConfig.getUserAgent());
  }

  @Test
  void shouldTestHttpConfigEqualsAndHashCode() {
    // Given
    AiConfig.HttpConfig config1 = new AiConfig.HttpConfig();
    config1.setConnectTimeoutMs(5000);

    AiConfig.HttpConfig config2 = new AiConfig.HttpConfig();
    config2.setConnectTimeoutMs(5000);

    AiConfig.HttpConfig config3 = new AiConfig.HttpConfig();
    config3.setConnectTimeoutMs(10000);

    // Then
    assertEquals(config1, config2);
    assertNotEquals(config1, config3);
    assertEquals(config1.hashCode(), config2.hashCode());
  }

  @Test
  void shouldTestHttpConfigToString() {
    // Given
    AiConfig.HttpConfig httpConfig = new AiConfig.HttpConfig();
    httpConfig.setUserAgent("TestAgent/1.0");

    // When
    String toString = httpConfig.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("TestAgent/1.0"));
  }

  @Test
  void shouldTestAiConfigEqualsWithSameInstance() {
    AiConfig config = new AiConfig();
    config.setApiKey("test");
    assertEquals(config, config); // Same instance reference
  }

  @Test
  void shouldTestAiConfigEqualsWithNull() {
    AiConfig config = new AiConfig();
    assertNotEquals(config, null); // Null comparison
  }

  @Test
  void shouldTestAiConfigEqualsWithDifferentType() {
    AiConfig config = new AiConfig();
    assertNotEquals(config, "not an AiConfig"); // Different type
  }

  @Test
  void shouldTestAiConfigEqualsWithCanEqualFalse() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    
    // Anonymous subclass that overrides canEqual to return false
    AiConfig config2 = new AiConfig() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    config2.setApiKey("test");
    
    assertNotEquals(config1, config2); // canEqual returns false
  }

  @Test
  void shouldTestAiConfigEqualsWithOneNullApiKeyField() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setBaseUrl("url");
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey(null);
    config2.setBaseUrl("url");
    
    assertNotEquals(config1, config2); // One has null apiKey, other doesn't
  }

  @Test
  void shouldTestAiConfigEqualsWithOneNullBaseUrlField() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setBaseUrl("url");
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setBaseUrl(null);
    
    assertNotEquals(config1, config2); // One has null baseUrl, other doesn't
  }

  @Test
  void shouldTestAiConfigEqualsWithOneNullModelField() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setModel("gpt-4");
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setModel(null);
    
    assertNotEquals(config1, config2); // One has null model, other doesn't
  }

  @Test
  void shouldTestAiConfigEqualsWithOneNullHttpField() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setHttp(new AiConfig.HttpConfig());
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setHttp(null);
    
    assertNotEquals(config1, config2); // One has null http, other doesn't
  }

  @Test
  void shouldTestAiConfigHashCodeWithAllNullFields() {
    AiConfig config1 = new AiConfig();
    config1.setHttp(null);
    AiConfig config2 = new AiConfig();
    config2.setHttp(null);
    
    assertEquals(config1.hashCode(), config2.hashCode()); // Both have all null fields
  }

  @Test
  void shouldTestHttpConfigEqualsWithSameInstance() {
    AiConfig.HttpConfig config = new AiConfig.HttpConfig();
    config.setUserAgent("test");
    assertEquals(config, config); // Same instance reference
  }

  @Test
  void shouldTestHttpConfigEqualsWithNull() {
    AiConfig.HttpConfig config = new AiConfig.HttpConfig();
    assertNotEquals(config, null); // Null comparison
  }

  @Test
  void shouldTestHttpConfigEqualsWithDifferentType() {
    AiConfig.HttpConfig config = new AiConfig.HttpConfig();
    assertNotEquals(config, "not an HttpConfig"); // Different type
  }

  @Test
  void shouldTestHttpConfigEqualsWithCanEqualFalse() {
    AiConfig.HttpConfig config1 = new AiConfig.HttpConfig();
    config1.setUserAgent("test");
    
    // Anonymous subclass that overrides canEqual to return false
    AiConfig.HttpConfig config2 = new AiConfig.HttpConfig() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    config2.setUserAgent("test");
    
    assertNotEquals(config1, config2); // canEqual returns false
  }

  @Test
  void shouldTestHttpConfigEqualsWithOneNullUserAgentField() {
    AiConfig.HttpConfig config1 = new AiConfig.HttpConfig();
    config1.setUserAgent("test");
    
    AiConfig.HttpConfig config2 = new AiConfig.HttpConfig();
    config2.setUserAgent(null);
    
    assertNotEquals(config1, config2); // One has null userAgent, other doesn't
  }

  @Test
  void shouldTestHttpConfigHashCodeWithAllNullFields() {
    AiConfig.HttpConfig config1 = new AiConfig.HttpConfig();
    config1.setUserAgent(null);
    AiConfig.HttpConfig config2 = new AiConfig.HttpConfig();
    config2.setUserAgent(null);
    
    assertEquals(config1.hashCode(), config2.hashCode()); // Both have null userAgent
  }

  @Test
  void shouldTestAiConfigEqualsWithAllNullFields() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey(null);
    config1.setBaseUrl(null);
    config1.setModel(null);
    config1.setHttp(null);
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey(null);
    config2.setBaseUrl(null);
    config2.setModel(null);
    config2.setHttp(null);
    
    assertEquals(config1, config2); // Both have all null fields
    assertEquals(config1.hashCode(), config2.hashCode());
  }

  @Test
  void shouldTestAiConfigEqualsWithDifferentTemperature() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setTemperature(0.5);
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setTemperature(0.8);
    
    assertNotEquals(config1, config2); // Different temperature values
  }

  @Test
  void shouldTestAiConfigEqualsWithDifferentMaxTokens() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setMaxTokens(1000);
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setMaxTokens(2000);
    
    assertNotEquals(config1, config2); // Different maxTokens values
  }

  @Test
  void shouldTestAiConfigEqualsWithDifferentTimeoutMs() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setTimeoutMs(30000);
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setTimeoutMs(60000);
    
    assertNotEquals(config1, config2); // Different timeoutMs values
  }

  @Test
  void shouldTestAiConfigEqualsWithDifferentMaxRetries() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setMaxRetries(3);
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setMaxRetries(5);
    
    assertNotEquals(config1, config2); // Different maxRetries values
  }

  @Test
  void shouldTestAiConfigEqualsWithDifferentStreamEnabled() {
    AiConfig config1 = new AiConfig();
    config1.setApiKey("test");
    config1.setStreamEnabled(true);
    
    AiConfig config2 = new AiConfig();
    config2.setApiKey("test");
    config2.setStreamEnabled(false);
    
    assertNotEquals(config1, config2); // Different streamEnabled values
  }

  @Test
  void shouldTestHttpConfigEqualsWithDifferentConnectTimeout() {
    AiConfig.HttpConfig config1 = new AiConfig.HttpConfig();
    config1.setConnectTimeoutMs(5000);
    
    AiConfig.HttpConfig config2 = new AiConfig.HttpConfig();
    config2.setConnectTimeoutMs(10000);
    
    assertNotEquals(config1, config2); // Different connectTimeoutMs values
  }

  @Test
  void shouldTestHttpConfigEqualsWithDifferentReadTimeout() {
    AiConfig.HttpConfig config1 = new AiConfig.HttpConfig();
    config1.setReadTimeoutMs(30000);
    
    AiConfig.HttpConfig config2 = new AiConfig.HttpConfig();
    config2.setReadTimeoutMs(60000);
    
    assertNotEquals(config1, config2); // Different readTimeoutMs values
  }
}