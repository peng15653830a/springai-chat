package com.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}